/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.mid;

/**
 * 中间数据写入器。<br>
 * 
 * 中间数据的存储位置只能在内存或者硬盘中选择一个。
 * 如果用户选择内存做为中间数据的保存地，那么系统将根据本地的情况来决定。
 * 如果内存不足，仍然会将数据保存到硬盘上。
 * 
 * @author scott.liang
 * @version 1.1 3/15/2013
 * @since laxcus 1.0
 */
public interface MiddleWriter extends MiddleTub {

	/**
	 * 中间数据在硬盘或者内存中的保存时间，单位：毫秒。<br>
	 * 这个参数由系统规定，超时后系统将删除数据。
	 * 
	 * @return 返回超时时间
	 */
	long getTimeout();
}