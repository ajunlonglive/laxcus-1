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
 * 应答数据传输模式调用器
 * 
 * @author scott.liang
 * @version 1.0 1/11/2019
 * @since laxcus 1.0
 */
public abstract class CommonReplyPacketModeInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造应答数据传输模式调用器，指定命令
	 * @param cmd 应答数据传输模式
	 */
	protected CommonReplyPacketModeInvoker(ReplyPacketMode cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReplyPacketMode getCommand() {
		return (ReplyPacketMode) super.getCommand();
	}

	/**
	 * 释放JVM内存释放间隔
	 * @return 返回真
	 */
	protected boolean reset() {
		ReplyPacketMode cmd = getCommand();

		ReplyTransfer.setDefaultTransferMode(cmd.getPacketMode());

		Logger.info(this, "reset", "packet transfer mode:%d",
				ReplyTransfer.getDefaultTransferMode());

		return true;
	}

}