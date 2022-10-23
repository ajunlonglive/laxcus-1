/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 授权修改账号密码调用器 <br>
 * 
 * BANK通知修改账号密码，TOP将命令转发给关联的HOME站点。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class TopAlterUserInvoker extends TopInvoker {

	/**
	 * 构造授权修改账号密码调用器，指定命令
	 * @param cmd 授权修改账号密码命令
	 */
	public TopAlterUserInvoker(AlterUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AlterUser getCommand() {
		return (AlterUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AlterUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		
		// 命令HOME站点修改账号密码
		AwardAlterUser award = new AwardAlterUser(cmd.getUser());

		// 查询HOME站点
		NodeSet set = HomeOnTopPool.getInstance().findSites(siger);

		// 检查参数
		List<Node> slaves = (set != null ? set.show() : null);
		boolean success = (slaves != null && slaves.size() > 0);
		if (success) {
			// 投递给HOME站点
			int count = directTo(slaves, award);
			success = (count > 0);
		}

		Logger.debug(this, "launch", success, "alter %s", siger);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}