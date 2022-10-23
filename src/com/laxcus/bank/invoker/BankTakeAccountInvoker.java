/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.access.account.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 获取账号命令调任器<br>
 * 
 * 命令来自WATCH/TOP站点，通过HASH站点，找到ACCOUNT站点，获得账号。
 * 
 * @author scott.liang
 * @version 1.0 7/1/2018
 * @since laxcus 1.0
 */
public class BankTakeAccountInvoker extends BankSeekAccountSiteInvoker {

	/** 操作步骤，从1开始 **/
	private int step;

	/**
	 * 构造获取账号命令调任器，指定命令
	 * @param cmd 获取账号命令
	 */
	public BankTakeAccountInvoker(TakeAccount cmd) {
		super(cmd);
		step = 1;
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
	 * 拒绝操作
	 * @return 发送成功返回真，否则假
	 */
	private boolean notfound() {
		return replyFault(Major.FAULTED, Minor.NOTFOUND);
	}

	/**
	 * 执行操作
	 * @return 成功返回真，否则假
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
		// 增1
		step++;
		// 符合退出要求
		if (!success || step > 3) {
			if (!success) {
				notfound();
			}
			// 通知线程退出
			setQuit(true);
		}
		return success;
	}

	/**
	 * 第一段操作：找到HASH站点
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		TakeAccount cmd = getCommand();
		Siger siger = cmd.getSiger();
		// 投递给HASH站点
		return seekSite(siger);

		//		// 根据用户签名，找到对应的HASH站点
		//		Node remote = HashOnBankPool.getInstance().locate(siger);
		//		// 发送给HASH站点
		//		boolean success = (remote != null);
		//		if (success) {
		//			TakeAccountSite sub = new TakeAccountSite(siger);
		//			success = launchTo(remote, sub);
		//		}
		//		return success;
	}

	/**
	 * 接收HASH站点返回的ACCOUNT站点地址，发送命令给ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		// 从HASH站点拿到ACCOUNT站点地址，发送命令给ACCOUNT站点
		Node account = replySite();
		boolean success = (account != null);
		if (success) {
			TakeAccount cmd = getCommand();
			success = launchTo(account, cmd);
		}
		return success;
	}

	/**
	 * 第三步：接收ACCOUNT反馈结果，返回给来源站点
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		// 判断处理结果
		TakeAccountProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAccountProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null && product.getAccount() != null);
		if (success) {
			success = replyProduct(product);
		}
		return success;
	}
}