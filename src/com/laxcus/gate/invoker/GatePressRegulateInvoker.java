/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.gate.*;
import com.laxcus.gate.pool.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.command.rule.*;
import com.laxcus.echo.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.gate.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 启动数据优化命令调用器。<br><br>
 * 
 * 处理流程：<br>
 * 1. 在本地，向GATE站点提交锁定资源申请。<br>
 * 2. 判断锁定资源结果。<br>
 * 3. 向CALL站点提交执行数据优化命令。<br>
 * 4. 判断CALL站点返回的数据优化操作，解除锁定。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 12/12/2016
 * @since laxcus 1.0
 */
public class GatePressRegulateInvoker extends GateInvoker {

	/** 处理GATE反馈的结果 **/
	private final static int FROM_GATE = 1;

	/** 处理从CALL站点反馈的结果 **/
	private final static int FROM_CALL = 2;

	/** 处理步骤 **/
	private int step;

	/** 锁定规则 **/
	private AttachRule attach;

	/**
	 * 构造启动数据优化命令调用器，指定命令
	 * @param cmd 启动数据优化命令
	 */
	public GatePressRegulateInvoker(PressRegulate cmd) {
		super(cmd);
		// 初始化步骤
		step = GatePressRegulateInvoker.FROM_GATE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PressRegulate getCommand() {
		return (PressRegulate) super.getCommand();
	}

	/**
	 * 取得GATE站点内网地址
	 * @return 内网地址
	 */
	private Node getPrivate() {
		GateSite site = (GateSite) GateLauncher.getInstance().getSite();
		return site.getPrivate();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 取得GATE站点内网地址
		Node local = getPrivate();
		// 取结果
		PressRegulate cmd = getCommand();

		// 生成事务规则，采用互斥锁定
		TableRuleItem item = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE, cmd.getSpace());
		RuleSheet sheet = new RuleSheet(item);

		// 生成事务规则命令
		ProcessRuleTag tag = new ProcessRuleTag(local, getInvokerId(), 0);
		attach = new AttachRule(tag, sheet);
		attach.setIssuer(cmd.getIssuer());

		// 投递给GATE自己的管理池
		boolean success = launchTo(local, attach);

		Logger.debug(this, "launch", success, "launch to %s", local);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		boolean success = false;
		// 处理结果
		switch (step) {
		case GatePressRegulateInvoker.FROM_GATE:
			success = doFromGate();
			break;
		case GatePressRegulateInvoker.FROM_CALL:
			success = doFromCall();
			break;
		}

		if (step == GatePressRegulateInvoker.FROM_CALL) {
			setQuit(true); // 设置为退出
		} else if (success) {
			step++; // 如果成功，继续下一步
		}
		// 输出处理结果
		return success;
	}

	/**
	 * 解除事务锁定
	 * @return 发送成功返回真，否则假
	 */
	private boolean detach() {
		PressRegulate cmd = getCommand();
		// 生成反向命令
		DetachRule detach = attach.reverse();
		detach.setIssuer(cmd.getIssuer());
		detach.setDirect(true);
		detach.setQuick(true);
		return getCommandPool().admit(detach);
	}

	/**
	 * 处理从GATE站点反馈的结果
	 * @return 成功返回真，否则假
	 */
	private boolean doFromGate() {
		RuleProduct product = null;
		int index = findEchoKey(0);
		try {
			if(isSuccessObjectable(index)) {
				product = getObject(RuleProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.fatal(e);
		}

		// 判断成功，不成功退出
		boolean success = (product != null && product.isSuccessful());
		if(!success) {
			return false;
		}

		// 找到CALL站点，向它投递命令
		PressRegulate cmd = getCommand();
		NodeSet set = CallOnGatePool.getInstance().find(cmd.getIssuer());
		Node call = (set != null ? set.next() : null);
		// 判断CALL站点有效
		success = (call != null);
		// 不成功，解除事务锁定，退出
		if (!success) {
			detach();
			return false;
		}

		// 回显地址修改为公网地址，之后发送的命令采用公网地址
		Cabin cabin = getListener().duplicate();
		cabin.setNode(GateLauncher.getInstance().getPublicListener());
		setListener(cabin);

		// 生成副本命令，发送给CALL站点（CALL站点使用公网地址）
		PressRegulate sub = getCommand().duplicate();
		sub.setIssuer(sub.getIssuer());
		success = launchTo(call, sub);

		// 撤销事务
		if (!success) {
			detach();
		}

		return success;
	}

	/**
	 * 处理从CALL站点反馈的结果
	 * @return 成功返回真，否则假
	 */
	private boolean doFromCall() {
		RegulateProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(RegulateProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.fatal(e);
		}

		// 判断成功
		boolean success = (product != null);
		//		// 结果
		//		if (success) {
		//			Logger.note(this, "doFromCall", success, "dock:%s, count:%d", product.getDock(), product.getCount());
		//		}

		// 修改为内网地址
		Cabin cabin = getListener().duplicate();
		cabin.setNode(GateLauncher.getInstance().getPrivate());
		setListener(cabin);
		// 解除锁定
		detach();

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		attach = null;
	}
}