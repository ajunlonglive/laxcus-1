/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.top.pool.*;
import com.laxcus.site.*;

/**
 * 删除注册用户给WATCH站点调用器
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class TopDropRegisterMemberInvoker extends TopInvoker {

	/**
	 * 构造删除注册用户给WATCH站点，设置命令
	 * @param cmd 删除注册用户给WATCH站点
	 */
	public TopDropRegisterMemberInvoker(DropRegisterMember cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropRegisterMember getCommand() {
		return (DropRegisterMember) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropRegisterMember cmd = getCommand();
		
		// 转发给全部WATCH站点
		List<Node> slaves = WatchOnTopPool.getInstance().detail();
		directTo(slaves, cmd);
		
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