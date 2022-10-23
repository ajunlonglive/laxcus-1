/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 禁用账号调用器。<br>
 * 禁用账号只是封存账号不给用户用户，非删除账号（DROP USER）。账号的所有资源仍然存在不会改变！
 * 
 * @author scott.liang
 * @version 1.0 1/4/2020
 * @since laxcus 1.0
 */
public class GateCloseUserInvoker extends GateSeekAccountSiteInvoker {

	/**
	 * 构造禁用账号调用器，指定命令
	 * @param cmd 禁用账号命令
	 */
	public GateCloseUserInvoker(CloseUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CloseUser getCommand() {
		return (CloseUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断有管理员权限
		boolean success = canDBA();
		if (success) {
			success = transmit(); // 转发给BANK站点
		}
		if (!success) {
			refuse();// 不成功就拒绝
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 解除冗余内容
		dissolve();
		// 原样输出
		return reflect();
	}

	/**
	 * 解除冗余内容，是可能存在于内存上的记录
	 */
	private void dissolve() {
		CloseUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CloseUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());

		// 成功，删除本地上可能存在的账号
		if (success) {
			Siger siger = product.getUsername();
			
			// 禁用账号
			CallOnGatePool.getInstance().remove(siger);
			
			// 判断有授权人！
			if (ConferrerFrontOnGatePool.getInstance().hasAuthorizer(siger)) {
				ConferrerFrontOnGatePool.getInstance().removeAuthorizer(siger);
			}
			// 判断有账号
			if (FrontOnGatePool.getInstance().contains(siger)) {
				FrontOnGatePool.getInstance().drop(siger);
			}
			// 删除资源
			if (StaffOnGatePool.getInstance().contains(siger)) {
				StaffOnGatePool.getInstance().drop(siger);
			}
		}
	}
}