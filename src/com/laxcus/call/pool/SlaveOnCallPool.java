/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * CALL站点的元数据资源汇总管理池。<br>
 * 
 * 此处保存来自DATA/WORK/BUILD站点的阶段命名和对应的站点地址。
 * 
 * @author scott.liang
 * @version 1.2 4/23/2013
 * @since laxcus 1.0
 */
public abstract class SlaveOnCallPool extends SitePool {
	
	/** 阶段命名 -> DATA/WORK/BUILD站点地址 **/
	private TreeMap<Phase, NodeSet> mapPhases = new TreeMap<Phase, NodeSet>();
	
	/** 用户签名 -> DATA/WORK/BUILD站点地址 **/
	private TreeMap<Siger, NodeSet> mapSigers = new TreeMap<Siger, NodeSet>();
	
	/**
	 * 构造分布站点的资源管理池，指定站点类型
	 * @param family 站点类型
	 */
	protected SlaveOnCallPool(byte family) {
		super(family);
	}

	/**
	 * 注入用户签名
	 * @param node 站点地址
	 * @param sigers 用户签名集合
	 */
	protected void infuseSigers(Node node, List<Siger> sigers) {
		for (Siger siger : sigers) {
			NodeSet set = mapSigers.get(siger);
			if (set == null) {
				set = new NodeSet();
				mapSigers.put(siger, set);
			}
			set.add(node);
			Logger.debug(this, "infuseSigers", "siger is '%s'", siger);
		}
	}
	
	/**
	 * 撤销用户签名
	 * @param node 站点地址
	 * @param sigers 用户签名集合
	 */
	protected void effuseSigers(Node node, List<Siger> sigers) {
		for (Siger siger : sigers) {
			NodeSet set = mapSigers.get(siger);
			if (set != null) {
				set.remove(node);
				if (set.isEmpty()) mapSigers.remove(siger);
			}
			Logger.debug(this, "effuseSigers", "release '%s'", siger);
		}
	}

	/**
	 * 注入阶段命名
	 * @param node 站点地址
	 * @param phases 阶段命名集合
	 */
	protected void infusePhases(Node node, List<Phase> phases) {
		for (Phase phase : phases) {
			NodeSet set = mapPhases.get(phase);
			if (set == null) {
				set = new NodeSet();
				mapPhases.put(phase, set);
			}
			set.add(node);
			Logger.debug(this, "infusePhases", "phase is '%s'", phase);
		}
	}

	/**
	 * 撤销阶段命名
	 * @param node 站点地址
	 * @param phases 阶段命名集合
	 */
	protected void effusePhases(Node node, List<Phase> phases) {
		for (Phase phase : phases) {
			NodeSet set = mapPhases.get(phase);
			if (set != null) {
				set.remove(node);
				if (set.isEmpty()) mapPhases.remove(phase);
			}
			Logger.debug(this, "effusePhases", "release '%s'", phase);
		}
	}

	/**
	 * 根据阶段命名查找对应的分布站点集合
	 * @param phase 阶段命名
	 * @return 返回NodeSet实例
	 */
	public NodeSet findSites(Phase phase) {
		Laxkit.nullabled(phase);

		super.lockMulti();
		try {
			return mapPhases.get(phase);
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 输出当前全部阶段命名
	 * @return 阶段命名
	 */
	public List<Phase> getPhases() {
		super.lockMulti();
		try {
			return new ArrayList<Phase>(mapPhases.keySet());
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 根据用户签名查找对应的分布站点集合
	 * @param siger 用户签名
	 * @return 返回NodeSet实例
	 */
	public NodeSet findSites(Siger siger) {
		Laxkit.nullabled(siger);

		super.lockMulti();
		try {
			return mapSigers.get(siger);
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 根据用户签名查找全部对应的分布站点
	 * @param siger 用户签名
	 * @return 返回Node列表
	 */
	public List<Node> findNodes(Siger siger) {
		Laxkit.nullabled(siger);

		ArrayList<Node> a = new ArrayList<Node>();

		// 锁定！
		super.lockMulti();
		try {
			NodeSet set = mapSigers.get(siger);
			if (set != null) {
				a.addAll(set.list());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return a;
	}
	
	/**
	 * 输出当前全部用户签名
	 * @return 用户签名
	 */
	public List<Siger> getSigers() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(mapSigers.keySet());
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 查找某个用户的阶段命名，包括普通注册用户和系统用户。
	 * 如果是系统用户，用户签名是空指针！
	 * 
	 * @param issuer 用户签名
	 * @return 阶段命名列表
	 */
	public List<Phase> findPhases(Siger issuer) {
		ArrayList<Phase> array = new ArrayList<Phase>();
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<Phase, NodeSet>> iterator = mapPhases.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Phase, NodeSet> entry = iterator.next();
				Phase e = entry.getKey();

				// 用户签名一致，即保存！
				if (Laxkit.compareTo(issuer, e.getIssuer()) == 0) {
					array.add(e.duplicate());
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
	 * 基于阶段类型，查找阶段命名
	 * @param family 阶段类弄
	 * @return 返回阶段命名
	 */
	public List<Phase> findPhases(int family) {
		ArrayList<Phase> array = new ArrayList<Phase>();
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<Phase, NodeSet>> iterator = mapPhases.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Phase, NodeSet> entry = iterator.next();
				Phase e = entry.getKey();

				// 用户签名一致，即保存！
				if (family == e.getFamily()) {
					array.add(e.duplicate());
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
	 * 判断保存有指定用户签名的阶段命名
	 * @param issuer 用户签名
	 * @return 返回真或者假
	 */
	public boolean hasPhases(Siger issuer) {
		List<Phase> all = findPhases(issuer);
		return (all != null && all.size() > 0);
	}
	
	/**
	 * 判断签名存在
	 * @param issuer 发布者签名
	 * @return 返回真或者假
	 */
	public boolean hasSiger(Siger issuer) {
		boolean success = false;
		// 锁定！
		super.lockMulti();
		try {
			success = (mapSigers.get(issuer) != null);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapPhases.clear();
	}
}
