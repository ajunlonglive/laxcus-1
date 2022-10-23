/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.io.*;

/**
 * WINDOWS磁盘容量读取器
 * 
 * @author scott.liang
 * @version 1.0 12/23/2020
 * @since laxcus 1.0
 */
public class DiskWindowsReader {
	
	/**
	 * 构造WINDOWS磁盘容量读取器
	 */
	public DiskWindowsReader() {
		super();
	}

	/**
	 * 读出参数
	 * @return
	 */
	public DiskInfoItem read() {
		// 提取磁盘空间
		long totalCapacity = 0L;
		long freeCapacity = 0L;
		
		File[] roots =	File.listRoots();
		
		// 读取每个目录
		for (File dir : roots) {
			// 必须保证是目录且存在！
			boolean success = (dir.exists() && dir.isDirectory());
			if (!success) {
				continue;
			}
			
			long total = dir.getTotalSpace(); // 全部空间
			long free = dir.getUsableSpace(); // 可使用的空间
			
			totalCapacity += total;
			freeCapacity += free;
		}
		
		DiskInfoItem item = new DiskInfoItem();
		item.setTotalCapacity(totalCapacity);
		item.setFreeCapacity(freeCapacity);
		return item;
	}
	
}
