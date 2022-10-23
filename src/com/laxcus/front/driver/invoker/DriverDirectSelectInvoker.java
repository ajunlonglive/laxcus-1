/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.mission.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;

/**
 * 标准SELECT调用器。 <br>
 * 
 * 标准SELECT是“SELECT ... FROM ... WHERE”格式，不包含嵌套等查询语句的SELECT命令。 
 * 
 * @author scott.liang
 * @version 1.0 12/03/2014
 * @since laxcus 1.0
 */
public class DriverDirectSelectInvoker extends DriverRuleInvoker {

	/** 处理步骤，从1开始**/
	private int step = 1;

	/**
	 * 构造标准SELECT调用器
	 * @param mission 驱动任务
	 */
	public DriverDirectSelectInvoker(DriverMission mission) {
		super(mission);
		initRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Select getCommand() {
		return (Select) super.getCommand();
	}

	/**
	 * 建立事务规则
	 */
	private void initRule() {
		Select cmd = getCommand();
		// 保存SELECT表事务处理规则
		addRules(cmd.getRules());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.DriverRuleInvoker#process()
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
		
		// 如果出错，在终端提示，算是完成
		if (!success) {
			faultX(FaultTip.FAILED_X, getCommand());
		}
		
		// 自增1
		step++;
		// 大于2是完成，否则是没有完成
		return (!success || step > 2);
	}

	/**
	 * 执行第一阶段操作。找到一个CALL站点，把命令发给它
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		Select cmd = getCommand();

		// 根据数据表名，找到它的对应CALL站点集合
		Space space = cmd.getSpace();
		NodeSet set = getStaffPool().findTableSites(space);
		// 顺序枚举一个CALL站点地址，保持调用平衡
		Node hub = (set != null ? set.next() : null);
		// 没有找到，弹出错误
		if (hub == null) {
			faultX(FaultTip.NOTFOUND_SITE_X, space);
			return false;
		}

		// 发送到目标地址
		boolean success = completeTo(hub, cmd);
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		Logger.debug(this, "send", success, "send to %s", hub);

		return success;
	}

	/**
	 * 执行第二阶段操作，显示数据处理结果
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		// 出错
		if (isFaultCompleted()) {
			faultX(FaultTip.FAILED_X, getCommand());
			return false;
		}

		// 判断数据全部在文件
		boolean ondisk = isEchoFiles();
		// 判断数据在内存或者磁盘
		if (ondisk) {
			File file = findFile(0);
			file = super.rename(file);
			super.setResult(new MissionFileResult(file));
		} else {
			byte[] b = collect();
			super.setResult(new MissionBufferResult(b));
		}

		// 正常退出
		return true;
	}

}
