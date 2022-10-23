/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.establish.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.driver.invoker.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.front.pool.*;

/**
 * 驱动程序命令管理池。
 * 
 * 区别在于对“press”接口的处理。
 * 
 * @author scott.liang
 * @version 1.1 8/12/2014
 * @since laxcus 1.0
 */
public class DriverCommandPool extends FrontCommandPool {

	/** 管理池句柄 **/
	private static DriverCommandPool selfHandle = new DriverCommandPool();
	
	/**
	 * 构造交互模式的异步命令管理池
	 */
	private DriverCommandPool() {
		super();
	}

	/**
	 * 返回管理池的静态句柄
	 * @return DriverCommandPool实例
	 */
	public static DriverCommandPool getInstance() {
		return DriverCommandPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#press(com.laxcus.command.Command)
	 */
	@Override
	public boolean press(Command cmd) {
		// 生成驱动任务
		DriverMission mission = new DriverMission(cmd);

		// 判断命令类型，生成调用器
		EchoInvoker invoker = null;
		if (cmd.getClass() == Conduct.class) {
			invoker = new DriverConductInvoker(mission);
		} else if (cmd.getClass() == Establish.class) {
			invoker = new DriverEstablishInvoker(mission);
		}
		// 查询存在
		else if(cmd.getClass() == ShiftAssertSchema.class) {
			invoker = new DriverShiftAssertSchemaInvoker(mission);
		} else if(cmd.getClass() == ShiftAssertTable.class) {
			invoker = new DriverShiftAssertTableInvoker(mission);
		} else if(cmd.getClass() == ShiftAssertUser.class) {
			invoker = new DriverShiftAssertUserInvoker(mission);
		} 
//		else if(cmd.getClass() == ShiftAssertSwift.class) {
//			invoker = new DriverShiftAssertSwiftInvoker(mission);
//		}
		// 上传数据文件/下载数据块
		else if (cmd.getClass() == ShiftSingleImportEntity.class) {
			invoker = new DriverShiftSingleImportEntityInvoker(mission);
		} else if (cmd.getClass() == ShiftSingleExportEntity.class) {
			invoker = new DriverShiftSingleExportEntityInvoker(mission);
		}
		
//		// 登录时的转发命令
//		else if (cmd.getClass() == ShiftTakeAccount.class) {
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
		
		// 以上不成立，判断是登录转发命令
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
