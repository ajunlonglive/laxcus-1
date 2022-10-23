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
 * 显示被授权资源调用器。
 * 
 * 被授权人操作。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class GateShowPassiveResourceInvoker extends GateInvoker {

	/**
	 * 构造显示被授权资源调用器，制定命令
	 * @param cmd 显示被授权资源
	 */
	public GateShowPassiveResourceInvoker(ShowPassiveResource cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowPassiveResource getCommand() {
		return (ShowPassiveResource) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShowPassiveResource cmd = getCommand();
		Siger conferrer = cmd.getIssuer(); // 操作者是被授权人

		// 找到授权人的账号副本
		Account account = StaffOnGatePool.getInstance().findAccount(conferrer, true);
		if(account == null){
			Logger.error(this, "launch", "cannot be find %s", conferrer);
			refuse();
			return false;
		}

		ArrayList<Siger> array = new ArrayList<Siger>();
		// 如果是全部，找到这个账号下的被授权资源；否则只取指定的账号
		if (cmd.isAll()) {
			List<Siger> sigers = account.getPassiveAuthorizers();
			if (sigers != null) array.addAll(sigers);
		} else {
			array.addAll(cmd.getUsers()); // 指定的授权人
		}
		
		Logger.debug(this, "launch", "all:%s size %d", cmd.isAll(), array.size());

		// 根据授权人，逐一查找被分享资源
		ShareCrossProduct product = new ShareCrossProduct();
		for (Siger authorizer : array) {
			List<PassiveItem> items = account.findPassiveItems(authorizer);
			
			Logger.debug(this, "launch", "%s size %d", authorizer, items.size());
			
			for (PassiveItem e : items) {
				product.add(e.getAuthorizer(), e.getFlag());
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