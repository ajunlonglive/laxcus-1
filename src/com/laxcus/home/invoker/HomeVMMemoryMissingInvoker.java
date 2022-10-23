/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.missing.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.site.build.*;
import com.laxcus.site.data.*;
import com.laxcus.site.work.*;
import com.laxcus.util.*;

/**
 * 虚拟机内存空间不足调用器。<BR>
 * 找到WATCH站点，发送给它！
 * 
 * @author scott.liang
 * @version 1.0 10/30/2019
 * @since laxcus 1.0
 */
public class HomeVMMemoryMissingInvoker extends HomeInvoker {

	/**
	 * 构造虚拟机内存空间不足调用器，指定命令
	 * @param cmd 虚拟机内存空间不足
	 */
	public HomeVMMemoryMissingInvoker(VMMemoryMissing cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public VMMemoryMissing getCommand() {
		return (VMMemoryMissing) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		List<Node> slaves = WatchOnHomePool.getInstance().detail();
		if (slaves.isEmpty()) {
			Logger.warning(this, "launch", "not found Watch Site!");
			return useful(false);
		}
		// 本地地址
		VMMemoryMissing cmd = getCommand();

		// 如果没有定义来源地址，是Home节点自己发的。
		VMMemoryMissing sub = (cmd.getSite() == null ? 
				new VMMemoryMissing(getLocal()) : cmd.duplicate());

		// 投递到指定的WATCH节点
		int count = directTo(slaves, sub);

		// 判断成功
		boolean success = (count > 0);
		
		// 如果命令来自DATA/WORK/BUILD站点，生成SelectFieldToCall反馈给DATA/WORK/BUILD，
		// DATA/WORK/BUILD站点更新PushDataField/PushWorkField/PushBuildFile给CALL节点。
		check(cmd.getSourceSite());

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
	
	/**
	 * 检查来自DATA/WORK/BUILD
	 * @param node 节点来源地址
	 */
	private void check(Node node) {
		// 如果没有定义，是本地HOME节点
		if (node == null) {
			return;
		}
		// 判断是DATA/WORK/BUILD的一种，投递给CALL节点
		if (node.isData()) {
			pushDataToCall(node);
		} else if (node.isWork()) {
			pushWorkToCall(node);
		} else if (node.isBuild()) {
			pushBuildToCall(node);
		}
	}

	/**
	 * 通知DATA站点，更新元数据给关联的CALL节点
	 * @param slave DATA节点
	 */
	private void pushDataToCall(Node slave) {
		DataSite site = (DataSite) DataOnHomePool.getInstance().find(slave);
		if (site == null) {
			return;
		}
		List<Siger> users = site.getUsers();
		if (users != null && users.size() > 0) {
			CallOnHomePool.getInstance().enroll(slave, users);
			
			Logger.debug(this, "pushDataToCall", "push %d users to %s", users.size(), slave );
		}
	}
	
	/**
	 * 通知WORK站点，更新元数据给关联的CALL节点
	 * @param slave WORK节点
	 */
	private void pushWorkToCall(Node slave) {
		WorkSite site = (WorkSite) WorkOnHomePool.getInstance().find(slave);
		if (site == null) {
			return;
		}

		List<Siger> users = site.getUsers();
		if (users != null && users.size() > 0) {
			CallOnHomePool.getInstance().enroll(slave, users);
			Logger.debug(this, "pushWorkToCall", "push %d users to %s", users.size(), slave);
		}
	}

	/**
	 * 通知BUILD站点，更新元数据给关联的CALL节点
	 * @param slave BUILD站点
	 */
	private void pushBuildToCall(Node slave) {
		BuildSite site = (BuildSite) BuildOnHomePool.getInstance().find(slave);
		if (site == null) {
			return;
		}

		List<Siger> users = site.getUsers();
		if (users != null && users.size() > 0) {
			CallOnHomePool.getInstance().enroll(slave, users);
			Logger.debug(this, "pushBuildToCall", "push %d users to %s", users.size(), slave);
		}
	}
}
