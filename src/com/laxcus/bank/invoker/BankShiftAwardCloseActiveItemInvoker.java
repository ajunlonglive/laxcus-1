/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 转发强制关闭授权单元命令
 * 
 * @author scott.liang
 * @version 1.0 5/27/2019
 * @since laxcus 1.0
 */
public class BankShiftAwardCloseActiveItemInvoker extends BankSeekAccountSiteInvoker {

	/** 操作步骤 **/
	private int step;

	/** ACCOUNT站点地址 **/
	private Node remote;

	/**
	 * 构造转发强制关闭授权单元命令，指定命令
	 * @param shift 转发强制关闭授权单元命令
	 */
	public BankShiftAwardCloseActiveItemInvoker(ShiftAwardCloseActiveItem shift) {
		super(shift);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftAwardCloseActiveItem getCommand() {
		return (ShiftAwardCloseActiveItem) super.getCommand();
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
		// 步骤增1
		step++;

		// 不成功，或者结束时...
		if (!success || step > 3) {
			// 不成功，唤醒它！
			if (!success) {
				ShiftAwardCloseActiveItem shift = getCommand();
				shift.getHook().done();
			}
			// 通知线程退出
			setQuit(true);
		}
		return success;
	}

	/**
	 * 第一步：通过HASH站点，找被授权人的ACCOUNT地址
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		ShiftAwardCloseActiveItem shift = getCommand();
		AwardCloseActiveItem cmd = shift.getCommand();
		// 找到被授权人地址
		Siger conferrer = cmd.getField().getConferrer();
		// 发送指令
		return seekSite(conferrer);
	}

	/**
	 * 第二步：拿到ACCOUN站点，把命令发给这个地址
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		remote = replySite();
		boolean success = (remote != null);
		// 拿到账号，检查这个表有没有被授权人
		if (success) {
			ShiftAwardCloseActiveItem shift = getCommand();
			// 保存ACCOUT站点
			shift.getHook().setAccountSite(remote);
			// 发送命令给ACCOUNT站点
			AwardCloseActiveItem cmd = shift.getCommand();
			success = launchTo(remote, cmd);
		}
		return success;
	}
	
	/**
	 * 第三步：取来自ACCOUN站点的反馈结果
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		ShareCrossProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ShareCrossProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		boolean success = (product != null && product.size() > 0);
		if (success) {
			ShiftAwardCloseActiveItem shift = getCommand();
			shift.getHook().setResult(product);
		}

		return success;
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
//	 */
//	@Override
//	public boolean launch() {
//		ShiftAwardCloseActiveItem shift = getCommand();
//		AwardCloseActiveItem cmd = shift.getCommand();
//
//		// 找到全部账号节点
//		List<Node> slaves = AccountOnBankPool.getInstance().detail();
//		// 以容错模式发送
//		int count = incompleteTo(slaves, cmd);
//		// 判断成功！
//		boolean success = (count > 0);
//
//		// 失败，通知命令钩子
//		if (!success) {
//			shift.getHook().done();
//		}
//		return success;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
//	 */
//	@Override
//	public boolean ending() {
//		ShareCrossProduct product = new ShareCrossProduct();
//		List<Integer> keys = getEchoKeys();
//		
////		ShareCrossProduct
//		
//		for (int index : keys) { 
//			try {
//				if (isSuccessObjectable(index)) {
//					ShareCrossProduct e = getObject(ShareCrossProduct.class,
//							index);
//					product.addAll(e);
//				}
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//
//		boolean success = (product.size() > 0);
//
//		ShiftAwardCloseActiveItem shift = getCommand();
//		if (success) {
//			shift.getHook().setResult(product);
//		}
//		shift.getHook().done();
//
//		return useful(success);
//	}

}
