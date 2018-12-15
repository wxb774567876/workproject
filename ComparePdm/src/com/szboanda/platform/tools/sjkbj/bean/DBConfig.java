package com.szboanda.platform.tools.sjkbj.bean;

import java.io.Serializable;

public class DBConfig implements Serializable, Cloneable {

    private static final long serialVersionUID = -1247292144220527900L;
    
    private String sourceFlag;//标识是否使用数据源
    
    private String sourceName;//数据源名称

    private String sourceType; // 源数据库类型

    private String sourceIp; // 源数据库IP

    private String sourceDatabase; // 源数据库名称

    private String sourcePort; // 端口

    private String sourceUsername; // 用户名称

    private String sourcePassword; // 密码
    
    private String destinationFlag;//标识是否使用数据源
    
    private String destinationName;//数据源名称

    private String destinationType; // 目标数据库类型

    private String destinationIp; // 目标数据库IP

    private String destinationDatabase; // 目标数据库名称

    private String destinationPort; // 端口

    private String destinationUsername; // 用户名称

    private String destinationPassword; // 密码

    public String getDestinationDatabase() {
        return destinationDatabase;
    }

    public void setDestinationDatabase(String destinationDatabase) {
        this.destinationDatabase = destinationDatabase;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public String getDestinationPassword() {
        return destinationPassword;
    }

    public void setDestinationPassword(String destinationPassword) {
        this.destinationPassword = destinationPassword;
    }

    public String getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getDestinationUsername() {
        return destinationUsername;
    }

    public void setDestinationUsername(String destinationUsername) {
        this.destinationUsername = destinationUsername;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getSourcePassword() {
        return sourcePassword;
    }

    public void setSourcePassword(String sourcePassword) {
        this.sourcePassword = sourcePassword;
    }

    public String getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceUsername() {
        return sourceUsername;
    }

    public void setSourceUsername(String sourceUsername) {
        this.sourceUsername = sourceUsername;
    }

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }
	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceFlag() {
		return sourceFlag;
	}

	public void setSourceFlag(String sourceFlag) {
		this.sourceFlag = sourceFlag;
	}

	public String getDestinationFlag() {
		return destinationFlag;
	}

	public void setDestinationFlag(String destinationFlag) {
		this.destinationFlag = destinationFlag;
	}

	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}
    
}