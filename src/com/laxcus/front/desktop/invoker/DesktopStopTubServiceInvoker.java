/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.tub.*;
import com.laxcus.log.client.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * 停止边缘计算服务调用器。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopStopTubServiceInvoker extends DesktopInvoker {

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 重装加载安全规则命令
	 */
	public DesktopStopTubServiceInvoker(StopTubService cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public StopTubService getCommand() {
		return (StopTubService) super.getCommand();
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

		StopTubService cmd = getCommand();
		long processId = cmd.getProcessId();
		String args = cmd.getArguments();

		TubStopResult result = null;
		// 停止它！
		try {
			result = TubPool.getInstance().stop(processId, args);
		} catch (TubException e) {
			Logger.error(e);
			fault(e.getMessage());
			return useful(false);
		}

		// 判断成功或者否
		boolean success = (result != null);
		if (success) {
			print(result);
		} else {
			faultX(FaultTip.NOTFOUND_X, String.format("%d", processId));
		}

		return useful(success);
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
	private void print(TubStopResult e) {
		// 设置标题
		createShowTitle(new String[] { "STOP-TUB-SERVICE/STATUS",
				"STOP-TUB-SERVICE/CODE", "STOP-TUB-SERVICE/NAMING",
				"STOP-TUB-SERVICE/HOST", "STOP-TUB-SERVICE/PID" });

		ShowItem item = new ShowItem();
		// 图标
		item.add(createConfirmTableCell(0, e.isSuccessful()));
		// 数据表
		item.add(new ShowIntegerCell(1, e.getStatus()));
		// 站点地址
		Naming naming = e.getNaming();
		item.add(new ShowStringCell(2, (naming != null ? naming.toString() : "")));
		// 主机地址
		String host = (e.getHost() == null ? "" : e.getHost().toString());
		item.add(new ShowStringCell(3, host));
		// 返回码
		item.add(new ShowLongCell(4, e.getProcessId()));
		// 显示
		addShowItem(item);
		// 输出全部记录
		flushTable();
	}

}