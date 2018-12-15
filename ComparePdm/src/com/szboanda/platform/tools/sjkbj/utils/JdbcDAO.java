package com.szboanda.platform.tools.sjkbj.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

public class JdbcDAO {
	   public Connection getConnection(String jndi) throws Exception  {
	        DataSource ds = null;
	        Connection cn = null;
	        try {      
	            Context context = new InitialContext();
	            Context envContext = null;
	            try{
	            	envContext = (Context) context.lookup("java:/comp/env");
	            }catch(Exception ff){
	            	//在测试的环境下无法通过java:方式获得
	            	try{
	            		envContext = (Context) context.lookup("/comp/env");
	            	}catch(Exception fff){}
	            }
	            ds = (DataSource) envContext.lookup(jndi);
	            cn = ds.getConnection();   
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new Exception(e); 
	        }
	        return cn;
	    }
	   
	   /**
	    * 具体获取数据库连接实现
	    * @param properties
	    * @return
	    * @throws Exception
	    */
		public Connection getConnection(String url,String driver,String user,String password) throws Exception {
			InputStream fis = null;
			Connection connection = null;
			try {
				Class.forName(driver);
				connection = DriverManager.getConnection(url, user, password);
			} catch (Exception f) {
				throw new Exception(f);
			} finally {
				IOUtils.closeQuietly(fis);
			}
			return connection;
		}
}
