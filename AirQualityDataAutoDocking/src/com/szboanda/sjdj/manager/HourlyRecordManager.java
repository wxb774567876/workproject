package com.szboanda.sjdj.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang.StringUtils;

import com.szboanda.platform.util.BeanUtils;
import com.szboanda.platform.util.exception.PlatformException;
import com.szboanda.sjdj.ConfigurationFactory;
import com.szboanda.sjdj.bean.HourlyRecordBean;
import com.szboanda.sjdj.cache.PointCache;
import com.szboanda.sjdj.dao.HourlyRecordImpl;
import com.szboanda.sjdj.utils.CommonUtils;
import com.szboanda.sjdj.utils.LogHandler;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 空气质量小时数据处理类
 * 
 * @Company  深圳博安达软件开发有限公司
 * @author   王贤炳
 * @version  2015年12月25日
 */
public class HourlyRecordManager {
	private static HourlyRecordImpl hourlyImpl = null;
	
	static{
		hourlyImpl = new HourlyRecordImpl();
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
		List<String> intervalUrls = getRequestUrlInDateInterval("realhour","yyyy-mm-dd hh24:mi:ss");
		if(!BeanUtils.emptyCollection(intervalUrls)){
			allRequestUrl.addAll(intervalUrls);
		}else{
			String requestUrl = getRequestUrl();
			allRequestUrl.add(requestUrl);
		}
		//获得以往提取数据失败的时间段
		List<String> failedUrls = hourlyImpl.getFailedUrl("0");
		if(!BeanUtils.emptyCollection(failedUrls)){
			allRequestUrl.addAll(failedUrls);
		}
		for(String str : allRequestUrl){
			String results;
			try {
				results = HttpRequestManager.sendGet(str);
				hourlyImpl.saveHourlyData(results,str);
			} catch (Exception e) {
				Connection conn = null;
				try {
					conn = ConfigurationFactory.getInstance().getConnection();
					conn.setAutoCommit(false);
					afterSave(conn, str, 1, false, e);
					conn.commit();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					CommonUtils.closeConnection(conn, null, null);;
				}
			}
		}
	}
	
	/**
	 * 获得请求的url
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
        String dataType = "realhour";
        String identityCode = configurationInstance.getIdentitycode().trim();
        if(StringUtils.isNotEmpty(timePoint)){
			timePoint = timePoint.trim().replaceAll(" ", "%20");
		}else{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
			Date date = new Date();
			timePoint = sdf.format(date);
			timePoint = timePoint.trim().replaceAll(" ", "%20");
		}
        //timePoint = "2016-01-20%2010:00:00";
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
	public List<HourlyRecordBean> parseData(String results){
		List<HourlyRecordBean> hourlyDataBeans = new ArrayList<HourlyRecordBean>();
		if(StringUtils.isNotEmpty(results)){
			JSONObject jsonObj = JSONObject.fromObject(results);
			if("1".equals(jsonObj.getString("ret"))){
				String result = CommonUtils.getPromptErrorInfo(jsonObj);
				throw new PlatformException("返回的响应结果异常：" + result);
			}else{
				JSONArray jsonArr = JSONArray.fromObject(jsonObj.getString("Data"));
				if(jsonArr != null && jsonArr.size() > 0){
					Map<String, Object> airPoints = PointCache.getInstance().getAirPoints();
					Map<String, String> areaParam = ConfigurationFactory.getInstance().getAreaParam();
					HourlyRecordBean hourlyDataBean = null;
					for (int i = 0; i < jsonArr.size(); i++) {
						JSONObject hourData = jsonArr.getJSONObject(i);
						hourlyDataBean = new HourlyRecordBean();
						hourlyDataBean.setMonitorTime(hourData.getString("Timepoint"));
						String pointName = hourData.getString("Stationname");
						String pointCode = null;//监测站代码
						String stationCode = null;
						String orgid = null;
						if (areaParam.containsKey(pointName)) {
							String[] str = null; 
							pointCode = areaParam.get(pointName);
							Object obj = airPoints.get(pointCode);
							if(obj != null){
								str =  obj.toString().split("##");
								stationCode = str[0];
								pointName = str[1];
								orgid = str[3];
							} else {
								throw new PlatformException("测点编号不匹配，请检查areaNo.properties配置是否正确...");
							}
						} else {
							throw new PlatformException("站点名称不匹配，请检查areaNo.properties配置是否正确...");
						}
						hourlyDataBean.setPointName(hourData.getString("Stationname"));
						hourlyDataBean.setPointCode(pointCode);
						hourlyDataBean.setAqi(hourData.getString("AQI"));
						hourlyDataBean.setPrimaryPollutant(hourData.getString("PrimaryPollutant"));
						hourlyDataBean.setAirQualityIndexLevel(hourData.getString("Level"));
						hourlyDataBean.setAirQualityType(hourData.getString("Type"));
						hourlyDataBean.setAirQualityColor(hourData.getString("ColorName"));
						hourlyDataBean.setPm2_5Nd(CommonUtils.getDoubleValue(hourData.getString("PM25"), true)/1000);
						hourlyDataBean.setPm2_5Hdnd(CommonUtils.getDoubleValue(hourData.getString("PM25_24H"), true)/1000);
						hourlyDataBean.setPm2_5hdFzs(CommonUtils.getDoubleValue(hourData.getString("PM25AQI"), true)/1000);
						hourlyDataBean.setSo2Nd(CommonUtils.getDoubleValue(hourData.getString("SO2"), true)/1000);
						hourlyDataBean.setSo2Fzs(CommonUtils.getDoubleValue(hourData.getString("SO2AQI"), true)/1000);
						hourlyDataBean.setNo2Nd(CommonUtils.getDoubleValue(hourData.getString("NO2"), true)/1000);
						hourlyDataBean.setNo2Fzs(CommonUtils.getDoubleValue(hourData.getString("NO2AQI"), true)/1000);
						hourlyDataBean.setPm10Xspjnd(CommonUtils.getDoubleValue(hourData.getString("PM10"), true)/1000);
						hourlyDataBean.setPm10Hdpjnd(CommonUtils.getDoubleValue(hourData.getString("PM10_24H"), true)/1000);
						hourlyDataBean.setPm10Hdpjzs(CommonUtils.getDoubleValue(hourData.getString("PM10AQI"), true)/1000);
						hourlyDataBean.setCoNd(CommonUtils.getDoubleValue(hourData.getString("CO"), true));
						hourlyDataBean.setCoFzs(CommonUtils.getDoubleValue(hourData.getString("COAQI"), true));
						hourlyDataBean.setO3Nd(CommonUtils.getDoubleValue(hourData.getString("O3"), true)/1000);
						hourlyDataBean.setO3Fzs(CommonUtils.getDoubleValue(hourData.getString("O3AQI"), true)/1000);
						hourlyDataBean.setO3bHdnd(CommonUtils.getDoubleValue(hourData.getString("O3_8H"), true)/1000);
						hourlyDataBean.setO3bHdzs(CommonUtils.getDoubleValue(hourData.getString("O3_8HAQI"), true)/1000);
						hourlyDataBean.setOrgid(orgid);
						hourlyDataBean.setMonitorNum(stationCode);
						hourlyDataBeans.add(hourlyDataBean);
					}
				}
			}
		}
		return hourlyDataBeans;
	}
	
	/**
	 * 获取该区域,该天未获取到数据的时间段
	 * @param timePoint	时间点
	 * @return
	 *
	 * @author  王贤炳
	 * @since   2015年12月26日
	 * @version V0.2.0
	 */
	public List<String> getFailedTimePoint(String timePoint){
		List<DynaBean> successBeans = hourlyImpl.getDockingFailureRecord(timePoint);
		List<String> list = new ArrayList<String>();
		List<Integer> savedTimes = new ArrayList<Integer>();
		if(!BeanUtils.emptyCollection(successBeans)){
			for (DynaBean dynaBean : successBeans) {
				String jcsj = BeanUtils.getString(dynaBean, "JCSJ");
				if(StringUtils.isNotEmpty(jcsj)){
					String time = jcsj.split(" ")[1];
					if(StringUtils.isEmpty(time)){
						savedTimes.add(0);
					}else{
						savedTimes.add(Integer.valueOf(time.split(":")[0]));
					}
				}
			}
		}
		int currentTimePoint = Integer.valueOf(timePoint.split("%20")[1].substring(0, 2));//当前时间点
		String date = timePoint.split("%20")[0];
		for(int i = 0; i < currentTimePoint; i++){
			if(!savedTimes.contains(i)){
				StringBuffer newDate = new StringBuffer(); 
				if(i < 10){
					newDate.append(date).append("%200").append(i).append(":00:00");
				}else{
					newDate.append(date).append("%20").append(i).append(":00:00");
				}
				list.add(newDate.toString());
			}
		}
		if(!list.contains(timePoint)){
			list.add(timePoint);
		}
		return list;
	}
	
	/**
	 * 过滤之前未成功保存过的时间点记录
	 * @param hourlyDataBeans	小时数据集合
	 * @return
	 * @throws SQLException
	 *
	 * @author  王贤炳
	 * @since   2015年12月26日
	 * @version V0.2.0
	 */
	public List<HourlyRecordBean> filterUnsavedBeans(Connection conn, List<HourlyRecordBean> hourlyDataBeans) throws SQLException{
		List<HourlyRecordBean> unsavedBeans = new ArrayList<HourlyRecordBean>();
		if(!BeanUtils.emptyCollection(hourlyDataBeans)){
			//存储未保存过的记录
			for (HourlyRecordBean hourlyDataBean : hourlyDataBeans) {
				boolean flag = hourlyImpl.checkIsExist(conn, hourlyDataBean);
				if(!flag){
					unsavedBeans.add(hourlyDataBean);
				}
			}
		}
		return unsavedBeans;
	}
	
	/**
	 * 批量更新语句
	 * @param conn				数据库连接对象
	 * @param ps				PreparedStatement对象
	 * @param hourlyDataBean	小时数据对象
	 * @throws SQLException
	 *
	 * @author  王贤炳
	 * @since   2015年12月28日
	 * @version V0.2.0
	 */
	public void addPreparedStatement(Connection conn, PreparedStatement ps, HourlyRecordBean hourlyDataBean) throws SQLException{
		hourlyDataBean.setXh(CommonUtils.getUUID());
		ps.setString(1, hourlyDataBean.getXh());
		ps.setString(2, hourlyDataBean.getMonitorNum());
		ps.setString(3, hourlyDataBean.getPointName());
		ps.setString(4, hourlyDataBean.getPointCode());
		ps.setDouble(5, hourlyDataBean.getSo2Nd());
		ps.setDouble(6, hourlyDataBean.getSo2Fzs());
		ps.setDouble(7, hourlyDataBean.getNo2Nd());
		ps.setDouble(8, hourlyDataBean.getNo2Fzs());
		ps.setDouble(9, hourlyDataBean.getPm10Xspjnd());
		ps.setDouble(10, hourlyDataBean.getPm10Hdpjnd());
		ps.setDouble(11, hourlyDataBean.getPm10Hdpjzs());
		ps.setDouble(12, hourlyDataBean.getCoNd());
		ps.setDouble(13, hourlyDataBean.getCoFzs());
		ps.setDouble(14, hourlyDataBean.getO3Nd());
		ps.setDouble(15, hourlyDataBean.getO3Fzs());
		ps.setDouble(16, hourlyDataBean.getO3bHdnd());
		ps.setDouble(17, hourlyDataBean.getO3bHdzs());
		ps.setDouble(18, hourlyDataBean.getPm2_5Nd());
		ps.setDouble(19, hourlyDataBean.getPm2_5Hdnd());
		ps.setDouble(20, hourlyDataBean.getPm2_5hdFzs());
		ps.setString(21, hourlyDataBean.getAqi());
		ps.setString(22, hourlyDataBean.getPrimaryPollutant());
		ps.setString(23, hourlyDataBean.getAirQualityIndexLevel());
		ps.setString(24, hourlyDataBean.getAirQualityType());
		ps.setString(25, hourlyDataBean.getAirQualityColor());
		ps.setString(26, hourlyDataBean.getMonitorTime());
		ps.setString(27, hourlyDataBean.getExecuteStandardNum());
		ps.setInt(28, 0);
		ps.setString(29, hourlyDataBean.getOrgid());
		ps.addBatch();
	}
	
	/**
	 * 保存数据操作成功或者失败后的操作，主要用于记录日志并插入日志表
	 * @param conn
	 * @param url
	 * @param dataType
	 * @param flag
	 * @param e
	 *
	 * @author  王贤炳
	 * @since   2016年1月20日
	 * @version V0.2.0
	 */
	public void afterSave(Connection conn, String url, int dataType, boolean flag, Exception e){
		String showTime = url.split("\\?")[1].split("&")[1].split("=")[1].replace("%20", " ");
		String msg = null;
		int tqzt = 0;//提取状态
		String type = "【小时】";
		if(dataType == 1){
			type = "【日】";
		}
		if (flag) {
			msg = showTime + type + "数据提取成功!!!" + "\r\n";
			LogHandler.logSuccessfulInfo(msg.toString());
		} else {
			tqzt = 1;
			msg = showTime + type + "数据提取失败【" + e.getMessage() + "】";
			LogHandler.logExceptionInfo(msg, e);
		}
		hourlyImpl.keepSaveState(conn, dataType, showTime, tqzt, url, msg);
		System.out.println(msg);
	}
	
	/**
	 * 获取指定时间区间的请求url，并过滤掉已经提取成功过的时间点
	 * @param dataType 数据类型realhour:表示小时数据，day：表示日数据
	 * @return
	 *
	 * @author  王贤炳
	 * @since   2016年1月20日
	 * @version V0.2.0
	 */
	public List<String> getRequestUrlInDateInterval(String dataType,String fmt){
		List<String> requestUrls = new ArrayList<String>();
		String beginDate = ConfigurationFactory.getInstance().getBeginDate();
		String endDate = ConfigurationFactory.getInstance().getEndDate();
		/*beginDate = "2015-02-09 11:34:34";
		endDate = "2015-02-10 11:23:33";*/
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if(StringUtils.isEmpty(beginDate)){
			return null;
		}
		if(StringUtils.isEmpty(endDate)){
			endDate = sdf.format(new Date());
		}
		try {
			//将配置文件中的开始时间和结束时间转换成yyyy-MM-dd的格式
			Calendar calBegin = Calendar.getInstance();
			calBegin.setTime(sdf.parse(beginDate));
			Calendar calEnd = Calendar.getInstance();
			calEnd.setTime(sdf.parse(endDate));
			List<String> timePoints = new ArrayList<String>();
			if(StringUtils.equals("realhour", dataType)){
				while((calBegin.compareTo(calEnd)<=0)){
					String date = sdf.format(calBegin.getTime());
					String timePoint = null;
					for(int i = 0; i < 24; i++){
						if(i < 10){
							timePoint = date + " 0" + i + ":00:00";
						}else{
							timePoint = date + " " + i + ":00:00";
						}
						if(!timePoints.contains(timePoint)){
							timePoints.add(timePoint);
						}
					}
					//加一天
					calBegin.add(Calendar.DAY_OF_MONTH, 1);
				}
			}else{
				while((calBegin.compareTo(calEnd)<=0)){
					String date = sdf.format(calBegin.getTime());
					if(!timePoints.contains(date)){
						timePoints.add(date);
					}
					//加一天
					calBegin.add(Calendar.DAY_OF_MONTH, 1);
				}
			}
			//防止出现java.sql.SQLSyntaxErrorException: ORA-01795: 列表中的最大表达式数为 1000,分多次处理
			int size = timePoints.size(); 
			if(size > 1000){
				double count = Math.ceil(size / 1000d);
				int index = 0;
				if(count > 1){
					int endIndex = 0;
					while(endIndex < size){
						endIndex = index + 1000;
						if(endIndex > size){
							endIndex = size;
						}
						List<String> list = timePoints.subList(index, endIndex);
						filter(requestUrls, list, fmt, dataType);
						index += 1000;
					}
				}
			}else{
				filter(requestUrls, timePoints, fmt, dataType);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return requestUrls;
	}
	
	public void filter(List<String> requestUrls,List<String> timePoints,String fmt,String dataType){
		List<String> newTimePoints = filterSuccessRecord(timePoints,fmt, dataType);
		ConfigurationFactory configurationInstance = ConfigurationFactory.getInstance();
		String address = configurationInstance.getAddress().trim();
        String areaNo = configurationInstance.getAreaNo().trim();
        String identityCode = configurationInstance.getIdentitycode().trim();
		if(!BeanUtils.emptyCollection(newTimePoints)){
			for(String timePoint : newTimePoints){
				timePoint = timePoint.trim().replaceAll(" ", "%20");
				String url = address + "?AreaNo="+ areaNo +"&Timepoint="+ timePoint +"&datatype="+ dataType +"&Identitycode="+ identityCode;
				if(!requestUrls.contains(url)){
					requestUrls.add(url);
				}
			}
		}
	}
	
	/**
	 * 过滤掉已成功提取到数据的时间点
	 * @param timePoints	区间内的时间点
	 *
	 * @author  王贤炳
	 * @since   2016年1月20日
	 * @version V0.2.0
	 */
	public List<String> filterSuccessRecord(List<String> timePoints, String fmt, String dataType){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null; 
		List<String> newTimePoints = new ArrayList<String>();
		if(!BeanUtils.emptyCollection(timePoints)){
			try {
				List<String> existTimePoints = new ArrayList<String>();
				conn = ConfigurationFactory.getInstance().getConnection();
				StringBuffer sb = new StringBuffer();
				for(int i = 0; i < timePoints.size(); i++){
					if(i > 0){
						sb.append(",");
					}
					sb.append("?");
				}
				ps = conn.prepareStatement("SELECT * FROM T_HJJC_QHJ_TQSJLOG WHERE TQZT=0 AND SJLX=? AND JCSJ IN(" + sb.toString() + ")");
				ps.setString(1, StringUtils.equals("realhour", dataType) ? "0" : "1");
				int index = 2;
				for (String timePoint : timePoints){
					ps.setString(index, timePoint);
					index ++;
				}
				rs = ps.executeQuery();
				while(rs.next()){
					String jcsj = rs.getString("JCSJ");
					if(!existTimePoints.contains(jcsj)){
						existTimePoints.add(jcsj);
					}
				}
				
				for (String timePoint : timePoints){
					if(!existTimePoints.contains(timePoint)){
						newTimePoints.add(timePoint);
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				CommonUtils.closeConnection(conn, ps, rs);
			}
		}
		return newTimePoints;
	}
}
