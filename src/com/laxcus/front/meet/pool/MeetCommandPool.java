/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.establish.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.meet.invoker.*;
import com.laxcus.front.pool.*;

/**
 * 基于交互模式的命令管理池，被终端和控制台使用。
 * 
 * @author scott.liang
 * @version 1.1 12/22/2013
 * @since laxcus 1.0
 */
public class MeetCommandPool extends FrontCommandPool {

	/** 管理池句柄 **/
	private static MeetCommandPool selfHandle = new MeetCommandPool();
	
	/**
	 * 构造交互模式的异步命令管理池
	 */
	private MeetCommandPool() {
		super();
	}

	/**
	 * 返回管理池的静态句柄
	 * @return
	 */
	public static MeetCommandPool getInstance() {
		return MeetCommandPool.selfHandle;
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

		// 判断内存/磁盘空间不足
		EchoInvoker invoker = null;
		if (cmd.getClass() == VMMemoryMissing.class) {
			invoker = new MeetVMMemoryMissingInvoker((VMMemoryMissing) cmd);
		} else if (cmd.getClass() == MemoryMissing.class) {
			invoker = new MeetMemoryMissingInvoker((MemoryMissing) cmd);
		} else if (cmd.getClass() == DiskMissing.class) {
			invoker = new MeetDiskMissingInvoker((DiskMissing) cmd);
		}
		// 判断有效
		success = (invoker != null);
		if (success) {
			success = getInvokerPool().launch(invoker);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.CommandPool#post(com.laxcus.command.Command)
	 */
	@Override
	public boolean press(Command cmd) {
		EchoInvoker invoker = null;

		if (cmd.getClass() == Conduct.class) {
			invoker = new MeetConductInvoker((Conduct) cmd);
		} else if (cmd.getClass() == Establish.class) {
			invoker = new MeetEstablishInvoker((Establish) cmd);
		}
		// 流量测试
		else if (cmd.getClass() == ShiftSwarm.class) {
			invoker = new MeetShiftSwarmInvoker((ShiftSwarm) cmd);
		}
		// 诊断测试
		else if (cmd.getClass() == ShiftAssertUser.class) {
			invoker = new MeetShiftAssertUserInvoker((ShiftAssertUser) cmd);
		} else if (cmd.getClass() == ShiftAssertSchema.class) {
			invoker = new MeetShiftAssertSchemaInvoker((ShiftAssertSchema) cmd);
		} else if (cmd.getClass() == ShiftAssertTable.class) {
			invoker = new MeetShiftAssertTableInvoker((ShiftAssertTable) cmd);
		} 
//		else if (cmd.getClass() == ShiftAssertSwift.class) {
//			invoker = new MeetShiftAssertSwiftInvoker((ShiftAssertSwift) cmd);
//		}
		// 上传数据文件/下载数据块
		else if (cmd.getClass() == ShiftSingleImportEntity.class) {
			invoker = new MeetShiftSingleImportEntityInvoker((ShiftSingleImportEntity) cmd);
		} else if (cmd.getClass() == ShiftSingleExportEntity.class) {
			invoker = new MeetShiftSingleExportEntityInvoker((ShiftSingleExportEntity) cmd);
		}
		
//		// 发布分布任务组件/分布任务组件应用附件/动态链接库
//		else if (cmd.getClass() == ShiftPublishSingleTaskComponent.class) {
//			invoker = new MeetShiftPublishSingleTaskComponentInvoker(
//					(ShiftPublishSingleTaskComponent) cmd);
//		} else if (cmd.getClass() == ShiftPublishSingleTaskAssistComponent.class) {
//			invoker = new MeetShiftPublishSingleTaskAssistComponentInvoker(
//					(ShiftPublishSingleTaskAssistComponent) cmd);
//		} else if(cmd.getClass() == ShiftPublishSingleTaskLibraryComponent.class) {
//			invoker = new MeetShiftPublishSingleTaskLibraryComponentInvoker((ShiftPublishSingleTaskLibraryComponent)cmd);
//		}
		
//		// 发布码位计算器
//		else if (cmd.getClass() == ShiftPublishSingleScalerComponent.class) {
//			invoker = new MeetShiftPublishSingleScalerComponentInvoker(
//					(ShiftPublishSingleScalerComponent) cmd);
//		} else if (cmd.getClass() == ShiftPublishSingleScalerAssistComponent.class) {
//			invoker = new MeetShiftPublishSingleScalerAssistComponentInvoker(
//					(ShiftPublishSingleScalerAssistComponent) cmd);
//		} else if (cmd.getClass() == ShiftPublishSingleScalerLibraryComponent.class) {
//			invoker = new MeetShiftPublishSingleScalerLibraryComponentInvoker(
//					(ShiftPublishSingleScalerLibraryComponent) cmd);
//		}
		
//		// 发布快捷组件
//		else if (cmd.getClass() == ShiftPublishSingleSwiftComponent.class) {
//			invoker = new MeetShiftPublishSingleSwiftComponentInvoker(
//					(ShiftPublishSingleSwiftComponent) cmd);
//		} else if (cmd.getClass() == ShiftPublishSingleSwiftAssistComponent.class) {
//			invoker = new MeetShiftPublishSingleSwiftAssistComponentInvoker(
//					(ShiftPublishSingleSwiftAssistComponent) cmd);
//		} else if (cmd.getClass() == ShiftPublishSingleSwiftLibraryComponent.class) {
//			invoker = new MeetShiftPublishSingleSwiftLibraryComponentInvoker(
//					(ShiftPublishSingleSwiftLibraryComponent) cmd);
//		}
		
//		// 转发命令
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
		
		// 以上不成立，判断是FRONT登录转发命令
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