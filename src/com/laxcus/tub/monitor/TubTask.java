/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.monitor;

import java.io.*;
import java.net.*;

import com.laxcus.log.client.*;
import com.laxcus.thread.*;
import com.laxcus.tub.*;
import com.laxcus.tub.command.*;
import com.laxcus.tub.invoke.*;
import com.laxcus.tub.product.*;
import com.laxcus.tub.turn.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 边缘容器数据流处理器。分析命令后，决定数据流的处理方向。
 * 
 * @author scott.liang
 * @version 1.0 10/10/2020
 * @since laxcus 1.0
 */
final class TubTask extends VirtualThread {

	/** SOCKET句柄 **/
	private Socket socket;

	/** 数据接收器 **/
	private TubInputStream receiver;

	/** 数据输出器 **/
	private TubOutputStream sender;

	/** 对方地址 **/
	private SocketHost remote;

	/** 数据流转调接口 **/
	private TubStreamMonitor monitor;

	/** RPC调用接口 **/
	private TubVisitInvoker visitInvoker;

	/** 方法调用接口 **/
	private TubMethodInvoker methodInvoker; 

	/** 根据命令判断是否退出 **/
	private boolean exited;

	/**
	 * 构造一个边缘容器数据流处理器，同时指定它需要的参数
	 * @param sock 套接字接口
	 * @param transmitter 边缘容器 TCP服务器监听接口
	 * @param visit RPC访问接口
	 * @param stream 数据流任务调用器
	 * @throws IOException
	 */
	public TubTask(Socket sock, TubStreamMonitor transmitter,
			TubVisitInvoker visit, TubMethodInvoker method) throws IOException {
		super();
		exited = false;
		socket = sock;
		remote = new SocketHost(SocketTag.TCP, sock.getInetAddress(), sock.getPort());

		receiver = new TubInputStream(sock.getInputStream());
		sender = new TubOutputStream(sock.getOutputStream());

		monitor = transmitter;
		visitInvoker = visit;
		methodInvoker = method;
	}

	/**
	 * 判断关闭
	 * @return 返回真或者假
	 */
	public boolean isClosed() {
		return (socket == null || socket.isClosed());
	}

	/**
	 * 关闭SOCKET
	 */
	private void close() {
		if (isClosed()) {
			return;
		}

		try {
			if (socket.isInputShutdown()) {
				socket.shutdownInput();
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		try {
			if (socket.isOutputShutdown()) {
				socket.shutdownOutput();
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		try {
			if (receiver != null) {
				receiver.close();
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			receiver = null;
		}

		try {
			if (sender != null) {
				sender.close();
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			sender = null;
		}

		try {
			socket.close();
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			socket = null;
		}
	}

	/**
	 * 根据边缘容器请求，执行RPC操作
	 * @param request 边缘容器请求数据流
	 */
	private void callRPC(TubStream request) throws IOException {
		byte[] data = request.getData();
		//1. 解析RPC数据
		TubConstructor apply = null;
		TubExtractor reply = null;
		try {
			if(data == null) {
				Logger.error(this, "callRPC", "rpc data null, from %s", request.getRemote());
				throw new NullPointerException("rpc data is null");
			}
			apply = TubConstructor.resolve(data);
		} catch (Throwable e) {
			Logger.fatal(e);
			reply = new TubExtractor(e);
		}
		//2. 调用对象实例，返回结果
		if (reply == null) {
			reply = visitInvoker.invoke(apply);
		}
		//3. 生成应答数据流
		data = reply.build();

		//4. 发送回客户端
		Mind cmd = new Mind(MindAnswer.OKAY);
		TubStream resp = new TubStream(cmd);
		resp.addMessage(SliceKey.CONTENT_TYPE, SliceKey.RAW_DATA);
		resp.setData(data, 0, data.length);
		send(resp);
	}

	/**
	 * 处理远程方法
	 * @param request
	 * @throws IOException
	 */
	private void callMethod(TubStream request) throws IOException {
		Mind mind = request.getMind();

		byte[] data = request.getData();
		
		ClassReader reader = new ClassReader(data);
		TubProduct product = null;

		// 判断方法，分别调用！
		if (MindAssert.isRunTubService(mind)) {
			TubRunService cmd = new TubRunService(reader);
			product = methodInvoker.doTubRunService(cmd);
		} else if (MindAssert.isStopTubService(mind)) {
			TubStopService cmd = new TubStopService(reader);
			product = methodInvoker.doTubStopService(cmd);
		} else if (MindAssert.isPrintTubService(mind)) {
			TubPrintService cmd = new TubPrintService(reader);
			product = methodInvoker.doTubPrintService(cmd);
		} else if (MindAssert.isShowTubService(mind)) {
			TubShowContainer cmd = new TubShowContainer(reader);
			product = methodInvoker.doTubShowContainer(cmd);
		} else if (MindAssert.isCheckTubListen(mind)) {
			TubCheckListen cmd = new TubCheckListen(reader);
			product = methodInvoker.doTubCheckListen(cmd);
		}
		// 以上不成立时，通知不支持退出!
		else {
			exited = true;
			// 通知对方!
			unsupport();
			return;
		}

		// 处理结果 ...
		if (product != null) {
			Mind mark = new Mind(MindAnswer.OKAY);
			TubStream resp = new TubStream(mark);
			resp.setData(product.build());
			send(resp);
		} else {
			exited = true;
			unsupport();
		}
	}

	/**
	 * 发送边缘容器数据流到目标站点
	 * @param stream 边缘容器反馈
	 * @throws IOException
	 */
	private void send(TubStream stream) throws IOException {
		byte[] b = stream.build();
		sender.write(b, 0, b.length);
		sender.flush();
	}

	/**
	 * 结束对话。向客户机发送“再见”命令
	 * @throws IOException 发生异常
	 */
	private void goodbye() throws IOException {
		Mind mark = new Mind(MindAnswer.GOODBYE);
		TubStream resp = new TubStream(mark);
		resp.addMessage(SliceKey.SPEAK, "see you next time");
		send(resp);
	}

	/**
	 * 返回测试流
	 * @throws IOException
	 */
	private void test() throws IOException {
		Logger.debug(this, "test", "from %s", remote);

		// 反馈结果
		Mind mark = new Mind(MindAnswer.ACCEPTED);
		TubStream resp = new TubStream(mark);
		resp.addMessage(SliceKey.SPEAK, "RE:TEST!");

		//	resp.setData("RE:TEST!".getBytes());

		// 发送反馈数据流
		send(resp);
	}

	//	/**
	//	 * 返回当前服务器的安全选项(地址验证、密文验证、地址/密文复合验证)
	//	 * @throws SecureException, IOException
	//	 */
	//	private void notifySecure() throws SecureException, IOException {
	//		Address client = remote.getAddress();
	//		ServerToken token = ServerTokenManager.getInstance().find(client);
	//		if (token == null) {
	//			throw new SecureException(
	//					"cannot be find server-token by '%s', please check local.xml", client);
	//		}
	//		int family = token.getFamily();
	//
	//		Mind cmd = new Mind(MindAnswer.SECURE_NOTIFY);
	//		TubStream resp = new TubStream(cmd);
	//		resp.addMessage(SliceKey.SECURE_FAMILY, family);
	//
	//		//		resp.addMessage(SliceKey.SECURE_FAMILY, TubMonitor.skey.getFamily());
	//		send(resp);
	//	}

	//	/**
	//	 * 接受客户端要求，执行安全初始化
	//	 * @param request 边缘容器请求流
	//	 * @return 初始化成功返回真，否则假
	//	 * @throws IOException
	//	 */
	//	private boolean createSecure(TubStream request) throws IOException {
	//		byte[] data = request.getData();
	//		Cipher cipher = new Cipher();
	//
	//		Address client = remote.getAddress();
	//		ServerToken token = ServerTokenManager.getInstance().find(client);
	//		cipher.decase(token.getKey(), data, 0, data.length);
	//
	//		//		cipher.resolve(TubMonitor.skey.getPrivateKey(), data, 0, data.length);
	//
	//		short code = MindAnswer.SECURE_ACCEPTED;
	//		Mind cmd = new Mind(code);
	//		TubStream resp = new TubStream(cmd);
	//		resp.addMessage(SliceKey.SPEAK, "secure okay");
	//		// 发送数据流
	//		send(resp);
	//		// 给输入/输出设置密文
	//		receiver.setCipher(cipher);
	//		sender.setCipher(cipher);
	//
	//		return true;
	//	}

	/**
	 * 发送处理无效给请求端
	 * @param code 反馈码
	 * @throws IOException
	 */
	private void unsupport() throws IOException {
		Mind mark = new Mind(MindAnswer.UNSUPPORT);
		TubStream resp = new TubStream(mark);
		resp.addMessage(SliceKey.SPEAK, "sorry!");
		send(resp);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 关闭SOCKET
		close();
		// 通知释放自己
		monitor.remove(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while (!isInterrupted()) {
			if (exited) {
				break;
			}
			try {
				subprocess();
			} catch (TubProtocolException e) {
				String from = String.format("from %s", remote);
				Logger.error(from, e);
				break;
			} catch (IOException e) {
				String from = String.format("from %s", remote);
				Logger.error(from, e);
				break;
			} catch (Throwable t) {
				String from = String.format("from %s", remote);
				Logger.fatal(from, t);
				break;
			}
		}
		// 退出线程
		setInterrupted(true);
	}

	/**
	 * 数据流操作
	 * @throws IOException
	 */
	private void subprocess() throws IOException {
		// 读数据流(同时解密)，但是不包括数据域。数据域是否读取根据命令判断
		TubStream stream = new TubStream(remote, receiver, sender);
		stream.read(false);

		// 取命令，判断应用
		Mind mind = stream.getMind();

		//		Logger.debug("TubTask.subprocess, command is %d,%d", cmd.getMajor(), cmd.getMinor());

		//		if (MindAssert.isSecureQuery(mind)) {
		//			// 数据流请求，首先进行加密询问
		//			notifySecure();
		//		} else if (MindAssert.isSecureCreate(mind)) {
		//			// 安全初始化，密文在数据域，必须读
		//			stream.readBody();
		//			boolean success = createSecure(stream);
		//			if (!success) exited = true;
		//		} else if(MindAssert.isSecureDrop(mind)) {
		//			Logger.warning(this, "subprocess", "illegal secure drop!");
		//			exited = true;
		//			invalid(MindAnswer.UNSUPPORT); // TCP不支持撤销密文的操作，它在通信结束时撤销密文。
		//		} else 

		if (MindAssert.isExit(mind)) {
			exited = true;
			goodbye();
		} 
		//		else if(ServerTokenManager.getInstance().isCipher(remote.getAddress()) && !receiver.isSecured()) {
		//			// 如果服务要求加密但是当前处于未加密状态时，要求客户端加密
		//			stream.readBody();
		//			notifySecure();
		//		} 
		else if(MindAssert.isTest(mind)) { 
			stream.readBody();
			test(); // 反馈测试
		} else if (MindAssert.isRPC(mind)) {
			// RPC调用，读取全部数据，交由实例处理
			stream.readBody();
			callRPC(stream);
		} else if(MindAssert.isMethod(mind)) {
			stream.readBody();
			callMethod(stream);
		}
		// 不能确定的操作，退出操作！
		else {
			Logger.error(this, "subprocess", "invalid! %s", mind);
			exited = true;
			// 通知对方!
			unsupport();
		}

	}

}