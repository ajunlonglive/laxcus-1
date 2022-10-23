/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 简单DELETE命令调用器。
 * 
 * @author scott.liang
 * @version 1.1 6/19/2013
 * @since laxcus 1.0
 */
public class MeetDirectDeleteInvoker extends MeetRuleInvoker {

	/** 处理步骤，从1开始**/
	private int step = 1;

	/**
	 * 构造DELETE命令调用器，指定DELETE命令
	 * @param cmd DELETE命令
	 */
	public MeetDirectDeleteInvoker(Delete cmd) {
		super(cmd);
		// 建立表规则
		initRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Delete getCommand() {
		return (Delete) super.getCommand();
	}

	/**
	 * 建立事务规则
	 */
	private void initRule() {
		Delete cmd = getCommand();
		// 设置DELETE表事务处理规则
		addRules(cmd.getRules());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetStageInvoker#process()
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

		// 向AID站点提交故障锁定
		if (!success) {
			sendFaultItems();
		}

		// 大于2是完成，否则是没有完成
		return (!success || step > 2);
	}

	/**
	 * 投递命令到CALL站点
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		Delete cmd = getCommand();

		// 根据数据表名，找到它的对应CALL站点集合
		Space space = cmd.getSpace();
		NodeSet set = getStaffPool().findTableSites(space);

		// 顺序枚举一个CALL站点地址，保持调用平衡
		Node hub = (set != null ? set.next() : null);
		if (hub == null) {
			// fault("cannot find call site by %s", space); // 没有找到，退出!
			faultX(FaultTip.NOTFOUND_SITE_X, space);
			return false;
		}

		// 发送到目标地址
		boolean success = fireToHub(hub, cmd);

		Logger.debug(this, "send", success, "send to %s", hub);

		return success;
	}

	/**
	 * 从目标站点接收处理结果
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		int index = findEchoKey(0);
		AssumeDelete consult = null;
		try {
			if (isSuccessObjectable(index)) {
				consult = getObject(AssumeDelete.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断处理成功
		boolean success = (consult != null && consult.isSuccess());

		if (success) {
			print(consult);
		} else {
			Delete cmd = getCommand();
			faultX(FaultTip.DELETE_FAILED_X, cmd.getSpace());
		}
		
		return success;
	}

	/**
	 * 打印结果
	 * @param consult
	 */
	private void print(AssumeDelete consult) {
		// 显示运行时间
		printRuntime();

		// 设置标题
		createShowTitle(new String[] { "DELETE/SPACE", "DELETE/ROWS" });

		ShowItem item = new ShowItem();
		// 表名
		item.add(new ShowStringCell(0, consult.getSpace()));
		// 删除行数
		item.add(new ShowLongCell(1, consult.getRows()));
		// 显示
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}
}