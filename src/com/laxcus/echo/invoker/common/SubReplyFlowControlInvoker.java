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
 * 工作类站点的应答数据流量控制参数调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 9/9/2020
 * @since laxcus 1.0
 */
public class SubReplyFlowControlInvoker extends CommonReplyFlowControlInvoker {

	/**
	 * 构造应答数据流量控制参数调用器，指定命令
	 * @param cmd 应答数据流量控制参数
	 */
	public SubReplyFlowControlInvoker(ReplyFlowControl cmd) {
		super(cmd);
	}

	/**
	 * 向请求端返回处理结果
	 * @param success
	 */
	private void reply(boolean success) {
		Node node = getLocal();
		
		// 生成单元
		ReplyFlowControlItem item = new ReplyFlowControlItem(node, success);
		if (success) {
			item.setBlock(ReplyTransfer.getDefaultFlowBlocks());
			item.setTimeslice(ReplyTransfer.getDefaultFlowTimeslice());
			item.setSubPacketContentSize(ReplyTransfer.getDefaultSubPacketContentSize());
		}
		
		ReplyFlowControlProduct product = new ReplyFlowControlProduct();
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
