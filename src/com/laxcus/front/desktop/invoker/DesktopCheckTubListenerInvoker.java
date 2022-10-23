/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.command.tub.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.net.*;
import com.laxcus.util.tip.*;

/**
 * 检测边缘服务监听调用器。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopCheckTubListenerInvoker extends DesktopInvoker {

	/**
	 * 构造检测边缘服务监听调用器，指定命令
	 * @param cmd 检测边缘服务监听
	 */
	public DesktopCheckTubListenerInvoker(CheckTubListener cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckTubListener getCommand() {
		return (CheckTubListener) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 管理员不允许执行这种操作
		if (isAdministrator()) {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
			return useful(false);
		}
		
		// 监听地址
		SocketHost local = getLauncher().getTubHost();
		ArrayList<SocketHost> array = new ArrayList<SocketHost>();
		// 有效！
		if (local != null) {
			array.add(local);

			// 如果是通配符...
			if (local.getAddress().isAnyLocalAddress()) {
				Address[] addresses = Address.locales();
				for (Address sub : addresses) {
					boolean success = (sub.isSiteLocalAddress()
							|| sub.isWideAddress() || sub.isLoopbackAddress());
					if (success) {
						SocketHost host = new SocketHost(local.getFamily(), sub, local.getPort());
						array.add(host);
					}
				}
			}
		}

		print(array);

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
	 * 打印结果
	 * @param e
	 */
	private void print(java.util.List<SocketHost> a) {
		// 设置标题
		createShowTitle(new String[] { "CHECK-TUB-LISTEN/IP",
				"CHECK-TUB-LISTEN/PORT" });

		for(SocketHost host : a) {
			ShowItem item = new ShowItem();
			// IP和端口
			item.add(new ShowStringCell(0, host.getAddress().toString()));
			item.add(new ShowIntegerCell(1, host.getPort()));
			// 显示
			addShowItem(item);
		}
		
		// 输出全部记录
		flushTable();
	}


}