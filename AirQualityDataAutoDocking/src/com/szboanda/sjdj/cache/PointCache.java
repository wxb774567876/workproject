package com.szboanda.sjdj.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.szboanda.sjdj.ConfigurationFactory;
import com.szboanda.sjdj.utils.CommonUtils;

/**
 * 
 * 
 * @Company  深圳博安达软件开发有限公司
 * @author   王贤炳
 * @version  2016年1月8日
 */
public class PointCache {
	private static PointCache instance = null; 
	private Map<String, Object> airPoints = new HashMap<String,Object>();
	private PointCache(){}
	
	public static PointCache getInstance(){
		if(instance == null){
			instance = new PointCache();
			instance.reloadAirQualityPoints();
		}
		return instance;
	}
	
	/**
	 * 加载测点名称缓存
	 * @param filePath
	 *
	 * @author  王贤炳
	 * @since   2016年1月8日
	 * @version V0.2.0
	 */
	public void reloadAirQualityPoints(){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if(airPoints != null && airPoints.size() > 0){
				airPoints.clear();
			}
			conn = ConfigurationFactory.getInstance().getConnection();
			ps = conn.prepareStatement("SELECT JCZDM,CDBH,CDMC,SFDQJC,JCDXZ,ORGID FROM T_HJJC_QHJ_ZDXX WHERE SFDQJC = '1'");
			rs = ps.executeQuery();
			while(rs.next()){
				airPoints.put(rs.getString("CDBH"), rs.getString("JCZDM") + "##" + 
						rs.getString("CDMC") + "##" + rs.getString("JCDXZ") + "##" + rs.getString("ORGID"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CommonUtils.closeConnection(conn, ps, rs);
		}
	}

	public Map<String, Object> getAirPoints() {
		return airPoints;
	}
	
}
