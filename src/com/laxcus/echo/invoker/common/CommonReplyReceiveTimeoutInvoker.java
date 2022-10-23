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
 * 异步接收超时调用器
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public abstract class CommonReplyReceiveTimeoutInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造异步接收超时调用器，指定命令
	 * @param cmd 释放节点内存
	 */
	protected CommonReplyReceiveTimeoutInvoker(ReplyReceiveTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReplyReceiveTimeout getCommand() {
		return (ReplyReceiveTimeout) super.getCommand();
	}

	/**
	 * 重置参数
	 * @return 返回真
	 */
	protected boolean reset() {
		ReplyReceiveTimeout cmd = getCommand();

		ReplyHelper.setDisableTimeout((int) cmd.getDisableTimeout());
		ReplyHelper.setSubPacketTimeout((int) cmd.getSubPacketTimeout());

		Logger.info(this, "reset", "packet disable timeout %d ms, sub packet timeout:%d ms",
				ReplyHelper.getDisableTimeout(), ReplyHelper.getSubPacketTimeout());

		return true;
	}

}