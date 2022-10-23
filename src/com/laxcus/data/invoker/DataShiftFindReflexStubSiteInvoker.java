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
import com.laxcus.command.stub.reflex.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 查询映像数据块站点命令调用器
 * 
 * @author scott.liang
 * @version 1.0 11/2/2013
 * @since laxcus 1.0
 */
public class DataShiftFindReflexStubSiteInvoker extends DataInvoker {

	/** 操作步骤 **/
	private int step;

	/**
	 * 构造查询映像数据块站点命令调用器，指定命令
	 * @param shift 转发查询映像数据块站点命令
	 */
	public DataShiftFindReflexStubSiteInvoker(ShiftFindReflexStubSite shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftFindReflexStubSite getCommand() {
		return (ShiftFindReflexStubSite) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftFindReflexStubSite shift = getCommand();
		Node hub = shift.getHub();

		// 如果是CALL节点，从第3步开始，去CALL站点查数据；如果不是，从第1步开始，先查CALL站点数据表
		if (hub.isCall()) {
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
			ShiftFindReflexStubSite shift = getCommand();
			shift.getHook().done();
		}
		return success;
	}

	/**
	 * 发送命令到HOME站点
	 * @return 成功返回真，否则假
	 */
	private boolean sendCommandToHome() {
		ShiftFindReflexStubSite shift = getCommand();
		Space space = shift.getCommand().getSpace();
		FindTableCallSite cmd = new FindTableCallSite(space);

		// 命令投递到HOME站点
		Node hub = getHub();
		return launchTo(hub, cmd);
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
			ShiftFindReflexStubSite shift = getCommand();
			FindReflexStubSite cmd = shift.getCommand();
			int count = incompleteTo(hubs, cmd); // 以容错模式发送
			success = (count > 0); // 判断成功
		}
		return success;
	}

	/**
	 * 发送命令到CALL站点
	 * @return 成功返回真，否则假
	 */
	private boolean sendCommandToCall() {
		ShiftFindReflexStubSite shift = getCommand();
		FindReflexStubSite cmd = shift.getCommand();
		Node hub = shift.getHub();
		return launchTo(hub, cmd);
	}

	/**
	 * 从CALL站点接收处理结果
	 * @return 成功返回真，否则假
	 */
	private boolean receiveProductFromCall() {
		ReflexStubSiteProduct product = new ReflexStubSiteProduct();
		List<Integer> keys = getEchoKeys();
		int errors = 0;
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					ReflexStubSiteProduct sub = getObject(ReflexStubSiteProduct.class, index);
					product.addAll(sub);
				}
			} catch (VisitException e) {
				Logger.error(e);
				errors++;
			}
		}

		ShiftFindReflexStubSite shift = getCommand();
		boolean success = (product.size() > 0);
		// 当成功，或者没有错误时，保存它
		if (success || errors == 0) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		Logger.debug(this, "receiveProductFromCall", success, "reflex site size:%d", product.size());

		return useful(success);
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		ShiftFindReflexStubSite shift = getCommand();
	//		FindReflexStubSite cmd = shift.getCommand();
	//		Node hub = shift.getHub();
	//		
	//		// 如果目标不是CALL节点，转成HOME节点地址，由HOME站点代为处理
	//		if (!hub.isCall()) {
	//			hub = getHub(); // 转为HOME站点
	//		}
	//
	//		// 发送命令到CALL/HOME站点
	//		boolean success = launchTo(hub, cmd);
	//		// 不成功，唤醒命令钩子
	//		if (!success) {
	//			shift.getHook().done();
	//		}
	//
	//		Logger.debug(this, "launch", success, "send %s to %s", cmd.getFlag(), hub);
	//		return success;
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	//	 */
	//	@Override
	//	public boolean ending() {
	//		ReflexStubSiteProduct product = null;
	//		int index = findEchoKey(0);
	//		try {
	//			if (isSuccessObjectable(index)) {
	//				product = getObject(ReflexStubSiteProduct.class, index);
	//			}
	//		} catch (VisitException e) {
	//			Logger.error(e);
	//		}
	//
	//		ShiftFindReflexStubSite shift = getCommand();
	//		boolean success = (product != null);
	//		if (success) {
	//			shift.getHook().setResult(product);
	//		}
	//		shift.getHook().done();
	//
	//		Logger.debug(this, "ending", success, "slave site sizes %d", (success ? product.size() : -1));
	//
	//		return useful(success);
	//	}

}