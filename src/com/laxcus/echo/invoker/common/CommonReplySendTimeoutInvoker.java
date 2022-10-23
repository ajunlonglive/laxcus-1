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
 * @version 1.0 9/1/2019
 * @since laxcus 1.0
 */
public abstract class CommonReplySendTimeoutInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造异步接收超时调用器，指定命令
	 * @param cmd 释放节点内存
	 */
	protected CommonReplySendTimeoutInvoker(ReplySendTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReplySendTimeout getCommand() {
		return (ReplySendTimeout) super.getCommand();
	}

	/**
	 * 重置参数
	 * @return 返回真
	 */
	protected boolean reset() {
		ReplySendTimeout cmd = getCommand();

		ReplyWorker.setDisableTimeout((int) cmd.getDisableTimeout());
		ReplyWorker.setSubPacketTimeout((int) cmd.getSubPacketTimeout());
		ReplyWorker.setSendInterval((int) cmd.getInterval());

		Logger.info(this, "reset", "packet disable time: %d ms, sub packet time:%d ms, sub packet send interval:%d ms",
				ReplyWorker.getDisableTimeout(), ReplyWorker.getSubPacketTimeout(), ReplyWorker.getSendInterval());

		return true;
	}

}