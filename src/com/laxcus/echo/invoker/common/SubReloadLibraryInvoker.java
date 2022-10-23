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
 * 工作类站点的重新加载动态链接库命令调用器 <br>
 * 
 * 用在除TOP、HOME、FRONT、WATCH四类站点之外的所有站点，即AID、ARCHIVE、LOG、DATA、WORK、BUILD、CALL。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2018
 * @since laxcus 1.0
 */
public class SubReloadLibraryInvoker extends CommonReloadLibraryInvoker {

	/**
	 * 构造重新加载动态链接库命令调用器，指定命令
	 * @param cmd 重新加载动态链接库命令
	 */
	public SubReloadLibraryInvoker(ReloadLibrary cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 重装加载本地动态链接库
		ReloadLibraryItem item = reload();
		// 生成报告
		ReloadLibraryProduct product = new ReloadLibraryProduct();
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
