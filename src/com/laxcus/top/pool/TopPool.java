/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.pool;

import java.util.*;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;

/**
 * TOP站点下的控制中心管理池。<br>
 * TOP站点下属的登录站点管理池由此派生。
 * 
 * @author scott.liang
 * @version 1.0 11/5/2011
 * @since laxcus 1.0
 */
public abstract class TopPool extends HubPool {

	/**
	 * 构造TOP站点的控制中心管理池，指定站点类型。
	 * @param siteFamily 站点类型
	 */
	public TopPool(byte siteFamily) {
		super(siteFamily);
	}

	/**
	 * 向TOP监视器站点和WATCH站点广播一个站点
	 * @param cmd 广播站点命令
	 */
	private void broadcast(CastSite cmd) {
		ArrayList<Node> array = new ArrayList<Node>();
		array.addAll(MonitorOnTopPool.getInstance().detail());
		array.addAll(WatchOnTopPool.getInstance().detail());
		// 被发送的站点地址中，过滤掉命令来源站点
		array.remove(cmd.getSite());

		int size = array.size();
		if (size > 0) {
			Node[] sites = new Node[size];
			sites = array.toArray(sites);
			ShiftCastSite shift = new ShiftCastSite(cmd, sites);
			TopCommandPool.getInstance().admit(shift);
		}
		
		Logger.debug(this, "broadcast", "%s by %s, site size:%s", cmd, cmd.getSite(), size);
	}

	/**
	 * 向TOP监视站点和WATCH站点推送一个新的注册站点。
	 * @param site 注册站点
	 */
	protected void pushSite(Site site) {
		PushSite cmd = new PushSite(site.getNode());
		broadcast(cmd);
	}

	/**
	 * 通知TOP监视站点和WATCH站点，撤销一个注册站点地址。撤销是正常的删除
	 * @param site 站点配置
	 */
	protected void dropSite(Site site) {
		DropSite cmd = new DropSite(site.getNode());
		broadcast(cmd);
	}

	/**
	 * 通知TOP监视器和WATCH站点，销毁注册站点。销毁是注册站点故障状态下的删除。
	 * @param site
	 */
	protected void destroySite(Site site) {
		DestroySite cmd = new DestroySite(site.getNode());
		broadcast(cmd);
	}

}