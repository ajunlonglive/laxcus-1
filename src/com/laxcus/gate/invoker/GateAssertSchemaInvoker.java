/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.schema.*;

/**
 * 判断数据库存在调用器。<br>
 * 命令转发给BANK站点。<br>
 * 判断数据库存在，普通用户和管理员都可以查询。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class GateAssertSchemaInvoker extends GateInvoker {

	/**
	 * 判断数据库存在调用器，设置异步命令
	 * @param cmd 判断数据库存在
	 */
	public GateAssertSchemaInvoker(AssertSchema cmd) {
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
	//	public AssertSchema getCommand() {
	//		return (AssertSchema) super.getCommand();
	//	}

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
	//		AssertSchemaProduct product = null;
	//		try {
	//			if (isSuccessObjectable(index)) {
	//				product = getObject(AssertSchemaProduct.class, index);
	//			}
	//		} catch (VisitException e) {
	//			Logger.error(e);
	//		}
	//		
	//		Logger.debug(this, "ending", "%s ", (product != null ? "有效":"无效!"));
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