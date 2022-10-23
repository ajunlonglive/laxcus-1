/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.user.*;

/**
 * 修改账号调用器。<br>
 * 修改注册用户的账号密码。<br><br>
 * 
 * 流程：<br>
 * FRONT -> GATE -> BANK（并行）-> ACCOUNT -> BANK -> <br>
 * (1) TOP -> HOME (DATA/WORK/CALL/BUILD) <br>
 * (2) GATE -> FRONT <br>
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class GateAlterUserInvoker extends GateSeekAccountSiteInvoker {

	/**
	 * 构造修改账号调用器，指定命令
	 * @param cmd 修改账号
	 */
	public GateAlterUserInvoker(AlterUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AlterUser getCommand() {
		return (AlterUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		boolean success = canAlterUser();
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
		return reflect();
	}

	//	/** 操作步骤，从1开始 **/
	//	private int step;
	//
	//	/**
	//	 * 构造修改账号调用器，指定命令
	//	 * @param cmd 修改账号
	//	 */
	//	public GateAlterUserInvoker(AlterUser cmd) {
	//		super(cmd);
	//		step = 1;
	//	}
	//	
	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	//	 */
	//	@Override
	//	public AlterUser getCommand() {
	//		return (AlterUser) super.getCommand();
	//	}
	//	
	//	/**
	//	 * 返回账号签名
	//	 * @return 账号签名
	//	 */
	//	private Siger getSiger() {
	//		AlterUser cmd = getCommand();
	//		return cmd.getUser().getUsername();
	//	}
	//	
	//	/**
	//	 * 向FRONT反馈结果
	//	 * @param success 成功标记
	//	 * @return 成功返回真，否则假
	//	 */
	//	protected boolean reply(boolean success) {
	//		AlterUser cmd = getCommand();
	//		AlterUserProduct product = new AlterUserProduct(cmd.getUsername(), success);
	//		product.setPrimitive(cmd.getPrimitive());
	//		return replyProduct(product);
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		if (!canAlterUser()) {
	//			refuse();
	//			return false;
	//		}
	//		// 处理
	//		return todo();
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	//	 */
	//	@Override
	//	public boolean ending() {
	//		return todo();
	//	}
	//
	//	/**
	//	 * 执行处理操作
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean todo() {
	//		boolean success = false;
	//		switch (step) {
	//		case 1:
	//			success = doFirst();
	//			break;
	//		case 2:
	//			success = doSecond();
	//			break;
	//		case 3:
	//			success = doThird();
	//			break;
	//		}
	//		step++;
	//		// 判断完成
	//		if (!success || step > 3) {
	//			if (!success) {
	//				refuse();
	//			}
	//			setQuit(true);
	//		}
	//		return success;
	//	}
	//	
	//	/**
	//	 * 去HASH站点查找账号站点
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean doFirst() {
	//		Siger siger = getSiger();
	//		return seekSite(siger);
	//	}
	//	
	//	/**
	//	 * 第二步操作：把命令发给ACCOUNT站点
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean doSecond() {
	//		Node account = replySite();
	//		// 判断有效
	//		boolean success = (account != null);
	//		if (success) {
	//			AlterUser cmd = getCommand();
	//			success = launchTo(account, cmd);
	//		}
	//		return success;
	//	}
	//	
	//	/**
	//	 * 接受反馈结果
	//	 * @return
	//	 */
	//	private boolean doThird() {
	//		AlterUserProduct product = null;
	//		int index = findEchoKey(0);
	//		try {
	//			if (isSuccessObjectable(index)) {
	//				product = getObject(AlterUserProduct.class, index);
	//			}
	//		} catch (VisitException e) {
	//			Logger.error(e);
	//		}
	//
	//		boolean success = (product != null && product.isSuccessful());
	//		// 判断成功
	//		if (success) {
	//			// 投递给BANK
	//			directToBank();
	//			// 反馈给FRONT站点
	//			success = reply(success);
	//		}
	//
	//		return success;
	//	}
	//	
	//	/**
	//	 * 投递给BANK站点
	//	 */
	//	private void directToBank() {
	//		AlterUser cmd = getCommand();
	//		User user = cmd.getUser(); // 新的用户账号
	//
	//		AwardAlterUser award = new AwardAlterUser(user);
	//
	//		// 通知BANK站点，BANK转发给关联站点，某个账号的密码已经改变
	//		directToHub(award);
	//	}
}