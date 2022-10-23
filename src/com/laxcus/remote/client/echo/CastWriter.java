/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.echo;

/**
 * 异步数据写入器。<br>
 * 
 * 这个接口在EchoBuffer/DoubleClient实现，被ReplyReceiver调用，ReplyReceiver把接收的数据写入它的缓存。
 * 
 * 功能同EchoReceiver接口的push方法。
 * 
 * @author scott.liang
 * @version 1.0 1/1/2018
 * @since laxcus 1.0
 */
public interface CastWriter {

	/**
	 * 向回显缓存写入数据。
	 * 
	 * @param seek 被写入数据的开始位置
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效数据长度
	 * 
	 * @return 返回下次数据的开始位置
	 */
	long push(long seek, byte[] b, int off, int len);

	/**
	 * 进入回显缓存写入数据，否则否。
	 * ReplyReceiver在进入线程时设置，退出线程时取消。见ReplyRceiver.run方法中的定义。
	 * 
	 * @param running 运行或者否
	 */
	void asPushThread(boolean running);
	
}