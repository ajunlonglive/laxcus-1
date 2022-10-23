/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.pool;

import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.lock.*;

/**
 * LAXCUS集群成员管道输入/输出代理。
 * 以串行方式，保证输入/输出在任意时间的唯一性
 * 
 * @author scott.liang
 * @version 1.0 1/18/2020
 * @since laxcus 1.0
 */
public class PipeBasketTrustor extends MutexHandler {

	/** 执行代理 **/
	private static PipeBasketTrustor selfHandle = new PipeBasketTrustor();

	/**
	 * 初始化执行代理
	 */
	private PipeBasketTrustor() {
		super();
	}

	/**
	 * 返回执行代理的静态句柄
	 * @return 执行代理实例
	 */
	public static PipeBasketTrustor getInstance() {
		return PipeBasketTrustor.selfHandle;
	}

	/**
	 * 注册一个成员
	 * @param seat
	 * @return
	 */
	public boolean pushRegisterMember(Seat seat) {
		Siger siger = seat.getSiger();

		boolean success = false;
		// 单向锁定，保证唯一性
		super.lockSingle();
		try {
			// 判断存在
			boolean b1 = RegisterMemberBasket.getInstance().contains(siger);
			// 保存
			RegisterMemberBasket.getInstance().add(seat);
			// 保存明文
			String plainText = seat.getPlainText();
			if (plainText != null) {
				RegisterMemberBasket.getInstance().putPlainText(siger, plainText);
			}
			// 判断不存在
			boolean b2 = RegisterMemberBasket.getInstance().contains(siger);
			// 之前没有，之后有，是成功!
			success = (!b1 && b2);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除注册成员
	 * @param seat 用户基点
	 * @return 成功返回真，否则假
	 */
	public boolean dropRegisterMember(Seat seat) {
		// 判断存在
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			Siger siger = seat.getSiger();
			boolean b1 = RegisterMemberBasket.getInstance().contains(siger);
			// 删除
			RegisterMemberBasket.getInstance().remove(seat);
			// 判断不存在
			boolean b2 = RegisterMemberBasket.getInstance().contains(siger);
			// 之前有，之后没有，是成功!
			success = (b1 && !b2);
			if (success) {
				// 在线成员池不存在，删除账号明文
				boolean exists = FrontMemberBasket.getInstance().contains(siger);
				if (!exists) {
					RegisterMemberBasket.getInstance().removePalinText(siger);
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
//	 * 保存在线成员
//	 * @param seat 用户基点
//	 * @return 成功返回真，否则假
//	 */
//	public boolean pushOnlineMember(Seat seat) {
//		boolean success = false;
//		// 锁定
//		super.lockSingle();
//		try {
//			Siger siger = seat.getSiger();
//			// 判断存在
//			boolean b1 = OnlineMemberBasket.getInstance().contains(siger);
//			// 删除
//			OnlineMemberBasket.getInstance().add(seat);
//
//			// 保存明文
//			String plainText = seat.getPlainText();
//			if (plainText != null) {
//				RegisterMemberBasket.getInstance().putPlainText(siger, plainText);
//			}
//
//			// 判断不存在
//			boolean b2 = OnlineMemberBasket.getInstance().contains(siger);
//			// 之前不存在，之后存在，成功
//			success = (!b1 && b2);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//		return success;
//	}
//
//	/**
//	 * 删除在线成员
//	 * @param seat 用户基点
//	 * @return 成功返回真，否则假
//	 */
//	public boolean dropOnlineMember(Seat seat) {
//		boolean success = false;
//		// 锁定
//		super.lockSingle();
//		try {
//			Siger siger = seat.getSiger();
//			// 判断存在
//			boolean b1 = OnlineMemberBasket.getInstance().contains(siger);
//			// 删除
//			OnlineMemberBasket.getInstance().remove(seat);
//			// 判断不存在
//			boolean b2 = OnlineMemberBasket.getInstance().contains(siger);
//			// 之前有，之后没有，成功！
//			success = (b1 && !b2);
//			if (success) {
//				// 账号已经在注册成员池不存在，删除明文
//				boolean exists = RegisterMemberBasket.getInstance().contains(siger);
//				if (!exists) {
//					RegisterMemberBasket.getInstance().removePalinText(siger);
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		// 返回结果！
//		return success;
//	}

//	/**
//	 * 保存在线成员
//	 * @param seat 用户基点
//	 * @return 成功返回真，否则假
//	 */
//	public boolean pushOnlineMember(FrontSeat seat) {
//		boolean success = false;
//		// 锁定
//		super.lockSingle();
//		try {
//			Siger siger = seat.getSiger();
//			// 判断存在
//			boolean b1 = FrontMemberBasket.getInstance().contains(siger);
//			// 保存
//			FrontMemberBasket.getInstance().add(seat);
//
//			// 保存明文
//			String plainText = seat.getPlainText();
//			if (plainText != null) {
//				RegisterMemberBasket.getInstance().putPlainText(siger, plainText);
//			}
//
//			// 判断不存在
//			boolean b2 = FrontMemberBasket.getInstance().contains(siger);
////			// 之前不存在，之后存在，成功
////			success = (!b1 && b2);
//			
//			// 任何一个保存成功即有效！
//			success = (b1 || b2);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//		return success;
//	}

	/**
	 * 保存在线成员。
	 * @param seat 用户基点
	 * @return 是第一次且保存成功时，返回真，否则假。
	 */
	public boolean pushOnlineMember(FrontSeat seat) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			Siger siger = seat.getSiger();
			// 判断存在
			boolean b1 = FrontMemberBasket.getInstance().contains(siger);
			// 保存
			FrontMemberBasket.getInstance().add(seat);

			// 保存明文
			String plainText = seat.getPlainText();
			if (plainText != null) {
				RegisterMemberBasket.getInstance().putPlainText(siger, plainText);
			}
			
			boolean b2 = FrontMemberBasket.getInstance().contains(siger);
			// 判断成功
			success = (!b1 && b2);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return success;
	}
	
//	/**
//	 * 删除在线成员
//	 * @param seat 用户基点
//	 * @return 成功返回真，否则假
//	 */
//	public boolean dropOnlineMember(FrontSeat seat) {
//		boolean success = false;
//		// 锁定
//		super.lockSingle();
//		try {
//			Siger siger = seat.getSiger();
//			// 判断存在
//			boolean b1 = FrontMemberBasket.getInstance().contains(siger);
//			// 删除
//			FrontMemberBasket.getInstance().remove(seat);
//			// 判断不存在
//			boolean b2 = FrontMemberBasket.getInstance().contains(siger);
//			// 之前有，之后没有，成功！
//			success = (b1 && !b2);
//			if (success) {
//				// 账号已经在注册成员池不存在，删除明文
//				boolean exists = RegisterMemberBasket.getInstance().contains(siger);
//				if (!exists) {
//					RegisterMemberBasket.getInstance().removePalinText(siger);
//				}
//			}
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//		// 返回结果！
//		return success;
//	}

	/**
	 * 删除在线成员
	 * @param seat 用户基点
	 * @return 删除最后一个返回真，否则假
	 */
	public boolean dropOnlineMember(FrontSeat seat) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			Siger siger = seat.getSiger();
			boolean b1 = FrontMemberBasket.getInstance().contains(siger);
			// 删除
			FrontMemberBasket.getInstance().remove(seat);
			boolean b2 = FrontMemberBasket.getInstance().contains(siger);
			// 删除前有，删除后没有!
			success = (b1 && !b2);
			// 成功删除最后一个，检测注册池
			if (success) {
				// 账号已经在注册成员池不存在，删除明文
				boolean exists = RegisterMemberBasket.getInstance().contains(siger);
				if (!exists) {
					RegisterMemberBasket.getInstance().removePalinText(siger);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 返回结果！
		return success;
	}
	
	/**
	 * 保存节点
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean pushSite(Node node) {
		// 保存节点
		return SiteOnWatchPool.getInstance().add(node);
	}

	/**
	 * 删除节点
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	public boolean dropSite(Node node) {
		// 删除节点
		return SiteOnWatchPool.getInstance().remove(node);
	}
}