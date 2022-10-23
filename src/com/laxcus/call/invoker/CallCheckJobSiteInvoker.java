/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.command.site.front.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 显示作业节点调用器
 * 
 * @author scott.liang
 * @version 1.0 9/21/2022
 * @since laxcus 1.0
 */
public class CallCheckJobSiteInvoker extends CallInvoker {

	/**
	 * 构造显示作业节点调用器，指定命令
	 * @param cmd 显示作业节点
	 */
	public CallCheckJobSiteInvoker(CheckJobSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckJobSite getCommand() {
		return (CheckJobSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckJobSite cmd = getCommand();
		Node hub = super.getHub();

		boolean success = false;
		if (hub != null) {
			success = super.launchTo(hub, cmd);
		}

		if (!success) {
			replyProduct(new CheckJobSiteProduct());
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CheckJobSiteProduct product = null;

		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CheckJobSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 反馈结果
		boolean success = (product != null);
		super.replyProduct(product);

		return useful(success);
	}

}