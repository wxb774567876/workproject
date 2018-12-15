<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>数据库配置</title>
    <script type="text/javascript" src="jquery-1.11.0.js"></script>
    <script type="text/javascript">
    	function changeSource(){
    		var flag = $('input[name="sourceFlag"]:checked').val();
    		if("false" == flag){
    			$("#sourceName").attr("readonly","readonly");
    			$("#sourceType").removeAttr("readonly");
    			$("#sourceIp").removeAttr("readonly");
    			$("#sourceDatabase").removeAttr("readonly");
    			$("#sourcePort").removeAttr("readonly");
    			$("#sourceUsername").removeAttr("readonly");
    			$("#sourcePassword").removeAttr("readonly");
    			//$("#sourceName").val("");
    		}else{
    			$("#sourceName").removeAttr("readonly");
    			$("#sourceType").attr("disable","true");
    			$("#sourceIp").attr("readonly","readonly");
    			$("#sourceDatabase").attr("readonly","readonly");
    			$("#sourcePort").attr("readonly","readonly");
    			$("#sourceUsername").attr("readonly","readonly");
    			$("#sourcePassword").attr("readonly","readonly");
    		}
    	}
    	function changeDestination(){
    		var flag = $('input[name="destinationFlag"]:checked').val();
    		if("false" == flag){
    			$("#destinationName").attr("readonly","readonly");
    			//$("#destinationName").val("");
    		}else{
    			$("#destinationName").removeAttr("readonly");
    		}
    	}
    	
    	function volidateSourceConnection(){
    		$.ajax(
    			{
	    			url:"volidateConnection.jsp", 
	   				data:{
		    			flag:$('input[name="sourceFlag"]:checked').val(),
		    			name:$("#sourceName").val(),
		    			type:$("#sourceType").val(),
		    			ip:$("#sourceIp").val(),
		    			database:$("#sourceDatabase").val(),
		    			port:$("#sourcePort").val(),
		    			userName:$("#sourceUsername").val(),
		    			password:$("#sourcePassword").val()
	   				}, 
	   				dataType: "json",
	    			success: function (data) {
	    				if("true" == $.trim(data)){
	    					alert("连接成功！！");
	    				}else{
	    					alert("测试失败！！");
	    				}
		            },
		            error: function (msg) {
		                alert(msg);
		            },
		            type:"POST"
	            }
    		);
    	}
    	
    	function saveConnection(){
    		$.ajax(
    			{
	    			url:"ajaxConfig.jsp", 
	   				data:{
	   					method:"save",
		    			sourceFlag:$('input[name="sourceFlag"]:checked').val(),
		    			sourceName:$("#sourceName").val(),
		    			sourceType:$("#sourceType").val(),
		    			sourceIp:$("#sourceIp").val(),
		    			sourceDatabase:$("#sourceDatabase").val(),
		    			sourcePort:$("#sourcePort").val(),
		    			sourceUsername:$("#sourceUsername").val(),
		    			sourcePassword:$("#sourcePassword").val(),
		    			destinationFlag:$('input[name="destinationFlag"]:checked').val(),
		    			destinationName:$("#destinationName").val(),
		    			destinationType:$("#destinationType").val(),
		    			destinationIp:$("#destinationIp").val(),
		    			destinationDatabase:$("#destinationDatabase").val(),
		    			destinationPort:$("#destinationPort").val(),
		    			destinationUsername:$("#destinationUsername").val(),
		    			destinationPassword:$("#destinationPassword").val()
	   				}, 
	   				dataType: "json",
	    			success: function (data) {
	    				if("true" == data){
	    					alert("保存成功！！");
	    				}
		            },
		            error: function (msg) {
		                alert(msg);
		            },
		            type:"POST"
	            }
    		);
    	}
    	
    	function initConnection(){
    		$.ajax(
    			{
	    			url:"ajaxConfig.jsp", 
	   				data:{
	   					method:"init"
	   				}, 
	   				dataType: "json",
	    			success: function (data) {
	    				$('input[name="sourceFlag"][value='+data.sourceFlag+']').attr("checked",true);
		    			$("#sourceName").val(data.sourceName);
		    			$("#sourceType").val(data.sourceType);
		    			$("#sourceIp").val(data.sourceIp);
		    			$("#sourceDatabase").val(data.sourceDatabase);
		    			$("#sourcePort").val(data.sourcePort);
		    			$("#sourceUsername").val(data.sourceUsername);
		    			$("#sourcePassword").val(data.sourcePassword);
		    			$('input[name="destinationFlag"][value='+data.destinationFlag+']').attr("checked",true);
		    			$("#destinationName").val(data.destinationName);
		    			$("#destinationType").val(data.destinationType);
		    			$("#destinationIp").val(data.destinationIp);
		    			$("#destinationDatabase").val(data.destinationDatabase);
		    			$("#destinationPort").val(data.destinationPort);
		    			$("#destinationUsername").val(data.destinationUsername);
		    			$("#destinationPassword").val(data.destinationPassword);
		            },
		            error: function (msg) {
		                alert(msg);
		            },
		            type:"POST"
	            }
    		);
    	}
    </script>
  </head>
  <body>
  	 <a href="index.jsp">首页</a>
  	 <hr/>
  	 <input type="button" value="加载配置" onclick="initConnection();"/>
  	 <input type="button" value="保存配置" onclick="saveConnection();"/>
  	 <hr/>
     <table>
     	<thead>
     		<tr>
     			<th>&nbsp;</th>
     			<th>源数据库</th>
     			<th>目标数据库</th>
     		</tr>
     	</thead>
     	<tr>
     		<td >是否数据源</td>
     		<td >
     			<input name="sourceFlag" type="radio" value="true" onclick="changeSource()">是</input>
     			<input name="sourceFlag" type="radio" value="false" onclick="changeSource()">否</input>
     		</td>
     		<td >
     			<input name="destinationFlag" type="radio" value="true" onclick="changeDestination()">是</input>
     			<input name="destinationFlag" type="radio" value="false" onclick="changeDestination()">否</input>
     		</td>
     	</tr>
     	<tr>
     		<td>数据源名称</td>
     		<td><input id="sourceName" type="text" name="sourceName"/></td>
     		<td><input id="destinationName" type="text" name="destinationName"/></td>
     	</tr>
     	<tr>
     		<td>数据库类型</td>
     		<td>
     			<select id="sourceType" name="sourceType">
     				<option value="">--请选择--</option>
     				<option value="ORCLE">ORCLE</option>
     				<option value="SQL SERVER">SQL SERVER</option>
     			</select>
     		</td>
     		<td>
     			<select id="destinationType" name="destinationType">
     				<option value="">--请选择--</option>
     				<option value="ORCLE">ORCLE</option>
     				<option value="SQL SERVER">SQL SERVER</option>
     			</select>
     		</td>
     	</tr>
     	<tr>
     		<td>数据库IP</td>
     		<td><input id="sourceIp" type="text" name="sourceIp"/></td>
     		<td><input id="destinationIp" type="text" name="destinationIp"/></td>
     	</tr>
     	<tr>
     		<td>数据库名称</td>
     		<td><input id="sourceDatabase" type="text" name="sourceDatabase"/></td>
     		<td><input id="destinationDatabase" type="text" name="destinationDatabase"/></td>
     	</tr>
     	<tr>
     		<td>端口</td>
     		<td><input id="sourcePort" type="text" name="sourcePort"/></td>
     		<td><input id="destinationPort" type="text" name="destinationPort"/></td>
     	</tr>
     	<tr>
     		<td>用户名称</td>
     		<td><input id="sourceUsername" type="text" name="sourceUsername"/></td>
     		<td><input id="destinationUsername" type="text" name="destinationUsername"/></td>
     	</tr>
     	<tr>
     		<td>密码</td>
     		<td><input id="sourcePassword" type="text" name="sourcePassword"/></td>
     		<td><input id="destinationPassword" type="text" name="destinationPassword"/></td>
     	</tr>
     	<tr>
     		<td>&nbsp;</td>
     		<td><input type="button" value="测试连接" onclick="volidateSourceConnection();"/></td>
     		<td><input type="button" value="测试连接" onclick="volidateDestinationConnection();"/></td>
     	</tr>
     </table>
  </body>
</html>
