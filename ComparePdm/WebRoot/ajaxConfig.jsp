<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="com.szboanda.platform.tools.sjkbj.bean.DBConfig"%>
<%@page import="com.szboanda.platform.tools.sjkbj.utils.CommonUtil"%>
<%@page import="net.sf.json.JSONObject"%>
<%
String method = request.getParameter("method");
if("save".equals(method)){
	String sourceFlag = request.getParameter("sourceFlag");
	String sourceName = request.getParameter("sourceName");
	String sourceType = request.getParameter("sourceType");
	String sourceIp = request.getParameter("sourceIp");
	String sourceDatabase= request.getParameter("sourceDatabase");
	String sourcePort= request.getParameter("sourcePort");
	String sourceUsername= request.getParameter("sourceUsername");
	String sourcePassword= request.getParameter("sourcePassword");
	String destinationFlag= request.getParameter("destinationFlag");
	String destinationName= request.getParameter("destinationName");
	String destinationType= request.getParameter("destinationType");
	String destinationIp= request.getParameter("destinationIp");
	String destinationDatabase= request.getParameter("destinationDatabase");
	String destinationPort= request.getParameter("destinationPort");
	String destinationUsername= request.getParameter("destinationUsername");
	String destinationPassword= request.getParameter("destinationPassword");
	DBConfig dbconfig = new DBConfig();
	dbconfig.setSourceFlag(sourceFlag);
	dbconfig.setSourceName(sourceName);
	dbconfig.setSourceType(sourceType);
	dbconfig.setSourceIp(sourceIp);
	dbconfig.setSourcePort(sourcePort);
	dbconfig.setSourceDatabase(sourceDatabase);
	dbconfig.setSourceUsername(sourceUsername);
	dbconfig.setSourcePassword(sourcePassword);
	
	dbconfig.setDestinationFlag(destinationFlag);
	dbconfig.setDestinationName(destinationName);
	dbconfig.setDestinationType(destinationType);
	dbconfig.setDestinationIp(destinationIp);
	dbconfig.setDestinationPort(destinationPort);
	dbconfig.setDestinationDatabase(destinationDatabase);
	dbconfig.setDestinationUsername(destinationUsername);
	dbconfig.setDestinationPassword(destinationPassword);
	CommonUtil.writeConfig(dbconfig,CommonUtil.initConfigFile(request));
	out.print("true");
}else if("init".equals(method)){
	DBConfig dbconfig = CommonUtil.readConfig(CommonUtil.initConfigFile(request));
	if(dbconfig != null){
		System.out.println("#######" + JSONObject.fromObject(dbconfig).toString());
		out.print(JSONObject.fromObject(dbconfig).toString());
	}
}
%>
