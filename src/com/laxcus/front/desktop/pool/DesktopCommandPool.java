/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.pool;

import com.laxcus.command.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.command.access.user.*;
import com.laxcus.command.conduct.*;
import com.laxcus.command.establish.*;
import com.laxcus.command.missing.*;
import com.laxcus.command.traffic.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.front.pool.*;
import com.laxcus.front.desktop.invoker.*;
import com.laxcus.ui.display.*;
import com.laxcus.util.*;

/**
 * 桌面命令池
 * 
 * @author scott.liang
 * @version 1.0 5/19/2021
 * @since laxcus 1.0
 */
public class DesktopCommandPool extends FrontCommandPool {

	/** 管理池句柄 **/
	private static DesktopCommandPool selfHandle = new DesktopCommandPool();
	
	/**
	 * 构造交互模式的异步命令管理池
	 */
	private DesktopCommandPool() {
		super();
	}

	/**
	 * 返回管理池的静态句柄
	 * @return DesktopCommandPool实例
	 */
	public static DesktopCommandPool getInstance() {
		return DesktopCommandPool.selfHandle;
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
			invoker = new DesktopVMMemoryMissingInvoker((VMMemoryMissing) cmd);
		} else if (cmd.getClass() == MemoryMissing.class) {
			invoker = new DesktopMemoryMissingInvoker((MemoryMissing) cmd);
		} else if (cmd.getClass() == DiskMissing.class) {
			invoker = new DesktopDiskMissingInvoker((DiskMissing) cmd);
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
		return press(cmd, null);
	}
	
	/**
	 * 快速提交执行分布命令
	 * @param cmd 命令
	 * @param display 显示接口
	 * @return 返回真或者假
	 */
	public boolean press(Command cmd, MeetDisplay display) {
		EchoInvoker invoker = null;

		if (cmd.getClass() == Conduct.class) {
			invoker = new DesktopConductInvoker((Conduct) cmd);
		} else if (cmd.getClass() == Establish.class) {
			invoker = new DesktopEstablishInvoker((Establish) cmd);
		}
		// 流量测试
		else if (cmd.getClass() == ShiftSwarm.class) {
			invoker = new DesktopShiftSwarmInvoker((ShiftSwarm) cmd);
		}
		// 诊断测试
		else if (cmd.getClass() == ShiftAssertUser.class) {
			invoker = new DesktopShiftAssertUserInvoker((ShiftAssertUser) cmd);
		} else if (cmd.getClass() == ShiftAssertSchema.class) {
			invoker = new DesktopShiftAssertSchemaInvoker((ShiftAssertSchema) cmd);
		} else if (cmd.getClass() == ShiftAssertTable.class) {
			invoker = new DesktopShiftAssertTableInvoker((ShiftAssertTable) cmd);
		} 
		// 上传数据文件/下载数据块
		else if (cmd.getClass() == ShiftSingleImportEntity.class) {
			invoker = new DesktopShiftSingleImportEntityInvoker((ShiftSingleImportEntity) cmd);
		} else if (cmd.getClass() == ShiftSingleExportEntity.class) {
			invoker = new DesktopShiftSingleExportEntityInvoker((ShiftSingleExportEntity) cmd);
		}

		// 以上不成立，判断是FRONT登录转发命令
		if (invoker == null) {
			invoker = createLoginInvoker(cmd);
		}

		// 启动命令
		boolean success = (invoker != null);
		if (success) {
			if (Laxkit.isClassFrom(invoker, DesktopInvoker.class)) {
				((DesktopInvoker) invoker).setDisplay(display);
			}
			success = getInvokerPool().launch(invoker);
		}
		return success;
	}

}
