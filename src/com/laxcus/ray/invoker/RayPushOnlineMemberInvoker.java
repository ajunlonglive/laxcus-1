/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.site.watch.*;

/**
 * 推送注册用户给WATCH站点调用器
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class RayPushOnlineMemberInvoker extends RayCastElementInvoker {

	/**
	 * 构造推送注册用户给WATCH站点，设置命令
	 * @param cmd 推送注册用户给WATCH站点
	 */
	public RayPushOnlineMemberInvoker(PushOnlineMember cmd) {
		super(cmd);
		setSound(false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public PushOnlineMember getCommand() {
		return (PushOnlineMember) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		PushOnlineMember cmd = getCommand();
		pushOnlineMember(cmd.list());
		
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}