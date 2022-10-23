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
 * 启动边缘计算服务调用器。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopRunTubServiceInvoker extends DesktopInvoker {

	/**
	 * 构造释放节点内存命令调用器，指定命令
	 * @param cmd 重装加载安全规则命令
	 */
	public DesktopRunTubServiceInvoker(RunTubService cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RunTubService getCommand() {
		return (RunTubService) super.getCommand();
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

		RunTubService cmd = getCommand();
		Naming naming = cmd.getNaming();
		String args = cmd.getArguments();
		
		// 启动参数
		TubStartResult result = null;
		try {
			result = TubPool.getInstance().launch(naming, args);
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
			faultX(FaultTip.NOTFOUND_X, naming); // 权限不足
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
	private void print(TubStartResult e) {
		// 设置标题
		createShowTitle(new String[] { "RUN-TUB-SERVICE/STATUS",
				"RUN-TUB-SERVICE/CODE", "RUN-TUB-SERVICE/NAMING",
				"RUN-TUB-SERVICE/HOST", "RUN-TUB-SERVICE/PID" });

		ShowItem item = new ShowItem();
		// 图标
		item.add(createConfirmTableCell(0, e.isSuccessful()));
		// 数据表
		item.add(new ShowIntegerCell(1, e.getStatus()));
		// 站点地址
		item.add(new ShowStringCell(2, e.getNaming()));
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