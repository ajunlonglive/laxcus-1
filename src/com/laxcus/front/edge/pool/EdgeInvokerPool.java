/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.edge.pool;

import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.edge.*;
import com.laxcus.front.edge.mission.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.mission.*;

/**
 * FRONT.EDGE调用器管理池。<br><br>
 * 
 * 遵循基本的异步调用规则，不同之处是，FRONT.EDGE以声明为起点。
 * 
 * @author scott.liang
 * @version 1.3 10/19/2013
 * @since laxcus 1.0
 */
public final class EdgeInvokerPool extends FrontInvokerPool {

	/** 回显任务管理池 **/
	private static EdgeInvokerPool selfHandle = new EdgeInvokerPool();

	/**
	 * 构造一个默认和私有的FRONT.EDGE调用器管理池
	 */
	private EdgeInvokerPool() {
		super();
	}

	/**
	 * 返回FRONT.EDGE调用器管理池句柄
	 * @return EdgeInvokerPool实例
	 */
	public static EdgeInvokerPool getInstance() {
		return EdgeInvokerPool.selfHandle;
	}

	/**
	 * 返回边缘计算任务生成器句柄
	 * @return EdgeMissionCreator实例
	 */
	private EdgeMissionCreator getMissionCreator() {
		EdgeLauncher launcher = (EdgeLauncher) getLauncher();
		return launcher.getMissionCreator();
	}

//	/**
//	 * 启动驱动程序异步调用器
//	 * @param invoker 调用器实例
//	 * @return 成功返回调用器编号，否则是-1。
//	 */
//	private long launchInvoker(EchoInvoker invoker) {
//		// 生成本地回显地址
//		Cabin cabin = createDefaultCabin();
//		// 给异步调用器设置本地回显地址
//		invoker.setListener(cabin);
//		// 给异步调用器设置异步数据受理器
//		invoker.setEchoAcceptor(this);
//
//		// 保存异步调用器
//		boolean success = addInvoker(invoker);
//		// 产生线程去处理异步任务
//		if (success) {
//			LaunchTrustor trustor = new LaunchTrustor(this, invoker);
//			trustor.start();
//		}
//
//		return (success ? cabin.getInvokerId() : InvokerIdentity.INVALID);
//	}
	
	/**
	 * 判断边缘计算任务中的命令类型，启动关联的异步调用器
	 * @param mission 边缘计算任务
	 * @return 启动成功返回系统分配的调用器编号，否则是-1。
	 */
	public long launch(EdgeMission mission) {
		EdgeMissionCreator creator = getMissionCreator();
		
		// 生成调用器
		EchoInvoker invoker = null;
		try {
			invoker = creator.createInvoker(mission);
		} catch (MissionException e) {
			mission.setException(e);
			return InvokerIdentity.INVALID;
		}

		// 以上不成立，判断是自定义命令，然后找到关联的调用器
		if (invoker == null) {
			invoker = CustomCreator.createInvoker(mission.getCommand()); // 根据命令找到关联的调用器
		}

		long invokerId = InvokerIdentity.INVALID;
		// 判断调用器有效
		if (invoker != null) {
			invokerId = launchMissionInvoker(invoker);
		}

		Logger.debug(this, "launch", InvokerIdentity.isValid(invokerId),
				"command is \'%s\', invoker id:%d", mission.getCommand(), invokerId);

		// 成功返回调用器编号：否则是-1。
		return invokerId;
	}
	
}