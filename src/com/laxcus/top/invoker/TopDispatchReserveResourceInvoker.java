/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.reserve.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.top.pool.*;
import com.laxcus.site.*;

/**
 * DispatchReserveResource命令调用器
 * 
 * 这个调用器运行在TOP主站点，向TOP镜像站点发送“CommitReserveResource”命令
 * 
 * @author scott.liang
 * @version 1.0 7/21/2014
 * @since laxcus 1.0
 */
public class TopDispatchReserveResourceInvoker extends TopInvoker {

	/**
	 * 构造DispatchReserveResource命令调用器，指定命令
	 * @param cmd - DispatchReserveResource命令
	 */
	public TopDispatchReserveResourceInvoker(DispatchReserveResource cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DispatchReserveResource getCommand() {
		return (DispatchReserveResource)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DispatchReserveResource cmd = getCommand();

		// 生产新的命令
		CommitReserveResource commit = new CommitReserveResource(cmd.getResource());
		// TOP管理节点，取出自己下属的监视器站点
		List<Node> nodes = MonitorOnTopPool.getInstance().getNodes();

		// 生成命令处理单元
		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (Node node : nodes) {
			CommandItem item = new CommandItem(node, commit);
			array.add(item);
		}

		// 投递到目标站点，不等待反馈
		int count = directTo(array, false);

		boolean success = (count > 0);

		Logger.debug(this, "launch", success, "send %s to mirror sites:%d, sended site:%d",
			cmd.getResource(), array.size(), count);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
