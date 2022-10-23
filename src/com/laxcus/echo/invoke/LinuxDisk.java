/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

/**
 * LINUX系统磁盘
 * 
 * @author scott.liang
 * @version 1.0 8/18/2019
 * @since laxcus 1.0
 */
public class LinuxDisk {

	/**
	 * 返回磁盘目录对应的文件系统ID
	 * @param path 磁盘目录
	 * @return 返回长整型的文件系统ID，失败是-1。
	 */
	public native static long getFileSystemId(String path);
}
