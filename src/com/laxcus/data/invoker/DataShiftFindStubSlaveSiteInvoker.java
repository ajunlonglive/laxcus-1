/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 转发查找数据块DATA从站点调用器
 * 
 * @author scott.liang
 * @version 1.0 4/26/2018
 * @since laxcus 1.0
 */
public class DataShiftFindStubSlaveSiteInvoker extends DataInvoker {

	/** 执行步骤 **/
	private int step;

	/**
	 * 构造转发查找数据块DATA从站点调用器，指定命令
	 * @param cmd 转发查找数据块DATA从站点命令
	 */
	public DataShiftFindStubSlaveSiteInvoker(ShiftFindStubSlaveSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftFindStubSlaveSite getCommand() {
		return (ShiftFindStubSlaveSite) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftFindStubSlaveSite shift = getCommand();
		Node hub = shift.getHub();

		// 如果是CALL站点，跳到第3步，发命令给CALL站点；如果不是，从第1步开始，去HOME站点拿CALL站点数据
		if(hub.isCall()) {
			step = 3;
		} else {
			step = 1;
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
	 * 执行分布操作
	 * @return 成功返回真，否则假
	 */
	private boolean todo() {
		boolean success = false;
		switch (step) {
		case 1:
			success = sendCommandToHome();
			if (success) step = 2; // 成功跳到第2步执行
			break;
		case 2:
			success = receiveProductFromHome();
			if (success) step = 4; // 成功跳到第4步执行
			break;
		case 3:
			success = sendCommandToCall();
			if (success) step = 4; // 成功跳至第4步执行
			break;
		case 4:
			success = receiveProductFromCall();
			setQuit(true);
			break;
		}

		// 不成功，唤醒命令钩子
		if (!success) {
			ShiftFindStubSlaveSite shift = getCommand();
			shift.getHook().done();
		}
		return success;
	}

	/**
	 * 发送命令到HOME站点
	 * @return 成功返回真，否则假
	 */
	private boolean sendCommandToHome() {
		ShiftFindStubSlaveSite shift = getCommand();
		Space space = shift.getCommand().getSpace();
		FindTableCallSite sub = new FindTableCallSite(space);

		// 命令投递到HOME站点
		Node hub = getHub();
		return launchTo(hub, sub);
	}

	/**
	 * 从HOME站点拿到CALL站点地址，发送命令这些CALL站点
	 * @return 成功返回真，否则假
	 */
	private boolean receiveProductFromHome() {
		FindTableCallSiteProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(FindTableCallSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null && product.size() > 0);
		// 发送到目标站点
		if (success) {
			List<Node> hubs = product.list();
			ShiftFindStubSlaveSite shift = getCommand();
			FindStubSlaveSite cmd = shift.getCommand();
			int count = incompleteTo(hubs, cmd); // 以容错模式发送
			success = (count > 0);
		}
		return success;
	}

	/**
	 * 发送命令到CALL站点
	 * @return 成功返回真，否则假
	 */
	private boolean sendCommandToCall() {
		ShiftFindStubSlaveSite shift = getCommand();
		Node hub = shift.getHub();
		FindStubSlaveSite cmd =  shift.getCommand();
		// 发送命令，返回数据保存到内存
		return launchTo(hub, cmd);
	}

	/**
	 * 从CALL站点接收处理结果
	 * @return 成功返回真，否则假
	 */
	private boolean receiveProductFromCall() {
		FindStubSiteProduct product = new FindStubSiteProduct();
		List<Integer> keys = getEchoKeys();
		for(int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					FindStubSiteProduct sub = getObject(FindStubSiteProduct.class, index);
					product.addAll(sub);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		ShiftFindStubSlaveSite shift = getCommand();
		boolean success = (product.size() > 0);
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		Logger.debug(this, "receiveProductFromCall", success, "slave site size: %d", product.size());

		return useful(success);
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		ShiftFindStubSlaveSite shift = getCommand();
	//		Node hub = shift.getHub();
	//		
	//		// 不是CALL节点，定位到HOME节点去处理
	//		if(!hub.isCall()) {
	//			hub = getHub();
	//		}
	//		
	//		FindStubSlaveSite cmd = (FindStubSlaveSite) shift.getCommand();
	//		// 发送命令，返回数据保存到内存
	//		boolean success = launchTo(hub, cmd);
	//
	//		// 不成功，通知等待
	//		if (!success) {
	//			FindStubSiteHook hook = shift.getHook();
	//			hook.done();
	//		}
	//		
	//		Logger.debug(this, "launch", success, "send to %s", hub);
	//
	//		return success;
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	//	 */
	//	@Override
	//	public boolean ending() {
	//		FindStubSiteProduct product = null;
	//		int index = findEchoKey(0);
	//		try {
	//			if (isSuccessObjectable(index)) {
	//				product = getObject(FindStubSiteProduct.class, index);
	//			}
	//		} catch (VisitException e) {
	//			Logger.error(e);
	//		}
	//
	//		ShiftFindStubSlaveSite shift = getCommand();
	//		boolean success = (product != null);
	//		if (success) {
	//			shift.getHook().setResult(product);
	//		}
	//		shift.getHook().done();
	//
	//		Logger.debug(this, "ending", success, "result is");
	//
	//		return useful(success);
	//	}

}