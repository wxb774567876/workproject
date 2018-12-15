package com.szboanda.sjdj.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


import net.sf.json.JSONObject;

public class CommonUtils {
	/**
	 * 生成32位随机码
	 * @return
	 *
	 * @author  王贤炳
	 * @since   2016年1月8日
	 * @version V0.2.0
	 */
	public static String getUUID(){
		UUID uuid = UUID.randomUUID();  
        String str = uuid.toString();  
        // 去掉"-"符号 
        String temp = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);  
        return temp;   
	}
	
	/**
	 * 关闭数据库资源
	 * @param conn		数据库连接对象
	 * @param ps		PreparedStatement对象
	 * @param rs		ResultSet对象
	 * @throws SQLException
	 *
	 * @author  王贤炳
	 * @since   2016年1月8日
	 * @version V0.2.0
	 */
	public static void closeConnection(Connection conn, PreparedStatement ps, ResultSet rs){
		try {
			if(rs != null){
				rs.close();
			}
			if(ps != null){
				ps.close();
			}
			if(conn != null){
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param value	需处理的值
	 * @return
	 *
	 * @author  王贤炳
	 * @since   2016年1月8日
	 * @version V0.2.0
	 */
	public static Double getDoubleValue(String value, boolean isZero){
		Double d = null;
		if(isZero){
			d = 0d;
		}
		if(StringUtils.isNotEmpty(value) && isNumeric(value)){
			return Double.valueOf(value);
		}
		return d;
	}
	
	/**
	 * 提示错误信息
	 * @param jsonObj	返回的响应结果
	 *
	 * @author  王贤炳
	 * @since   2015年12月26日
	 */
	public static String getPromptErrorInfo(JSONObject jsonObj){
		String error = "";
		int errCode = Integer.parseInt(jsonObj.getString("errCode"));
		switch (errCode) {
			case 1:
				error = "数据格式有误";
				break;
			case 2:
				error = "数据不能为空";
				break;
			case 3:
				error = "数据不存在";
				break;
			case 4:
				error = "权限不允许";
				break;
			default:
				break;
		}
		return error;
	}
	
	public static boolean isNumeric(String str){ 
	    	Pattern pattern = Pattern.compile("^((-?[1-9]\\d*\\.?\\d*)[Ee]{0,1}([+-]?\\d+))|(-?0\\.\\d*[1-9])|(-?[0])|(-?[1-9]{1}\\d*)|(-?[0]\\.\\d*)$");
			Matcher isNum = pattern.matcher(str);
			if( !isNum.matches() ){
			    return false; 
			} 
			return true; 
	}
}
