package com.szboanda.platform.tools.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
/**
* @ClassName: DaoHelper
* @Description: 获取数据库连接的帮助类
* @author wangjihong
* @date 2013-9-16 上午11:25:29
 */
public class DaoHelper {
	//默认配置文件名称
	public static final String PROPERTIES = "unittest.properties";
	//获取数据库连接
	public static Connection getConnection() throws Exception {
		return getConnection(PROPERTIES);
	}
   /**
    * 具体获取数据库连接实现
    * @param properties
    * @return
    * @throws Exception
    */
	public static Connection getConnection(String properties) throws Exception {
		Connection connection = null;
		try {
			String url = getString(properties,"url");
			String driver = getString(properties,"driver");
			String user = getString(properties,"user");
			String password = getString(properties,"password");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
		} catch (Exception f) {
			throw new Exception(f);
		} finally {
		}
		return connection;
	}
	
	/**
	 * 根据绝对路径获取数据库连接
	 * @param path 绝对路径
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection2(String path) throws Exception {
		return getConnection2(path ,PROPERTIES);
	}
	/**
	 * 根据绝对路径和配置文件名称获取数据库连接
	 * @param path
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection2(String path,String properties) throws Exception {
		Properties propfile = new Properties();
		InputStream fis = null;
		Connection connection = null;
		try {
			fis = FileLoader.readFile(path + "/" + properties);
			propfile.load(fis);
			String url = propfile.getProperty("url");
			String driver = propfile.getProperty("driver");
			String user = propfile.getProperty("user");
			String password = propfile.getProperty("password");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
		} catch (Exception f) {
			throw new Exception(f);
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return connection;
	}
	
	public static String getString(String properties,String key)throws Exception {
		String resultStr = "";
		Properties propfile = new Properties();
		InputStream fis = null;
		try {
			fis = FileLoader.loadFile(properties);
			propfile.load(fis);
			resultStr =  propfile.getProperty(key);
		} catch (Exception f) {
			throw new Exception(f);
		} finally {
			IOUtils.closeQuietly(fis);
		}
		return resultStr;
	}
}
