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
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.impl.*;

/**
 * 回显数据缓存，或者称“回显数据存储器”。<br><br>
 * 
 * 回显数据缓存实现“EchoReceiver”接口。它保存来自异步数据代理（EchoAgent）的异步应答数据。<br><br>
 * 
 * 回显数据缓存在初始化时将自己绑定到“异步数据代理（EchoAgent）”上。在随后的等待过程中，
 * 通过“异步数据代理”接收到来自服务端的异步应答数据。当收到全部数据后，自动解除与异步数据代理的绑定。 
 * <br><br>
 * 
 * 在回显数据缓存的参数中，“回显数据受理器（EchoAcceptor）”和“回显标识（EchoFlag）”是必须参数，
 * “服务器地址（Node）”和“异步命令（Command）”是可选参数。
 * 
 * @author scott.liang
 * @version 1.2 5/13/2014
 * @since laxcus 1.0
 */
public class EchoBuffer implements EchoReceiver, CastWriter, Comparable<EchoBuffer> {

	/** 异步RPC接收代理。站点启动时向它注册。 **/
	private static EchoAgent agent;

	/** 站点启动器 **/
	private static SiteLauncher launcher;

	/**
	 * 设置异步数据代理。节点在启动时设置（见BasicLauncher.loadListen）。
	 * @param e 异步数据代理句柄
	 */
	public static void setEchoAgent(EchoAgent e) {
		EchoBuffer.agent = e;
	}

	/**
	 * 返回异步数据代理
	 * @return EchoAgent实例
	 */
	public static EchoAgent getEchoAgent() {
		return EchoBuffer.agent;
	}

	/**
	 * 设置站点启动器。每个站点在启动时都要调用这个方法。
	 * @param e 站点启动器句柄
	 */
	public static void setLauncher(SiteLauncher e) {
		EchoBuffer.launcher = e;
	}

	/**
	 * 返回站点启动器句柄。
	 * @return SiteLauncher实例
	 */
	public static SiteLauncher getLauncher() {
		return EchoBuffer.launcher;
	}

	/** 回显数据受理器。必选设置 **/
	private EchoAcceptor acceptor;

	/** 回显标识。必选设置 **/
	private EchoFlag flag;

	/** 服务器地址。异步数据的来源地址，可选设置 **/
	private Node hub;

	/** 请求端命令。可选设置 **/
	private Command command;

	/** 系统中断标记。当系统要求结束时，这个参数是“真”。 **/
	private boolean halted;

	/** 回显报头 **/
	private EchoHead head;

	/** 回显报尾 **/
	private EchoTail tail;

	/** 回显档案，保存数据到内存或者磁盘。在构造时指定。**/
	private EchoArchive archive;

	/** 接收本次数据的坐标位置，从0开始，逐次增加 **/
	private long seek;

	/** 统计执行"push"方法的ReplyReceiver数目，轻量级同步 **/
	private volatile int pushThreads;

	/**
	 * 构造默认的回显数据缓存
	 */
	private EchoBuffer() {
		super();
		// 默认是假
		halted = false;
		// 坐标从0开始
		seek = 0L;
		// 推送数据线程是0
		pushThreads = 0;
	}

	/**
	 * 构造回显数据缓存，指定参数
	 * @param acceptor 异步数据受理器
	 * @param flag 回显标识
	 * @param ondisk 异步应答数据写入磁盘
	 */
	public EchoBuffer(EchoAcceptor acceptor, EchoFlag flag, boolean ondisk) {
		this();
		setEchoAcceptor(acceptor);
		setFlag(flag);
		archive = new EchoArchive(flag, ondisk);
		// 绑定到异步代理
		attach();
	}

	/**
	 * 构造回显数据缓存，指定参数
	 * @param acceptor 异步数据受理器
	 * @param flag 回显标识
	 */
	public EchoBuffer(EchoAcceptor acceptor, EchoFlag flag) {
		this(acceptor, flag, false);
	}

	/**
	 * 设置异步数据受理器。必选项，不允许空值
	 * @param e EchoAcceptor实例
	 */
	private void setEchoAcceptor(EchoAcceptor e) {
		Laxkit.nullabled(e);

		acceptor = e;
	}

	/**
	 * 设置命令实例。
	 * 命令通过“CommandVisit.submit”方法被发往服务器，客户机将命令做为备分保存，以备需要。这个参数允许空值
	 * @param e Command实例
	 */
	public void setCommand(Command e) {
		command = e;
	}

	/**
	 * 返回命令实例
	 * @return Command实例
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 设置异步回显标识。必选项，不允许空值
	 * @param e EchoFlag实例
	 */
	private void setFlag(EchoFlag e) {
		Laxkit.nullabled(e);

		flag = e;
	}

	/**
	 * 设置服务器地址（异步应答数据的来源地址）。这个参数是可选项，允许空值
	 * @param e Node实例
	 */
	public void setHub(Node e) {
		hub = e;
	}

	/**
	 * 返回服务器地址（异步应答数据的来源地址）
	 * @return Node实例
	 */
	public Node getHub() {
		return hub;
	}

	/**
	 * 返回磁盘文件
	 * @return File实例
	 */
	public File getFile() {
		return archive.getFile();
	}

	/**
	 * 返回磁盘文件名称
	 * @return 文件名称
	 */
	public String getFilename() {
		return archive.getFilename();
	}

	/**
	 * 判断数据存储是磁盘文件模式
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return archive.isDisk();
	}

	/**
	 * 判断数据存储是内存模式。<br>
	 * 如果数据处理的存取全程采用内存模式，实际就是一个标准的内存计算。
	 * 
	 * @return 返回真或者假
	 */
	public boolean isMemory() {
		return archive.isMemory();
	}

	/**
	 * 返回异步数据处理器。
	 * @return EchoAcceptor实例
	 */
	public EchoAcceptor getEchoAcceptor() {
		return acceptor;
	}

	/**
	 * 绑定到异步应答转发器
	 * @return 绑定成功返回真，否则假
	 */
	public boolean attach() {
		return EchoBuffer.agent.regsiter(this);
	}

	/**
	 * 解除绑定
	 * @return 解析绑定成功返回真，否则假
	 */
	public boolean detach() {
		return EchoBuffer.agent.unregsiter(this);
	}

	/**
	 * 判断已经绑定
	 * @return 已经真或者假
	 */
	public boolean isAttached() {
		return EchoBuffer.agent.contains(this);
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
		boolean success = isCompleted();
		if (success) {
			success = (head.isSuccessful() && tail.isSuccessful());
		}
		return success;
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
	 * 在数据完全接收并且成功后，判断应答数据是“可对象化”的对象实例
	 * @return 如果是返回“真”，否则“假”。
	 */
	public boolean isSuccessObjectable() {
		return isSuccessCompleted() && isObjectable();
	}

	/**
	 * 判断退出
	 * @return 返回真或者假
	 */
	public boolean isHalted() {
		return halted;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.impl.EchoReceiver#halt()
	 */
	@Override
	public void halt() {
		// 解除绑定
		detach();
		// 设置标识
		halted = true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.impl.EchoReceiver#getEchoFlag()
	 */
	@Override
	public EchoFlag getFlag() {
		return flag;
	}

	/**
	 * 返回数据尺寸，三种情况：<br>
	 * 1. -1，异步应答端没确定。<br>
	 * 2. 0，没有数据。<br>
	 * 3. 大于0，确定长度。<br>
	 * @return 返回-1、0、>0三种。
	 */
	public long length() {
		return head.getLength();
	}

	/**
	 * 输出内存数据，原数据在内存中仍然保留
	 * @return 字节数组
	 */
	public byte[] getMemory() {
		return archive.getMemory();
	}

	/**
	 * 返回内存中的数据长度
	 * @return 数据长度的整数值
	 */
	public int getMemorySize() {
		return archive.getMemorySize();
	}

	/**
	 * 读缓存中的一段数据
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 返回读取的字节长度
	 */
	public int readMemory(byte[] b, int off, int len) {
		return archive.readMemory(b, off, len);
	}

	/**
	 * 读取出全部数据
	 * @return 返回读取的字节长度
	 */
	public byte[] readFullMemory() {
		return archive.readFullMemory();
	}

	/**
	 * 输出内存数据到磁盘上
	 * @param file 文件名
	 * @param append 追加模式。如果文件存在，数据添加到末尾。否则建立一个新文件，从头写入。
	 * @throws IOException
	 */
	public void flushMemoryToDisk(File file, boolean append) throws IOException {
		archive.flushMemoryToDisk(file, append);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EchoBuffer that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		return flag.compareTo(that.flag);
	}

	/**
	 * 返回数据坐标位置
	 * @return 坐标位置
	 */
	public long getSeek() {
		return seek;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.impl.EchoReceiver#start(com.laxcus.echo.EchoHead)
	 */
	@Override
	public boolean start(EchoHead e) {
		head = e;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.impl.EchoReceiver#push(com.laxcus.echo.EchoField)
	 */
	@Override
	public long push(EchoField e) {
		// 要求传输的下标位置
		long pos = e.getSeek();
		if (pos != seek) {
			return seek;
		}
		// 异步应答数据
		byte[] b = e.getData();

		// 数据写入磁盘或者内存
		int size = archive.write(b, 0, b.length);
		// 统计新的字节长度
		seek += size;

		// 返回下次数据传输的下标位置
		return seek;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.impl.EchoReceiver#stop(com.laxcus.echo.EchoTail)
	 */
	@Override
	public boolean stop(EchoTail e) {
		tail = e;

		// 判断成功且长度一致
		boolean success = (tail.isSuccessful() && tail.getLength() == seek);
		// 纠错
		if (!success) {
			tail.setSuccessful(false);
		}

		Logger.debug(this, "stop", success, "check point:%d - %d", seek, tail.getLength());

		// 解除与代理的绑定
		detach();
		// 通知监视器，数据接收完毕
		acceptor.look(flag);
		// 接受!
		return success;
	}

	/**
	 * 来自客户端的网关（GATE/CALL/ENTRANCE），向位于内网的服务端节点（FRONT）做投递数据准备！请求得到FRONT的NAT节点。<br><br>
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

		// 判断服务器端的地址有效且匹配
		Site site = launcher.getSite();

		if (server == null) {
			Logger.error(this, "cast", "server is null pointer!");
		}

		// 判断客户端发来的服务器地址匹配当前节点
		boolean success = site.matches(server);
		// 不成功，判断是否与NAT地址匹配！
		if (!success) {
			SocketHost clientHost = flag.getClientHost();
			success = (launcher.findPockLocal(clientHost) != null);
		}
		// 以上不成功，是错误！
		if (!success) {
			Logger.error(this, "cast", "illegal address %s / %s", server , site);
			return null;
		}

		Cipher cipher = null;
		// 如果系统服务器端要求加密时，生成密文
		if (SecureController.getInstance().isCipher(client)) {
			cipher = Cipher.create(true);
		}
		
//		if (ServerTokenManager.getInstance().isCipher(client)) {
//			cipher = Cipher.create(true);
//		}

		/** 此方法针对ReplySucker服务器（异步数据接收器），3种情况： **/

		//		/** 1. 默认是ReplySucker的内网地址，适用于大多数的集群内网环境！ **/
		//		SocketHost listener = launcher.getReplyHelper().getDefinePrivateHost();

		// 支持MIMO技术，确定ReplySucker/MISucker主机端口。
		// 1. 如果client的IP地址是公网并且不是本地同源地址，返回ReplySucker主机地址
		// 2. 如果client的IP地址是内网，循环返回一个MISucker主机地址
		SocketHost listener = launcher.getReplyHelper().findDefinePrivateHost(client);

		/** 
		 * 2. 请求者是网关（GATE/ENTRANCE/CALL），它的身份是客户端，向服务端且位于内网的FRONT，投递数据。
		 * 这时的服务端FRONT站点要从ReplySucker中找到网关（GATE/ENTRANCE/CALL）ReplyDispatcher的NAT地址。
		 * 
		 * 这是网关下传数据给FRONT前，申请获得FRONT节点接收数据入口（Sucker Server）
		 ***/
		if (isGatewayToFrontNAT(flag)) {
			// 从reply sucker服务器的记录里拿到对应的地址
			SocketHost clientHost = flag.getClientHost();
			listener = launcher.getReplyHelper().findPockLocal(clientHost);

			Logger.debug(this, "cast", "FRONT在NAT内网里，当前NAT地址！requestor is %s, local front sucker server is %s", clientHost, listener);
		}
		/** 
		 * 3. 客户端是FRONT，且位于公网，向服务端（网关）投递数据。
		 * 这时的服务端的网关返回自己的ReplySucker公网地址。 
		 * 
		 * 这是FRONT上传数据给网关前，申请获得网关节点接收数据入口（Sucker Server）
		 ***/
		else if (isWideFrontToGateway(flag)) {
			listener = launcher.getReplyHelper().getDefinePublicHost();
			Logger.debug(this, "cast", "来自FRONT，当前节点是：%s, local gateway sucker server %s", SiteTag.translate(launcher.getFamily()), listener);

			// 如果有映射地址，转换为映射主机
			if (listener.hasReflectPort()) {
				SocketHost host = new SocketHost(listener.getFamily(),
						listener.getAddress(), listener.getReflectPort());
				listener = host;

				Logger.debug(this, "cast", "%s 调整网关映射地址为 %s", launcher.getListener(), listener);
			}
		}

		// 生成异步通信令牌，这是异步通信双方交互的基础。非常重要！！！
		CastToken token = new CastToken(launcher.getFamily(), listener, flag, cipher);

		// 生成异步接收器
		ReplyReceiver rs = new ReplyReceiver(this, token);
		// 注册到服务器中
		success = launcher.getReplyHelper().push(rs);

		Logger.note(this, "cast", success, "return token: %s", token);

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
		int size = archive.write(b, off, len);
		// 写入字节长度被统计
		seek += size;

		// 返回下次数据写入位置
		return seek;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.remote.client.echo.CastWriter#asPushThread(boolean)
	 */
	@Override
	public void asPushThread(boolean running) {
		// 进入状态加1，退出减1
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

		// 如果出错显示日志
		if (!success) {
			Logger.error(this, "exit", "check point:%d - %d", seek, tail.getLength());
		}

		// 解除与代理的绑定
		detach();
		// 通知监视器，数据接收完毕
		boolean looked = acceptor.look(flag);
		if (!looked) {
			Logger.error(this, "exit", "cannot be look %s", flag);
		}

		// 接受或者否
		return success;
	}

	/**
	 * 销毁资源
	 */
	public void destroy() {
		// 解除关联
		detach();

		// 释放磁盘或者内存资源
		if (archive != null) {
			archive.destroy();
			archive = null;
		}
		// 释放其它句柄
		flag = null;
		hub = null;
		command = null;
		head = null;
		tail = null;
	}

	/**
	 * 释放资源
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		// 销毁资源
		destroy();
	}

}


//	/**
//	 * 判断当前节点是位于内网
//	 * @return 返回真或者假
//	 */
//	private boolean isPock() {
//		return launcher.isPock();
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.visit.impl.EchoReceiver#cast(com.laxcus.echo.EchoHead)
//	 */
//	@Override
//	public CastToken cast(EchoHead e) {
//		// 保存句柄
//		head = e;
//
//		// 分析请求地址与当前地址匹配
//		CastFlag flag = head.getCastFlag();
//		Address server = flag.getServer();
//		Address client = flag.getClient();
//
//		// 判断服务器端的地址有效且匹配
//		Site site = launcher.getSite();
//
//		if (server == null) {
//			Logger.error(this, "cast", "server is null pointer!");
//		}
//
//		// 判断客户端发来的服务器地址匹配当前节点
//		boolean success = site.matches(server);
//		// 不成功，判断是否与NAT地址匹配！
//		if (!success) {
//			SocketHost clientHost = flag.getClientHost();
//			success = (launcher.findPockLocal(clientHost) != null);
//		}
//		// 以上不成功，是错误！
//		if (!success) {
//			Logger.error(this, "cast", "illegal address %s / %s", server , site);
//			return null;
//		}
//
//		Cipher cipher = null;
//		// 如果系统服务器端要求加密时，生成密文
//		if (ServerTokenManager.getInstance().isCipher(client)) {
//			cipher = Cipher.create(true);
//		}
//
//		/** 此方法针对ReplySucker服务器（异步数据接收器），4种情况： **/
//
//		/** 1. 默认是ReplySucker的内网地址，适用于大多数的集群内网环境！ **/
//		SocketHost listener = launcher.getReplyHelper().getDefinePrivateHost();
//
//		/** 2. 客户端是网关（CALL/GATE/ENTRANCE），向服务端的FRONT，且位于内网，投递数据。
//		 * 这时的服务端FRONT站点从ReplySucker中找到对应网关ReplyDispatcher的NAT地址。**/
//		if (isNAT(flag)) {
//			// 从reply sucker服务器的记录里拿到对应的地址
//			SocketHost clientHost = flag.getClientHost();
//			listener = launcher.getReplyHelper().findPockLocal(clientHost);
//
//			Logger.debug(this, "cast", "FRONT在NAT内网里，当前NAT地址！client is %s, server is %s", clientHost, listener);
//		}
//		/** 3. 客户端是FRONT，且位于公网，向服务端（网关）投递数据。
//		 * 这时的服务端的网关返回它ReplySucker的公网地址。 **/
//		else if (isWideFrontToGateway(flag)) {
//			listener = launcher.getReplyHelper().getDefinePublicHost();
//		}
//		/**
//		 * 4. 客户端是任何节点，向服务端的任何节点（就是当前这个EchoBuffer）投递数据，此时服务端节点位于内网。
//		 * 这时要从服务端（就是当前EchoBuffer）的ReplySucker中找到对应网关的NAT地址，发送给客户端。
//		 * 
//		 * 任何节点包括：TOP/HOME/BANK/ACCOUNT/HASH/GATE/ENTRANCE/DATA/BUILD/WORK/CALL/LOG/WATCH
//		 */
//		else if (isPock()) {
//			// 从reply sucker服务器的记录里拿到对应的地址
//			SocketHost clientHost = flag.getClientHost();
//			listener = launcher.getReplyHelper().findPockLocal(clientHost);
//
//			Logger.debug(this, "cast", "内网穿透！%s#%s to %s:%s",
//					SiteTag.translate(flag.getClientFamily()), flag.getClientHost(),
//					SiteTag.translate(launcher.getFamily()), listener);
//		}
//
//		// 生成异步通信令牌，这是异步通信双方交互的基础。非常重要！！！
//		CastToken token = new CastToken(listener, flag, cipher);
//
//		// 生成异步接收器
//		ReplyReceiver receiver = new ReplyReceiver(this, token);
//		// 注册到服务器中
//		success = launcher.getReplyHelper().push(receiver);
//
//		Logger.note(this, "cast", success, "return token: %s", token);
//
//		// 注册成功，返回异步通信令牌；失败空指针。
//		return (success ? token : null);
//	}
