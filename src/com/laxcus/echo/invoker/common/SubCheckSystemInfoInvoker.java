/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.mix.*;

/**
 * 工作类站点的检测服务器系统信息调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public class SubCheckSystemInfoInvoker extends CommonCheckSystemInfoInvoker {

	/**
	 * 构造检测服务器系统信息调用器，指定命令
	 * @param cmd 检测服务器系统信息
	 */
	public SubCheckSystemInfoInvoker(CheckSystemInfo cmd) {
		super(cmd);
	}

	//	/**
	//	 * 向请求端返回处理结果
	//	 * @param success
	//	 */
	//	private void reply(CheckSystemInfoItem item) {
	//		CheckSystemInfoProduct product = new CheckSystemInfoProduct();
	//		product.add(item);
	//		product.setProcessTime(getThreadUsedTime());
	//		replyProduct(product);
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		CheckSystemInfoItem item = pickup();
	//		// 通知调用端
	//		reply(item);
	//		
	//		// 退出
	//		return useful(item.isSuccessful());
	//	}


	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckSystemInfoItem item = pickup();
		CheckSystemInfoProduct product = new CheckSystemInfoProduct();
		product.add(item);
//		product.setProcessTime(getProcessTime());
		replyProduct(product);

		// 退出
		return useful(item.isSuccessful());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}