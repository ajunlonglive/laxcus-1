/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.bank.pool.*;
import com.laxcus.command.access.account.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;

/**
 * 建立数据库转发调用器。
 * 这是串行处理命令。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class BankSerialShiftCreateSchemaInvoker extends BankSeekAccountSiteInvoker {

	/** 操作步骤，从1开始 **/
	private int step;

	/** ACCOUNT站点地址 **/
	private Node remote;

	/**
	 * 构造建立数据库转发命令，指定转发命令
	 * @param shift 建立数据库转发命令
	 */
	public BankSerialShiftCreateSchemaInvoker(ShiftCreateSchema shift) {
		super(shift);
		step = 1;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftCreateSchema getCommand() {
		return (ShiftCreateSchema) super.getCommand();
	}
	
	/**
	 * 返回数据库名
	 * @return 数据库名
	 */
	private Fame getFame() {
		// 如果数据库存储于故障库中，拒绝操作！
		ShiftCreateSchema shift = getCommand();
		CreateSchema cmd = shift.getCommand();
		return cmd.getSchema().getFame();
	}
	
	/**
	 * 判断数据库存在故障池中
	 * @return 返回真或者假
	 */
	private boolean isFaultSchema() {
		// 如果数据库存储于故障库中，拒绝操作！
		Fame fame = getFame();
		return FaultOnBankPool.getInstance().hasSchema(fame);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 1. 判断存在于故障池中
		boolean success = isFaultSchema();
		// 以上条件成立，反馈拒绝操作
		if (success) {
			refuse();
			return false;
		}

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
	 * 通知GATE站点，增加了一个数据库名
	 */
	private void multicast() {
		// 取出全部GATE站点
		ArrayList<Node> slaves = new ArrayList<Node>();
		slaves.addAll(GateOnBankPool.getInstance().detail());

		// 通知全部GATE站点，理新账号记录。为了简化处理，增加冗余操作
		Siger siger = getIssuer();
		RefreshAccount cmd = new RefreshAccount(remote, siger);
		directTo(slaves, cmd);
	}

	/**
	 * 操作步骤
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
		case 4:
			success = doFourth();
			break;
		}
		// 步骤增1
		step++;
		// 以上不成功或者完成时，退出
		if (!success || step > 4) {
			// 如果不成功，通知GATE站点
			if (success) {
				multicast();
			} else {
				refuse();
			}
			// 通知线程退出
			setQuit(true);
			// 通知SerialCommandPool释放等待
			getCommand().getHook().done();
		}
		// 返回结果
		return success;
	}
	
	/**
	 * 第一步：向全部ACCOUNT查询数据库存在
	 * @return 发送成功返回真，否则假
	 */
	private boolean doFirst() {
		Fame fame = getFame();
		AssertSchema cmd = new AssertSchema(fame);

		// 投递到全部ACCOUNT站点
		List<Node> slaves = AccountOnBankPool.getInstance().detail();
		return launchTo(slaves, cmd);
	}

	/**
	 * 第二步：从HASH站点中查出账号所在节点，并且发送给这个HASH节点
	 * @return 成功返回真，否则假
	 */
	private boolean doSecond() {
		int count = 0;
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					AssertSchemaProduct e = getObject(AssertSchemaProduct.class, index);
					if (e.isSuccessful()) {
						count++;
					}
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 判断找到，不能执行
		boolean success = (count > 0);
		if (success) {
			return false;
		}
		
		// 通过HASH站点检查匹配的ACCOUNT站点
		Siger siger = getIssuer();
		return seekSite(siger);
	}

	/**
	 * 第三步：接收HASH站点返回的ACCOUNT站点地址，发送命令给ACCOUNT站点
	 * @return 成功返回真，否则假
	 */
	private boolean doThird() {
		remote = replySite();
		boolean success = (remote != null);
		if (success) {
			ShiftCreateSchema shift = getCommand();
			CreateSchema cmd = shift.getCommand();
			success = launchTo(remote, cmd);
		}
		return success;
	}

	/**
	 * 第四步：接收ACCOUNT站点反馈结果，并把结果反馈给GATE站点
	 * @return 成功返回真，否则假
	 */
	private boolean doFourth() {
		// 判断处理结果
		CreateSchemaProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateSchemaProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null && product.isSuccessful());
		// 把结果发送给GATE站点
		if (success) {
			success = replyProduct(product);
		}
		return success;
	}

}