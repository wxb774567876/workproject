package com.szboanda.sjdj.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.szboanda.platform.util.StringUtils;

public class TranUtils {

	private static File file = null;
	
	public static void setFileName(String filePath){
		file = new File(filePath);
	}
	
	/**
	 * 得到数据库字段与domain字段对应值
	 * @return
	 * @throws Exception
	 */
	private static Map<String, String[]> getFileString() throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Map<String, String[]> maps = new LinkedHashMap<String, String[]>();
		boolean isRead = false;
		String databaseStr = null;
		while(reader.read() != -1){
//			String str = new String(reader.readLine().getBytes("GBK"),"UTF-8");
			String str = reader.readLine();
			if(StringUtils.isEmpty(str)) continue;
			if(str.startsWith("/**") && str.endsWith("*/") && (str.indexOf(":") != -1 || str.indexOf("：") != -1)){
				int index = str.indexOf(":") != -1 ? str.indexOf(":") : str.indexOf("：");
				databaseStr = str.substring(index + 1, str.indexOf("*/")).trim();
				if(StringUtils.isEmpty(databaseStr)) continue;
				isRead = true;
				continue;
			}
			if(isRead){
				str = str.substring(0, str.indexOf(";"));
				str = str.replaceAll("\\s+", " ");
				maps.put(databaseStr, str.split(" "));
				isRead = false;
			}
		}
		return maps;
	}
	
	/**
	 * 将首字段转换成小写
	 * @param str
	 * @return
	 */
	private static String firstToLowerCase(String str){
		char oldChar = str.charAt(0);
		char newChar = (oldChar + "").toLowerCase().charAt(0);
		return str.replaceFirst(oldChar + "", newChar + "");
	}
	
	/**
	 * 将首字段转换成大写
	 * @param str
	 * @return
	 */
	private static String firstToUpperCase(String str){
		char oldChar = str.charAt(0);
		char newChar = (oldChar + "").toUpperCase().charAt(0);
		return str.replaceFirst(oldChar + "", newChar + "");
	}
	
	/**
	 * ConvertDynaBeanToObject
	 * 
	 * @param author
	 * @return
	 * @throws Exception
	 */
	public static String transConvertDynaBeanToObject(String author) throws Exception{
		SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy-MM-dd");
		String cls = file.getName().substring(0, file.getName().lastIndexOf("."));
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("/**\n").append("*\n");
		buffer.append("* 把DynaBean转化成").append(cls).append("\n");
		buffer.append("* @param\tbean\n");
		buffer.append("*\n").append("* @author\t").append(author).append("\n");
		buffer.append("* @version\t").append(dateFormat.format(new Date())).append("\n");
		buffer.append("* @since\t").append("V0.1.1").append("\n");
		buffer.append("*/\n");
		buffer.append("public ").append(cls).append(" fromDynaBean(DynaBean bean){\n");
		buffer.append("if(bean != null){\n");
		
		Map<String, String[]> maps = getFileString();
		Iterator<String> iters = maps.keySet().iterator();
		while(iters.hasNext()){
			String key = iters.next();
			String[] strs = maps.get(key);
			buffer.append("this.set").append(firstToUpperCase(strs[2])).append("(");
			boolean end = false;
			if(strs[1].equalsIgnoreCase("int") || strs[1].equalsIgnoreCase("integer")){
				buffer.append("Integer.parseInt(");
				end = true;
			}
			if(strs[1].equalsIgnoreCase("boolean")){
				buffer.append("BooleanUtil.isTrue(");
				end = true;
			}
			if(strs[1].equalsIgnoreCase("BigDecimal")){
				buffer.append("new BigDecimal(");
				end = true;
			}
			if(strs[1].equalsIgnoreCase("Date")){
				buffer.append("BeanUtils.getDateValue");
			}else{
				buffer.append("BeanUtils.getString");
			}
			buffer.append("(bean, ").append("\"").append(key).append("\")");
			if(end){
				buffer.append(")");
			}
			buffer.append(");\n");
		}
		buffer.append("}\n");
		buffer.append("return this;\n");
		buffer.append("}");
		return buffer.toString();
	}
	
	/**
	 * convertObjectToMap
	 * 
	 * @param author
	 * @return
	 * @throws Exception
	 */
	public static String transConvertObjectToMap(String author) throws Exception{
		SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy-MM-dd");
		String cls = file.getName().substring(0, file.getName().lastIndexOf("."));
		String clsField = firstToLowerCase(cls);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("/**\n").append("*\n");
		buffer.append("* 把").append(cls).append("实体类转化成").append("Map对象\n");
		buffer.append("* @param\t").append(clsField).append("\n");
		buffer.append("*\n").append("* @author\t").append(author).append("\n");
		buffer.append("* @version\t").append(dateFormat.format(new Date())).append("\n");
		buffer.append("* @since\t").append("V0.1.1").append("\n");
		buffer.append("*/\n");
		buffer.append("public ").append("Map<String, Object>").append(" toMap(){\n");
		buffer.append("Map<String,Object> map = new HashMap<String, Object>();\n");
		
		Map<String, String[]> maps = getFileString();
		Iterator<String> iters = maps.keySet().iterator();
		while(iters.hasNext()){
			String key = iters.next();
			String[] strs = maps.get(key);
			buffer.append("map.put(\"").append(key).append("\", ");
			boolean end = false;
			if(strs[1].equalsIgnoreCase("boolean")){
				buffer.append("BooleanUtil.getValue(");
				end = true;
			}
			buffer.append("this");
			if(strs[1].equalsIgnoreCase("boolean")){
				buffer.append(".is");
			}else{
				buffer.append(".get");
			}
			buffer.append(firstToUpperCase(strs[2])).append("()");
			if(end){
				buffer.append(")");
			}
			buffer.append(");\n");
		}
		buffer.append("return map;\n");
		buffer.append("}");
		return buffer.toString();
	}
	
	public static void toFile(String author){
		try{
			String convertObjectToMap = TranUtils.transConvertObjectToMap(author);
			String convertDynaBeanToObject = TranUtils.transConvertDynaBeanToObject(author);
//			compose = new String(compose.getBytes("GBK"), "UTF-8");
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			writer.write(convertObjectToMap + "\n\n" + convertDynaBeanToObject);
			writer.flush();
			Logger logger = Logger.getLogger(TranUtils.class.getName());
			logger.info("----文件写入成功----");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		TranUtils.setFileName("D:/workspace/NewEclipse/AirQualityDataAutoDocking2/src/com/szboanda/sjdj/bean/HourlyDataBean.java");
		TranUtils.toFile("王贤炳");
	}
}
