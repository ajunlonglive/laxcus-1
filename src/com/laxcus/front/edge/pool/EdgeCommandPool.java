/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.edge.pool;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.pool.*;

/**
 * 边缘计算命令管理池。
 * 
 * 区别在于对“press”接口的处理。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2019
 * @since laxcus 1.0
 */
public class EdgeCommandPool extends FrontCommandPool {

	/** 管理池句柄 **/
	private static EdgeCommandPool selfHandle = new EdgeCommandPool();
	
	/**
	 * 构造交互模式的异步命令管理池
	 */
	private EdgeCommandPool() {
		super();
	}

	/**
	 * 返回管理池的静态句柄
	 * @return EdgeCommandPool实例
	 */
	public static EdgeCommandPool getInstance() {
		return EdgeCommandPool.selfHandle;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.FrontCommandPool#dispatch(com.laxcus.command.Command)
	 */
	@Override
	protected boolean dispatch(Command cmd) {
		boolean success = super.dispatch(cmd);
		if (success) {
			return true;
		}

		// 检查属于EdgeCommandPool的命令

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#press(com.laxcus.command.Command)
	 */
	@Override
	public boolean press(Command cmd) {
		//		// 生成驱动任务
		//		EdgeMission mission = new EdgeMission(cmd);
		//
		//		// 判断命令类型，生成调用器
		//		EchoInvoker invoker = null;

		//		if (cmd.getClass() == Conduct.class) {
		//			invoker = new EdgeConductInvoker(mission);
		//		} else if (cmd.getClass() == Establish.class) {
		//			invoker = new EdgeEstablishInvoker(mission);
		//		}
		//		// 查询存在
		//		else if(cmd.getClass() == ShiftAssertSchema.class) {
		//			invoker = new EdgeShiftAssertSchemaInvoker(mission);
		//		} else if(cmd.getClass() == ShiftAssertTable.class) {
		//			invoker = new EdgeShiftAssertTableInvoker(mission);
		//		} else if(cmd.getClass() == ShiftAssertUser.class) {
		//			invoker = new EdgeShiftAssertUserInvoker(mission);
		//		} else if(cmd.getClass() == ShiftAssertSwift.class) {
		//			invoker = new EdgeShiftAssertSwiftInvoker(mission);
		//		}

		//		// 登录时的转发命令
		//
		//		if (cmd.getClass() == ShiftTakeAccount.class) {
		//			invoker = new FrontShiftTakeAccountInvoker((ShiftTakeAccount) cmd);
		//		} else if (cmd.getClass() == ShiftTakeAuthorizerSite.class) {
		//			invoker = new FrontShiftTakeAuthorizerSiteInvoker((ShiftTakeAuthorizerSite) cmd);
		//		} else if (cmd.getClass() == ShiftTakeOwnerCall.class) {
		//			invoker = new FrontShiftTakeOwnerCallInvoker((ShiftTakeOwnerCall) cmd);
		//		} else if(cmd.getClass() == ShiftTakeGrade.class) {
		//			invoker = new FrontShiftTakeGradeInvoker((ShiftTakeGrade)cmd);
		//		} else if (cmd.getClass() == ShiftTakeAuthorizerCall.class) {
		//			invoker = new FrontShiftTakeAuthorizerCallInvoker((ShiftTakeAuthorizerCall) cmd);
		//		} else if(cmd.getClass() == ShiftTakeAuthorizerTable.class) {
		//			invoker = new FrontShiftTakeAuthorizerTableInvoker((ShiftTakeAuthorizerTable)cmd);
		//		}

		// 判断命令类型，生成调用器
		EchoInvoker invoker = null;
		// 以上不成立，检查是登录命令
		if (invoker == null) {
			invoker = createLoginInvoker(cmd);
		}

		// 启动命令
		boolean success = (invoker != null);
		if (success) {
			success = getInvokerPool().launch(invoker);
		}
		return success;
	}

}