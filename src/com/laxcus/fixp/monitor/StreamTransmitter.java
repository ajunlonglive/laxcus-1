/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

/**
 * FIXP TCP服务器监听接口
 * 
 * @author scott.liang
 * @version 1.0 3/12/2009
 * @since laxcus 1.0
 */
public interface StreamTransmitter {

	/**
	 * 从内存中注销数据流任务句柄
	 * 
	 * @param task 数据流任务
	 * @return 返回真或者假
	 */
	boolean remove(StreamTask task);

}