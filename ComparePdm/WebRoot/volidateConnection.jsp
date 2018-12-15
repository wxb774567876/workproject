<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="com.szboanda.platform.tools.sjkbj.utils.JdbcDAO"%>
<%@page import="java.sql.Connection"%>
<%
String message = "false";
String flag = request.getParameter("flag");
String name = request.getParameter("name");
String type = request.getParameter("type");
String ip = request.getParameter("ip");
String database = request.getParameter("database");
String port = request.getParameter("port");
String userName = request.getParameter("userName");
String password = request.getParameter("password");
Connection conn = null;
if("true".equals(flag)){
	try{
		JdbcDAO dao = new JdbcDAO();
		conn = dao.getConnection(name);
		message="true";
	}catch(Exception e){
		e.printStackTrace();
	}finally{
		try{conn.close();}catch(Exception e){}
	}
	
}else{
	try{
		JdbcDAO dao = new JdbcDAO();
		String url = "";
		String driver = "";
		if("ORCLE".equals(type)){
			driver = "oracle.jdbc.driver.OracleDriver";
			url = "jdbc:oracle:thin:@"+ip+":"+port+":" + database;
		}
		conn = dao.getConnection(url,driver,userName,password);
		message="true";
	}catch(Exception e){
		e.printStackTrace();
	}finally{
		try{conn.close();}catch(Exception e){}
	}
}
out.print(message);
%>