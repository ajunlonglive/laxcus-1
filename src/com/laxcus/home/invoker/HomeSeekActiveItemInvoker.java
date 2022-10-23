/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.cross.*;
import com.laxcus.home.pool.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 查询授权单元调用器。
 * 
 * @author scott.liang
 * @version 1.0 8/15/2017
 * @since laxcus 1.0
 */
public class HomeSeekActiveItemInvoker extends HomeInvoker {

	/**
	 * 查询授权单元调用器，指定命令
	 * @param cmd - 查询授权单元命令
	 */
	public HomeSeekActiveItemInvoker(SeekActiveItem cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekActiveItem getCommand() {
		return (SeekActiveItem) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekActiveItem cmd = getCommand();
		Siger authorizer = cmd.getAuthorizer();

		// 查询某个账号下的授权单元
		List<ActiveItem> items = StaffOnHomePool.getInstance().findActiveItems(
				authorizer);

		SeekActiveItemProduct product = new SeekActiveItemProduct();
		if (items != null) {
			product.addAll(items);
		}

		// 反馈结果
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "seek:%s, active item size:%d",
				authorizer, product.size());

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
