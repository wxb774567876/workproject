/******************************************************************************
 * Copyright (C) ShenZhen Powerdata Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳市博安达信息技术股份有限公司开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 *****************************************************************************/

package com.szboanda.readexcel.service;

import com.szboanda.platform.util.BeanUtils;
import com.szboanda.platform.util.CloseUtils;
import com.szboanda.platform.util.dao.FormBeanDAO;
import com.szboanda.platform.util.dao.JdbcDAO;
import com.szboanda.platform.util.exception.PlatformException;
import com.szboanda.platform.util.helper.QueryHelper;
import com.szboanda.utils.ExcelUtil;
import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @company:    深圳市博安达信息技术股份有限公司
 * @copyright:  PowerData Software Co.,Ltd. Rights Reserved.
 * @author:     王贤炳
 * @since:      2018/12/14
 * @version:    V1.0
 */
public class CityDataService {
    //日志对象
    protected Logger logger = Logger.getLogger(this.getClass());

    //表字段集合
    private static List<String> columnNames = new ArrayList<>();

    //重复数据记录集合
    private static Map<String, Object> repeatMap = new HashMap<>();

    //城市基础数据
    private static Map<String, DynaBean> citysMap = new HashMap<>();

    public static void main(String[] args) {
        //初始化Excel中列标题对应的字段集合
        initColumnNames();
        initCitysMap();

        CityDataService service = new CityDataService();
        service.recursiveFolder("F:\\work\\05 临时工作安排\\07 补录全国城市、站点历史数据\\全国空气质量（城市日均值）");
    }

    /**
     * 递归文件夹中的excel文件
     *
     * @param forderPath 读取的文件夹路径
     * @return   void
     *
     * @author   王贤炳
     * @since    2018/12/14
     * @version: V1.0
     */
    public void recursiveFolder(String forderPath) {
        File parentFile = new File(forderPath);
        if (!parentFile.isDirectory()) {
            logger.info(forderPath + "不是目录");
            return;
        }
        File[] files = parentFile.listFiles();
        if (files == null || files.length == 0) {
            logger.info(forderPath + "文件夹是空的");
            return;
        } else {
            for (File childFile : files) {
                if (childFile.isDirectory()) {
                    recursiveFolder(childFile.getAbsolutePath());
                } else {
                    logger.info(childFile.getAbsolutePath());
                    //解析并保存数据
                    parseAndSave(childFile);
                }
            }
        }
    }

    /**
     * 从Excel中读取文件
     *
     * @param file 被读取的Excel文件对象
     * @return   java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     *
     * @author   王贤炳
     * @since    2018/12/14
     * @version: V1.0
     */
    public List<Map<String, Object>> parseDataFromExcel(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        String fileName = file.getName();

        Workbook workBook = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            if (fileName.indexOf(".xlsx") > -1) {
                workBook = new XSSFWorkbook(fileInputStream);
            } else {
                workBook = new HSSFWorkbook(fileInputStream);
            }
            Sheet sheet = workBook.getSheet("Sheet0");
            int rows = sheet.getPhysicalNumberOfRows();
            int columns = 0;
            Map<String, Object> resultMap = null;
            String xh = null;
            for (int r = 0; r < rows; r++) { // 循环遍历表格的行
                if (r == 0) {
                    // 在第一行标题行计算出列宽度,因为数据行中可能会有空值
                    columns = sheet.getRow(r).getLastCellNum();
                    continue;
                }
                // 获取单元格中指定的行对象
                Row row = sheet.getRow(r);
                if (row != null) {
                    resultMap = new HashMap<>();
                    // 循环遍历每行的单元格
                    for (short c = 0; c < columns; c++) {
                        Cell cell = row.getCell((short) c);
                        resultMap.put(columnNames.get(c), ExcelUtil.getCellValue(cell));
                    }
                    String cityName = getCityName(resultMap.get("SSXZQ").toString());
                    String monitorDate = resultMap.get("JCSJ").toString().replace("-","");
                    DynaBean dynaBean = citysMap.get(cityName);
                    if (dynaBean == null) {
                        logger.error("未匹配到城市信息，请检查："+ resultMap.get("JCSJ").toString() +"【" + cityName + "】");
                        throw new PlatformException("未匹配到城市信息，请检查");
                    }

                    xh =  monitorDate + "#" + BeanUtils.getString(dynaBean,"city_code", true);
                    if (!repeatMap.containsKey(xh)) {
                        repeatMap.put(xh, xh);
                        resultMap.put("XH", xh);
                        resultMap.put("CSDM", BeanUtils.getString(dynaBean, "city_code", true));
                        resultMap.put("SFMC", BeanUtils.getString(dynaBean, "province", true));
                        resultMap.put("KQZLJB", ExcelUtil.getAQILevel(resultMap.get("KQZLQK").toString()));

                        //处理非数值情况
                        resultMap.put("CO", ExcelUtil.getStringValue(resultMap.get("CO")));
                        resultMap.put("NO2", ExcelUtil.getStringValue(resultMap.get("NO2")));
                        resultMap.put("SO2", ExcelUtil.getStringValue(resultMap.get("SO2")));
                        resultMap.put("O3", ExcelUtil.getStringValue(resultMap.get("O3")));
                        resultMap.put("PM10", ExcelUtil.getStringValue(resultMap.get("PM10")));
                        resultMap.put("PM25", ExcelUtil.getStringValue(resultMap.get("PM25")));
                        resultMap.put("AQI", ExcelUtil.getStringValue(resultMap.get("AQI")));

                        resultList.add(resultMap);
                    } else {
                        logger.error("############" + "数据重复:" + xh);
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            logger.info(fileName + "文件解析并保存成功，耗时：" + (endTime - startTime)/1000);
        } catch (Exception e) {
            logger.error("解析文件发生异常：" + e.getMessage(), e);
            throw new PlatformException("解析文件发生异常：" + e.getMessage(), e);
        }
        return resultList;
    }

    /**
     * 解析并保存数据
     *
     * @param file 文件对象
     * @return   void
     *
     * @author   王贤炳
     * @since    2018/12/14
     * @version: V1.0
     */
    public void parseAndSave(File file) {
        List<Map<String, Object>> resultList = parseDataFromExcel(file);
        if (!BeanUtils.emptyCollection(resultList)) {
            Connection conn = null;

            try {
                conn = new JdbcDAO().getConnection();
                conn.setAutoCommit(false);

                FormBeanDAO formBeanDAO = new FormBeanDAO();
                //插入数据
                formBeanDAO.insert(conn, "T_JCSJZX_AQI_RJZ", resultList);

                conn.commit();
            } catch (Exception e) {
                if (conn != null) {
                    JdbcDAO.rollback(conn);
                }
                logger.error("保存【"+ file.getName() + "】数据时发生异常：" + e.getMessage(), e);
                throw new PlatformException("保存【"+ file.getName() + "】数据时发生异常：" + e.getMessage(), e);
            } finally {
                CloseUtils.close(conn);
            }
        }
    }

    /**
     * 初始化Excel列标题对应的字段名
     *
     * @return   void
     *
     * @author   王贤炳
     * @since    2018/12/14
     * @version: V1.0
     */
    private static void initColumnNames() {
        columnNames.add("JCSJ");
        columnNames.add("CO");
        columnNames.add("NO2");
        columnNames.add("O3");
        columnNames.add("PM10");
        columnNames.add("PM25");
        columnNames.add("SSXZQ");
        columnNames.add("SYWRW");
        columnNames.add("KQZLQK");
        columnNames.add("SO2");
        columnNames.add("AQI");
    }

    /**
     * 初始化城市基础信息
     *
     * @return   void
     *
     * @author   王贤炳
     * @since    2018/12/14
     * @version: V1.0
     */
    private static void initCitysMap() {
        QueryHelper queryHelper = new QueryHelper("SELECT * FROM T_AQI_CITYS");
        Connection conn =  ExcelUtil.getAQIConnection();
        JdbcDAO jdbcDAO = new JdbcDAO();
        List<DynaBean> list = jdbcDAO.getListValue(conn, queryHelper);
        if (!BeanUtils.emptyCollection(list)) {
            String cityName = null;
            for (DynaBean dynaBean : list) {
                cityName = BeanUtils.getString(dynaBean, "city", true);
                citysMap.put(cityName, dynaBean);
            }
        }

    }

    /**
     * 对部分城市进行处理，返回新的城市名称
     *
     * @param cityName 城市名称
     * @return   java.lang.String
     *
     * @author   王贤炳
     * @since    2018/12/14
     * @version: V1.0
     */
    public String getCityName(String cityName) {
        String newCityName = cityName;
        if ("昌都地区".equals(cityName)) {
            newCityName = "昌都市";
        } else if ("山南地区".equals(cityName)) {
            newCityName = "山南市";
        } else if ("日喀则地区".equals(cityName)) {
            newCityName = "日喀则市";
        } else if ("林芝地区".equals(cityName)) {
            newCityName = "林芝市";
        } else if ("襄樊市".equals(cityName)) {
            newCityName = "襄阳市";
        } else if ("思茅市".equals(cityName)) {
            newCityName = "普洱市";
        }
        return newCityName;
    }

}
