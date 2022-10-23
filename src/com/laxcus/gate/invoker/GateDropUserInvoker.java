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
 * 删除账号调用器。<br>
 * 删除账号，以及账号下的全部表和数据库记录。这个命令只能由管理员来操作。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class GateDropUserInvoker extends GateSeekAccountSiteInvoker {

	/**
	 * 构造删除账号调用器，指定命令
	 * @param cmd 删除账号命令
	 */
	public GateDropUserInvoker(DropUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropUser getCommand() {
		return (DropUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断有删除权限
		boolean success = canDropUser();
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
		DropUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());

		// 成功，删除本地上可能存在的账号
		if (success) {
			Siger siger = product.getUsername();
			
			// 删除账号
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