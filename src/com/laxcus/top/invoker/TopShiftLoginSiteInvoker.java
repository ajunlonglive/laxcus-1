/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.login.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 *
 * @author scott.liang
 * @version 1.0 9/25/2022
 * @since laxcus 1.0
 */
public class TopShiftLoginSiteInvoker extends TopInvoker {

	/**
	 * @param cmd
	 */
	public TopShiftLoginSiteInvoker(ShiftLoginSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftLoginSite getCommand() {
		return (ShiftLoginSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftLoginSite shift = getCommand();
		LoginSite cmd = shift.getCommand();

		// 找到管理员节点
		Node hub = getLauncher().getManager();

		// 判断HUB地址有效
		boolean success = (hub != null);
		// 如果直接投递，发送后退出。
		if (success) {
			if (cmd.isDirect()) {
				success = directTo(hub, cmd);
				setQuit(true);
			} else {
				success = launchTo(hub, cmd);
			}
			Logger.debug(this, "launch", success, "submit to %s", hub);
		}

		// 如果不成功，或者退出时，唤醒钩子
		if (!success || isQuit()) {
			shift.getHook().done();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		LoginSiteProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(LoginSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);
		ShiftLoginSite shift = getCommand();
		LoginSiteHook hook = shift.getHook();
		// 如果成功，设置返回结果
		if (success) {
			hook.setResult(product);
			// 判断成功或者失败
			success = product.isSuccessful();
		}
		// 唤醒
		hook.done();
		
		Logger.debug(this, "ending", success, "%s", product);

		return useful(success);
	}

}
