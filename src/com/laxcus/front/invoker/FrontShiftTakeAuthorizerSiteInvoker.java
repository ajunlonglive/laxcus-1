/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import com.laxcus.command.site.entrance.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 转发获得授权人账号注册地址调用器
 * 
 * @author scott.liang
 * @version 1.0 5/31/2019
 * @since laxcus 1.0
 */
public class FrontShiftTakeAuthorizerSiteInvoker extends FrontInvoker {

	/**
	 * 构造转发获得授权人账号注册地址，指定命令
	 * @param shift
	 */
	public FrontShiftTakeAuthorizerSiteInvoker(ShiftTakeAuthorizerSite shift) {
		super(shift);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeAuthorizerSite getCommand() {
		return (ShiftTakeAuthorizerSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeAuthorizerSite shift = getCommand();
		TakeAuthorizerSite cmd = shift.getCommand();

		// 取得初始登录站点（ENTRANCE站点）
		Node hub = getLauncher().getInitHub();
		// 发送给ENTRANCE站点
		boolean success = launchTo(hub, cmd);
		// 不成功退出！
		if (!success) {
			shift.getHook().done();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		TakeAuthorizerSiteProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAuthorizerSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);

		// 判断成功，保存结果
		ShiftTakeAuthorizerSite shift = getCommand();
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		return useful(success);
	}

}