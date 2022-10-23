/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.pool;

import java.util.*;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;

/**
 * BANK站点上的控制中心管理池。<br>
 * BANK站点下属的登录站点管理池由此派生。
 * 
 * @author scott.liang
 * @version 1.0 6/25/2018
 * @since laxcus 1.0
 */
public abstract class BankPool extends HubPool {

	/**
	 * 构造BANK站点的控制中心管理池，指定站点类型。
	 * @param siteFamily 站点类型
	 */
	protected BankPool(byte siteFamily) {
		super(siteFamily);
	}

	/**
	 * 向BANK监视器站点、BANK.WATCH站点推送一个新的站点
	 * @param cmd 广播站点命令
	 */
	private void push(PushSite cmd) {
		ArrayList<Node> array = new ArrayList<Node>();
		array.addAll(MonitorOnBankPool.getInstance().detail());
		array.addAll(WatchOnBankPool.getInstance().detail());
		// 从站点队列中，删除命令源站点地址
		array.remove(cmd.getSite());

		// 命令数组
		int size = array.size();
		if (size > 0) {
			Node[] sites = new Node[size];
			sites = array.toArray(sites);
			ShiftCastSite shift = new ShiftCastSite(cmd, sites);
			BankCommandPool.getInstance().admit(shift);
		}
		
		Logger.debug(this, "push", "%s by %s, site size:%s", cmd, cmd.getSite(), size);
	}


	/**
	 * 向TOP节点、BANK监视器节点、BANK.WATCH站点广播一个站点，杀掉它！
	 * @param cmd 广播站点命令
	 */
	private void kill(CastSite cmd) {
		ArrayList<Node> array = new ArrayList<Node>();
		// TOP站点
		Node hub = getHub();
		if (hub != null) {
			array.add(hub);
		}
		array.addAll(MonitorOnBankPool.getInstance().detail());
		array.addAll(WatchOnBankPool.getInstance().detail());
		// 从站点队列中，删除命令源站点地址
		array.remove(cmd.getSite());

		// 命令数组
		int size = array.size();
		if (size > 0) {
			Node[] sites = new Node[size];
			sites = array.toArray(sites);
			ShiftCastSite shift = new ShiftCastSite(cmd, sites);
			BankCommandPool.getInstance().admit(shift);
		}
		
		Logger.debug(this, "kill", "%s by %s, site size:%s", cmd, cmd.getSite(), size);
	}
	
	/**
	 * 向BANK监视站点和WATCH站点推送一个新的注册站点。
	 * @param site 注册站点
	 */
	protected void pushSite(Site site) {
		PushSite cmd = new PushSite(site.getNode());
		push(cmd);
	}

	/**
	 * 通知BANK监视站点和WATCH站点，撤销一个注册站点地址。撤销是正常的删除
	 * @param site 站点配置
	 */
	protected void dropSite(Site site) {
		DropSite cmd = new DropSite(site.getNode());
		kill(cmd);
	}

	/**
	 * 通知BANK监视器和WATCH站点，销毁注册站点。销毁是注册站点故障状态下的删除。
	 * @param site 站点配置
	 */
	protected void destroySite(Site site) {
		DestroySite cmd = new DestroySite(site.getNode());
		kill(cmd);
	}
}