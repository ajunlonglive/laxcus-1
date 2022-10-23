/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.remote.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 站点最大CPU使用率限制命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopMostCPUInvoker extends DesktopInvoker {

	/**
	 * 构造站点最大CPU使用率限制命令调用器，指定命令
	 * @param cmd 站点最大CPU使用率限制命令
	 */
	public DesktopMostCPUInvoker(MostCPU cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MostCPU getCommand() {
		return (MostCPU) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MostCPU cmd = getCommand();
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
	 * 最大CPU占比
	 */
	private boolean reset() {
		MostCPU cmd = getCommand();
		EchoTransfer.setMaxCpuRate(cmd.getRate());
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
		createShowTitle(new String[] { "MOST-CPU/LOCAL" });

		ShowItem item = new ShowItem();
		String text = (success ? getXMLContent("MOST-CPU/LOCAL/SUCCESS")
				: getXMLContent("MOST-CPU/LOCAL/FAILED"));
		// 站点地址
		item.add(new ShowStringCell(0, text));
		// 保存单元
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}


}