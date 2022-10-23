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

import com.laxcus.command.conduct.*;
import com.laxcus.front.tub.mission.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.mission.*;
import com.laxcus.site.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;

/**
 * 分布计算命令调用器
 * 
 * @author scott.liang
 * @version 1.0 8/4/2019
 * @since laxcus 1.0
 */
public class TubConductInvoker extends TubRuleInvoker {

	/** CONDUCT处理步骤，从1开始 **/
	private int step = 1;

	/**
	 * 构造分布计算命令调用器，指定边缘容器任务
	 * @param mission 边缘容器任务
	 */
	public TubConductInvoker(TubMission mission) {
		super(mission);
		createRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Conduct getCommand() {
		return (Conduct) super.getCommand();
	}

	/**
	 * 定义事务处理规则
	 */
	private void createRule() {
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
	 * 第一阶段处理
	 * @return 处理成功返回真，否则假
	 */
	private boolean send() {
		Conduct cmd = getCommand();
		Sock root = cmd.getSock();

		// 查找CONDUCT.INIT注册地址(包括对系统组件的判断)
		Phase phase = new Phase(getUsername(), PhaseTag.INIT, root);
		NodeSet set = getStaffPool().findTaskSites(phase);
		if (set == null || set.isEmpty()) {
			faultX(FaultTip.NOTFOUND_X, phase);
			return false;
		}
		Node hub = set.next();

		// 发送到CALL站点
		boolean success = completeTo(hub, cmd);
		// 不成功，通知DRIVER会话
		if(!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		Logger.debug(this, "launch", success, "send to %s", hub);

		return success;
	}

	/**
	 * 接收分布计算命令处理结果，把结果数据保存到边缘容器结果中
	 * @return 处理成功返回真，否则假
	 */
	private boolean receive() {
		// 如果存在故障时
		if (isFaultCompleted()) {
			faultX(FaultTip.FAILED_X, getCommand());
			return false;
		}

		// 判断全部数据在磁盘，或者其它
		boolean ondisk = isEchoFiles();
		// 显示信息
		boolean success = false;
		try {
			if (ondisk) {
				File file = findFile(getFlag());
				File tub = rename(file);
				setResult(new MissionFileResult(tub));
			} else {
				byte[] b = super.collect();
				setResult(new MissionBufferResult(b));
			}
			success = true;
		} catch (Throwable e) {
			Logger.error(e);
			super.fault(e);
		}

		return success;
	}	

}