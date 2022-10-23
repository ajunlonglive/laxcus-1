/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.io.*;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 最少内存限制命令调用器。
 * 
 * 
 * @author scott.liang
 * @version 1.0 8/17/2019
 * @since laxcus 1.0
 */
public class MeetLeastMemoryInvoker extends MeetInvoker {

	/**
	 * 构造最少内存限制命令调用器，指定命令
	 * @param cmd 最少内存限制命令命令
	 */
	public MeetLeastMemoryInvoker(LeastMemory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public LeastMemory getCommand() {
		return (LeastMemory) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		LeastMemory cmd = getCommand();
		// 本地内存限制
		if (cmd.isLocal()) {
			reset();
		} else {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
		}
		// 投递到HUB站点
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
	/**
	 * 最小内存限制
	 */
	private boolean reset() {
		LeastMemory cmd = getCommand();

		boolean success = true;
		if (isLinux()) {
			if (cmd.isUnlimit()) {
				LinuxDevice.getInstance().setMemoryUnlimit();
			} else {
				LinuxDevice.getInstance().setMemoryLeast(cmd.getCapacity());
				LinuxDevice.getInstance().setMemoryLeastRate(cmd.getRate());
			}
			// 重新检测
			try {
				LinuxDevice.getInstance().checkMemory();
			} catch (IOException e) {
				Logger.error(e);
			}
		} else if (isWindows()) {
			if (cmd.isUnlimit()) {
				WindowsDevice.getInstance().setMemoryUnlimit();
			} else {
				WindowsDevice.getInstance().setMemoryLeast(cmd.getCapacity());
				WindowsDevice.getInstance().setMemoryLeastRate(cmd.getRate());
			}
			// 重新检测
			WindowsDevice.getInstance().checkMemory();
		} else {
			success = false; // 无确定！
		}
		
		// 打印结果
		print(success);
		
		return success;
	}
	
	/**
	 * 打印本地参数
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		// 设置标题
		createShowTitle(new String[] { "LEAST-MEMORY/LOCAL" });

		ShowItem item = new ShowItem();
		String text = (success ? getXMLContent("LEAST-MEMORY/LOCAL/SUCCESS")
				: getXMLContent("LEAST-MEMORY/LOCAL/FAILED"));
		// 站点地址
		item.add(new ShowStringCell(0, text));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}


}