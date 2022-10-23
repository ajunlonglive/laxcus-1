/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.contact.*;
import com.laxcus.echo.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;

/**
 * SWIFT命令调用器。<br>
 * FRONT负责发起SWIFT命令，然后进入管理池反馈数据，在解释显示数据后退出。
 * 
 * @author scott.liang
 * @version 1.0 5/4/2020
 * @since laxcus 1.0 
 */
public class MeetContactInvoker extends MeetRuleInvoker {

	/** SWIFT处理步骤，从1开始 **/
	private int step = 1;

	/**
	 * 构造SWIFT命令调用器，指定命令
	 * @param cmd SWIFT异步命令
	 */
	public MeetContactInvoker(Contact cmd) {
		super(cmd);
		// 不允许管理员操纵这个命令
		setRefuseAdministrator(true);
		// 初始化事务处理规则
		initRule();
	}

	/**
	 * 定义事务处理规则
	 */
	private void initRule() {
		Contact conduct = getCommand();
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
	public Contact getCommand() {
		return (Contact) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#process()
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
		Contact cmd = getCommand();

		// 检查NEAR阶段任务实例（系统/用户阶段命名）
		if (!checkNear()) {
			return false;
		}

		// 随机取一个CALL节点，正常情况下，CALL节点必然有指向WORK节点的DISTANT阶段命名
		NodeSet set = getStaffPool().getCallSites();
		Node hub = (set != null ? set.next() : null);

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

		Contact cmd = getCommand();
		
		// 建立阶段命名
		Phase phase = createNear();
		// 1. 查找NEAR阶段命名，取它对应的实例
		NearTask task = NearTaskPool.getInstance().create(phase);

		// 失败
		if (task == null) {
			super.faultX(FaultTip.NOTFOUND_X, cmd.getSock());
			return false;
		}

		// 本次运行时间
		printRuntime();

		// 指定命令
		task.setCommand(cmd);
		task.setInvokerId(getInvokerId());
		// 设置显示终端
		task.setDisplay(MeetInvoker.getDefaultDisplay());

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
	 * 生成NEAR阶段命名
	 * @return Phase实例
	 */
	private Phase createNear() {
		Contact cmd = getCommand();
		return new Phase(getUsername(), PhaseTag.NEAR, cmd.getSock());
	}

	/**
	 * 检查NEAR阶段命名存在
	 * @return 存在返回真，否则假。
	 */
	private boolean checkNear() {
		Phase phase = createNear();
		
		// 判断NEAR组件存在
		boolean success = false;
		try {
			success = NearTaskPool.getInstance().contains(phase);
			if (success) {
				NearTask task = NearTaskPool.getInstance().create(phase);
				success = (task != null);
			}
			if (!success) {
				faultX(FaultTip.NOTFOUND_X, phase);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
			faultX(FaultTip.ILLEGAL_COMMAND);
		}

		Logger.debug(this, "checkNear", success, "phase is '%s'", phase);

		return success;
	}

	/**
	 * 打印SWIFT执行故障
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