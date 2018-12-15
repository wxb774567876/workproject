package com.szboanda.platform.tools;
/**
 * 
  * 此类描述的是：常量类
  * @Title: Constant.java
  * @author: wangjihong
  * @Copyright: PowerData Software Co.,Ltd. Rights Reserved.
  * @Company:深圳市博安达软件开发有限公司
  * @version:1.0
  * create date:Feb 8, 2014 10:26:17 AM
 */
public interface Constant {
	/**'
	 * 查询数据库sql
	 */
	public static final String QUERY_COLUMN_SQL = "select col.TABLE_NAME,col.COLUMN_NAME,col.DATA_TYPE,col.DATA_LENGTH,comm.COMMENTS from user_tab_columns col"
		+" left join  user_col_comments comm on col.TABLE_NAME=comm.TABLE_NAME and col.COLUMN_NAME = comm.COLUMN_NAME WHERE 1=1 #WHERESTRING# "
		+ " order by col.TABLE_NAME ";
	/**
	 * 表示目标表中没有所需要的表
	 */
	public static final String CHECK_RESULT_NOTABLE = "CHECK_RESULT_NOTABLE";
	/**
	 * 表示目标库中有需要的表，但字段类型有误
	 */
	public static final String CHECK_RESULT_ERRORCOLUMN = "CHECK_RESULT_ERRORCOLUMN";
	/**
	 * 表时目标表中没有所需要的字段
	 */
	public static final String CHECK_RESULT_NOCOLUMN = "CHECK_RESULT_NOCOLUMN";
	/**
	 * 表示目标表中数据类型出错
	 */
	public static final String CHECK_RESULT_COLUMNTYPE = "CHECK_RESULT_COLUMNTYPE";
	/**
	 * 表示目标表中数据类型长度出错
	 */
	public static final String CHECK_RESULT_COLUMNLENGTH = "CHECK_RESULT_COLUMNLENGTH";
	/**
	 * 标识数据库比对类型
	 */
	public static final String COMPARE_TYPE_YES = "1";
	/**
	 * 标识数据库比对类型
	 */
	public static final String COMPARE_TYPE_NO = "0";
	
	public static final String altTABALE = " alter table T_ADMIN_RMS_ZZJG add zzjg varchar2(20);\n "
										 + " comment on column T_ADMIN_RMS_ZZJG.zzjg is 'weewe'; ";
	
}
