<%@ page language="java" pageEncoding="UTF-8"%>
<%@page import="java.sql.Connection"%>
<%@page import="com.szboanda.platform.tools.utils.DaoHelper"%>
<%@page import="com.szboanda.platform.tools.ColumnService"%>
<%@page import="java.util.*"%>
<%@page import="com.szboanda.platform.tools.Column"%>
<%@page import="com.szboanda.platform.tools.Constant"%>
<%
String sflag = request.getParameter("self_flag");
String tabName = request.getParameter("q_TABALE");
String compareType = request.getParameter("q_CTYPE");
String bjfs = request.getParameter("bjfs");
boolean flagConn = true;
if(tabName == null){
	tabName = "";
}
if(compareType == null){
	compareType = "";
}
if(bjfs == null){
	bjfs = "0";
}
Connection s_conn = null;
Connection t_conn = null;
String ysjkConn = "";
String mbsjkConn = "";
Map<String,String> compareResult = null;
Map<String,Map<String,Column>> s_tables = null;
Map<String,Map<String,Column>> t_tables = null;
ColumnService serv = new ColumnService();
try{
	ysjkConn = DaoHelper.getString("source.properties", "user");
	mbsjkConn = DaoHelper.getString("target.properties", "user");
}catch (Exception e) {
	flagConn = false;
	e.printStackTrace();
}finally{
	
}
if(sflag != null && sflag != ""){
	try {
		// 获取连接
		s_conn = DaoHelper.getConnection("source.properties");
		t_conn = DaoHelper.getConnection("target.properties");
		s_tables = serv.getColums(s_conn,tabName);
		t_tables = serv.getColums(t_conn,tabName);
		if("0".equals(bjfs)){
			compareResult = serv.compareTables(s_tables,t_tables,compareType);
		}else{
			compareResult = serv.compareTables(t_tables,s_tables,compareType);
		}
	}catch (Exception e) {
		flagConn = false;
		e.printStackTrace();
	}finally{
		try{s_conn.close();}catch (Exception e){};
		try{t_conn.close();}catch (Exception e){};
	}
}
%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<link rel="STYLESHEET" type="text/css" href="default.css">
		<script type="text/javascript">
		var divid = "infoId";
		function showInfo(id){
			if(document.getElementById(divid)){
				document.body.removeChild(document.getElementById(divid));
			}
			if(document.getElementById(id)){
				var newDiv=document.createElement("div");//创建div
				newDiv.style.position="absolute";//relative
				newDiv.style.backgroundColor="#000000";
				newDiv.style.height="200px";
				newDiv.style.width="450px";
				newDiv.style.border="#ffff00 2px solid";
				newDiv.style.top=getTop(document.getElementById(id));
				newDiv.style.left=getLeft(document.getElementById(id)) + 20;
				newDiv.style.fontSize="12px"
				newDiv.style.lineHeight="25px"
				newDiv.style.padding="5px"
				newDiv.style.zIndex="100"
				newDiv.id=divid;
				newDiv.style.visibility ="visible";//hidden
				newDiv.style.opacity="0.5";
				newDiv.style.filter="alpha(opacity=50)"
				newDiv.innerHTML=document.getElementById(id).innerHTML;
				document.body.appendChild(newDiv);//把创建好的div插入到body节点后，否则newDiv会处于游离状态，无法显示；
			}
		}
		
		function getTop(e){
			var offset=e.offsetTop;
			if(e.offsetParent!=null) offset+=getTop(e.offsetParent);
			return offset;
		}
		
		//获取元素的横坐标
		function getLeft(e){
			var offset=e.offsetLeft;
			if(e.offsetParent!=null) offset+=getLeft(e.offsetParent);
			return offset;
		} 
		
		function closeDiv(){
			if(document.getElementById(divid)){
				document.body.removeChild(document.getElementById(divid));
			}
		}
		</script>
	</head>
	<body>
		<p>源数据库连接：<%=ysjkConn%>目标数据库连接：<%=mbsjkConn%></p>
		<p>
			<font color="blue">
				1，请配置好数据库连接,路径（ComparePdm\WebRoot\WEB-INF\classes）
				源数据库连接配置：source.properties
				目标数据库连接配置：target.properties<br/>
				2，比较方式（选择数据库比较方向）
					源表和目标表比较：以源数据库表作为基础表，和目标数据库中表进行比较；
					目标表和源表比较：以源数据库表作为基础表，和源数据库中表进行比较；<br/>
				3，目标表和源表比对(控制比较类型)<br/>
					否：只比较源数据库表中存在的数据表结构和目标表不一致的表结构。<br/>
					是：不但比较较源数据库表中存在的数据表结构和目标表不一致的表结构，
					还比较源数据库表中存在的数据库表字段与目标数据库表字段不一致的表结构<br/>
			</font>	
			<%if(!flagConn){ %>
				<font color="red">数据库链接异常，请检查数据配置文件！！！</font>
			<%}%>
		</p>
		<hr/>
		<form action="" method="post">
			<input name="self_flag" value="true" type="hidden"/>
			表名称：<input name="q_TABALE" value="<%=tabName%>"/>
			比较方式：<select name="bjfs">
						<option value="0" <%if("0".equals(bjfs)){out.print("selected=\"selected\"");} %>>源表和目标表比对</option>
						<option value="1" <%if("1".equals(bjfs)){out.print("selected=\"selected\"");} %>>目标表和源表比对</option>
			         </select>
			目标表和源表比对：
			<%
				if("".equals(compareType) || Constant.COMPARE_TYPE_NO.equals(compareType)){
			%>
			<input name="q_CTYPE" value="0" type="radio" checked="checked">否
			<input name="q_CTYPE" value="1" type="radio">是
			<%}else{ %>
			<input name="q_CTYPE" value="0" type="radio">否
			<input name="q_CTYPE" value="1" type="radio" checked="checked">是
			<%}%>
			<input value="比较" type="submit"/>
			<input value="关闭" type="button" onclick="closeDiv()">
		</form>
		<hr/>
		<%
		if(compareResult != null && s_tables != null && t_tables != null){
		%>
		<table id="mytable" cellspacing="0">
			<caption>
			</caption>
			<tr>
				<th scope="col">
					序号
				</th>
				<th scope="col" >
					数据库表名
				</th>
				<!--  
				<th scope="col">
					表注释
				</th>
				-->
				<th scope="col">
					比较结果
				</th>
				<th scope="col">
					操作
				</th>
			</tr>
			<%
			int i = 0;
			for(String item : compareResult.keySet()){
				String result = compareResult.get(item);
				i++;
			%>
			<tr onclick="showInfo('<%=item%>');" id="tr_<%=item%>" >
				<td class="row">
					<%=i%>
				</td>
				<td class="row"><%=item%></td>
				<!-- 
				<td class="row">
					&nbsp;
				</td>
				 -->
				<td class="row">
					<%
					boolean flag = false;
					if(Constant.CHECK_RESULT_NOTABLE.endsWith(result)){
						out.print("表或视图不存在");					
					}else{
						flag = true;
						out.print("表或视图字段不一致");
					}
					%>
				</td>
				<td class="row">
					<%
					Map<String,Column> s_table = s_tables.get(item);
					Map<String,Column> t_table = t_tables.get(item);
					if(flag){
						Map<String,String> comResult  = serv.compareTable(s_table,t_table);
						Map<String,String> comResult2 = serv.compareTable(t_table,s_table);
						out.print("<span style=\"display:none;\" id='" +item+ "'>");
						out.print("<table>");
						out.print("<thead>");
						out.print("<tr>");
						out.print("<th width='120px' colspan='2' style='word-break:break-all; word-wrap:break-all;'>" + item + "</th>");
						out.print("	<th colspan='2' width='120px'>字段类型</th>");
						out.print("	<th colspan='2' width='100px'>字段长度</th>");
						out.print("</tr>");
						out.print("<tr>");
						out.print("<th width='120px'>字段名称</th>");
						out.print("<th width='120px'>注释</th>");
						out.print("	<th width='60'>源表</th>");
						out.print("	<th width='60'>目标表</th>");
						out.print("<th width='50px'>源表</th>");
						out.print("	<th width='50px'>目标</th>");
						out.print("</tr>");
						out.print("</thead>");
						for(String key:comResult.keySet()){
							Column s_column = s_table.get(key);
							Column t_column = t_table.get(key);
							String val = comResult.get(key);
							out.print("<tr>");
							String zs = "";
							if(s_column != null){
								zs = s_column.getComments();
							}
							if(t_column != null && zs == ""){
								zs = t_column.getComments();
							}
							out.print("<td>" + key + "</td>");
							out.print("<td>" + zs + "&nbsp;</td>");
							if(!Constant.CHECK_RESULT_NOCOLUMN.equals(val)){
								out.print("<td>" + s_column.getData_type() + "</td>");
								out.print("<td>" + t_column.getData_type() + "</td>");
								out.print("<td>" + s_column.getData_length() + "</td>");
								out.print("<td>" + t_column.getData_length() + "</td>");
							}else{
								out.print("<td>" + s_column.getData_type() + "</td>");
								out.print("<td>~~</td>");
								out.print("<td>" + s_column.getData_length() + "</td>");
								out.print("<td>~~</td>");
							}
							out.print("</tr>");
						}
						if(Constant.COMPARE_TYPE_YES.equals(compareType)){
							for(String key:comResult2.keySet()){
								Column s_column = s_table.get(key);
								Column t_column = t_table.get(key);
								String val = comResult2.get(key);
								if(s_column == null && t_column != null){
									out.print("<tr>");
									String zs = "";
									if(s_column != null){
										zs = s_column.getComments();
									}
									if(t_column != null && zs == ""){
										zs = t_column.getComments();
									}
									out.print("<td>" + key + "</td>");
									out.print("<td>" + zs + "</td>");
									if(!Constant.CHECK_RESULT_NOCOLUMN.equals(val)){
										out.print("<td>" + s_column.getData_type() + "</td>");
										out.print("<td>" + t_column.getData_type() + "</td>");
										out.print("<td>" + s_column.getData_length() + "</td>");
										out.print("<td>" + t_column.getData_length() + "</td>");
									}else{
										out.print("<td>~~</td>");
										out.print("<td>" + t_column.getData_type() + "</td>");
										out.print("<td>~~</td>");
										out.print("<td>" + t_column.getData_length() + "</td>");
									}
									out.print("</tr>");
								}
							}
						}
						out.print("</table>");
						out.print("</span>");
					}
					%>
				</td>
			</tr>
			<%}%>
		</table>
		<%}%>
	</body>
</html>
