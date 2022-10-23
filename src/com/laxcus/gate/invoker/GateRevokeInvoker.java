/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.permit.*;

/**
 * 解除授权调用器。<br>
 * 只有管理员或者拥有解除授权操作的用户才能执行这个操作。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class GateRevokeInvoker extends GateSeekAccountSiteInvoker {

	/**
	 * 构造解除授权操作调用器，指定命令
	 * 
	 * @param cmd 解除授权操作命令
	 */
	public GateRevokeInvoker(Revoke cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Revoke getCommand() {
		return (Revoke) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断有操作权
		boolean success = canRevoke();
		if (success) {
			success = transmit(); // 转发给BANK站点
		}
		// 反馈拒绝
		if (!success) {
			refuse();
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

}