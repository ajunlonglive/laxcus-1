/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.runtime;


import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;
import com.laxcus.ray.util.*;

/**
 * FRONT成员合集。<br>
 * 保存被WATCH节点监控的LAXCUS集群节点和注册用户的映射关系。
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class RayFrontMemberBasket extends MutexHandler {

	/** 在线成员合集 **/
	private static RayFrontMemberBasket selfHandle = new RayFrontMemberBasket();

	/** 用户签名 -> FRONT成员 **/
	private TreeMap<Siger, RayFrontMemo> issuers = new TreeMap<Siger, RayFrontMemo>();

	/** 节点地址 -> 节点成员 **/
	private TreeMap<Node, RayGatewayMemo> sites = new TreeMap<Node, RayGatewayMemo>();

	/**
	 * 初始化在线成员合集
	 */
	private RayFrontMemberBasket() {
		super();
	}

	/**
	 * 返回在线成员合集的静态句柄
	 * @return 在线成员合集实例
	 */
	public static RayFrontMemberBasket getInstance() {
		return RayFrontMemberBasket.selfHandle;
	}

	/**
	 * 清除全部
	 */
	public void clear() {
		super.lockSingle();
		try {
			issuers.clear();
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
			return new ArrayList<Siger>(issuers.keySet());
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

//	private FrontSock createSock(FrontSeat seat) {
//		return new FrontSock(seat.getGateway(), seat.getFront());
//	}
	
	/**
	 * 保存注册地址
	 * @param seat 用户基点
	 * @return 成功返回真，否则假
	 */
	public boolean add(FrontSeat seat) {
		Laxkit.nullabled(seat);
		
		Siger siger = seat.getSiger();
		Node gateway = seat.getGateway();
//		FrontSock sock = createSock(seat);

		boolean b1 = false;
		boolean b2 = false;
		super.lockSingle();
		try {
			// 保存注册成员
			RayFrontMemo fm = issuers.get(siger);
			if (fm == null) {
				fm = new RayFrontMemo(siger);
				issuers.put(fm.getSiger(), fm);
			}
			b1 = fm.add(seat);

			// 保存网关地址
			RayGatewayMemo sm = sites.get(gateway);
			if (sm == null) {
				sm = new RayGatewayMemo(gateway);
				sites.put(gateway, sm);
			}
			b2 = sm.add(seat);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 判断增加成功
		return (b1 && b2);
	}

	/**
	 * 删除注册用户
	 * @param seat 用户基点
	 * @return 返回真或者假
	 */
	public boolean remove(FrontSeat seat) {
		Laxkit.nullabled(seat);
		
		Siger siger = seat.getSiger();
		Node gateway = seat.getGateway();
//		FrontSock sock = createSock(seat);

		boolean b1 = false;
		boolean b2 = false;
		// 锁定
		super.lockSingle();
		try {
			RayFrontMemo fm = issuers.get(siger);
			if (fm != null) {
				b1 = fm.remove(seat); // 删除
				if (fm.isEmpty()) issuers.remove(siger);
			}

			RayGatewayMemo sm = sites.get(gateway);
			if (sm != null) {
				b2 = sm.remove(seat); // 删除
				if (sm.isEmpty()) sites.remove(gateway);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 判断删除成功！
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
			RayFrontMemo mm = issuers.remove(siger);
			success = (mm != null);
			if (success) {
				for (FrontSeat e : mm.list()) {
					Node gateway = e.getGateway();
					RayGatewayMemo sm = sites.get(gateway);
					if (sm != null) {
						FrontSeat seat = new FrontSeat(siger, e.getGateway(), e.getFront());
						sm.remove(seat);
						if (sm.isEmpty()) sites.remove(gateway);
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
	 * 删除节点及下属的节点
	 * @param node 节点
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Node node) {
		boolean success = false;
		// 锁定!
		super.lockSingle();
		try {
			RayGatewayMemo gm = sites.remove(node);
			success = (gm != null);
			if (success) {
				for (FrontSeat seat : gm.list()) {
					Siger issuer = seat.getSiger();
					RayFrontMemo fm = issuers.get(issuer);
					if (fm != null) {
						fm.remove(seat);
						if (fm.isEmpty()) issuers.remove(issuer);
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

//	/**
//	 * 删除节点地址，以及它全部的用户签名
//	 * @param sock 节点地址
//	 * @return 成功返回真，否则假
//	 */
//	public boolean remove(FrontSock sock) {
//		Laxkit.nullabled(sock);
//
//		boolean success = false;
//		super.lockSingle();
//		try {
//			RayGatewayMemo sm = sites.get(sock.getGateway());
//			success = (sm != null);
//			if (success) {
//				for (Siger e : sm.list()) {
//					RayFrontMemo mm = sigers.get(e);
//					if (mm != null) {
//						mm.remove(sock);
//						if (mm.isEmpty()) sites.remove(e);
//					}
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		return success;
//	}

	/**
	 * 基于用户签名，查找全部节点
	 * @param siger 用户签名
	 * @return 返回节点地址列表，没有是空指针
	 */
	public List<FrontSeat> find(Siger siger) {
		ArrayList<FrontSeat> array = new ArrayList<FrontSeat>();
		// 锁定！
		super.lockMulti();
		try {
			RayFrontMemo memo = issuers.get(siger);
			if (memo != null) {
				array.addAll(memo.list());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 基于节点地址，查找它下属的全部基点
	 * @param gateway 节点地址 
	 * @return 返回用户签名列表，没有是空指针
	 */
	public List<FrontSeat> find(Node gateway) {
		ArrayList<FrontSeat> array = new ArrayList<FrontSeat>();

		super.lockMulti();
		try {
			RayGatewayMemo sm = sites.get(gateway);
			if (sm != null) {
				array.addAll(sm.list());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * 判断签名存在
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger) {
		boolean success = false;
		// 锁定
		super.lockMulti();
		try {
			RayFrontMemo memo = issuers.get(siger);
			success = (memo != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 判断节点存在
	 * @param gateway 节点地址
	 * @return 返回真或者假
	 */
	public boolean contains(Node gateway) {
		boolean success = false;
		super.lockMulti();
		try {
			RayGatewayMemo sm = sites.get(gateway);
			if (sm != null) {
				success = (sm.size()>0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/**
	 * 判断账号和节点有效
	 * @param siger 签名
	 * @param gateway 网关节点
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger, Node gateway) {
		boolean success = contains(siger);
		if (success) {
			success = contains(gateway);
		}
		return success;
	}

	/**
	 * 统计在线账号数量
	 * @return 整数值
	 */
	public int countMember() {
		super.lockMulti();
		try {
			return issuers.size();
		} finally {
			super.unlockMulti();
		}
	}
	
	/**
	 * 统计在线人数
	 * @return 整数
	 */
	public int countUsers() {
		int count = 0;
		// 锁定!
		super.lockMulti();
		try {
			Iterator<Map.Entry<Siger, RayFrontMemo>> iterator = issuers.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, RayFrontMemo> entry = iterator.next();
				RayFrontMemo memo = entry.getValue();
				count += memo.getFronts().size();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return count;
	}

	/**
	 * 注册节点
	 * @return
	 */
	public int countSites() {
		super.lockMulti();
		try {
			return sites.size();
		} finally {
			super.unlockMulti();
		}
	}

}