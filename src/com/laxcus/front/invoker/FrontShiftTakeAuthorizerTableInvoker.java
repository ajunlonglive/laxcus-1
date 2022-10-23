/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import com.laxcus.command.relate.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;
import com.laxcus.util.*;
import com.laxcus.site.Node;

/**
 * 申请授权人分享表调用器。<br>
 * 在获取授权人数据表之前，必须注册到授权人的GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 8/7/2018
 * @since laxcus 1.0
 */
public class FrontShiftTakeAuthorizerTableInvoker extends FrontInvoker {
	
	/**
	 * 构造申请授权人分享表调用器，指定命令
	 * @param cmd 申请授权人分享表
	 */
	public FrontShiftTakeAuthorizerTableInvoker(ShiftTakeAuthorizerTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeAuthorizerTable getCommand() {
		return (ShiftTakeAuthorizerTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeAuthorizerTable shift = getCommand();
		TakeAuthorizerTable cmd = shift.getCommand();
		Siger authorizer = cmd.getAuthorizer();
		// 根据授权人签名找到GATE站点，然后把命令发送到GATE站点
		Node hub = AuthroizerGateOnFrontPool.getInstance().findSite(authorizer);
		boolean success = (hub != null);
		if (success) {
			success = launchTo(hub, cmd);
		}
		
		// 以上不成功，通知退出
		if (!success) {
			shift.getHook().done();
		}
		
		Logger.debug(this, "launch", success, "check share table [%s] to [%s]", authorizer, hub);
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftTakeAuthorizerTable shift = getCommand();
		TakeAuthorizerTableHook hook = shift.getHook();
		
		TakeAuthorizerTableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAuthorizerTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功或者失败
		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		}
		hook.done();

		return useful(success);

//		if (product == null) {
//			Logger.error(this, "ending", "cannot find product");
//			return false;
//		}
//
//		Logger.debug(this, "ending", "[%s] share table count %d", getCommand().getAuthorizer(), product.size());
//		
//		// 保存数据表
//		for (Table table : product.list()) {
//			boolean success = getStaffPool().addPassiveTable(table);
//			Logger.debug(this, "ending", success, "save table: %s", table);
//		}
//		return useful();
	}

}