/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 设置DSM表压缩倍数异步调用器。<br><br>
 * 
 * <B> 这个命令供管理员或者有管理员权限的注册用户使用。它将修改ACCOUNT节点上的表配置参数，同时通知全部的DATA站点。</B>
 * 
 * @author scott.liang
 * @version 1.0 6/2/2019
 * @since laxcus 1.0
 */
public class GateSetMaxDSMReduceInvoker extends GateSeekAccountSiteInvoker {
	
	/** 执行步骤 **/
	private int step;
	
	/**
	 * 构造设置DSM表压缩倍数异步调用器，指定命令
	 * @param cmd 设置DSM表压缩倍数
	 */
	public GateSetMaxDSMReduceInvoker(SetMaxDSMReduce cmd) {
		super(cmd);
		step = 1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetMaxDSMReduce getCommand(){
		return (SetMaxDSMReduce)super.getCommand();
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
	 * 反馈结果
	 * @param success 成功标记
	 * @return 发送成功返回真，否则假
	 */
	private boolean reply(boolean success) {
		SetMaxDSMReduceProduct e = new SetMaxDSMReduceProduct(success);
		return replyProduct(e);
	}

	/**
	 * 执行操作
	 * @return 成功返回真，否则假
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = doFirst(); // 申请ACCOUNT站点地址
			break;
		case 2:
			success = doSecond(); // 取得ACCOUNT站点地址，发送命令
			break;
		case 3:
			success = doThird(); // 取得ACCOUNT结果，如果成功，转发给BANK，请求转发给TOP
			break;
		case 4:
			success = doFourth(); // 第四步，得取BANK反馈结果
			break;
		}
		step++;

		if (!success || isQuit() || step > 4) {
			if (!success) {
				reply(false);
			}
			setQuit(true);
		}

		return success;
	}
	
	/**
	 * 去HASH站点申请关联ACCOUNT站点地址
	 * @return 成功返回真，否则假
	 */
	private boolean doFirst() {
		SetMaxDSMReduce cmd = getCommand();
		Siger siger = cmd.getSiger();
		return seekSite(siger);
	}

	/**
	 * 第二步，接受HASH反馈，发送命令给ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		Node account = replySite();
		boolean success = (account != null);
		// 生成命令副本
		SetMaxDSMReduce sub = getCommand().duplicate();
		if (success) {
			success = launchTo(account, sub);
		}
		return success;
	}

	/**
	 * 第三步，接收ACCOUNT反馈结果，判断下一步操作
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		int index = findEchoKey(0);
		SetMaxDSMReduceProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SetMaxDSMReduceProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null && product.isSuccessful());

		// 如果成功，发命令给BANK站点，经它转发给TOP/HOME，作用到全部DATA站点
		if (success) {
			SetMaxDSMReduce cmd = getCommand();
			// 成功，发命令给BANK站点
			SetDSMReduce sub = new SetDSMReduce(cmd.getSpace(), cmd.getMultiple());
			success = launchToHub(sub);
		}
		return success;
	}
	
	/**
	 * 第四步，收拾反馈结果
	 * @return 成功返回真，否则假
	 */
	private boolean doFourth() {
		int index = findEchoKey(0);
		SetDSMReduceProduct sub = null;
		try {
			if (isSuccessObjectable(index)) {
				sub = getObject(SetDSMReduceProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		SetMaxDSMReduceProduct product = new SetMaxDSMReduceProduct(true);
		if (sub != null) {
			product.addAll(sub.list());
		}

		// 反馈结果
		replyProduct(product);

		return true;
	}
}
