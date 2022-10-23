/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.stub.transfer.*;
import com.laxcus.echo.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.data.*;
import com.laxcus.util.*;

/**
 * 复制DATA主节点数据块调用器
 * 
 * @author scott.liang
 * @version 1.0 6/15/2019
 * @since laxcus 1.0
 */
public class HomeCopyMasterMassInvoker extends HomeInvoker {

	/**
	 * 构造复制DATA主节点数据块调用器，指定命令
	 * @param cmd 复制DATA主节点数据块
	 */
	public HomeCopyMasterMassInvoker(CopyMasterMass cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CopyMasterMass getCommand() {
		return (CopyMasterMass) super.getCommand();
	}
	
	/**
	 * 判断有DATA主节点
	 * @param node 节点地址
	 * @return 返回真或者假
	 */
	private boolean hasMaster(Node node) {
		Site site = DataOnHomePool.getInstance().find(node);
		boolean success = (site != null && Laxkit.isClassFrom(site,
				DataSite.class));
		if (success) {
			DataSite ds = (DataSite) site;
			success = ds.isMaster();
		}
		return success;
	}

	/**
	 * 判断有DATA从节点
	 * @param node 节点地址
	 * @return 返回真或者假
	 */
	private boolean hasSlaves(List<Node> nodes) {
		int count = 0;
		for (Node node : nodes) {
			Site site = DataOnHomePool.getInstance().find(node);
			boolean success = (site != null && Laxkit.isClassFrom(site, DataSite.class));
			if (success) {
				DataSite ds = (DataSite) site;
				if (ds.isSlave()) count++; // 是从节点，统计值增1
			}
		}
		return count == nodes.size();
	}

	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CopyMasterMass cmd = this.getCommand();
		Node master = cmd.getMaster();
		List<Node> slaves = cmd.getSlaves();

		// 判断有匹配的地址
		boolean success = (hasMaster(master) && hasSlaves(slaves));
		// 返回错误！
		if (!success) {
			replyFault(Major.FAULTED, Minor.SITE_NOTFOUND);
			return useful(false);
		}
		
		// 命令发给DATA主节点，由它来主动发送数据块给从节点
		success = launchTo(master, cmd);
		
		Logger.debug(this, "launch", success, "submit to %s", master);
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return reflect();
	}

}
