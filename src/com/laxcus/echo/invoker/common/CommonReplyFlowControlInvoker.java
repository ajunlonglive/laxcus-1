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
 * 应答数据流量控制参数调用器
 * 
 * @author scott.liang
 * @version 1.0 9/9/2020
 * @since laxcus 1.0
 */
public abstract class CommonReplyFlowControlInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造应答数据流量控制参数调用器，指定命令
	 * @param cmd 应答数据流量控制参数
	 */
	protected CommonReplyFlowControlInvoker(ReplyFlowControl cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReplyFlowControl getCommand() {
		return (ReplyFlowControl) super.getCommand();
	}

	/**
	 * 重置参数
	 * @return 返回真
	 */
	protected boolean reset() {
		ReplyFlowControl cmd = getCommand();

		ReplyTransfer.setDefaultFlowBlocks(cmd.getBlock());
		ReplyTransfer.setDefaultFlowTimeslice(cmd.getTimeslice());
		// 子包尺寸
		int len = cmd.getSubPacketContentSize();
		// 小于规定的最大子包内容尺寸
		if (!ReplyTransfer.isSubPacketContentSize(len)) {
			len = len - ReplyTransfer.PACKET_HEADSIZE; // 减少包头
		}
		if (ReplyTransfer.isSubPacketContentSize(len)) {
			ReplyTransfer.setDefaultSubPacketContentSize(len);
		}

		Logger.info(this, "reset", "flow block is %d, time slice:%d ns", ReplyTransfer.getDefaultFlowBlocks(), 
				ReplyTransfer.getDefaultFlowTimeslice());

		return true;
	}

}