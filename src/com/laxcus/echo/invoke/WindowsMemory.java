/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

/**
 * WINDOWS系统内存参数获取接口
 * 
 * 注意：<br>
 * laxcuswm.dll 要放在“java.library.path”目录下面，或者用户当前目录；否则会在加载时找不到。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/9/2019
 * @since laxcus 1.0
 */
public class WindowsMemory {

	/**
	 * 返回内存的三个参数：<br>
	 * 1. 已用内存占比 <br>
	 * 2. 全部内存数 <br>
	 * 3. 自由内存数 <br><br>
	 * 
	 * @return 成功，返回long数组，否则是空指针。
	 */
	public native static long[] getMemory();
}