package com.szboanda.platform.tools;

import java.sql.Connection;
import java.util.Map;

import com.szboanda.platform.tools.utils.DaoHelper;

public class T {
	/**
	 * @Title: main
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param args   
	 * @return void  返回类型
	 * @throws
	 */
	public static void main(String[] args) {
		Connection s_conn = null;
		Connection t_conn = null;
		try {
			// 获取连接
			s_conn = DaoHelper.getConnection("source.properties");
			t_conn = DaoHelper.getConnection("target.properties");
			ColumnService serv = new ColumnService();
			Map<String,Map<String,Column>> s_tables = serv.getColums(s_conn,"T_ADMIN_");
			Map<String,Map<String,Column>> t_tables = serv.getColums(t_conn);
			System.out.println("源表：" + s_tables.size());
			System.out.println("目标表：" + t_tables.size());
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{s_conn.close();}catch (Exception e){};
			try{t_conn.close();}catch (Exception e){};
		}

	}

}
