/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.pool;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.invoker.*;
import com.laxcus.front.tub.mission.*;
import com.laxcus.mission.*;
import com.laxcus.util.*;

/**
 * FRONT调用器管理池<br><br>
 * 
 * 异步调用器的“启动/分派（launch/dispatch）”工作，交给上级默认的接口处理。<br><br>
 * 
 * 3.6版本增加对边缘计算的支持，提供“云/边/端”全栈一体化处理。<br>
 * 对此，FRONT节点在保持原有角色不变情况下，加入做为“边缘计算”节点的中继角色，支持本地运行的边缘容器向云端发起请求。<br>
 * “TubMission”是边缘容器向云端发起请求的会话任务。<br>
 * 
 * @author scott.liang
 * @version 1.2 7/12/2019
 * @since laxcus 1.0
 */
public class FrontInvokerPool extends InvokerPool {
	
	/** 边缘容器任务生成器，由子类确定它！**/
	private TubMissionCreator creator;

	/**
	 * 构造默认的FRONT调用器管理池。
	 */
	protected FrontInvokerPool() {
		super();
	}

	/**
	 * 边缘容器任务生成器，由子类确定它！
	 * @param e 边缘容器任务生成器
	 */
	public void setTubMissionCreator(TubMissionCreator e) {
		creator = e;
	}

	/**
	 * 返回边缘容器任务生成器
	 * @return 边缘容器任务生成器
	 */
	public TubMissionCreator getTubMissionCreator() {
		return creator;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerPool#launch(com.laxcus.echo.invoke.EchoInvoker)
	 */
	@Override
	public boolean launch(EchoInvoker invoker) {
		// 判断调用器从FrontInvoker派生
		boolean success = isFrom(invoker.getClass(), FrontInvoker.class);
		// 也允许从EchoInvoker派生，可能是定义调用器
		if (!success) {
			success = isFrom(invoker.getClass(), EchoInvoker.class);
		}
		// 启动工作
		if (success) {
			success = defaultLaunch(invoker);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerPool#dispatch(com.laxcus.echo.EchoFlag)
	 */
	@Override
	protected boolean dispatch(EchoFlag flag) {
		return defaultDispatch(flag);
	}
	
	/**
	 * 启动任务调用器。<br>
	 * TubMissionCreator、EdgeMissionCreator、DriverMissionCreator产生的调用器。
	 * 
	 * @param invoker 异步调用器
	 * @return 返回调用器编号
	 */
	protected long launchMissionInvoker(EchoInvoker invoker) {
		// 生成本地回显地址
		Cabin cabin = createDefaultCabin();
		// 给异步调用器设置本地回显地址
		invoker.setListener(cabin);
		// 给异步调用器设置异步数据受理器
		invoker.setEchoAcceptor(this);
		// 重置启动时间
		invoker.resetLaunchTime();

		// 保存异步调用器
		boolean success = addInvoker(invoker);
		// 产生线程去处理异步任务
		if (success) {
			LaunchTrustor trustor = new LaunchTrustor(this, invoker);
			trustor.start();
		}

		return (success ? cabin.getInvokerId() : InvokerIdentity.INVALID);
	}

//	/**
//	 * 启动边缘容器调用器
//	 * @param invoker 异步调用器
//	 * @return 返回调用器编号
//	 */
//	private long launchTub(FrontInvoker invoker) {
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
	 * 边缘计算向云端发起操作请求
	 * @param mission 边缘计算任务
	 * @return 返回任务结果
	 * @throws MissionException
	 */
	public MissionResult launchTub(TubMission mission) throws MissionException {
		// 生成调用器
		FrontInvoker invoker = creator.createInvoker(mission);
		if (invoker == null) {
			throw new MissionUnsupportedException("cannot be create invoker!");
		}

		// 发送命令到集群，返回被分配的调用器编号
		long invokerId = launchMissionInvoker(invoker);
		// 判断成功
		boolean success = InvokerIdentity.isValid(invokerId);
		// 不成功，取消在线状态，弹出异常
		if (!success) {
			throw new MissionException("commit failed!");
		}

		// 没能收到退出指令，将进入等待，直到收到为止。
		while (!mission.isExit()) {
			delay(1000);
		}

		// 弹出故障
		if (mission.getException() != null) {
			throw mission.getException();
		}

		// 找到结果
		MissionResult result = mission.getResult();
		if (result == null) {
			throw new MissionException("mission timeout!");
		}

		// 设置命令
		result.setCommand(mission.getCommand());
		// 返回结果
		return result;
	}

	/**
	 * 边缘计算向云端发起操作请求
	 * @param cmd 分布命令
	 * @return 返回任务结果
	 * @throws MissionException
	 */
	public MissionResult launchTub(Command cmd) throws MissionException {
		// 空指针，忽略！
		if (cmd == null) {
			throw new MissionException("null pointer!");
		}

		Mission mission = creator.create(cmd);
		// 如果是空指针
		if (mission == null) {
			throw new MissionNotFoundException("cannot be find command!");
		}
		// 判断是基于TubMission
		if (!Laxkit.isClassFrom(mission, TubMission.class)) {
			throw new MissionUnsupportedException("cannot be support! %s",
					mission.getClass().getName());
		}
		// 任务调用
		return launchTub((TubMission) mission);
	}

	/**
	 * 边缘计算向云端发起操作请求
	 * @param input 分布命令
	 * @return 返回任务结果
	 * @throws MissionException
	 */
	public MissionResult launchTub(String input) throws MissionException {
		Command cmd = creator.create(input);
		if (cmd == null) {
			return null;
		}
		return launchTub(cmd);
	}

}