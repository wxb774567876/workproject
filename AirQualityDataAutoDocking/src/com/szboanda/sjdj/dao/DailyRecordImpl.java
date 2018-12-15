package com.szboanda.sjdj.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.szboanda.platform.util.BeanUtils;
import com.szboanda.sjdj.ConfigurationFactory;
import com.szboanda.sjdj.bean.DailyRecordBean;
import com.szboanda.sjdj.manager.DailyRecordManager;
import com.szboanda.sjdj.manager.HourlyRecordManager;
import com.szboanda.sjdj.utils.CommonUtils;
import com.szboanda.sjdj.utils.LogHandler;

/**
 * 空气质量日数据实现类
 * 
 * @Company  深圳博安达软件开发有限公司
 * @author   王贤炳
 * @version  2016年1月8日
 * @since    V0.2.0
 */
public class DailyRecordImpl{
	/**
	 * 保存空气质量日数据
	 * @param results	返回的响应结果
	 * @return
	 *
	 * @author  王贤炳
	 * @since   2016年01月08日
	 * @version V0.2.0
	 * @throws SQLException 
	 */
	public void saveDailyData(String results, String url){
		DailyRecordManager manager = new DailyRecordManager();
		HourlyRecordManager hourlyManager = new HourlyRecordManager();
		Connection conn = null;
		PreparedStatement ps = null;
		List<DailyRecordBean> dailyDataBeans = manager.parseData(results);
		try {
			conn = ConfigurationFactory.getInstance().getConnection();
			conn.setAutoCommit(false);
			if(!BeanUtils.emptyCollection(dailyDataBeans)){
				String sql = new String("insert into T_HJJC_QHJ_JCJGRPJ(XH,JCZDM,CDBH,YEAR,MONTH,DAY,SO2,NO2,PM10,O3,PM25,CO,AQI,JCDXZ,SYWRW,KQZLZSJB,ZXBZBH,JCSJ,CJR,CJSJ,ORGID) "
						+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?,'yyyy/mm/dd'),?,TO_DATE(?,'yyyy/mm/dd'),?)");
				ps = conn.prepareStatement(sql);
				int count = 0;
				for (DailyRecordBean dailyDataBean : dailyDataBeans) {
					boolean flag = checkIsExist(conn, dailyDataBean);
					if(!flag){
						manager.addPreparedStatement(conn, ps, dailyDataBean);
						count ++;
					}
				}
				if(count > 0){
					ps.executeBatch();
					hourlyManager.afterSave(conn, url, 1, true, null);
				}else{
					String showTime = url.split("\\?")[1].split("&")[1].split("=")[1].replace("%20", " ");
					String msg = showTime + "【日】数据已提取过" + "\r\n";
					LogHandler.logSuccessfulInfo(msg.toString());
					System.out.println(msg);
				}
			}
			conn.commit();
		} catch (Exception e) {
			try {
				conn.rollback();
				Connection conn2 = null;
				try {
					conn2 = ConfigurationFactory.getInstance().getConnection();
					conn2.setAutoCommit(false);
					hourlyManager.afterSave(conn, url, 1, false, e);
					conn2.commit();
				} catch (SQLException e1) {
					e1.printStackTrace();
				} finally {
					CommonUtils.closeConnection(conn2, null, null);;
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally{
			CommonUtils.closeConnection(conn, ps, null);
		}
	}
	
	/**
	 * 检查该日数据是否存在
	 * @param pointName		监测点名称
	 * @param pointCode		监测点编号
	 * @param monitorTime	监测时间
	 *
	 * @author  王贤炳
	 * @throws SQLException 
	 * @since   2016年01月08日
	 */
	public boolean checkIsExist(Connection conn, DailyRecordBean dailyDataBean){
		String pointCode = dailyDataBean.getPointCode();
		String monitorTime = dailyDataBean.getMonitorDate();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM T_HJJC_QHJ_JCJGRPJ WHERE CDBH = ? AND JCSJ = to_date(?,'yyyy/mm/dd')");
			if(StringUtils.isNotEmpty(pointCode) && StringUtils.isNotEmpty(monitorTime)){
				ps.setString(1, pointCode);
				ps.setString(2, monitorTime);
				rs = ps.executeQuery();
				//判断该区，该日数据记录是否存在
				if(rs.next()){
					return true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CommonUtils.closeConnection(null, ps, rs);
		}
		return false;
	}
}
