/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl;

import java.util.*;

import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.lock.*;
import com.laxcus.visit.*;

/**
 * 异步数据代理。 RPC异步应答数据转发器，或者称“RPC异步应答数据代理”。<br><br>
 * 
 * 这个类实现“EchoVisit”接口，在所有站点上运行，在站点启动时注册FIXP监听器中。处于异步命令请求端的位置。
 * 它接收来自服务端，通常由“EchoClient”发送的异步应答数据。<br><br>
 * 
 * “异步数据代理”只负责数据转发工作，它通过回显标识找到每个“异步数据接收器（EchoReceiver）”，将数据转发给它们。
 * 
 * 这个类是“EchoVisit”接口的服务端实现，在所有节点上运行，处于异步命令请求端的位置。<br>
 * 它接收来自应答方，由“EchoClient”发送的异步应答数据包。
 * 根据“EchoClient”传递的回显标识，找到注册的异步数据接收器（EchoReceiver），将数据转发给它。<br>
 * 
 * @author scott.liang
 * @version 1.0 03/09/2009
 * @since laxcus 1.0
 */
public final class EchoAgent extends MutexHandler implements EchoVisit {

	/** 等待中的异步数据接收器 **/
	private Map<EchoFlag, EchoReceiver> brothers = new TreeMap<EchoFlag, EchoReceiver>();

	/**
	 * 构造异步RPC转发器
	 */
	public EchoAgent() {
		super();
	}

	/**
	 * 注册一个异步数据接收器
	 * @param receiver  EchoReceiver实现实例
	 * @return  注册成功返回真，否则返回假。
	 */
	public boolean regsiter(EchoReceiver receiver) {
		EchoFlag flag = receiver.getFlag();
		// 不允许注册空值
		if (flag == null) {
			return false;
		}
		boolean success = false;
		// 单向锁定
		this.lockSingle();
		try {
			success = (brothers.put(flag, receiver) == null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			this.unlockSingle();
		}
		return success;
	}

	/**
	 * 注销一个异步数据接收器
	 * @param receiver  EchoReceiver实例
	 * @return 注销成功返回真，否则返回假。
	 */
	public boolean unregsiter(EchoReceiver receiver) {
		EchoFlag flag = receiver.getFlag();
		// 如果出现空值，表示已经解除绑定
		if (flag == null) {
			return false;
		}

		boolean success = false;
		// 单向锁定
		this.lockSingle();
		try {
			success = (brothers.remove(flag) != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			this.unlockSingle();
		}
		return success;
	}

	/**
	 * 检查一个注册的异步数据接收器（EchoBuffer或者DoubleClient）是否存在
	 * @param receiver EchoReceiver实现实例
	 * @return 返回真或者假
	 */
	public boolean contains(EchoReceiver receiver) {
		EchoFlag flag = receiver.getFlag();
		if (flag == null) {
			return false;
		}
		boolean success = false;
		this.lockMulti();
		try {
			EchoReceiver that = brothers.get(flag);
			success = (that != null && that == receiver);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			this.unlockMulti();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		// 释放全部注册
		for (EchoReceiver e : brothers.values()) {
			e.halt();
		}
	}

	/**
	 * 在锁定状态下，根据异步回显标识，找到关联的异步数据接收器
	 * @param flag EchoFlag实例
	 * @return 返回EchoReceiver实现实例
	 */
	private EchoReceiver find(EchoFlag flag) {
		this.lockMulti();
		try {
			return brothers.get(flag);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			this.unlockMulti();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.EchoVisit#start(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoHead)
	 */
	@Override
	public boolean start(EchoFlag flag, EchoHead head) throws VisitException {
		// 无效
		if (brothers.isEmpty()) {
			return false;
		}

		EchoReceiver receiver = find(flag);
		// 通知异步数据接收器，启动异步数据接收
		boolean success = (receiver != null);
		if (success) {
			success = receiver.start(head);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.EchoVisit#push(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoField)
	 */
	@Override
	public long push(EchoFlag flag, EchoField field) throws VisitException {
		// 无效
		if (brothers.isEmpty()) {
			return -1L;
		}

		EchoReceiver receiver = find(flag);
		// 将异步应答数据包交给异步数据接收器
		boolean success = (receiver != null);
		if (success) {
			return receiver.push(field);
		}
		// 出错
		return -1L;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.EchoVisit#stop(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoTail)
	 */
	@Override
	public boolean stop(EchoFlag flag, EchoTail tail) throws VisitException {
		// 无效
		if (brothers.isEmpty()) {
			return false;
		}

		EchoReceiver receiver = find(flag);
		// 通知异步数据接收器，完成异步接收
		boolean success = (receiver != null);
		if (success) {
			success = receiver.stop(tail);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.EchoVisit#start(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoHead)
	 */
	@Override
	public CastToken cast(EchoFlag flag, EchoHead head) throws VisitException {
		// 无效
		if (brothers.isEmpty()) {
			return null;
		}
		
//		Logger.debug(this, "cast", "查找标识：%s", flag);

		EchoReceiver receiver = find(flag);
		CastToken token = null;
		// 通知异步数据接收器，启动快速异步通信
		if (receiver != null) {
			token = receiver.cast(head);
		}
		
//		Logger.debug(this, "cast", "查找标识：%s 本地监听：%s", flag, (token != null ? token.getHost() : "无效!"));
		
		return token;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.EchoVisit#stop(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoTail)
	 */
	@Override
	public boolean exit(EchoFlag flag, EchoTail tail) throws VisitException {
		// 无效
		if (brothers.isEmpty()) {
			return false;
		}

		EchoReceiver receiver = find(flag);
		// 通知异步数据接收器，关闭快速异步通信
		boolean success = (receiver != null);
		if (success) {
			success = receiver.exit(tail);
		}
		return success;
	}

}