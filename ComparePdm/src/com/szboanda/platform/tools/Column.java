package com.szboanda.platform.tools;
/**
 * 
  * 此类描述的是：数据库字段描述类
  * @Title: Column.java
  * @author: wangjihong
  * @Copyright: PowerData Software Co.,Ltd. Rights Reserved.
  * @Company:深圳市博安达软件开发有限公司
  * @version:1.0
  * create date:Feb 8, 2014 10:24:59 AM
 */
public class Column {
	//表名称
	private String table_name;
	//字段名称
	private String column_name;
	//字段类型
	private String data_type;
	//字段长度
	private String data_length;
	//注释
	private String comments;
	
	public Column(){
		
	}
	
	public Column(String table_name,String column_name,String data_type,String data_length,String comments){
		this.table_name = table_name;
		this.column_name = column_name;
		this.data_type = data_type;
		this.data_length = data_length;
		this.comments = comments;
	}
	
	public String getTable_name() {
		return table_name;
	}
	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}
	public String getColumn_name() {
		return column_name;
	}
	public void setColumn_name(String column_name) {
		this.column_name = column_name;
	}
	public String getData_type() {
		return data_type;
	}
	public void setData_type(String data_type) {
		this.data_type = data_type;
	}
	public String getData_length() {
		return data_length;
	}
	public void setData_length(String data_length) {
		this.data_length = data_length;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public String toString() {
		return "[table_name="+table_name
			+ ",column_name="+ column_name
			+ ",data_type=" + data_type
			+ ",data_length=" +data_length
			+ ",comments=" + comments+ "]";
	}
	
	
}
