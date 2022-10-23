/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.echo;

import java.io.*;

import com.laxcus.echo.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.visit.*;

/**
 * EchoCustomer/EchoTraveller类实例
 * 
 * @author scott.liang
 * @version 1.0 3/2/2019
 * @since laxcus 1.0
 */
public interface EchoClient extends EchoVisit {
	
	/**
	 * 销毁！
	 */
	void destroy();
	
	/**
	 * 关闭连接
	 */
	void close();
	
	/**
	 * 返回运行时间
	 * @return 长整型值
	 */
	long getRunTime();
	
	/**
	 * 设置回显标识
	 * @param e
	 */
	void setEchoFlag(EchoFlag e);

	/**
	 * 返回接收的数据流量
	 * @return 字节长度（长整型）
	 */
	long getReceiveFlowSize();

	/**
	 * 返回已经发送的数据流量
	 * @return 字节长度（长整型）
	 */
	long getSendFlowSize();
	
	/**
	 * 启动RPC异步通信
	 * @param flag 回显标识
	 * @param head 回显报头
	 * @return 成功返回真，否则假
	 */
	boolean doStart(EchoFlag flag, EchoHead head);
	
	/**
	 * 启动RPC异步通信
	 * @param head 回显报头
	 * @return 成功返回真，否则假
	 */
	boolean doStart(EchoHead head);
	
	/**
	 * 结束异步通信
	 * @param tail 回显报尾
	 * @return 成功返回真，否则假
	 */
	boolean doStop(EchoTail tail);
	
	/**
	 * 启动RPC快速异步通信
	 * @param head 回显报头
	 * @return 返回标记头，失败返回空指针。
	 */
	CastToken doCast(EchoHead head);
	
	/**
	 * 结束快速RPC异步通信
	 * @param tail 回显报尾
	 * @return 成功返回真，否则假
	 */
	boolean doExit(EchoTail tail);
	
	/**
	 * 简化投递。<br>
	 * 只调用EchoVisit.start, EchoVisit.stop方法，忽略EchoVisit.push方法，发送处理结果。
	 * 
	 * @param head 异步应答报头
	 * @param b 应答数据
	 * @return 发送成功返回真，否则假
	 */
	boolean shoot(EchoHead head);
	
	/**
	 * 快速投递文件到目标地址
	 * @param head 回显报头
	 * @param tailHelp 报尾辅助信息
	 * @param sender 投递代理
	 * @return 成功返回真，否则假
	 */
	boolean post(EchoHead head, EchoHelp tailHelp, ReplySender sender);
	
	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速通信标识
	 * @param sender 快速投递代理
	 * @return 成功返回真，否则假
	 */
	boolean post(EchoCode code, CastFlag flag, ReplySender sender);
	
	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速异步通信标识，这个参数必须指定
	 * @param files 文件
	 * @return 投递成功返回真，否则假。
	 */
	boolean post(EchoCode code, CastFlag flag, File[] files);
	
	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速异步通信标识
	 * @param files 文件数组
	 * @return 成功返回真，否则假
	 */
	boolean post(boolean successful, CastFlag flag, File[] files);
	
	/**
	 * 快速投递文件到目标地址
	 * @param code 回显码
	 * @param flag 快速异步通信标识
	 * @param data 数据内容
	 * @return 投递成功返回真，否则假。
	 */
	boolean post(EchoCode code, CastFlag flag, byte[] data);
	
	/**
	 * 快速投递数据内容到目标地址
	 * @param successful 成功或者失败
	 * @param flag 快速异步通信标识
	 * @param data 数据内容
	 * @return 成功返回真，否则假
	 */
	boolean post(boolean successful, CastFlag flag, byte[] data);
	
	/**
	 * 指定成功或者失败，发送一个对象
	 * @param successful 成功
	 * @param flag 辅助信息
	 * @param param 对象
	 * @return 发送成功返回真，否则假
	 */
	boolean post(boolean successful, CastFlag flag, Object param);
	
	/**
	 * 默认是成功，向目标地址发送对象
	 * @param flag 辅助信息
	 * @param param 对象
	 * @return 发送成功返回真，否则假
	 */
	boolean post(CastFlag flag, Object param) ;
	
}

///**
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// * 
// * Copyright 2009 laxcus.com. All rights reserved
// * 
// * @license GNU Lesser General Public License (LGPL)
// */
//package com.laxcus.remote.client.echo;
//
//import java.io.*;
//import java.lang.reflect.*;
//
//import com.laxcus.echo.*;
//import com.laxcus.fixp.reply.*;
//import com.laxcus.launch.*;
//import com.laxcus.log.client.*;
//import com.laxcus.remote.*;
//import com.laxcus.remote.client.*;
//import com.laxcus.util.*;
//import com.laxcus.util.net.*;
//import com.laxcus.visit.*;
//
///**
// * RPC异步通信客户端 <br><br>
// * 
// * <pre>
// * 这个类是“EchoVisit”接口的客户端实现 
// * 
// * 当EchoInvoker需要与其它站点进行通信时，将调用这个类，向目标站点发送RPC异步数据。位于接收站点的异步代理（EchoAgent，是EchoVisit的服务端实现）将接收发来的数据包，然后根据回显标识，将数据转发给异步数据接收器（EchoReceiver）处理。
// * 
// * 大规模数据传输说明：
// * 本系统网络通信采用数据分组交换和交互方式，客户端发送一段数据，服务端接收后向客户端做出一个反馈，这样可以避免网络通信过程中的丢包现象。一般情况下，再结合UDP和小块数据（要求尺寸小于一个IP包）的方案，还有利于减少网络和计算机的负载。
// * 但是对于数据块动辄数百GB的大规模数据传输，因为频繁的通信交互和响应，这种传输效率极低，再采用小块数据传输就很不合适。这时就要避免通信交互次数，和采用可持续的数据传输。
// * 大规模数据传输有两个参数：传输模式和数据传输尺寸。这两个参数在“local.xml”文件中定义。
// * 
// * RPC异步通信分为三部分：
// * 1. 启动异步通信，对应方法是"start"。
// * 2. 异步应答数据传输，对应方法是"push"。
// * 3. 结束异步通信，对应方法是"stop"。 
// * "push"在通信过程中是可选操作，可以多次使用。"start"、"stop"是必选操作，只能使用一次。
// * </pre>
// * 
// * @author scott.liang
// * @version 1.25 10/26/2016
// * @since laxcus 1.0
// */
//public final class EchoClient extends RemoteClient implements EchoVisit {
//
//	/** 远程过程调用函数 **/
//	private static Method methodStart;
//	private static Method methodStop;
//	private static Method methodPush;
//
//	private static Method methodCast;
//	private static Method methodExit;
//
//	static {
//		try {
//			EchoClient.methodStart = (EchoVisit.class).getMethod("start",
//					new Class<?>[] { EchoFlag.class, EchoHead.class });
//			EchoClient.methodStop = (EchoVisit.class).getMethod("stop",
//					new Class<?>[] { EchoFlag.class, EchoTail.class });
//			EchoClient.methodPush = (EchoVisit.class).getMethod("push",
//					new Class<?>[] { EchoFlag.class, EchoField.class });
//
//			EchoClient.methodCast = (EchoVisit.class).getMethod("cast",
//					new Class<?>[] { EchoFlag.class, EchoHead.class });
//			EchoClient.methodExit = (EchoVisit.class).getMethod("exit",
//					new Class<?>[] { EchoFlag.class, EchoTail.class });
//		} catch (NoSuchMethodException exp) {
//			throw new NoSuchMethodError("stub class initialization failed");
//		}
//	}
//	
//	/** 站点启动器 **/
//	private static SiteLauncher launcher;
//
//	/**
//	 * 设置站点启动器。每个站点在启动时都要调用这个方法。
//	 * @param e 站点启动器句柄
//	 */
//	public static void setLauncher(SiteLauncher e) {
//		EchoClient.launcher = e;
//	}
//
//	/**
//	 * 返回站点启动器句柄。
//	 * @return SiteLauncher实例
//	 */
//	public static SiteLauncher getLauncher() {
//		return EchoClient.launcher;
//	}
//
////	/** 最大错误重新传输次数，默认3次。 **/
////	private static int maxRetry = 3;
////
////	/** 故障重试延时时间，默认2秒。 **/
////	private static int retryInterval = 2000;
////
////	/**
////	 * 设置最大错误重传次数
////	 * @param how 重试次数
////	 * @return 返回实际的错误重传次数
////	 */
////	public static int setMaxRetry(int how) {
////		if (how >= 1) {
////			EchoClient.maxRetry = how;
////		}
////		return EchoClient.maxRetry;
////	}
////
////	/**
////	 * 返回最大重传次数
////	 * @return 重传次数
////	 */
////	public static int getMaxRetry() {
////		return EchoClient.maxRetry;
////	}
////	
////	/**
////	 * 设置重试间隔时间
////	 * @param timeout 间隔时间
////	 * @return 返回重试间隔时间
////	 */
////	public static int setRetryInterval(int timeout) {
////		if (timeout >= 1000) {
////			EchoClient.retryInterval = timeout;
////		}
////		return EchoClient.retryInterval;
////	}
////
////	/**
////	 * 输出重试间隔时间
////	 * @return 重试间隔时间
////	 */
////	public static int getRetryInterval() {
////		return EchoClient.retryInterval;
////	}
//
//	/** 默认单次传输的数据块尺寸，小于一个IP包长度。**/
//	private int defaultSize; 
//
//	/** 目标地址的回显标识 **/
//	private EchoFlag m_Flag;
//
//	/**
//	 * 构造私有的RPC异步通信客户端，指定传输模式
//	 * @param stream 流模式（TCP）
//	 */
//	private EchoClient(boolean stream) {
//		super(stream, EchoVisit.class.getName());
//		setDefaultSize(256);
//	}
//
//	/**
//	 * 构造RPC异步通信客户端，指定目标地址。
//	 * @param endpoint 目标站点地址
//	 */
//	public EchoClient(SocketHost endpoint) {
//		this(endpoint.isStream());
//		setRemote(endpoint);
//	}
//
//	/**
//	 * 构造RPC异步通信客户端，指定目标站点回显地址和数据传输模式
//	 * @param hub 目标站点回显地址
//	 * @param stream 数据流模式 
//	 */
//	public EchoClient(Cabin hub, boolean stream) {
//		this(hub.getNode().choice(stream));
//		setEchoFlag(hub.getFlag());
//	}
//
//	/**
//	 * 构造RPC异步通信客户端，指定目标站点回显地址，传输模式根据配置要求而定
//	 * @param hub 目标站点回显地址
//	 */
//	public EchoClient(Cabin hub) {
//		this(hub, EchoTransfer.isStreamTransfer());
//	}
//	
//	/**
//	 * 默认延时
//	 */
//	private void defaultDelay() {
//		delay(EchoTransfer.getRetryInterval());
//	}
//	
//	/**
//	 * 判断达到最大重试次数
//	 * @param index 当前索引数
//	 * @return 返回真或者假
//	 */
//	private boolean isMaxRetry(int index) {
//		return index >= EchoTransfer.getMaxRetry();
//	}
//
//	/**
//	 * 设置本次数据块传输尺寸
//	 * @param size 数据块尺寸，必须大于0。
//	 */
//	public void setDefaultSize(int size) {
//		if (size < 1) {
//			throw new IllegalValueException("%d < 1", size);
//		}
//		defaultSize = size;
//	}
//
//	/**
//	 * 返回本次数据块传输尺寸
//	 * @return 数据块传输尺寸
//	 */
//	public int getDefaultSize() {
//		return defaultSize;
//	}
//
//	/**
//	 * 设置回显标识。这个参数在构造类实例，或者执行“start”方法之前设置。
//	 * 然后将和每次发送数据包中的回显标识做比较，防止出错。
//	 * 
//	 * @param e 回显标识
//	 */
//	public void setEchoFlag(EchoFlag e) {
//		Laxkit.nullabled(e);
//
//		m_Flag = e;
//	}
//
//	/**
//	 * 返回回显标识
//	 * @return EchoFlag实例
//	 */
//	public EchoFlag getEchoFlag() {
//		return m_Flag;
//	}
//
//	/**
//	 * 服务器以应答方的身份，通知客户机身份的请求方，启动本次PRC异步通信。
//	 * @see com.laxcus.visit.EchoVisit#start(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoHead)
//	 */
//	@Override
//	public boolean start(EchoFlag flag, EchoHead head) throws VisitException {
//		if (m_Flag != null && !m_Flag.equals(flag)) {
//			throw new VisitException("not match! %s,%s", m_Flag, flag);
//		} else if (flag == null) {
//			throw new VisitException("echo flag is null pointer");
//		}
//
//		Object[] params = new Object[] { flag, head };
//		Object param = super.invoke(EchoClient.methodStart, params);
//		return ((Boolean) param).booleanValue();
//	}
//
//	/**
//	 * 启动异步通信
//	 * @param head 回显报头
//	 * @return 启动成功返回真，否则假
//	 * @throws VisitException
//	 */
//	public boolean start(EchoHead head) throws VisitException {
//		if (m_Flag == null) {
//			throw new VisitException("cannot be null");
//		}
//		return start(m_Flag, head);
//	}
//
//	/**
//	 * 服务器以应答方的身份，向客户机身份的请求方，发送RPC异步应答数据包。
//	 * @see com.laxcus.visit.EchoVisit#push(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoField)
//	 */
//	@Override
//	public long push(EchoFlag flag, EchoField field) throws VisitException {
//		if (m_Flag != null && !m_Flag.equals(flag)) {
//			throw new VisitException("cannot match %s,%s", m_Flag, flag);
//		}
//
//		Object[] params = new Object[] { flag, field };
//		Object param = super.invoke(EchoClient.methodPush, params);
//		return ((Long) param).longValue();
//	}
//
//	/**
//	 * 发送异步数据包
//	 * @param field EchoField实例
//	 * @return 返回下次数据发送的下标位置
//	 * @throws VisitException
//	 */
//	public long push(EchoField field) throws VisitException {
//		if (m_Flag == null) {
//			throw new VisitException("cannot be null");
//		}
//		return push(m_Flag, field);
//	}
//
//	/**
//	 * 服务器以应答方的身份，通知客户机身份的请求方，结束本次PRC异步通信。
//	 * @see com.laxcus.visit.EchoVisit#stop(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoTail)
//	 */
//	@Override
//	public boolean stop(EchoFlag flag, EchoTail tail) throws VisitException {
//		if (m_Flag != null && !m_Flag.equals(flag)) {
//			throw new VisitException("cannot match %s,%s", m_Flag, flag);
//		} else if (flag == null) {
//			throw new VisitException("echo flag is null pointer");
//		}
//
//		Object[] params = new Object[] { flag, tail };
//		Object param = super.invoke(EchoClient.methodStop, params);
//		return ((Boolean) param).booleanValue();
//	}
//
//	/**
//	 * 结束异步通信
//	 * @param tail EchoTail实例
//	 * @return 成功返回真，否则假
//	 * @throws VisitException
//	 */
//	public boolean stop(EchoTail tail) throws VisitException {
//		if (m_Flag == null) {
//			throw new VisitException("cannot be null");
//		}
//		return stop(m_Flag, tail);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.visit.EchoVisit#cast(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoHead)
//	 */
//	@Override
//	public CastToken cast(EchoFlag flag, EchoHead head) throws VisitException {
//		if (m_Flag != null && !m_Flag.equals(flag)) {
//			throw new VisitException("cannot match %s,%s", m_Flag, flag);
//		} else if(flag ==null){
//			throw new VisitException("echo flag is null pointer");
//		}
//		
//		Object[] params = new Object[] { flag, head };
//		Object param = super.invoke(EchoClient.methodCast, params);
//		return (CastToken) param;
//	}
//
//	/**
//	 * 启动快速RPC异步通信
//	 * @param head 回显报头
//	 * @return 成功返回异步通信令牌，否则是空指针
//	 * @throws VisitException
//	 */
//	public CastToken cast(EchoHead head) throws VisitException {
//		if (m_Flag == null) {
//			throw new VisitException("cannot be null");
//		}
//		return cast(m_Flag, head);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.visit.EchoVisit#exit(com.laxcus.echo.EchoFlag, com.laxcus.echo.EchoTail)
//	 */
//	@Override
//	public boolean exit(EchoFlag flag, EchoTail tail) throws VisitException {
//		if (m_Flag != null && !m_Flag.equals(flag)) {
//			throw new VisitException("cannot match %s,%s", m_Flag, flag);
//		} else if (flag == null) {
//			throw new VisitException("echo flag is null pointer");
//		}
//
//		Object[] params = new Object[] { flag, tail };
//		Object param = super.invoke(EchoClient.methodExit, params);
//		return ((Boolean) param).booleanValue();
//	}
//
//	/**
//	 * 结束快速RPC异步通信
//	 * @param tail EchoTail实例
//	 * @return 成功返回真，否则假
//	 * @throws VisitException
//	 */
//	public boolean exit(EchoTail tail) throws VisitException {
//		if (m_Flag == null) {
//			throw new VisitException("cannot be null");
//		}
//		return exit(m_Flag, tail);
//	}
//
//	/**
//	 * 重新连接
//	 * @return 重连成功返回真，否则假
//	 */
//	private boolean redo() {
//		try {
//			reconnect();
//			return true;
//		} catch (IOException e) {
//			Logger.error(e, "redo");
//		}
//		// 关闭
//		close();
//		return false;
//	}
//
//	/**
//	 * 启动RPC异步通信
//	 * @param flag 回显标识
//	 * @param head 回显报头
//	 * @return 成功返回真，否则假
//	 */
//	public boolean doStart(EchoFlag flag, EchoHead head) {
//		for (int index = 0; !isMaxRetry(index); index++) {
//			if (index > 0) {
//				// 关闭连接
//				close();
//				// 延时
//				defaultDelay();
//				// 再次尝试连接
//				boolean success = redo();
//				// 不成功就继续下一次
//				if (!success) continue;
//			}
//			try {
//				return start(flag, head);
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//		// 关闭连接
//		close();
//		return false;
//	}
//
//	/**
//	 * 启动RPC异步通信
//	 * @param head 回显报头
//	 * @return 成功返回真，否则假
//	 */
//	public boolean doStart(EchoHead head) {
//		for (int index = 0; !isMaxRetry(index); index++) {
//			if (index > 0) {
//				// 关闭套接字
//				close();
//				// 延时
//				defaultDelay();
//				// 再次尝试连接
//				boolean success = redo();
//				// 不成功就继续下一次
//				if (!success) continue;
//			}
//			try {
//				return start(head);
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//		close();
//		return false;
//	}
//	
//	/**
//	 * 结束异步通信
//	 * @param flag 回显标识
//	 * @param tail 回显报尾
//	 * @return 成功返回真，否则假
//	 */
//	public boolean doStop(EchoFlag flag, EchoTail tail) {
//		for (int index = 0; !isMaxRetry(index); index++) {
//			if (index > 0) {
//				// 关闭套接字
//				close();
//				// 延时
//				defaultDelay();
//				// 再次尝试连接
//				boolean success = redo();
//				// 不成功就继续下一次
//				if (!success) continue;
//			}
//			try {
//				return stop(flag, tail);
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//		// 关闭
//		close();
//		return false;
//	}
//
//	/**
//	 * 结束异步通信
//	 * @param tail 回显报尾
//	 * @return 成功返回真，否则假
//	 */
//	public boolean doStop(EchoTail tail) {
//		for (int index = 0; !isMaxRetry(index); index++) {
//			if (index > 0) {
//				// 关闭套接字
//				close();
//				// 延时
//				defaultDelay();
//				// 再次尝试连接
//				boolean success = redo();
//				// 不成功就继续下一次
//				if (!success) continue;
//			}
//			try {
//				return stop(tail);
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//		// 关闭
//		close();
//		return false;
//	}
//
//	/**
//	 * 启动RPC快速异步通信
//	 * @param flag 回显标识
//	 * @param head 回显报头
//	 * @return 成功返回真，否则假
//	 */
//	public CastToken doCast(EchoFlag flag, EchoHead head) {
//		for (int index = 0; !isMaxRetry(index); index++) {
//			if (index > 0) {
//				// 关闭套接字
//				close();
//				// 延时
//				defaultDelay();
//				// 再次尝试连接
//				boolean success = redo();
//				// 不成功就继续下一次
//				if (!success) continue;
//			}
//			// 发送命令
//			try {
//				return cast(flag, head);
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//		// 关闭SOCKET
//		close();
//		return null;
//	}
//
//	/**
//	 * 启动RPC快速异步通信
//	 * @param head 回显报头
//	 * @return 返回标记头，失败返回空指针。
//	 */
//	public CastToken doCast(EchoHead head) {
//		for (int index = 0; !isMaxRetry(index); index++) {
//			if (index > 0) {
//				// 关闭套接字
//				close();
//				// 延时
//				defaultDelay();
//				// 再次尝试连接
//				boolean success = redo();
//				// 不成功就继续下一次
//				if (!success) continue;
//			}
//			try {
//				return cast(head);
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//		
//		// 关闭SOCKET
//		close();
//
//		return null;
//	}
//
//	/**
//	 * 结束快速RPC异步通信
//	 * @param flag 回显标识
//	 * @param tail 回显报尾
//	 * @return 成功返回真，否则假
//	 */
//	public boolean doExit(EchoFlag flag, EchoTail tail) {
//		for (int index = 0; !isMaxRetry(index); index++) {
//			if (index > 0) {
//				// 关闭套接字
//				close();
//				// 延时
//				defaultDelay();
//				// 再次尝试连接
//				boolean success = redo();
//				// 不成功就继续下一次
//				if (!success) continue;
//			}
//			try {
//				return exit(flag, tail);
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//		close();
//		return false;
//	}
//
//	/**
//	 * 结束快速RPC异步通信
//	 * @param tail 回显报尾
//	 * @return 成功返回真，否则假
//	 */
//	public boolean doExit(EchoTail tail) {
//		for (int index = 0; !isMaxRetry(index); index++) {
//			if (index > 0) {
//				// 关闭套接字
//				close();
//				// 延时
//				defaultDelay();
//				// 再次尝试连接
//				boolean success = redo();
//				// 不成功就继续下一次
//				if (!success) continue;
//			}
//			try {
//				return exit(tail);
//			} catch (VisitException e) {
//				Logger.error(e);
//			}
//		}
//		// 关闭SOCKET
//		close();
//		return false;
//	}
//
//
//	/**
//	 * 简化投递。<br>
//	 * 只调用EchoVisit.start, EchoVisit.stop方法，忽略EchoVisit.push方法，发送处理结果。
//	 * 
//	 * @param head 异步应答报头
//	 * @param b 应答数据
//	 * @return 发送成功返回真，否则假
//	 */
//	public boolean shoot(EchoHead head) {
//		// 发送报头
//		boolean success = doStart(head);
//		// 在报头发送成功后，发送报尾
//		if (success) {
//			EchoTail tail = new EchoTail(success, 0, 0);
//			success = doStop(tail);
//		}
//
//		Logger.debug(this, "shoot", success, "TO %s", getRemote());
//
//		// 关闭连接
//		close();
//
//		return success;
//	}
//
//	/**
//	 * 统计文件长度
//	 * @param files 文件数组
//	 * @return 文件长度
//	 */
//	private long countFileLength(File[] files) {
//		// 统计长度
//		long length = 0L;
//		for (File file : files) {
//			if (!(file.exists() && file.isFile())) {
//				continue;
//			}
//			length += file.length();
//		}
//		return length;
//	}
//	
//	/**
//	 * 快速投递文件到目标地址
//	 * @param head 回显报头
//	 * @param tailHelp 报尾辅助信息
//	 * @param sender 投递代理
//	 * @return 成功返回真，否则假
//	 */
//	public boolean post(EchoHead head, EchoHelp tailHelp, ReplySender sender) {
//		// 统计文件长度
//		long length = 0;
//		if (sender.hasFile()) {
//			length = countFileLength(sender.getFiles());
//		} else if (sender.hasData()) {
//			length = sender.getDataLength();
//		}
//		// 定义数据长度
//		head.setLength(length);
//		
//		// 投递到目标站点，判断对方接受
//		CastToken token = doCast(head);
//		boolean accepted = (token != null);
//		// 关闭套接字
//		close();
//		
//		Logger.note(this, "post", accepted, "connect %s, token is %s", getRemote(), token);
//		
//		if (!accepted) {
//			Logger.error(this, "post", "not accepted! from %s#%s", getRemote(), m_Flag);
//			return false;
//		}
//		
//		// 设置异步通信令牌，包括：接收端地址、异步通信标识、对称密钥
//		sender.setToken(token);
//				
//		// 注册发送器到异步工作代理中，由ReplyWorker代理上传数据的工作
//		boolean success = launcher.getReplyWorker().push(sender);
//		// 等待发送器，直到它发送完数据！
//		if (success) {
//			sender.await();
//		}
//		// 结果是成功，或者否
//		success = sender.isSuccessful();
//		
////		Logger.note(this, "post", success, "send to: %s", getRemote());
//
//		// 统计发送/接收的数据流量
//		addSendFlowSize(sender.getSendFlowSize());
//		addReceiveFlowSize(sender.getReceiveFlowSize());
//
//		// 投递报尾。包括发送的数据长度，子包数目
//		EchoTail tail = new EchoTail(success, sender.getSendFlowSize(), sender.getSubPackets());
//		if (tailHelp != null) {
//			tail.setHelp(tailHelp);
//		}
//		
//		accepted = redo();
//		if (accepted) {
//			accepted = doExit(tail);
//		}
//		// 关闭套接字
//		close();
//
//		// 完成成功
//		return accepted && success;
//	}
//
////	/**
////	 * 快速投递文件到目标地址
////	 * @param head 回显报头
////	 * @param tailHelp 报尾辅助信息
////	 * @param machine 投递代理
////	 * @return 成功返回真，否则假
////	 */
////	public boolean post1(EchoHead head, EchoHelp tailHelp, CastMachine machine) {
////		// 统计文件长度
////		long length = 0;
////		if (machine.hasFile()) {
////			length = countFileLength(machine.getFiles());
////		} else if (machine.hasData()) {
////			length = machine.getDataLength();
////		}
////		// 定义数据长度
////		head.setLength(length);
////		
//////		Logger.debug(this, "post", "启动快速异步通信! %s 发送到 %s", m_Flag, getRemote());
////		
////		// 投递到目标站点，判断对方接受
////		CastToken token = doCast(head);
////		boolean accepted = (token != null);
////		// 关闭套接字
////		close();
////		
//////		Logger.debug(this, "post", accepted, "启动快速异步通信! %s 发送到 %s", m_Flag, getRemote());
////		
////		if (!accepted) {
////			Logger.error(this, "post", "not accepted! from %s#%s", getRemote(), m_Flag);
////			return false;
////		}
////		
////		// 设置FIXP UDP数据包尺寸
////		machine.setPacketSize(SocketTransfer.getTransferSize());
////
////		// 设置投递标识：目标站点地址和密文
////		machine.setToken(token);
////		// 定义一个空指针，由系统绑定
////		boolean success = machine.bind();
////		// 启动线程
////		if (success) {
////			success = machine.start();
////		}
////		// 等待线程发送完毕
////		if (success) {
////			machine.await();
////		}
////		success = machine.isSuccessful();
////		// 关闭代理的套接字
////		machine.close();
////
////		// 统计发送/接收的数据流量
////		addSendFlowSize(machine.getSendFlowSize());
////		addReceiveFlowSize(machine.getReceiveFlowSize());
////
////		// 投递报尾。包括发送的数据长度，子包数目
////		EchoTail tail = new EchoTail(success, machine.getLength(), machine.getSubPackets());
////		if (tailHelp != null) {
////			tail.setHelp(tailHelp);
////		}
////		
////		accepted = redo();
////		if (accepted) {
////			accepted = doExit(tail);
////		}
////		// 关闭套接字
////		close();
////
////		// 完成成功
////		return accepted && success;
////	}
//
//	/**
//	 * 快速投递文件到目标地址
//	 * @param code 回显码
//	 * @param flag 快速通信标识
//	 * @param sender 快速投递代理
//	 * @return 成功返回真，否则假
//	 */
//	public boolean post(EchoCode code, CastFlag flag, ReplySender sender) {
//		Laxkit.nullabled(code);
//		Laxkit.nullabled(flag);
//
//		// 生成报头
//		EchoHead head = new EchoHead(code, 0, flag);
//		return post(head, null, sender);
//	}
//
//	/**
//	 * 快速投递文件到目标地址
//	 * @param code 回显码
//	 * @param flag 快速异步通信标识，这个参数必须指定
//	 * @param files 文件
//	 * @return 投递成功返回真，否则假。
//	 */
//	public boolean post(EchoCode code, CastFlag flag, File[] files) {
//		ReplySender sender = new ReplySender();
//		sender.setFiles(files);
//		return post(code, flag, sender);
//	}
//
//	/**
//	 * 快速投递文件到目标地址
//	 * @param code 回显码
//	 * @param flag 快速异步通信标识
//	 * @param files 文件数组
//	 * @return 成功返回真，否则假
//	 */
//	public boolean post(boolean successful, CastFlag flag, File[] files) {
//		short resp = (successful ? Major.SUCCESSFUL_FILE : Major.FAULTED_FILE);
//		EchoCode code = new EchoCode(resp);
//		return post(code, flag, files);
//	}
//
//	/**
//	 * 快速投递文件到目标地址
//	 * @param code 回显码
//	 * @param flag 快速异步通信标识
//	 * @param data 数据内容
//	 * @return 投递成功返回真，否则假。
//	 */
//	public boolean post(EchoCode code, CastFlag flag, byte[] data) {
//		ReplySender sender = new ReplySender();
//		sender.setData(data);
//		return post(code, flag, sender);
//	}
//
//	/**
//	 * 快速投递数据内容到目标地址
//	 * @param successful 成功或者失败
//	 * @param flag 快速异步通信标识
//	 * @param data 数据内容
//	 * @return 成功返回真，否则假
//	 */
//	public boolean post(boolean successful, CastFlag flag, byte[] data) {
//		short resp = (successful ? Major.SUCCESSFUL_DATA : Major.FAULTED_DATA);
//		EchoCode code = new EchoCode(resp);
//		return post(code, flag, data);
//	}
//
//	/**
//	 * 指定成功或者失败，发送一个对象
//	 * @param successful 成功
//	 * @param flag 辅助信息
//	 * @param param 对象
//	 * @return 发送成功返回真，否则假
//	 */
//	public boolean post(boolean successful, CastFlag flag, Object param) {
//		// 生成应答
//		PatternExtractor reply = new PatternExtractor(param);
//		byte[] b = null;
//		try {
//			b = reply.build();
//		} catch (Throwable e) {
//			Logger.fatal(e);
//			return false;
//		}
//
////		Logger.debug(this, "post", "post size %d", b.length);
//
//		short resp = (successful ? Major.SUCCESSFUL_OBJECT : Major.FAULTED_OBJECT);
//		EchoCode code = new EchoCode(resp);
//		// 发送对象
//		return post(code, flag, b);
//	}
//
//	/**
//	 * 默认是成功，向目标地址发送对象
//	 * @param flag 辅助信息
//	 * @param param 对象
//	 * @return 发送成功返回真，否则假
//	 */
//	public boolean post(CastFlag flag, Object param) {
//		return post(true, flag, param);
//	}
//}