/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.command.access.account.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 获取账号命令调任器<br><br>
 * 
 * 命令来自FRONT站点，在本地检查获得它。<br><br>
 * 
 * 1. 如果是查找自己的账号，本地返回。<br>
 * 2. 如果是管理员身份或者等同管理员身份，去ACCOUNT节点查询。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/1/2018
 * @since laxcus 1.0
 */
public class GateTakeAccountInvoker extends GateInvoker {

	/**
	 * 构造获取账号命令调任器，指定命令
	 * @param cmd 获取账号命令
	 */
	public GateTakeAccountInvoker(TakeAccount cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeAccount getCommand() {
		return (TakeAccount) super.getCommand();
	}
	
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeAccount cmd = getCommand();
		Siger siger = cmd.getSiger();
		Siger issuer = cmd.getIssuer(); // 发布者自己
		
		// 是用户自己，返回
		boolean success = (Laxkit.compareTo(issuer, siger) == 0);
		if (success) {
			TakeAccountProduct product = new TakeAccountProduct();
			// 获这个注册用户的账号数据副本
			Account account = findAccount(true);
			success = (account != null && Laxkit.compareTo(siger, account.getUsername()) == 0);
			// 判断成功
			if (success) {
				product.setAccount(account);
				success = replyProduct(product);
			} else {
				refuse();
			}
			Logger.debug(this, "launch", success, "%s reply to %s", siger, getCommandSite());

			return useful(success);
		}
		
		// 管理员或者等同管理员身份
		success = (isAdministrator() || isSameAdministrator());
		if (!success) {
			refuse();
			return false;
		}
		
		// 获得账号
		Account account = StaffOnGatePool.getInstance().seekAccount(siger);
		TakeAccountProduct product = new TakeAccountProduct();
		success = (account != null && Laxkit.compareTo(siger, account.getUsername()) == 0);
		// 判断成功
		if (success) {
			product.setAccount(account);
			success = replyProduct(product);
		} else {
			refuse();
		}

		Logger.debug(this, "launch", success, "%s reply to %s", siger, getCommandSite());

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		// 如果是管理员，不能通过GATE取账号，必须通过WATCH站点
//		if (isAdministrator()) {
//			refuse();
//			return false;
//		}
//
//		TakeAccount cmd = getCommand();
//		Siger siger = cmd.getSiger();
//
//		TakeAccountProduct product = new TakeAccountProduct();
//
//		// 获这个注册用户的账号数据副本
//		Account account = findAccount(true);
//		boolean success = (account != null && 
//				Laxkit.compareTo(siger, account.getUsername()) == 0);
//		// 判断成功
//		if(success) {
//			product.setAccount(account);
//			success = replyProduct(product);
//		} else {
//			refuse();
//		}
//
//		Logger.debug(this, "launch", success, "%s reply to %s", siger, getCommandSite());
//
//		return useful(success);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
//	 */
//	@Override
//	public boolean ending() {
//		return false;
//	}

}