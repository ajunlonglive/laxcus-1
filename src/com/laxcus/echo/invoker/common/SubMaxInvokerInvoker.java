/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.mix.*;
import com.laxcus.remote.client.*;
import com.laxcus.site.*;

/**
 * 工作类站点的调用器数目调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 9/9/2020
 * @since laxcus 1.0
 */
public class SubMaxInvokerInvoker extends CommonMaxInvokerInvoker {

	/**
	 * 构造调用器数目调用器，指定命令
	 * @param cmd 调用器数目
	 */
	public SubMaxInvokerInvoker(MaxInvoker cmd) {
		super(cmd);
	}

	/**
	 * 向请求端返回处理结果
	 * @param success
	 */
	private void reply(boolean success) {
		Node node = getLocal();
		
		// 生成单元
		MaxInvokerItem item = new MaxInvokerItem(node, success);
		if (success) {
			item.setInvokers(EchoTransfer.getMaxInvokers());
			item.setConfineTime(EchoTransfer.getMaxConfineTime());
		}
		
		MaxInvokerProduct product = new MaxInvokerProduct();
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