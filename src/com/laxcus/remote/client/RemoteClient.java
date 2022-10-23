/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

import com.laxcus.fixp.*;
import com.laxcus.fixp.client.*;
import com.laxcus.remote.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;

/**
 * RPC远程访问客户端。<br>
 * 
 * RemoteClient在构造时绑定一个Visit接口命名，选择TCP或者KEEP UDP中的一种做为它的连接模式，实现RPC客户端服务。<br><br>
 * 
 * 
 * @author scott.liang
 * @version 1.3 2/6/2013
 * @since laxcus 1.0
 * 
 * @see com.laxcus.fixp
 * @see com.laxcus.fixp.client.FixpStreamClient
 * @see com.laxcus.fixp.client.FixpPacketClient
 */
public class RemoteClient {

	/** FIXP TCP连接器 */
	private FixpStreamClient streamClient;

	/** FIXP UDP连接器（发送KEEP UDP数据包）*/
	private FixpPacketClient packetClient;

	/** Visit接口子类名 **/
	private String visitName;

	/** 启动时间 **/
	private long launchTime = System.currentTimeMillis();

	/**
	 * 构造一个远程访问客户端，指定网络连接模式
	 * @param stream 数据流模式或者否(TCP/KEEP UDP)
	 */
	protected RemoteClient(boolean stream) {
		super();
		// 流/包模式
		if (stream) {
			streamClient = new FixpStreamClient();
		} else {
			packetClient = new FixpPacketClient();
		}
	}

	/**
	 * 构造一个远程访问客户端，指定网络连接模式和远程访问的接口名称
	 * @param stream 数据流模式
	 * @param interfaceName 远程访问的接口名称（Visit的子接口）
	 */
	protected RemoteClient(boolean stream, String interfaceName) {
		this(stream);
		setVisitName(interfaceName);
	}

	/**
	 * 返回启用耗时
	 * @return 长整型
	 */
	public long getLaunchTime() {
		return System.currentTimeMillis() - launchTime;
	}

	/**
	 * 判断是“数据流模式“（TCP）
	 * @return 是数据流模式返回”真“，否则“假”。
	 */
	public boolean isStream() {
		return streamClient != null;
	}

	/**
	 * 判断是“包模式“(KEEP UDP)
	 * @return 是包模式返回“真”，否则“假”。
	 */
	public boolean isPacket() {
		return packetClient != null;
	}

	/**
	 * 是否执行了安全通信
	 * @return 返回真或者假
	 */
	public boolean isSecured() {
		if(streamClient != null) {
			return streamClient.isSecured();
		} else {
			return packetClient.isSecured();
		}
	}

	/**
	 * 客户端延时
	 * @param timeout 延时时间，单位:毫秒
	 */
	public synchronized void delay(long timeout) {
		try {
			wait(timeout);
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.error(e);
		}
	}

	/**
	 * 唤醒线程
	 */
	public synchronized void wakeup() {
		try {
			notify();
		} catch (IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.error(e);
		}
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
	 * 设置为毫秒为单位的SOCKET接收超时间
	 * @param ms 单位：毫秒
	 */
	public int setReceiveTimeout(int ms) {
		if (streamClient != null) {
			return streamClient.setReceiveTimeout(ms);
		} else {
			return packetClient.setReceiveTimeout(ms);
		}
	}

	/**
	 * 返回SOCKET接收超时
	 * @return 接收超时时间，单位：秒
	 */
	public int getReceiveTimeout() {
		if (streamClient != null) {
			return streamClient.getReceiveTimeout();
		} else {
			return packetClient.getReceiveTimeout();
		}
	}

	/**
	 * 设置UDP模式的子包接收超时。TCP模式忽略。
	 * @param second 单位：秒
	 */
	public void setSubPacketReceiveTimeout(int second) {
		if (packetClient != null) {
			packetClient.setSubPacketTimeout(second * 1000);
		}
	}

	/**
	 * 返回UDP模式的子包接收超时。TCP模式返回0。
	 * @return UDP子包接收超时时间，单位：秒
	 */
	public int getSubPacketReceiveTimeout() {
		if (packetClient != null) {
			return packetClient.getSubPacketTimeout() / 1000;
		}
		return 0;
	}

	/**
	 * 设置服务器地址
	 * @param endpoint 目标主机
	 * @throws IllegalArgumentException，如果连接模式不匹配，弹出异常。
	 */
	public void setRemote(SocketHost endpoint) {
		if (endpoint.isStream() && streamClient != null) {
			streamClient.setRemote(endpoint);
		} else if (endpoint.isPacket() && packetClient != null) {
			packetClient.setRemote(endpoint);
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
		} else {
			return packetClient.getRemote();
		}
	}

	/**
	 * 返回目标服务器IP地址
	 * @return InetAddress实例
	 */
	public InetAddress getRemoteAddress() {
		if (streamClient != null) {
			return streamClient.getRemote().getInetAddress();
		} else {
			return packetClient.getRemote().getInetAddress();
		}
	}

	/**
	 * 返回当前SOCKET绑定的地址
	 * @return SocketHost实例
	 */
	public SocketHost getLocal() {
		if (streamClient != null) {
			return streamClient.getLocal();
		} else {
			return packetClient.getLocal();
		}
	}

	/**
	 * 重新连接
	 * @throws IOException
	 */
	public void reconnect() throws IOException {
		if (streamClient != null) {
			SocketHost remote = streamClient.getRemote().duplicate();
			// 先尝试销毁，关闭socket，如果有连接的话
			streamClient.destroy();
			// 再进行连接
			try {
				streamClient.connect(remote);
			} catch (IOException e) {
				String prefix = String.format("to %s | ", remote);
				throw new IOException(prefix + getThrowableText(e));
			}
		} else {
			SocketHost remote = packetClient.getRemote().duplicate();
			// 先尝试销毁，关闭socket，如果有连接的话
			packetClient.destroy();
			// 再进行连接
			try {
				packetClient.connect(remote);
			} catch (IOException e) {
				String prefix = String.format("to %s | ", remote);
				throw new IOException(prefix + getThrowableText(e));
			}
		}
	}

	/**
	 * 连接目标服务器
	 * @param endpoint 服务器地址
	 * @throws IOException
	 */
	public void connect(SocketHost endpoint) throws IOException {
		if (streamClient != null) {
			// 先尝试销毁，关闭socket，如果有连接的话
			streamClient.destroy();
			// 再进行连接
			streamClient.connect(endpoint);
		} else {
			// 先尝试销毁，关闭socket，如果有连接的话
			packetClient.destroy();
			// 连接服务器
			packetClient.connect(endpoint);
		}
	}

	/**
	 * 判断是否已经连接成功
	 * @return 返回真或者假
	 */
	public boolean isConnected() {
		if (streamClient != null) {
			return streamClient.isConnected();
		} else {
			return packetClient.isConnected();
		}
	}

	/**
	 * 判断是否已经关闭
	 * @return 返回真或者假
	 */
	public boolean isClosed() {
		if (streamClient != null) {
			return streamClient.isClosed();
		} else {
			return packetClient.isClosed();
		}
	}

	/**
	 * 柔性关闭连接，首先通知服务器结束对话，然后关闭SOCKET
	 */
	public void close() {
		if (streamClient != null) {
			streamClient.close();
		}
		if (packetClient != null) {
			packetClient.close();
		}
	}
	
	/**
	 * 增加接收的数据流量
	 * @param len
	 */
	public void addReceiveFlowSize(long len) {
		if (streamClient != null) {
			streamClient.addReceiveFlowSize(len);
		} else if (packetClient != null) {
			packetClient.addReceiveFlowSize(len);
		}
	}

	/**
	 * 增加已经发送的数据流量
	 * @param len
	 */
	public void addSendFlowSize(long len) {
		if (streamClient != null) {
			streamClient.addSendFlowSize(len);
		} else if (packetClient != null) {
			packetClient.addSendFlowSize(len);
		}
	}

	/**
	 * 返回接收的数据流量
	 * @return 字节长度（长整型）
	 */
	public long getReceiveFlowSize() {
		if (streamClient != null) {
			return streamClient.getReceiveFlowSize();
		} else if (packetClient != null) {
			return packetClient.getReceiveFlowSize();
		}
		return 0L;
	}

	/**
	 * 返回已经发送的数据流量
	 * @return 字节长度（长整型）
	 */
	public long getSendFlowSize() {
		if (streamClient != null) {
			return streamClient.getSendFlowSize();
		} else if (packetClient != null) {
			return packetClient.getSendFlowSize();
		}
		return 0L;
	}

	/**
	 * 强制关闭SOCKET
	 */
	public void destroy() {
		if (streamClient != null) {
			streamClient.destroy();
		}
		if (packetClient != null) {
			packetClient.destroy();
		}
	}

	/**
	 * 执行TCP模式的数据交换处理
	 * @param request 请求实体
	 * @param loadBody 是否加载数据域
	 * @return Stream实例
	 * @throws IOException
	 */
	public Stream swap(Stream request, boolean loadBody) throws IOException {
		if (streamClient == null) {
			throw new IOException("cannot be null!");
		}
		return streamClient.swap(request, loadBody);
	}

	/**
	 * 执行KEEP UDP模式的数据交换处理
	 * @param request 请求包实体
	 * @return Packet实例
	 * @throws IOException
	 */
	public Packet swap(Packet request) throws IOException {
		if (packetClient == null) {
			throw new IOException("cannot be null!");
		}
		return packetClient.batch(request);
	}

	/**
	 * 根据接口方法、数据进行远程调用（接口名称已经固定，不允许修改）。
	 * @param method 远程方法
	 * @param params 方法关联参数
	 * @return 返回对象
	 * @throws IOException
	 */
	private Object swap(Method method, Object[] params) throws VisitException, IOException {
		//1. 串行化请求数据
		String methodName = method.getName();
		Class<?>[] paramTypes = method.getParameterTypes();
		PatternConstructor creator = new PatternConstructor(visitName, methodName, paramTypes, params);
		byte[] data = creator.build();

		// 2. 生成FIXP请求数据，发送，然后接收反馈数据
		Mark mark = new Mark(Ask.RPC, Ask.EXECUTE);
		if (streamClient != null) {
			Stream request = new Stream(streamClient.getRemote(), mark);
			// 保存串行化数据(TCP模式)
			request.setData(data);
			// 3. 发送和接收数据
			Stream resp = streamClient.swap(request, true);
			data = resp.getData();
		} else {
			Packet request = new Packet(packetClient.getRemote(), mark);
			request.setData(data);
			// 3. 发送和接收数据(KEEP UDP模式)
			Packet resp = packetClient.batch(request);
			data = resp.getData();
		}

		//4. 解析应答数据和截获可能发生的错误
		PatternExtractor reply = null;
		try {
			reply = PatternExtractor.resolve(data);
		} catch (Throwable e) {
			throw new VisitException(e);
		}
		if (reply == null) {
			throw new VisitException("reply is null pointer!");
		}
		// 弹出错误
		if (reply.getThrowable() != null) {
			throw new VisitException(reply.getThrowText());
		}
		// 5. 返回处理结果
		return reply.getObject();
	}

	/**
	 * 远程方法调用
	 * @param method 被操作的远程方法
	 * @param params 参数
	 * @return 返回对象结果
	 * @throws VisitException - 如果出错错误，弹出异常
	 */
	public Object invoke(Method method, Object[] params) throws VisitException {
		try {
			return swap(method, params);
		} catch (IOException e) {
			String prefix = String.format("goto %s | ", getRemote());
			throw new VisitException(prefix + getThrowableText(e));
		} catch (Throwable e) {
			String prefix = String.format("goto %s | ", getRemote());
			throw new VisitException(prefix + getThrowableText(e));
		}
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
	 * 确保在不使用的时候释放资源
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		destroy();
		streamClient = null;
		packetClient = null;
		visitName = null;
	}
	

	//	/**
	//	 * 柔性关闭连接，首先通知服务器结束对话，然后关闭SOCKET
	//	 * @param exit 发出“exit”指令
	//	 */
	//	public void close(boolean exit) {
	//		if (streamClient != null) {
	//			streamClient.close(exit);
	//		}
	//		if (packetClient != null) {
	//			packetClient.close(exit);
	//		}
	//	}
	//
	//	/**
	//	 * 柔性关闭连接，首先通知服务器结束对话，然后关闭SOCKET
	//	 */
	//	public void close() {
	//		close(true);
	//	}
	
}