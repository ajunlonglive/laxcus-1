/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.reload.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.log.client.*;

/**
 * 应答包尺寸调用器
 * 
 * @author scott.liang
 * @version 1.0 1/11/2019
 * @since laxcus 1.0
 */
public abstract class CommonReplyPacketSizeInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造应答包尺寸调用器，指定命令
	 * @param cmd 应答包尺寸
	 */
	protected CommonReplyPacketSizeInvoker(ReplyPacketSize cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReplyPacketSize getCommand() {
		return (ReplyPacketSize) super.getCommand();
	}

	/**
	 * 释放JVM内存释放间隔
	 * @return 返回真
	 */
	protected boolean reset() {
		ReplyPacketSize cmd = getCommand();

		// 区分公网/内网，分别设置
		if (cmd.isWide()) {
			ReplyTransfer.setDefaultWidePacketContentSize(cmd.getPacketSize());
			ReplyTransfer.setDefaultWideSubPacketContentSize(cmd.getSubPacketSize());
		} else {
			ReplyTransfer.setDefaultPacketContentSize(cmd.getPacketSize());
			ReplyTransfer.setDefaultSubPacketContentSize(cmd.getSubPacketSize());
		}
		
//		// 调整包尺寸
//		if (isLinux()) {
//			ReplyTransfer.doLinuxPacketSize();
//		}

		//		Logger.info(this, "reset", "packet size:%d, sub packet size:%d",
		//				cmd.getPacketSize(), cmd.getSubPacketSize());

		Logger.info(this, "reset", "packet size:%d, sub packet size:%d",
				ReplyTransfer.getDefaultPacketContentSize(), ReplyTransfer.getDefaultSubPacketContentSize());

		return true;
	}

}