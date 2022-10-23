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
 * 工作类站点的应答数据传输模式调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 9/3/2020
 * @since laxcus 1.0
 */
public class SubReplyPacketModeInvoker extends CommonReplyPacketModeInvoker {

	/**
	 * 构造应答数据传输模式调用器，指定命令
	 * @param cmd 应答数据传输模式
	 */
	public SubReplyPacketModeInvoker(ReplyPacketMode cmd) {
		super(cmd);
	}
	
	/**
	 * 生成单元
	 * @param success
	 * @return
	 */
	private ReplyPacketModeItem createItem(boolean success) {
		ReplyPacketModeItem item = new ReplyPacketModeItem(getLocal(), success);
		if (success) {
			item.setPacketMode(ReplyTransfer.getDefaultTransferMode());
		}
		return item;
	}

	/**
	 * 向请求端返回处理结果
	 * @param success 成功或者失败
	 */
	private void reply(boolean success) {
		ReplyPacketModeItem item = createItem(success);
		ReplyPacketModeProduct product = new ReplyPacketModeProduct();
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
