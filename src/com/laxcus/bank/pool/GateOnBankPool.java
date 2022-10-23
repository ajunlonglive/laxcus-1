/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.pool;

import java.util.*;

import com.laxcus.command.site.bank.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.gate.*;
import com.laxcus.util.*;

/**
 * GATE站点管理池。<br>
 * 
 * 保存GATE站点的注册数据
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class GateOnBankPool extends BankPool {

	/** 管理池句柄 **/
	private static GateOnBankPool selfHandle = new GateOnBankPool();

	/** GATE站点地址 -> GATE站点配置 **/
	private Map<Node, GateSite> mapSites = new TreeMap<Node, GateSite>();

	/**
	 * 构造默认的GATE站点管理池
	 */
	private GateOnBankPool() {
		super(SiteTag.GATE_SITE);
		// GATE节点的默认最大注册数目
		setMaxMembers(1);
	}

	/**
	 * 返回GATE站点管理池的静态句柄
	 * @return GATE站点管理池句柄
	 */
	public static GateOnBankPool getInstance() {
		return GateOnBankPool.selfHandle;
	}
	
	/**
	 * 根据主机地址产生一个主机编号，条件：<br>
	 * 1. 主机必须已经注册。<br>
	 * 2. 主机号如果已经分配，则使用它已经定义的。<br>
	 * 3. 主机号没有分配，从当前队列中选择一个。<br><br>
	 * 
	 * @param from 来源地址
	 * @return 返回大于等于0的编号，不成功返回-1。
	 */
	public int doSerial(Node from) {
		// 锁定处理
		super.lockSingle();
		try {
			GateSite site = mapSites.get(from);
			// 1. 主机没有注册，返回-1.
			if (site == null) {
				return GateSite.INVALID_NO;
			}
			// 2. 如果已经定义，返回这个编号
			if (site.isValidNo()) {
				return site.getNo();
			}
			// 3. 确定空置位置
			int size = mapSites.size();
			int[] array = new int[size];
			Iterator<Map.Entry<Node, GateSite>> iterator = mapSites.entrySet().iterator();
			for (int i = 0; iterator.hasNext(); i++) {
				array[i] = GateSite.INVALID_NO; // 默认是-1
				Map.Entry<Node, GateSite> entry = iterator.next();
				GateSite next = entry.getValue();
				int no = next.getNo();
				// 如果定义，设置它
				if (no > GateSite.INVALID_NO) {
					array[i] = no;
				}
			}
			// 4. 如果某个空置位置，用这个下标做为编号
			for (int i = 0; i < array.length; i++) {
				if (array[i] == GateSite.INVALID_NO) {
					site.setNo(i);
					return site.getNo();
				}
			}
			// 没有，取它的最大值，下标从0开始计算
			site.setNo(array.length);
			return site.getNo();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 出错，返回-1
		return GateSite.INVALID_NO;
	}

	/**
	 * 输出全部GATE站点地址
	 * @return GATE站点列表
	 */
	public List<Node> getNodes() {
		ArrayList<Node> array = new ArrayList<Node>();
		super.lockMulti();
		try {
			array.addAll(mapSites.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 输出全部GATE站点地址
	 * @return GATE站点列表
	 */
	public List<Site> getSites() {
		ArrayList<Site> array = new ArrayList<Site>();
		super.lockMulti();
		try {
			array.addAll(mapSites.values());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}
	
	/**
	 * 根据注册地址，查找匹配的站点
	 * @param node GATE站点注册地址
	 * @return Site实例，或者空指针
	 */
	@Override
	public Site find(Node node) {
		Laxkit.nullabled(node);
		super.lockMulti();
		try {
			return mapSites.get(node);
		} finally {
			super.unlockMulti();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#infuse(com.laxcus.site.Site)
	 */
	@Override
	protected boolean infuse(Site site) {
		// 判断许可证超时
		if (isLicenceTimeout()) {
			Logger.error(this, "infuse", "licence timeout!");
			return false;
		}
		
		GateSite hash = (GateSite) site;
		Node node = hash.getNode();
		
		// 1. 不允许重复注册
		boolean success = (mapSites.get(node) == null);
		// 如果节点不存在，判断最大数目；若存在，忽略它！
		if (success) {
			if (isMaxMembers(mapSites.size())) {
				Logger.error(this, "infuse", "member out! %d >= %d",mapSites.size(), getMaxMembers());
				return false;
			}
		}

		// 2. 保存管理员账号和刷新时间
		if (success) {
			// 保存注册地址
			mapSites.put(node, hash);
		}

		// 刷新注册时间
		if (success) {
			hash.refreshTime();
		}

		Logger.note(this, "infuse", success, "from %s", site);

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#effuse(com.laxcus.site.Node)
	 */
	@Override
	protected Site effuse(Node node) {
		// 删除注册地址
		GateSite site = mapSites.remove(node);

		// 判断站点存在
		boolean success = (site != null);

		Logger.debug(this, "effuse", success, "from %s", node);

		return site;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#transmit(com.laxcus.site.Site)
	 */
	@Override
	protected void transmit(Site site) {
		// 通知BANK监视站点/WATCH站点，一个GATE站点加入
		super.pushSite(site);
		
		// 节点有效，推送给ENTRANCE节点
		GateSite real = (GateSite) site;
		if (real.isValidNo()) {
			PushGateSite cmd = new PushGateSite(real.getPrivate(), real.getPublic(), real.getNo());
			ShiftPushGateSite shift = new ShiftPushGateSite(cmd);
			BankCommandPool.getInstance().admit(shift);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 通知BANK监视站点/WATCH站点，一个GATE站点退出
		super.dropSite(site);
		
		// 推送给ENTRANCE节点
		GateSite real = (GateSite) site;
		if (real.isValidNo()) {
			DropGateSite cmd = new DropGateSite(real.getPrivate(), real.getPublic(), real.getNo());
			ShiftDropGateSite shift = new ShiftDropGateSite(cmd);
			BankCommandPool.getInstance().admit(shift);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 通知TOP监视站点和WATCH站点，一个GATE站点故障被撤销，请求检查和处理
		super.destroySite(site);
		
		// 推送给ENTRANCE节点
		GateSite real = (GateSite) site;
		if (real.isValidNo()) {
			DropGateSite cmd = new DropGateSite(real.getPrivate(), real.getPublic(), real.getNo());
			ShiftDropGateSite shift = new ShiftDropGateSite(cmd);
			BankCommandPool.getInstance().admit(shift);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#iterator()
	 */
	@Override
	protected Map<Node, ? extends Site> iterator() {
		return mapSites;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapSites.clear();
	}

}