/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;

/**
 * 逗留账号管理池 <br>
 * 
 * 当账号注册到ConferrerFrontOnGatePool之前，在此临时逗留位置。
 * 
 * @author scott.liang
 * @version 1.0 7/25/2017
 * @since laxcus 1.0
 */
public final class StayConferrerFrontOnGatePool extends VirtualPool {

	/** 被授权FRONT站点管理池 **/
	private static StayConferrerFrontOnGatePool selfHandle = new StayConferrerFrontOnGatePool();

	/** 被授权FRONT站点 -> 站点配置 **/
	private Map<Node, ConferrerSite> mapSites = new TreeMap<Node, ConferrerSite>();

	/**
	 * 构造被授权FRONT站点管理池
	 */
	private StayConferrerFrontOnGatePool() {
		super();
		setSleepTime(60); //60秒触发一次
	}

	/**
	 * 返回被授权FRONT站点管理池静态句柄
	 * @return
	 */
	public static StayConferrerFrontOnGatePool getInstance() {
		return StayConferrerFrontOnGatePool.selfHandle;
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
		while (!isInterrupted()) {
			sleep();
			check();
		}
		Logger.info(this, "process", "exit!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapSites.clear();
	}

	/**
	 * 保存一个账号
	 * @param front 被授权FRONT站点地址
	 * @return 成功返回真，否则假
	 */
	public boolean add(ConferrerSite front) {
		if (front == null) {
			Logger.error(this, "add", "null pointer!");
			return false;
		}
		
		// 锁定处理
		boolean success = false;
		super.lockSingle();
		try {
			// FRONT站点参数
			Node node = front.getNode();
			success = (mapSites.get(node) == null);
			if (success) {
				success = (mapSites.put(node, front) == null);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "add", success, "add %s", front.getNode());
		
		return success;
	}

	/**
	 * 删除被授权FRONT站点
	 * @param node 账号地址
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Node node) {
		if (node == null) {
			Logger.error(this, "remove", "null pointer!");
			return false;
		}
		
		// 锁定处理
		boolean success = false;
		super.lockSingle();
		try {
			// FRONT站点参数
			success = (mapSites.remove(node) != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		
		Logger.debug(this, "remove", success, "drop %s", node);
		
		return success;
	}

	/**
	 * 检查被授权FRONT站点地址存在
	 * @param node 被授权FRONT站点地址
	 * @return 返回真或者假
	 */
	public boolean contains(Node node) {
		// 锁定
		boolean success = false;
		super.lockMulti();
		try {
			if (node != null) {
				ConferrerSite site = mapSites.get(node);
				success = (site != null);
				if (success) {
					site.refreshTime();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 检查超时的临时注册站点
	 */
	private void check() {
		int size = mapSites.size();
		if (size == 0) {
			return;
		}

		ArrayList<Node> array = new ArrayList<Node>(size);

		// 锁定
		super.lockSingle();
		try {
			Iterator<Map.Entry<Node, ConferrerSite>> iterator = mapSites.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Node, ConferrerSite> entry = iterator.next();
				ConferrerSite site = entry.getValue();
				// 1分钟内没有激活，删除它！
				if (site.isTimeout(60000)) {
					array.add(entry.getKey());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 删除超时的站点
		for (Node node : array) {
			remove(node);
		}
	}
}