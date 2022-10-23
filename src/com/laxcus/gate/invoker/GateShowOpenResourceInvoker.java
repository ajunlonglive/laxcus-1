/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.cross.*;
import com.laxcus.gate.pool.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 显示授权资源调用器。
 * 
 * 授权人操作。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class GateShowOpenResourceInvoker extends GateInvoker {

	/**
	 * 构造显示授权资源调用器，制定命令
	 * @param cmd 显示授权资源
	 */
	public GateShowOpenResourceInvoker(ShowOpenResource cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowOpenResource getCommand() {
		return (ShowOpenResource) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShowOpenResource cmd = getCommand();
		Siger authorizer = cmd.getIssuer();

		// 找到授权人的账号副本
		Account account = StaffOnGatePool.getInstance().findAccount(authorizer, true);
		if (account == null) {
			refuse();
			return false;
		}

		ArrayList<Siger> array = new ArrayList<Siger>();
		// 如果是全部，找到这个账号下的全部资源；否则只取指定的账号
		if (cmd.isAll()) {
			List<Siger> sigers = account.getActiveConferrers();
			if (sigers != null) array.addAll(sigers);
		} else {
			array.addAll(cmd.getUsers()); // 被授权人
		}

		// 保存结果
		ShareCrossProduct product = new ShareCrossProduct();
		// 根据被授权人，逐一查找被分享资源
		for (Siger conferrer : array) {
			List<ActiveItem> items = account.findActiveItems(conferrer);
			
			Logger.debug(this, "launch", "%s size %d", conferrer, items.size());
			
			// 保存它
			for (ActiveItem e : items) {
				product.add(e.getConferrer(), e.getFlag());
			}
		}

		// 反馈处理结果
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "share table size:%d", product.size());

		return useful(success);
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