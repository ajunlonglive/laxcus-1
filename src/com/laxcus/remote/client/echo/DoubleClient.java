/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client.echo;

import java.io.*;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.fixp.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.*;
import com.laxcus.remote.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.impl.*;

/**
 * 双模式客户端。<br><br>
 * 
 * 同时支持同步/异步两种模式的客户端。<br>
 * 这个类是在RemoteClient同步请求的基础上，提供了异步数据接收能力。
 * 它实现EchoReceiver接口，接受来自异步代理（EchoAgent）的应答数据。
 * 
 * @author scott.liang
 * @version 1.2 5/13/2015
 * @since laxcus 1.0
 */
public class DoubleClient extends RemoteClient implements EchoReceiver, CastWriter { 

	/** 异步RPC应答数据接收代理 **/
	private static EchoAgent agent;

	/**
	 * 设置异步RPC接收代理。节点在启动时设置。
	 * @param e EchoAgent实例
	 */
	public static void setEchoAgent(EchoAgent e) {
		DoubleClient.agent = e;
	}

	/**
	 * 返回异步RPC接收代理
	 * @return EchoAgent实例
	 */
	public static EchoAgent getEchoAgent() {
		return DoubleClient.agent;
	}

	/** 站点启动器 **/
	private static SiteLauncher launcher;

	/**
	 * 设置站点启动器。每个站点在启动时都要调用这个方法。
	 * @param e 站点启动器句柄
	 */
	public static void setLauncher(SiteLauncher e) {
		DoubleClient.launcher = e;
	}

	/**
	 * 返回站点启动器句柄。
	 * @return SiteLauncher实例
	 */
	public static SiteLauncher getLauncher() {
		return DoubleClient.launcher;
	}

	/** 系统中断标记。当系统要求结束时，这个参数是true **/
	private boolean halted;

	/** 回显标识 **/
	private EchoFlag flag;

	/** 回显报告头 **/
	private EchoHead head;

	/** 回显尾端 **/
	private EchoTail tail;

	/** 异步应答数据缓存 **/
	private ContentBuffer buff = new ContentBuffer();

	/** 当前接收的数据下标位置，从0开始，逐次增加 **/
	private long seek;

	/** 统计执行"push"方法的ReplyReceiver数目，轻量级同步 **/
	private volatile int pushThreads;
	
	/**
	 * 构造一个双请求客户端，指定它的传输模式
	 * @param stream 流模式（TCP）
	 */
	protected DoubleClient(boolean stream) {
		super(stream);
		// 默认是false
		halted = false;
		// 从0开始
		seek = 0L;
		// 初始0
		pushThreads = 0;
	}

	/**
	 * 构造双请求客户端，指定传输模式和Visit接口名称
	 * @param stream 流模式
	 * @param interfaceName Visit接口名称
	 */
	protected DoubleClient(boolean stream, String interfaceName) {
		this(stream);
		super.setVisitName(interfaceName);
	}

	/**
	 * 返回异步应答报头
	 * @return EchoHead实例
	 */
	public EchoHead getHead() {
		return head;
	}

	/**
	 * 返回异步应答报尾
	 * @return EchoTail实例
	 */
	public EchoTail getTail() {
		return tail;
	}

	/**
	 * 判断接收的数据是“可对象化”的。<br>
	 * 对象化的应答数据可以用“com.laxcus.remote.Reply”还原为对象。
	 * @return 如果是返回”真“，否则”假“。
	 */
	public boolean isObjectable() {
		return head != null && head.isObjectable();
	}

	/**
	 * 判断全部异步数据接收工作完成。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean isCompleted() {
		return head != null && tail != null;
	}

	/**
	 * 在数据全部收到的情况下，判断成功。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean isSuccessCompleted() {
		boolean b = isCompleted();
		if (b) {
			b = (head.isSuccessful() && tail.isSuccessful());
		}
		return b;
	}

	/**
	 * 在数据完全接收的情况下，判断发生故障。
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean isFaultCompleted() {
		boolean b = isCompleted();
		if (b) {
			b = (head.isFaulted() || tail.isFaulted());
		}
		return b;
	}

	/**
	 * 判断系统中断
	 * @return 返回真或者假
	 */
	public boolean isHalted() {
		return halted;
	}

	/**
	 * 根据传入的异步回显标识，注册绑定到RPC异步数据转代器。
	 * @param e 回显标识
	 * @throws EchoException 用户在一个实例中调用新方法和重新绑定时，必须先解除锁定，否则弹出异常
	 * @return  成功返回true，失败false。
	 */
	protected boolean attach(EchoFlag e) {
		// 如果处在绑定状态，弹出异常。解除绑定由外部判断处理。
		if (isAttached()) {
			throw new EchoException("attached by '%s'", flag);
		}
		// 空指针错误，这个错误一般发生在编码中
		Laxkit.nullabled(e);

		// 保存回显标识
		flag = e.duplicate();
		// 注册到异步转发器
		return DoubleClient.agent.regsiter(this);
	}

	/**
	 * 根据传入的命令，注册绑定到RPC异步数据转代器。
	 * @param method  操作命令
	 * @throws EchoException 用户在一个实例中调用新方法和重新绑定时，必须先解除锁定，否则弹出异常
	 * @return  成功返回true，失败false。
	 */
	protected boolean attach(Command method) {
		Cabin site = method.getSource();
		// 回显地址空指针，这个错误发生在编码中
		Laxkit.nullabled(site);

		return attach(site.getFlag());
	}

	/**
	 * 从RPC异步数据转发器中解除关联
	 * @return 解除返回true，否则返回false。
	 */
	protected boolean detach() {
		// 如果没有定义异步回显标识，不需要解除绑定
		if (flag == null) {
			return false;
		}
		return DoubleClient.agent.unregsiter(this);
	}

	/**
	 * 判断绑定
	 * @return 返回真或者假
	 */
	public boolean isAttached() {
		return DoubleClient.agent.contains(this);
	}

	/**
	 * 外部中断通知，结束异步处理。
	 * @see com.laxcus.visit.impl.EchoReceiver#halt()
	 */
	@Override
	public void halt() {
		// 解除绑定
		detach();
		// 关闭与服务器的连接
		super.destroy();
		// 设置退出标志
		halted = true;
	}

	/**
	 * 返回客户端绑定的回显标识
	 * @see com.laxcus.visit.impl.EchoReceiver#getFlag()
	 */
	@Override
	public EchoFlag getFlag() {
		return flag;
	}

	/**
	 * 以阻塞方式读取一段数据
	 * @param b 字节数组
	 * @param off 开始下标 
	 * @param len 有效长度
	 * @return 成功返回读取字节尺寸，否则返回-1。如果是0，延时后再读。
	 * @throws IOException 在读取数据过程中，如果发生来自系统的强制中断，弹出EOF异常
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		do {
			// 如果系统已经要求退出，弹出异常
			if (isHalted()) {
				throw new EOFException();
			}
			// 读字节
			int readlen = buff.read(b, off, len);
			if (readlen > 0) {
				return readlen;
			}
			// 数据接收完成，返回-1；否则进入延时等待
			if (isCompleted()) {
				return -1;
			} else {
				delay(50);
			}
		} while (true);
	}

	/**
	 * 等待全部数据接收完成，读取全部数据。
	 * @return 返回读取的字节数组
	 * @throws IOException 在等待完成过程中，如果发生来自系统强制中断，弹出EOF异常
	 */
	public byte[] readFully() throws IOException {
		while (!isCompleted()) {
			if (isHalted()) {
				throw new EOFException();
			} else {
				delay(50);
			}
		}
		// 读取全部缓存数据
		return buff.readFully();
	}

	/**
	 * 还原异步应答中的对象
	 * @return 返回对象
	 * @throws VisitException
	 */
	public Object getObject() throws VisitException {
		if (!isCompleted()) {
			throw new VisitException("be running!");
		}
		// 判断是对象。对象有两种：成功或者错误的
		if (!isObjectable()) {
			throw new VisitException("cannot be objectable! echo code \"%s\"", head.getCode());
		}

		byte[] data = buff.toByteArray();
		// 解析数据
		PatternExtractor reply = null;
		try {
			reply = PatternExtractor.resolve(data);
		} catch (Throwable e) {
			Logger.fatal(e);
			throw new VisitException(e);
		}
		if (reply == null) {
			throw new VisitException("reply is null pointer!");
		}
		// 如果有错误，弹出异常。
		if (reply.getThrowable() != null) {
			throw new VisitException(reply.getThrowText());
		}
		// 返回对象
		return reply.getObject();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.remote.client.RemoteClient#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
		// 解除关联
		detach();

		// 关闭和清除数据
		buff.clear();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.impl.EchoReceiver#start(com.laxcus.echo.EchoHead)
	 */
	@Override
	public boolean start(EchoHead e) {
		head = e;
		// 接受!
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.impl.EchoReceiver#push(com.laxcus.echo.EchoField)
	 */
	@Override
	public long push(EchoField field) {
		// 数据在数组队列中的下标位置
		long pos = field.getSeek();
		// 如果与要求不符，返回指定的位置
		if (pos != seek) {
			return seek;
		}
		// 数据
		byte[] b = field.getData();
		// 数据添加到末尾
		int size = buff.append(b, 0, b.length);
		// 统计写入的字节长度
		seek += size;

		// 有接收等待，唤醒它
		wakeup();

		// 返回下次数据发送的开始位置
		return seek;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.remote.client.echo.CastWriter#asPushThread(boolean)
	 */
	@Override
	public void asPushThread(boolean running) {
		// 进入状态增加，退出减1
		if (running) {
			pushThreads += 1;
		} else {
			pushThreads -= 1;
		}
	}
	
	/**
	 * 返回当前执行“push”异步写入数据的线程数目
	 * @return 返回当前值
	 */
	public int getPushThreads() {
		return pushThreads;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.impl.EchoReceiver#stop(com.laxcus.echo.EchoTail)
	 */
	@Override
	public boolean stop(EchoTail e) {
		tail = e;

		// 判断长度一致
		boolean success = (tail.isSuccessful() && tail.getLength() == seek);
		// 纠错
		if (!success) {
			tail.setSuccessful(false);
		}

		// 解除锁定
		detach();
		// 如果有等待，唤醒它
		wakeup();
		// 处理成功
		return success;
	}

	/**
	 * 来自客户端的网关（GATE/CALL/ENTRANCE），向位于内网的服务端节点（FRONT/其它...），做投递数据准备！请求得到内网的NAT节点。<br><br>
	 * 
	 * 这时的服务端，位于内网的FRONT节点要返回它的NAT出口地址！<br>
	 * 
	 * 对应“EchoInvoker.createCastFlag”函数方法。
	 * 
	 * @param flag 异步通信标识
	 * @return 返回真或者假
	 */
	private boolean isGatewayToFrontNAT(CastFlag flag) {
		// 1. 判断客户端来自网关！
		boolean success = SiteTag.isGateway(flag.getClientFamily());
		// 2. 判断服务器位于内网
		if (success) {
			success = launcher.isPock();
		}
		return success;
	}

	/**
	 * 来自客户端的FRONT节点，且位于公网，向服务器端的网关（GATE/ENTRANCE/CALL），做投递数据准备！<br><br>
	 * 
	 * 这时的服务端，当前的网关要返回它的公网地址！<br>
	 * 
	 * 
	 * 对应“EchoInvoker.createCastFlag”函数方法。
	 * 
	 * @param flag 异步通信标识
	 * @return 返回真或者假
	 */
	private boolean isWideFrontToGateway(CastFlag flag) {
		// 1. 判断客户是FRONT站点且位于公网上
		boolean success = (SiteTag.isFront(flag.getClientFamily()) && 
				flag.getClient().isWideAddress());
		// 2. 判断服务器端是网关（GATE/ENTRANCE/CALL节点）
		if (success) {
			success = launcher.isGateway();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.impl.EchoReceiver#cast(com.laxcus.echo.EchoHead)
	 */
	@Override
	public CastToken cast(EchoHead e) {
		// 保存句柄
		head = e;

		// 分析请求地址与当前地址匹配
		CastFlag flag = head.getCastFlag();
		Address server = flag.getServer();
		Address client = flag.getClient();

		// 判断地址有效且匹配
		Site site = launcher.getSite();
		boolean success = (server != null && site.matches(server));
		if (!success) {
			Logger.error(this, "cast", "illegal address %s", server);
			return null;
		}

		Cipher cipher = null;
		// 如果系统服务器端要求加密时，生成密文
		if (SecureController.getInstance().isCipher(client)) {
			cipher = Cipher.create(true);
		}
		
//		if(ServerTokenManager.getInstance().isCipher(client)) {
//			cipher = Cipher.create(true);
//		}
		
		/** 此方法执行方是服务器，三种情况： **/

		/** 1. 默认是异步数据接收器（ReplySucker）的内网地址，适用于大多数的集群内网环境！ **/
		SocketHost listener = launcher.getReplyHelper().getDefinePrivateHost();

		/** 2. 客户端是网关（CALL/GATE/ENTRANCE），向服务端的FRONT，且位于内网，投递数据。
		 * 这里服务端的FRONT从ReplySucker中找到对应网关ReplyDispatcher的NAT地址。**/
		if (isGatewayToFrontNAT(flag)) {
			// 从reply sucker服务器的记录里拿到对应的地址
			SocketHost clientHost = flag.getClientHost();
			listener = launcher.getReplyHelper().findPockLocal(clientHost);

			Logger.debug(this, "cast", "FRONT在NAT内网里，当前NAT地址！requestor is %s, server is %s", clientHost, listener);
		}
		/** 3. 客户端是FRONT，且位于公网，向服务端（网关）投递数据。
		 * 这时的服务端的网关返回它ReplySucker的公网地址。 **/
		else if (isWideFrontToGateway(flag)) {
			listener = launcher.getReplyHelper().getDefinePublicHost();
		}

		// 生成异步通信令牌，这是异步通信双方交互的基础。非常重要！！！
		CastToken token = new CastToken(launcher.getFamily(), listener, flag, cipher);

		// 生成异步接收器
		ReplyReceiver rs = new ReplyReceiver(this, token);
		// 注册到服务器中
		success = launcher.getReplyHelper().push(rs);
		// 注册成功，返回异步通信令牌；失败空指针。
		return (success ? token : null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.remote.client.echo.CastWriter#push(long, byte[], int, int)
	 */
	@Override
	public long push(long pos, byte[] b, int off, int len) {
		// 判断传入下标与当前下标匹配，不匹配返回实际下标
		if (pos != seek) {
			return seek;
		}

		// 数据写入磁盘或者内存
		int size = buff.append(b, off, len);
		// 写入字节长度被统计
		seek += size;

		// 返回下次数据写入位置
		return seek;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.impl.EchoReceiver#exit(com.laxcus.echo.EchoTail)
	 */
	@Override
	public boolean exit(EchoTail e) {
		tail = e;
		// 判断长度一致
		boolean success = (tail.isSuccessful() && tail.getLength() == seek);
		// 纠错
		if (!success) {
			tail.setSuccessful(false);
		}

		// 解除锁定
		detach();
		// 如果有等待，唤醒它
		wakeup();
		// 处理成功
		return success;
	}

}