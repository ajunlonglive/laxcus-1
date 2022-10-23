/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import com.laxcus.build.pool.*;
import com.laxcus.command.task.*;
import com.laxcus.task.*;
import com.laxcus.task.archive.*;
import com.laxcus.task.establish.sift.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 删除云端应用调用器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2020
 * @since laxcus 1.0
 */
public class BuildDropTaskApplicationInvoker extends BuildInvoker {

	/**
	 * 构造删除云端应用
	 * @param cmd 删除应用命令
	 */
	public BuildDropTaskApplicationInvoker(DropTaskApplication cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropTaskApplication getCommand() {
		return (DropTaskApplication) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropTaskApplication cmd = getCommand();
		TaskSection section = cmd.getSection();
		int family = section.getFamily();
		Siger issuer = section.getIssuer();
		Naming software = section.getWare();
		
		// 非系统包，判断账号存在！
		if (!software.toString().matches(Sock.SYSTEM_REGEX)) {
			boolean exists = StaffOnBuildPool.getInstance().allow(issuer);
			if (!exists) {
				DropTaskApplicationProduct sub = new DropTaskApplicationProduct(0, 0);
				replyProduct(sub);
				return useful(false);
			}
		}
		
		// 目标任务池
		RemoteTaskPool taskPool = null;
		if (PhaseTag.isSift(family)) {
			taskPool = SiftTaskPool.getInstance();
		}

		// 有效!
		int rights = 0;
		if (taskPool != null) {
			boolean success = taskPool.hasTask(issuer, software);
			if (success) {
				boolean b = taskPool.drop(issuer, software);
				if(b) rights++;
			}
		}
		
		// 异步投递反馈
		DropTaskApplicationProduct product = new DropTaskApplicationProduct(rights, 0);
		boolean success = replyProduct(product);

		// 返回结果
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