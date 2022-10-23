/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.client;

import java.io.*;
import java.net.*;

import com.laxcus.log.client.*;
import com.laxcus.tub.*;
import com.laxcus.util.net.*;

/**
 * 边缘计算数据流模式客户端。<br>
 * 安全通信的密文同时放到FIXP协议的输入/输出流中。<br>
 * 
 * @author scott.liang
 * @version 1.1 3/16/2009
 * @see java.net.Socket
 * @since laxcus 1.0
 */
public class TubStreamClient extends TubRootClient {

	/** SOCKET句柄 */
	private Socket socket;

	/** SOCKET数据读取 **/
	private TubInputStream receiver;

	/** SOCKET数据发送 **/
	private TubOutputStream sender;

	/**
	 * 默认数据流客户端构造函数
	 */
	public TubStreamClient() {
		super(SocketTag.TCP);
		socket = null;
	}

	/**
	 * 构造一个实例，并且指定套接字
	 * @param sock Socket实例（java）
	 * @throws IOException
	 */
	public TubStreamClient(Socket sock) throws IOException {
		this();
		socket = sock;
		SocketHost endpoint = new SocketHost(SocketTag.TCP, sock.getInetAddress(), sock.getPort());
		setRemote(endpoint);
		receiver = new TubInputStream(sock.getInputStream());
		sender = new TubOutputStream(sock.getOutputStream());
	}

//	/**
//	 * 设置FIXP密文
//	 * @param e Cipher实例
//	 */
//	public void setCipher(Cipher e) {
//		if (receiver != null) {
//			receiver.setCipher(e);
//		}
//		if (sender != null) {
//			sender.setCipher(e);
//		}
//	}

//	/**
//	 * 判断是执行了安全通信
//	 * @return 返回真或者假
//	 */
//	public boolean isSecured() {
//		return receiver.isSecured() || sender.isSecured();
//	}

	/**
	 * 返回本地SOCKET地址
	 * @return SocketHost实例
	 */
	public SocketHost getLocal() {
		return new SocketHost(SocketTag.TCP, socket.getLocalAddress(), socket.getLocalPort());
	}

	/**
	 * 判断已经绑定本地地址
	 * @return 返回真或者假
	 */
	public boolean isBound() {
		return (socket != null && socket.isBound());
	}

	/**
	 * 判断是处于连接状态
	 *
	 * @return 返回真或者假
	 */
	public boolean isConnected() {
		return (socket != null && socket.isConnected());
	}

	/**
	 * 判断是处于关闭状态
	 * 
	 * @return 返回真或者假
	 */
	public boolean isClosed() {
		return (socket == null || socket.isClosed());
	}

	/**
	 * 返回FIXP服务器地址
	 * 
	 * @return InetAddress实例
	 */
	public InetAddress getRemoteAddress() {
		if (socket == null) return null;
		return socket.getInetAddress();
	}

	/**
	 * 返回对等方的端口号地址
	 * 
	 * @return 目标站点的端口号
	 */
	public int getRemotePort() {
		return socket == null ? -1 : socket.getPort();
	}

	/**
	 * 返回SOCKET绑定的本地地址
	 * 
	 * @return InetAddress实例
	 */
	public InetAddress getLocalAddress() {
		if (socket == null) {
			return null;
		}
		return socket.getLocalAddress();
	}

	/**
	 * 返回SOCKET绑定的本地端口号
	 * 
	 * @return 本地端口号
	 */
	public int getLocalPort() {
		return socket == null ? -1 : socket.getLocalPort();
	}

	/**
	 * 优雅关闭套接字。<br>
	 * 即判断套接字处于连接的状态下，向服务器发送“通信结束”报文，再关闭套接字。
	 * @param exit “exit”指令优雅关闭！
	 */
	public final void close(boolean exit) {
		// 如果套接字已经置空，忽略
		if (isClosed()) {
			return;
		}

		// 通知服务器结束会话
		try {
			if (isConnected() && exit) {
				exit();
			}
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 判断套接字处于半读状态（read-half），关闭输入流
		try {
			if (socket.isInputShutdown()) {
				socket.shutdownInput();
			}
		} catch (IOException e) {
			Logger.error(e);
		}

		// 判断套接字处于半写状态（write-half），关闭输出流
		try {
			if (socket.isOutputShutdown()) {
				socket.shutdownOutput();
			}
		} catch (IOException e) {
			Logger.error(e);
		}

		// 读关闭
		try {
			if (receiver != null) {
				receiver.close();
			}
		} catch (IOException e) {
			Logger.error(e);
		} finally {
			receiver = null;
		}

		// 写关闭
		try {
			if (sender != null) {
				sender.close();
			}
		} catch (IOException e) {
			Logger.error(e);
		} finally {
			sender = null;
		}

		// 关闭SOCKET
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
	 * 优雅关闭套接字。<br>
	 * 即判断套接字处于连接的状态下，向服务器发送“exit”指令（通信结束报文），再关闭套接字。
	 */
	public final void close() {
		close(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.client.TubClient#destroy()
	 */
	@Override
	public void destroy() {
		// 关闭套接字
		close();
		// 释放上级资源
		super.destroy();
	}

	//	/**
	//	 * 优雅关闭套接字。即判断套接字处于连接的状态下，向服务器发送“通信结束”报文，再关闭套接字。
	//	 */
	//	public final void close() {
	//		// 通知服务器结束会话
	//		try {
	//			if (isConnected()) {
	//				exit();
	//			}
	//		} catch (IOException e) {
	//			Logger.error(e);
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		}
	//		// 在会话结束关闭SOCKET
	//		destroy();
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see com.laxcus.fixp.client.TubClient#destroy()
	//	 */
	//	@Override
	//	public void destroy() {
	//		// 释放上级资源
	//		super.destroy();
	//
	//		// 以下是清除本地资源
	//		if (socket == null) {
	//			return;
	//		}
	//
	//		// 关闭SOCKET
	//		try {
	//			socket.shutdownInput();
	//			socket.shutdownOutput();
	//			receiver.close();
	//			sender.close();
	//			socket.close();
	//		} catch (IOException e) {
	//			Logger.error(e);
	//		} catch (Throwable e) {
	//			Logger.fatal(e);
	//		} finally {
	//			receiver = null;
	//			sender = null;
	//			socket = null;
	//		}
	//	}

	/**
	 * 重新连接到原来的服务器（先关闭再连接）
	 * @throws IOException
	 */
	public void reconnect() throws IOException {
		close();
		connect(remote);
	}

	/**
	 * 连接FIXP TCP服务器
	 * @param endpoint FIXP TCP服务器监听地址
	 * @throws IOException
	 */
	private void connect(SocketAddress endpoint) throws IOException {
		// 如果处于连接状态，先关闭它
		if (isConnected() || isBound()) {
			close();
		}

		socket = new Socket();
		// 设置SOCKET参数(接收/发送缓存、接收超时)
		if (receiveBufferSize > 0) {
			socket.setReceiveBufferSize(receiveBufferSize);
		}
		if (sendBufferSize > 0) {
			socket.setSendBufferSize(sendBufferSize);
		}
		// 接收超时
		if (receiveTimeout > 0) {
			socket.setSoTimeout(receiveTimeout);
		}

		// 绑定通配符地址
		InetSocketAddress local = new InetSocketAddress(new Address().getInetAddress(), 0);
		socket.bind(local);

		// 打开SO_LINGER，实现套接字的优雅关闭。优雅关闭最大延时5秒。
		socket.setSoLinger(true, 5);

		// 连接服务器
		socket.connect(endpoint, connectTimeout);
		// 取得输入/输出流句柄
		receiver = new TubInputStream(socket.getInputStream());
		sender = new TubOutputStream(socket.getOutputStream());

//		// 查询服务器是否要求密文校验
//		int secure = askSecure();
//		// 确定是密文校验时
//		if (SecureType.isCipher(secure)) {
//			createSecure(null);
//		}
	}

	/**
	 * 连接FIXP服务器
	 * @param endpoint FIXP TCP服务器监听地址
	 * @throws IOException
	 */
	public void connect(SocketHost endpoint) throws IOException {
		// 连接服务器
		connect(endpoint.getSocketAddress());
		// 保存地址
		setRemote(endpoint);
	}

	/**
	 * 连接FIXP服务器
	 * @param inet TCP FIXP服务器IP地址
	 * @param port TCP FIXP服务器监听端口
	 * @throws IOException
	 */
	public void connect(InetAddress inet, int port) throws IOException {
		if (inet == null || port < 1) {
			throw new IllegalArgumentException("illegal address or port! " + inet + ", " + port);
		}
		// 连接服务器
		SocketHost endpoint = new SocketHost(SocketTag.TCP, inet, port);
		connect(endpoint);
	}

//	/**
//	 * 向服务器查询是否要求密文验证
//	 * 
//	 * @return 返回安全属性，不能确定返回-1。
//	 * @throws SecureException
//	 * @throws IOException
//	 */
//	private int askSecure() throws SecureException, IOException {
//		if (isSecured()) {
//			throw new SecureException("secure existed!");
//		}
//
//		Mind mark = new Mind(Ask.NOTIFY, Ask.SECURE_QUERY);
//		Stream request = new Stream(getRemote(), mark);
//		// 发送和接收数据
//		TubStream resp = swap(request, false);
//		// 分析报文，返回结果
//		mark = resp.getMind();
//		if (mark.getAnswer() == Answer.SECURE_NOTIFY) {
//			return resp.findInteger(MessageKey.SECURE_FAMILY);
//		}
//		// 不能确定
//		return -1;
//	}

//	/**
//	 * 建立密文通信服务。<br><br>
//	 * 
//	 * 指定FIXP密文，向服务器投递，要求服务器保存，启动双方的安全通信。<br>
//	 * 此后数据通信将在加密状态下操作，即客户机加密，服务器解密。<br>
//	 * 
//	 * @param cipher FIXP密文
//	 * @return 初始化成功返回真，否则假
//	 * @throws SecureException
//	 * @throws IOException
//	 */
//	private boolean createSecure(Cipher cipher) throws SecureException, IOException {
//		// 如果已经通过密文校验，再次校验是错误
//		if (isSecured()) {
//			throw new SecureException("secure existed!");
//		}
//		// 如果密文未定义，生成一个新的
//		if (cipher == null) {
//			cipher = super.createCipher();
//		}
//
//		// 根据连接的服务器地址，找到关联的RSA公钥令牌，然后对密文进行RSA加密。
//		Address server = getRemote().getAddress();
//		ClientToken token =	ClientTokenManager.getInstance().find(server);
//		// 没有找到是配置错误（至少有一个默认的令牌）
//		if(token == null) {
//			throw new SecureException(
//					"cannot be find client-token by '%s', please check local.xml", server);
//		}
//		byte[] b = cipher.encase(token.getKey());
//
//		//		// 生成密文信息
//		//		byte[] b = cipher.build(TubClient.getRSAKey());
//
//		// 生成流
//		Mind cmd = new Mind(Ask.NOTIFY, Ask.SECURE_CREATE);
//		Stream request = new Stream(getRemote(), cmd);
//		request.setData(b);
//
//		// 发送安全初始化数据和接收数据流
//		TubStream resp = swap(request, false);
//
//		// 判断结果
//		int code = resp.getMind().getAnswer();
//		boolean success = (code == Answer.SECURE_ACCEPTED);
//		// 如果成功，保存密文配置
//		if (success) {
//			setCipher(cipher);
//		}
//
//		Logger.note(this, "createSecure", success, "send to %s, reply code:%d", getRemote(), code);
//		return success;
//	}

	/**
	 * 通知服务器，结束会话，并且释放资源
	 * @return 释放成功返回真，否则假
	 * @throws IOException
	 */
	private boolean exit() throws IOException {
		Mind cmd = new Mind(MindAsk.NOTIFY, MindAsk.EXIT);
		TubStream request = new TubStream(getRemote(), cmd);
		TubStream resp = swap(request, false);
		cmd = resp.getMind();

//		Logger.debug(this, "exit", "reply code is %d", cmd.getCode());

		// 判断是“再见”命令
		return MindAssert.isGoodbye(cmd);
		// return cmd.getResponse() == Answer.OKAY;
	}

	/**
	 * 发送数据流到服务器
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效数据长度
	 * @throws IOException
	 */
	public void send(byte[] b, int off, int len) throws IOException {
		sender.write(b, off, len);
		sender.flush();

		// 统计发送的数据流量
		addSendFlowSize(len);
	}

	/**
	 * 发送数据流到FIXP TCP服务器
	 * 
	 * @param request 请求流
	 * @throws IOException
	 */
	private void send(TubStream request) throws IOException {
		// 发送数据，如果要求加密，在输出流中执行
		byte[] b = request.build();

		// 以10K为单位，持续发送数据(这是做当有加密要求时，可以分段加密发送)
		final int size = 10240;
		for (int seek = 0; seek < b.length;) {
			// 本次发送数据单位
			int len = (b.length - seek > size ? size : b.length - seek);
			// 输出数据并且统计
			send(b, seek, len);
			seek += len;
		}
	}

	/**
	 * 从FIXP服务器接收数据
	 * @param readBody 读数据域
	 * @return 返回封装后的Stream实例
	 * @throws IOException
	 */
	public TubStream receive(boolean readBody) throws IOException {
		//		// 接收数据，如果有加密，在输入流中解密
		//		Stream resp = new Stream(getRemote());
		//		long flow = resp.read(receiver, sender, readBody);

		// 接收数据，如果有加密，在输入流中解密
		TubStream resp = new TubStream(getRemote(), receiver, sender);
		long flows = resp.read(readBody);

		// 统计接收的数据流量
		addReceiveFlowSize(flows);

		return resp;
	}

	/**
	 * 发送和接收数据
	 * @param request 请求数据流
	 * @param readBody 读数据域
	 * @return 返回接收的Stream实例
	 * @throws IOException
	 */
	public TubStream swap(TubStream request, boolean readBody) throws IOException {
		// 检查连接
		if (isClosed()) {
			throw new IOException("socket closed!");
		}
		// 发送和接收
		send(request);
		return receive(readBody);
	}

	/**
	 * 向目标FIXP流服务器发送一个测试，检查与服务器的连通，和双方加密参数。<br>
	 * 外部操作流程：<br>
	 * 1. 调用“connect”方法，连接FIXP服务器。<br>
	 * 2. 调用“test”方法，进行测试。<br>
	 * 3. 调用“close”方法，close方法先调用“exit”通知FIXP流服务器关闭，再关闭本地socket tcp连接。<br>
	 * 
	 * @return 成功返回真，否则假
	 * @throws IOException
	 */
	public boolean test() throws IOException {
		Mind mark = new Mind(MindAsk.NOTIFY, MindAsk.TEST);
		TubStream request = new TubStream(getRemote(), mark);
		request.addMessage(SliceKey.SPEAK, "TEST!");

		TubStream resp = swap(request, true);
		// 判断应答结果
		return MindAnswer.isAccept(resp.getMind());
	}

}