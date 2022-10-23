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
 * 检索LAXCUS集群成员调用器
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class HomeAskClusterMemberInvoker extends HomeInvoker {

	/**
	 * 构造检索LAXCUS集群成员调用器，指定命令
	 * @param cmd 检索LAXCUS集群成员调用器
	 */
	public HomeAskClusterMemberInvoker(AskClusterMember cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AskClusterMember getCommand() {
		return (AskClusterMember) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AskClusterMember cmd = getCommand();
		AskClusterMember sub = cmd.duplicate();
		// 如果没有定义WATCH节点，在此定义！
		if (sub.getRemote() == null) {
			sub.setRemote(cmd.getSourceSite());
		}

		// 找到全部CALL/WORK/BUILD/DATA节点，投递给它们
		ArrayList<Node> array = new ArrayList<Node>();
		array.addAll(CallOnHomePool.getInstance().detail());
		array.addAll(WorkOnHomePool.getInstance().detail());
		array.addAll(DataOnHomePool.getInstance().detail());
		array.addAll(BuildOnHomePool.getInstance().detail());
		// 投递给全部节点
		directTo(array, sub);

		return useful();
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
