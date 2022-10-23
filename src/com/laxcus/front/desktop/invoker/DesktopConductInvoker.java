/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.echo.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;

/**
 * CONDUCT命令调用器。<br>
 * FRONT负责发起CONDUCT命令，然后进入管理池反馈数据，在解释显示数据后退出。
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0 
 */
public class DesktopConductInvoker extends DesktopRuleInvoker {

	/** CONDUCT处理步骤，从1开始 **/
	private int step = 1;

	/**
	 * 构造CONDUCT命令调用器，指定命令
	 * @param cmd CONDUCT异步命令
	 */
	public DesktopConductInvoker(Conduct cmd) {
		super(cmd);
		// 拒绝管理员操作
		setRefuseAdministrator(true);
		initRule();
	}

	/**
	 * 定义事务处理规则
	 */
	private void initRule() {
		Conduct conduct = getCommand();
		List<RuleItem> rules = conduct.getRules();
		// 最少返回一个事务处理规则，判断是空指针，弹出异常
		if (rules.isEmpty()) {
			throw new NullPointerException();
		}
		// 保存事务规则
		addRules(rules);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Conduct getCommand() {
		return (Conduct) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.DesktopRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		boolean success = false;
		switch (step) {
		case 1:
			success = send();
			break;
		case 2:
			success = receive();
			break;
		}
		// 自增1
		step++;

		// 不成功，向GATE提交故障锁定命令
		if (!success) {
			sendFaultItems();
		}

		// 不成功，或者大于2是完成
		return (!success || step > 2);
	}

	/**
	 * 第一阶段处理
	 * @return 处理成功返回真，否则假
	 */
	private boolean send() {
		Conduct cmd = getCommand();
		Sock root = cmd.getSock();

		// 检查PUT阶段任务实例（系统/用户阶段命名）
		if (!checkPut()) {
			// super.fault("illegal conduct naming: '%s'", root);
			return false;
		}

		// 查找CONDUCT.INIT注册地址(包括对系统组件的判断)
		Phase phase = new Phase(getUsername(), PhaseTag.INIT, root);
		NodeSet set = getStaffPool().findTaskSites(phase);
		if (set == null || set.isEmpty()) {
			faultX(FaultTip.NOTFOUND_TASK_X, phase);
			return false;
		}
		Node hub = set.next();

		// 发送到CALL站点
		boolean success = fireToHub(hub, cmd);

		Logger.debug(this, "send", success, "send to %s", hub);

		return success;
	}

	/**
	 * 第二阶段处理
	 * @return 处理成功返回真，否则假
	 */
	private boolean receive() {
		// 如果存在故障时
		if (isFaultCompleted()) {
			printResultFault();
			return false;
		}

		Conduct cmd = getCommand();
		// 1. 查找PUT阶段命名，取它对应的实例
		PutObject put = cmd.getPutObject();
		Phase phase = (put != null ? put.getPhase() : null);
		if (phase == null) {
			phase = new Phase(getUsername(), PhaseTag.PUT, cmd.getSock());
		}
		// 返回对象
		PutTask task = PutTaskPool.getInstance().create(phase);

		// 失败
		if (task == null) {
			super.faultX(FaultTip.NOTFOUND_TASK_X, cmd.getSock());
			return false;
		}

		// 本次运行时间
		printRuntime();

		// 指定命令
		task.setCommand(cmd);
		task.setInvokerId(getInvokerId());
		// 设置显示终端
		task.setDisplay(getDisplay());

		// 判断全部数据在磁盘，或者其它
		boolean ondisk = isEchoFiles();
		// 显示信息
		boolean success = false;
		try {
			if (ondisk) {
				File[] files = getAllFiles();
				task.display(files);
			} else {
				byte[] b = collect();
				task.display(b, 0, b.length);
			}
			success = true;
		} catch (TaskException e) {
			Logger.error(e);
			fault(e);
		} catch (Throwable e) {
			Logger.fatal(e);
			fault(e);
		}

		// 返回结果
		return success;
	}

	/**
	 * 检查PUT阶段命名存在
	 * @return 存在返回真，否则假。
	 */
	private boolean checkPut() {
		Conduct cmd = getCommand();
		// 对象实例
		PutObject put = cmd.getPutObject();
		Phase phase = (put != null ? put.getPhase() : null);
		if (phase == null) {
			phase = new Phase(getUsername(), PhaseTag.PUT, cmd.getSock());
		}

		// 判断PUT组件存在
		boolean success = false;
		try {
			success = PutTaskPool.getInstance().contains(phase);
			if (!success) {
				super.faultX(FaultTip.NOTFOUND_TASK_X, phase);
			}
		} catch (Throwable e) {
			super.faultX(FaultTip.ILLEGAL_COMMAND);
		}

		Logger.debug(this, "checkPut", success, "phase is '%s'", phase);

		return success;
	}

	/**
	 * 打印CONDUCT执行故障
	 */
	private void printResultFault() {
		int index = findEchoKey(0);

		EchoHead head = findBufferHead(index);
		EchoHelp help = head.getHelp();
		if (help != null) {
			fault(help.toString());
		} else {
			printFault(head.getCode());
		}
	}
}