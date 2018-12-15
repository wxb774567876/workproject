package com.szboanda.platform.tools.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
/**
 * 
* @Title: FileLoader.java
* @Package com.szboanda.platform.util
* @ 此类描述的是：读取文件帮助类
* @author wangjihong
* @Copyright: PowerData Software Co.,Ltd. Rights Reserved.
* @Company:深圳市博安达软件开发有限公司
* @version V1.0   create date: 2013-9-16 上午11:26:40
 */
public class FileLoader {
	/**
	 * 读取文件
	 * @param path
	 * @return
	 */
	public static InputStream loadFile(String path) {
		URL url = FileLoader.class.getResource(path);
		if (url == null) {
			url = FileLoader.class.getResource("/" + path);
		}
		if (url != null) {
			try {
				return url.openStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 读取文件
	 * @param path
	 * @return
	 */
	public static InputStream readFile(String path) {
		try {
			File file =  new File(path);
			FileInputStream in = new FileInputStream(file);  
			return in;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
