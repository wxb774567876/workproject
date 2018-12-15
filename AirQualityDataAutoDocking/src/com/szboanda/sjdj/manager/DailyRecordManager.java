package com.szboanda.sjdj.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.szboanda.platform.util.BeanUtils;
import com.szboanda.platform.util.exception.PlatformException;
import com.szboanda.sjdj.ConfigurationFactory;
import com.szboanda.sjdj.bean.DailyRecordBean;
import com.szboanda.sjdj.cache.PointCache;
import com.szboanda.sjdj.dao.DailyRecordImpl;
import com.szboanda.sjdj.dao.HourlyRecordImpl;
import com.szboanda.sjdj.utils.CommonUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 空气质量日数据处理类
 * 
 * @Company  深圳博安达软件开发有限公司
 * @author   王贤炳
 * @version  2016年1月8日
 * @since    V0.2.0
 */
public class DailyRecordManager {
	private static DailyRecordImpl dailyImpl = null;
	private static HourlyRecordImpl hourlyImpl = null;
	
	static{
		hourlyImpl = new HourlyRecordImpl();
		dailyImpl = new DailyRecordImpl();
	}
	/**
	 * 
	 * 执行入口
	 *
	 * @author  王贤炳
	 * @throws SQLException 
	 * @since   2015年12月26日
	 */
	public void exec(){
		List<String> allRequestUrl = new ArrayList<String>();
		HourlyRecordManager manager = new HourlyRecordManager();
		List<String> intervalUrls = manager.getRequestUrlInDateInterval("day","yyyy-mm-dd");
		if(!BeanUtils.emptyCollection(intervalUrls)){
			allRequestUrl.addAll(intervalUrls);
		}else{
			String requestUrl = getRequestUrl();
			allRequestUrl.add(requestUrl);
		}
		//获得以往提取数据失败的时间段
		List<String> failedUrls = hourlyImpl.getFailedUrl("1");
		if(!BeanUtils.emptyCollection(failedUrls)){
			allRequestUrl.addAll(failedUrls);
		}
		for(String str : allRequestUrl){
			try {
				String results = HttpRequestManager.sendGet(str);
				dailyImpl.saveDailyData(results, str);
			} catch (Exception e) {
				Connection conn = null;
				try {
					conn = ConfigurationFactory.getInstance().getConnection();
					conn.setAutoCommit(false);
					HourlyRecordManager hourlyManager = new HourlyRecordManager();
					hourlyManager.afterSave(conn, str, 1, false, e);
					conn.commit();
				} catch (SQLException e1) {
					e1.printStackTrace();
				} finally {
					CommonUtils.closeConnection(conn, null, null);;
				}
			}
		}
	}
	
	/**
	 * 获取请求路径
	 * @return
	 *
	 * @author  王贤炳
	 * @since   2016年1月20日
	 * @version V0.2.0
	 */
	public String getRequestUrl(){
		ConfigurationFactory configurationInstance = ConfigurationFactory.getInstance();
		String address = configurationInstance.getAddress().trim();
        String timePoint = configurationInstance.getTimePoint().trim();
        String areaNo = configurationInstance.getAreaNo().trim();
        String dataType = "day";
        String identityCode = configurationInstance.getIdentitycode().trim();
        if(StringUtils.isEmpty(timePoint)){
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			timePoint = sdf.format(date);
        }
        String url = address + "?AreaNo="+ areaNo +"&Timepoint="+ timePoint +"&datatype="+ dataType +"&Identitycode="+ identityCode;
        return url;
	}
	
	/**
	 * 解析HTTP响应返回的数据
	 * @return
	 *
	 * @author  王贤炳
	 * @throws ParseException 
	 * @since   2015年12月25日
	 */
	public List<DailyRecordBean> parseData(String results){
		List<DailyRecordBean> dailyDataBeans = new ArrayList<DailyRecordBean>();
		if(StringUtils.isNotEmpty(results)){
			JSONObject jsonObj = JSONObject.fromObject(results);
			if("1".equals(jsonObj.getString("ret"))){
				String result = CommonUtils.getPromptErrorInfo(jsonObj);
				throw new PlatformException("返回结果错误：" + result);
			}else{
				JSONArray jsonArr = JSONArray.fromObject(jsonObj.getString("Data"));
				if(jsonArr != null && jsonArr.size() > 0){
					Map<String, Object> airPoints = PointCache.getInstance().getAirPoints();
					Map<String, String> areaParam = ConfigurationFactory.getInstance().getAreaParam();
					DailyRecordBean dailyDataBean = null;
					for (int i = 0; i < jsonArr.size(); i++) {
						JSONObject dayData = jsonArr.getJSONObject(i);
						dailyDataBean = new DailyRecordBean();
						String timePoint = dayData.getString("Timepoint");
						String[] tempStr = null;
						if(StringUtils.isNotEmpty(timePoint)){
							timePoint = timePoint.split(" ")[0];
							tempStr = timePoint.split("-");
						}
						String pointName = dayData.getString("Stationname");
						String pointCode = null;//监测站代码
						String stationCode = null;
						String jcdxz = null;
						String orgid = null;
						if(areaParam.containsKey(pointName)){
							String[] str = null;
							pointCode = areaParam.get(pointName);
							Object obj = airPoints.get(pointCode);
							if(obj != null){
								str =  obj.toString().split("##");
								stationCode = str[0];
								pointName = str[1];
								jcdxz = str[2];
								orgid = str[3];
							}else {
								throw new PlatformException("测点编号不匹配，请检查areaNo.properties配置是否正确...");
							}
						}else{
							throw new PlatformException("站点名称不匹配，请检查areaNo.properties配置是否正确...");
						}
						dailyDataBean.setMonitorDate(timePoint);
						dailyDataBean.setPointCode(pointCode);
						dailyDataBean.setYear(Integer.parseInt(tempStr[0]));
						dailyDataBean.setMonth(Integer.parseInt(tempStr[1]));
						dailyDataBean.setDay(Integer.parseInt(tempStr[2]));
						dailyDataBean.setAqi(CommonUtils.getDoubleValue(dayData.getString("AQI"),true));
						dailyDataBean.setPrimaryPollutant(dayData.getString("PrimaryPollutant"));
						dailyDataBean.setAirQualityIndexLevel(dayData.getString("Level"));
						dailyDataBean.setPm2_5Nd(CommonUtils.getDoubleValue(dayData.getString("PM25"),true)/1000);
						dailyDataBean.setSo2Nd(CommonUtils.getDoubleValue(dayData.getString("SO2"),true)/1000);
						dailyDataBean.setNo2Nd(CommonUtils.getDoubleValue(dayData.getString("NO2"),true)/1000);
						dailyDataBean.setPm10(CommonUtils.getDoubleValue(dayData.getString("PM10"),true)/1000);
						dailyDataBean.setCoNd(CommonUtils.getDoubleValue(dayData.getString("CO"),true));
						dailyDataBean.setO3bHdnd(CommonUtils.getDoubleValue(dayData.getString("O3_8H"),true)/1000);
						dailyDataBean.setStationCode(stationCode);
						dailyDataBean.setJcdxz(jcdxz);
						dailyDataBean.setCjsj(timePoint);
						dailyDataBean.setCjr("系统管理员");
						dailyDataBean.setOrgid(orgid);
						dailyDataBeans.add(dailyDataBean);
					}
				}
			}
		}
		return dailyDataBeans;
	}
	/**
	 * 过滤之前未成功保存过的时间点记录
	 * @param dailyDataBeans	
	 * @return
	 * @throws SQLException
	 *
	 * @author  王贤炳
	 * @since   2015年12月26日
	 * @version V0.2.0
	 */
	public List<DailyRecordBean> filterUnsavedBeans(Connection conn, List<DailyRecordBean> dailyDataBeans) throws SQLException{
		List<DailyRecordBean> unsavedBeans = new ArrayList<DailyRecordBean>();
		if(!BeanUtils.emptyCollection(dailyDataBeans)){
			//存储未保存过的记录
			for (DailyRecordBean dailyDataBean : dailyDataBeans) {
				boolean flag = dailyImpl.checkIsExist(conn, dailyDataBean);
				if(!flag){
					unsavedBeans.add(dailyDataBean);
				}
			}
		}
		return unsavedBeans;
	}
	
	/**
	 * 批量更新语句
	 * @param conn			数据库连接对象
	 * @param ps			PreparedStatement
	 * @param dailyDataBean	日数据对象
	 * @throws SQLException
	 *
	 * @author  王贤炳
	 * @since   2016年01月08日
	 * @version V0.2.0
	 */
	public void addPreparedStatement(Connection conn, PreparedStatement ps, DailyRecordBean dailyDataBean) throws SQLException{
		dailyDataBean.setXh(CommonUtils.getUUID());
		ps.setString(1, dailyDataBean.getXh());
		ps.setString(2, dailyDataBean.getStationCode());
		ps.setString(3, dailyDataBean.getPointCode());
		ps.setInt(4, dailyDataBean.getYear());
		ps.setInt(5, dailyDataBean.getMonth());
		ps.setInt(6, dailyDataBean.getDay());
		ps.setDouble(7, dailyDataBean.getSo2Nd());
		ps.setDouble(8, dailyDataBean.getNo2Nd());
		ps.setDouble(9, dailyDataBean.getPm10());
		ps.setDouble(10, dailyDataBean.getO3bHdnd());
		ps.setDouble(11, dailyDataBean.getPm2_5Nd());
		ps.setDouble(12, dailyDataBean.getCoNd());
		ps.setDouble(13, dailyDataBean.getAqi());
		ps.setString(14, dailyDataBean.getJcdxz());
		ps.setString(15, dailyDataBean.getPrimaryPollutant());
		ps.setString(16, dailyDataBean.getAirQualityIndexLevel());
		ps.setString(17, dailyDataBean.getExecuteStandardNum());
		ps.setString(18, dailyDataBean.getMonitorDate());
		ps.setString(19, dailyDataBean.getCjr());
		ps.setString(20, dailyDataBean.getCjsj());
		ps.setString(21, dailyDataBean.getOrgid());
		ps.addBatch();
	}
}
