/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.log.client.*;
import com.laxcus.command.task.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 获取保存系统组件的ACCOUNT站点调用器。<br>
 * HOME站点转发给TOP站点。
 * 
 * @author scott.liang
 * @version 1.0 10/11/2019
 * @since laxcus 1.0
 */
public class HomeTakeSystemTaskSiteInvoker extends HomeInvoker {

	/**
	 * 构造获取保存系统组件的ACCOUNT站点调用器，指定命令
	 * @param cmd 获取保存系统组件的ACCOUNT站点
	 */
	public HomeTakeSystemTaskSiteInvoker(TakeSystemTaskSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeSystemTaskSite getCommand() {
		return (TakeSystemTaskSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeSystemTaskSite cmd = getCommand();
		Node hub = getHub();
		boolean success = (hub != null);
		if (success) {
			success = launchTo(hub, cmd);
		}
		if (!success) {
			replyFault();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		TakeSystemTaskSiteProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeSystemTaskSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			replyProduct(product);
		} else {
			replyFault();
		}

		return useful(success);
	}

}
