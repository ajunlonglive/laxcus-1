/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.command.access.user.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 刷新注册用户调用器。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2018
 * @since laxcus 1.0
 */
public class GateRefreshUserInvoker extends GateInvoker {

	/**
	 * 构造刷新注册用户调用器，指定命令
	 * @param cmd 刷新注册用户命令
	 */
	public GateRefreshUserInvoker(RefreshUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshUser getCommand() {
		return (RefreshUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshUser cmd = getCommand();
		Node local = getLocal();

		RefreshUserProduct product = new RefreshUserProduct();

		for (Siger siger : cmd.getUsers()) {
			// 账号不存在，忽略它
			boolean success = StaffOnGatePool.getInstance().contains(siger);
			if (!success) {
				product.add(local, siger, false);
				continue;
			}

			// 重新加载账号
			boolean b1 = StaffOnGatePool.getInstance().reloadAccount(siger);
			// 重新获取CALL站点
			boolean b2 = CallOnGatePool.getInstance().loadCallSites(siger);
			success = (b1 && b2);
			// 保存
			product.add(local, siger, success);
		}

		Logger.debug(this, "launch", "count is %d", product.size());

		// 反馈处理结果给BANK站点
		if (cmd.isReply()) {
			replyProduct(product);
		}

		// 退出
		return useful();
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
