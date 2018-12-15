package com.szboanda.platform.deploy;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class DeployHelper {
	//项目目录，指向项目源代码目录
	private static String mainpath = ""; 
	//起始时间，只搜索修改时间晚于起始时间的文件
	private static String stime = ""; 
	private static String etime = ""; 
	//生成的更新文件的目录
	private static String destpath = "";
	
	private static String webappName = "";
	
	private static Date starttime =  null; 
	private static Date endtime =  null; 
	
	private static String tips = " **************************************************************       " +
			"\n *  配置文件config.properties中参数的含义：                           " +
			"\n *  StartTime:程序将根据搜索出所有修改时间晚于这个时间的文件          " +
			"\n *  EndTime:程序将根据搜索出所有修改时间早于这个时间的文件               " +
			"\n *     格式为：2015-05-21 18：00                                      " +
			"\n *  MainPath:指定Maven工程源码路径                                    " +
			"\n *     例如：D:\\WORKSPACES\\NEW_SVN\\LIMS\\src\\main                 " +
			"\n *  DestPath：指定更新文件生成到哪个目录                              " +
			"\n *     例如：D:\\updateCode                                           " +
			"\n **************************************************************       ";
	
	public static void main(String[] args)  throws Exception {
//		File f = new File("D:\\WORKSPACES\\Eclipse_Luma\\LIMS\\src\\main\\webapp\\WEB-INF\\classes\\com\\szboanda\\lims\\core\\sample\\domain\\SamplingRecordDefinition.class");
//		System.out.println(f.getName());
//		System.out.println(f.getName().replace(".class", ""));   
//		test(args);  
		test(args);
	}
	
	public static void test(String[] args) throws Exception{
		System.out.println(tips); 
		System.out.println(" 请将配置文件config.properties与jar文件放在同一目录下，按任意键继续!");
		 
		if(!readConfigProperties()){
			System.in.read();  
			return ;
		}
		
		//先查找出所有符合条件的java文件
		List<String> files = findModifiedJavaCode(mainpath + "\\java");  
		List<String> classes = convertClassFilePath(files);   
		
		//copy class文件 
		copyClassFiles(classes);  
		List<String> otherFiles = findOtherFiles(mainpath + "\\" + webappName); 
		//copy 其它文件 
		copyOtherFiles(otherFiles);  
		//copy java文件 
		copyJavaFiles(files); 
		System.out.println("处理完成，共计：" + classes.size() + "个class文件," + otherFiles.size() + "个其它文件!");
		System.in.read();  
	}

	private static void copyJavaFiles(List<String> files) {
		if(files == null){
			return ;  
		}
		String dest = (destpath + "/java").replaceAll("\\\\", "/");
		String src = (mainpath + "\\java").replaceAll("\\\\", "/");    
		
		for(String f : files){  
			f = f.replaceAll("\\\\", "/");    
			String d = f.replace(src, dest);   
			File fd = new File(d);
			File dir = new File(fd.getParent()); 
			if(!dir.exists()){
				dir.mkdirs(); 
			}
			copyFile(f,d);  
		}
	}

	private static boolean readConfigProperties() {
		File f = new File("config.properties");
		if(!f.exists()){
			System.out.println("配置文件：" + f.getAbsolutePath() + "不存在，无法运行!");
			return false;  
		}
		try{
			Properties p = new Properties();
			p.load(new FileInputStream(f)); 
			stime = p.getProperty("StartTime");
			etime = p.getProperty("EndTime");
			destpath =  p.getProperty("DestPath");
			mainpath =  p.getProperty("MainPath");
			starttime = parseDate(stime); 
			endtime = parseDate(etime); 
			webappName = p.getProperty("webappName");
			if((starttime == null) && (endtime == null)){
				throw new Exception("不能两个日期都为空!");
			}
		}catch(Exception ff){
			System.out.println("配置文件:" + f.getAbsolutePath() + "读取异常，无法运行!");
			ff.printStackTrace();
			return false;
		}
		return true;
	}

	private static List<String> findOtherFiles(String basePath) {
		File base = new File(basePath); 
		List<String> results = new ArrayList<String>();
		//如果是目录
		if(base.isDirectory() && (!base.isHidden())){
			String path = base.getAbsolutePath().replaceAll("\\\\", "/");
			
			if(path.endsWith("/WEB-INF/classes")){ 
				return results;
			} 
			//罗列所有子文件
			File[] files = base.listFiles();
			for(File file : files){
				//如果是目录,递归查处所有文件 
				if(file.isDirectory()){ 
					results.addAll(findOtherFiles(file.getAbsolutePath())); 
				}
				//如果是文件，则判断是否符合条件
				if(file.isFile() && check2(file)){ 
					results.add(file.getAbsolutePath());
				}
			}
		//如果是文件 
		}else if(base.isFile()){  
			if(check2(base)){
				results.add(base.getAbsolutePath()); 
			}
		}
		return results; 
	}

	private static boolean check2(File f) {
		if(!f.isFile()){
			return false;
		}
		Date modify = new Date(f.lastModified());
		if((starttime != null) && (!modify.after(starttime))){ 
			return false;
		}
		if((endtime != null) && (!modify.before(endtime))){   
			return false;  
		}  
		return true;
	}

	private static void copyOtherFiles(List<String> otherFiles) {
		if(otherFiles == null){
			return ;  
		}
		String dest = (destpath + "/" + webappName).replaceAll("\\\\", "/");
		String src = (mainpath + "\\" + webappName).replaceAll("\\\\", "/");  
		
		for(String f : otherFiles){ 
			f = f.replaceAll("\\\\", "/"); 
			String d = f.replace(src, dest);  
			File fd = new File(d);
			File dir = new File(fd.getParent()); 
			if(!dir.exists()){
				dir.mkdirs(); 
			}
			copyFile(f,d);  
		}
	}

	private static void copyClassFiles(List<String> classes) {
		String dest = (destpath + "/"+ webappName +"/WEB-INF/classes").replaceAll("\\\\", "/");  
		String src = (mainpath + "\\"+ webappName +"\\WEB-INF\\classes").replaceAll("\\\\", "/");    
		for(String f : classes){
			String d = f.replace(src, dest);
			File fd = new File(d);
			File dir = new File(fd.getParent()); 
			if(!dir.exists()){
				dir.mkdirs(); 
			}
			copyFile(f,d);
			File fsrc = new File(f); 
			if(fsrc.exists()){
				//源文件的父目录
				File parent = fsrc.getParentFile();    
				if((parent!=null) && parent.exists()){
					File[] files = parent.listFiles();
					if(files != null){
						//源文件的名称-去掉.class
						String fname = fsrc.getName().replace(".class", "");
						//同目录下的其他文件 
						for(File other : files){
							//其它文件的名称 
							String oname = other.getName().replace(".class", ""); 
							//文件名称中包含源文件名、不完全相同、长度不一样 
							if((oname.indexOf(fname) > -1) && (!fname.equals(oname)) && (oname.length() > fname.length())){   
								//目标文件夹
								File destdir = new File(d).getParentFile();
								//copy到目标文件件
								copyFile(other.getAbsolutePath(),destdir.getAbsolutePath() + File.separator + other.getName());
							}
						}
					}
				}
			}
		}
	}

	private static void copyFile(String f, String d) {
//		System.out.println(f); 
//		System.out.println(d);
		byte[] buffer = new byte[1024*8];
		try{
			FileInputStream fis = new FileInputStream(f);
			FileOutputStream fos = new FileOutputStream(d); 
			int length = -1;
			while((length = fis.read(buffer)) != -1){
				fos.write(buffer,0,length);
			} 
			fos.close();
			fis.close();  
			System.out.println("copy:" + f + "\nto  :" + d + " 成功!");  
		}catch(Exception ff){ 
			System.out.println("copy:" + f + "\nto  :" + d + " 出错:");  
			ff.printStackTrace(); 
		}
	}

	/**
	 * 根据被修改的java文件找到被修改过的class文件 
	 * @param files
	 * @return
	 */
	private static List<String> convertClassFilePath(List<String> files) {
		List<String> classes = new ArrayList<String>();
		String s1 = (mainpath + "\\java").replaceAll("\\\\", "/");
		String s2 = (mainpath + "\\"+ webappName +"\\WEB-INF\\classes").replaceAll("\\\\", "/");  
		String s3 = ".java";
		String s4 = ".class"; 
		if(files != null){
			for(String s : files){ 
				s = s.replaceAll("\\\\", "/");
				s = s.replace(s1, s2);  
				s = s.replace(s3, s4);
				classes.add(s);   
			}
		}
		return classes;
	}


	/**
	 * 根据源代码目录和起始时间，查找出目录下，所有修改时间晚于起始时间的java文件
	 * @param codepath
	 * @param starttime
	 * @return
	 * @throws Exception
	 */
	private static List<String> findModifiedJavaCode(String codepath) throws Exception {
		File base = new File(codepath);
		List<String> results = new ArrayList<String>();
		//如果是目录
		if(base.isDirectory()){
			//罗列所有子文件
			File[] files = base.listFiles();
			for(File file : files){
				//如果是目录,递归查处所有文件
				if(file.isDirectory()){
					results.addAll(findModifiedJavaCode(file.getAbsolutePath())); 
				}
				//如果是文件，则判断是否符合条件
				if(file.isFile() && check(file)){
					System.out.println(file.getAbsolutePath());
					results.add(file.getAbsolutePath());
				}
			}
		//如果是文件
		}else if(base.isFile()){
			if(check(base)){
				results.add(base.getAbsolutePath()); 
			}
		}
		return results; 
	}
	
	/**
	 * 检查是否文件是否复核要求：java文件，而且修改时间晚于指定的起始时间
	 * @param f
	 * @param starttime
	 * @return
	 */
	private static boolean check(File f){
		if(!f.isFile()){
			return false;   
		}
		Date modify = new Date(f.lastModified());
		if((starttime != null) && (!modify.after(starttime))){ 
			return false;  
		}
		if((endtime != null) && (!modify.before(endtime))){ 
			return false;  
		} 
		if(!(f.getAbsolutePath().endsWith(".java") || f.getAbsolutePath().endsWith(".xml"))){
			return false;
		}
		return true;
	}
	
	private static Date parseDate(String time){ 
		String format = "yyyy-MM-dd HH:mm";
        try {
        	time = time.replaceAll("\"", "");   
            SimpleDateFormat formater = new SimpleDateFormat(format);
            return formater.parse(time);
        } catch (Exception e) {
//           e.printStackTrace(); 
           return null;
        }  
	} 
	
	private static void printList(List<String> files) {
		if(files == null){
			return ;
		}
		int index = 0;
		for(String file : files){
			System.out.println((++index) + "\t" + file);  
		}
	}


}
