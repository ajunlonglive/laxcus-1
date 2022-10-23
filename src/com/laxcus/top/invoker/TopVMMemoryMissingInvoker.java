/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.missing.*;
import com.laxcus.top.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 虚拟机内存空间不足调用器。<BR>
 * 找到WATCH站点，发送给它！
 * 
 * @author scott.liang
 * @version 1.0 10/30/2019
 * @since laxcus 1.0
 */
public class TopVMMemoryMissingInvoker extends TopInvoker {

	/**
	 * 构造虚拟机内存空间不足调用器，指定命令
	 * @param cmd 虚拟机内存空间不足
	 */
	public TopVMMemoryMissingInvoker(VMMemoryMissing cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public VMMemoryMissing getCommand() {
		return (VMMemoryMissing) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		List<Node> slaves = WatchOnTopPool.getInstance().detail();
		if (slaves.isEmpty()) {
			Logger.warning(this, "launch", "not found Watch Site!");
			return useful(false);
		}
		// 本地地址
		VMMemoryMissing cmd = getCommand();

		// 如果没有定义来源地址，是Top节点自己发的。
		VMMemoryMissing sub = (cmd.getSite() == null ? 
				new VMMemoryMissing(getLocal()) : cmd.duplicate());

		// 投递到指定的WATCH节点
		int count = directTo(slaves, sub);

		// 判断成功
		boolean success = (count > 0);

		Logger.debug(this, "launch", success, "direct count %d", count);

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