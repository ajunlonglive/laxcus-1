/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.echo;

import java.io.*;
import java.lang.reflect.*;

import com.laxcus.echo.*;
import com.laxcus.fixp.*;
import com.laxcus.fixp.monitor.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.*;
import com.laxcus.remote.client.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;

/**
 * RPC包异步通信客户端 <br>
 * 
 * 以客户机的身份，借用自己的FixpPacketMonitor.notice信道，向服务端的FixpPacketMonitor发起RPC操作，并且等待返回结果。
 * 
 * @author scott.liang
 * @version 1.0 3/1/2019
 * @since laxcus 1.0
 */
public class EchoCustomer implements EchoClient {

	/** 远程过程调用函数 **/
	private static Method methodStart;
	private static Method methodStop;
	private static Method methodPush;

	private static Method methodCast;
	private static Method methodExit;

	static {
		try {
			EchoCustomer.methodStart = (EchoVisit.class).getMethod("start",
					new Class<?>[] { EchoFlag.class, EchoHead.class });
			EchoCustomer.methodStop = (EchoVisit.class).getMethod("stop",
					new Class<?>[] { EchoFlag.class, EchoTail.class });
			EchoCustomer.methodPush = (EchoVisit.class).getMethod("push",
					new Class<?>[] { EchoFlag.class, EchoField.class });

			EchoCustomer.methodCast = (EchoVisit.class).getMethod("cast",
					new Class<?>[] { EchoFlag.class, EchoHead.class });
			EchoCustomer.methodExit = (EchoVisit.class).getMethod("exit",
					new Class<?>[] { EchoFlag.class, EchoTail.class });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/** 站点启动器 **/
	private static SiteLauncher launcher;
	

	/**
	 * 设置站点启动器。每个站点在启动时都要调用这个方法。
	 * @param e 站点启动器句柄
	 */
	public static void setLauncher(SiteLauncher e) {
		EchoCustomer.launcher = e;
	}

	
	/**
	 * 返回站点启动器句柄。
	 * @return SiteLauncher实例
	 */
	public static SiteLauncher getLauncher() {
		return EchoCustomer.launcher;
	}

	/** 异步通信信者 **/
	private static EchoMessenger messenger;

	/**
	 * 设置异步通信信者
	 * @param e 回显通信信者句柄
	 */
	public static void setEchoMessenger(EchoMessenger e) {
		EchoCustomer.messenger = e;
	}

	/**
	 * 返回异步通信信者句柄
	 * @return EchoMessenger实例
	 */
	public static EchoMessenger getEchoMessenger() {
		return EchoCustomer.messenger;
	}
	
	/** 远程FIXP UDP MONITOR节点地址 **/
	private SocketHost remote;
	
	/** Visit接口子类名 **/
	private String visitName;
	
	/** 目标地址的回显标识 **/
	private EchoFlag m_Flag;
	
	/** 默认单次传输的数据块尺寸，小于一个IP包长度。**/
	private int defaultSize; 
	
	/** 发送数据流量 **/
	private long sendSize = 0;
	
	/** 接收数据流量 **/
	private long receiveSize = 0;
	
	/** 异步通信接收超时，以毫秒为单位，默认60秒 **/
	private int receiveTimeout = 60000;
	
	/** 启动时间，一旦设置不会改变 **/
	private long launchTime = System.currentTimeMillis();
	
	/**
	 * 构造默认和私有的RPC包异步通信客户端
	 */
	private EchoCustomer() {
		super();
		setVisitName(EchoVisit.class.getName());
		setDefaultSize(256);
	}

	/**
	 * 构造RPC包异步通信客户端，指定服务端地址
	 * @param remote 目标站点
	 */
	public EchoCustomer(SocketHost remote) {
		this();
		setRemote(remote);
	}
	
	/**
	 * 构造RPC包异步通信客户端，指定服务端地址
	 * @param remote 目标站点
	 * @param receiveTimeout 超时时间
	 */
	public EchoCustomer(SocketHost remote, int receiveTimeout) {
		this(remote);
		setReceiveTimeout(receiveTimeout);
	}
	
	/**
	 * 构造RPC包异步通信客户端，指定目标站点回显地址
	 * @param hub 目标站点回显地址
	 */
	public EchoCustomer(Cabin hub) {
		this(hub.getNode().getPacketHost());
		setEchoFlag(hub.getFlag());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.remote.client.echo.EchoClient#getRunTime()
	 */
	@Override
	public long getRunTime() {
		return System.currentTimeMillis() - launchTime;
	}
	
	/**
	 * 设置以毫秒为单位的接收超时
	 * @param ms 毫秒
	 * @return 返回设置的接收超时
	 */
	public int setReceiveTimeout(int ms) {
		if (ms > 0) {
			receiveTimeout = ms;
		}
		return receiveTimeout;
	}

	/**
	 * 返回以毫秒为单位的接收超时
	 * @return 毫秒为单位的接收超时
	 */
	public int getReceiveTimeout() {
		return receiveTimeout;
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
	public void setRemote(SocketHost e) {
		Laxkit.nullabled(e);
		remote = e;
	}

	/**
	 * 返回连接的目标服务器地址
	 * @return SocketHost实例
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/**
	 * 设置回显标识。这个参数在构造类实例，或者执行“start”方法之前设置。
	 * 然后将和每次发送数据包中的回显标识做比较，防止出错。
	 * 
	 * @param e 回显标识
	 */
	@Override
	public void setEchoFlag(EchoFlag e) {
		Laxkit.nullabled(e);

		m_Flag = e;
	}

	/**
	 * 返回回显标识
	 * @return EchoFlag实例
	 */
	public EchoFlag getEchoFlag() {
		return m_Flag;
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
		
		Logger.debug(this, "swap", "packet bytes size %d, send to %s", data.length, remote);
		
		// 2. 生成FIXP请求数据，发送，然后接收反馈数据
		Mark mark = new Mark(Ask.NOTIFY, Ask.VISIT);
		// 借用FixpPacketMonitor的信道，以客户机身份，调用“mailing”函数，向服务器的FixpPacketMonitor发包
		Packet request = new Packet(remote, mark);
		// 保存数据
		request.setData(data);
		
		// 调用接口，返回数据
		Packet resp = EchoCustomer.messenger.mailing(request, receiveTimeout);
		
		// 当空指针时...
		if (resp == null) {
			throw new VisitException("receive packet timeout! by %s # %d ms", remote, receiveTimeout);
		}
		
//		Logger.note(this, "swap", resp != null, "response from %s",
//				(resp != null ? resp.getRemote() : "null pointer!"));
//		// 判断有效
//		data = (resp != null ? resp.getData() : null);
		
		data = resp.getData();

		// 4. 解析应答数据和截获可能发生的错误
		PatternExtractor reply = null;
		try {
			if (data != null) {
				reply = PatternExtractor.resolve(data);
			}
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

	/* (non-Javadoc)
	 * @see com.laxcus.visit.EchoVisit#start(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoHead)
	 */
	@Override
	public boolean start(EchoFlag flag, EchoHead head) throws VisitException {
		if (m_Flag != null && !m_Flag.equals(flag)) {
			throw new VisitException("not match! %s,%s", m_Flag, flag);
		} else if (flag == null) {
			throw new VisitException("echo flag is null pointer");
		}

		Object[] params = new Object[] { flag, head };
		Object param = invoke(EchoCustomer.methodStart, params);
		return ((Boolean) param).booleanValue();
	}
	
	/**
	 * 启动异步通信
	 * @param head 回显报头
	 * @return 启动成功返回真，否则假
	 * @throws VisitException
	 */
	public boolean start(EchoHead head) throws VisitException {
		if (m_Flag == null) {
			throw new VisitException("cannot be null");
		}
		return start(m_Flag, head);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.EchoVisit#push(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoField)
	 */
	@Override
	public long push(EchoFlag flag, EchoField field) throws VisitException {
		if (m_Flag != null && !m_Flag.equals(flag)) {
			throw new VisitException("cannot match %s,%s", m_Flag, flag);
		}

		Object[] params = new Object[] { flag, field };
		Object param = invoke(EchoCustomer.methodPush, params);
		return ((Long) param).longValue();
	}

	/**
	 * 发送异步数据包
	 * @param field EchoField实例
	 * @return 返回下次数据发送的下标位置
	 * @throws VisitException
	 */
	public long push(EchoField field) throws VisitException {
		if (m_Flag == null) {
			throw new VisitException("cannot be null");
		}
		return push(m_Flag, field);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.EchoVisit#stop(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoTail)
	 */
	@Override
	public boolean stop(EchoFlag flag, EchoTail tail) throws VisitException {
		if (m_Flag != null && !m_Flag.equals(flag)) {
			throw new VisitException("cannot match %s,%s", m_Flag, flag);
		} else if (flag == null) {
			throw new VisitException("echo flag is null pointer");
		}

		Object[] params = new Object[] { flag, tail };
		Object param = invoke(EchoCustomer.methodStop, params);
		return ((Boolean) param).booleanValue();
	}

	/**
	 * 结束异步通信
	 * @param tail EchoTail实例
	 * @return 成功返回真，否则假
	 * @throws VisitException
	 */
	public boolean stop(EchoTail tail) throws VisitException {
		if (m_Flag == null) {
			throw new VisitException("cannot be null");
		}
		return stop(m_Flag, tail);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.EchoVisit#cast(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoHead)
	 */
	@Override
	public CastToken cast(EchoFlag flag, EchoHead head) throws VisitException {
		if (m_Flag != null && !m_Flag.equals(flag)) {
			throw new VisitException("cannot match %s,%s", m_Flag, flag);
		} else if(flag ==null){
			throw new VisitException("echo flag is null pointer");
		}
		
		Object[] params = new Object[] { flag, head };
		Object param = invoke(EchoCustomer.methodCast, params);
		return (CastToken) param;
	}

	/**
	 * 启动快速RPC异步通信
	 * @param head 回显报头
	 * @return 成功返回异步通信令牌，否则是空指针
	 * @throws VisitException
	 */
	public CastToken cast(EchoHead head) throws VisitException {
		if (m_Flag == null) {
			throw new VisitException("cannot be null");
		}
		return cast(m_Flag, head);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.EchoVisit#exit(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoTail)
	 */
	@Override
	public boolean exit(EchoFlag flag, EchoTail tail) throws VisitException {
		if (m_Flag != null && !m_Flag.equals(flag)) {
			throw new VisitException("cannot match %s,%s", m_Flag, flag);
		} else if (flag == null) {
			throw new VisitException("echo flag is null pointer");
		}

		Object[] params = new Object[] { flag, tail };
		Object param = invoke(EchoCustomer.methodExit, params);
		return ((Boolean) param).booleanValue();
	}

	/**
	 * 结束快速RPC异步通信
	 * @param tail EchoTail实例
	 * @return 成功返回真，否则假
	 * @throws VisitException
	 */
	public boolean exit(EchoTail tail) throws VisitException {
		if (m_Flag == null) {
			throw new VisitException("cannot be null");
		}
		return exit(m_Flag, tail);
	}
	
	private void reconnect() throws IOException {
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.remote.client.echo.EchoClient#close()
	 */
	@Override
	public void close() {
		
	}

	/**
	 * 重新连接
	 * @return 重连成功返回真，否则假
	 */
	private boolean redo() {
		try {
			reconnect();
			return true;
		} catch (IOException e) {
			Logger.error(e, "redo");
		}
		// 关闭
		close();
		return false;
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
	 * 默认延时
	 */
	private void defaultDelay() {
		delay(EchoTransfer.getRetryInterval());
	}
	
	/**
	 * 判断达到最大重试次数
	 * @param index 当前索引数
	 * @return 返回真或者假
	 */
	private boolean isMaxRetry(int index) {
		return index >= EchoTransfer.getMaxRetry();
	}

	/**
	 * 设置本次数据块传输尺寸
	 * @param size 数据块尺寸，必须大于0。
	 */
	public void setDefaultSize(int size) {
		if (size < 1) {
			throw new IllegalValueException("%d < 1", size);
		}
		defaultSize = size;
	}

	/**
	 * 返回本次数据块传输尺寸
	 * @return 数据块传输尺寸
	 */
	public int getDefaultSize() {
		return defaultSize;
	}

	/**
	 * 启动RPC异步通信
	 * @param flag 回显标识
	 * @param head 回显报头
	 * @return 成功返回真，否则假
	 */
	public boolean doStart(EchoFlag flag, EchoHead head) {
		for (int index = 0; !isMaxRetry(index); index++) {
			if (index > 0) {
				// 关闭连接
				close();
				// 延时
				defaultDelay();
				// 再次尝试连接
				boolean success = redo();
				// 不成功就继续下一次
				if (!success) continue;
			}
			try {
				return start(flag, head);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 关闭连接
		close();
		return false;
	}

	/**
	 * 启动RPC异步通信
	 * @param head 回显报头
	 * @return 成功返回真，否则假
	 */
	public boolean doStart(EchoHead head) {
		for (int index = 0; !isMaxRetry(index); index++) {
			if (index > 0) {
				// 关闭套接字
				close();
				// 延时
				defaultDelay();
				// 再次尝试连接
				boolean success = redo();
				// 不成功就继续下一次
				if (!success) continue;
			}
			try {
				return start(head);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		close();
		return false;
	}
	
	/**
	 * 结束异步通信
	 * @param flag 回显标识
	 * @param tail 回显报尾
	 * @return 成功返回真，否则假
	 */
	public boolean doStop(EchoFlag flag, EchoTail tail) {
		for (int index = 0; !isMaxRetry(index); index++) {
			if (index > 0) {
				// 关闭套接字
				close();
				// 延时
				defaultDelay();
				// 再次尝试连接
				boolean success = redo();
				// 不成功就继续下一次
				if (!success) continue;
			}
			try {
				return stop(flag, tail);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 关闭
		close();
		return false;
	}

	/**
	 * 结束异步通信
	 * @param tail 回显报尾
	 * @return 成功返回真，否则假
	 */
	public boolean doStop(EchoTail tail) {
		for (int index = 0; !isMaxRetry(index); index++) {
			if (index > 0) {
				// 关闭套接字
				close();
				// 延时
				defaultDelay();
				// 再次尝试连接
				boolean success = redo();
				// 不成功就继续下一次
				if (!success) continue;
			}
			try {
				return stop(tail);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 关闭
		close();
		return false;
	}

	/**
	 * 启动RPC快速异步通信
	 * @param flag 回显标识
	 * @param head 回显报头
	 * @return 成功返回真，否则假
	 */
	public CastToken doCast(EchoFlag flag, EchoHead head) {
		for (int index = 0; !isMaxRetry(index); index++) {
			if (index > 0) {
				// 关闭套接字
				close();
				// 延时
				defaultDelay();
				// 再次尝试连接
				boolean success = redo();
				// 不成功就继续下一次
				if (!success) continue;
			}
			// 发送命令
			try {
				return cast(flag, head);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 关闭SOCKET
		close();
		return null;
	}

	/**
	 * 启动RPC快速异步通信
	 * @param head 回显报头
	 * @return 返回标记头，失败返回空指针。
	 */
	public CastToken doCast(EchoHead head) {
		for (int index = 0; !isMaxRetry(index); index++) {
			if (index > 0) {
				// 关闭套接字
				close();
				// 延时
				defaultDelay();
				// 再次尝试连接
				boolean success = redo();
				// 不成功就继续下一次
				if (!success) continue;
			}
			try {
				return cast(head);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		
		// 关闭SOCKET
		close();

		return null;
	}

	/**
	 * 结束快速RPC异步通信
	 * @param flag 回显标识
	 * @param tail 回显报尾
	 * @return 成功返回真，否则假
	 */
	public boolean doExit(EchoFlag flag, EchoTail tail) {
		for (int index = 0; !isMaxRetry(index); index++) {
			if (index > 0) {
				// 关闭套接字
				close();
				// 延时
				defaultDelay();
				// 再次尝试连接
				boolean success = redo();
				// 不成功就继续下一次
				if (!success) continue;
			}
			try {
				return exit(flag, tail);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		close();
		return false;
	}

	/**
	 * 结束快速RPC异步通信
	 * @param tail 回显报尾
	 * @return 成功返回真，否则假
	 */
	public boolean doExit(EchoTail tail) {
		for (int index = 0; !isMaxRetry(index); index++) {
			if (index > 0) {
				// 关闭套接字
				close();
				// 延时
				defaultDelay();
				// 再次尝试连接
				boolean success = redo();
				// 不成功就继续下一次
				if (!success) continue;
			}
			try {
				return exit(tail);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 关闭SOCKET
		close();
		return false;
	}

	/**
	 * 简化投递。<br>
	 * 只调用EchoVisit.start, EchoVisit.stop方法，忽略EchoVisit.push方法，发送处理结果。
	 * 
	 * @param head 异步应答报头
	 * @param b 应答数据
	 * @return 发送成功返回真，否则假
	 */
	public boolean shoot(EchoHead head) {
		// 发送报头
		boolean success = doStart(head);
		Logger.debug(this, "shoot", success, "start [%s] to %s", head, getRemote());
		
		// 在报头发送成功后，发送报尾
		if (success) {
			EchoTail tail = new EchoTail(success, 0, 0);
			success = doStop(tail);
			Logger.debug(this, "shoot", success, "stop [%s] to %s", tail, getRemote());
		}

		// 关闭连接
		close();

		return success;
	}

	/**
	 * 统计文件长度
	 * @param files 文件数组
	 * @return 文件长度
	 */
	private long countFileLength(File[] files) {
		// 统计长度
		long length = 0L;
		for (File file : files) {
			if (!(file.exists() && file.isFile())) {
				continue;
			}
			length += file.length();
		}
		return length;
	}
	
	/**
	 * 统计发送数据流量
	 * @param len
	 */
	public void addSendFlowSize(long len) {
		if(len >0)
		sendSize += len;
	}
	
	/**
	 * 统计接收流量
	 * @param len
	 */
	public void addReceiveFlowSize(long len) {
		if(len>0)
		receiveSize += len;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.remote.client.echo.EchoTracker#getReceiveFlowSize()
	 */
	@Override
	public long getReceiveFlowSize() {
		return receiveSize;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.remote.client.echo.EchoTracker#getSendFlowSize()
	 */
	@Override
	public long getSendFlowSize() {
		return sendSize;
	}
	
	/**
	 * 快速投递文件到目标地址
	 * @param head 回显报头
	 * @param tailHelp 报尾辅助信息
	 * @param sender 投递代理
	 * @return 成功返回真，否则假
	 */
	public boolean post(EchoHead head, EchoHelp tailHelp, ReplySender sender) {
		// 统计文件长度
		long length = 0;
		if (sender.hasFile()) {
			length = countFileLength(sender.getFiles());
		} else if (sender.hasData()) {
			length = sender.getDataLength();
		}
		// 定义数据长度
		head.setLength(length);

		// 投递到目标站点，判断对方接受
		CastToken token = doCast(head);
		boolean accepted = (token != null);
		// 关闭套接字
		close();
		
		Logger.note(this, "post", accepted, "connect %s, head %s, token %s",
				getRemote(), head, token);

		if (!accepted) {
			Logger.error(this, "post", "not accepted! from %s#%s", getRemote(), m_Flag);
			return false;
		}
		
		// 设置异步通信令牌，包括：接收端地址、异步通信标识、对称密钥
		sender.setToken(token);

		// 注册发送器到异步工作代理中，由ReplyWorker代理上传数据的工作
		boolean success = launcher.getReplyWorker().push(sender);
		// 等待发送器，直到它发送完数据！
		if (success) {
			sender.await();
		}
		// 结果是成功，或者否
		success = sender.isSuccessful();

		// 提示
		if (success) {
			Logger.debug(this, "post", "send data successful! to: %s", getRemote());
		} else {
			Logger.error(this, "post", "send data failed! to: %s", getRemote());
		}

		// 统计发送/接收的数据流量
		addSendFlowSize(sender.getSendFlowSize());
		addReceiveFlowSize(sender.getReceiveFlowSize());

		// 投递报尾。包括发送的数据长度，子包数目
		EchoTail tail = new EchoTail(success, sender.getSendFlowSize(), sender.getSubPackets());
		if (tailHelp != null) {
			tail.setHelp(tailHelp);
		}
		
		accepted = redo();
		if (accepted) {
			accepted = doExit(tail);
		}
		// 关闭套接字
		close();

		// 完成成功
		return accepted && success;
	}

	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速通信标识
	 * @param sender 快速投递代理
	 * @return 成功返回真，否则假
	 */
	public boolean post(EchoCode code, CastFlag flag, ReplySender sender) {
		Laxkit.nullabled(code);
		Laxkit.nullabled(flag);

		// 生成报头
		EchoHead head = new EchoHead(code, 0, flag);
		return post(head, null, sender);
	}

	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速异步通信标识，这个参数必须指定
	 * @param files 文件
	 * @return 投递成功返回真，否则假。
	 */
	public boolean post(EchoCode code, CastFlag flag, File[] files) {
		ReplySender sender = new ReplySender();
		sender.setFiles(files);
		return post(code, flag, sender);
	}

	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速异步通信标识
	 * @param files 文件数组
	 * @return 成功返回真，否则假
	 */
	public boolean post(boolean successful, CastFlag flag, File[] files) {
		short resp = (successful ? Major.SUCCESSFUL_FILE : Major.FAULTED_FILE);
		EchoCode code = new EchoCode(resp);
		return post(code, flag, files);
	}

	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速异步通信标识
	 * @param data 数据内容
	 * @return 投递成功返回真，否则假。
	 */
	public boolean post(EchoCode code, CastFlag flag, byte[] data) {
		ReplySender sender = new ReplySender();
		sender.setData(data);
		return post(code, flag, sender);
	}

	/**
	 * 快速投递数据内容到目标地址
	 * @param successful 成功或者失败
	 * @param flag 快速异步通信标识
	 * @param data 数据内容
	 * @return 成功返回真，否则假
	 */
	public boolean post(boolean successful, CastFlag flag, byte[] data) {
		short resp = (successful ? Major.SUCCESSFUL_DATA : Major.FAULTED_DATA);
		EchoCode code = new EchoCode(resp);
		return post(code, flag, data);
	}

	/**
	 * 指定成功或者失败，发送一个对象
	 * @param successful 成功
	 * @param flag 辅助信息
	 * @param param 对象
	 * @return 发送成功返回真，否则假
	 */
	public boolean post(boolean successful, CastFlag flag, Object param) {
		// 生成应答
		PatternExtractor reply = new PatternExtractor(param);
		byte[] b = null;
		try {
			b = reply.build();
		} catch (Throwable e) {
			Logger.fatal(e);
			return false;
		}

		short resp = (successful ? Major.SUCCESSFUL_OBJECT : Major.FAULTED_OBJECT);
		EchoCode code = new EchoCode(resp);
		// 发送对象
		return post(code, flag, b);
	}

	/**
	 * 默认是成功，向目标地址发送对象
	 * @param flag 辅助信息
	 * @param param 对象
	 * @return 发送成功返回真，否则假
	 */
	public boolean post(CastFlag flag, Object param) {
		return post(true, flag, param);
	}


	/* (non-Javadoc)
	 * @see com.laxcus.remote.client.echo.EchoTracker#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}



}