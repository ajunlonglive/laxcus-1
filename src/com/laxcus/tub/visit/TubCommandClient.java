/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.visit;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

import com.laxcus.tub.*;
import com.laxcus.tub.client.*;
import com.laxcus.tub.command.*;
import com.laxcus.tub.product.*;
import com.laxcus.tub.turn.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;

/**
 * 边缘计算远程RPC命令客户机
 * 
 * @author scott.liang
 * @version 1.0 10/13/2020
 * @since laxcus 1.0
 */
public class TubCommandClient implements TubVisit {

	private static Method methodSubmit;

	static {
		try {
			TubCommandClient.methodSubmit = (TubVisit.class).getMethod("submit", new Class<?>[] { TubCommand.class });
		} catch (NoSuchMethodException e) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.visit.TubVisit#submit(com.laxcus.tub.visit.TubRequest)
	 */
	@Override
	public TubProduct submit(TubCommand cmd) throws TubVisitException {
		// 向服务端发送命令，服务端结果
		Object[] params = new Object[] { cmd };
		Object param = invoke(TubCommandClient.methodSubmit, params);
		return (TubProduct) param;
	}

	/**
	 * 远程方法调用
	 * @param method 被操作的远程方法
	 * @param params 参数
	 * @return 返回对象结果
	 * @throws VisitException - 如果出错错误，弹出异常
	 */
	public Object invoke(Method method, Object[] params) throws TubVisitException {
		try {
			return swap(method, params);
		} catch (IOException e) {
			String prefix = String.format("goto %s | ", getRemote());
			throw new TubVisitException(prefix + getThrowableText(e));
		} catch (Throwable e) {
			String prefix = String.format("goto %s | ", getRemote());
			throw new TubVisitException(prefix + getThrowableText(e));
		}
	}

	/**
	 * 根据接口方法、数据进行远程调用（接口名称已经固定，不允许修改）。
	 * @param method 远程方法
	 * @param params 方法关联参数
	 * @return 返回对象
	 * @throws IOException
	 */
	private Object swap(Method method, Object[] params) throws TubVisitException, IOException {
		//1. 串行化请求数据
		String methodName = method.getName();
		Class<?>[] paramTypes = method.getParameterTypes();
		TubConstructor creator = new TubConstructor(visitName, methodName, paramTypes, params);
		byte[] data = creator.build();

		// 2. 生成FIXP请求数据，发送，然后接收反馈数据
		Mind cmd = new Mind(MindAsk.RPC, MindAsk.EXECUTE);
		if (streamClient != null) {
			TubStream request = new TubStream(streamClient.getRemote(), cmd);
			// 保存串行化数据(TCP模式)
			request.setData(data);
			// 3. 发送和接收数据
			TubStream resp = streamClient.swap(request, true);
			data = resp.getData();
		} else {
			throw new IOException("null!");
		}

		//4. 解析应答数据和截获可能发生的错误
		TubExtractor reply = null;
		try {
			reply = TubExtractor.resolve(data);
		} catch (Throwable e) {
			throw new TubVisitException(e);
		}
		if (reply == null) {
			throw new TubVisitException("reply is null pointer!");
		}
		// 弹出错误
		if (reply.getThrowable() != null) {
			throw new TubVisitException(reply.getThrowText());
		}
		// 5. 返回处理结果
		return reply.getObject();
	}

	/** FIXP TCP连接器  */
	private TubStreamClient streamClient;

	/** Visit接口子类名 **/
	private String visitName;

	/**
	 * 构造边缘计算远程RPC命令客户机
	 */
	public TubCommandClient() {
		super();
		setVisitName(TubVisit.class.getName());
		streamClient = new TubStreamClient();
	}

	/**
	 * 构造边缘计算远程RPC命令客户机，指定服务器地址。
	 * @param endpoint 服务器地址
	 */
	public TubCommandClient(SocketHost endpoint) {
		this();
		setRemote(endpoint);
	}

	/**
	 * 设置远程访问的接口名称（接口必须从Visit派生）
	 * @param e 接口名称
	 */
	public void setVisitName(String e) {
		visitName = e;
	}

	/**
	 * 返回JAVA类实现的接口名称
	 * @return 字符串接口名称
	 */
	public String getVisitName() {
		return visitName;
	}

	/**
	 * 设置服务器地址
	 * @param endpoint 目标主机
	 * @throws IllegalArgumentException，如果连接模式不匹配，弹出异常。
	 */
	public void setRemote(SocketHost endpoint) {
		if (endpoint.isStream() && streamClient != null) {
			streamClient.setRemote(endpoint);
		} else {
			throw new IllegalArgumentException("illegal socket");
		}
	}

	/**
	 * 返回连接的目标服务器地址
	 * @return SocketHost实例
	 */
	public SocketHost getRemote() {
		if (streamClient != null) {
			return streamClient.getRemote();
		} 
		return null;
	}

	/**
	 * 返回目标服务器IP地址
	 * @return InetAddress实例
	 */
	public InetAddress getRemoteAddress() {
		if (streamClient != null) {
			return streamClient.getRemote().getInetAddress();
		} 
		return null;
	}

	/**
	 * 返回当前SOCKET绑定的地址
	 * @return SocketHost实例
	 */
	public SocketHost getLocal() {
		if (streamClient != null) {
			return streamClient.getLocal();
		} 
		return null;
	}

	/**
	 * 输出异常信息
	 * @return 字符串异常文本
	 */
	private String getThrowableText(Throwable fatal) {
		if (fatal == null) return "";
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		PrintStream s = new PrintStream(buff, true);
		fatal.printStackTrace(s);
		byte[] b = buff.toByteArray();
		return new String(b, 0, b.length);
	}

	/**
	 * 重新连接
	 * @throws IOException
	 */
	public void reconnect() throws IOException {
		if (streamClient != null) {
			try {
				streamClient.connect(streamClient.getRemote());
			} catch (IOException e) {
				String prefix = String.format("to %s | ", streamClient.getRemote());
				throw new IOException(prefix + getThrowableText(e));
			}
		} else {
			throw new IOException("null!");
		}
	}

	/**
	 * 连接目标服务器
	 * @param endpoint 服务器地址
	 * @throws IOException
	 */
	public void connect(SocketHost endpoint) throws IOException {
		if (streamClient != null) {
			streamClient.connect(endpoint);
		} else {
			throw new IOException("null!");
		}
	}

	/**
	 * 判断是否已经连接成功
	 * @return 返回真或者假
	 */
	public boolean isConnected() {
		if (streamClient != null) {
			return streamClient.isConnected();
		} 
		return false;
	}

	/**
	 * 判断是否已经关闭
	 * @return 返回真或者假
	 */
	public boolean isClosed() {
		if (streamClient != null) {
			return streamClient.isClosed();
		}
		return true;
	}

	/**
	 * 柔性关闭连接，首先通知服务器结束对话，然后关闭SOCKET
	 * @param exit 发出“exit”指令
	 */
	public void close(boolean exit) {
		if (streamClient != null) {
			streamClient.close(exit);
		}
	}

	/**
	 * 柔性关闭连接，首先通知服务器结束对话，然后关闭SOCKET
	 */
	public void close() {
		close(true);
	}
}
