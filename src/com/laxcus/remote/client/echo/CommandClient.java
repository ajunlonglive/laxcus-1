/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.echo;

import java.lang.reflect.*;

import com.laxcus.command.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;

/**
 * 异步命令客户端。<br>
 * 
 * 提供网络环境下的异步命令操作服务。
 * 
 * @author scott.liang
 * @version 1.0 7/28/2009
 * @since laxcus 1.0
 */
public class CommandClient extends DoubleClient implements CommandVisit {

	private static Method methodSubmit;

	static {
		try {
			CommandClient.methodSubmit = (CommandVisit.class).getMethod("submit", new Class<?>[] { Command.class });
		} catch (NoSuchMethodException e) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/**
	 * 构造异步命令客户端，指定传输模式
	 * @param stream 流传输模式
	 */
	public CommandClient(boolean stream) {
		super(stream, CommandVisit.class.getName());
	}

	/**
	 * 构造异步命令客户端，指定服务器地址。
	 * @param endpoint 服务器地址
	 */
	public CommandClient(SocketHost endpoint) {
		this(endpoint.isStream());
		setRemote(endpoint);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		// 必须有回显地址
		if (cmd.getSource() == null) {
			throw new VisitException("cannot be null for echo site");
		}

		// 向服务端发送命令，服务端返回“接受/不接受”的布尔值
		Object[] params = new Object[] { cmd };
		Object param = invoke(CommandClient.methodSubmit, params);
		return ((Boolean) param).booleanValue();
	}

	//	/**
	//	 * 向服务端发送异步命令，同时等待服务端的异步应答。实现一个完整的异步处理全过程。<br>
	//	 * 这个方法是在调用“submit”方法的基础上，加上等待和接收异步应答结果。
	//	 * @param cmd 异步操作命令
	//	 * @return 返回异步应答对象
	//	 * @throws VisitException
	//	 */
	//	public Object reflect(Command cmd) throws VisitException {
	//		// 绑定到异步代理
	//		if (!super.attach(cmd)) {
	//			throw new VisitException("attach failed");
	//		}
	//
	//		// 向服务端提交异步命令，服务端返回“接受/不接受”的布尔值
	//		boolean accepted = submit(cmd);
	//		// 关闭网络连接
	//		super.close();
	//		// 不接受，解除锁定和弹出异常
	//		if (!accepted) {
	//			detach();
	//			throw new VisitException("cannot be accept");
	//		}
	//
	//		// 等待异步应答结果，直到完成。(异步数据写入内存或者硬盘）
	//		while (!isCompleted()) {
	//			delay(50);
	//		}
	//
	//		Logger.debug(this, "reflect", "head:(%s), tail:(%s)", getHead(), getTail());
	//
	//		// 返回对象
	//		return super.getObject();
	//	}
	//
	//	/**
	//	 * 在“reflect(Command)”方法基础上，对反馈的类进行强制转换和输出
	//	 * @param <T> 类定义
	//	 * @param cmd 请求命令
	//	 * @param clazz 反馈类的类型
	//	 * @return 返回的结果实例
	//	 * @throws VisitException
	//	 */
	//	@SuppressWarnings("unchecked")
	//	public <T> T reflect(Command cmd, java.lang.Class<?> clazz) throws VisitException {
	//		Object that = reflect(cmd);
	//		// 判断对象有效
	//		if (that != null) {
	//			return (T) that;
	//		}
	//		// 返回空指针
	//		return null;
	//	}

}