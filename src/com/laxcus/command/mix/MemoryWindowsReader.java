/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import com.laxcus.echo.invoke.*;

/**
 * WINDOWS内存容量读取器
 * 
 * @author scott.liang
 * @version 1.0 12/23/2020
 * @since laxcus 1.0
 */
public class MemoryWindowsReader {
	
	/**
	 * 构造WINDOWS内存容量读取器
	 */
	public MemoryWindowsReader() {
		super();
	}

	/**
	 * 读取资源
	 * @return 返回MemoryInfoItem实例
	 */
	public MemoryInfoItem read() {
		long[] b = WindowsMemory.getMemory();
		if (b == null || b.length != 3) {
			return null;
		}

		// 三个参数，依次是：占用率、全部内存、剩余内存（可用内存）。
		// long rate = b[0];
		long total = b[1];
		long free = b[2];

		// 构造参数
		MemoryInfoItem item = new MemoryInfoItem();
		item.setTotal(total);
		item.setAvailable(free);
		return item;
	}

}