/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.missing.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 用户虚拟空间耗尽调用器。<BR>
 * 找到WATCH站点，发送给它！
 * 
 * @author scott.liang
 * @version 1.0 10/20/2019
 * @since laxcus 1.0
 */
public class HomeFrontFullInvoker extends HomeInvoker {

	/**
	 * 构造用户虚拟空间耗尽调用器，指定命令
	 * @param cmd 用户虚拟空间耗尽
	 */
	public HomeFrontFullInvoker(FrontFull cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FrontFull getCommand() {
		return (FrontFull) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		List<Node> slaves = WatchOnHomePool.getInstance().detail();
		if (slaves.isEmpty()) {
			Logger.warning(this, "launch", "not found Watch Site!");
			return useful(false);
		}
		// 本地地址
		FrontFull cmd = getCommand();

		// 如果没有定义来源地址，是Home节点自己发的。
		FrontFull sub = (cmd.getSite() == null ? 
				new FrontFull(getLocal()) : cmd.duplicate());

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