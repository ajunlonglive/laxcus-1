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

/**
 * 工作类站点的应答包尺寸调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 1/11/2019
 * @since laxcus 1.0
 */
public class SubReplyPacketSizeInvoker extends CommonReplyPacketSizeInvoker {

	/**
	 * 构造应答包尺寸调用器，指定命令
	 * @param cmd 应答包尺寸
	 */
	public SubReplyPacketSizeInvoker(ReplyPacketSize cmd) {
		super(cmd);
	}
	
	/**
	 * 生成单元
	 * @param success
	 * @return
	 */
	private ReplyPacketSizeItem createItem(boolean success) {
		ReplyPacketSizeItem item = new ReplyPacketSizeItem(getLocal(), success);
		if (success) {
			item.setPacketSize(ReplyTransfer.getDefaultPacketContentSize());
			item.setSubPacketSize(ReplyTransfer.getDefaultSubPacketContentSize());
		}
		return item;
	}

	/**
	 * 向请求端返回处理结果
	 * @param success 成功或者失败
	 */
	private void reply(boolean success) {
		ReplyPacketSizeItem item = createItem(success);
		ReplyPacketSizeProduct product = new ReplyPacketSizeProduct();
		product.add(item);
//		product.setProcessTime(getProcessTime());
		// 反馈结果
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
