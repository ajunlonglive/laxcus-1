/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.edge.mission;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.edge.pool.*;
import com.laxcus.mission.*;

/**
 * 边缘计算任务
 * 
 * @author scott.liang
 * @version 1.3 2/23/2013
 * @since laxcus 10
 */
public final class EdgeMission extends Mission {
	
	/**
	 * 构造默认的边缘计算任务
	 */
	public EdgeMission() {
		super();
	}

	/**
	 * 构造边缘计算任务，指定命令
	 * @param cmd 命令
	 */
	public EdgeMission(Command cmd) {
		super(cmd);
	}
	
	/**
	 * 提交命令到服务器，返回处理结果
	 * 
	 * @param timeout 命令超时时间，单位：毫秒
	 * @param memory 采用内存处理模式
	 * @return 返回处理结果
	 * 
	 * @throws MissionException 如果处理过程中发生异常
	 */
	public MissionResult commit(long timeout, boolean memory) throws MissionException {
		// 设置超时时间
		command.setTimeout(timeout);
		// 设置内存处理模式
		command.setMemory(memory);
		
		// 发送命令给集群，返回被分配的调用器编号
		invokerId = EdgeInvokerPool.getInstance().launch(this);
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