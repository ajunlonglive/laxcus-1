/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.reload.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 释放节点内存命令调用器。
 * 只能释放本地内存！
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopReleaseMemoryInvoker extends DesktopInvoker {

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 重装加载安全规则命令
	 */
	public DesktopReleaseMemoryInvoker(ReleaseMemory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReleaseMemory getCommand() {
		return (ReleaseMemory) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReleaseMemory cmd = getCommand();
		// 判断和清除本地内存
		if (cmd.isLocal()) {
			clear();
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
	 * 清除本地内存
	 */
	private void clear() {
		System.gc();

		// 设置标题
		createShowTitle(new String[] { "RELEASE-MEMORY/LOCAL" });

		ShowItem item = new ShowItem();
		String text = getXMLContent("RELEASE-MEMORY/LOCAL/FREE") ;
		// 站点地址
		item.add(new ShowStringCell(0, text));
		// 保存单元
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}

}