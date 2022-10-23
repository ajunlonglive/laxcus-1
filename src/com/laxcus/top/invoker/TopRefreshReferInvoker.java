/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.refer.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 构造刷新资源引用调用器。<br>
 * TOP站点将命令投递给关联的HOME站点。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class TopRefreshReferInvoker extends TopInvoker {

	/**
	 * 构造刷新资源引用调用器，指定命令
	 * @param cmd 刷新资源引用
	 */
	public TopRefreshReferInvoker(RefreshRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshRefer getCommand() {
		return (RefreshRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshRefer cmd = getCommand();
		Siger siger = cmd.getSiger();

		ArrayList<Node> slaves = new ArrayList<Node>();

		// 找到关联的HOME站点
		NodeSet set = HomeOnTopPool.getInstance().findSites(siger);
		if (set != null) {
			slaves.addAll(set.show());
		}

		// 发送到TOP站点，不需要回应
		boolean success = (slaves.size() > 0);
		if (success) {
			directTo(slaves, cmd);
		}

		Logger.debug(this, "launch", success, "%s submit sites: %d", cmd, slaves.size());

		// 退出
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
