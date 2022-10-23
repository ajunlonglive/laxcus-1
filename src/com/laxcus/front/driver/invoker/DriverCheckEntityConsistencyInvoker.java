/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.law.forbid.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检查表数据一致性调用器。<br>
 * 
 * 此命令由FRONT站点发出，目标是CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public class DriverCheckEntityConsistencyInvoker extends DriverForbidInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造检查表数据一致性调用器，指定命令
	 * @param cmd 检查表数据一致性命令
	 */
	public DriverCheckEntityConsistencyInvoker(DriverMission cmd) {
		super(cmd);
		initForbid();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckEntityConsistency getCommand() {
		return (CheckEntityConsistency) super.getCommand();
	}

	/**
	 * 初始化禁止操作单元
	 */
	private void initForbid() {
		CheckEntityConsistency cmd = getCommand();
		// 表级禁止操作单元
		TableForbidItem item = new TableForbidItem(cmd.getSpace());
		addForbidItem(item);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.DriverForbidInvoker#process()
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
		// 不成功或者完成，退出！
		return (!success || step > 2);
	}

	/**
	 * 发送数据一致性检查性命令到任意一个CALL站点
	 * @return 命令发送成功返回真，否则假
	 */
	private boolean send() {
		CheckEntityConsistency cmd = getCommand();
		Space space = cmd.getSpace();

		NodeSet set = getStaffPool().findTableSites(space);
		Node hub = (set != null ? set.next() : null);
		// 没有站点
		if (hub == null) {
			faultX(FaultTip.SYSTEM_FAULT);
			return false;
		}
		// 发送到指定的CALL站点
		return fireToHub(hub, cmd);

		//		// 发送到指定的CALL站点，返回数据保存在内存
		//		boolean success = launchTo(hub, cmd);
		//		if (!success) {
		//			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		//		}
		//
		//		return success;
	}

	/**
	 * 第二阶段。接收反馈结果。
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		int index = findEchoKey(0);
		CheckEntityConsistencyProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CheckEntityConsistencyProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);

		// 成功，显示处理结果
		if (success) {
			setProduct(product);
		} else {
			fault("cannot be git product!");
		}
		// 退出
		return success;
	}

}