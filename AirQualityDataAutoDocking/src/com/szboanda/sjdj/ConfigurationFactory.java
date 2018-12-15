package com.szboanda.sjdj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.szboanda.platform.util.exception.PlatformException;

/**
 * 深圳市站空气质量系统自动对接程序
 * 
 * @Company  深圳博安达软件开发有限公司
 * @author   王贤炳
 * @version  2015年12月25日
 */
public class ConfigurationFactory {
	private static final String TARGET_DB_USERNAME = "sjdj.target.db.username";
	private static final String TARGET_DB_PASSWORD = "sjdj.target.db.password";
	private static final String TARGET_DB_DRIVER = "sjdj.target.db.driver";
	private static final String TARGET_DB_DBURL = "sjdj.target.db.dburl";
	
	private static final String REQUEST_ADDRESS  = "sjdj.request.address";
	private static final String TIMEPOINT  = "sjdj.request.param.timePoint";
	private static final String AREANO  = "sjdj.request.param.areaNo";
	private static final String IDENTITYCODE  = "sjdj.request.param.identityCode";
	private static final String BEGINDATE  = "sjdj.request.param.beginDate";
	private static final String ENDDATE  = "sjdj.request.param.endDate";
	
	private Properties properties = null;
	private String dbUserName = null;
	private String dbPassWord = null;
	private String dbDriver = null;
	private String dbUrl = null;
	private String address = null;
	private String timePoint = null;
	private String identitycode = null;
	private String areaNo = null;
	private String beginDate = null;
	private String endDate = null;
	private Map<String,String> areaParam = new HashMap<String, String>();
	private static ConfigurationFactory single = null;  
	
	private ConfigurationFactory() {
		 init();
	}  
    
    //静态工厂方法   
    public static ConfigurationFactory getInstance() {  
         if (single == null) {    
             single = new ConfigurationFactory();  
         }    
        return single;  
    } 
    
	 /**
	  * 初始化数据
	  * @throws ClassNotFoundException
	  * @throws SQLException
	  *
	  * @author  王贤炳
	  * @since   2015年12月25日
	  * @version V0.2.0
	 * @throws IOException 
	  */
	 public void init(){
		 try {
			loadServiceProperties();
		} catch (Exception e) {
			throw new PlatformException("初始化【sourceParam.properties】配置文件失败...", e.getMessage());
		}
		 try {
			loadAreaProperties();
		} catch (Exception e) {
			throw new PlatformException("初始化【areaNo.properties】配置文件失败...", e.getMessage());
		}
    }
	 
	/**
	 * 解析配置文件
	 *
	 * @author  王贤炳
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @since   2015年12月26日
	 */
	public void loadServiceProperties() throws IOException {
		 properties = new Properties();
		 //properties.load(this.getClass().getResourceAsStream("/sourceParam.properties"));
		 InputStream inputStream = this.getClass().getResourceAsStream("/sourceParam.properties");
		 BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));  
		 properties.load(bf);
	     this.setDbUserName(properties.getProperty(TARGET_DB_USERNAME));
	     this.setDbPassWord(properties.getProperty(TARGET_DB_PASSWORD));
	     this.setDbDriver(properties.getProperty(TARGET_DB_DRIVER));
	     this.setDbUrl(properties.getProperty(TARGET_DB_DBURL));
	     this.setAddress(properties.getProperty(REQUEST_ADDRESS));
	     this.setTimePoint(properties.getProperty(TIMEPOINT));
	     this.setAreaNo(properties.getProperty(AREANO));
	     this.setIdentitycode(properties.getProperty(IDENTITYCODE));
	     this.setBeginDate(properties.getProperty(BEGINDATE));
	     this.setEndDate(properties.getProperty(ENDDATE));
	}
	 
	/**
	 * 解析area.properties文件,读取区域代码配置关系
	 * 
	 *
	 * @author  王贤炳
	 * @throws IOException 
	 * @since   2015年12月26日
	 */
	public void loadAreaProperties() throws IOException{
		 properties = new Properties();
		 //properties.load(this.getClass().getResourceAsStream("/areaNo.properties"));
		 InputStream inputStream = this.getClass().getResourceAsStream("/areaNo.properties");
		 BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream,  "UTF-8"));  
		 properties.load(bf); 
		 for(Entry<Object, Object> entry : properties.entrySet()){
			 this.areaParam.put(entry.getKey().toString(), entry.getValue().toString());
		 }
	}
	
	public Connection getConnection() {
		 Connection connection = null;
		 try {
			//初始化目标数据库配置信息
			 String dbUserName = this.getDbUserName();
			 String dbPassWord = this.getDbPassWord();
			 String dbDriver = this.getDbDriver();
			 String dbUrl = this.getDbUrl();
			 Class.forName(dbDriver);
			 connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassWord);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return connection;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public String getTimePoint() {
		return timePoint;
	}

	public void setTimePoint(String timePoint) {
		this.timePoint = timePoint;
	}

	public String getIdentitycode() {
		return identitycode;
	}

	public void setIdentitycode(String identitycode) {
		this.identitycode = identitycode;
	}

	public String getAreaNo() {
		return areaNo;
	}

	public void setAreaNo(String areaNo) {
		this.areaNo = areaNo;
	}

	public Map<String, String> getAreaParam() {
		return areaParam;
	}

	public void setAreaParam(Map<String, String> areaParam) {
		this.areaParam = areaParam;
	}

	public String getDbUserName() {
		return dbUserName;
	}

	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}

	public String getDbPassWord() {
		return dbPassWord;
	}

	public void setDbPassWord(String dbPassWord) {
		this.dbPassWord = dbPassWord;
	}

	public String getDbDriver() {
		return dbDriver;
	}

	public void setDbDriver(String dbDriver) {
		this.dbDriver = dbDriver;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(String beginDate) {
		this.beginDate = beginDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
}
