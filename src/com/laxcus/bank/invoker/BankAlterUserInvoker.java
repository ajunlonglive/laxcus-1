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
 * 修改账号调用器。<br>
 * 修改注册用户的账号密码。
 * 
 * @author scott.liang
 * @version 1.0 2/12/2012
 * @since laxcus 1.0
 */
public class BankAlterUserInvoker extends BankSeekAccountSiteInvoker {

	/** 操作步骤，从1开始 **/
	private int step;

	/** ACCOUNT站点地址 **/
	private Node accountSite;
	
	/**
	 * 构造修改账号调用器，指定命令
	 * @param cmd 修改账号
	 */
	public BankAlterUserInvoker(AlterUser cmd) {
		super(cmd);
		step = 1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AlterUser getCommand() {
		return (AlterUser) super.getCommand();
	}
	
	/**
	 * 返回账号签名
	 * @return 账号签名
	 */
	private Siger getSiger() {
		AlterUser cmd = getCommand();
		return cmd.getUser().getUsername();
	}
	
	/**
	 * 向FRONT反馈结果
	 * @param success 成功标记
	 * @return 成功返回真，否则假
	 */
	protected boolean reply(boolean success) {
		AlterUser cmd = getCommand();
		AlterUserProduct product = new AlterUserProduct(cmd.getUsername(), success);
		product.setPrimitive(cmd.getPrimitive());
		return replyProduct(product);
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
	 * 执行处理操作
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
		step++;
		// 判断完成
		if (!success || step > 3) {
			if (!success) {
				refuse();
			}
			setQuit(true);
		}
		return success;
	}
	
	/**
	 * 去HASH站点查找账号站点
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		Siger siger = getSiger();
		return seekSite(siger);
	}
	
	/**
	 * 第二步操作：把命令发给ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		accountSite = replySite();
		// 判断有效
		boolean success = (accountSite != null);
		if (success) {
			AlterUser cmd = getCommand();
			success = launchTo(accountSite, cmd);
		}
		return success;
	}
	
	/**
	 * 接受反馈结果
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		AlterUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(AlterUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());
		// 判断成功
		if (success) {
			// 投递给TOP
			directToHub();
			// 广播给全部HASH/GATE站点
			multicast(accountSite, product.getUsername());
			// 反馈给GATE站点
			success = reply(success);
		}

		return success;
	}
	
	/**
	 * 投递给TOP站点
	 */
	private void directToHub() {
		AlterUser cmd = getCommand();
		
//		User user = cmd.getUser(); // 新的用户账号
//
//		AwardAlterUser award = new AwardAlterUser(user);

		// 通知TOP站点，TOP转发给关联站点，某个账号的密码已经改变
		directToHub(cmd);
	}
}