/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import java.util.*;

import com.laxcus.command.tub.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.net.*;
import com.laxcus.util.tip.*;

/**
 * 打印边缘计算服务调用器。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2019
 * @since laxcus 1.0
 */
public class MeetPrintTubServiceInvoker extends MeetInvoker {

	/**
	 * 构造打印边缘计算服务调用器，指定命令
	 * @param cmd 打印边缘计算服务
	 */
	public MeetPrintTubServiceInvoker(PrintTubService cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PrintTubService getCommand() {
		return (PrintTubService) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 管理员不允许执行这种操作
		if (isAdministrator()) {
			faultX(FaultTip.PERMISSION_MISSING); // 权限不足
			return useful(false);
		}

		PrintTubService cmd = getCommand();
		Naming[] names = cmd.getNamings();

		// 找到结果
		List<TubToken> tubs = TubPool.getInstance().findTubs(names);
		print(tubs);
		
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
	private void print(List<TubToken> tubs) {
		// 设置标题
		createShowTitle(new String[] { "PRINT-TUB-SERVICE/NAMING",
				"PRINT-TUB-SERVICE/PID", "PRINT-TUB-SERVICE/HOST",
				"PRINT-TUB-SERVICE/RUNTIME" });
		// 显示结果
		for (TubToken e : tubs) {
			ShowItem item = new ShowItem();
			// 命名
			item.add(new ShowStringCell(0, e.getNaming()));
			// 进入ID
			item.add(new ShowLongCell(1, e.getId()));

			// 主机地址
			SocketHost host = e.getHost();
			String str = (host == null ? "" : host.toString());
			item.add(new ShowStringCell(2, str));

			// 运行时间
			String time = super.doStyleTime(e.getRunTime());
			item.add(new ShowStringCell(3, time));

			// 显示
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

}