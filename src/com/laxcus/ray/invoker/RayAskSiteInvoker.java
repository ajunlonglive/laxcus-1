/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 询问分布站点命令调用器。<br>
 * WATCH站点向TOP/HOME/BANK站点询问当前的分布站点地址。
 * 
 * @author scott.liang
 * @version 1.0 3/21/2013
 * @since laxcus 1.0
 */
public class RayAskSiteInvoker extends RayCastElementInvoker {

	/**
	 * 构造询问分布站点命令调用器，指定命令
	 * @param cmd 询问分布站点命令
	 */
	public RayAskSiteInvoker(AskSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AskSite getCommand() {
		return (AskSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果没有注册
		if (isLogout()) {
			faultX(FaultTip.SITE_NOT_LOGING);
			return false;
		}

		Node hub = getHub();
		// 判断服务器地址有效
		AskSite cmd = getCommand();
		// 不使用fireTo，采用completeTo方法发送到TOP/HOME/BANK站点
		boolean success = completeTo(hub, cmd);
		// 如果成功则忽略显示，不成功才显示错误信息
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		AskSiteProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(AskSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		boolean success = (product != null);
		if (success) {
			for (Node node : product.list()) {
				// 保存一个节点
				boolean push = pushSite(node);
				if (push) {
					messageX(MessageTip.PUSH_NODE_X, node); // 显示成功
				}
			}
		}

		return useful(success);
	}

}