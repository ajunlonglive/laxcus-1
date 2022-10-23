/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.remote.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 站点最大虚拟机内存使用率限制命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 1/21/2020
 * @since laxcus 1.0
 */
public class MeetMostVMMemoryInvoker extends MeetInvoker {

	/**
	 * 构造站点最大虚拟机内存使用率限制命令调用器，指定命令
	 * @param cmd 站点最大虚拟机内存使用率限制命令
	 */
	public MeetMostVMMemoryInvoker(MostVMMemory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MostVMMemory getCommand() {
		return (MostVMMemory) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MostVMMemory cmd = getCommand();
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
	 * 最大虚拟机内存占比
	 */
	private boolean reset() {
		MostVMMemory cmd = getCommand();
		EchoTransfer.setMaxVMMemoryRate(cmd.getRate());
		// 打印结果
		print(true);
		
		return true;
	}
	
	/**
	 * 打印本地参数
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		// 设置标题
		createShowTitle(new String[] { "MOST-VMMEMORY/LOCAL" });

		ShowItem item = new ShowItem();
		String text = (success ? getXMLContent("MOST-VMMEMORY/LOCAL/SUCCESS")
				: getXMLContent("MOST-VMMEMORY/LOCAL/FAILED"));
		// 站点地址
		item.add(new ShowStringCell(0, text));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}


}