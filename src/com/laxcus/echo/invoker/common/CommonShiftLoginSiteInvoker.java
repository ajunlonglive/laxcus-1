/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.login.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 客户端注册站点命令调用器。<br>
 * 
 * 这个调用器以客户机模式存在，命令投递到BANK/HOME/TOP站点，由当前运行节点发出。
 * 
 * @author scott.liang
 * @version 1.0 12/4/2017
 * @since laxcus 1.0
 */
public class CommonShiftLoginSiteInvoker extends CommonInvoker {

	/**
	 * 构造注册站点命令调用器，指定命令
	 * @param shift 转发注册站点命令
	 */
	public CommonShiftLoginSiteInvoker(ShiftLoginSite shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftLoginSite getCommand() {
		return (ShiftLoginSite) super.getCommand();
	}
	
	/**
	 * 如果注册失败，调用SiteLauncher线程注册到服务器
	 */
	private void kiss() {
		// 非立即唤醒SiteLauncher线程
		getLauncher().kiss(false);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftLoginSite shift = getCommand();
		LoginSite cmd = shift.getCommand();
		Node hub = getHub();
		
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
		
		// 调用SiteLauncher重新注册到管理节点
		if (!success) {
			kiss();
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
		
		// 不成功，调用SiteLauncher重新注册
		if (!success) {
			kiss();
		}
		
		Logger.debug(this, "ending", success, "%s", product);

		return useful(success);
	}

}