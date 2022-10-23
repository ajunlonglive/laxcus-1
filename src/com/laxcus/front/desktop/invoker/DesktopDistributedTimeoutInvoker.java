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
 * 设置分布处理超时调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopDistributedTimeoutInvoker extends DesktopInvoker {

	/**
	 * 构造设置分布处理超时调用器，指定命令
	 * @param cmd 设置分布处理超时
	 */
	public DesktopDistributedTimeoutInvoker(DistributedTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DistributedTimeout getCommand() {
		return (DistributedTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DistributedTimeout cmd = getCommand();

		// 如果不是本地，显示权限不足
		if (!cmd.isLocal()) {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
			return false;
		}
		// 拒绝接受命令模式
		if (cmd.isCommand()) {
			faultX(FaultTip.SYSTEM_DENIED);
			return false;
		}

		// 调用器超时时间
		long interval = cmd.getInterval();
//		getInvokerPool().setMemberTimeout(interval);
		EchoTransfer.setInvokerTimeout(interval);
		print(interval);

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
	 * 打印时间
	 * @param interval
	 */
	private void print(long interval) {
		createShowTitle(new String[] { "DISTRIBUTED-TIMEOUT-LOCAL/TIME" });
		String text = (interval < 1 ? getXMLAttribute("DISTRIBUTED-TIMEOUT-LOCAL/TIME/unlimit")
				: doStyleTime(interval));

		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, text));
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}
}