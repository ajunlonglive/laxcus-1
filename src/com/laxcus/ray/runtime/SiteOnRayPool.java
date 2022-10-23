/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.runtime;

import java.util.*;

import com.laxcus.ray.pool.*;
import com.laxcus.site.*;
import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.platform.listener.*;
import com.laxcus.pool.*;

/**
 * WATCH节点监视管理池。定时检查被监视节点。
 * 
 * @author scott.liang
 * @version 1.0 4/13/2018
 * @since laxcus 1.0
 */
public class SiteOnRayPool extends VirtualPool {

	/** WATCH节点监视管理池 **/
	private static SiteOnRayPool selfHandle = new SiteOnRayPool();

	/** 节点地址 -> 被监视节点 **/
	private Map<Node, RayTube> sites = new TreeMap<Node, RayTube>();

	/**
	 * 初始化WATCH节点监视管理池
	 */
	private SiteOnRayPool() {
		super();
	}

	/**
	 * 返回WATCH节点监视管理池的静态句柄
	 * @return WATCH节点监视管理池实例
	 */
	public static SiteOnRayPool getInstance() {
		return SiteOnRayPool.selfHandle;
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
		Logger.debug(this, "process", "into...");

		while (!isInterrupted()) {
			// 必须是登录状态，才启动检查!
			// if (isLogined()) {
			
			// 必须登录并且有集群管理窗口运行时，才处理
			if (allow()) {
				check();
			}
			// 以最小延时为基础，进行单元检查
			delay(RayTube.getMinTimeout());
		}

		Logger.debug(this, "process", "exit!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		clear();
	}
	
	/**
	 * 允许发送
	 * @return 返回真或者假
	 */
	private boolean allow() {
		boolean success = isLogined();
		if (success) {
			WatchClient[] listeners = PlatformKit.findListeners(WatchClient.class);
			success = (listeners != null && listeners.length > 0);
		}
		return success;
	}
	
	/**
	 * 统计节点数目
	 * @return 数字
	 */
	public int size() {
		super.lockMulti();
		try {
			return sites.size();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 弹出全部
	 * @return
	 */
	public List<Node> list() {
		super.lockMulti();
		try {
			return new ArrayList<Node>(sites.keySet());
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 显示匹配的节点
	 * @param siteFamily 节点类型
	 * @return 节点集合
	 */
	public List<Node> list(byte siteFamily) {
		ArrayList<Node> array = new ArrayList<Node>();

		// 锁定！
		super.lockMulti();
		try {
			Iterator<Map.Entry<Node, RayTube>> iterator = sites.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Node, RayTube> entry = iterator.next();
				Node node = entry.getKey();
				// 类型一致，保存
				if (node.getFamily() == siteFamily) {
					array.add(node);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return array;
	}

	/**
	 * 保存一个节点地址。这个节点必须不存在！
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean add(Node node) {
		// 忽略FRONT/WATCH站点！
		if (node.isFront() || node.isWatch()) {
			return false;
		}
		
		boolean success = false;
		// 锁定！
		super.lockSingle();
		try {
			RayTube tube = sites.get(node);
			success = (tube == null);
			if (success) {
				tube = new RayTube(node);
				sites.put(tube.getNode(), tube);
				tube.refresh(); // 刷新时间
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 立即执行
		if (success) {
			fire(node);
		}

		return success;
	}

	/**
	 * 删除一个节点地址
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Node node) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			RayTube tube = sites.remove(node);
			success = (tube != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}
	
	/**
	 * 查找WATCH监视地址
	 * @param node 节点地址
	 * @return 返回实例或者空指针
	 */
	public RayTube find(Node node) {
		// 锁定！
		super.lockMulti();
		try {
			return sites.get(node);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断节点存在
	 * @param node 节点地址
	 * @return 返回真或者假
	 */
	public boolean contains(Node node) {
		return find(node) != null;
	}

	/**
	 * 清除记录
	 */
	public void clear() {
		super.lockSingle();
		try {
			sites.clear();
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 检查超时节点
	 */
	private void check() {
		// 判断是空集合
		int size = sites.size();
		if (size == 0) {
			return;
		}

		ArrayList<Node> array = new ArrayList<Node>(size);
		// 锁定！
		super.lockMulti();
		try {
			Iterator<Map.Entry<Node, RayTube>> iterator = sites.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Node, RayTube> entry = iterator.next();
				if (entry.getValue().isTimeout()) {
					array.add(entry.getKey()); // 保存节点地址
					entry.getValue().refresh(); // 刷新时间
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		if (array.size() > 0) {
			ShiftSeekSiteRuntime cmd = new ShiftSeekSiteRuntime(array);
			cmd.setFast(true); // 极速处理
			RayCommandPool.getInstance().admit(cmd);
		}
	}

	/**
	 * 执行
	 * @param node
	 */
	private void fire(Node node) {
		ShiftSeekSiteRuntime cmd = new ShiftSeekSiteRuntime(node);
		cmd.setFast(true); // 极速处理
		RayCommandPool.getInstance().admit(cmd);
	}
	
}