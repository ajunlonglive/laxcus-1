/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.tub.invoker;

import java.io.*;
import java.util.*;

import com.laxcus.command.establish.*;
import com.laxcus.front.tub.mission.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.mission.*;
import com.laxcus.site.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;

/**
 * ESTABLISH命令调用器
 * 
 * @author scott.liang
 * @version 1.0 8/4/2019
 * @since laxcus 1.0
 */
public class TubEstablishInvoker extends TubRuleInvoker {

	/** ESTABLISH处理步骤 **/
	private int step = 1;

	/**
	 * 构造ESTABLISH命令调用器，指定边缘容器任务
	 * @param mission 边缘容器任务
	 */
	public TubEstablishInvoker(TubMission mission) {
		super(mission);
		createRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Establish getCommand() {
		return (Establish) super.getCommand();
	}

	/**
	 * 定义事务处理规则
	 */
	private void createRule() {
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
	 * @see com.laxcus.front.tub.invoker.TubRuleInvoker#process()
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
		// 不成功，或者大于2是完成
		return (!success || step > 2);
	}

	/**
	 * 处理第1阶段工作
	 * @return 处理成功返回真，否则假
	 */
	private boolean send() {
		Establish cmd = getCommand();
		Sock root = cmd.getSock();
		// 查找ESTABLISH.ISSUE阶段对象
		Phase phase = new Phase(getUsername(), PhaseTag.ISSUE, root);
		NodeSet set = getStaffPool().findTaskSites(phase);
		if (set == null || set.isEmpty()) {
			faultX(FaultTip.NOTFOUND_X, phase);
			return false;
		}
		// 顺序枚举一个CALL站点地址，保持均衡的调用
		Node hub = set.next();

		// 发送命令，返回数据保存到内存里
		boolean success = fireToHub(hub, cmd, isDisk());
		if (!success) {
			super.faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		Logger.debug(this, "launch", success, "send to %s", hub);

		return success;
	}

	/**
	 * 接收ESTABLISH命令处理结果，把结果数据保存到边缘容器结果中
	 * @return 处理成功返回真，否则假
	 */
	private boolean receive() {
		// 如果存在故障时
		if (isFaultCompleted()) {
			faultX(FaultTip.FAILED_X, getCommand());
			return false;
		}

		// 判断数据在磁盘文件或者其它
		boolean ondisk = isEchoFiles();
		boolean success = false;
		try {
			if (ondisk) {
				File file = findFile(getFlag());
				File tub = rename(file);
				setResult(new MissionFileResult(tub));
			} else {
				byte[] b = collect();
				setResult(new MissionBufferResult(b));
			}
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
			super.fault(e);
		}

		return success;
	}


}