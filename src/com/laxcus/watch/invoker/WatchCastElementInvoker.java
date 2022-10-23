/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.watch.pool.*;

/**
 * 投递节点用户信息给WATCH站点
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public abstract class WatchCastElementInvoker extends WatchInvoker {

	/**
	 * 构造投递节点用户信息给WATCH站点调用器，指定命令
	 * @param cmd 相关命令
	 */
	protected WatchCastElementInvoker(Command cmd) {
		super(cmd);
	}
	
//	/**
//	 * 增加注册用户
//	 * @param seats 用户基点集合
//	 */
//	protected void pushRegisterMember(Collection<Seat> seats) {
//		ArrayList<Siger> array = new ArrayList<Siger>();
//
//		// 增加
//		for (Seat seat : seats) {
//			Siger siger = seat.getSiger();
//			// 判断存在
//			boolean b1 = RegisterMemberBasket.getInstance().contains(siger);
//			// 保存
//			RegisterMemberBasket.getInstance().add(seat);
//			// 保存明文
//			String plainText = seat.getPlainText();
//			if (plainText != null) {
//				RegisterMemberBasket.getInstance().putPlainText(siger, plainText);
//			}
//			// 判断不存在
//			boolean b2 = RegisterMemberBasket.getInstance().contains(siger);
//			// 之前不存在，之后存在，保存！
//			if (!b1 && b2) {
//				array.add(siger);
//			}
//		}
//
//		// 通知界面，增加新的用户签名
//		if (array.size() > 0) {
//			for (Siger siger : array) {
//				getLauncher().pushRegisterMember(siger);
//			}
//			// 更新注册栏的成员数目
//			getLauncher().updateStatusMembers();
//		}
//	}
	
	/**
	 * 增加注册用户
	 * @param seats 用户基点集合
	 */
	protected void pushRegisterMember(Collection<Seat> seats) {
		ArrayList<Siger> array = new ArrayList<Siger>();

		// 增加
		for (Seat seat : seats) {
			// 保存到内存
			boolean success = PipeBasketTrustor.getInstance().pushRegisterMember(seat);
			if (success) {
				array.add(seat.getSiger());
			}
		}

		// 通知界面，增加新的用户签名
		if (array.size() > 0) {
			for (Siger siger : array) {
				getLauncher().pushRegisterMember(siger);
			}
			// 更新注册栏的成员数目
			getLauncher().updateStatusMembers();
		}
	}

	/**
	 * 增加一个注册用户
	 * @param seat 用户基点
	 */
	protected void pushRegisterMember(Seat seat) {
		ArrayList<Seat> array = new ArrayList<Seat>();
		array.add(seat);
		pushRegisterMember(array);
	}

	/**
	 * 增加一组基于某个节点的用户
	 * @param node 节点地址
	 * @param sigers 用户签名集合
	 */
	protected void pushRegisterMember(Node node, Collection<Siger> sigers) {
		ArrayList<Seat> array = new ArrayList<Seat>();
		for (Siger siger : sigers) {
			Seat seat = new Seat(siger, node);
			array.add(seat);
		}
		pushRegisterMember(array);
	}

//	/**
//	 * 删除一组注册用户
//	 * @param seats 用户基点集合
//	 */
//	protected void dropRegisterMember(Collection<Seat> seats) {
//		ArrayList<Siger> array = new ArrayList<Siger>();
//
//		// 删除
//		for (Seat seat : seats) {
//			Siger siger = seat.getSiger();
//			// 判断存在
//			boolean b1 = RegisterMemberBasket.getInstance().contains(siger);
//			// 删除
//			RegisterMemberBasket.getInstance().remove(seat);
//			// 判断不存在
//			boolean b2 = RegisterMemberBasket.getInstance().contains(siger);
//			// 之前有，之后没有，保存！
//			if (b1 && !b2) {
//				array.add(siger);
//				// 在线成员池不存在，删除账号明文
//				boolean exists = OnlineMemberBasket.getInstance().contains(siger);
//				if (!exists) {
//					RegisterMemberBasket.getInstance().removePalinText(siger);
//				}
//			}
//		}
//
//		// 通知界面，删除相关用户签名
//		if (array.size() > 0) {
//			for (Siger siger : array) {
//				getLauncher().dropRegisterMember(siger);
//			}
//			// 更新注册栏的成员数目
//			getLauncher().updateStatusMembers();
//		}
//	}

	/**
	 * 删除一组注册用户
	 * @param seats 用户基点集合
	 */
	protected void dropRegisterMember(Collection<Seat> seats) {
		ArrayList<Siger> array = new ArrayList<Siger>();

		// 删除
		for (Seat seat : seats) {
			// 从内存中删除
			boolean success = PipeBasketTrustor.getInstance()
					.dropRegisterMember(seat);
			// 成功，保存签名
			if (success) {
				array.add(seat.getSiger());
			}
		}

		// 通知界面，删除相关用户签名
		if (array.size() > 0) {
			for (Siger siger : array) {
				getLauncher().dropRegisterMember(siger);
			}
			// 更新注册栏的成员数目
			getLauncher().updateStatusMembers();
		}
	}
	
	/**
	 * 删除一个注册用户
	 * @param seat 用户基点
	 */
	protected void dropRegisterMember(Seat seat) {
		ArrayList<Seat> array = new ArrayList<Seat>();
		array.add(seat);
		dropRegisterMember(array);
	}

	/**
	 * 删除一组基于节点的用户
	 * @param node 节点
	 * @param sigers 用户签名集合
	 */
	protected void dropRegisterMember(Node node, Collection<Siger> sigers) {
		ArrayList<Seat> array = new ArrayList<Seat>();
		for (Siger siger : sigers) {
			Seat seat = new Seat(siger, node);
			array.add(seat);
		}
		dropRegisterMember(array);
	}
	
	/**
	 * 删除一个节点和它下属全部用户签名
	 * @param node 节点地址
	 */
	protected void dropRegisterMember(Node node) {
		List<Siger> sigers = RegisterMemberBasket.getInstance().find(node);
		if (sigers != null && sigers.size() > 0) {
			dropRegisterMember(node, sigers);
		}
	}

//	/**
//	 * 增加在线用户
//	 * @param seats 用户基点集合
//	 */
//	protected void pushOnlineMember(Collection<Seat> seats) {
//		ArrayList<Siger> array = new ArrayList<Siger>();
//
//		// 增加
//		for (Seat seat : seats) {
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
//			// 之前不存在，之后存在，保存！
//			if (!b1 && b2) {
//				array.add(siger);
//			}
//		}
//
//		// 通知界面，增加新的用户签名
//		if (array.size() > 0) {
//			for (Siger siger : array) {
//				getLauncher().pushOnlineMember(siger);
//			}
//			// 更新注册栏的成员数目
//			getLauncher().updateStatusMembers();
//		}
//	}
	
//	/**
//	 * 增加在线用户
//	 * @param seats 用户基点集合
//	 */
//	protected void pushOnlineMember(Collection<Seat> seats) {
//		ArrayList<Siger> array = new ArrayList<Siger>();
//
//		// 增加
//		for (Seat seat : seats) {
//			//			Siger siger = seat.getSiger();
//			//			// 判断存在
//			//			boolean b1 = OnlineMemberBasket.getInstance().contains(siger);
//			//			// 删除
//			//			OnlineMemberBasket.getInstance().add(seat);
//			//			
//			//			// 保存明文
//			//			String plainText = seat.getPlainText();
//			//			if (plainText != null) {
//			//				RegisterMemberBasket.getInstance().putPlainText(siger, plainText);
//			//			}
//			//			
//			//			// 判断不存在
//			//			boolean b2 = OnlineMemberBasket.getInstance().contains(siger);
//			//			// 之前不存在，之后存在，保存！
//			//			if (!b1 && b2) {
//			//				array.add(siger);
//			//			}
//
//			// 保存！
//			boolean success = PipeBasketTrustor.getInstance().pushOnlineMember(seat);
//			if (success) {
//				array.add(seat.getSiger());
//			}
//		}
//
//		// 通知界面，增加新的用户签名
//		if (array.size() > 0) {
//			for (Siger siger : array) {
//				getLauncher().pushOnlineMember(siger);
//			}
//			// 更新注册栏的成员数目
//			getLauncher().updateStatusMembers();
//		}
//	}
	
	/**
	 * 增加在线用户
	 * @param seats 用户基点集合
	 */
	protected void pushOnlineMember(Collection<FrontSeat> seats) {
		ArrayList<Siger> a1 = new ArrayList<Siger>();
		ArrayList<Siger> a2 = new ArrayList<Siger>();

		// 增加
		for (FrontSeat seat : seats) {
			// 保存！如果是第一次返回真，否则假
			boolean success = PipeBasketTrustor.getInstance().pushOnlineMember(seat);
			if (success) {
				a1.add(seat.getSiger());
			} else {
				a2.add(seat.getSiger());
			}
		}

		// 通知界面，增加新的用户签名
		for (Siger siger : a1) {
			getLauncher().pushOnlineMember(siger);
		}
		// 通知界面，更新用户签名关联参数
		for (Siger siger : a2) {
			getLauncher().updateOnlineMember(siger);
		}
		// 更新注册栏的成员数目
		getLauncher().updateStatusMembers();
	}
	
	/**
	 * 增加一个在线用户
	 * @param seat 用户基点
	 */
	protected void pushOnlineMember(FrontSeat seat) {
		ArrayList<FrontSeat> array = new ArrayList<FrontSeat>();
		array.add(seat);
		pushOnlineMember(array);
	}

//	/**
//	 * 增加一组基于某个节点的用户
//	 * @param node 节点地址
//	 * @param sigers 用户签名集合
//	 */
//	protected void pushOnlineMember(Node node, Collection<Siger> sigers) {
//		ArrayList<Seat> array = new ArrayList<Seat>();
//		for (Siger siger : sigers) {
//			Seat seat = new Seat(siger, node);
//			array.add(seat);
//		}
//		pushOnlineMember(array);
//	}

//	/**
//	 * 删除一组在线用户
//	 * @param seats 用户基点集合
//	 */
//	protected void dropOnlineMember(Collection<Seat> seats) {
//		ArrayList<Siger> array = new ArrayList<Siger>();
//
//		// 删除
//		for (Seat seat : seats) {
//			Siger siger = seat.getSiger();
//			// 判断存在
//			boolean b1 = OnlineMemberBasket.getInstance().contains(siger);
//			// 删除
//			OnlineMemberBasket.getInstance().remove(seat);
//			// 判断不存在
//			boolean b2 = OnlineMemberBasket.getInstance().contains(siger);
//			// 之前有，之后没有，保存！
//			if (b1 && !b2) {
//				array.add(siger);
//				
//				// 账号已经在注册成员池不存在，删除明文
//				boolean exists = RegisterMemberBasket.getInstance().contains(siger);
//				if (!exists) {
//					RegisterMemberBasket.getInstance().removePalinText(siger);
//				}
//			}
//		}
//
//		// 通知界面，删除相关用户签名
//		if (array.size() > 0) {
//			for (Siger siger : array) {
//				getLauncher().dropOnlineMember(siger);
//			}
//			// 更新注册栏的成员数目
//			getLauncher().updateStatusMembers();
//		}
//	}

	/**
	 * 删除一组在线用户
	 * @param seats 用户基点集合
	 */
	protected void dropOnlineMember(Collection<FrontSeat> seats) {
		ArrayList<Siger> a1 = new ArrayList<Siger>();
		ArrayList<Siger> a2 = new ArrayList<Siger>();

		// 删除
		for (FrontSeat seat : seats) {
			// 从内存中删除。是删除了最后一个，返回真；否则假。
			boolean success = PipeBasketTrustor.getInstance().dropOnlineMember(seat);
			if (success) {
				a1.add(seat.getSiger());
			} else {
				a2.add(seat.getSiger());
			}
		}

		// 通知界面，删除相关用户签名
		for (Siger siger : a1) {
			getLauncher().dropOnlineMember(siger);
		}
		// 通知界面，刷新用户签名相关的在线状态
		for (Siger siger : a2) {
			getLauncher().updateOnlineMember(siger);
		}
		// 更新注册栏的成员数目
		getLauncher().updateStatusMembers();
	}
	
	/**
	 * 删除一个在线用户
	 * @param seat 用户基点
	 */
	protected void dropOnlineMember(FrontSeat seat) {
		ArrayList<FrontSeat> array = new ArrayList<FrontSeat>();
		array.add(seat);
		dropOnlineMember(array);
	}

//	/**
//	 * 删除一组基于节点的用户
//	 * @param node 节点
//	 * @param sigers 用户签名集合
//	 */
//	protected void dropOnlineMember(Node node, Collection<Siger> sigers) {
//		ArrayList<Seat> array = new ArrayList<Seat>();
//		for (Siger siger : sigers) {
//			Seat seat = new Seat(siger, node);
//			array.add(seat);
//		}
//		dropOnlineMember(array);
//	}

	/**
	 * 删除一个节点和它下属全部用户签名
	 * @param node 节点地址
	 */
	protected void dropOnlineMember(Node node) {
		List<FrontSeat> seats = FrontMemberBasket.getInstance().find(node);
		dropOnlineMember(seats);
		
//		for (FrontSeat seat : seats) {
//			FrontMemberBasket.getInstance().remove(seat);
//		}

		// List<Siger> sigers = OnlineMemberBasket.getInstance().find(node);
		// if (sigers != null && sigers.size() > 0) {
		// dropOnlineMember(node, sigers);
		// }
	}

	/**
	 * 保存一个节点，同时显示在界面上
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	protected boolean pushSite(Node node) {
		boolean success = PipeBasketTrustor.getInstance().pushSite(node);
		
		// 成功，更新界面
		if (success) {
			getLauncher().pushSite(node);
		}
		return success;
	}

	/**
	 * 删除一个节点，同时从界面中删除
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	protected boolean dropSite(Node node) {
		boolean success = PipeBasketTrustor.getInstance().dropSite(node);
		// 成功，从图形界面删除
		if (success) {
			getLauncher().dropSite(node);
		}
		return success;
	}

	/**
	 * 销毁一个节点，同时从界面上删除
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	protected boolean destroySite(Node node) {
		boolean success = PipeBasketTrustor.getInstance().dropSite(node);
		// 成功，删除界面的显示
		if (success) {
			getLauncher().destroySite(node);
		}
		return success;
	}
}