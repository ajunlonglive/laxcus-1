/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance.pool;

import java.util.*;

import com.laxcus.command.site.bank.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * ENTRANCE站点资源管理池
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class StaffOnEntrancePool extends VirtualPool {

	/** ENTRANCE资源管理池句柄 **/
	private static StaffOnEntrancePool selfHandle = new StaffOnEntrancePool();
	
	/** GATE站点编号 -> GATE主机地址（内/外两个节点） **/
	private TreeMap<Integer, GatewayNode> mapGates = new TreeMap<Integer, GatewayNode>();
	
	/** 内网节点 **/
	private TreeSet<Node> privates = new TreeSet<Node>();

	/**
	 * 构造ENTRANCE资源管理池
	 */
	private StaffOnEntrancePool() {
		super();
		setSleepTime(30);
	}

	/**
	 * 返回ENTRANCE资源管理池句柄
	 * 
	 * @return 资源管理池实例
	 */
	public static StaffOnEntrancePool getInstance() {
		return StaffOnEntrancePool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");
		
		// 加载全部GATE站点
		boolean success = loadGateSites();
		// 成功，重新注册；否则退出
		if (success) {
			getLauncher().checkin(true); // 立即重新注册
		} else {
			getLauncher().stop();
		}

		// 延时等待退出
		while (!isInterrupted()) {
			// 延时
			sleep();
			// 不成功，忽略！
			if (!success) {
				continue;
			}

			// 如果没有GATE站点记录，去BANK站点加载
			if (mapGates.size() == 0) {
				loadGateSites();
			}
		}

		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapGates.clear();
		privates.clear();
	}
	
	/**
	 * 取出全部内网GATE站点地址
	 * @return Node列表
	 */
	public List<Node> getPrivateSites() {
		// 锁定
		super.lockMulti();
		try {
			return new ArrayList<Node>(privates);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 保存地址
	 * @param inner GATE内网地址
	 * @param outer GATE外网地址
	 * @param no 节点编号
	 * @return 成功返回真，否则假
	 */
	public boolean add(Node inner, Node outer, int no) {
		GatewayNode node = new GatewayNode(inner, outer);

		Logger.debug(this, "add", "no:%d, gate site:%s", no, node);

		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			mapGates.put(no, node);
			privates.add(inner);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除地址
	 * @param no 节点编号
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(int no) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			GatewayNode node = mapGates.remove(no);
			success = (node != null);
			if (success) {
				privates.remove(node.getPrivate());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 加载关联链站点，ENTRANCE站点关联全部GATE站点
	 * @return 成功返回真，否则假
	 */
	private boolean loadGateSites() {
		TakeBankSubSites cmd = new TakeBankSubSites(SiteTag.GATE_SITE); // 申请GATE站点
		TakeBankSubSitesHook hook = new TakeBankSubSitesHook();
		ShiftTakeBankSubSites shift = new ShiftTakeBankSubSites(cmd, hook);

		// 交给命令管理池
		boolean success = getLauncher().getCommandPool().admit(shift);
		if (!success) {
			Logger.error(this, "loadGateSites", "cannot be admit!");
			return false;
		}
		// 钩子等待，直到被唤醒
		hook.await();

		// 返回处理结果
		TakeBankSubSitesProduct product = hook.getProduct();
		success = (product != null && product.size() > 0);
		if (!success) {
			Logger.error(this, "loadGateSites", "cannot be catch gate sites!");
			return false;
		}
		
		// 生成GATE站点的编号 -> 主机地址的关联
		for (BankSubSiteItem e : product.list()) {
			BankSerialSiteItem item = (BankSerialSiteItem) e;
			add(item.getInner(), item.getOuter(), item.getNo());
		}
		
		Logger.info(this, "loadGateSites", "all gate sites:%d", mapGates.size());
		
		return true;
	}

	/**
	 * 根据用户签名的模值，定位GATE主机地址（区分内外网）
	 * @param siger 用户签名
	 * @param wide 外网地址
	 * @return 返回GATE主机地址，发生故障返回空指针
	 */
	public Node locate(Siger siger, boolean wide) {
		// 锁定
		super.lockMulti();
		try {
			int size = mapGates.size();
			// 根据模值，返回对应的GATE站点地址
			if (size > 0) {
				int no = siger.mod(size);
				GatewayNode node = mapGates.get(no);
				return (wide ? node.getPublic().duplicate() : node.getPrivate().duplicate());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

}