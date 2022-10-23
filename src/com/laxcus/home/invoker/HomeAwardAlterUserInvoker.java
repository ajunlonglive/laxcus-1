/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.access.user.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 授权修改账号密码调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 3/01/2012
 * @since laxcus 1.0
 */
public class HomeAwardAlterUserInvoker extends HomeInvoker {

	/**
	 * 构造授权修改账号密码调用器，指定命令
	 * @param cmd 授权修改账号密码命令
	 */
	public HomeAwardAlterUserInvoker(AwardAlterUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardAlterUser getCommand() {
		return (AwardAlterUser) super.getCommand();
	}

	/**
	 * 反馈处理结果
	 * @param success
	 */
	private void reply(boolean success) {
		AwardAlterUser cmd = getCommand();
		// 如果命令不需要反馈，退出
		if (cmd.isDirect()) {
			return;
		}
		User user = cmd.getUser();
		AlterUserProduct product = new AlterUserProduct(user.getUsername(), success);
		replyProduct(product);
	}

	/**
	 * 投递命令到CALL站点
	 */
	private void directToSlaves() {
		AwardAlterUser cmd = getCommand();
		User user = cmd.getUser();
		Siger username = user.getUsername();

		ArrayList<CommandItem> items = new ArrayList<CommandItem>();
		NodeSet set = CallOnHomePool.getInstance().findSites(username);
		if (set != null) {
			AwardAlterUser award = new AwardAlterUser(user);
			for (Node slave : set.show()) {
				CommandItem item = new CommandItem(slave, award);
				items.add(item);
			}
		}
		// 投递给CALL站点
		super.directTo(items, false);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardAlterUser cmd = getCommand();
		User user = cmd.getUser();

		// 修改本地的账号密码
		boolean success = StaffOnHomePool.getInstance().alter(user);
		// 修改成功，查找CALL站点，通知它们修改用户账号密码
		if (success) {
			directToSlaves();
		}

		// 向TOP反馈成功或者否
		reply(success);

		Logger.debug(this, "launch", success, "alter %s", user);

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