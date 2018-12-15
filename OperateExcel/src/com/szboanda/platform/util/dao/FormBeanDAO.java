/******************************************************************************
 * Copyright (C) ShenZhen Powerdata Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳市博安达信息技术股份有限公司开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 *****************************************************************************/

package com.szboanda.platform.util.dao;

import com.szboanda.platform.dts.Entity;
import com.szboanda.platform.dynaquery.entity.DataType;
import com.szboanda.platform.util.*;
import com.szboanda.platform.util.dao.DataTable.DataTableColumn;
import com.szboanda.platform.util.dao.event.DaoEvent;
import com.szboanda.platform.util.dao.event.DaoEventManager;
import com.szboanda.platform.util.dao.event.IDaoListener;
import com.szboanda.platform.util.exception.PlatformException;
import com.szboanda.platform.util.helper.ActionHelper;
import com.szboanda.platform.util.helper.NamedQueryHelper;
import com.szboanda.platform.util.helper.QueryHelper;
import com.szboanda.platform.util.record.DeleteRecordDetail;
import com.szboanda.platform.util.record.InsertRecordDetail;
import com.szboanda.platform.util.record.UpdateRecordDetail;
import com.szboanda.platform.util.resources.Configuration;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 *
 * 重写FormBeanDAO对象
 *
 * @company:    深圳市博安达信息技术股份有限公司
 * @copyright:  PowerData Software Co.,Ltd. Rights Reserved.
 * @author:     王贤炳
 * @since:      2018/12/14
 * @version:    V1.0
 */
public class FormBeanDAO {

    /** 创建人 */
    public static final String CJR = "CJR";

    /** 创建时间 */
    public static final String CJSJ = "CJSJ";

    /** 修改人 */
    public static final String XGR = "XGR";

    /** 修改时间 */
    public static final String XGSJ = "XGSJ";

    /** 组织代码 */
    public static final String ORGID = "ORGID";

    /** 插入 */
    public static final String INSERT = "INSERT";

    /** 更新 */
    public static final String UPDATE = "UPDATE";

    /** 删除 */
    public static final String DELETE = "DELETE";

    /** 查询 */
    public static final String SELECT = "SELECT";

    /** 插入前 */
    public static final String BEFORE_INSERT = "beforeInsert";

    /** 更新前 */
    public static final String BEFORE_UPDATE = "beforeUpdate";

    /** 删除前 */
    public static final String BEFORE_DELETE = "beforeDelete";

    /** 插入后 */
    public static final String AFTER_INSERT  = "afterInsert";

    /** 更新后 */
    public static final String AFTER_UPDATE  = "afterUpdate";

    /** 删除后 */
    public static final String AFTER_DELETE  = "afterDelete";

    /** 日志记录对象 */
    public static final Logger LOG = Logger.getLogger(FormBeanDAO.class);

    /** 最后的操作 */
    private String lastOperate = null;

    /** 数据库 */
    private String dsJndi = null;

    /** 系统运行模式 */
    private static final PlatformMode mode = PlatformMode.getMode();

    /** 数据库操作监听器 */
    private IDaoListener listener = null;

    /**
     * 注册数据库操作监听
     *
     * @param listener 数据库操作监听器
     *
     * @since V0.1.0
     */
    public void registerListener(IDaoListener listener){
        this.listener = listener;
    }

    /**
     * 得到默认的数据源对象
     *
     * @return 默认的数据源
     *
     * @since  V0.1.0
     */
    public static String getDefaultDataSource(){
        return Configuration.getProperty(Constants.PLATFORM_JDBC_JNDI,Constants.PLATFORM_JDBC_JNDI_VALUE);
    }

    /**
     * 无参构造
     *
     * @since V0.1.0
     */
    public FormBeanDAO(){
        dsJndi = getDefaultDataSource();
    }

    /**
     * 根据JNDI实例FormBeanDAO
     *
     * @param jndi2 JNDI
     *
     * @since V0.1.0
     */
    public FormBeanDAO(String jndi2){
        if(StringUtils.isBlank(jndi2)){
            jndi2 = getDefaultDataSource();
        }
        this.dsJndi = jndi2;
    }
    /**
     * 获取根据构造函数中指定的jndi的数据库连接
     * 如果构造函数中没有制定则使用系统默认的jndi
     *
     * @return 数据库连接
     *
     * @since  V0.1.0
     */
    public Connection getConnection(){
        return new JdbcDAO(dsJndi).getConnection();
    }

    /**
     * 插入操作,主键字段必须已经有值
     * 如果bean中没有为cjr,cjsj,xgr,xgsj赋值，程序自动赋值
     *
     * @param 	connection 数据库连接
     * @param 	table	数据库表
     * @param 	bean DynaBean对象
     * @return 	操作影响的行数
     *
     * @since	V0.1.0
     */
    public int insert(Connection connection,String table,DynaBean bean){
        return insert(connection, table, convert(bean));
    }

    /**
     * 将DynaBean转换成Map对象
     *
     * @param 	bean DynaBean
     * @return 	Map对象
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    private Map convert(DynaBean bean){
        Map map = null;
        if(bean instanceof Map){
            map = (Map)bean;
        }else if(bean instanceof LazyDynaBean){
            map = ((LazyDynaBean)bean).getMap();
        }else{
            map = new DynaBeanMapDecorator(bean);
        }
        return map;
    }

    /**
     * 根据数据库连接，表名，MAP对象插入数据
     *
     * @param 	connection 数据库连接
     * @param 	table 表名
     * @param 	map MAP对象
     * @return 	操作影响的行数
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public int insert(Connection connection,String table,Map map){
        DataTable dataTable = DataTable.findDataTable(this.dsJndi,connection,table);
        if((dataTable == null) || (!dataTable.canInsert())){
            throw new PlatformException("表[" + table + "]元数信息不全，或者没有字段，无法执行插入!");
        }
        JdbcDAO dao = new JdbcDAO(dsJndi);
        DaoEvent event = new DaoEvent(map,dataTable);
        try{
            int i = 0;
            boolean valid = DaoEventManager.getInstance().fireBeforeEvent(connection, listener, BEFORE_INSERT, event);
            if(valid){
                i = dao.executeSQL(connection, buildInsertHelper(connection, dataTable, map));
                this.lastOperate = INSERT;
            }
            DaoEventManager.getInstance().fireAfterEvent(connection, listener, AFTER_INSERT, event);
            return i;
        }catch (Exception e) {
            throw new PlatformException("表[" + table + "]执行插入操作发生异常!" + e.getMessage(), e);
        }
    }

    /**
     * 保存操作
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	map	Map对象
     * @return 	操作影响的行数
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public int save(Connection connection,String table,Map map){
        if(getRowByKey(connection,table,map) != null){
            return update(connection,table,map);
        }else{
            return insert(connection,table,map);
        }
    }

    /**
     * 保存操作
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	bean	DynaBean对象
     * @return 	操作影响的行数
     *
     * @since 	V0.1.0
     */
    public int save(Connection connection,String table,DynaBean bean){
        //modify by 廖声乐 2013-04-24
        //return insert(connection, table, convert(bean));
        return save(connection, table, convert(bean));
    }

    /**
     * 根据主键查询数据
     *
     * @param 	connection 数据库连接
     * @param 	table 表名
     * @param 	bean DynaBean对象
     * @return 	DynaBean对象
     *
     * @since 	V0.1.0
     */
    public DynaBean getRowByKey(Connection connection,String table,DynaBean bean){
        return getRowByKey(connection, table, convert(bean));
    }

    /**
     * 根据主键查询数据
     *
     * @param 	connection 数据库连接
     * @param 	table 表名
     * @param 	map Map对象
     * @return 	DynaBean对象
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public DynaBean getRowByKey(Connection connection,String table,Map map){
        DataTable dataTable = DataTable.findDataTable(this.dsJndi,connection, table);
        if(dataTable == null){
            throw new PlatformException("表[" + table + "]元数信息不全，或者没有字段，无法执行查询!");
        }
        NamedQueryHelper helper = new NamedQueryHelper(dataTable.getSelectString());
        for(DataTableColumn dtc : dataTable.getKeys()){
            String name = dtc.getName();
            int type = DataType.parseSqlType(dtc.getType());
            helper.bindParameter(dtc.getName(), type, parseParameterValue(dtc, map.get(name)));
        }
        this.lastOperate = SELECT;
        return new JdbcDAO(dsJndi).getSingleRow(connection, helper);
    }

    /**
     * 根据主键判断数据是否存在
     *
     * @param 	connection 数据库连接
     * @param 	table 表名
     * @param 	bean DynaBean对象
     * @return 	数据是否存在
     *
     * @since 	V0.1.0
     */
    public boolean isExistsByKey(Connection connection,String table,DynaBean bean){
        return isExistsByKey(connection, table, convert(bean));
    }

    /**
     * 根据主键判断数据是否存在
     *
     * @param 	connection 数据库连接
     * @param 	table 表名
     * @param 	map Map对象
     * @return 	数据是否存在
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public boolean isExistsByKey(Connection connection,String table,Map map){
        DataTable dataTable = DataTable.findDataTable(this.dsJndi,connection, table);
        if(dataTable == null || dataTable.getKeys().size() < 1){
            throw new PlatformException("表[" + table + "]元数信息不全!");
        }

        if(!allKeyHasValue(dataTable,map)){
            throw new PlatformException("并未对[" + table + "]中的所有的主键设置值，无法进行验证数据是否存在操作!");
        }

        NamedQueryHelper helper = new NamedQueryHelper(dataTable.getExistsByKeyString());
        for(DataTableColumn dtc : dataTable.getKeys()){
            String name = dtc.getName();
            int type = DataType.parseSqlType(dtc.getType());
            helper.bindParameter(dtc.getName(), type, parseParameterValue(dtc, map.get(name)));
        }
        DynaBean bean = new JdbcDAO(dsJndi).getSingleRow(connection, helper);
        if(bean != null){
            return BeanUtils.getIntValue(bean, "CNT") > 0;
        }
        return false;
    }

    public String getLastOperate() {
        return lastOperate;
    }

    /**
     * 判断数据是否存在
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	map Map对象
     * @return 	数据是否存在
     *
     * @author 	廖声乐
     * @version 2012-05-23
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public boolean isExists(Connection connection,String table,Map<String,String> map){
        if(StringUtils.isEmpty(table) || map == null || map.size() == 0){
            throw new PlatformException("表名为空或者需要验证的字段数据信息为空!");
        }
        StringBuffer sql = new StringBuffer("SELECT COUNT(1) CNT FROM " + table + " WHERE ");
        NamedQueryHelper helper = new NamedQueryHelper();
        boolean first = true;
        for(String name : map.keySet()){
            if(!first){
                sql.append(" AND ");
            }
            // 添加值为空的处理，因为 field = '' 是查不到空数据的，要改成 is null
            // modify by 黄冠豪 2017/9/6
            final String value = map.get(name);
            if (StringUtils.isEmpty(value)) {
                sql.append(name).append(" IS NULL ");
            } else {
                sql.append(name).append(" = ").append("#").append(name).append("#");
                helper.bindParameter(name, Types.VARCHAR, value);
            }

            first = false;
        }
        helper.setUsql(sql.toString());
        DynaBean bean = new JdbcDAO(dsJndi).getSingleRow(connection, helper);
        if(bean != null){
            return BeanUtils.getIntValue(bean, "CNT") > 0;
        }
        return false;
    }

    /**
     * 得到单行数据
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	bean DynaBean对象
     * @return	DynaBean对象
     *
     * @since 	V0.1.0
     */
    public DynaBean getRowByColumns(Connection connection,String table,DynaBean bean){
        return getRowByColumns(connection, table, convert(bean));
    }

    /**
     * 得到单行数据
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	map Map对象
     * @return	DynaBean对象
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public DynaBean getRowByColumns(Connection connection,String table,Map map){
        DataTable dataTable = DataTable.findDataTable(this.dsJndi,connection, table);
        if(dataTable == null){
            throw new PlatformException("表[" + table + "]元数信息不全，或者没有字段，无法执行插入!");
        }
        NamedQueryHelper helper = buildSelectHelper(dataTable,map);
        return new JdbcDAO(dsJndi).getSingleRow(connection, helper);
    }


    /**
     * 根据数据库表信息描述与Map对象构造NamedQueryHelper对象
     *
     * @param 	dataTable 数据库表信息
     * @param 	map Map对象
     * @return 	NamedQueryHelper对象
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    private NamedQueryHelper buildSelectHelper(DataTable dataTable, Map map) {
        StringBuilder buffer = new StringBuilder(1024);
        buffer.append(" SELECT * FROM ").append(dataTable.getTable());
        boolean first = true;
        Set<String> temp = new HashSet<String>();
        for(Object o : map.keySet()){
            String name = o.toString().toUpperCase();
            if(dataTable.getColMaps().containsKey(name)){
                //modify by 廖声乐 2013-03-15
                if(first){
                    buffer.append(" WHERE  ");
                }else {
                    buffer.append(" AND ");
                }
                buffer.append(name).append(" = #").append(name).append("# ");
                first = false;
                temp.add(o.toString());
            }
        }
        //modify by 廖声乐 2013-03-15
        if(first){
            buffer.append(" WHERE 1 = 2 ");
        }
        NamedQueryHelper helper = new NamedQueryHelper(buffer.toString());
        for(String name : temp){
            DataTableColumn dtc = dataTable.findColumn(name.toUpperCase());
            helper.bindParameter(name.toUpperCase(), DataType.parseSqlType(dtc.getType()),
                    parseParameterValue(dtc, map.get(name)));
        }
        return helper;
    }

    /**
     * 更新操作，根据主键字段更新，所以主键字段必须有值
     * 如果bean中没有为xgr，xgsj赋值，程序会自动赋值
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	bean	DynaBean对象
     * @return 	操作影响的行数
     *
     * @since 	V0.1.0
     */
    public int update(Connection connection,String table,DynaBean bean){
        return update(connection, table, convert(bean));
    }

    /**
     * 更新操作，根据主键字段更新，所以主键字段必须有值
     * 如果bean中没有为xgr，xgsj赋值，程序会自动赋值
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	map	Map对象
     * @return 	操作影响的行数
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public int update(Connection connection,String table,Map map){
        DataTable dataTable = DataTable.findDataTable(this.dsJndi,connection, table);
        if((dataTable == null) || (!dataTable.canUpdate())){
            throw new PlatformException("表[" + table + "]元数信息不全,没有为主键赋值或者没有字段无法执行更新!");
        }
        if(!allKeyHasValue(dataTable,map)){
            throw new PlatformException("并未对[" + table + "]中的所有的主键设置值，无法进行更新操作!");
        }
        JdbcDAO dao = new JdbcDAO(dsJndi);
        DaoEvent event = new DaoEvent(map,dataTable);
        try{
            int i = 0;
            boolean valid = DaoEventManager.getInstance().fireBeforeEvent(connection, listener, BEFORE_UPDATE, event);
            if(valid){
                i = dao.executeSQL(connection, buildUpdateHelper(connection, dataTable, map));
                this.lastOperate = UPDATE;
            }
            DaoEventManager.getInstance().fireAfterEvent(connection, listener, AFTER_UPDATE, event);
            return i;
        }catch (Exception e) {
            throw new PlatformException("表[" + table + "]执行更新操作发生异常!" + e.getMessage(), e);
        }
    }

    /**
     * 删除操作，根据主键字段进行删除，所以主键字段必须有值
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	bean	DynaBean对象
     * @return 	操作影响的行数
     *
     * @since 	V0.1.0
     */
    public int delete(Connection connection,String table,DynaBean bean){
        return delete(connection, table, convert(bean));
    }

    /**
     * 删除操作，根据主键字段进行删除，所以主键字段必须有值
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	map	Map对象
     * @return 	操作影响的行数
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public int delete(Connection connection,String table,Map map){
        DataTable dataTable = DataTable.findDataTable(this.dsJndi,connection, table);
        if((dataTable == null) || (!dataTable.canDelete())){
            throw new PlatformException("表[" + table + "]元数信息不全,没有为主键赋值或者没有字段无法执行删除!");
        }
        if(!allKeyHasValue(dataTable,map)){
            throw new PlatformException("并未对表[" + table + "]中所有的主键设置值，无法进行删除操作!");
        }
        JdbcDAO dao = new JdbcDAO(dsJndi);
        DaoEvent event = new DaoEvent(map,dataTable);
        try{
            int i = 0;
            boolean valid = DaoEventManager.getInstance().fireBeforeEvent(connection, listener, BEFORE_DELETE, event);
            if(valid){
                i = dao.executeSQL(connection, buildDeleteHelper(connection, dataTable, map));
                this.lastOperate = DELETE;
            }
            DaoEventManager.getInstance().fireAfterEvent(connection, listener, AFTER_DELETE, event);
            return i;
        }catch (Exception e) {
            throw new PlatformException("表[" + table + "]执行删除操作发生异常!" + e.getMessage(), e);
        }
    }

    /**
     * 为插入操作生成NamedQueryHelper对象
     *
     * @param 	dataTable 数据库表信息
     * @param 	map Map对象
     * @return 	NamedQueryHelper对象
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    private NamedQueryHelper buildInsertHelper(Connection conn, DataTable dataTable,Map map){
        boolean closeConn = false;
        if(conn == null) {
            conn = this.getConnection();
            closeConn = true;
        }
        boolean isMySql = DatabaseType.MYSQL.equals(new JdbcDAO().getDatabaseType(conn));
        if(closeConn) {
            CloseUtils.close(conn);
        }

        StringBuilder buffer = new StringBuilder(1024);
        StringBuilder values = new StringBuilder(256);
        buffer.append(" INSERT INTO ").append(dataTable.getTable()).append(" (");
        values.append("(");
        boolean first = true;
        Set<String> temp = new HashSet<String>();
        StringBuilder exclusion =  new StringBuilder();
        for(Object key : map.keySet()){
            String name = key.toString().toUpperCase();
            if(dataTable.getColMaps().containsKey(name)){
                if(!first){
                    buffer.append(",");
                    values.append(",");
                }
                first = false;
                // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
                if(isMySql) {
                    buffer.append("`");
                    buffer.append(name);
                    buffer.append("`");
                } else {
                    buffer.append(name);
                }

                values.append("#").append(name).append("#");
                //注意，这里还需要从map中获取值，所以不能转为大写
                temp.add(key.toString());
            }else{
                if(!StringUtils.isEmpty(exclusion.toString())){
                    exclusion.append(",");
                }
                exclusion.append(name);
            }
        }
        if(dataTable.getColMaps().containsKey(CJR)
                && (!temp.contains(CJR))
                && (dataTable.findColumn(CJR).getType() == DataType.String)){
            if(temp.size() > 0){
                buffer.append(",");
                values.append(",");
            }
            // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
            buffer.append(isMySql ? "`"+CJR+"`" : CJR);
            values.append("#").append(CJR).append("#");
            temp.add(CJR);
        }
        if(dataTable.getColMaps().containsKey(CJSJ)
                && (!temp.contains(CJSJ))
                && (dataTable.findColumn(CJSJ).getType() == DataType.Timestamp)){
            if(temp.size() > 0){
                buffer.append(",");
                values.append(",");
            }
            // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
            buffer.append(isMySql ? "`"+CJSJ+"`" : CJSJ);
            values.append("#").append(CJSJ).append("#");
            temp.add(CJSJ);
        }

        if(dataTable.getColMaps().containsKey(XGR)
                && (!temp.contains(XGR))
                && (dataTable.findColumn(XGR).getType() == DataType.String)){
            if(temp.size() > 0){
                buffer.append(",");
                values.append(",");
            }
            // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
            buffer.append(isMySql ? "`"+XGR+"`" : XGR);
            values.append("#").append(XGR).append("#");
            temp.add(XGR);
        }

        if(dataTable.getColMaps().containsKey(XGSJ)
                && (!temp.contains(XGSJ))
                && (dataTable.findColumn(XGSJ).getType() == DataType.Timestamp)){
            if(temp.size() > 0){
                buffer.append(",");
                values.append(",");
            }
            // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
            buffer.append(isMySql ? "`"+XGSJ+"`" : XGSJ);
            values.append("#").append(XGSJ).append("#");
            temp.add(XGSJ);
        }

        if(dataTable.getColMaps().containsKey(ORGID)
                && (!temp.contains(ORGID))
                && (dataTable.findColumn(ORGID).getType() == DataType.String)){
            if(temp.size() > 0){
                buffer.append(",");
                values.append(",");
            }
            // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
            buffer.append(isMySql ? "`"+ORGID+"`" : ORGID);
            values.append("#").append(ORGID).append("#");
            temp.add(ORGID);
        }


        values.append(")");
        buffer.append(")").append("VALUES").append(values.toString());
        Date current = new Date();
        NamedQueryHelper helper = new NamedQueryHelper(buffer.toString());
        for(String name : temp){
            if(map.keySet().contains(name)){
                DataTableColumn dtc = dataTable.findColumn(name.toUpperCase());
                Object value = parseParameterValue(dtc, map.get(name));
                helper.bindParameter(name.toUpperCase(), DataType.parseSqlType(dtc.getType()),value);
            }else{
                if(CJR.equals(name)){
                    helper.bindParameter(CJR, Types.VARCHAR, ActionHelper.getShareName());
                }else if(XGR.equals(name)){
                    helper.bindParameter(XGR, Types.VARCHAR, ActionHelper.getShareName());
                }else if(CJSJ.equals(name)){
                    helper.bindParameter(CJSJ, Types.DATE,current);
                }else if(XGSJ.equals(name)){
                    helper.bindParameter(XGSJ, Types.DATE,current);
                }else if(ORGID.equals(name)){
                    helper.bindParameter(ORGID, Types.VARCHAR,ActionHelper.getShareOrgId());
                }
            }
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("INSERT TABLE[" + dataTable.getTable() + "] " + helper.getUsql());
            StringBuilder b = new StringBuilder(1024);
            for(DataTableColumn dtc : dataTable.getKeys()){
                b.append(" ").append(dtc.getName()).append("=").append(map.get(dtc.getName()));
            }
            LOG.debug("KEYS[" + b.toString() + "]");
        }
        if(mode == PlatformMode.development){
            StringBuilder b = new StringBuilder(1024);
            b.append("生成的insert语句:" + helper.getUsql() +"\r\n主键值:");
            for(DataTableColumn dtc : dataTable.getKeys()){
                b.append(" ").append(dtc.getName()).append("=").append(map.get(dtc.getName()));
            }
            b.append("\r\n不匹配的字段: " + exclusion.toString());
            System.out.println(b.toString());
        }
        return helper;
    }

    private Object parseParameterValue(DataTableColumn dtc , Object o){
        if(o == null){
            return null;
        }
        String strValue = o.toString();
        if(dtc.getType() == DataType.BigDecimal){
            if((o instanceof Number) || (o instanceof BigDecimal)){
                return o;
            }else{
                if(StringUtils.isEmpty(strValue)){
                    return null;
                }else{
                    return new BigDecimal(strValue);
                }
            }
        }else if(dtc.getType() == DataType.Timestamp){
            if((o instanceof Date) || (o instanceof java.sql.Date)){
                return o;
            }else{
                if(StringUtils.isEmpty(strValue)){
                    return null;
                }else{
                    return DateUtils.parseDate(strValue);
                }
            }
        }else if(dtc.getType() == DataType.Blob || dtc.getType() == DataType.ByteArray){
            return o;
        }else if(dtc.getType() == DataType.Clob){
            return strValue;
        }else{
            return strValue;
        }
    }

    /**
     * 为更新操作生成NamedQueryHelper对象
     * @param dataTable
     * @param map
     * @return
     */
    @SuppressWarnings("unchecked")
    private NamedQueryHelper buildUpdateHelper(Connection conn, DataTable dataTable,Map map) {
        boolean closeConn = false;
        if(conn == null) {
            conn = this.getConnection();
            closeConn = true;
        }
        boolean isMySql = DatabaseType.MYSQL.equals(new JdbcDAO().getDatabaseType(conn));
        if(closeConn) {
            CloseUtils.close(conn);
        }

        StringBuilder buffer = new StringBuilder(1024);
        buffer.append(" UPDATE ").append(dataTable.getTable()).append(" SET ");
        boolean first = true;
        Set<String> temp = new HashSet<String>();
        StringBuilder exclusion =  new StringBuilder();
        for(Object key : map.keySet()){
            String name = key.toString().toUpperCase();
            //表中确实存在该字段，并且不是主键才进行更新
            if(dataTable.getColMaps().containsKey(name)){
                if(!dataTable.getKeyMaps().containsKey(name)){
                    if(!first){
                        buffer.append(",");
                    }
                    first = false;
                    // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
                    buffer.append(isMySql ? "`"+name+"`" : name).append("= #").append(name).append("#");
                    //注意，这里还需要从map中获取值，所以不能转为大写
                    temp.add(key.toString());
                }
            }else{
                if(!StringUtils.isEmpty(exclusion.toString())){
                    exclusion.append(",");
                }
                exclusion.append(name);
            }
        }
        //处理修改人和修改时间，如果数据库中有字段，但是页面没有指定修改，就自动生成
        if(dataTable.getColMaps().containsKey(XGR)
                && (!temp.contains(XGR))
                && (dataTable.findColumn(XGR).getType() == DataType.String)){
            if(temp.size() > 0){
                buffer.append(",");
            }
            // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
            buffer.append(isMySql ? "`"+XGR+"`" : XGR).append(" = #").append(XGR).append("#");
            temp.add(XGR);
        }

        if(dataTable.getColMaps().containsKey(XGSJ)
                && (!temp.contains(XGSJ))
                && (dataTable.findColumn(XGSJ).getType() == DataType.Timestamp)){
            if(temp.size() > 0){
                buffer.append(",");
            }
            // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
            buffer.append(isMySql ? "`"+XGSJ+"`" : XGSJ).append(" = #").append(XGSJ).append("#");
            temp.add(XGSJ);
        }
        buffer.append(" WHERE ");

        //处理主键条件，更新时总是使用主键作为条件进行更新
        first = true;
        for(DataTableColumn dtc : dataTable.getKeys()){
            if(!first){
                buffer.append(" AND ");
            }
            // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
            buffer.append(isMySql ? "`"+dtc.getName()+"`" : dtc.getName()).append(" = #").append(dtc.getName()).append("#");
            temp.add(dtc.getName());
            first = false;
        }
        //绑定数据
        Date current = new Date();
        NamedQueryHelper helper = new NamedQueryHelper(buffer.toString());
        for(String name : temp){
            if(map.keySet().contains(name)){
                DataTableColumn dtc = dataTable.findColumn(name.toUpperCase());
                DataType type = dtc.getType();
                Object value = parseParameterValue(dtc, map.get(name));
                helper.bindParameter(name.toUpperCase(), DataType.parseSqlType(type), value);
            }else if(dataTable.getKeyMaps().containsKey(name)){
                Set<String> set = map.keySet();
                for(String str : set){
                    if(name.equalsIgnoreCase(str)){
                        DataTableColumn dtc = dataTable.findColumn(name.toUpperCase());
                        DataType type = dtc.getType();
                        Object value = parseParameterValue(dtc, map.get(str));
                        helper.bindParameter(name.toUpperCase(), DataType.parseSqlType(type), value);
                        break;
                    }
                }
            }else{
                if(XGR.equals(name)){
                    helper.bindParameter(XGR, Types.VARCHAR, ActionHelper.getShareName());
                }else if(XGSJ.equals(name)){
                    helper.bindParameter(XGSJ, Types.DATE,current);
                }else{
                    throw new PlatformException("出了逻辑问题，表单中不存在，而且不是XGR和XGSJ，但是又被生成了UPDATE!");
                }
            }
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("UPDATE TABLE[" + dataTable.getTable() + "] " + helper.getUsql());
            StringBuilder b = new StringBuilder(1024);
            for(DataTableColumn dtc : dataTable.getKeys()){
                b.append(" ").append(dtc.getName()).append("=").append(map.get(dtc.getName()));
            }
            LOG.debug("KEYS[" + b.toString() + "]");
        }
        if(mode == PlatformMode.development){
            StringBuilder b = new StringBuilder(1024);
            b.append("生成的update语句:" + helper.getUsql() +"\r\n主键值:");
            for(DataTableColumn dtc : dataTable.getKeys()){
                b.append(" ").append(dtc.getName()).append("=").append(map.get(dtc.getName()));
            }
            b.append("\r\n不匹配的字段: " + exclusion.toString());
            System.out.println(b.toString());
        }
        return helper;
    }

    /**
     * 为删除操作生成NamedQueryHelper对象
     * @param dataTable
     * @param map
     * @return
     */
    @SuppressWarnings("unchecked")
    private NamedQueryHelper buildDeleteHelper(Connection conn, DataTable dataTable,Map map){
        boolean closeConn = false;
        if(conn == null) {
            conn = this.getConnection();
            closeConn = true;
        }
        boolean isMySql = DatabaseType.MYSQL.equals(new JdbcDAO().getDatabaseType(conn));
        if(closeConn) {
            CloseUtils.close(conn);
        }

        StringBuilder buffer = new StringBuilder(1024);
        buffer.append(" DELETE FROM  ").append(dataTable.getTable()).append(" WHERE ");
        Set<String> temp = new HashSet<String>();

        //处理主键条件，删除时总是使用主键作为条件进行更新
        boolean first = true;
        for(DataTableColumn dtc : dataTable.getKeys()){
            if(!first){
                buffer.append(" AND ");
            }
            // MySQL生成语句字段要加上`，防止字段名称是保留字时异常 modify by 黄冠豪 2017-05-20
            buffer.append(isMySql ? "`"+dtc.getName()+"`" : dtc.getName()).append(" = #").append(dtc.getName()).append("#");
            temp.add(dtc.getName());
            first = false;
        }
        //绑定数据
        NamedQueryHelper helper = new NamedQueryHelper(buffer.toString());
        for(String name : temp){
            if(map.keySet().contains(name)){
                DataTableColumn dtc = dataTable.findColumn(name.toUpperCase());
                Object value = parseParameterValue(dtc, map.get(name));
                helper.bindParameter(name.toUpperCase(), DataType.parseSqlType(dtc.getType()), value);
            }else if(dataTable.getKeyMaps().containsKey(name)){
                Set<String> set = map.keySet();
                for(String str : set){
                    if(name.equalsIgnoreCase(str)){
                        DataTableColumn dtc = dataTable.findColumn(name.toUpperCase());
                        DataType type = dtc.getType();
                        Object value = parseParameterValue(dtc, map.get(str));
                        helper.bindParameter(name.toUpperCase(), DataType.parseSqlType(type), value);
                        break;
                    }
                }
            }else{
                throw new PlatformException("出了逻辑问题，执行delete操作时，表单中不存在主键["+name+"]值!");
            }
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("DELETE FROM TABLE[" + dataTable.getTable() + "] " + helper.getUsql());
            StringBuilder b = new StringBuilder(1024);
            for(DataTableColumn dtc : dataTable.getKeys()){
                b.append(" ").append(dtc.getName()).append("=").append(map.get(dtc.getName()));
            }
            LOG.debug("KEYS[" + b.toString() + "]");
        }
        return helper;
    }

    /**
     * 查询数据操作，所以主键字段必须有值
     * 根据bean对象中指定的主键的值来查询数据
     *
     * @param 	connection 数据库连接
     * @param 	table 表名
     * @param 	bean DynaBean对象
     * @return 	DynaBean对象
     *
     * @since 	V0.1.0
     */
    public DynaBean select(Connection connection,String table,DynaBean bean){
        return getRowByKey(connection, table, bean);
    }

    /**
     * 查询数据操作，所以主键字段必须有值
     * 根据bean对象中指定的主键的值来查询数据
     *
     * @param 	connection 数据库连接
     * @param 	table 表名
     * @param 	map Map对象
     * @return 	DynaBean对象
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public DynaBean select(Connection connection,String table,Map map){
        return getRowByKey(connection, table, map);
    }

    /**
     * 判断数据库表信息描述中是否为所有的主键字段都设置了值
     *
     * @param 	dataTable 数据库表信息描述
     * @param 	map Map对象
     * @return 	是否为所有的主键字段都设置了值
     *
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    private boolean allKeyHasValue(DataTable dataTable,Map map) {

        boolean flag1 = true;
        for(DataTableColumn key : dataTable.getKeys()){
            flag1 = flag1 && (map.get(key.getName())!=null);
            if(!flag1){
                Set<String> set = map.keySet();
                for(String name : set){
                    if(name.equalsIgnoreCase(key.getName()) && map.get(name) != null){
                        flag1 = true;
                        break;
                    }
                }
            }
            if(!flag1){
                return flag1;
            }
        }
        return flag1;
    }

    public String getDsJndi() {
        return dsJndi;
    }

    public void setDsJndi(String jndi) {
        this.dsJndi = jndi;
    }

    /**
     * 查询出数据集合
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	bean DynaBean对象
     * @return 	数据集合
     *
     * @author 	廖声乐
     * @version 2013-04-09
     * @since 	V0.1.0
     */
    public List<DynaBean> getListValue(Connection connection, String table, DynaBean bean){
        return getListValue(connection, table, convert(bean));
    }

    /**
     * 查询出数据集合
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	map Map对象
     * @return 	数据集合
     *
     * @author 	廖声乐
     * @version 2013-04-09
     * @since 	V0.1.0
     */
    @SuppressWarnings("unchecked")
    public List<DynaBean> getListValue(Connection connection,String table,Map map){
        DataTable dataTable = DataTable.findDataTable(this.dsJndi,connection, table);
        if(dataTable == null){
            throw new PlatformException("表[" + table + "]元数信息不全，或者没有字段，无法执行插入!");
        }
        NamedQueryHelper helper = buildSelectHelper(dataTable,map);
        return new JdbcDAO(dsJndi).getListValue(connection, helper);
    }

    /**
     * 保存操作
     *
     * @param 	connection 数据库连接
     * @param 	table	        表名
     * @param 	obj	                   存储数据的对象
     * @param   convert    转换器
     *
     * @return 	保存影响的行数
     *
     * @author 	廖声乐
     * @version 2015-06-29
     * @since 	V0.1.0
     */
    public <T> int save(Connection connection, String table, T obj, DAOConvert<T> convert){
        Map<String, Object> map = convert.convert(obj);
        return this.save(connection, table, map);
    }

    /**
     * 批量插入操作
     *
     * @param 	connection 数据库连接
     * @param 	table	        表名
     * @param 	list	        存储数据的集合
     * @param   convert    转换器
     *
     * @return 	保存影响的行数
     *
     * @author 	廖声乐
     * @version 2015-06-29
     * @since 	V0.1.0
     */
    public <T> int[] insert(Connection connection, String table, List<T> list, DAOConvert<T> convert){
        if(list == null || list.size() == 0){
            return null;
        }

        boolean hasConnection = (connection != null);
        PreparedStatement ptmt = null;
        try{
            if(!hasConnection){
                connection = new JdbcDAO(dsJndi).getConnection();
                connection.setAutoCommit(false);
            }

            DataTable dataTable = DataTable.findDataTable(this.dsJndi, connection, table);
            if((dataTable == null) || (!dataTable.canInsert())){
                throw new PlatformException("表[" + table + "]元数信息不全，或者没有字段，无法执行插入!");
            }

            boolean first = true;
            NamedQueryHelper helper = null;
            Date current = new Date();
            for(T obj : list){
                Map<String, Object> map = convert.convert(obj);
                if(first){
                    helper = buildInsertHelper(connection, dataTable, map);
                    try {
                        ptmt = connection.prepareStatement(helper.getSql());
                    } catch (SQLException e) {
                        throw new PlatformException(
                                StringUtils.joint("批量插入数据时，根据sql语句转换为PreparedStatement对象出错!sql语句为->[",
                                        helper.getSql(), "]"), e);
                    }
                    first = false;
                }else{
                    Iterator<String> iter = map.keySet().iterator();
                    while(iter.hasNext()){
                        String name = iter.next();
                        DataTableColumn dtc = dataTable.findColumn(name.toUpperCase());
                        Object value = parseParameterValue(dtc, map.get(name));
                        helper.bindParameter(name.toUpperCase(), DataType.parseSqlType(dtc.getType()),value);
                    }
                    if(dataTable.getColMaps().containsKey(CJR) && !map.containsKey(CJR)){
                        helper.bindParameter(CJR, Types.VARCHAR, ActionHelper.getShareName());
                    }
                    if(dataTable.getColMaps().containsKey(XGR) && !map.containsKey(XGR)){
                        helper.bindParameter(XGR, Types.VARCHAR, ActionHelper.getShareName());
                    }
                    if(dataTable.getColMaps().containsKey(CJSJ) && !map.containsKey(CJSJ)){
                        helper.bindParameter(CJSJ, Types.DATE, current);
                    }
                    if(dataTable.getColMaps().containsKey(XGSJ) && !map.containsKey(XGSJ)){
                        helper.bindParameter(XGSJ, Types.DATE,current);
                    }
                    if(dataTable.getColMaps().containsKey(ORGID) && !map.containsKey(ORGID)){
                        helper.bindParameter(ORGID, Types.VARCHAR,ActionHelper.getShareOrgId());
                    }
                }
                try{
                    helper.bind(ptmt);
                    ptmt.addBatch();
                    helper.clearParameters();
                    ptmt.clearParameters();
                }catch(Exception e){
                    throw new PlatformException("批量插入数据时，绑定ptmt出错！" + e.getMessage(), e);
                }
            }
            int[] result = ptmt.executeBatch();
            if(!hasConnection){
                connection.commit();
            }
            return result;
        }catch (Exception e) {
            if(!hasConnection){
                JdbcDAO.rollback(connection);
            }
            throw new PlatformException("批量插入数据出错!" + e.getMessage(), e);
        }finally {
            CloseUtils.closeStatements(ptmt);
            if(!hasConnection){
                CloseUtils.close(connection);
            }
        }
    }


    /**
     * 批量更新操作
     *
     * @param 	connection 数据库连接
     * @param 	table	        表名
     * @param 	list	        存储数据的集合
     * @param   convert    转换器
     *
     * @return 	保存影响的行数
     *
     * @author 	廖声乐
     * @version 2015-06-29
     * @since 	V0.1.0
     */
    public <T> int[] update(Connection connection, String table, List<T> list, DAOConvert<T> convert){
        if(list == null || list.size() == 0){
            return null;
        }

        boolean hasConnection = (connection != null);
        PreparedStatement ptmt = null;
        try{
            if(!hasConnection){
                connection = new JdbcDAO(dsJndi).getConnection();
                connection.setAutoCommit(false);
            }

            DataTable dataTable = DataTable.findDataTable(this.dsJndi, connection, table);
            if((dataTable == null) || (!dataTable.canUpdate())){
                throw new PlatformException("表[" + table + "]元数信息不全,没有为主键赋值或者没有字段无法执行更新!");
            }

            boolean first = true;
            NamedQueryHelper helper = null;
            Date current = new Date();
            for(T obj : list){
                Map<String, Object> map = convert.convert(obj);
                if(map == null || map.size() == 0){
                    continue;
                }
                if(!allKeyHasValue(dataTable,map)){
                    throw new PlatformException("并未对[" + table + "]中的所有的主键设置值，无法进行更新操作!");
                }
                if(first){
                    helper = buildUpdateHelper(connection, dataTable, map);
                    try {
                        ptmt = connection.prepareStatement(helper.getSql());
                    } catch (SQLException e) {
                        throw new PlatformException(
                                StringUtils.joint("批量更新数据时，根据sql语句转换为PreparedStatement对象出错!sql语句为->[",
                                        helper.getSql(), "]"), e);
                    }
                    first = false;
                }else{
                    Iterator<String> iter = map.keySet().iterator();
                    while(iter.hasNext()){
                        String name = iter.next();
                        DataTableColumn dtc = dataTable.findColumn(name.toUpperCase());
                        Object value = parseParameterValue(dtc, map.get(name));
                        helper.bindParameter(name.toUpperCase(), DataType.parseSqlType(dtc.getType()),value);
                    }
                    if(dataTable.getColMaps().containsKey(XGR) && !map.containsKey(XGR)){
                        helper.bindParameter(XGR, Types.VARCHAR, ActionHelper.getShareName());
                    }
                    if(dataTable.getColMaps().containsKey(XGSJ) && !map.containsKey(XGSJ)){
                        helper.bindParameter(XGSJ, Types.DATE,current);
                    }
                }
                try{
                    helper.bind(ptmt);
                    ptmt.addBatch();
                    helper.clearParameters();
                    ptmt.clearParameters();
                }catch(Exception e){
                    throw new PlatformException("批量更新数据时，绑定ptmt出错！" + e.getMessage(), e);
                }
            }
            int[] result = ptmt.executeBatch();
            if(!hasConnection){
                connection.commit();
            }
            return result;
        }catch (Exception e) {
            if(!hasConnection){
                JdbcDAO.rollback(connection);
            }
            throw new PlatformException("批量更新数据出错!" + e.getMessage(), e);
        }finally {
            CloseUtils.closeStatements(ptmt);
            if(!hasConnection){
                CloseUtils.close(connection);
            }
        }
    }

    /**
     * 插入操作,返回新增数据记录
     * 主键字段必须已经有值
     * 如果bean中没有为CJR,CJSJ,XGR,XGSJ赋值，程序自动赋值
     *
     * @param 	connection 数据库连接
     * @param 	table	数据库表
     * @param 	bean DynaBean对象
     * @return 	新增数据记录
     *
     * @author   廖声乐
     * @version  2015年8月18日
     * @since    V0.2.6
     */
    public InsertRecordDetail insertAndRecord(Connection connection, String table, DynaBean bean){
        return this.insertAndRecord(connection, table, convert(bean));
    }

    /**
     * 插入操作,返回新增数据记录
     * 主键字段必须已经有值
     * 如果bean中没有为CJR,CJSJ,XGR,XGSJ,ORGID赋值，程序自动赋值
     *
     * @param 	connection 数据库连接
     * @param 	table	数据库表
     * @param 	map     Map对象
     * @return 	新增数据记录
     *
     * @author   廖声乐
     * @version  2015年8月18日
     * @since    V0.2.6
     */
    public InsertRecordDetail insertAndRecord(Connection connection, String table, Map map){
        boolean hasConnection = (connection != null);
        try{
            if(!hasConnection){
                connection = this.getConnection();
                connection.setAutoCommit(false);
            }

            //插入
            int i = this.insert(connection, table, map);
            if(i < 1){
                return null;
            }

            //得到主键信息
            String[] pkInfo = this.getPkInfo(connection, table, map);

            //构造InsertRecordDetail
            InsertRecordDetail detail = new InsertRecordDetail();
            detail.setTable(table);
            detail.setPk(pkInfo[0]);
            detail.setPkValue(pkInfo[1]);
            detail.setEntity(new Entity(map));

            if(!hasConnection){
                connection.commit();
            }

            //返回
            return detail;
        }catch (Exception e) {
            if(!hasConnection){
                JdbcDAO.rollback(connection);
            }
            throw new PlatformException("表[" + table + "]执行插入并记录操作发生异常!" + e.getMessage(), e);
        }finally {
            if(!hasConnection){
                CloseUtils.close(connection);
            }
        }
    }

    /**
     * 得到主键信息
     *
     * @param 	connection 数据库连接
     * @param 	table	数据库表
     * @param 	map     Map对象
     * @return  主键信息
     *
     * @author   廖声乐
     * @version  2015年8月20日
     * @since    V0.2.0
     */
    private String[] getPkInfo(Connection connection, String table, Map map){
        DataTable dataTable = DataTable.findDataTable(this.dsJndi, connection, table);
        Map<String, DataTableColumn> keysMap = dataTable.getKeyMaps();
        Iterator<String> iter = keysMap.keySet().iterator();

        StringBuffer pks = new StringBuffer();
        StringBuffer pkValues = new StringBuffer();
        while(iter.hasNext()){
            String pk = iter.next();
            pks.append(pk).append("#");

            String pkValue = (String)map.get(pk);
            pkValues.append(pkValue).append("#");
        }
        if(pks.length() > 0){
            pks = pks.deleteCharAt(pks.length() - 1);
            pkValues = pkValues.deleteCharAt(pkValues.length() - 1);
        }
        return new String[]{pks.toString(), pkValues.toString()};
    }

    /**
     * 更新操作，返回更新数据记录
     * 根据主键字段更新，所以主键字段必须有值
     * 如果bean中没有为XGR，XGSJ赋值，程序会自动赋值
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	bean	DynaBean对象
     * @return 	更新数据记录
     *
     * @author   廖声乐
     * @version  2015年8月18日
     * @since    V0.2.6
     */
    public UpdateRecordDetail updateAndRecord(Connection connection, String table, DynaBean bean){
        return this.updateAndRecord(connection, table, convert(bean));
    }

    /**
     * 更新操作，返回更新数据记录
     * 根据主键字段更新，所以主键字段必须有值
     * 如果bean中没有为XGR，XGSJ赋值，程序会自动赋值
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	map	    Map对象
     * @return 	更新数据记录
     *
     * @author   廖声乐
     * @version  2015年8月18日
     * @since    V0.2.6
     */
    public UpdateRecordDetail updateAndRecord(Connection connection, String table, Map map){
        boolean hasConnection = (connection != null);
        try{
            if(!hasConnection){
                connection = this.getConnection();
                connection.setAutoCommit(false);
            }

            //得到更新前数据
            DynaBean beforeBean = this.select(connection, table, map);

            //更新
            int i = this.update(connection, table, map);
            if(i < 1){
                return null;
            }

            //得到更新后数据
            DynaBean afterBean = this.select(connection, table, map);

            //得到主键信息
            String[] pkInfo = this.getPkInfo(connection, table, map);

            //构造UpdateRecordDetail
            UpdateRecordDetail detail = new UpdateRecordDetail();
            detail.setTable(table);
            detail.setPk(pkInfo[0]);
            detail.setPkValue(pkInfo[1]);
            detail.setBeforeEntity(new Entity(beforeBean));
            detail.setAfterEntity(new Entity(afterBean));

            if(!hasConnection){
                connection.commit();
            }

            //返回
            return detail;
        }catch (Exception e) {
            if(!hasConnection){
                JdbcDAO.rollback(connection);
            }
            throw new PlatformException("表[" + table + "]执行更新并记录操作发生异常!" + e.getMessage(), e);
        }finally {
            if(!hasConnection){
                CloseUtils.close(connection);
            }
        }
    }

    /**
     * 删除操作，返回删除数据记录
     * 根据主键字段进行删除，所以主键字段必须有值
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	bean	DynaBean对象
     * @return 	删除数据记录
     *
     * @author   廖声乐
     * @version  2015年8月18日
     * @since    V0.2.6
     */
    public DeleteRecordDetail deleteAndRecord(Connection connection, String table, DynaBean bean){
        return deleteAndRecord(connection, table, convert(bean));
    }


    /**
     * 删除操作，返回删除数据记录
     * 根据主键字段进行删除，所以主键字段必须有值
     *
     * @param 	connection 数据库连接
     * @param 	table	表名
     * @param 	map	    Map对象
     * @return 	删除数据记录
     *
     * @author   廖声乐
     * @version  2015年8月18日
     * @since    V0.2.6
     */
    public DeleteRecordDetail deleteAndRecord(Connection connection, String table, Map map){
        boolean hasConnection = (connection != null);
        try{
            if(!hasConnection){
                connection = this.getConnection();
                connection.setAutoCommit(false);
            }

            //得到删除的数据
            DynaBean bean = this.select(connection, table, map);

            //插入
            int i = this.delete(connection, table, map);
            if(i < 1){
                return null;
            }

            //得到主键信息
            String[] pkInfo = this.getPkInfo(connection, table, map);

            //构造DeleteRecordDetail
            DeleteRecordDetail detail = new DeleteRecordDetail();
            detail.setTable(table);
            detail.setPk(pkInfo[0]);
            detail.setPkValue(pkInfo[1]);
            detail.setEntity(new Entity(bean));

            if(!hasConnection){
                connection.commit();
            }

            //返回
            return detail;
        }catch (Exception e) {
            if(!hasConnection){
                JdbcDAO.rollback(connection);
            }
            throw new PlatformException("表[" + table + "]执行删除并记录操作发生异常!" + e.getMessage(), e);
        }finally {
            if(!hasConnection){
                CloseUtils.close(connection);
            }
        }
    }


    /**
     * 批量插入操作
     * List集合中的对象支持两种形式：
     * 1、实现Map接口
     * 2、实现IConvertable接口
     *
     * @param 	connection 数据库连接
     * @param 	table	        表名
     * @param 	list	        存储数据的集合，其中的对象需要实现IConvertable接口
     *
     * @return 	保存影响的行数
     *
     * @author 	廖声乐
     * @version 2015-08-24
     * @since 	V0.1.0
     */
    @SuppressWarnings("all")
    public <T> int[] insert(Connection connection, String table, List<T> list){
        if(list == null || list.size() == 0){
            return null;
        }

        boolean isMap = false;
        boolean isDynaBean = false;
        boolean isConvertable = false;
        boolean first = true;
        for(T o : list){
            if(first){
                if(o instanceof Map){
                    isMap = true;
                }else if(o instanceof DynaBean){
                    isDynaBean = true;
                }else if(o instanceof IConvertable){
                    isConvertable = true;
                }else{
                    throw new PlatformException("不支持的类型，list集合中只能放实现了Map或IConvertable接口的对象!");
                }
                first = false;
            }else{
                if((isMap && !(o instanceof Map))
                        || (isDynaBean && !(o instanceof DynaBean))
                        || (isConvertable && !(o instanceof IConvertable))){
                    throw new PlatformException("传入的类型不一致！");
                }
            }
        }

        boolean hasConnection = (connection != null);
        PreparedStatement ptmt = null;
        try{
            if(!hasConnection){
                connection = new JdbcDAO(dsJndi).getConnection();
                connection.setAutoCommit(false);
            }

            DataTable dataTable = DataTable.findDataTable(this.dsJndi, connection, table);
            if((dataTable == null) || (!dataTable.canInsert())){
                throw new PlatformException("表[" + table + "]元数信息不全，或者没有字段，无法执行插入!");
            }

            first = true;
            NamedQueryHelper helper = null;
            Date current = new Date();
            for(T obj : list){
                Map<String, Object> map = null;
                if(isMap){
                    map = (Map)obj;
                }else if(isDynaBean){
                    map = convert((DynaBean)obj);
                }else if(isConvertable){
                    map = ((IConvertable)obj).toMap();
                }
                if(map == null || map.size() == 0){
                    continue;
                }
                if(first){
                    helper = buildInsertHelper(connection, dataTable, map);
                    try {
                        ptmt = connection.prepareStatement(helper.getSql());
                    } catch (SQLException e) {
                        throw new PlatformException(
                                StringUtils.joint("批量插入数据时，根据sql语句转换为PreparedStatement对象出错!sql语句为->[",
                                        helper.getSql(), "]"), e);
                    }
                    first = false;
                }else{
                    Iterator<String> iter = map.keySet().iterator();
                    while(iter.hasNext()){
                        String name = iter.next().toUpperCase();
                        // 修改过滤数据库中不存在字段 edit by 朱永军 2017-06-30
                        if(dataTable.getColMaps().containsKey(name)){
                            DataTableColumn dtc = dataTable.findColumn(name.toUpperCase());
                            Object value = parseParameterValue(dtc, map.get(name));
                            helper.bindParameter(name.toUpperCase(), DataType.parseSqlType(dtc.getType()),value);
                        }
                    }
                    if(dataTable.getColMaps().containsKey(CJR) && !map.containsKey(CJR)){
                        helper.bindParameter(CJR, Types.VARCHAR, ActionHelper.getShareName());
                    }
                    if(dataTable.getColMaps().containsKey(XGR) && !map.containsKey(XGR)){
                        helper.bindParameter(XGR, Types.VARCHAR, ActionHelper.getShareName());
                    }
                    if(dataTable.getColMaps().containsKey(CJSJ) && !map.containsKey(CJSJ)){
                        helper.bindParameter(CJSJ, Types.DATE, current);
                    }
                    if(dataTable.getColMaps().containsKey(XGSJ) && !map.containsKey(XGSJ)){
                        helper.bindParameter(XGSJ, Types.DATE,current);
                    }
                    if(dataTable.getColMaps().containsKey(ORGID) && !map.containsKey(ORGID)){
                        helper.bindParameter(ORGID, Types.VARCHAR,ActionHelper.getShareOrgId());
                    }
                }
                try{
                    helper.bind(ptmt);
                    ptmt.addBatch();
                    helper.clearParameters();
                    ptmt.clearParameters();
                }catch(Exception e){
                    throw new PlatformException("批量插入数据时，绑定ptmt出错！" + e.getMessage(), e);
                }
            }
            int[] result = ptmt.executeBatch();
            if(!hasConnection){
                connection.commit();
            }
            return result;
        }catch (Exception e) {
            if(!hasConnection){
                JdbcDAO.rollback(connection);
            }
            throw new PlatformException("批量插入数据出错!" + e.getMessage(), e);
        }finally {
            CloseUtils.closeStatements(ptmt);
            if(!hasConnection){
                CloseUtils.close(connection);
            }
        }
    }


    /**
     * 批量更新操作，集合中的对象需要实现IConvertable接口
     *
     * @param 	connection 数据库连接
     * @param 	table	        表名
     * @param 	list	       存储数据的集合，其中的对象需要实现IConvertable接口
     *
     * @return 	保存影响的行数
     *
     * @author 	廖声乐
     * @version 2015-08-24
     * @since 	V0.1.0
     */
    public <T> int[] update(Connection connection, String table, List<T> list){
        if(list == null || list.size() == 0){
            return null;
        }

        boolean isMap = false;
        boolean isDynaBean = false;
        boolean isConvertable = false;
        boolean first = true;
        for(T o : list){
            if(first){
                if(o instanceof Map){
                    isMap = true;
                }else if(o instanceof DynaBean){
                    isDynaBean = true;
                }else if(o instanceof IConvertable){
                    isConvertable = true;
                }else{
                    throw new PlatformException("不支持的类型，list集合中只能放实现了Map或IConvertable接口的对象!");
                }
                first = false;
            }else{
                if((isMap && !(o instanceof Map))
                        || (isDynaBean && !(o instanceof DynaBean))
                        || (isConvertable && !(o instanceof IConvertable))){
                    throw new PlatformException("传入的类型不一致！");
                }
            }
        }

        boolean hasConnection = (connection != null);
        PreparedStatement ptmt = null;
        try{
            if(!hasConnection){
                connection = new JdbcDAO(dsJndi).getConnection();
                connection.setAutoCommit(false);
            }

            DataTable dataTable = DataTable.findDataTable(this.dsJndi, connection, table);
            if((dataTable == null) || (!dataTable.canUpdate())){
                throw new PlatformException("表[" + table + "]元数信息不全,没有为主键赋值或者没有字段无法执行更新!");
            }

            first = true;
            NamedQueryHelper helper = null;
            Date current = new Date();
            for(T obj : list){
                Map<String, Object> map = null;
                if(isMap){
                    map = (Map)obj;
                }else if(isDynaBean){
                    map = convert((DynaBean)obj);
                }else if(isConvertable){
                    map = ((IConvertable)obj).toMap();
                }
                if(map == null || map.size() == 0){
                    continue;
                }
                if(!allKeyHasValue(dataTable,map)){
                    throw new PlatformException("并未对[" + table + "]中的所有的主键设置值，无法进行更新操作!");
                }
                if(first){
                    helper = buildUpdateHelper(connection, dataTable, map);
                    try {
                        ptmt = connection.prepareStatement(helper.getSql());
                    } catch (SQLException e) {
                        throw new PlatformException(
                                StringUtils.joint("批量更新数据时，根据sql语句转换为PreparedStatement对象出错!sql语句为->[",
                                        helper.getSql(), "]"), e);
                    }
                    first = false;
                }else{
                    Iterator<String> iter = map.keySet().iterator();
                    while(iter.hasNext()){
                        String name = iter.next();
                        DataTableColumn dtc = dataTable.findColumn(name.toUpperCase());
                        Object value = parseParameterValue(dtc, map.get(name));
                        helper.bindParameter(name.toUpperCase(), DataType.parseSqlType(dtc.getType()),value);
                    }
                    if(dataTable.getColMaps().containsKey(XGR) && !map.containsKey(XGR)){
                        helper.bindParameter(XGR, Types.VARCHAR, ActionHelper.getShareName());
                    }
                    if(dataTable.getColMaps().containsKey(XGSJ) && !map.containsKey(XGSJ)){
                        helper.bindParameter(XGSJ, Types.DATE,current);
                    }
                }
                try{
                    helper.bind(ptmt);
                    ptmt.addBatch();
                    helper.clearParameters();
                    ptmt.clearParameters();
                }catch(Exception e){
                    throw new PlatformException("批量更新数据时，绑定ptmt出错！" + e.getMessage(), e);
                }
            }
            int[] result = ptmt.executeBatch();
            if(!hasConnection){
                connection.commit();
            }
            return result;
        }catch (Exception e) {
            if(!hasConnection){
                JdbcDAO.rollback(connection);
            }
            throw new PlatformException("批量更新数据出错!" + e.getMessage(), e);
        }finally {
            CloseUtils.closeStatements(ptmt);
            if(!hasConnection){
                CloseUtils.close(connection);
            }
        }
    }

    /**
     * 得到数据库表的记录数
     *
     * @param connection 数据库连接
     * @param table      表名
     * @return   记录数
     *
     * @author   廖声乐
     * @version  2015年9月28日
     * @since    V0.2.0
     */
    public static int getCount(Connection connection, String table){
        JdbcDAO dao = new JdbcDAO();
        QueryHelper query = new QueryHelper("SELECT COUNT(*) CNT FROM " + table);
        DynaBean bean = dao.getSingleRow(connection, query);
        return BeanUtils.getIntValue(bean, "CNT");
    }
}
