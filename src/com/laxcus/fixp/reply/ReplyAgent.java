/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import com.laxcus.echo.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.thread.*;

/**
 * 异步反馈数据代理。
 * 提供基础和公共的接口。
 * 
 * @author scott.liang
 * @version 1.0 7/20/2018
 * @since laxcus 1.0
 */
public abstract class ReplyAgent extends MutexThread {

	/**
	 * 构造默认的异步反馈数据代理
	 */
	protected ReplyAgent() {
		super();
	}

	/**
	 * 从数据包中取出异步通信标识
	 * @param packet FIXP数据包
	 * @return 异步通信标识
	 */
	protected CastFlag readFlag(Packet packet) {
		try {
			byte[] b = packet.findRaw(MessageKey.CAST_FLAG);
			if (b != null) {
				return new CastFlag(b);
			}
		} catch (Throwable e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 从数据包中取出异步通信码
	 * @param packet FIXP数据包
	 * @return 异步通信码
	 */
	protected CastCode readCode(Packet packet) {
		try {
			byte[] b = packet.findRaw(MessageKey.CAST_CODE);
			if (b != null) {
				return new CastCode(b);
			}
		} catch (Throwable e) {
			Logger.error(e);
		}
		return null;
	}

}