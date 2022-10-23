/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.invoke;

import com.laxcus.fixp.*;

/**
 * 数据包任务调用接口，由各个节点分别实现具体的类。
 * 
 * @author scott.liang
 * @version 1.1 11/7/2011
 * @since laxcus 1.0
 */
public interface PacketInvoker {

	/**
	 * 设置数据包转发器
	 * @param e 数据包转发器句柄
	 */
	void setPacketTransmitter(PacketTransmitter e);

	/**
	 * 返回数据包转发器句柄
	 * @return 数据包转发器句柄
	 */
	PacketTransmitter getPacketTransmitter();
	
	/**
	 * FIXP数据包调用。数据包可以是请求或者应答的任何一种。如果需要应答，返回一个应答包实例，否则是空指针。
	 * @param packet FIXP数据包（请求/应答）
	 * @return 返回反馈FIXP数据包实例
	 */
	Packet invoke(Packet packet);

}