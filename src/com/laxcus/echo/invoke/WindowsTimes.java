/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

/**
 * WINDOWS系统时间片获取接口 <br><br>
 * 
 * 注意：<br>
 * laxcuswnf.dll要放在“java.library.path”目录下面，或者用户当前目录；否则会在加载时找不到。<br>
 * 
 * @author scott.liang
 * @version 1.0 10/8/2011
 * @since laxcus 1.0
 */
public class WindowsTimes {

	//	/**
	//	 * 在启动时加载
	//	 */
	//	static {
	//		try {
	//			System.loadLibrary("laxcuswnf");
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		}
	//	}

	/**
	 * 返回时间片长整型值，共三个，依次时：空闲、内核、用户。
	 * @return 成功，返回long数组；否则是空值。
	 */
	public native static long[] getTimes();

}