package com.szboanda.sjdj.utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogHandler 
{
	private final static String exceptionLogFile = "./././././exception.log";
	private final static String successfulLogFile = "./././././success.log";
	private static boolean hasFile = false;
	
	/**
	 * 判断日志文件是否存在，不存在则创建
	 * 
	 *
	 * @author  王贤炳
	 * @since   2016年1月11日
	 * @version V0.2.0
	 */
	private static void generateLogFile()
	{
		File file = new File(exceptionLogFile);
		File successfulFile = new File(successfulLogFile);
		if(!file.exists()){
			try {
				file.createNewFile();			
			} catch(IOException e) {
				e.printStackTrace();
			}
		}else{
			deleteFileAndRecreate(file);
		}
		if(!successfulFile.exists()) {
			try {
				successfulFile.createNewFile();				
			} catch(IOException e) {
				System.out.println(e.toString());
			}
		}else{
			deleteFileAndRecreate(successfulFile);
		}
		hasFile = true;
	}
	
	public static void logExceptionInfo(String msg, Exception ex) {	
		if(!hasFile) {
			generateLogFile();
		}
		
		File file = new File(exceptionLogFile);
		if(file.canWrite()) {
			PrintWriter streamWriter = null;
			try
			{				
				streamWriter = new PrintWriter(new FileOutputStream(file, true));
				streamWriter.write("记录日志时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\r\n");
				streamWriter.write(msg + "错误信息如下：\r\n");
				streamWriter.write("----------------------------------------------------------------\r\n");
				ex.printStackTrace(streamWriter);
				streamWriter.write("----------------------------------------------------------------\r\n");
				streamWriter.write("\r\n");
				streamWriter.close();
			} catch(IOException e) {
				System.out.println(e.toString());				
			} finally {
				streamWriter.close();
			}
		} else {
			System.out.println("异常日志文件没有写权限");
		}
	}
	
	public static void logSuccessfulInfo(String msg) {	
		if(!hasFile) {
			generateLogFile();
		}
		
		File file = new File(successfulLogFile);
		if(file.canWrite()) {
			PrintWriter streamWriter = null;
			try {				
				streamWriter = new PrintWriter(new FileOutputStream(file, true));
				streamWriter.write("记录日志时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\r\n");
				streamWriter.write("----------------------------------------------------------------\r\n");
				streamWriter.write(msg + "\r\n");
				streamWriter.write("----------------------------------------------------------------\r\n");
				streamWriter.write("\r\n");
				streamWriter.close();
			} catch(IOException e) {
				System.out.println(e.toString());				
			} finally {
				streamWriter.close();
			}
		} else {
			System.out.println("异常日志文件没有写权限");
		}
	}
	
	public static void deleteFileAndRecreate(File file){
		long size = file.length()/1024/2014;//该文件大小,兆
		if(size > 60){
			try {
				file.delete();
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
