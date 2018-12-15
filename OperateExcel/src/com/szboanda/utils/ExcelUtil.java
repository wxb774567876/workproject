/******************************************************************************
 * Copyright (C) ShenZhen Powerdata Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳市博安达信息技术股份有限公司开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 *****************************************************************************/

package com.szboanda.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel工具类
 *
 * @company:    深圳市博安达信息技术股份有限公司
 * @copyright:  PowerData Software Co.,Ltd. Rights Reserved.
 * @author:     王贤炳
 * @since:      2018/12/14
 * @version:    V1.0
 */
public class ExcelUtil {

    //格式化日期字符串
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.####");

    public static String getCellValue(Cell cell) {

        if (cell == null){
            return "";
        }
        switch (cell.getCellTypeEnum()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMAT.format(cell.getDateCellValue());
                }
                return DECIMAL_FORMAT.format(cell.getNumericCellValue());
            case STRING:
                return cell.getStringCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            case BOOLEAN:
                return cell.getBooleanCellValue() + "";
            case ERROR:
                return cell.getErrorCellValue() + "";
            default:
                break;
        }
        return "";
    }

    /**
     * 判断是否是数字
     *
     * @param object 要转换的字符串
     * @return   boolean
     *
     * @author   王贤炳
     * @since    2018/12/14
     * @version: V1.0
     */
    public static boolean isNumeric(Object object) {
        boolean flag = true;
        if (object != null) {
            Pattern pattern = Pattern.compile("^((-?[1-9]\\d*\\.?\\d*)[Ee]{0,1}([+-]?\\d+))|(-?0\\.\\d*[1-9])|(-?[0])|(-?[1-9]{1}\\d*)|(-?[0]\\.\\d*)$");
            Matcher isNum = pattern.matcher(object.toString());
            if( !isNum.matches() ){
                flag = false;
            }
        } else {
            flag = false;
        }
        return flag;
    }

    /**
     * 对非数字值进行统一处理，空值存储
     *
     * @param object 1
     * @return   java.lang.String
     *
     * @author   王贤炳
     * @since    2018/12/14
     * @version: V1.0
     */
    public static String getStringValue(Object object) {
        if (isNumeric(object)) {
            return object.toString();
        } else {
            return null;
        }
    }

    /**
     * 返回AQI数据库连接对象
     *
     * @return   java.sql.Connection
     *
     * @author   王贤炳
     * @since    2018/12/14
     * @version: V1.0
     */
    public static Connection getAQIConnection() {
        Connection connection = null;
        String dbUserNameDest = "sa";
        String dbPassWordDest = "powerdata@1";
        String dbDriverDest = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        String dbUriDest = "jdbc:sqlserver://192.168.3.55:1433;databaseName=AQI";
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
     * 根据空气质量状况返回空气质量级别值
     *
     * @param quality 空气质量状况
     * @return   java.lang.String
     *
     * @author   王贤炳
     * @since    2018/12/14
     * @version: V1.0
     */
    public static String getAQILevel(String quality) {
        String level = null;
        if ("优".equals(quality)) {
            level = "1";
        } else if ("良".equals(quality)) {
            level = "2";
        } else if ("轻度污染".equals(quality)) {
            level = "3";
        } else if ("中度污染".equals(quality)) {
            level = "4";
        } else if ("重度污染".equals(quality)) {
            level = "5";
        } else if ("严重污染".equals(quality)) {
            level = "6";
        }
        return level;
    }
}
