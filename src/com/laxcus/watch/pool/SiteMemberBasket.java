/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.pool;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;
import com.laxcus.watch.util.*;

/**
 * 注册成员合集。<br>
 * 保存被WATCH节点监控的LAXCUS集群节点和注册用户的映射关系。
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class SiteMemberBasket extends MutexHandler {

	/** 用户签名 -> 注册成员 **/
	private TreeMap<Siger, MemberMemo> sigers = new TreeMap<Siger, MemberMemo>();

	/** 节点地址 -> 节点成员 **/
	private TreeMap<Node, SiteMemo> sites = new TreeMap<Node, SiteMemo>();

	/**
	 * 构造注册成员合集
	 */
	protected SiteMemberBasket() {
		super();
	}

	/**
	 * 清除全部
	 */
	public void clear() {
		super.lockSingle();
		try {
			sigers.clear();
			sites.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 输出全部签名
	 * @return Siger列表
	 */
	public List<Siger> getUsers() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>( sigers.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return new ArrayList<Siger>();
	}

	/**
	 * 输出全部地址
	 * @return Node列表
	 */
	public List<Node> getSites() {
		super.lockMulti();
		try {
			return new ArrayList<Node>(sites.keySet());
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return new ArrayList<Node>();
	}

	/**
	 * 保存注册地址
	 * @param seat 用户基点
	 * @return 成功返回真，否则假
	 */
	public boolean add(Seat seat) {
		Laxkit.nullabled(seat);

		boolean success = false;
		super.lockSingle();
		try {
			// 保存注册成员
			MemberMemo mm = sigers.get(seat.getSiger());
			if (mm == null) {
				mm = new MemberMemo(seat.getSiger());
				sigers.put(mm.getSiger(), mm);
			}
			boolean b1 = mm.add(seat.getSite());
			// 保存集群节点
			SiteMemo sm = sites.get(seat.getSite());
			if (sm == null) {
				sm = new SiteMemo(seat.getSite());
				sites.put(seat.getSite(), sm);
			}
			boolean b2 = sm.add(seat.getSiger());
			// 判断成功
			success = (b1 && b2);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除注册用户
	 * @param seat 用户基点
	 * @return 返回真或者假
	 */
	public boolean remove(Seat seat) {
		Laxkit.nullabled(seat);

		boolean b1 = false;
		boolean b2 = false;
		// 锁定
		super.lockSingle();
		try {
			MemberMemo mm = sigers.get(seat.getSiger());
			if (mm != null) {
				b1 = mm.remove(seat.getSite()); // 删除
				if (mm.isEmpty())
					sigers.remove(seat.getSiger());
			}

			SiteMemo sm = sites.get(seat.getSite());
			if (sm != null) {
				b2 = sm.remove(seat.getSiger()); // 删除
				if (sm.isEmpty())
					sites.remove(seat.getSite());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return b1 && b2;
	}

	/**
	 * 删除用户签名，以及它的全部地址
	 * @param siger 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Siger siger) {
		Laxkit.nullabled(siger);

		boolean success = false;
		super.lockSingle();
		try {
			MemberMemo mm = sigers.remove(siger);
			success = (mm != null);
			if (success) {
				for (Node e : mm.list()) {
					SiteMemo sm = sites.get(e);
					if (sm != null) {
						sm.remove(siger);
						if (sm.isEmpty()) sites.remove(e);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除节点地址，以及它全部的用户签名
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Node node) {
		Laxkit.nullabled(node);

		boolean success = false;
		super.lockSingle();
		try {
			SiteMemo sm = sites.get(node);
			success = (sm != null);
			if (success) {
				for (Siger e : sm.list()) {
					MemberMemo mm = sigers.get(e);
					if (mm != null) {
						mm.remove(node);
						if (mm.isEmpty()) sites.remove(e);
					}
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 基于用户签名，查找全部节点
	 * @param siger 用户签名
	 * @return 返回节点地址列表，没有是空指针
	 */
	public List<Node> find(Siger siger) {
		ArrayList<Node> array = new ArrayList<Node>();
		// 锁定
		super.lockMulti();
		try {
			MemberMemo mm = sigers.get(siger);
			if (mm != null) {
				array.addAll(mm.list());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	//	/**
	//	 * 统计这个用户的地址
	//	 * @param siger 签名
	//	 * @return 返回统计值
	//	 */
	//	public int track(Siger siger) {
	//		List<Node> nodes = find(siger);
	//		return (nodes == null ? 0 : nodes.size());
	//	}

	/**
	 * 基于节点地址，查找它下属的全部用户签名
	 * @param node 节点地址 
	 * @return 返回用户签名列表，没有是空指针
	 */
	public List<Siger> find(Node node) {
		super.lockMulti();
		try {
			SiteMemo sm = sites.get(node);
			if (sm != null) {
				return sm.list();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断签名存在
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger) {
		List<Node> e = find(siger);
		return e != null && e.size() > 0;
	}

	/**
	 * 判断节点存在
	 * @param node 节点地址
	 * @return 返回真或者假
	 */
	public boolean contains(Node node) {
		List<Siger> e = find(node);
		return e != null && e.size() > 0;
	}

	/**
	 * 统计注册成员
	 * @return
	 */
	public int countMember() {
		super.lockMulti();
		try {
			return sigers.size();
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 注册节点
	 * @return
	 */
	public int countSites() {
		return sites.size();
	}
}
