/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.*;
import com.laxcus.ray.runtime.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.platform.*;
import com.laxcus.platform.listener.*;

/**
 * 投递节点用户信息给WATCH站点
 * 
 * @author scott.liang
 * @version 1.0 1/12/2020
 * @since laxcus 1.0
 */
public abstract class RayCastElementInvoker extends RayInvoker {

	/**
	 * 构造投递节点用户信息给WATCH站点调用器，指定命令
	 * @param cmd 相关命令
	 */
	protected RayCastElementInvoker(Command cmd) {
		super(cmd);
	}

	/**
	 * 找到用户注册的监听器
	 * @return 返回全部
	 */
	private WatchClient[] findListeners() {
		return PlatformKit.findListeners(WatchClient.class);
	}

	/**
	 * 增加一个注册用户
	 * @param seat 用户基点
	 */
	protected void pushRegisterMember(Seat seat) {
		// 先在本地记录它
		RayPipeBasketTrustor.getInstance().pushRegisterMember(seat);
		// 发送注册器
		WatchClient[] listeners = findListeners();
		if (listeners != null) {
			for (WatchClient as : listeners) {
				as.pushRegisterMember(seat);
			}
		}
	}

	/**
	 * 增加注册用户
	 * @param seats 用户基点集合
	 */
	protected void pushRegisterMember(Collection<Seat> seats) {
		// 增加
		for (Seat seat : seats) {
			pushRegisterMember(seat);
		}
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

	/**
	 * 删除一个注册用户
	 * @param seat 用户基点
	 */
	protected void dropRegisterMember(Seat seat) {
		RayPipeBasketTrustor.getInstance().dropRegisterMember(seat);
		
		// 找到全部监听器句柄，向它们发送
		WatchClient[] listeners = findListeners();
		if (listeners != null) {
			for (WatchClient as : listeners) {
				as.dropRegisterMember(seat);
			}
		}
	}
	
	/**
	 * 删除一组注册用户
	 * @param seats 用户基点集合
	 */
	protected void dropRegisterMember(Collection<Seat> seats) {
		// 删除
		for (Seat seat : seats) {
			dropRegisterMember(seat);
		}
	}

	/**
	 * 删除本地的注册成员
	 * @param node
	 */
	private void dropLocalRegisterMember(Node node) {
		java.util.List<Siger> sigers = RayRegisterMemberBasket.getInstance().find(node);
		if (sigers != null && sigers.size() > 0) {
			for (Siger siger : sigers) {
				Seat seat = new Seat(siger, node);
				RayPipeBasketTrustor.getInstance().dropRegisterMember(seat);
			}
		}
	}

	/**
	 * 删除一个节点和它下属全部用户签名
	 * @param node 节点地址
	 */
	protected void dropRegisterMember(Node node) {
		dropLocalRegisterMember(node);
		
		// 找到全部监听器句柄，向它们发送
		WatchClient[] listeners = findListeners();
		if (listeners != null) {
			for (WatchClient as : listeners) {
				as.dropRegisterMember(node);
			}
		}
	}
	
	/**
	 * 推送在线用户
	 * @param seat
	 */
	private void pushOnlineMember(FrontSeat seat) {
		// 本地记录
		RayPipeBasketTrustor.getInstance().pushOnlineMember(seat);
		
		// 找到全部监听器句柄，向它们发送
		WatchClient[] listeners = findListeners();
		if (listeners != null) {
			for (WatchClient as : listeners) {
				as.pushOnlineMember(seat);
			}
		}
	}
	
	/**
	 * 推送在线用户
	 * @param seats
	 */
	protected void pushOnlineMember(Collection<FrontSeat> seats) {
		for (FrontSeat seat : seats) {
			pushOnlineMember(seat);
		}
	}

	/**
	 * 删除在线用户
	 */
	private void dropOnlineMember(FrontSeat seat) {
		RayPipeBasketTrustor.getInstance().dropOnlineMember(seat);
		
		// 找到全部监听器句柄，向它们发送
		WatchClient[] listeners = findListeners();
		if (listeners != null) {
			for (WatchClient as : listeners) {
				as.dropOnlineMember(seat);
			}
		}
	}
	
	/**
	 * 删除一组在线用户
	 * @param seats 用户基点集合
	 */
	protected void dropOnlineMember(Collection<FrontSeat> seats) {
		for (FrontSeat seat : seats) {
			dropOnlineMember(seat);
		}
	}

	/**
	 * 根据节点，删除在线用户
	 * @param node
	 */
	private void dropLocalOnlineMember(Node node) {
		java.util.List<FrontSeat> seats = RayFrontMemberBasket.getInstance().find(node);
		if (seats != null && seats.size() > 0) {
			for (FrontSeat seat : seats) {
				RayPipeBasketTrustor.getInstance().dropOnlineMember(seat);
			}
		}
	}
	
	/**
	 * 删除一个节点和它下属全部用户签名
	 * @param node 节点地址
	 */
	protected void dropOnlineMember(Node node) {
		// 删除本地记录
		dropLocalOnlineMember(node);
		
		// 找到全部监听器句柄，向它们发送
		WatchClient[] listeners = findListeners();
		if (listeners != null) {
			for (WatchClient as : listeners) {
				as.dropOnlineMember(node);
			}
		}
	}

	/**
	 * 保存一个节点，同时显示在界面上
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	protected boolean pushSite(Node node) {
		boolean success = RayPipeBasketTrustor.getInstance().pushSite(node);

		// 找到全部监听器句柄，向它们发送
		WatchClient[] listeners = findListeners();
		boolean pass = (listeners != null && listeners.length > 0);
		if (pass) {
			for (WatchClient as : listeners) {
				as.pushSite(node);
			}
		}

		// 任何一个成立时
		return success || pass;
	}

	/**
	 * 删除一个节点，同时从界面中删除
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	protected boolean dropSite(Node node) {
		boolean success = RayPipeBasketTrustor.getInstance().dropSite(node);
		
		// 找到全部监听器句柄，向它们发送
		WatchClient[] listeners = findListeners();
		boolean pass = (listeners != null && listeners.length > 0);
		if (pass) {
			for (WatchClient as : listeners) {
				as.dropSite(node);
			}
		}
		// 任何一个成立时
		return success || pass;
	}

	/**
	 * 销毁一个节点，同时从界面上删除
	 * @param node 节点地址
	 * @return 成功返回真，否则假
	 */
	protected boolean destroySite(Node node) {
		// 删除本地
		boolean success = RayPipeBasketTrustor.getInstance().dropSite(node);
		
		// 找到全部监听器句柄，向它们发送
		WatchClient[] listeners = findListeners();
		boolean pass = (listeners != null && listeners.length > 0);
		if (pass) {
			for (WatchClient as : listeners) {
				as.destroySite(node);
			}
		}
		// 任何一个成立时
		return success || pass;
	}

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

//private void pushRegisterMember(Siger siger) {
//	WatchMonitorListener[] listeners = findListeners();
//	if (listeners != null) {
//		for (WatchMonitorListener ds : listeners) {
//			ds.pushRegisterMember(siger);
//		}
//	}
//}

//private void pushRegisterMember(Seat siger) {
//	WatchMonitorListener[] listeners = findListeners();
//	if (listeners != null) {
//		for (WatchMonitorListener ds : listeners) {
//			ds.pushRegisterMember(siger);
//		}
//	}
//}

//private void updateStatusMembers() {
//	WatchMonitorListener[] listeners = findListeners();
//	if (listeners != null) {
//		for (WatchMonitorListener ds : listeners) {
//			ds.updateStatusMembers();
//		}
//	}
//}


//	/**
//	 * 增加注册用户
//	 * @param seats 用户基点集合
//	 */
//	protected void pushRegisterMember(Collection<Seat> seats) {
//		ArrayList<Siger> array = new ArrayList<Siger>();
//
//		// 增加
//		for (Seat seat : seats) {
//			// 保存到内存
//			boolean success = RayPipeBasketTrustor.getInstance().pushRegisterMember(seat);
//			if (success) {
//				array.add(seat.getSiger());
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

//private void dropRegisterMember(Siger siger) {
//	// 找到全部监听器句柄，向它们发送
//	WatchMonitorListener[] listeners = findListeners();
//	if (listeners != null) {
//		for (WatchMonitorListener ds : listeners) {
////			ds.dropRegisterMember(siger);
//		}
//	}
//}
//
///**
// * 删除一组注册用户
// * @param seats 用户基点集合
// */
//protected void dropRegisterMember(Collection<Seat> seats) {
//	ArrayList<Siger> array = new ArrayList<Siger>();
//
//	// 删除
//	for (Seat seat : seats) {
//		// 从内存中删除
//		boolean success = RayPipeBasketTrustor.getInstance()
//		.dropRegisterMember(seat);
//		// 成功，保存签名
//		if (success) {
//			array.add(seat.getSiger());
//		}
//	}
//
//	// 通知界面，删除相关用户签名
//	if (array.size() > 0) {
//		for (Siger siger : array) {
//			dropRegisterMember(siger);
//		}
//		// 更新注册栏的成员数目
//		updateStatusMembers();
//	}
//}

//	/**
//	 * 删除一组注册用户
//	 * @param seats 用户基点集合
//	 */
//	protected void dropRegisterMember(Collection<Seat> seats) {
//		ArrayList<Siger> array = new ArrayList<Siger>();
//
//		// 删除
//		for (Seat seat : seats) {
//			// 从内存中删除
//			boolean success = RayPipeBasketTrustor.getInstance()
//					.dropRegisterMember(seat);
//			// 成功，保存签名
//			if (success) {
//				array.add(seat.getSiger());
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

///**
// * 删除一个注册用户
// * @param seat 用户基点
// */
//protected void dropRegisterMember(Seat seat) {
//	ArrayList<Seat> array = new ArrayList<Seat>();
//	array.add(seat);
//	dropRegisterMember(array);
//}


///**
// * 删除一组基于节点的用户
// * @param node 节点
// * @param sigers 用户签名集合
// */
//protected void dropRegisterMember(Node node, Collection<Siger> sigers) {
//	ArrayList<Seat> array = new ArrayList<Seat>();
//	for (Siger siger : sigers) {
//		Seat seat = new Seat(siger, node);
//		array.add(seat);
//	}
//	dropRegisterMember(array);
//}

///**
// * 删除一个节点和它下属全部用户签名
// * @param node 节点地址
// */
//protected void dropRegisterMember(Node node) {
//	List<Siger> sigers = RayRegisterMemberBasket.getInstance().find(node);
//	if (sigers != null && sigers.size() > 0) {
//		dropRegisterMember(node, sigers);
//	}
//}

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


//private void updateOnlineMember(Siger siger) {
//	// 找到全部监听器句柄，向它们发送
//	WatchMonitorListener[] listeners = findListeners();
//	if (listeners != null) {
//		for (WatchMonitorListener ds : listeners) {
//			ds.updateOnlineMember(siger);
//		}
//	}
//}
//
///**
// * 增加在线用户
// * @param seats 用户基点集合
// */
//protected void pushOnlineMember(Collection<FrontSeat> seats) {
//	ArrayList<Siger> a1 = new ArrayList<Siger>();
//	ArrayList<Siger> a2 = new ArrayList<Siger>();
//
//	// 增加
//	for (FrontSeat seat : seats) {
//		// 保存！如果是第一次返回真，否则假
//		boolean success = RayPipeBasketTrustor.getInstance().pushOnlineMember(seat);
//		if (success) {
//			a1.add(seat.getSiger());
//		} else {
//			a2.add(seat.getSiger());
//		}
//	}
//
//	// 通知界面，增加新的用户签名
//	for (Siger siger : a1) {
//		pushOnlineMember(siger);
//	}
//	// 通知界面，更新用户签名关联参数
//	for (Siger siger : a2) {
//		updateOnlineMember(siger);
//	}
//	// 更新注册栏的成员数目
//	updateStatusMembers();
//}


//	/**
//	 * 增加在线用户
//	 * @param seats 用户基点集合
//	 */
//	protected void pushOnlineMember(Collection<FrontSeat> seats) {
//		ArrayList<Siger> a1 = new ArrayList<Siger>();
//		ArrayList<Siger> a2 = new ArrayList<Siger>();
//
//		// 增加
//		for (FrontSeat seat : seats) {
//			// 保存！如果是第一次返回真，否则假
//			boolean success = RayPipeBasketTrustor.getInstance().pushOnlineMember(seat);
//			if (success) {
//				a1.add(seat.getSiger());
//			} else {
//				a2.add(seat.getSiger());
//			}
//		}
//
//		// 通知界面，增加新的用户签名
//		for (Siger siger : a1) {
//			getLauncher().pushOnlineMember(siger);
//		}
//		// 通知界面，更新用户签名关联参数
//		for (Siger siger : a2) {
//			getLauncher().updateOnlineMember(siger);
//		}
//		// 更新注册栏的成员数目
//		getLauncher().updateStatusMembers();
//	}

///**
// * 增加一个在线用户
// * @param seat 用户基点
// */
//protected void pushOnlineMember(FrontSeat seat) {
//	ArrayList<FrontSeat> array = new ArrayList<FrontSeat>();
//	array.add(seat);
//	pushOnlineMember(array);
//}

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


//private void updateOnlineMember(Siger siger) {
//	// 找到全部监听器句柄，向它们发送
//	WatchMonitorListener[] listeners = findListeners();
//	if (listeners != null) {
//		for (WatchMonitorListener ds : listeners) {
//			ds.updateOnlineMember(siger);
//		}
//	}
//}
//
///**
// * 增加在线用户
// * @param seats 用户基点集合
// */
//protected void pushOnlineMember(Collection<FrontSeat> seats) {
//	ArrayList<Siger> a1 = new ArrayList<Siger>();
//	ArrayList<Siger> a2 = new ArrayList<Siger>();
//
//	// 增加
//	for (FrontSeat seat : seats) {
//		// 保存！如果是第一次返回真，否则假
//		boolean success = RayPipeBasketTrustor.getInstance().pushOnlineMember(seat);
//		if (success) {
//			a1.add(seat.getSiger());
//		} else {
//			a2.add(seat.getSiger());
//		}
//	}
//
//	// 通知界面，增加新的用户签名
//	for (Siger siger : a1) {
//		pushOnlineMember(siger);
//	}
//	// 通知界面，更新用户签名关联参数
//	for (Siger siger : a2) {
//		updateOnlineMember(siger);
//	}
//	// 更新注册栏的成员数目
//	updateStatusMembers();
//}


//	/**
//	 * 增加在线用户
//	 * @param seats 用户基点集合
//	 */
//	protected void pushOnlineMember(Collection<FrontSeat> seats) {
//		ArrayList<Siger> a1 = new ArrayList<Siger>();
//		ArrayList<Siger> a2 = new ArrayList<Siger>();
//
//		// 增加
//		for (FrontSeat seat : seats) {
//			// 保存！如果是第一次返回真，否则假
//			boolean success = RayPipeBasketTrustor.getInstance().pushOnlineMember(seat);
//			if (success) {
//				a1.add(seat.getSiger());
//			} else {
//				a2.add(seat.getSiger());
//			}
//		}
//
//		// 通知界面，增加新的用户签名
//		for (Siger siger : a1) {
//			getLauncher().pushOnlineMember(siger);
//		}
//		// 通知界面，更新用户签名关联参数
//		for (Siger siger : a2) {
//			getLauncher().updateOnlineMember(siger);
//		}
//		// 更新注册栏的成员数目
//		getLauncher().updateStatusMembers();
//	}

///**
// * 增加一个在线用户
// * @param seat 用户基点
// */
//protected void pushOnlineMember(FrontSeat seat) {
//	ArrayList<FrontSeat> array = new ArrayList<FrontSeat>();
//	array.add(seat);
//	pushOnlineMember(array);
//}

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



///**
// * 删除一组在线用户
// * @param seats 用户基点集合
// */
//protected void dropOnlineMember(Collection<FrontSeat> seats) {
//	ArrayList<Siger> a1 = new ArrayList<Siger>();
//	ArrayList<Siger> a2 = new ArrayList<Siger>();
//
//	// 删除
//	for (FrontSeat seat : seats) {
//		// 从内存中删除。是删除了最后一个，返回真；否则假。
//		boolean success = RayPipeBasketTrustor.getInstance().dropOnlineMember(seat);
//		if (success) {
//			a1.add(seat.getSiger());
//		} else {
//			a2.add(seat.getSiger());
//		}
//	}
//
//	// 通知界面，删除相关用户签名
//	for (Siger siger : a1) {
//		dropOnlineMember(siger);
//	}
//	// 通知界面，刷新用户签名相关的在线状态
//	for (Siger siger : a2) {
//		updateOnlineMember(siger);
//	}
//	// 更新注册栏的成员数目
//	updateStatusMembers();
//}

//	/**
//	 * 删除一组在线用户
//	 * @param seats 用户基点集合
//	 */
//	protected void dropOnlineMember(Collection<FrontSeat> seats) {
//		ArrayList<Siger> a1 = new ArrayList<Siger>();
//		ArrayList<Siger> a2 = new ArrayList<Siger>();
//
//		// 删除
//		for (FrontSeat seat : seats) {
//			// 从内存中删除。是删除了最后一个，返回真；否则假。
//			boolean success = RayPipeBasketTrustor.getInstance().dropOnlineMember(seat);
//			if (success) {
//				a1.add(seat.getSiger());
//			} else {
//				a2.add(seat.getSiger());
//			}
//		}
//
//		// 通知界面，删除相关用户签名
//		for (Siger siger : a1) {
//			getLauncher().dropOnlineMember(siger);
//		}
//		// 通知界面，刷新用户签名相关的在线状态
//		for (Siger siger : a2) {
//			getLauncher().updateOnlineMember(siger);
//		}
//		// 更新注册栏的成员数目
//		getLauncher().updateStatusMembers();
//	}

///**
// * 删除一个在线用户
// * @param seat 用户基点
// */
//protected void dropOnlineMember(FrontSeat seat) {
//	ArrayList<FrontSeat> array = new ArrayList<FrontSeat>();
//	array.add(seat);
//	dropOnlineMember(array);
//}

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

///**
// * 删除一个节点和它下属全部用户签名
// * @param node 节点地址
// */
//protected void dropOnlineMember(Node node) {
//	List<FrontSeat> seats = RayFrontMemberBasket.getInstance().find(node);
//	dropOnlineMember(seats);
//
//	//		for (FrontSeat seat : seats) {
//	//			FrontMemberBasket.getInstance().remove(seat);
//	//		}
//
//	// List<Siger> sigers = OnlineMemberBasket.getInstance().find(node);
//	// if (sigers != null && sigers.size() > 0) {
//	// dropOnlineMember(node, sigers);
//	// }
//}


///**
// * 保存一个节点，同时显示在界面上
// * @param node 节点地址
// * @return 成功返回真，否则假
// */
//protected boolean pushSite(Node node) {
//	boolean success = RayPipeBasketTrustor.getInstance().pushSite(node);
//
////	// 成功，更新界面
////	if (success) {
////		// 找到全部监听器句柄，向它们发送
////		WatchMonitorListener[] listeners = findListeners();
////		if (listeners != null) {
////			for (WatchMonitorListener ds : listeners) {
////				ds.pushSite(node);
////			}
////		}
////	}
//	
//	// 找到全部监听器句柄，向它们发送
//	WatchMonitorListener[] listeners = findListeners();
//	if (listeners != null) {
//		for (WatchMonitorListener as : listeners) {
//			as.pushSite(node);
//		}
//	}
//	
//	return success;
//}

//	/**
//	 * 保存一个节点，同时显示在界面上
//	 * @param node 节点地址
//	 * @return 成功返回真，否则假
//	 */
//	protected boolean pushSite(Node node) {
//		boolean success = RayPipeBasketTrustor.getInstance().pushSite(node);
//		
//		// 成功，更新界面
//		if (success) {
//			getLauncher().pushSite(node);
//		}
//		return success;
//	}

///**
// * 删除一个节点，同时从界面中删除
// * @param node 节点地址
// * @return 成功返回真，否则假
// */
//protected boolean dropSite(Node node) {
//	boolean success = RayPipeBasketTrustor.getInstance().dropSite(node);
//	// 成功，从图形界面删除
//	if (success) {
//		// 找到全部监听器句柄，向它们发送
//		WatchMonitorListener[] listeners = findListeners();
//		if (listeners != null) {
//			for (WatchMonitorListener ds : listeners) {
//				ds.dropSite(node);
//			}
//		}
//	}
//	return success;
//}


//	/**
//	 * 删除一个节点，同时从界面中删除
//	 * @param node 节点地址
//	 * @return 成功返回真，否则假
//	 */
//	protected boolean dropSite(Node node) {
//		boolean success = RayPipeBasketTrustor.getInstance().dropSite(node);
//		// 成功，从图形界面删除
//		if (success) {
//			getLauncher().dropSite(node);
//		}
//		return success;
//	}


///**
// * 销毁一个节点，同时从界面上删除
// * @param node 节点地址
// * @return 成功返回真，否则假
// */
//protected boolean destroySite(Node node) {
//	boolean success = RayPipeBasketTrustor.getInstance().dropSite(node);
//	// 成功，删除界面的显示
//	if (success) {
//		// 找到全部监听器句柄，向它们发送
//		WatchMonitorListener[] listeners = findListeners();
//		if (listeners != null) {
//			for (WatchMonitorListener ds : listeners) {
//				ds.destroySite(node);
//			}
//		}
//	}
//	return success;
//}

//	/**
//	 * 销毁一个节点，同时从界面上删除
//	 * @param node 节点地址
//	 * @return 成功返回真，否则假
//	 */
//	protected boolean destroySite(Node node) {
//		boolean success = RayPipeBasketTrustor.getInstance().dropSite(node);
//		// 成功，删除界面的显示
//		if (success) {
//			getLauncher().destroySite(node);
//		}
//		return success;
//	}
