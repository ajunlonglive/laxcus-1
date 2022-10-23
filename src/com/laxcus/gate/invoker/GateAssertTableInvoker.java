/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.table.*;

/**
 * 判断数据表存在调用器。<br>
 * 命令转发给BANK站点。<br>
 * 判断数据表存在，普通用户和管理员都可以查询。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class GateAssertTableInvoker extends GateInvoker {

	/**
	 * 判断数据表存在调用器，设置异步命令
	 * @param cmd 判断数据表存在
	 */
	public GateAssertTableInvoker(AssertTable cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		boolean success = transmit();
		if (!success) {
			failed();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return reflect();
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	//	 */
	//	@Override
	//	public AssertTable getCommand() {
	//		return (AssertTable) super.getCommand();
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		// 投递到BANK站点
	//		boolean success = launchToHub();
	//		if (!success) {
	//			failed();
	//		}
	//		return success;
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	//	 */
	//	@Override
	//	public boolean ending() {
	//		int index = findEchoKey(0);
	//		AssertTableProduct product = null;
	//		try {
	//			if (isSuccessObjectable(index)) {
	//				product = getObject(AssertTableProduct.class, index);
	//			}
	//		} catch (VisitException e) {
	//			Logger.error(e);
	//		}
	//
	//		// 发生错误，反馈一个操作失败通知
	//		boolean success = (product != null);
	//		if (success) {
	//			replyProduct(product);
	//		} else {
	//			failed();
	//		}
	//		return useful(success);
	//	}

}