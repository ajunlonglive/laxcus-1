/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.io.*;
import java.util.*;

import com.laxcus.echo.invoke.*;

/**
 * LINUX磁盘容量读取器
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public class DiskLinuxReader {

	/**
	 * 构造默认的磁盘容量读取器
	 */
	public DiskLinuxReader() {
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

		// 文件系统ID集合
		TreeSet<Long> keys = new TreeSet<Long>();
		
		File[] roots =	File.listRoots();
		
		// 读取每个目录
		for (File dir : roots) {
			// 必须保证是目录且存在！
			boolean success = (dir.exists() && dir.isDirectory());
			if (!success) {
				continue;
			}
			
			// 取文件系统ID，如果是-1，表示不能检测到。
			long sid = LinuxDisk.getFileSystemId(dir.getPath());
			if (sid == -1) {
				continue;
			}

			long total = dir.getTotalSpace(); // 全部空间
			long free = dir.getUsableSpace(); // 可使用的空间

			// 如果存在，忽略它
			if (keys.contains(sid)) {
				continue;
			}

			// 保存和统计
			keys.add(sid);
			totalCapacity += total;
			freeCapacity += free;
		}
		
		DiskInfoItem item = new DiskInfoItem();
		item.setTotalCapacity(totalCapacity);
		item.setFreeCapacity(freeCapacity);
		return item;
	}
}
