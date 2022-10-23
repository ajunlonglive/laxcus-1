/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
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
public class BankOpenUserInvoker extends BankSeekAccountSiteInvoker {

	/** 操作步骤 **/
	private int step;

	/** ACCOUNT站点地址 **/
	private Node remote;

	/**
	 * 构造删除账号调用器，指定命令
	 * @param cmd 删除账号命令
	 */
	public BankOpenUserInvoker(OpenUser cmd) {
		super(cmd);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public OpenUser getCommand() {
		return (OpenUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return todo();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return todo();
	}

	/**
	 * 删除操作
	 * @return
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = doFirst();
			break;
		case 2:
			success = doSecond();
			break;
		case 3:
			success = doThird();
			break;
		}
		// 步骤增1
		step++;

		if (!success || isQuit() || step > 3) {
			if (!success) {
				failed();
			}
			// 通知线程退出
			setQuit(true);
		}
		return success;
	}

	/**
	 * 去HASH站点查找ACCOUNT站点地址
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		OpenUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		return seekSite(siger);
	}

	/**
	 * 第二步：从HASH站点获得ACCOUNT站点，去ACCOUNT站点去申请账号实例
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		remote = replySite();
		// 判断ACCOUNT站点有效
		boolean success = (remote != null);
		if (success) {
			OpenUser cmd = getCommand();
			success = launchTo(remote, cmd);
		}
		return success;
	}

	/**
	 * 第三步：接收ACCOUNT站点反馈结果。分发给：
	 * 1. BANK站点向TOP站点发送RefreshRefer
	 * 2. BANK站点通知下属全部HASH/GATE站点，更新账号
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		OpenUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(OpenUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());
		// 此时不成功，尚可以恢复
		if (success) {
			// 通知HASH/GATE更新账号
			Seat seat = new Seat(product.getUsername(), remote);
			multicast(seat);

			// BANK向TOP/HOME集群更新资源引用
			refreshRefer(seat);

			// 反馈结果给GATE，GATE再转发给FRONT
			success = replyProduct(product);
		} else {
			OpenUser cmd = getCommand();
			// 反馈结果给GATE
			product = new OpenUserProduct(cmd.getUsername(), false);
			replyProduct(product);
		}

		return success;
	}

}