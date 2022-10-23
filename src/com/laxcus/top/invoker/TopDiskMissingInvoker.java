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
 * 磁盘空间不足调用器。<BR>
 * 找到WATCH站点，发送给它！
 * 
 * @author scott.liang
 * @version 1.0 8/19/2019
 * @since laxcus 1.0
 */
public class TopDiskMissingInvoker extends TopInvoker {

	/**
	 * 构造磁盘空间不足调用器，指定命令
	 * @param cmd 磁盘空间不足
	 */
	public TopDiskMissingInvoker(DiskMissing cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DiskMissing getCommand() {
		return (DiskMissing) super.getCommand();
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
		DiskMissing cmd = getCommand();
		
		// 如果没有站点地址，是源于本地
		if (cmd.getSite() == null) {
			cmd.setSite(getLocal());
		}
		// 投递到指定的WATCH节点
		int count = directTo(slaves, cmd);

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