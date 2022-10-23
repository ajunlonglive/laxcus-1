/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

import java.io.*;
import java.net.*;

import com.laxcus.fixp.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.*;
import com.laxcus.security.*;
import com.laxcus.thread.*;
import com.laxcus.util.net.*;

/**
 * FIXP数据流处理器。分析命令后，决定数据流的处理方向。
 * 
 * @author scott.liang
 * @version 1.1 5/17/2012
 * @since laxcus 1.0
 */
final class StreamTask extends VirtualThread {

	/** SOCKET句柄 **/
	private Socket socket;

	/** 数据接收器 **/
	private FixpInputStream receiver;

	/** 数据输出器 **/
	private FixpOutputStream sender;

	/** 对方地址 **/
	private SocketHost remote;

	/** 数据流转调接口 **/
	private StreamTransmitter streamTransmitter;

	/** RPC调用接口 **/
	private VisitInvoker visitInvoker;

	/** 流处理接口 **/
	private StreamInvoker streamInvoker;

	/** 根据命令判断是否退出 **/
	private boolean exited;

	/**
	 * 构造一个FIXP数据流处理器，同时指定它需要的参数
	 * @param sock 套接字接口
	 * @param transmitter FIXP TCP服务器监听接口
	 * @param visit RPC访问接口
	 * @param stream 数据流任务调用器
	 * @throws IOException
	 */
	public StreamTask(Socket sock, StreamTransmitter transmitter, VisitInvoker visit, 
			StreamInvoker stream) throws IOException {
		super();
		exited = false;
		socket = sock;
		remote = new SocketHost(SocketTag.TCP, sock.getInetAddress(), sock.getPort());

		receiver = new FixpInputStream(sock.getInputStream());
		sender = new FixpOutputStream(sock.getOutputStream());

		streamTransmitter = transmitter;
		visitInvoker = visit;
		streamInvoker = stream;
	}

	/**
	 * 判断关闭
	 * @return 返回真或者假
	 */
	public boolean isClosed() {
		return socket == null;
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
	 * 根据FIXP请求，执行RPC操作
	 * @param request FIXP请求数据流
	 */
	private void callRPC(Stream request) throws IOException {
		byte[] data = request.getData();
		//1. 解析RPC数据
		PatternConstructor apply = null;
		PatternExtractor reply = null;
		try {
			if(data == null) {
				Logger.error(this, "callRPC", "rpc data null, from %s", request.getRemote());
				throw new NullPointerException("rpc data is null");
			}
			apply = PatternConstructor.resolve(data);
		} catch (Throwable e) {
			Logger.fatal(e);
			reply = new PatternExtractor(e);
		}
		//2. 调用对象实例，返回结果
		if (reply == null) {
			reply = visitInvoker.invoke(apply);
		}
		//3. 生成应答数据流
		data = reply.build();

		//4. 发送回客户端
		Mark cmd = new Mark(Answer.OKAY);
		Stream resp = new Stream(cmd);
		resp.addMessage(MessageKey.CONTENT_TYPE, MessageKey.RAW_DATA);
		resp.setData(data, 0, data.length);
		send(resp);
	}

	/**
	 * 根据FIXP请求，调用流接口
	 * @param stream FIXP请求数据流
	 * @throws IOException
	 */
	private void callMethod(Stream stream) throws IOException {		
		// 调用对象实例
		streamInvoker.invoke(stream, sender);
	}

	/**
	 * 发送FIXP数据流到目标站点
	 * @param stream FIXP反馈
	 * @throws IOException
	 */
	private void send(Stream stream) throws IOException {
		byte[] b = stream.build();
		sender.write(b, 0, b.length);
		sender.flush();
	}

	/**
	 * 结束对话。向客户机发送“再见”命令
	 * @throws IOException 发生异常
	 */
	private void exit() throws IOException {
		Mark mark = new Mark(Answer.GOODBYE);
		Stream resp = new Stream(mark);
		resp.addMessage(MessageKey.SPEAK, "see you next time");
		send(resp);
	}

	/**
	 * 返回测试流
	 * @throws IOException
	 */
	private void test() throws IOException {
		//		Logger.debug(this, "test", "from %s", remote);

		// 反馈结果
		Mark mark = new Mark(Answer.ACCEPTED);
		Stream resp = new Stream(mark);
		resp.addMessage(MessageKey.SPEAK, "RE:TEST!");
		// 记录来源主机地址
		byte[] b = remote.build();
		resp.addMessage(MessageKey.HOST, b);

		//	resp.setData("RE:TEST!".getBytes());

		// 发送反馈数据流
		send(resp);
	}

	/**
	 * 返回当前服务器的安全选项(地址验证、密文验证、地址/密文复合验证)
	 * @throws SecureException, IOException
	 */
	private boolean notifySecure() throws SecureException, IOException {
		Address client = remote.getAddress();
		// 查找客户机密钥
		PublicSecure token = SecureController.getInstance().findPublicSecure(client);
		// 判断成功
		boolean success = (token != null);

		// 不成功，记录这个主机
		if (!success) {
			Logger.error(this, "notifySecure", "cannot be find security-token, from %s", remote);
		}

		// 生成命令
		Mark cmd = new Mark(success ? Answer.SECURE_NOTIFY : Answer.REFUSE);
		Stream resp = new Stream(cmd);
		if (success) {
			resp.addMessage(MessageKey.SECURE_FAMILY, token.getFamily());
			resp.setData(token.build());
		}

		//		Logger.debug(this, "notifySecure", "secure type %d - %s, data length:%d", token.getFamily(),
		//				SecureType.translate(token.getFamily()), resp.getData().length);

		send(resp);
		// 返回处理结果
		return success;
	}

	/**
	 * 接受客户端要求，执行安全初始化
	 * @param request FIXP请求流
	 * @return 初始化成功返回真，否则假
	 * @throws IOException
	 */
	private boolean createSecure(Stream request) throws IOException {
		byte[] data = request.getData();
		Cipher cipher = new Cipher();

		Address client = remote.getAddress();
		// 服务端查找RSA私钥
		SecureToken token = SecureController.getInstance().find(client);
		if (token == null) {
			Logger.error(this, "createSecure", "cannot be find RSA private key!");
			return false;
		}
		// 用RAS私钥解密数据
		cipher.decase(token.getServerKey().getKey(), data, 0, data.length);

		//		cipher.resolve(FixpMonitor.skey.getPrivateKey(), data, 0, data.length);

		short code = Answer.SECURE_ACCEPTED;
		Mark cmd = new Mark(code);
		Stream resp = new Stream(cmd);
		resp.addMessage(MessageKey.SPEAK, "secure okay");
		// 发送数据流
		send(resp);
		// 给输入/输出设置密文
		receiver.setCipher(cipher);
		sender.setCipher(cipher);

		return true;
	}

	/**
	 * 发送处理无效给请求端
	 * @param code 反馈码
	 * @throws IOException
	 */
	private void invalid(short code) throws IOException {
		Mark mark = new Mark(code);
		Stream resp = new Stream(mark);
		resp.addMessage(MessageKey.SPEAK, "sorry!");
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
		streamTransmitter.remove(this);
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
			} catch (FixpProtocolException e) {
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
		Stream stream = new Stream(remote, receiver, sender);
		stream.read(false);

		// 取命令，判断应用
		Mark mark = stream.getMark();

		// 服务器要求加密，接收端还没有执行加密时
		boolean missing = (SecureController.getInstance().isCipher(remote.getAddress()) 
				&& !receiver.isSecured());
		if (missing) {
			boolean success = (Assert.isTest(mark) || Assert.isRPCall(mark));
			if (success) {
				// 如果服务要求加密但是当前处于未加密状态时，要求客户端加密
				stream.readBody();
				notifySecure();
				return;
			}
		}

		// Logger.debug("StreamTask.subprocess, command is %d,%d", cmd.getMajor(), cmd.getMinor());

		if (Assert.isSecureQuery(mark)) {
			// 数据流请求，首先进行加密询问
			boolean success = notifySecure();
			if (!success) exited = true;
		} else if (Assert.isSecureCreate(mark)) {
			// 安全初始化，密文在数据域，必须读
			stream.readBody();
			boolean success = createSecure(stream);
			if (!success) exited = true;
		} else if(Assert.isSecureDrop(mark)) {
			Logger.warning(this, "subprocess", "illegal secure drop!");
			exited = true;
			invalid(Answer.UNSUPPORT); // TCP不支持撤销密文的操作，它在通信结束时撤销密文。
		} else if (Assert.isExit(mark)) {
			exited = true;
			exit();
		}

		////		} else if(ServerTokenManager.getInstance().isCipher(remote.getAddress()) && !receiver.isSecured()) {
		////			// 如果服务要求加密但是当前处于未加密状态时，要求客户端加密
		////			stream.readBody();
		////			notifySecure();
		//			
		//		} else if(SecureController.getInstance().isCipher(remote.getAddress()) && !receiver.isSecured()) {
		//			// 如果服务要求加密但是当前处于未加密状态时，要求客户端加密
		//			stream.readBody();
		//			notifySecure();
		//		}

		else if (Assert.isTest(mark)) {
			stream.readBody();
			test(); // 反馈测试
		} else if (Assert.isRPCall(mark)) {
			// RPC调用，读取全部数据，交由实例处理
			stream.readBody();
			callRPC(stream);
		} else if (streamInvoker != null) {
			// 数据读操作，数据域交由实类处理
			callMethod(stream);
		} else {
			// 不能确定的操作，退出操作！
			Logger.error(this, "subprocess", "invalid! %s", mark);
			exited = true;
			// 通知对方!
			invalid(Answer.UNSUPPORT);
		}
	}

	//	/**
	//	 * 数据流操作
	//	 * @throws IOException
	//	 */
	//	private void subprocess() throws IOException {
	//		// 读数据流(同时解密)，但是不包括数据域。数据域是否读取根据命令判断
	//		Stream stream = new Stream(remote, receiver, sender);
	//		stream.read(false);
	//
	//		// 取命令，判断应用
	//		Mark mark = stream.getMark();
	//
	//		//		Logger.debug("StreamTask.subprocess, command is %d,%d", cmd.getMajor(), cmd.getMinor());
	//
	//		if (Assert.isSecureQuery(mark)) {
	//			// 数据流请求，首先进行加密询问
	//			boolean success = notifySecure();
	//			if (!success) exited = true;
	//		} else if (Assert.isSecureCreate(mark)) {
	//			// 安全初始化，密文在数据域，必须读
	//			stream.readBody();
	//			boolean success = createSecure(stream);
	//			if (!success) exited = true;
	//		} else if(Assert.isSecureDrop(mark)) {
	//			Logger.warning(this, "subprocess", "illegal secure drop!");
	//			exited = true;
	//			invalid(Answer.UNSUPPORT); // TCP不支持撤销密文的操作，它在通信结束时撤销密文。
	//		} else if (Assert.isExit(mark)) {
	//			exited = true;
	//			exit();
	//			
	////		} else if(ServerTokenManager.getInstance().isCipher(remote.getAddress()) && !receiver.isSecured()) {
	////			// 如果服务要求加密但是当前处于未加密状态时，要求客户端加密
	////			stream.readBody();
	////			notifySecure();
	//			
	//		} else if(SecureController.getInstance().isCipher(remote.getAddress()) && !receiver.isSecured()) {
	//			// 如果服务要求加密但是当前处于未加密状态时，要求客户端加密
	//			stream.readBody();
	//			notifySecure();
	//		} else if(Assert.isTest(mark)) { 
	//			stream.readBody();
	//			test(); // 反馈测试
	//		} else if (Assert.isRPCall(mark)) {
	//			// RPC调用，读取全部数据，交由实例处理
	//			stream.readBody();
	//			callRPC(stream);
	//		} else if (streamInvoker != null) {
	//			// 数据读操作，数据域交由实类处理
	//			callMethod(stream);
	//		} else {
	//			// 不能确定的操作，退出操作！
	//			Logger.error(this, "subprocess", "invalid! %s", mark);
	//			exited = true;
	//			// 通知对方!
	//			invalid(Answer.UNSUPPORT);
	//		}
	//	}

}


///**
// * 返回当前服务器的安全选项(地址验证、密文验证、地址/密文复合验证)
// * @throws SecureException, IOException
// */
//private void notifySecure() throws SecureException, IOException {
//	Address client = remote.getAddress();
//	ServerToken token = ServerTokenManager.getInstance().find(client);
//	if (token == null) {
//		throw new SecureException(
//				"cannot be find server-token by '%s', please check local.xml", client);
//	}
//	int family = token.getFamily();
//
//	Mark cmd = new Mark(Answer.SECURE_NOTIFY);
//	Stream resp = new Stream(cmd);
//	resp.addMessage(MessageKey.SECURE_FAMILY, family);
//
//	//		resp.addMessage(MessageKey.SECURE_FAMILY, FixpMonitor.skey.getFamily());
//	send(resp);
//}

///**
// * 返回当前服务器的安全选项(地址验证、密文验证、地址/密文复合验证)
// * @throws SecureException, IOException
// */
//private void notifySecure() throws SecureException, IOException {
//	Address client = remote.getAddress();
//	// 查找客户机密钥
//	PublicSecure token = SecureController.getInstance().findPublicSecure(client);
//	if (token == null) {
//		throw new SecureException(
//				"cannot be find security-token by '%s', please check local.xml", client);
//	}
//
//	Mark cmd = new Mark(Answer.SECURE_NOTIFY);
//	Stream resp = new Stream(cmd);
//	resp.addMessage(MessageKey.SECURE_FAMILY, token.getFamily());
//	resp.setData(token.build());
//
//	//		Logger.debug(this, "notifySecure", "secure type %d - %s, data length:%d", token.getFamily(),
//	//				SecureType.translate(token.getFamily()), resp.getData().length);
//
//	send(resp);
//}


///**
// * 接受客户端要求，执行安全初始化
// * @param request FIXP请求流
// * @return 初始化成功返回真，否则假
// * @throws IOException
// */
//private boolean createSecure(Stream request) throws IOException {
//	byte[] data = request.getData();
//	Cipher cipher = new Cipher();
//
//	Address client = remote.getAddress();
//	ServerToken token = ServerTokenManager.getInstance().find(client);
//	cipher.decase(token.getKey(), data, 0, data.length);
//
//	//		cipher.resolve(FixpMonitor.skey.getPrivateKey(), data, 0, data.length);
//
//	short code = Answer.SECURE_ACCEPTED;
//	Mark cmd = new Mark(code);
//	Stream resp = new Stream(cmd);
//	resp.addMessage(MessageKey.SPEAK, "secure okay");
//	// 发送数据流
//	send(resp);
//	// 给输入/输出设置密文
//	receiver.setCipher(cipher);
//	sender.setCipher(cipher);
//
//	return true;
//}

