/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.home.pool.*;
import com.laxcus.site.*;

/**
 * 删除注册用户给WATCH站点调用器
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class HomeDropRegisterMemberInvoker extends HomeInvoker {

	/**
	 * 构造删除注册用户给WATCH站点，设置命令
	 * @param cmd 删除注册用户给WATCH站点
	 */
	public HomeDropRegisterMemberInvoker(DropRegisterMember cmd) {
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
		
		// 转发给TOP节点，全部WATCH站点
		ArrayList<Node> slaves = new ArrayList<Node>();
		slaves.add(getHub()); // TOP节点
		slaves.addAll(WatchOnHomePool.getInstance().detail());
		// 投递给上级的TOP节点，和当前WATCH节点
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