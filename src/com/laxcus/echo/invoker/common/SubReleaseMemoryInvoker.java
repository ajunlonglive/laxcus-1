/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.reload.*;
import com.laxcus.site.*;

/**
 * 工作类站点的释放节点内存命令调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/11/2015
 * @since laxcus 1.0
 */
public class SubReleaseMemoryInvoker extends CommonReleaseMemoryInvoker {

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 释放节点内存命令
	 */
	public SubReleaseMemoryInvoker(ReleaseMemory cmd) {
		super(cmd);
	}

	/**
	 * 向请求端返回处理结果
	 * @param success
	 */
	private void reply(boolean success) {
		Node node = getLocal();
		ReleaseMemoryProduct product = new ReleaseMemoryProduct(node, success);
		replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		boolean success = clear();
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
