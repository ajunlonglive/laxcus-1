/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.mission;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.driver.pool.*;
import com.laxcus.mission.*;

/**
 * 驱动程序任务
 * 
 * @author scott.liang
 * @version 1.3 7/30/2019
 * @since laxcus 10
 */
public final class DriverMission extends Mission {
	
	/**
	 * 构造默认的驱动程序任务
	 */
	public DriverMission() {
		super();
	}

	/**
	 * 构造驱动程序任务，指定命令
	 * @param cmd 命令
	 */
	public DriverMission(Command cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.mission.Mission#commit(long, boolean)
	 */
	@Override
	public MissionResult commit(long timeout, boolean memory)
			throws MissionException {
		// 设置超时时间
		command.setTimeout(timeout);
		// 设置内存处理模式
		command.setMemory(memory);

		// 发送命令到集群，返回被分配的调用器编号
		invokerId = DriverInvokerPool.getInstance().launch(this);
		// 判断成功
		boolean success = InvokerIdentity.isValid(invokerId);
		// 不成功，取消在线状态，弹出异常
		if (!success) {
			throw new MissionException("commit failed!");
		}

		// 没能收到退出指令，将进入等待，直到收到为止。
		while (!isExit()) {
			delay(1000);
		}

		// 弹出故障
		if (exception != null) {
			throw exception;
		} else if (result == null) {
			throw new MissionException("mission timeout!");
		}
		
		// 设置命令
		result.setCommand(command);
		return result;
	}
	
}