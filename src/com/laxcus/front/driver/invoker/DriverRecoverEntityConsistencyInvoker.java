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
 * 恢复表数据一致性调用器。<br>
 * 
 * 此命令由FRONT站点发出，目标是CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public class DriverRecoverEntityConsistencyInvoker extends DriverForbidInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造恢复表数据一致性调用器
	 * @param mission 驱动任务
	 */
	public DriverRecoverEntityConsistencyInvoker(DriverMission mission) {
		super(mission);
		initForbid();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RecoverEntityConsistency getCommand() {
		return (RecoverEntityConsistency) super.getCommand();
	}

	/**
	 * 设置禁止操作的单元
	 * 当前执行时，禁止任何操作
	 */
	private void initForbid() {
		RecoverEntityConsistency cmd = getCommand();
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
		// 增1
		step++;
		// 不成功，或者完成，退出!
		return (!success || step > 2);
	}

	/**
	 * 发送修复数据命令到CALL站点
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		RecoverEntityConsistency cmd = getCommand();
		Space space = cmd.getSpace();

		NodeSet set = getStaffPool().findTableSites(space);
		Node hub = (set != null ? set.next() : null);
		// 没有站点
		if (hub == null) {
			super.faultX(FaultTip.SYSTEM_FAULT);
			return false;
		}
		// 发送到指定的CALL站点
		return fireToHub(hub, cmd);

		//		// 发送到指定的CALL站点
		//		boolean success = launchTo(hub, cmd);
		//		if (!success) {
		//			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		//		}
		//		return success;
	}

	/**
	 * 接收数据修复结果
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		RollTableMassProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(RollTableMassProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);
		// 打印结果
		if (success) {
			setProduct(product);
		} else {
			fault("recover failed!");
		}
		// 返回结果
		return success;
	}

}