/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.reload.*;

/**
 * 扫描堆栈命令调用器 <br>
 * 
 * 应用到除FRONT/WATCH之外的所有节点。
 * 
 * @author scott.liang
 * @version 1.0 7/26/2018
 * @since laxcus 1.0
 */
public class SubScanCommandStackInvoker extends CommonScanCommandStackInvoker {

	/**
	 * 构造扫描堆栈命令命令调用器，指定命令
	 * @param cmd 扫描堆栈命令命令
	 */
	public SubScanCommandStackInvoker(ScanCommandStack cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 重装加载本地动态链接库
		ScanCommandStackItem item = reload();
		// 生成报告
		ScanCommandStackProduct product = new ScanCommandStackProduct();
		product.add(item);
		boolean success = replyProduct(product);
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
