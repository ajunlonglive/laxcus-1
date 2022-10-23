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
import com.laxcus.site.account.*;
import com.laxcus.util.*;

/**
 * ACCOUNT站点管理池。<br>
 * 
 * 保存ACCOUNT站点的注册数据
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class AccountOnBankPool extends BankPool {

	/** 管理池句柄 **/
	private static AccountOnBankPool selfHandle = new AccountOnBankPool();

	/** ACCOUNT站点地址 -> ACCOUNT站点配置 **/
	private Map<Node, AccountSite> mapSites = new TreeMap<Node, AccountSite>();

	/** 站点编号 -> 站点实例 **/
	private Map<java.lang.Integer, AccountSite> mapSerials = new TreeMap<java.lang.Integer, AccountSite>();

	/**
	 * 构造默认的ACCOUNT站点管理池
	 */
	private AccountOnBankPool() {
		super(SiteTag.ACCOUNT_SITE);
		// ACCOUNT节点的默认最大注册数目
		setMaxMembers(1);
	}

	/**
	 * 返回ACCOUNT站点管理池的静态句柄
	 * @return ACCOUNT站点管理池句柄
	 */
	public static AccountOnBankPool getInstance() {
		return AccountOnBankPool.selfHandle;
	}

	/**
	 * 根据用户签名，定位ACCOUNT主机地址
	 * @param siger 用户签名
	 * @return 返回ACCOUNT主机地址，发生故障返回空指针
	 */
	public Node locate(Siger siger) {
		super.lockMulti();
		try {
			int size = mapSerials.size();
			// 计算模值
			if (size > 0) {
				int no = siger.mod(size);
				// 根据模值，返回对应的ACCOUNT站点地址
				AccountSite site = mapSerials.get(no);
				if (site != null) {
					return site.getNode();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}
	
	/**
	 * 根据主机地址产生一个主机编号，条件：
	 * 1. 主机必须已经注册。
	 * 2. 主机号如果已经分配，则使用它已经定义的。
	 * 3. 主机号没有分配，从当前队列中选择一个。
	 * 
	 * @param from 来源地址
	 * @return 返回大于等于0的编号，不成功返回-1。
	 */
	public int doSerial(Node from) {
		// 锁定处理
		super.lockSingle();
		try {
			AccountSite site = mapSites.get(from);
			// 1. 主机没有注册，返回-1.
			if (site == null) {
				return AccountSite.INVALID_NO;
			}
			// 2. 如果已经定义，返回这个编号
			if (site.isValidNo()) {
				return site.getNo();
			}
			// 3. 确定空置位置
			int size = mapSites.size();
			int[] array = new int[size];
			Iterator<Map.Entry<Node, AccountSite>> iterator = mapSites.entrySet().iterator();
			for (int i = 0; iterator.hasNext(); i++) {
				array[i] = AccountSite.INVALID_NO; // 默认是-1
				Map.Entry<Node, AccountSite> entry = iterator.next();
				AccountSite next = entry.getValue();
				int no = next.getNo();
				// 如果定义，设置它
				if (no > AccountSite.INVALID_NO) {
					array[i] = no;
				}
			}
			// 4. 如果某个空置位置，用这个下标做为编号
			for (int i = 0; i < array.length; i++) {
				if (array[i] == AccountSite.INVALID_NO) {
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
		return AccountSite.INVALID_NO;
	}

	/**
	 * 输出全部ACCOUNT站点地址
	 * @return ACCOUNT站点列表
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
	 * 输出全部ACCOUNT站点地址
	 * @return ACCOUNT站点列表
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
	 * @param node ACCOUNT站点注册地址
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
		
		AccountSite account = (AccountSite) site;
		Node node = account.getNode();
		
		// 1. 不允许重复注册
		boolean success = (mapSites.get(node) == null);
		
		// 如果节点不存在，判断最大数目；若存在，忽略它！
		if (success) {
			if (isMaxMembers(mapSites.size())) {
				Logger.error(this, "infuse", "member out! %d >= %d", mapSites.size(), getMaxMembers());
				return false;
			}
		}

		// 2. 保存管理员账号和刷新时间
		if (success) {
			// 保存注册地址
			mapSites.put(node, account);
			// 取出编号，大于-1是有效
			int no = account.getNo();
			if (no > -1) {
				// 如果编号存在，注册失败
				if (mapSerials.containsKey(no)) {
					success = false;
				} else {
					mapSerials.put(no, account);
				}
			}
		}

		// 刷新注册时间
		if (success) {
			account.refreshTime();
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
		AccountSite site = mapSites.remove(node);

		// 判断站点存在
		boolean success = (site != null);
		// 删除站点编号
		if (success) {
			mapSerials.remove(site.getNo());
		}
		
		Logger.debug(this, "effuse", success, "from %s", node);

		return site;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#transmit(com.laxcus.site.Site)
	 */
	@Override
	protected void transmit(Site site) {
		// 通知BANK监视站点和WATCH站点，一个ACCOUNT站点加入
		super.pushSite(site);
		
		// 节点有效，推送给HASH节点
		AccountSite real = (AccountSite) site;
		if (real.isValidNo()) {
			PushAccountSite cmd = new PushAccountSite(real.getNode());
			ShiftPushAccountSite shift = new ShiftPushAccountSite(cmd);
			BankCommandPool.getInstance().admit(shift);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#dismiss(com.laxcus.site.Site)
	 */
	@Override
	protected void dismiss(Site site) {
		// 通知BANK监视站点和WATCH站点，一个ACCOUNT站点退出
		super.dropSite(site);
		
		// 节点有效，推送给HASH节点
		AccountSite real = (AccountSite) site;
		if (real.isValidNo()) {
			DropAccountSite cmd = new DropAccountSite(real.getNode());
			ShiftDropAccountSite shift = new ShiftDropAccountSite(cmd);
			BankCommandPool.getInstance().admit(shift);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.HubPool#destroy(com.laxcus.site.Site)
	 */
	@Override
	protected void destroy(Site site) {
		// 通知BANK监视站点和WATCH站点，一个ACCOUNT站点故障被撤销，请求检查和处理
		super.destroySite(site);
		
		// 节点有效，推送给HASH节点
		AccountSite real = (AccountSite) site;
		if (real.isValidNo()) {
			DropAccountSite cmd = new DropAccountSite(real.getNode());
			ShiftDropAccountSite shift = new ShiftDropAccountSite(cmd);
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