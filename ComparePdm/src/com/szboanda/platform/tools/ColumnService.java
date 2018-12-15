package com.szboanda.platform.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
/**
 * 
  * 此类描述的是：查询数据库表字段，实现表字段的对比
  * @Title: ColumnService.java
  * @author: wangjihong
  * @Copyright: PowerData Software Co.,Ltd. Rights Reserved.
  * @Company:深圳市博安达软件开发有限公司
  * @version:1.0
  * create date:Feb 8, 2014 10:43:55 AM
 */
public class ColumnService {
	
	private static Logger logger = Logger.getLogger(ColumnService.class);
	/**
	 * 
	  * @Title: getColums
	  * @Description:根据提供的连接，查询表结构
	  * @param conn
	  * @return   
	  * @return Map<String,Map<String,Column>>  返回类型
	  * @throws
	 */
	public Map<String,Map<String,Column>> getColums(Connection conn){
		return getColums(conn,"");
	}
	/**
	 * 
	  * @Title: getColums
	  * @Description: 根据提供的连接及查询条件，查询表结构
	  * @param conn
	  * @param query
	  * @return   
	  * @return Map<String,Map<String,Column>>  返回类型
	  * @throws
	 */
	public Map<String,Map<String,Column>> getColums(Connection conn,String tableName){
		String sql = Constant.QUERY_COLUMN_SQL;
		//判断，是否根据查询条件查询
		if(tableName != null && tableName != "" && tableName.length() > 0){
			sql = sql.replace("#WHERESTRING#","AND col.TABLE_NAME LIKE '%" + tableName + "%'");
		}else{
			sql = sql.replace("#WHERESTRING#","");
		}
		logger.info("执行sql:" + sql);
		Map<String,Map<String,Column>> result = new LinkedHashMap<String,Map<String,Column>>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			Map<String,Column> table = null;
			while(rs.next()){
				String table_name  = rs.getString("TABLE_NAME");
				String column_name = rs.getString("COLUMN_NAME");
				String data_type = rs.getString("DATA_TYPE");
				String data_length = rs.getString("DATA_LENGTH");
				String comments = rs.getString("COMMENTS");
				if(result.containsKey(table_name)){
					table =result.get(table_name);
					table.put(column_name, new Column(table_name,column_name,data_type,data_length,comments));
				}else{
					table = new HashMap<String,Column>();
					table.put(column_name, new Column(table_name,column_name,data_type,data_length,comments));
					result.put(table_name,table);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{rs.close();}catch (Exception e){};
			try{ps.close();}catch (Exception e){};
		}
		return result;
	}
	
	/**
	 * 
	  * @Title: compareTables
	  * @Description: 根据传递的参数，比较结果
	  * @param s_tables
	  * @param t_tables
	  * @return   
	  * @return Map<String,String>  返回类型
	  * @throws
	 */
	public Map<String,String> compareTables(Map<String,Map<String,Column>> s_tables,Map<String,Map<String,Column>> t_tables,String compareType){
		Map<String,String> result = new LinkedHashMap<String,String>();
		for(String talbe_name : s_tables.keySet()){
			if(t_tables.containsKey(talbe_name)){
				Map<String,String> compareResult = compareTable(s_tables.get(talbe_name),t_tables.get(talbe_name));
				if(compareResult.size() > 0){
					result.put(talbe_name, Constant.CHECK_RESULT_ERRORCOLUMN);
				}
				if(Constant.COMPARE_TYPE_YES.equals(compareType)){
					Map<String,String> compareResult2 = compareTable(t_tables.get(talbe_name),s_tables.get(talbe_name));
					if(compareResult2.size() > 0){
						result.put(talbe_name, Constant.CHECK_RESULT_ERRORCOLUMN);
					}
				}
			}else{
				result.put(talbe_name,Constant.CHECK_RESULT_NOTABLE);
				logger.info("目标库中不包含表：" + talbe_name);
			}
		}
		return result;
	}
	/**
	 * 
	  * @Title: compareTable
	  * @Description: 比较两个表的字段类型是否一致
	  * @param s_table
	  * @param t_table
	  * @return   
	  * @return Map<String,String>  返回类型
	  * @throws
	 */
	public Map<String,String> compareTable(Map<String,Column> s_table,Map<String,Column> t_table){
		Map<String,String> result = new HashMap<String,String>();
		for(String column:s_table.keySet()){
			if(t_table.containsKey(column)){
				Column s_col = s_table.get(column);
				Column t_col = t_table.get(column);
				if(s_col.getData_type().equals(t_col.getData_type())){
					if(s_col.getData_length().equals(t_col.getData_length())){
						
					}else{
						result.put(column,Constant.CHECK_RESULT_COLUMNLENGTH); 
						logger.info("字段长度不一致：" + t_col.toString());
					}
				}else{
					result.put(column,Constant.CHECK_RESULT_COLUMNTYPE); 
					logger.info("字段类型不一致：" + t_col.toString());
				}
			}else{
				result.put(column,Constant.CHECK_RESULT_NOCOLUMN); 
				logger.info("目标库中不包含字段：" + s_table.get(column).toString());
			}
		}
		return result;
	}
}
