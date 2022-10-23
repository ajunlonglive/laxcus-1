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

import com.laxcus.command.establish.*;
import com.laxcus.distribute.establish.*;
import com.laxcus.echo.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;

/**
 * ESTABLISH命令调用器 <br><br>
 * 
 * FRONT站点是发起数据构建工作的开始，和显示最后的处理结果。<br>
 * 数据构建是一个ETL过程。
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0 
 */
public class DesktopEstablishInvoker extends DesktopRuleInvoker { 

	/** ESTABLISH处理步骤，从1开始 **/
	private int step = 1;

	/**
	 * 构造ESTABLISH调用器，指定命令。
	 * @param cmd 数据构建命令
	 */
	public DesktopEstablishInvoker(Establish cmd) {
		super(cmd);
		// 拒绝管理员操作
		setRefuseAdministrator(true);
		// 初始化事务规则
		initRule();
	}

	/**
	 * 初始化事务规则
	 */
	private void initRule() {
		Establish establish = getCommand();
		List<RuleItem> rules = establish.getRules();
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
	public Establish getCommand() {
		return (Establish) super.getCommand();
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

		// 不成功，向AID站点提交故障锁定命令
		if (!success) {
			sendFaultItems();
		}

		// 处理不成功，或者大于2是完成
		return (!success || step > 2);
	}

	/**
	 * 处理第1阶段工作
	 * @return 处理成功返回真，否则假
	 */
	private boolean send() {
		// 检查END阶段任务实例，判断是系统命名或者用户命名
		if (!checkEnd()) {
			return false;
		}

		Establish cmd = getCommand();
		Sock root = cmd.getSock();
		// 查找ESTABLISH.ISSUE阶段对象
		Phase phase = new Phase(getUsername(), PhaseTag.ISSUE, root);
		NodeSet set = getStaffPool().findTaskSites(phase);
		if (set == null || set.isEmpty()) {
			//			super.fault("cannot find site by '%s'", cmd.getRoot());
			faultX(FaultTip.NOTFOUND_X, phase);
			return false;
		}
		// 顺序枚举一个CALL站点地址，保持均衡的调用
		Node hub = set.next();

		// 发送命令，返回数据保存到内存里
		boolean success = fireToHub(hub, cmd);

		Logger.debug(this, "send", success, "send to %s", hub);

		return success;
	}

	/**
	 * 处理第2阶段工作
	 * @return 处理成功返回真，否则假
	 */
	private boolean receive() {
		// 如果存在故障时
		if (isFaultCompleted()) {
			printResultFault();
			return false;
		}

		Establish cmd = getCommand();
		// 获得阶段命名
		EndObject end = cmd.getEndObject();
		Phase phase = (end != null ? end.getPhase() : null);
		if(phase == null) {
			phase = new Phase(getIssuer(), PhaseTag.END, cmd.getSock());
		}
		// 查找END阶段任务实例
		EndTask task = EndTaskPool.getInstance().create(phase);
		// 不存在时，使用默认实例
		if(task == null) {
			task = new DefaultEndTask();
		}

		// 本次运行时间
		printRuntime();

		// 指定命令
		task.setCommand(cmd);
		task.setInvokerId(getInvokerId());
		// 设置显示终端
		task.setDisplay(getDisplay());

		// 判断数据全部在磁盘文件或者其它
		boolean ondisk = isEchoFiles();
		boolean success = false;
		try {
			// 从文件或者内存中读内容
			if (ondisk) {
				File file = findFile(getFlag());
				task.display(new File[] { file });
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

		return success;
	}

	/**
	 * 检查END阶段命名。无论是用户自定义或者系统定义，这个END阶段命名必须存在。
	 * @return 存在返回真，否则假。
	 */
	private boolean checkEnd() {
		Establish cmd = getCommand();
		// 对象实例
		EndObject end = cmd.getEndObject();
		Phase phase = (end != null ? end.getPhase() : null);
		if (phase == null) {
			phase = new Phase(getUsername(), PhaseTag.END, cmd.getSock());
		}
		// 判断END阶段命名存在
		boolean success = false;
		try {
			success = EndTaskPool.getInstance().contains(phase);
			if (!success) {
				super.faultX(FaultTip.NOTFOUND_X, phase);
			}
		} catch (Throwable e) {
			super.faultX(FaultTip.ILLEGAL_COMMAND);
		}

		Logger.debug(this, "checkEnd", success, "phase is '%s'", phase);

		return success;
	}

	/**
	 * 打印ESTABLISH执行故障
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