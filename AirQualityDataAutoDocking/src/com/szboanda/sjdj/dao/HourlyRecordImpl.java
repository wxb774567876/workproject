package com.szboanda.sjdj.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.lang.StringUtils;

import com.szboanda.platform.util.BeanUtils;
import com.szboanda.platform.util.exception.PlatformException;
import com.szboanda.sjdj.ConfigurationFactory;
import com.szboanda.sjdj.bean.HourlyRecordBean;
import com.szboanda.sjdj.manager.HourlyRecordManager;
import com.szboanda.sjdj.utils.CommonUtils;
import com.szboanda.sjdj.utils.LogHandler;

/**
 * 空气质量小时数据实现类
 * 
 * @Company  深圳博安达软件开发有限公司
 * @author   王贤炳
 * @version  2015年12月25日
 */
public class HourlyRecordImpl{
	
	/**
	 * 
	 * 获取到对接空气质量小时数据成功的记录
	 *
	 * @author  王贤炳
	 * @since   2015年12月26日
	 * @version V0.2.0
	 */
	public List<DynaBean> getDockingFailureRecord(String timePoint){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ConfigurationFactory configurationInstance = ConfigurationFactory.getInstance();
			conn = configurationInstance.getConnection();
			Map<String, String> areaParam = configurationInstance.getAreaParam();
			String areaName = areaParam.get(configurationInstance.getAreaNo());
			if(StringUtils.isEmpty(areaName)){
				return null;
			}
			//根据行政区划名称获取行政区划代码信息
			ps = conn.prepareStatement("SELECT * FROM T_COMN_XZQHDM WHERE XZQH = ?");
			ps.setString(1, areaName);
			rs=ps.executeQuery();
			String administrativeAreaCode = null;
			while(rs.next()){
				administrativeAreaCode = rs.getString("XZQHDM");
				if(StringUtils.isNotEmpty(administrativeAreaCode)) break;
            }
			if(StringUtils.isNotEmpty(administrativeAreaCode)){
				ps = conn.prepareStatement("SELECT CDMC,CDBH FROM T_HJJC_QHJ_ZDXX WHERE XZQH = ? AND SFDQJC = '1' ");
				ps.setString(1, administrativeAreaCode);
				rs = ps.executeQuery();
				String date = timePoint.split("%20")[0];//当前日期，不包含时间，如2015-12-26
				List<DynaBean> beans = new ArrayList<DynaBean>();
				//查询行政区划下任一个站点当天24小时的所有记录
				while(rs.next()){
					ps = conn.prepareStatement("SELECT * FROM T_HJJC_QHJ_JCJG WHERE (JCDBH = ? OR JCDWMC = ?) AND JCSJ >= TO_DATE(?,'yyyy/mm/dd hh24:mi:ss') AND JCSJ < TO_DATE(?,'yyyy/mm/dd hh24:mi:ss')");
					ps.setString(1, rs.getString("CDBH"));
					ps.setString(2, rs.getString("CDMC"));
					ps.setString(3, date + " 00:00:00");
					ps.setString(4, date + "23:59:59");
					rs = ps.executeQuery();
					while(rs.next()){
						DynaBean bean = new LazyDynaBean();
						bean.set("JCSJ", rs.getString("JCSJ"));
						beans.add(bean);
					}
					if(!BeanUtils.emptyCollection(beans)){
						return beans;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CommonUtils.closeConnection(conn, ps, rs);
		}
		return null;
	}
	
	/**
	 * 检查该小时数据是否存在
	 * @param pointName		监测点名称
	 * @param pointCode		监测点编号
	 * @param monitorTime	监测时间
	 *
	 * @author  王贤炳
	 * @throws SQLException 
	 * @since   2015年12月26日
	 */
	public boolean checkIsExist(Connection conn, HourlyRecordBean hourlyDataBean){
		String pointCode = hourlyDataBean.getPointCode();
		String monitorTime = hourlyDataBean.getMonitorTime();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM T_HJJC_QHJ_JCJG WHERE JCDBH = ? AND JCSJ = to_date(?,'yyyy/mm/dd hh24:mi:ss')");
			ps.setString(1, pointCode);
			ps.setString(2, monitorTime);
			rs = ps.executeQuery();
			//判断该区，该小时的记录是否存在
			if(rs.next()){
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CommonUtils.closeConnection(null, ps, rs);
		}
		return false;
	}
	
	/**
	 * 保存空气质量小时数据
	 * @param results
	 * @param url
	 *
	 * @author  王贤炳
	 * @since   2016年1月20日
	 * @version V0.2.0
	 */
	public void saveHourlyData(String results, String url){
		HourlyRecordManager manager = new HourlyRecordManager();
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = ConfigurationFactory.getInstance().getConnection();
			conn.setAutoCommit(false);
			List<HourlyRecordBean> hourlyDataBeans = manager.parseData(results);
			if(!BeanUtils.emptyCollection(hourlyDataBeans)){
				String sql = new String("insert into T_HJJC_QHJ_JCJG(XH,JCBH,JCDWMC,JCDBH,SO2ND,SO2FZS,NO2ND,NO2FZS,KLWXY10XSPJND,KLWXY10HDPJND,KLWXY10HDPJFZS,COND,COFZS,"
						+ "O3XSND,O3XSFZS,O3BXSHDPJ,O3BXSHDFZS,KLWXY25ND,KLWXY25HDND,KLWXY25HDFZS,AQI,SYWRW,KQZLZSJB,KQZLLB,KQZLYS,JCSJ,ZXBZBH,PXH,ORGID) "
						+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?,'yyyy-mm-dd hh24:mi:ss'),?,?,?)");
				ps = conn.prepareStatement(sql);
				int count = 0;
				for (HourlyRecordBean hourlyDataBean : hourlyDataBeans) {
					//判断该时间点数据是否存在
					boolean flag = checkIsExist(conn, hourlyDataBean);
					if(!flag){
						manager.addPreparedStatement(conn, ps, hourlyDataBean);
						count ++;
					}
				}
				if(count > 0){
					ps.executeBatch();
					manager.afterSave(conn, url, 0, true, null);
				}else{
					String showTime = url.split("\\?")[1].split("&")[1].split("=")[1].replace("%20", " ");
					String msg = showTime + "【小时】数据已提取过" + "\r\n";
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
					manager.afterSave(conn2, url, 0, false, e);
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
	 * 记录提取数据状态
	 * @param conn			数据库连接对象
	 * @param dataType		数据类型0:表示时数据，1:表示日数据
	 * @param timePoint		提取成功或失败的时间点
	 * @param tqzt			提取状态0表示成功，1表示失败
	 * @param url			请求数据的url完整路径
	 * @return
	 *
	 * @author  王贤炳
	 * @since   2016年1月20日
	 * @version V0.2.0
	 */
	public boolean keepSaveState(Connection conn, int dataType, String timePoint, int tqzt, String url, String msg){
		int flag = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM T_HJJC_QHJ_TQSJLOG WHERE SJLX = ? AND URL = ?");
			ps.setInt(1, dataType);
			ps.setString(2, url);
			rs = ps.executeQuery();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String date = sdf.format(new Date());
			if (rs.next()) {
				ps = conn.prepareStatement("UPDATE T_HJJC_QHJ_TQSJLOG SET TQZT=?,BZ=?,XGSJ=TO_DATE(?,'yyyy-mm-dd hh24:mi:ss') WHERE URL=? AND SJLX=?");
				ps.setInt(1, tqzt);
				ps.setString(2, msg);
				ps.setString(3, date);
				ps.setString(4, url);
				ps.setInt(5, dataType);
				ps.executeUpdate();
			} else {
				ps = conn.prepareStatement("INSERT INTO T_HJJC_QHJ_TQSJLOG(XH,SJLX,JCSJ,TQZT,URL,BZ,CJSJ)"
						+ "VALUES(?,?,TO_DATE(?,'yyyy-mm-dd hh24:mi:ss'),?,?,?,TO_DATE(?,'yyyy-mm-dd hh24:mi:ss'))");
				ps.setString(1, CommonUtils.getUUID());
				ps.setInt(2, dataType);
				ps.setString(3, timePoint);
				ps.setInt(4, tqzt);
				ps.setString(5, url);
				ps.setString(6, msg);
				ps.setString(7, date);
				flag = ps.executeUpdate();
				
			}
		} catch (SQLException e) {
			throw new PlatformException("日志信息更新失败" + e.getMessage());
		} finally {
			CommonUtils.closeConnection(null, ps, rs);
		}
		return flag > 0;
	}
	
	/**
	 * 返回提取数据失败时请求的url路径
	 * @param dataType	0表示时数据，1表示日数据
	 * @return
	 *
	 * @author  王贤炳
	 * @since   2016年1月19日
	 * @version V0.2.0
	 */
	public List<String> getFailedUrl(String dataType){
		List<String> timePoints = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = ConfigurationFactory.getInstance().getConnection();
			ps = conn.prepareStatement("SELECT URL,JCSJ FROM T_HJJC_QHJ_TQSJLOG WHERE TQZT = 1 AND SJLX = ?");
			ps.setString(1, dataType);
			rs = ps.executeQuery();
			while(rs.next()){
				String url = rs.getString("URL");
				if(!timePoints.contains(url)){
					timePoints.add(rs.getString("URL"));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CommonUtils.closeConnection(conn, ps, rs);;
		}
		return timePoints;
	}
}
