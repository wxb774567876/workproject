package com.szboanda.platform.tools;

public class T2 {

	/**
	 * @Title: main
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param args   
	 * @return void  返回类型
	 * @throws
	 */

	public static void main(String[] args) {
		String sql = " alter table %s add %s %s(%s);\n "
			 + " comment on column %s.%s is '%s'; ";
		
		String sql1 = "alter table %s modify %s %s(%s);";
		String[] arg = new String[7];
		arg[0] = "T_ADMIN_RMS_";
		arg[1] = "XH";
		arg[2] = "VARCHAR2";
		arg[3] = "50";
		arg[4] = "T_ADMIN_RMS_";
		arg[5] = "XH";
		arg[6] = "序号";
		System.out.println(String.format(sql,arg));
		String[] arg1 = new String[7];
		arg1[0] = "T_ADMIN_RMS_XT";
		arg1[1] = "cjr";
		arg1[2] = "VARCHAR2";
		arg1[3] = "400";
		System.out.println(String.format(sql1,arg1));
	}

}
