/******************************************************************************
 * Copyright (C) ShenZhen Powerdata Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳市博安达信息技术股份有限公司开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 *****************************************************************************/

package com.szboanda.platform.util.dao;

import com.szboanda.platform.util.*;
import com.szboanda.platform.util.domain.DBConstants;
import com.szboanda.platform.util.exception.PlatformException;
import com.szboanda.platform.util.helper.NamedQueryHelper;
import com.szboanda.platform.util.resources.Configuration;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.util.List;

/**
 * 数据库操作对象
 * <pre>
 * 可以得到数据库连接，根据指定的sql语句查询出列表数据，单行数据等功能
 * </pre>
 *
 * @company    深圳市博安达信息技术股份有限公司
 * @author     王贤炳
 * @since      2018/11/8
 */
public class JdbcDAO {

    /**
     * 数据源
     *
     * @since 	V0.1.0
     */
    private String dsJndi;

    /**
     * 无参构造方法
     *
     * @since 	V0.1.0
     */
    public JdbcDAO() {
    }

    /**
     * 获取数据连接
     * @return connection
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public Connection getConnection(){
        Connection connection = null;
        String dbUserNameDest = Configuration.getProperty(DBConstants.DB_USERNAME, "");
        String dbPassWordDest = Configuration.getProperty(DBConstants.DB_PASSWORD, "");
        String dbDriverDest = Configuration.getProperty(DBConstants.DB_DRIVER, "");
        String dbUriDest = Configuration.getProperty(DBConstants.DB_DBURI, "");
        try {
            Class.forName(dbDriverDest);
            connection = DriverManager.getConnection(dbUriDest, dbUserNameDest, dbPassWordDest);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 根据Jndi得到数据库连接
     *
     * @param 	jndi JNDI
     * @return	数据库连接
     *
     * @since 	V0.1.0
     */
    public Connection getConnection(String jndi) {
        return getConnection();
    }

    /**
     * 根据数据源实例JdbcDAO
     *
     * @param 	dsJndi2 数据源
     *
     * @since 	V0.1.0
     */
    public JdbcDAO(String dsJndi2) {
        /*if(PlatformMode.isDebugMode()){
            ProxoolHelper helper = ProxoolHelper.getInstance();
            helper.bindDataSource();
        }
        if(StringUtils.isBlank(dsJndi2)){
            dsJndi2 = getDefaultDataSource();
        }
        this.dsJndi = dsJndi2;*/
    }

    /**
     * 得到默认的数据源
     *
     * @return	 默认的数据源
     *
     * @since 	V0.1.0
     */
    public static String getDefaultDataSource(){
        return Configuration.getProperty(Constants.PLATFORM_JDBC_JNDI, Constants.PLATFORM_JDBC_JNDI_VALUE);
    }

    /**
     * 执行SQL查询 不返回结果集
     *
     * @param 	connection 查询所依赖的Connection对象
     * @param 	helper  查询参数使用的QueryHelper对象
     * @return 	影响的行数量
     *
     * @since 	V0.1.0
     */
    public int executeSQL(Connection connection, IQueryHelper helper){
        PreparedStatement ptmt = null;
        int result = 0;
        if(connection == null){
            try{
                connection = this.getConnection();
                result = this.executeSQL(connection, helper);
            }catch(Exception e){
                throw PlatformException.processException(e);
            }finally{
                CloseUtils.closeConnections(connection);
            }
        }else{
            try{
                ptmt = connection.prepareStatement(helper.getHql());
                String[] paramNames = helper.getArgNames();
                Integer[] types = helper.getJdbcTypes();
                Object[] values = helper.getArgs();
                if ( ( paramNames != null ) && ( paramNames.length > 0) ){
                    for(int i = 0 ; i < paramNames.length ;i++){
                        //如果设置的参数为空则使用索引作为参数位置
                        int index = StringUtils.isEmpty(paramNames[i])?(i+1):Integer.parseInt(paramNames[i],10);
                        this.setParameter(ptmt, index, types[i], values[i]);
                    }
                }
                result = ptmt.executeUpdate();
            }catch(Exception e){
                throw PlatformException.processException(e);
            }finally{
                CloseUtils.closeStatements(ptmt);
            }
        }
        return result;
    }

    /**
     * 根据参数设置PreparedStatement的参数
     *
     * @param 	ptmt        需要设置值的PreparedStatement对象
     * @param 	index       参数的索引从1开始
     * @param 	type        参数类型
     * @param 	value       参数的值
     * @return	设置后的PreparedStatement
     * @throws	SQLException
     *
     * @since	V0.1.0
     */
    private PreparedStatement setParameter(PreparedStatement ptmt,int index,Integer type,Object value) throws SQLException{
        //设置String类型的参数
        if( type == Types.VARCHAR ){
            if(value == null){
                ptmt.setNull(index,Types.VARCHAR);
            }else{
                ptmt.setString( index, value.toString() );
            }
            //设置Date类型的参数
        }else if( type == Types.DATE ){
            if(value == null){
                ptmt.setNull(index, Types.TIMESTAMP);
            }else if(value instanceof java.util.Date){
                java.util.Date val = (java.util.Date) value;
                ptmt.setTimestamp( index , new Timestamp(DateUtils.getSQLDate( val).getTime()));
            }else if(value instanceof Date){
                ptmt.setTimestamp( index , new Timestamp(((Date) value).getTime()));
            }else{
                throw new RuntimeException("设置参数类型出错【只支持将java.util.Date和java.sql.Date转换为数据库DATE类型】");
            }
            //设置Int类型的参数
        }else if( type == Types.INTEGER ){
            if(value == null){
                //实际应该设置为null
                ptmt.setNull(index, Types.NUMERIC);
            }else if(value instanceof Number){
                Number val = (Number) value;
                ptmt.setInt( index, val.intValue());
            }else{
                throw new RuntimeException("设置参数类型出错【只支持将Number类型的数据转换为数据库INT类型】");
            }

        }else if( type == Types.FLOAT ){
            if(value == null){
                ptmt.setNull(index, Types.NUMERIC);
            }else if(value instanceof Number){
                Number val = (Number) value;
                ptmt.setFloat( index, val.floatValue() );
            }else{
                throw new RuntimeException("设置参数类型出错【只支持将Number类型的数据转换为数据库FLOAT类型】");
            }
            //设置Double类型的参数
        }else if( type == Types.DOUBLE ){
            if(value == null){
                ptmt.setNull(index, Types.NUMERIC);
            }else if(value instanceof Number){
                Number val = (Number) value;
                ptmt.setDouble( index, val.doubleValue());
            }else{
                throw new RuntimeException("设置参数类型出错【只支持将Number类型的数据转换为数据库DOUBLE类型】");
            }
        }else if(type == Types.NUMERIC){
            if(value == null){
                ptmt.setNull(index, Types.NUMERIC);
            }else if(value instanceof Number){
                Number val = (Number) value;
                ptmt.setBigDecimal(index, new java.math.BigDecimal(val.doubleValue()));
            }
        }else if(type == Types.DECIMAL){
            if(value == null){
                ptmt.setNull(index, Types.NUMERIC);
            }else if(value instanceof Number){
                Number val = (Number) value;
                ptmt.setBigDecimal(index, new java.math.BigDecimal(val.doubleValue()));
            }
        }else if(type == Types.BLOB){
            //添加对大字段的操作
            if(value == null){
                ptmt.setNull(index, Types.BLOB);
            }else{
                ByteArrayOutputStream os = getStreamByObject(value);
                byte[] tempbuf = os.toByteArray();
                ByteArrayInputStream is = new ByteArrayInputStream(tempbuf);
                ptmt.setBinaryStream(index, is, tempbuf.length);
            }
        }else{
            throw new RuntimeException("设置参数类型出错【不支持的数据类型】");
        }
        return ptmt;
    }

    /**
     * 将对象转换为空字节数组流
     *
     * @param	obj Object对象
     * @return	空字节数组流
     *
     * @author 	刘剑
     * @since	V0.1.0
     */
    protected ByteArrayOutputStream getStreamByObject(Object obj)throws PlatformException{
        ByteArrayOutputStream os = null;
        try{
            os = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(obj);
            oos.close();

            return os;
        }catch(Exception e){
            e.printStackTrace();
            throw new PlatformException("转换字节数据流出错！");
        }
    }

    /**
     * 根据数据库连接与IQueryHelper对象得到DynaBean的列表数据
     *
     * @param 	conn 数据库连接
     * @param 	qHelper	IQueryHelper对象
     * @return	DynaBean的列表数据
     *
     * @since	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public List<DynaBean> getListValue(Connection conn, IQueryHelper qHelper) {
        try{
            RowSetDynaClass dynaClass = getDynaClass(conn,qHelper);
            return dynaClass==null?null:(List<DynaBean>)dynaClass.getRows();
        }catch(Exception e){
            throw PlatformException.processException(e);
        }
    }

    /**
     * 查询出一个RowSetDynaClass
     *
     * @param 	conn 数据库连接
     * @param 	qHelper IQueryHelper对象
     * @return	RowSetDynaClass
     * @throws 	Exception
     *
     * @since 	V0.1.0
     */
    public RowSetDynaClass getDynaClass(Connection conn,IQueryHelper qHelper){
        try{
            return getDynaClass(conn,qHelper,false,-1);
        }catch(Exception e){
            throw PlatformException.processException(e);
        }
    }
    @SuppressWarnings("unchecked")
    public List<DynaBean> getListValue(Connection conn, NamedQueryHelper qHelper) {
        try{
            RowSetDynaClass dynaClass = getDynaClass(conn,qHelper);
            return dynaClass==null?null:(List<DynaBean>)dynaClass.getRows();
        }catch(Exception e){
            throw PlatformException.processException(e);
        }
    }

    /**
     * 查询出一个RowSetDynaClass
     * @param conn
     * @param qHelper
     * @return
     * @throws Exception
     */
    public RowSetDynaClass getDynaClass(Connection conn,NamedQueryHelper qHelper){
        try{
            return getDynaClass(conn,qHelper,false,-1);
        }catch(Exception e){
            throw PlatformException.processException(e);
        }
    }

    /**
     * 查询出一个RowSetDynaClass 并且可以指定DynaProperty的name是否使用小写
     * 指定最多查询出的行数
     * @param conn       数据库连接 如果为空将主动获得一个Connection
     * @param qHelper    查询信息的相关对象
     * @param lowerCase  字段名称是否使用小写
     * @param limit      最多提取的行数，如果是负数则没有限制
     * @return
     * @throws Exception
     */
    public RowSetDynaClass getDynaClass(Connection conn,NamedQueryHelper qHelper,boolean lowerCase,int limit) throws Exception{
        RowSetDynaClass dynaClass = null;
        if(conn == null){
            try {
                //如果传入的Connection对象为null，就自己获得一个Connection
                conn = this.getConnection();
                dynaClass = getDynaClass(conn,qHelper,lowerCase,limit);
            } finally {
                //用完后关闭
                CloseUtils.closeConnections(conn);
            }
        }else{
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try{
                stmt = conn.prepareStatement(qHelper.getSql());
                qHelper.bind(stmt);
                rs = stmt.executeQuery();
                //modify by 廖声乐 2012-06-18 增加第四个参数
                dynaClass = new RowSetDynaClass(rs,lowerCase,limit,true);
            }catch(Exception e){
                throw PlatformException.processException(e);
            }finally{
                CloseUtils.closeResultSets(rs);
                CloseUtils.closeStatements(stmt);
            }
        }
        return dynaClass;
    }
    /**
     * 查询出一个RowSetDynaClass 并且可以指定DynaProperty的name是否使用小写
     * 指定最多查询出的行数
     *
     * @param 	conn       数据库连接 如果为空将主动获得一个Connection
     * @param 	qHelper    查询信息的相关对象
     * @param 	lowerCase  字段名称是否使用小写
     * @param 	limit      最多提取的行数，如果是负数则没有限制
     * @return 	RowSetDynaClass
     * @throws 	Exception
     *
     * @since	V0.1.0
     */
    public RowSetDynaClass getDynaClass(Connection conn,IQueryHelper qHelper,boolean lowerCase,int limit) throws Exception{
        RowSetDynaClass dynaClass = null;
        if(conn == null){
            try {
                //如果传入的Connection对象为null，就自己获得一个Connection
                conn = this.getConnection();
                dynaClass = getDynaClass(conn,qHelper,lowerCase,limit);
            } finally {
                //用完后关闭
                CloseUtils.closeConnections(conn);
            }
        }else{
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try{
                stmt = conn.prepareStatement(qHelper.getHql());
                //如果设置了查询参数
                if((qHelper.getArgNames() != null) && (qHelper.getArgNames().length > 0)){
                    for(int i = 0;i<qHelper.getArgNames().length;i++){
                        this.setParameter(stmt, Integer.parseInt(qHelper.getArgNames()[i])
                                , qHelper.getJdbcTypes()[i], qHelper.getArgs()[i]);
                    }
                }
                rs = stmt.executeQuery();
                //modify by 廖声乐 2012-06-18 增加第四个参数
                dynaClass = new RowSetDynaClass(rs,lowerCase,limit,true);
            }catch(Exception e){
                throw PlatformException.processException(e);
            }finally{
                CloseUtils.closeResultSets(rs);
                CloseUtils.closeStatements(stmt);
            }
        }
        return dynaClass;
    }

    /**
     * 强制回滚
     *
     * @param connection 需要回滚的链接对象
     *
     * @author  康庆
     * @version 2009-3-28
     * @since	V0.1.0
     */
    public static final void rollback(Connection connection){
        try{
            connection.rollback();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 执行SQL查询 不返回结果集
     *
     * @param 	connection 查询所依赖的Connection对象
     * @param 	helper  查询参数使用的QueryHelper对象
     * @return 	影响的行数量
     *
     * @since	V0.1.0
     */
    public int executeSQL(Connection connection,NamedQueryHelper helper){
        PreparedStatement ptmt = null;
        int result = 0;
        if(connection == null){
            try{
                connection = this.getConnection();
                result = this.executeSQL(connection, helper);
            }catch(Exception e){
                throw PlatformException.processException(e);
            }finally{
                CloseUtils.closeConnections(connection);
            }
        }else{
            try{
                ptmt = connection.prepareStatement(helper.getSql());
                helper.bind(ptmt);
                result = ptmt.executeUpdate();
            }catch(Exception e){
                throw PlatformException.processException(e);
            }finally{
                CloseUtils.closeStatements(ptmt);
            }
        }
        return result;
    }

    /**
     * 查询出一个DynaBean 最多只查询出一行
     * 指定最多查询出的行数
     *
     * @param 	conn       数据库连接 如果为空将主动获得一个Connection
     * @param 	qHelper    查询信息的相关对象
     * @return 	DynaBean数据对象
     * @throws 	Exception
     *
     * @since	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public DynaBean getSingleRow(Connection conn,IQueryHelper qHelper) {
        try{
            RowSetDynaClass dynaClass = getDynaClass(conn,qHelper,false,1);
            List<DynaBean> list = (List<DynaBean>)dynaClass.getRows();
            if((list != null) && (list.size() > 0)){
                return list.get(0);
            }
            return null;
        }catch(Exception e){
            throw PlatformException.processException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public DynaBean getSingleRow(Connection conn,NamedQueryHelper qHelper) {
        try{
            RowSetDynaClass dynaClass = getDynaClass(conn,qHelper,false,1);
            List<DynaBean> list = (List<DynaBean>)dynaClass.getRows();
            if((list != null) && (list.size() > 0)){
                return list.get(0);
            }
            return null;
        }catch(Exception e){
            throw PlatformException.processException(e);
        }
    }
    /**
     * 关闭数据库
     * @param conn
     * @throws SQLException
     */
    public static  void closeConnection(Connection conn) {
        if(conn != null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行SQL批处理 确保所有执行语句中的SQL一致才能使用该方法(即是对同一张表的相同字段作相同操作)
     *
     * @param 	connection 执行SQL所依赖的Connection对象
     * @param 	list  执行使用的QueryHelper对象
     * @return 	影响的行数量的集合
     *
     * @author 	廖声乐
     * @version	2013-11-06
     * @since	V0.1.0
     */
    public int[] executeBatch(Connection connection,List<IQueryHelper> list){
        if(list == null || list.size() == 0){
            throw new PlatformException("传入批处理的命令为空!");
        }
        PreparedStatement ptmt = null;
        int[] result = null;
        if(connection == null){
            try{
                connection = this.getConnection();
                connection.setAutoCommit(false);
                result = this.executeBatch(connection, list);
                connection.commit();
            }catch(Exception e){
                rollback(connection);
                throw PlatformException.processException(e);
            }finally{
                CloseUtils.closeConnections(connection);
            }
        }else{
            try{
                String hql = list.get(0).getHql();
                ptmt = connection.prepareStatement(hql);
                for(IQueryHelper helper : list){
                    String tempHql = helper.getHql();
                    if(!hql.equals(tempHql)){
                        throw new PlatformException("传入批处理的命令不一致!");
                    }
                    String[] paramNames = helper.getArgNames();
                    Integer[] types = helper.getJdbcTypes();
                    Object[] values = helper.getArgs();
                    if ( ( paramNames != null ) && ( paramNames.length > 0) ){
                        for(int i = 0 ; i < paramNames.length ;i++){
                            int index = StringUtils.isEmpty(paramNames[i])?(i+1):Integer.parseInt(paramNames[i],10);
                            this.setParameter(ptmt, index, types[i], values[i]);
                        }
                    }
                    ptmt.addBatch();
                }
                return ptmt.executeBatch();
            }catch(Exception e){
                throw PlatformException.processException(e);
            }finally{
                CloseUtils.closeStatements(ptmt);
            }
        }
        return result;
    }

    /**
     * 获取数据库类型
     *
     * @return	数据库类型
     *
     * @since	V0.1.0
     */
    public DatabaseType getDatabaseType(){
        Connection connection = null;
        try{
            connection = this.getConnection();
            return getDatabaseType(connection);
        }catch(Exception f){
            throw PlatformException.processException(f);
        }finally{
            CloseUtils.close(connection);
        }
    }

    /**
     * 根据数据库连接获取数据库类型
     *
     * @return 	数据库类型
     *
     * @since 	V0.1.0
     */
    public DatabaseType getDatabaseType(Connection connection){
        try{
            String str = connection.getMetaData().getDatabaseProductName().toUpperCase();
            String currentNumber = connection.getMetaData().getDatabaseProductVersion();
            if(str.indexOf("ORACLE") > -1){
                return DatabaseType.ORACLE;
            }else if(str.indexOf("MICROSOFT") > -1){
                if(currentNumber.indexOf("8.00") > -1){
                    return DatabaseType.MSSQL;
                }else if(currentNumber.indexOf("9.00") > -1){
                    return DatabaseType.MSSQL2005;
                }else if(currentNumber.indexOf("10.") > -1){
                    return DatabaseType.MSSQL2008;
                }else if(currentNumber.indexOf("11.") > -1){
                    return DatabaseType.MSSQL2012;
                }else{
                    return DatabaseType.MSSQL;
                }
            }else if(str.indexOf("MYSQL") > -1){
                return DatabaseType.MYSQL;
            }else if(str.indexOf("SQLITE") > -1){
                return DatabaseType.SQLITE;
            }else if(str.indexOf("DB2") > -1){
                return DatabaseType.DB2;
            }else if(str.indexOf("SYBASE") > -1){
                return DatabaseType.SYBASE;
            }else{
                return DatabaseType.UNKNOW;
            }
        }catch(Exception f){
            throw PlatformException.processException(f);
        }
    }
}
