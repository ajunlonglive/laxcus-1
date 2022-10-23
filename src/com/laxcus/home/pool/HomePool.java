/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.pool;

import java.util.*;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.build.*;
import com.laxcus.site.call.*;
import com.laxcus.site.data.*;
import com.laxcus.site.work.*;

/**
 * HOME站点上的控制中心管理池。<br>
 * HOME站点下属的登录站点管理池由此派生。
 * 
 * @author scott.liang
 * @version 1.0 11/5/2011
 * @since laxcus 1.0
 */
public abstract class HomePool extends HubPool {

	/**
	 * 构造HOME站点的控制中心管理池，指定站点类型。
	 * @param siteFamily 站点类型
	 */
	protected HomePool(byte siteFamily) {
		super(siteFamily);
	}
	
	/**
	 * 导入新的节点
	 * @param cmd
	 */
	private void push(PushSite cmd) {
		ArrayList<Node> array = new ArrayList<Node>();
		array.addAll(MonitorOnHomePool.getInstance().detail());
		array.addAll(WatchOnHomePool.getInstance().detail());
		// 从站点队列中，删除命令源站点地址
		array.remove(cmd.getSite());

		int size = array.size();
		if (size > 0) {
			Node[] sites = new Node[size];
			sites = array.toArray(sites);
			ShiftCastSite shift = new ShiftCastSite(cmd, sites);
			HomeCommandPool.getInstance().admit(shift);
		}
		
		Logger.debug(this, "push", "%s by %s, site size:%s", cmd, cmd.getSite(), size);
	}

	/**
	 * 向TOP节点、HOME监视器站点、HOME.WATCH站点广播一个站点，杀掉一个节点
	 * @param cmd 被广播站点命令
	 */
	private void kill(CastSite cmd) {
		ArrayList<Node> array = new ArrayList<Node>();
		// 同时通知父类的TOP节点
		Node hub = getHub();
		if (hub != null) {
			array.add(hub);
		}
		// 通知HOME备份节点和WATCH节点
		array.addAll(MonitorOnHomePool.getInstance().detail());
		array.addAll(WatchOnHomePool.getInstance().detail());
		// 从站点队列中，删除命令源站点地址
		array.remove(cmd.getSite());

		int size = array.size();
		if (size > 0) {
			Node[] sites = new Node[size];
			sites = array.toArray(sites);
			ShiftCastSite shift = new ShiftCastSite(cmd, sites);
			HomeCommandPool.getInstance().admit(shift);
		}
		
		Logger.debug(this, "kill", "%s by %s, site size:%s", cmd, cmd.getSite(), size);
	}

	/**
	 * 向HOME监视站点和WATCH站点推送一个新的注册站点。
	 * @param site 注册站点
	 */
	protected void pushSite(Site site) {
		PushSite cmd = new PushSite(site.getNode());
		push(cmd);
	}

	/**
	 * 收集失效的对象
	 * @param cmd
	 * @param site
	 */
	private void disabled(DisableSite cmd, Site site) {
		// 取出内部参数
		if (site.getClass() == DataSite.class) {
			DataSite ds = (DataSite) site;
			for(DataMember e : ds.getMembers()) {
				DisableMember member = new DisableMember(e.getSiger());
				member.addAll(e.getSpaces());
				cmd.add(member);
			}
		} else if (site.getClass() == WorkSite.class) {
			WorkSite ws = (WorkSite) site;
			for(WorkMember e : ws.getMembers()) {
				DisableMember member = new DisableMember(e.getSiger());
				member.addAll( e.getTables());
				cmd.add(member);
			}
		} else if (site.getClass() == CallSite.class) {
			CallSite cs = (CallSite) site;
			for (CallMember e : cs.getMembers()) {
				DisableMember member = new DisableMember(e.getSiger());
				member.addAll(e.getTables());
				cmd.add(member);
			}
		} else if (site.getClass() == BuildSite.class) {
			BuildSite bs = (BuildSite) site;
			for(BuildMember e : bs.getMembers()) {
				DisableMember member = new DisableMember(e.getSiger());
				member.addAll(e.getTables());
				cmd.add(member);
			}
		}
	}

	/**
	 * 通知HOME监视站点和WATCH站点，撤销一个注册站点地址。撤销是正常的删除
	 * @param site 站点配置
	 */
	protected void dropSite(Site site) {
		DropSite cmd = new DropSite(site.getNode());
		disabled(cmd, site);
		kill(cmd);
	}

	/**
	 * 通知HOME监视器和WATCH站点，销毁注册站点。销毁是注册站点故障状态下的删除。
	 * @param site 站点配置
	 */
	protected void destroySite(Site site) {
		DestroySite cmd = new DestroySite(site.getNode());
		disabled(cmd, site);
		kill(cmd);
	}
}