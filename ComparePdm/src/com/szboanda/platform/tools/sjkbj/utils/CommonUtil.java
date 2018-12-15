package com.szboanda.platform.tools.sjkbj.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.szboanda.platform.tools.sjkbj.bean.DBConfig;

public class CommonUtil {
    
    private static Logger log = Logger.getLogger(CommonUtil.class);
    
    private CommonUtil() {
    }
    
    /**
     * 检查配置文件是否存在, 不存在则创建并返回配置文件, 否则直接返回配置文件
     * Dec 19, 2008
     * @param request
     * @return
     */
    @SuppressWarnings("deprecation")
    public static File initConfigFile(HttpServletRequest request) {
        String realPath = request.getRealPath("");
        try {
            File configDir = new File(realPath + "/WEB-INF/config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            File configFile = new File(configDir.getAbsolutePath()
                    + "/DBConfig.xml");
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            return configFile;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return null;
    }
    /**
     * 把配置对象dtConfig写到配置文件config中
     * Dec 19, 2008
     * @param dtConfig
     * @param configFile
     * @return
     */
    public static boolean writeConfig(DBConfig dtConfig,
            File configFile) {
        boolean done = false;
        XMLEncoder encoder = null;
        try {
            encoder = new XMLEncoder(new FileOutputStream(configFile));
            encoder.writeObject(dtConfig);
            encoder.flush();
            done = true;
        } catch (Exception e) {
            log.info(e.getMessage());
        } finally {
            if (null != encoder) {
                encoder.close();
            }
        }
        return done;
    }

    /**
     * 从配置文件configFile中读取配置对象
     * Dec 19, 2008
     * @param configFile
     * @return
     */
    public static DBConfig readConfig(File configFile) {
        DBConfig dtConfig = null;
        XMLDecoder decoder = null;
        try {
            decoder = new XMLDecoder(new FileInputStream(configFile));
            dtConfig = (DBConfig) decoder.readObject();
        } catch (Exception e) {
            log.info(e.getMessage());
        } finally {
            if (null != decoder) {
                decoder.close();
            }
        }
        return dtConfig;
    }
    
    /**
     * 从request中获取数据并设置到dtConfig对象中
     * Dec 19, 2008
     * @param dtConfig
     * @param request
     */
    public static void setDtConfig(DBConfig dtConfig, HttpServletRequest request) {
        dtConfig.setSourceType(request.getParameter("source_type"));
        dtConfig.setSourceIp(request.getParameter("source_address"));
        dtConfig.setSourceDatabase(request.getParameter("source_dataBase"));
        dtConfig.setSourcePort(request.getParameter("source_port"));
        dtConfig.setSourceUsername(request.getParameter("source_username"));
        dtConfig.setSourcePassword(request.getParameter("source_password"));
        
        dtConfig.setDestinationType(request.getParameter("destination_type"));
        dtConfig.setDestinationIp(request.getParameter("destination_address"));
        dtConfig.setDestinationDatabase(request.getParameter("destination_dataBase"));
        dtConfig.setDestinationPort(request.getParameter("destination_port"));
        dtConfig.setDestinationUsername(request.getParameter("destination_username"));
        dtConfig.setDestinationPassword(request.getParameter("destination_password"));
    }
}
