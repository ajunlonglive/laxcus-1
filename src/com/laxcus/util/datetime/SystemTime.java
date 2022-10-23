/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.datetime;

/**
 * 本地系统时间。<br>
 * 时间参数从LINUX/WINDOWS获取。
 * 
 * @author scott.liang
 * @version 1.0 5/2/2009
 * @since laxcus 1.0
 */
public class SystemTime {

//	static {
//		try {
//			System.loadLibrary("laxcusnow");
//		} catch (UnsatisfiedLinkError exp) {
//			exp.printStackTrace();
//		} catch (SecurityException exp) {
//			exp.printStackTrace();
//		}
//	}

	/**
	 * 返回本地系统时间
	 * @return 返回SimpleTimestamp格式时间
	 */
	public native static long get();
	
	/**
	 * 设置为本地系统时间。<br>
	 * 在LINUX系统执行这个操作时，必须获得ROOT权限。
	 * @param time 经过SimpleTimestamp格式的时间
	 * @return 设置成功返回0，否则是负数。
	 */
	public native static int set(long time);

}