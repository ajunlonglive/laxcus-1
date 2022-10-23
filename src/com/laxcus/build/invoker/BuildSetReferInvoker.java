/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.build.pool.*;
import com.laxcus.command.refer.*;
import com.laxcus.log.client.*;

/**
 * 设置用户资源引用命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 4/12/2013
 * @since laxcus 1.0
 */
public class BuildSetReferInvoker extends BuildInvoker {

	/**
	 * 构造设置用户资源引用命令调用器，指定命令
	 * @param cmd 设置用户资源引用命令
	 */
	public BuildSetReferInvoker(SetRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetRefer getCommand() {
		return (SetRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetRefer cmd = getCommand();
		Refer refer = cmd.getRefer();
		boolean success = StaffOnBuildPool.getInstance().create(refer);

		// 延迟通知重新注册
		if (success) {
			getLauncher().checkin(false);
		}
		
		Logger.debug(this, "launch", success, "set %s", refer.getUsername());

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