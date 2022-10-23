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
import com.laxcus.site.*;

/**
 * 工作类站点的设置发送异步数据超时调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class SubReplySendTimeoutInvoker extends CommonReplySendTimeoutInvoker {

	/**
	 * 构造设置发送异步数据超时调用器，指定命令
	 * @param cmd 设置发送异步数据超时
	 */
	public SubReplySendTimeoutInvoker(ReplySendTimeout cmd) {
		super(cmd);
	}

	/**
	 * 向请求端返回处理结果
	 * @param success
	 */
	private void reply(boolean success) {
		Node node = getLocal();
		
		// 处理结果
		ReplySendTimeoutItem item = new ReplySendTimeoutItem(node, success);
		if (success) {
			item.setDisableTimeout(ReplyWorker.getDisableTimeout());
			item.setSubPacketTimeout(ReplyWorker.getSubPacketTimeout());
			item.setInterval(ReplyWorker.getSendInterval());
		}

		// 生成结果
		ReplySendTimeoutProduct product = new ReplySendTimeoutProduct();
		product.add(item);
//		product.setProcessTime(getProcessTime());
		replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		boolean success = reset();
		// 通知调用端
		reply(success);
		
		// 退出
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
