/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.fixp.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;

/**
 * 设置FIXP本地密文超时调用器。<br>
 * 
 * 本处密文设置只在FRONT站点起作用，不影响集群其它节点。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopCipherTimeoutInvoker extends DesktopInvoker {

	/**
	 * 构造设置FIXP本地密文超时调用器，指定命令
	 * @param cmd 设置FIXP本地密文超时
	 */
	public DesktopCipherTimeoutInvoker(CipherTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CipherTimeout getCommand() {
		return (CipherTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CipherTimeout cmd = getCommand();
		
		// 如果不是本地，显示权限不足
		if (!cmd.isLocal()) {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
			return false;
		}

		long interval = cmd.getInterval();
		
		// 设置本地时间
		Cipher.setTimeout(interval);
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
		createShowTitle(new String[] { "LOCAL-CIPHER-TIMEOUT/TIME" });
		String text = doStyleTime(interval);

		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, text));
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}
}
