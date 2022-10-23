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
 * 释放节点内存间隔命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopReleaseMemoryIntervalInvoker extends DesktopInvoker {

	/**
	 * 构造释放节点内存间隔命令调用器，指定命令
	 * @param cmd 释放节点内存间隔命令
	 */
	public DesktopReleaseMemoryIntervalInvoker(ReleaseMemoryInterval cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReleaseMemoryInterval getCommand() {
		return (ReleaseMemoryInterval) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ReleaseMemoryInterval cmd = getCommand();
		// 判断和清除本地内存
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
	 * 重置内存间隔时间
	 */
	private void reset() {
		// 释放间隔
		ReleaseMemoryInterval cmd = getCommand();
		getLauncher().setReleaseMemoryInterval(cmd.getInterval());

		// 设置标题
		createShowTitle(new String[] { "RELEASE-MEMORY-INTERVAL/LOCAL" });
		
		String time = doStyleTime(cmd.getInterval());
		// 如果是0值
		if (cmd.getInterval() < 1) {
			time = getXMLContent("RELEASE-MEMORY-INTERVAL/LOCAL/CANCELED");
		}
		
		ShowItem item = new ShowItem();
		// 站点地址
		item.add(new ShowStringCell(0, time));
		// 保存单元
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}

}