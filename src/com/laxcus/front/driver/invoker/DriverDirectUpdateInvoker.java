/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.mission.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 简单UPDATE命令异步调用器。
 * 
 * @author scott.liang
 * @version 1.1 12/30/2013
 * @since laxcus 1.0
 */
public class DriverDirectUpdateInvoker extends DriverRuleInvoker {

	/** 处理步骤，从1开始**/
	private int step = 1;

	/**
	 * 构造UPDATE命令调用器
	 * @param mission 驱动任务
	 */
	public DriverDirectUpdateInvoker(DriverMission mission) {
		super(mission);
		// 建立表规则
		init();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Update getCommand() {
		return (Update) super.getCommand();
	}

	/**
	 * 初始化事务规则
	 */
	private void init() {
		Update cmd = getCommand();
		// 设置表事务处理规则
		addRules(cmd.getRules());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.DriverStageInvoker#process()
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

		// 如果出错，向AID提交故障锁定
		if (!success) {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		// 大于2是完成，否则是没有完成
		return (!success || step > 2);
	}

	/**
	 * 从地址池中找到一个CALL站点，将命令发送给它
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		Update cmd = getCommand();

		// 根据数据表名，找到它的对应CALL站点集合
		Space space = cmd.getSpace();
		NodeSet set = getStaffPool().findTableSites(space);

		// 顺序枚举一个CALL站点地址，保持调用平衡
		Node hub = (set != null ? set.next() : null);
		if (hub == null) {
			faultX(FaultTip.NOTFOUND_X, space); // 没有找到，退出!
			return false;
		}

		// 发送到目标地址
		boolean success = completeTo(hub, cmd);
		// 出错
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}
		
		Logger.debug(this, "send", success, "send to %s", hub);

		return success;
	}

	/**
	 * 接收结果
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		int index = findEchoKey(0);
		AssumeUpdate consult = null;
		try {
			if (isSuccessObjectable(index)) {
				consult = getObject(AssumeUpdate.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断处理成功
		boolean success = (consult != null && consult.isSuccess());

		if (success) {
			setResult(new MissionObjectResult(consult));
		} else {
			Update cmd = getCommand();
			faultX(FaultTip.UPDATE_FAILED_X, cmd.getSpace());
		}
		return success;
	}

}