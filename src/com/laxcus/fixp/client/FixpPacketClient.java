/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.client;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import com.laxcus.fixp.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.log.client.*;
import com.laxcus.security.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * FIXP协议数据报客户端。<br>
 * FIXP客户端有两种模式：UDP和KEEP UDP。
 * KEEP UDP综合了TCP持续传输和UDP对系统资源占用少的特点，可以批量发送和接收数据。<br>
 * 数据报的安全规则与数据流不同。如果在连接情况下，即Socket.isConnected，套接字只有一个密文；
 * 如果没有连接，因为存在向服务器发送数据包的可能，允许有多个密文存在。<br><br>
 * 
 * FIXP子包尺寸设计：<br>
 * 核心要求是减少TCP/IP堆栈处理UDP包的频率（FIXP包数据域尺寸过小，拆分数据会造成子包数量过多，
 * TCP/IP发送/接收的处理效率下降），同时要保证小于UDP MTU尺寸，考虑FIXP包头的余量（默认100字节），FIXP子包的最大尺寸是65407。<br>
 * 
 * <br><br>
 * 
 * 另一种FIXP子包尺寸设计：<br>
 * FIXP子包数据块长度设计：以INTERNET的IP MTU的576字节为基础，可保证网络传输中不发生拆包。
 * 考虑FIXP包头在60字节余量（不同类型的包头长度不一样），FIXP数据块最佳限制为512字节，
 * 保证不超过IP MTU的576字节的长度限制。<br><br>
 * 
 * 特别注意！！！"bind" / "connect"两个方法是互斥的，如果使用"bind"方法，就不要使用"connect"；反之亦然！！！<br><br>
 * 
 * @author scott.liang
 * @version 1.2 12/3/2012
 * @since laxcus 1.0
 */
public class FixpPacketClient extends FixpClient {

	/** UDP数据包最大传输尺寸 **/
	private final static int MTU = 65507;

	/** FIXP UDP子包超时 **/
	private static int defaultSubPacketTimeout = 10000;

	/** 子包数据域长度(加上FIXP包头，总计应小于IP MTU)，避免传输过程中拆包 **/
	private static int defaultSubPacketSize = 480;

	/**
	 * 设置FIXP UDP子包接收超时
	 * @param ms 为毫秒为单位的超时时间
	 * @return 返回设置的FIXP UDP子包接收超时
	 */
	public static int setDefaultSubPacketTimeout(int ms) {
		if (ms >= 3000) {
			FixpPacketClient.defaultSubPacketTimeout = ms;
		}
		return FixpPacketClient.defaultSubPacketTimeout;
	}

	/**
	 * 返回FIXP UDP子包接收超时
	 * @return FIXP UDP子包接收超时时间
	 */
	public static int getDefaultSubPacketTimeout() {
		return FixpPacketClient.defaultSubPacketTimeout;
	}

	/**
	 * 设置FIXP UDP子包数据域尺寸。可在配置文件local.xml中定义，尽量全网一致。<br>
	 * 子包数据域尺寸选择在128 到 65507 - 100字节之间。65507是UDP包最大长度，100字节是FIXP包头余量。<br>
	 * （除重传的FIXP数据包，一般FIXP包头不会超过100字节，而重传FIXP数据包没有数据域）。<br><br>
	 * 
	 * 数据传输过程，每个子包的数据长度不能超过这个尺寸。
	 * 
	 * @param len 数据包最大长度
	 * @return 返回设置的FIXP UDP子包长度
	 */
	public static int setDefaultSubPacketSize(int len) {
		if (len >= 128 && len <= MTU - 100) {
			FixpPacketClient.defaultSubPacketSize = len;
		}
		return FixpPacketClient.defaultSubPacketSize;
	}

	/**
	 * 返回FIXP UDP子包数据长度
	 * @return FIXP UDP子包长度
	 */
	public static int getDefaultSubPacketSize() {
		return FixpPacketClient.defaultSubPacketSize;
	}

	/** UDP套接字 */
	private DatagramSocket socket;

	/** FIXP数据包标识号，从1开始 */
	private int packetIdentity;

	/** 子包超时时间，默认是10秒 */
	private int subPacketTimeout;

	/** 分别针对多个地址的密文集合 **/
	private Map<SocketHost, Cipher> ciphers = new TreeMap<SocketHost, Cipher>();

	/** UDP包发送次数 **/
	private int sendPackets;

	/** UDP包接收次数 **/
	private int receivePackets;

	/**
	 * 默认数据包客户端构造函数
	 */
	public FixpPacketClient() {
		super(SocketTag.UDP);
		// 初始化IO统计参数
		resetIO();

		// 初始化包编号（用于子包），从1开始。
		packetIdentity = 1;
		// 子包超时：10秒
		setSubPacketTimeout(FixpPacketClient.getDefaultSubPacketTimeout());
	}

	/**
	 * IO数据包统计值初始化为0
	 */
	private void resetIO() {
		sendPackets = 0;
		receivePackets = 0;
	}

	/**
	 * 返回UDP数据包发送次数
	 * @return 整数
	 */
	public int getSendPackets() {
		return sendPackets;
	}

	/**
	 * 返回UDP数据包接收次数
	 * @return 整数
	 */
	public int getReceivePackets() {
		return receivePackets;
	}

	/**
	 * 保存一个目标地址和FIXP密文
	 * @param endpoint 目标站点
	 * @param cipher FIXP通信密文
	 * @return 保存成功返回真，否则假
	 */
	private boolean addCipher(SocketHost endpoint, Cipher cipher) {
		SocketHost e = endpoint.duplicate();
		return ciphers.put(e, cipher.duplicate()) == null;
	}

	/**
	 * 根据目标地址查找FIXP密文
	 * @param endpoint 目标地址
	 * @return FIXP通信密文
	 */
	private Cipher findCipher(SocketHost endpoint) {
		Cipher cipher = ciphers.get(endpoint);
		// 刷新使用时间
		if(cipher != null) {
			cipher.refresh();
		}
		return cipher;
	}

	/**
	 * 根据目标地址，检查执行了安全通信
	 * @param endpoint 目标地址
	 * @return 返回真或者假
	 */
	public boolean isSecured(SocketHost endpoint) {
		return ciphers.size() > 0 && ciphers.get(endpoint) != null;
	}

	/**
	 * 根据绑定的目标地址，检查执行了安全通信
	 * @return 返回真或者假
	 */
	public boolean isSecured() {
		return isSecured(getRemote());
	}

	/**
	 * 返回下一个子包标识
	 * @return 整型值的子包标识
	 */
	private int nextPacketIdentify() {
		// 循环取值
		if (packetIdentity >= Integer.MAX_VALUE) {
			packetIdentity = 1;
		}
		return packetIdentity++;
	}

	/**
	 * 设置子包超时时间，单位：毫秒(子包超时时间小于包超时时间)
	 * @param ms 子超超时时间
	 */
	public void setSubPacketTimeout(int ms) {
		if (ms >= 3000) {
			subPacketTimeout = ms;
		}
	}

	/**
	 * 返回子包超时时间，单位：毫秒
	 * @return 子超超时时间
	 */
	public int getSubPacketTimeout() {
		return subPacketTimeout;
	}

	/**
	 * 判断是处于绑定状态
	 * @return 返回真或者假
	 */
	public boolean isBound() {
		return (socket != null && socket.isBound());
	}

	/**
	 * 判断是处于连接状态
	 * @return 返回真或者假
	 */
	public boolean isConnected() {
		return (socket != null && socket.isConnected());
	}

	/**
	 * 判断是关闭
	 * @return 返回真或者假
	 */
	public boolean isClosed() {
		return (socket == null || socket.isClosed());
	}

	/**
	 * 返回当前SOCKET地址
	 * @return SocketHost句柄
	 */
	public SocketHost getLocal() {
		if (socket == null) {
			return null;
		}
		return new SocketHost(SocketTag.UDP, socket.getLocalAddress(), socket.getLocalPort());
	}

	/**
	 * 返回本地站点地址
	 * @return InetAddress实例
	 */
	public InetAddress getLocalAddress() {
		if (socket == null) {
			return null;
		}
		return socket.getLocalAddress();
	}

	/**
	 * 返回目标站点地址
	 * @return InetAddress实例
	 */
	public InetAddress getRemoteAddress() {
		if (socket == null) {
			return null;
		}
		return socket.getInetAddress();
	}

	/**
	 * 关闭套接字，同时通知服务器释放客户端密钥。<br>
	 * 判断是绑定和连接的状态，并且发送/接收数据包后，向服务器发送“exit”指令（结束通信报文），再关闭套接字；否则直接关闭套接字。<br>
	 */
	public final void close() {
		// 套接字已经释放，返回
		if (isClosed()) {
			return;
		}

		// 如果已经绑定或者连接，且发生了IO操作时，执行“exit”，通知服务端释放密钥
		boolean exit = (isBound() || isConnected()) && (sendPackets > 0 || receivePackets > 0);
		if (exit) {
			try {
				exit();
			} catch (IOException e) {
				Logger.error(e);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
		}

		// 关闭套接字
		try {
			socket.close();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			socket = null;
		}
		// 释放全部密文信息
		ciphers.clear();
		// 重置IO
		resetIO();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.client.FixpClient#destroy()
	 */
	@Override
	public void destroy() {
		// 关闭套接字
		close();
		// 释放上级资源
		super.destroy();
	}

	/**
	 * 建立UDP套接字，绑定本地地址。如果地址是空指针，交给系统随机分配和绑定。
	 * 
	 * @param local 本地套接字地址
	 * @return 绑定成功返回真，否则假
	 * @throws IOException
	 */
	public boolean bind(SocketAddress local) throws IOException {
		// 如果没有指定本地IP，绑定通配符地址，端口由系统随机分配
		if (local == null) {
			local = new InetSocketAddress(new Address().getInetAddress(), 0);
		}

		// 绑定本地IP地址
		socket = new DatagramSocket(null);
		socket.bind(local);

		// 设置接收缓冲区尺寸
		if (receiveBufferSize > 0) {
			socket.setReceiveBufferSize(receiveBufferSize);
		}
		// 设置发送缓冲区尺寸
		if (sendBufferSize > 0) {
			socket.setSendBufferSize(sendBufferSize);
		}

		// 设置SOCKET超时时间
		resetDefaultSoTimeout();
		return true;
	}

	/**
	 * 随机指定一个本地SOCKET地址并且绑定
	 * @return 绑定成功返回真，否则假
	 * @throws IOException
	 */
	public boolean bind() throws IOException {
		SocketAddress local = null;
		return bind(local);
	}

	/**
	 * 连接到FIXP服务器
	 * @param remote UDP FIXP服务器监听地址
	 * @throws IOException
	 */
	public void connect(SocketHost remote) throws IOException {
		// 判断远程目标主机有效
		if (!remote.isValid()) {
			throw new SocketException("illegal host " + remote.toString());
		}

		// 如果已经连接，先关闭它
		if (isConnected() || isBound()) {
			close();
		}
		// 绑定本地地址
		boolean success = bind();
		if (!success) {
			throw new BindException("bind failed!");
		}

		// 设置接收超时
		resetDefaultSoTimeout();
		// 连接到服务器
		SocketAddress address = remote.getSocketAddress();
		socket.connect(address);

		// 保存服务器地址
		setRemote(remote);
	}

	/**
	 * 连接到FIXP服务器
	 * @param inet FIXP服务器IP地址
	 * @param port FIXP服务监听端口
	 * @throws IOException
	 */
	public void connect(InetAddress inet, int port) throws IOException {
		if (inet == null || port < 1) {
			throw new IllegalArgumentException("illegal address or port! " + inet + ", " + port);
		}

		// 连接服务器
		SocketHost endpoint = new SocketHost(SocketTag.UDP, inet, port);
		connect(endpoint);
	}

	/**
	 * 接收FIXP数据，返回FIXP包
	 * @return Packet实例
	 * @throws IOException
	 */
	public Packet receive() throws FixpProtocolException, SecureException, IOException {
		// 如果关闭，弹出异常
		if (isClosed()) {
			throw new SocketException("socket closed!");
		}
		// 从服务器接收数据
		byte[] buff = new byte[MTU];
		DatagramPacket gram = new DatagramPacket(buff, 0, buff.length);
		socket.receive(gram);

		// 接收包统计值自增1
		receivePackets++;

		// 来源地址
		SocketHost from = new SocketHost(SocketTag.UDP, gram.getAddress(), gram.getPort());
		// 接收UDP数据包
		byte[] data = gram.getData();
		int off = gram.getOffset();
		int len = gram.getLength();
		if (len < 1) {
			throw new IOException("empty datagram!");
		}

		// 统计接收的数据流量
		addReceiveFlowSize(len);

		// 取实际数据
		byte[] b = Arrays.copyOfRange(data, off, off + len);
		// 如果有密文先解密
		Cipher cipher = findCipher(from);
		if (cipher != null) {
			b = cipher.decrypt(b, 0, b.length);
		}
		// 解析数据包并且返回
		return new Packet(from, b, 0, b.length);
	}

	/**
	 * 发送FIXP数据包
	 * @param packet FIXP包
	 * @throws IOException
	 */
	public void send(Packet packet) throws SecureException, IOException {
		// 如果关闭，弹出异常
		if (isClosed()) {
			throw new SocketException("socket closed!");
		}

		SocketHost remote = packet.getRemote();
		// 数据包转换为数据流
		byte[] b = packet.build();
		// 加密
		Cipher cipher = findCipher(remote);
		if (cipher != null) {
			b = cipher.encrypt(b, 0, b.length);
		}

		//		Logger.debug(this, "send", "当前 %s 发送数据包到 %s", getSocket(), remote);

		DatagramPacket udp = new DatagramPacket(b, 0, b.length);
		udp.setSocketAddress(remote.getSocketAddress());
		socket.send(udp);

		// 统计发送的数据流量
		addSendFlowSize(b.length);

		// 统计值自增1
		sendPackets++;
	}

	/**
	 * 发送数据包，不等待回答
	 * @param packet FIXP包
	 * @throws IOException
	 */
	public void notice(Packet packet) throws IOException {
		send(packet);
	}

	/**
	 * 设置SOCKET超时，单位：毫秒
	 * @param ms 超时时间
	 * @throws SocketException
	 */
	public void setSoTimeout(int ms) throws SocketException {
		if (socket != null) {
			socket.setSoTimeout(ms);
		}
	}

	/**
	 * 重置子包超时时间
	 * @throws SocketException
	 */
	public void resetSubPacketSoTimeout() throws SocketException {
		// 设置子包接收超时
		setSoTimeout(subPacketTimeout);
	}

	/**
	 * 根据接收等待时间，重置SOCKET超时时间
	 * @throws SocketException
	 */
	public void resetDefaultSoTimeout() throws SocketException {
		// 接收超时时间，0是无限制超时时间
		int ms = (receiveTimeout > 0 ? receiveTimeout : 0);
		setSoTimeout(ms);
	}

	/**
	 * 撤销密文。<br><br>
	 * 
	 * 此操作建立在“已经建立安全通信”的基础上，即已经使用“createSecure”加密了数据。<br>
	 * 并且要求客户机主机和密文主机来自一个IP地址，服务器将对此做验证。<br>
	 * 
	 * @param remote FIXP服务器地址
	 * @param host 已经记录在服务器的主机地址
	 * @param cipher 已经记录在服务器的密文
	 * @return 成功返回真，否则假。
	 * 
	 * @throws SecureException
	 * @throws IOException
	 */
	public boolean dropSecure(SocketHost remote, SocketHost host, Cipher cipher)
			throws SecureException, IOException {
		// 如果没有指定目标站点地址，判断已经连接，取出目标站点地址
		if (remote == null) {
			if (isConnected()) remote = getRemote();
		}
		// 判断目标站点地址
		if (remote == null) {
			throw new IOException("must be set server address!");
		}
		// 必须已经执行了安全通信，否则是错误
		if (!isSecured(remote)) {
			throw new SecureException("must be RSA ENCRYPT!");
		}

		// 判断参数是空指针
		Laxkit.nullabled(host);
		Laxkit.nullabled(cipher);

		// 可类化数据
		ClassWriter writer = new ClassWriter();
		writer.writeObject(host);
		writer.writeObject(cipher);
		byte[] b = writer.effuse();

		// 撤销加密安全数据包
		Mark mark = new Mark(Ask.NOTIFY, Ask.SECURE_DROP);
		Packet packet = new Packet(remote, mark);
		packet.setData(b);
		// 发送和接受数据包(不要调用swap方法)
		send(packet);
		Packet resp = receive();

		// 判断是否接受
		int code = resp.getMark().getAnswer();
		// 删除密文，返回接受/被拒绝，都是正确
		boolean success = (code == Answer.SECURE_ACCEPTED || code == Answer.SECURE_REFUSE);

		Logger.note(this, "dropSecure", success, "%s TO %s, cipher host:%s, cipher family:%s", 
				getLocal(), remote, host, cipher.getFamilyText());

		return success;
	}
	
	/**
	 * 建立密文通信服务。<br><br>
	 * 
	 * 向服务器投递一个FIXP密文，要求服务器保存，启动安全通信。<br>
	 * 此后数据通信将在加密状态下操作，即客户机加密，服务器解密。加解密由FIXP密文完成。<br>
	 * 
	 * @param remote 目标站点地址
	 * @param cipher FIXP密文
	 * @return 启动返回真，否则假
	 * @throws SecureException
	 * @throws IOException
	 */
	public boolean createSecure(SocketHost remote, Cipher cipher) throws SecureException, IOException {
		// 安全初始化前，不能保存有对称密钥
		if (isSecured(remote)) {
			throw new SecureException("cipher existed!");
		}
		if (remote == null) {
			throw new IOException("unknown remote address!");
		}
		// 如果没有定义，生成一个
		if (cipher == null) {
			cipher = createCipher();
		}

		// 从代理中找到服务器提供的RSA公钥，加密对称密钥。正常情况下，这个地址对应的公钥已经保存，没有肯定是错误！
		ClientSecure token = ClientSecureTrustor.getInstance().findSiteSecure(remote);
		if (token == null) {
			throw new SecureException("cannot be find public key! %s", remote);
		}
		// 加密对称密钥
		byte[] data = cipher.encase(token.getKey());

		// 通常服务器上的RSA私钥解密都很耗时，所以接收等待时间要延长。保证最少60秒
		int ms = (receiveTimeout > 60000 ? receiveTimeout : 60000);
		setSoTimeout(ms);

		// 建立加密初始化数据包
		Mark cmd = new Mark(Ask.NOTIFY, Ask.SECURE_CREATE);
		Packet packet = new Packet(remote, cmd);
		// 写入数据
		packet.setData(data);
		// 发送和接受数据包(不要调用swap方法)
		send(packet);
		
		// 接收数据包
		boolean success = false;
		while (true) {
			Packet resp = receive();

			// 取应答码
			int code = resp.getMark().getAnswer();
			// 如果是"SECURE_NOTIFY"，这是“askSecure”命令多次发送后，FixpPacketHelper返回的冗余，忽略它！
			if (code == Answer.SECURE_NOTIFY) {
				continue;
			}

			// 判断是安全接受！
			success = (code == Answer.SECURE_ACCEPTED);
			// 如果成功，保存这个对称密钥
			if (success) {
				addCipher(remote, cipher);
			} else {
				Logger.error(this, "createSecure", "encrypt failed! %s to %s, secure code: %d",
						getLocal(), remote, code);
			}
			break;
		}

		// 恢复为原来的接收时间
		resetDefaultSoTimeout();

		return success;
	}
	
	/**
	 * 建立密文通信服务。<br><br>
	 * 
	 * 向服务器投递一个FIXP密文，要求服务器保存，启动安全通信。<br>
	 * 此后数据通信将在加密状态下操作，即客户机加密，服务器解密。<br>
	 * 本处FIXP密文是随机产生。<br>
	 * 
	 * @param remote 目标站点地址
	 * @throws SecureException
	 * @throws IOException
	 */
	public boolean createSecure(SocketHost remote) throws SecureException, IOException {
		return createSecure(remote, null);
	}

	/**
	 * 建立密文通信服务。<br><br>
	 * 
	 * 指定FIXP密文，向服务器投递，要求服务器保存，启动安全通信。<br>
	 * 此后数据通信将在加密状态下操作，即客户机加密，服务器解密。<br>
	 * 本处使用默认的远程节点地址，这个地址之前已经设置，否则是错误。<br>
	 * 
	 * @param cipher FIXP密文
	 * @throws SecureException
	 * @throws IOException
	 */
	public boolean createSecure(Cipher cipher) throws SecureException, IOException {
		return createSecure(getRemote(), cipher);
	}

	/**
	 * 保存来自服务器的RSA公钥
	 * @param resp 数据包
	 * @return 返回解析的RSA公钥，没有是空指针
	 */
	private PublicSecure savePublicKey(Packet resp) {
		// 保存密钥
		byte[] b = resp.getData();

		// 判断包有效，解析和保存！
		try {
			if (b != null && b.length > 0) {
				SocketHost remote = resp.getRemote().duplicate();
				PublicSecure key = new PublicSecure(b);
				ClientSecureTrustor.getInstance().addSiteSecure(remote, key);
				return key;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		return null;
	}

	/**
	 * 向服务器查询它的安全属性，包括四种：<br>
	 * 1. 无验证要求 <br>
	 * 2. 地址验证 <br>
	 * 3. 密钥验证 <br>
	 * 4. 地址+密钥的复合验证 <br><br>
	 * 
	 * @param remote 目标站点地址
	 * @return 返回安全属性，不能确定返回-1。
	 * @throws SecureException
	 * @throws IOException
	 */
	public int askSecure(SocketHost remote) throws SecureException, IOException {
		// 如果密文存在，不能进行查询
		if (isSecured(remote)) {
			throw new SecureException("secure existed!");
		}

		// 设置子包接收超时
		resetSubPacketSoTimeout();

		// 生成数据包
		Mark mark = new Mark(Ask.NOTIFY, Ask.SECURE_QUERY);
		Packet request = new Packet(remote, mark);
		
		// 发送和接收数据(不要调用swap方法)，3次测试，防止服务器压力过大，出现不能及时反馈的现象！
		for (int index = 0; index < 3; index++) {
			// 发送请求包
			send(request);

			// 接收
			Packet resp = null;
			try {
				resp = receive();
			} catch (SocketTimeoutException e) {
				Logger.error(e, "from %s", remote);
			}

			// 有效！判断并且返回结果
			if (resp != null) {
				mark = resp.getMark();
				if (mark.getAnswer() == Answer.SECURE_NOTIFY) {
					// 重置成默认超时
					resetDefaultSoTimeout();
					
					// 保存来自服务器的RSA公钥
					PublicSecure secure = savePublicKey(resp);
					if (secure != null) {
						return secure.getFamily();
					} else {
						break;
					}
				}
			}
		}

		// 弹出错误！
		throw new SocketTimeoutException("ask secure timeout! " + remote.toString());
	}
	

	/**
	 * 关闭一个连接! 
	 * 尝试发送三次
	 * @param endpoint 目标地址
	 * @return 成功返回真，否则假
	 * @throws IOException
	 */
	private boolean exit(SocketHost endpoint) throws IOException {
		Mark mark = new Mark(Ask.NOTIFY, Ask.EXIT);
		Packet request = new Packet(endpoint, mark);

		// 子包超时时间
		resetSubPacketSoTimeout();

		boolean success = false;
		// 发送三次，不成功退出
		for (int index = 0; index < 3; index++) {
			// 发送数据
			send(request);
			// 接收数据
			Packet resp = null;
			try {
				resp = receive();
			} catch (SocketTimeoutException e) {
				Logger.error(e, "from %s", endpoint);
			}

			// 判断有效，是“再见”应答
			if (resp != null) {
				success = Assert.isGoodbye(resp.getMark());
				break;
			}
		}

		// 默认的超时时间
		resetDefaultSoTimeout();

		// 返回结果
		return success;
	}

	/**
	 * 通知所有保持着连接的节点，结束会话。
	 * 
	 * @return 返回关闭的数目
	 * @throws IOException
	 */
	private int exit() throws IOException {
		TreeSet<SocketHost> array = new TreeSet<SocketHost>();
		// 如果目标地址是有效时，保存它
		SocketHost host = getRemote();
		if (host != null && host.isValid()) {
			array.add(host);
		}

		// 复制其它有安全通信的节点
		array.addAll(ciphers.keySet());

		int count = 0;
		for (SocketHost endpoint : array) {
			boolean success = exit(endpoint);
			// 成功，释放密文，统计值加1
			if (success) {
				ciphers.remove(endpoint);
				count++;
			}

			// Logger.debug(this, "exit", "%s result %d", endpoint,
			// resp.getMark().getAnswer());
		}

		// Logger.debug(this, "exit", "count:%d", count);

		return count;
	}


	/**
	 * 向目标FIXP服务器发送测试。<br>
	 * 操作流程：<br>
	 * 1. 调用“bind”方法，绑定一个本机地址。<br>
	 * 2. 调用“test”方法，进行通信测试。<br>
	 * 3. 调用“close”方法，close先用“exit”方法关闭通话（secure模式），再关闭本地socket udp。<br>
	 * 
	 * @param remote 目标服务器地址
	 * @param secure 安全通信模式。如果是安全通信在启动前将进行密钥交换，否则不启用。
	 * @return 成功服务端记录的主机地址，否则是空指针
	 * @throws IOException
	 */
	public SocketHost test(SocketHost remote, boolean secure) throws IOException {
		Mark mark = new Mark(Ask.NOTIFY, Ask.TEST);
		Packet request = new Packet(remote, mark);
		request.addMessage(MessageKey.SPEAK, "TEST!");
		// 交换数据包
		Packet resp = null;
		if (secure) {
			resp = swap(request);
		} else {
			send(request);
			resp = receive();
			// 如果要加密时，尝试以加密方式进行！
			if (resp.getMark().getAnswer() == Answer.ENCRYPT_CONTENT_NOTIFY) {
				// 保存服务器的RSA公钥
				savePublicKey(resp);
				
				// 依据RSA公钥，生成对称密钥进行通信
				createSecure(request.getRemote());
				send(request);
				resp = receive();
			}
		}
		
		// 检测
		if (resp != null) {
			// 取出地址地址
			if (Answer.isAccept(resp.getMark())) {
				byte[] b = resp.findRaw(MessageKey.HOST);
				if (b != null) {
					return new SocketHost(new ClassReader(b));
				}
			}
		}

		return null;
	}
	
	/**
	 * 向目标FIXP UDP服务器发送检测本地地址命令。
	 * 操作流程：
	 * 1. 采用通配符模式，调用“bind”方法，绑定本地通配符地址。
	 * 2. 调用“reflect”方法，进行通信测试
	 * 3. 调用“close”方法，close内部用“exit”方法关闭通话，再关闭本地socket udp。
	 * 
	 * @param remote 目标FIXP UDP服务器地址。
	 * @return 返回经过FIXP服务器检测后的本机地址，或者空指针
	 * @throws IOException
	 */
	public SocketHost reflect(SocketHost remote) throws IOException {
		Mark mark = new Mark(Ask.NOTIFY, Ask.REFLECT);
		Packet request = new Packet(remote, mark);
		request.addMessage(MessageKey.SPEAK, "reflect!");
		byte[] bytes = remote.build();
		request.addMessage(MessageKey.HOST, bytes);
		// 交换数据包
		Packet resp = swap(request);
		// 判断结果
		if (Answer.isReflectAccepted(resp.getMark())) {
			byte[] b = resp.getData();
			return new SocketHost(new ClassReader(b));
		}
		return null;
	}

	/**
	 * 发送并且接收FIXP数据包
	 * @param packet 发送的FIXP数据包
	 * @return 返回接收的FIXP数据包
	 */
	public Packet swap(Packet packet) throws IOException {
		// 检查SOCKET
		check(packet.getRemote());
		// 设置接收超时，0是无限制
		resetDefaultSoTimeout();
		// 发送数据包
		send(packet);
		// 接收数据包
		Packet resp = receive();

		// 如果服务器要求加密时，用RSA公钥加密一个随机密文
		if (resp.getMark().getAnswer() == Answer.ENCRYPT_CONTENT_NOTIFY) {
			// 保存来自服务器的RSA公钥
			savePublicKey(resp);
			
			// 建立对称密钥的通信
			createSecure(packet.getRemote());
			// 重新发送数据包
			send(packet);
			resp = receive();
		}

		return resp;
	}

	// 以下是KEEP UDP子包方法 //

	/**
	 * 切割数据
	 * @param data 字节数组
	 * @return 返回切割后的字节数组集
	 */
	private List<ByteBuffer> split(byte[] data) {
		// 末端位置
		int end = (data == null ? 0 : data.length);

		ArrayList<ByteBuffer> array = new ArrayList<ByteBuffer>();
		// 判断空值或者某个长度
		if (end == 0) {
			array.add(ByteBuffer.wrap(new byte[0]));
		} else {
			for (int seek = 0; seek < end;) {
				int size = Laxkit.limit(seek, end, FixpPacketClient.defaultSubPacketSize);
				byte[] b = Arrays.copyOfRange(data, seek, seek + size);
				seek += size;
				// 保存
				array.add(ByteBuffer.wrap(b));
			}
		}
		// 输出
		return array;
	}

	/**
	 * 将一个大的UDP数据包，切割成N个子包
	 * @param packet 标准包
	 * @param packetId 包编号
	 * @return 子包数组
	 */
	private Packet[] split(Packet packet, int packetId) {
		// 切割数据
		List<ByteBuffer> buffs = split(packet.getData());
		int blocks = buffs.size();
		// 分包
		Packet[] packets = new Packet[blocks];
		for (int index = 0; index < blocks; index++) {
			// 生成子包，保存消息
			Packet sub = new Packet(packet.getRemote(), packet.getMark());
			sub.addMessage(MessageKey.SUBPACKET_COUNT, blocks);
			sub.addMessage(MessageKey.SUBPACKET_SERIAL, index); // 子包序号，从0开始
			sub.addMessage(MessageKey.PACKET_IDENTIFY, packetId);
			sub.addMessage(MessageKey.SUBPACKET_TIMEOUT, 6000); //subPacketTimeout);
			sub.addMessage(MessageKey.SUBPACKET_DISABLE_TIMEOUT, 60000); //subPacketTimeout * 10);
			sub.addMessages(packet.getMessages()); // 复制全部消息
			// 保存数据
			ByteBuffer buf = buffs.get(index);
			sub.setData(buf.array());
			// 保存子包
			packets[index] = sub;
		}
		// 输出子包
		return packets;
	}

	/**
	 * 发送FIXP UDP数据子包
	 * @param packets 子包数组
	 * @throws IOException
	 */
	private void sendPackets(Packet[] packets) throws IOException {
		for (int i = 0; i < packets.length; i++) {
			send(packets[i]);
		}
	}

	/**
	 * 要求服务器重发部分序号的数据子包
	 * @param remote 服务器地址
	 * @param packetId 数据包编号
	 * @param serials 子包序号，从0开始
	 * @throws IOException
	 */
	private void sendRetryPacket(SocketHost remote, int packetId, List<Integer> serials) throws IOException {
		int end = serials.size();
		for (int seek = 0; seek < end;) {
			// 截取一段子包序号
			int size = Laxkit.limit(seek, end, PacketBucket.RETRY_SERIALS);
			List<Integer> subs = serials.subList(seek, seek + size);
			// 移到下标
			seek += size;

			// 生成数据包，发送它
			Mark mark = new Mark(Ask.NOTIFY, Ask.RETRY_SUBPACKET);
			Packet packet = new Packet(remote, mark);
			packet.addMessage(MessageKey.PACKET_IDENTIFY, packetId);
			for (int index : subs) {
				packet.addMessage(MessageKey.SUBPACKET_SERIAL, index);
			}
			send(packet);
		}
	}

	/**
	 * 通知服务器，释放与本地址和包标识号的连接记录
	 * @param remote 目标地址
	 * @param packetId 包编号
	 * @throws IOException
	 */
	private void cancel(SocketHost remote, int packetId) throws SecureException, IOException {
		Mark mark = new Mark(Ask.NOTIFY, Ask.CANCEL_PACKET);
		Packet request = new Packet(remote, mark);
		request.addMessage(MessageKey.PACKET_IDENTIFY, packetId);

		// 取消操作。如果发生超时，会再次发送包，直到收到结果（取消成功/取消没有找到!）。冗余数据会传导到batch方法里
		boolean skip = false; // 跳过发送
		while (true) {
			// 发送数据包
			if (!skip) {
				send(request);
			}
			skip = false;

			// 等待应答结果
			try {
				Packet resp = receive();
				// 返回两个结果：撤销成功，或者没有找到，都是退出。第三种可能是返回其他包，这些包被忽略！
				mark = resp.getMark();
				// 确认！
				if (mark.getAnswer() == Answer.CANCEL_OKAY) {
					// Logger.debug(this, "cancel", "撤销包，确认成功！");
					break;
				}
				// 撤销
				else if (mark.getAnswer() == Answer.CANCEL_NOTFOUND) {
					// Logger.debug(this, "cancel", "撤销包，没有找到！");
					break;
				}
				// FixpPacketHelper连续发送“SUBPACKETS_ACCEPTED”，本处承接"batch"方法的冗余数据。跳过这个方法，再处理
				else if (Answer.isSubPacketsAccepted(mark)) {
					skip = true;
					//	Logger.debug(this, "cancel", "忽略前面的应答包！SUBPACKETS_ACCEPTED");
				}
				// 其他情况，忽略，再接收！
				else {
					skip = true;
					Logger.warning(this, "cancel", "ignore packet %s, from %s", mark, resp.getRemote());
				}
			} catch (SocketTimeoutException e) {
				Logger.error(e, "cancel packet timeout! local %s, receive from %s", getLocal(), getRemote());
			}
		}

	}

	/**
	 * 删除超时密文
	 */
	private void deleteTimeoutClipher() {
		ArrayList<SocketHost> array = new ArrayList<SocketHost>();
		Iterator<Map.Entry<SocketHost, Cipher>> iterators = ciphers.entrySet().iterator();
		// 判断超时的节点并且保存
		while (iterators.hasNext()) {
			Map.Entry<SocketHost, Cipher> entry = iterators.next();
			if (entry.getValue().isTimeout()) {
				array.add(entry.getKey());
			}
		}
		for (SocketHost e : array) {
			ciphers.remove(e);
		}
	}

	/**
	 * 检查SOCKET状态，判断服务器安全状态和选择是否初始化密文
	 * @param remote
	 * @throws IOException
	 */
	private void check(SocketHost remote) throws SecureException, IOException {
		// 判断远程目标主机有效
		if (!remote.isValid()) {
			throw new SocketException("illegal host " + remote.toString());
		}

		// 1. 检查。两种情况：
		// <1> 主机连接和之前不一致时，关闭重新连接；<2> 已经关闭或者没有绑定，重新绑定
		if (isConnected()) {
			// 连接状态只能有一个连接地址。此时地址必须一致，否则关闭再连接到目标主机
			SocketHost that = super.getRemote();
			if (remote.compareTo(that) != 0) {
				close();
				connect(remote);
			}
		} else if (isClosed() || !isBound()) {
			// 先关联，再绑定！
			close();
			bind();
		}

		// 2. 删除超时密文
		deleteTimeoutClipher();

		// 3. 检查密文，如果存在则返回
		if (isSecured(remote)) {
			return;
		}

		// 4. 询问服务器安全状态
		int secured = askSecure(remote);
		// 如果要求加密时
		if (SecureType.isCipher(secured)) {
			if (!createSecure(remote)) {
				throw new SecureException("create secure, failed!");
			}
		}
	}

	/**
	 * 采取数据分组方式，将一个请求包拆分成多个子包，发送到服务器，然后等待和接收应答子包，合并后再返回。<br>
	 * 
	 * 在终端拆分重组数据包，可以避免网络中继环节（路由器、交换机）多次拆分重组UDP包，从而减少中继压力，提高整形工作效率。
	 * 
	 * @param request FIXP请求包
	 * @return 返回FIXP接收包
	 * @throws IOException
	 */
	public Packet batch(Packet request) throws SecureException, SocketTimeoutException, IOException {		
		// 检查并且发送数据包
		SocketHost remote = request.getRemote();
		// 检查SOCKET和密文
		check(remote);
		// 设置子包接收超时
		//		setSoTimeout(subPacketTimeout);
		resetSubPacketSoTimeout();

		// 生成一个数据包标识号
		final int packetId = nextPacketIdentify();
		// 将一个数据包分割成多个子包，每个子包设置本次标识号
		Packet[] packets = split(request, packetId);
		// 连续发送一批数据包到服务器
		sendPackets(packets);

		// 毫秒为单位的超时统计
		int count = 0;
		// 接收的应答包放在这个篮子里
		PacketBucket bucket = new PacketBucket();

		// 接受反馈结果，确认全部子包已经收到且受理时，退出
		while (!bucket.isFull()) {		
			// 接收应答包
			Packet resp = null;
			try {
				resp = receive();
				count = 0; // 统计值清零
			} catch (SocketTimeoutException e) {
				Logger.error(e, "sub packet timeout! local %s, receive from %s", getLocal(), getRemote());
			}

			if (resp == null) {
				// 在接收超时有效条件下，统计超时！抛出异常！
				if (receiveTimeout > 0) {
					count += subPacketTimeout;
					if (count >= receiveTimeout) {
						throw new SocketTimeoutException("receive packet timeout!");
					}
				}

				// 判断是空集合或者要求服务器重传部分子包
				if (bucket.isEmpty()) {
					Logger.warning(this, "batch", "resend all sub packets! sub packets count is %d", packets.length);
					sendPackets(packets);
				} else {
					Logger.warning(this, "batch", "resend sub packets!");
					// 要求服务器重传子包
					List<Integer> serials = bucket.getMissSerials();
					sendRetryPacket(remote, packetId, serials);
				}
				continue;
			}

			// 检查接收数据包
			Mark mark = resp.getMark();
			// 服务器要求重发子包
			if (Assert.isRetrySubPacket(mark)) {
				// 按照服务器要求，根据子包编号，找匹配子包和发送它
				List<Integer> serials = resp.findIntegers(MessageKey.SUBPACKET_SERIAL);
				for (int index : serials) {
					if (index >= 0 && index < packets.length) {
						send(packets[index]);
					}
				}
			}
			// 是服务器反馈的已经受理全部命令时，退出当前的循环
			else if (Answer.isSubPacketsAccepted(mark)) {
				Integer returnId = resp.findInteger(MessageKey.PACKET_IDENTIFY);
				if (returnId != null && returnId.intValue() == packetId) {
					//					Logger.debug(this, "batch", "收到子包集确定包，退出当前循环");
					break;
				}
			}
			// 保存子包（是否子包在内部判断）
			else {
				bucket.add(resp);
			}
		}

		// 接收反馈的FIXP数据子包，直到完成
		count = 0;
		while (!bucket.isFull()) {
			// 接收应答包
			Packet resp = null;
			try {
				resp = receive();
				count = 0; // 统计值清零
			} catch (SocketTimeoutException e) {
				Logger.error(e, "sub packet timeout! local %s, receive from %s", getLocal(), getRemote());
			}

			if (resp == null) {
				// 在接收超时有效条件下，统计超时！
				if (receiveTimeout > 0) {
					count += subPacketTimeout;
					if (count >= receiveTimeout) {
						throw new SocketTimeoutException("receive packet timeout!");
					}
				}
				continue;
			}

			// 检查接收数据包
			Mark mark = resp.getMark();
			// 服务器要求重发子包时，或者是服务器反馈的已经受理全部命令时，忽略它
			if (Assert.isRetrySubPacket(mark) || Answer.isSubPacketsAccepted(mark)) {
				//				Logger.debug(this, "batch", "收到冗余包，忽略！");
				continue;
			}
			// 保存子包（是否子包在内部判断）
			else {
				bucket.add(resp);
			}
		}

		// 通知服务器，释放本次包标识号的记录
		cancel(remote, packetId);
		// 重新设为原来的超时
		resetDefaultSoTimeout();

		//		Logger.debug(this, "batch", "本地 %s 发送到 %s 完成, 包编号:%d, 发送子包数:%d，接收子包数：%d",
		//			getSocket(), remote, packetId, packets.length, bucket.size());

		// 合并篮子里的数据包，成为一个数据包返回
		return bucket.compose();
	}

	/**
	 * 发送一个空操作，服务器不做任何处理
	 * @param remote 目标地址
	 * @throws IOException
	 */
	public void empty(SocketHost remote) throws IOException {
		Mark mark = new Mark(Ask.NOTIFY, Ask.EMPTY_OPERATE);
		Packet request = new Packet(remote, mark);
		request.addMessage(MessageKey.SPEAK, "helo, laxcus server!");

		// 检查通信!
		check(remote);
		// 发送包
		send(request);
	}

}


//	private void test() {
//		int size =  PacketBucket.MAX_DATASIZE * 3 + 232;
//		byte[] b = new byte[size];
//		Arrays.fill(b, (byte)'F');
//		SocketHost host = new SocketHost(SocketTag.UDP, Address.select(), 1233);
//		Signal signal = new Signal(Ask.NOTIFY, Ask.CAST);
//		Packet packet = new Packet(host, signal);
//		packet.setData(b);
//
//		Packet[] packets = split(packet, 16);
//		System.out.printf("packet size:%d\n", packets.length);
//		for (Packet e : packets) {
//			b = e.getData();
//			if(Laxkit.isEmpty(b)) {
//				System.out.printf("%d is Empty!\n", e.findInteger(MessageKey.SUBPACKET_SERIAL));
//			} else {
//				System.out.printf("%d data size:%d\n", e.findInteger(MessageKey.SUBPACKET_SERIAL), b.length);
//			}
//		}
//	}
//
//	public void test2() {
//		ArrayList<Integer> a = new ArrayList<Integer>();
//		for(int i =0; i < 122; i++){
//			a.add(i);
//		}
//
//		SocketHost host = new SocketHost(SocketTag.UDP, Address.select(), 1233);
//		try {
//			sendRetryPacket(host, 3, a);
//		} catch (IOException e){
//			e.printStackTrace();
//		}
//
//		Message msg = new Message(MessageKey.SUBPACKET_SERIAL, Integer.MAX_VALUE);
//		byte[] b = msg.build();
//		System.out.printf("message bytes:%d\n", b.length);
//	}
//
//	public static void main(String[] args) {
//		FixpPacketClient e = new FixpPacketClient();
//		e.test2();
//	}


//	/**
//	 * 优雅关闭套接字。<br>
//	 * 即在判断是绑定和连接的状态下，向服务器发送“exit”指令（结束通信报文），再关闭套接字。<br>
//	 * 
//	 * 优雅关闭套接字促使服务器立即释放保存的客户端密钥，而不是等到密钥超时后再释放！
//	 * 
//	 * @param exit 在关闭SOCKET前，发起“exit”命令结束操作（优雅关闭）
//	 */
//	public final void close(boolean exit) {
//		// 套接字已经释放，返回
//		if (isClosed()) {
//			return;
//		}
//
//		// 如果已经绑定或者连接，通知服务器结束会话
//		if (exit) {
//			try {
//				if (isBound() || isConnected()) {
//					exit();
//				}
//			} catch (IOException e) {
//				Logger.error(e);
//			} catch (Throwable e) {
//				Logger.fatal(e);
//			}
//		}
//
//		// 关闭套接字
//		try {
//			socket.close();
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			socket = null;
//		}
//		// 释放全部密文信息
//		ciphers.clear();
//	}

//	/**
//	 * 优雅关闭套接字。
//	 * 即在判断是绑定和连接的状态下，向服务器发送“通信结束”的数据包，再关闭套接字
//	 */
//	public final void close() {
//		close(true);
//	}


//	/**
//	 * 脱钩！<br>
//	 * 
//	 * 通知服务器，释放加密通道，但是不关闭本地的套接字。<br>
//	 * 
//	 * @return 返回与服务器解决的加密通道数目
//	 * @throws IOException IO异常
//	 */
//	public int detach() throws IOException {
//		return exit();
//	}


//	/**
//	 * 优雅关闭套接字。
//	 * 即在判断是绑定和连接的状态下，向服务器发送“通信结束”的数据包，再关闭套接字
//	 */
//	public final void close(boolean exit) {
//		close();
//	}

//	/**
//	 * 连接到FIXP服务器
//	 * @param endpoint FIXP UDP服务器监听地址
//	 * @throws IOException
//	 */
//	private void connect(SocketAddress endpoint) throws IOException {
//		// 如果已经连接，先关闭它
//		if (isConnected() || isBound()) {
//			close(false);
//		}
//		// 绑定本地地址
//		if (!bind()) {
//			throw new BindException("bind failed!");
//		}
//
//		// 设置接收缓冲区尺寸
//		if (receiveBufferSize > 0) {
//			socket.setReceiveBufferSize(receiveBufferSize);
//		}
//		// 设置发送缓冲区尺寸
//		if (sendBufferSize > 0) {
//			socket.setSendBufferSize(sendBufferSize);
//		}
//		// 设置接收超时
//		resetSoTimeout();
//		// 连接到服务器
//		socket.connect(endpoint);
//	}


//	/**
//	 * 向服务器查询它的安全属性，包括四种：<br>
//	 * 1. 无验证要求 <br>
//	 * 2. 地址验证 <br>
//	 * 3. 密钥验证 <br>
//	 * 4. 地址+密钥的复合验证 <br><br>
//	 * 
//	 * @param remote 目标站点地址
//	 * @return 返回安全属性，不能确定返回-1。
//	 * @throws SecureException
//	 * @throws IOException
//	 */
//	public int askSecure(SocketHost remote) throws SecureException, IOException {
//		// 如果密文存在，不能进行查询
//		if (isSecured(remote)) {
//			throw new SecureException("secure existed!");
//		}
//		// 生成数据包
//		Mark mark = new Mark(Ask.NOTIFY, Ask.SECURE_QUERY);
//		Packet request = new Packet(remote, mark);
//		// 发送和接收数据(不要调用swap方法)
//		send(request);
//
//		// 接收
//		Packet resp = receive();
//		// 判断并且返回结果
//		mark = resp.getMark();
//		if (mark.getAnswer() == Answer.SECURE_NOTIFY) {
//			Message e = resp.findMessage(MessageKey.SECURE_FAMILY);
//			if (e != null && e.isInteger()) {
//				return e.getInteger();
//			}
//		}
//		// 不能确定
//		return -1;
//	}

//	/**
//	 * 关闭一个连接!
//	 * @param endpoint 目标地址
//	 * @return 成功返回真，否则假
//	 * @throws IOException
//	 */
//	private boolean exit(SocketHost endpoint) throws IOException {
//		Mark mark = new Mark(Ask.NOTIFY, Ask.EXIT);
//		Packet request = new Packet(endpoint, mark);
//
//		// 子包超时时间
//		resetSubPacketSoTimeout();
//		
//		// 发送数据
//		send(request);
//		// 接收数据
//		Packet resp = receive();
//		
//		// 默认的超时时间
//		resetDefaultSoTimeout();
//
//		// 判断是“再见”应答
//		return (resp != null && Assert.isGoodbye(resp.getMark()));
//	}


//	/**
//	 * 通知所有保持着连接的节点，结束会话。
//	 * 
//	 * @return 返回关闭的数目
//	 * @throws IOException
//	 */
//	private int exit() throws IOException {
//		TreeSet<SocketHost> array = new TreeSet<SocketHost>();
//		// 如果目标地址是有效时，保存它
//		if (remote != null && remote.isValid()) {
//			array.add(remote);
//		}
//
//		// 复制其它有安全通信的节点
//		array.addAll(ciphers.keySet());
//
//		int count = 0;
//		Mark mark = new Mark(Ask.NOTIFY, Ask.EXIT);
//		for (SocketHost endpoint : array) {
//			Packet request = new Packet(endpoint, mark);
//			// 发送数据
//			send(request);
//			// 接收数据
//			Packet resp = receive();
//
//			// 判断是“再见”应答
//			if (Assert.isGoodbye(resp.getMark())) {
//				// 释放密文
//				ciphers.remove(endpoint);
//				count++;
//			}
//
////			Logger.debug(this, "exit", "%s result %d", endpoint, resp.getMark().getAnswer());
//		}
//
////		Logger.debug(this, "exit", "count:%d", count);
//
//		return count;
//	}

//	/**
//	 * 向目标FIXP包服务器发送测试。<br>
//	 * 操作流程：<br>
//	 * 1. 调用“bind”方法，绑定一个本机地址。<br>
//	 * 2. 调用“test”方法，进行通信测试。<br>
//	 * 3. 调用“close”方法，close先用“exit”方法关闭通话，再关闭本地socket udp。<br>
//	 * 
//	 * @param remote 目标服务器地址
//	 * @return 成功返回真，否则假
//	 * @throws IOException
//	 */
//	public boolean test(SocketHost remote) throws IOException {
//		Mark mark = new Mark(Ask.NOTIFY, Ask.TEST);
//		Packet request = new Packet(remote, mark);
//		request.addMessage(MessageKey.SPEAK, "TEST!");
//		// 交换数据包
//		Packet resp = swap(request);
//		// 判断结果
//		return Answer.isAccept(resp.getMark());
//	}

//	/**
//	 * 向目标FIXP包服务器发送测试。<br>
//	 * 操作流程：<br>
//	 * 1. 调用“bind”方法，绑定一个本机地址。<br>
//	 * 2. 调用“test”方法，进行通信测试。<br>
//	 * 3. 调用“close”方法，close先用“exit”方法关闭通话，再关闭本地socket udp。<br>
//	 * 
//	 * @param remote 目标服务器地址
//	 * @return 成功返回真，否则假
//	 * @throws IOException
//	 */
//	public boolean test(SocketHost remote) throws IOException {
//		return test(remote, true);
//	}

//	/**
//	 * 通知服务器，释放与本地址和包标识号的连接记录
//	 * @param remote
//	 * @param packetId
//	 * @throws IOException
//	 */
//	private void cancel(SocketHost remote, int packetId) throws IOException {
//		Mark mark = new Mark(Ask.NOTIFY, Ask.CANCEL_PACKET);
//		Packet request = new Packet(remote, mark);
//		request.addMessage(MessageKey.PACKET_IDENTIFY, packetId);
//
//		// 10秒超时
//		setSoTimeout(10000);
//
//		// 取消操作
//		while (true) {
//			// 发送包
//			send(request);
//			// 等待应答结果
//			try {
//				Packet resp = receive();
//				// if "okay" packet, next; if "not-found" exit!
//				// 如果是OKAY包，退出等待；如果通知是没有找到，直接返回
//				mark = resp.getMark();
//				// 确认！
//				if (mark.getAnswer() == Answer.CANCEL_OKAY) {
//					Logger.debug(this, "cancel", "撤销包，确认成功！");
//					break;
//				}
//				// 撤销
//				else if (mark.getAnswer() == Answer.CANCEL_NOTFOUND) {
//					Logger.debug(this, "cancel", "撤销包，没有找到！");
//					return;
//				} else {
//					Logger.warning(this, "cancel", "ignore %s, from %s", mark, resp.getRemote());
//				}
//			} catch (SocketTimeoutException e) {
//				Logger.error(e);
//			}
//		}
//
//		// 再次检查确定包记录已经释放
//		while (true) {
//			send(request);
//			try {
//				Packet resp = receive();
//				// 如果是没有找到，退出
//				mark = resp.getMark();
//				if (mark.getAnswer() == Answer.CANCEL_NOTFOUND) {
//					Logger.debug(this, "cancel", "再次撤销包，没有找到！");
//					break;
//				} else {
//					Logger.warning(this, "cancel", "ignore %s, from %s", mark, resp.getRemote());
//				}
//			} catch(SocketTimeoutException e) {
//				Logger.error(e);
//			}
//		}
//
////		// 重设超时时间为原来状态
////		resetSoTimeout();
//	}

//	/**
//	 * 通知服务器，释放与本地址和包标识号的连接记录
//	 * @param remote
//	 * @param packetId
//	 * @throws IOException
//	 */
//	private void cancel(SocketHost remote, int packetId) throws IOException {
//		Mark mark = new Mark(Ask.NOTIFY, Ask.CANCEL_PACKET);
//		Packet request = new Packet(remote, mark);
//		request.addMessage(MessageKey.PACKET_IDENTIFY, packetId);
//
//		// 10秒超时
//		setSoTimeout(10000);
//
//		// 发送包
//		send(request);
//		// 等待应答结果
//		try {
//			Packet resp = receive();
//			// 判断：1. 如果是CANCEL_OKAY包，继续下一次确认；
//			// 2. 如果CANCEL_NOTFOUND，证明服务端没有找到，直接返回
//			mark = resp.getMark();
//			if (mark.getAnswer() == Answer.CANCEL_OKAY) {
////				Logger.debug(this, "cancel", "撤销包，确认成功！");
//			}
//			if (mark.getAnswer() == Answer.CANCEL_NOTFOUND) {
////				Logger.debug(this, "cancel", "撤销包，没有找到！");
//				return;
//			}
//		} catch (SocketTimeoutException e) {
//			Logger.error(e);
//		}
//
//		// 再次检查确定包记录已经释放
//		send(request);
//		try {
//			Packet resp = receive();
//			// 希望本次确认没有找到
//			mark = resp.getMark();
//			if (mark.getAnswer() == Answer.CANCEL_NOTFOUND) {
////				Logger.debug(this, "cancel", "再次撤销包，没有找到！");
//			}
//		} catch(SocketTimeoutException e) {
//			Logger.error(e);
//		}
//	}


//	/**
//	 * 检查SOCKET状态，判断服务器安全状态和选择是否初始化密文
//	 * @param remote
//	 * @throws IOException
//	 */
//	private void check(SocketHost remote) throws SecureException, IOException {
//		//1. 检查
//		if (isConnected()) {
//			// 连接状态只能有一个连接地址。此时地址必须一致，否则关闭再绑定
//			if (remote.isValid() && remote.compareTo(super.getRemote()) != 0) {
//				close();
////				bind();
//				connect(remote);
//			}
//		} else if (isClosed() || !isBound()) {
//			// 绑定本地地址
//			close();
//			bind();
//		}
//
//		// 2. 删除超时密文
//		ArrayList<SocketHost> array = new ArrayList<SocketHost>();
//		Iterator<Map.Entry<SocketHost, Cipher>> iterators = ciphers.entrySet().iterator();
//		// 判断超时的节点并且保存
//		while (iterators.hasNext()) {
//			Map.Entry<SocketHost, Cipher> entry = iterators.next();
//			if (entry.getValue().isTimeout()) {
//				array.add(entry.getKey());
//			}
//		}
//		for (SocketHost e : array) {
//			ciphers.remove(e);
//		}
//
//		// 3. 检查密文，如果存在则返回
//		if (isSecured(remote)) {
//			return;
//		}
//
//		// 4. 询问服务器安全状态
//		int secured = askSecure(remote);
//		// 如果要求加密时
//		if (SecureType.isCipher(secured)) {
//			if (!createSecure(remote)) {
//				throw new SecureException("create secure, failed!");
//			}
//		}
//	}


//	/**
//	 * 发送并且接收FIXP数据包
//	 * @param packet 发送的FIXP数据包
//	 * @return 返回接收的FIXP数据包
//	 */
//	public Packet swap(Packet packet) throws IOException {
//		// 检查SOCKET
//		check(packet.getRemote());
//		// 设置接收超时，0是无限制
//		resetDefaultSoTimeout();
//		// 发送数据包
//		send(packet);
//		// 接收数据包
//		Packet resp = receive();
//		// 如果服务器要求加密时，用RSA公钥加密一个随机密文
//		if (resp.getMark().getAnswer() == Answer.SECURE_NOTIFY) {
//			createSecure(packet.getRemote());
//			send(packet);
//			resp = receive();
//		}
//
//		return resp;
//	}


//	/**
//	 * 连接到FIXP服务器
//	 * @param endpoint FIXP UDP服务器监听地址
//	 * @throws IOException
//	 */
//	private void connect(SocketAddress endpoint) throws IOException {
//		// 如果已经连接，先关闭它
//		if (isConnected() || isBound()) {
//			close();
//		}
//		// 绑定本地地址
//		boolean success = bind();
//		if (!success) {
//			throw new BindException("bind failed!");
//		}
//
//		// 设置接收超时
//		resetDefaultSoTimeout();
//		// 连接到服务器
//		socket.connect(endpoint);
//	}
//
//	/**
//	 * 连接到FIXP服务器
//	 * @param endpoint UDP FIXP服务器监听地址
//	 * @throws IOException
//	 */
//	public void connect(SocketHost endpoint) throws IOException {
//		// 连接服务器
//		connect(endpoint.getSocketAddress());
//		// 保存服务器地址
//		setRemote(endpoint);
//	}


///**
// * 更新密文。<br><br>
// * 
// * 此操作建立在“已经建立安全通信”的基础上，即已经使用“createSecure”加密了数据。
// * 并且要求客户机主机和密文主机来自一个IP地址，服务器将对此做验证。<br>
// * 
// * 此操作由FIXP客户机发超，被投递到FIXP服务器上，FIXP服务器将根据命令要求，删除旧密文，保存新的密文。<br>
// * 
// * @param remote FIXP服务器地址
// * @param host 已经记录在服务器的主机地址
// * @param oldCipher 旧密文（已经保存在FIXP服务器）
// * @param newCipher 新密文
// * @return 更新成功返回真，否则假
// * 
// * @throws SecureException
// * @throws IOException
// */
//public boolean replaceSecure(SocketHost remote, SocketHost host,
//		Cipher oldCipher, Cipher newCipher) throws SecureException, IOException {
//	// 如果没有指定目标站点地址，判断已经连接，取出目标站点地址
//	if (remote == null) {
//		if (isConnected()) remote = getRemote();
//	}
//	// 判断目标站点地址
//	if (remote == null) {
//		throw new IOException("must be set server address!");
//	}
//	// 必须已经执行了安全通信，否则是错误
//	if(!isSecured(remote)) {
//		throw new SecureException("must be RSA ENCRYPT!");
//	}
//
//	// 判断参数是空指针
//	Laxkit.nullabled(host);
//	Laxkit.nullabled(oldCipher);
//	Laxkit.nullabled(newCipher);
//
//	// 可类化数据
//	ClassWriter writer = new ClassWriter();
//	writer.writeObject(host);
//	writer.writeObject(oldCipher);
//	writer.writeObject(newCipher);
//	byte[] b = writer.effuse();
//
//	// 撤销加密安全数据包
//	Mark mark = new Mark(Ask.NOTIFY, Ask.SECURE_REPLACE);
//	Packet packet = new Packet(remote, mark);
//	packet.setData(b);
//	// 发送和接受数据包(不要调用swap方法)
//	send(packet);
//	Packet resp = receive();
//
//	// 判断是否接受
//	int code = resp.getMark().getAnswer();
//	// 必须返回接受，否则都是错误
//	boolean success = (code == Answer.SECURE_ACCEPTED);
//
//	Logger.note(this, "replaceSecure", success, "%s To %s, cipher host:%s, old cipher:%s | new cipher:%s",
//			getLocal(), remote, host, oldCipher, newCipher);
//
//	return success;
//}


///**
// * 建立密文通信服务。<br><br>
// * 
// * 向服务器投递一个FIXP密文，要求服务器保存，启动安全通信。<br>
// * 此后数据通信将在加密状态下操作，即客户机加密，服务器解密。加解密由FIXP密文完成。<br>
// * 
// * @param remote 目标站点地址
// * @param cipher FIXP密文
// * @return 启动返回真，否则假
// * @throws SecureException
// * @throws IOException
// */
//public boolean createSecure(SocketHost remote, Cipher cipher) throws SecureException, IOException {
//	// 安全初始化前，不能保存有密文
//	if (isSecured(remote)) {
//		throw new SecureException("cipher existed!");
//	}
//	if (remote == null) {
//		throw new IOException("unknown remote address!");
//	}
//	// 如果没有定义，生成一个
//	if (cipher == null) {
//		cipher = createCipher();
//	}
//
//	// 通过目标站点地址，找到对应的公钥，对数据进行加密
//	Address server = remote.getAddress();
//	ClientToken token = ClientTokenManager.getInstance().find(server);
//	if (token == null) {
//		throw new SecureException(
//				"cannot be find client-token by '%s', please check local.xml", server);
//	}
//	byte[] data = cipher.encase(token.getKey());
//
//	// 通常服务器上的RSA私钥解密都很耗时，所以接收等待时间要延长。保证最少60秒
//	int ms = (receiveTimeout > 60000 ? receiveTimeout : 60000);
//	setSoTimeout(ms);
//
//	// 建立加密初始化数据包
//	Mark cmd = new Mark(Ask.NOTIFY, Ask.SECURE_CREATE);
//	Packet packet = new Packet(remote, cmd);
//	packet.setData(data);
//	// 发送和接受数据包(不要调用swap方法)
//	send(packet);
//	Packet resp = receive();
//
//	// 判断是否接受
//	int code = resp.getMark().getAnswer();
//	boolean success = (code == Answer.SECURE_ACCEPTED);
//	// 如果成功，保存这个配置
//	if (success) {
//		addCipher(remote, cipher);
//	} else {
//		Logger.error(this, "createSecure", "encrypt failed! %s to %s, secure code: %d", getLocal(), remote, code);
//	}
//
//	// 恢复为原来的接收时间
//	resetDefaultSoTimeout();
//
//	return success;
//}

//	/**
//	 * 判断是“替换安全”命令
//	 * @param mark FIXP协议标头
//	 * @return 返回真或者假
//	 */
//	public static boolean isSecureReplace(Mark mark) {
//		return Assert.match(mark, Ask.NOTIFY, Ask.SECURE_REPLACE);
//	}

///**
//* 向目标FIXP服务器发送测试。<br>
//* 操作流程：<br>
//* 1. 调用“bind”方法，绑定一个本机地址。<br>
//* 2. 调用“test”方法，进行通信测试。<br>
//* 3. 调用“close”方法，close先用“exit”方法关闭通话（secure模式），再关闭本地socket udp。<br>
//* 
//* @param remote 目标服务器地址
//* @param secure 安全通信模式。如果是安全通信在启动前将进行密钥交换，否则不启用。
//* @return 成功返回真，否则假
//* @throws IOException
//*/
//public boolean test(SocketHost remote, boolean secure) throws IOException {
//	Mark mark = new Mark(Ask.NOTIFY, Ask.TEST);
//	Packet request = new Packet(remote, mark);
//	request.addMessage(MessageKey.SPEAK, "TEST!");
//	// 交换数据包
//	Packet resp = null;
//	if (secure) {
//		resp = swap(request);
//	} else {
//		send(request);
//		resp = receive();
//	}
//
//	// 判断结果
//	return resp != null && Answer.isAccept(resp.getMark());
//}


///**
// * 建立密文通信服务。<br><br>
// * 
// * 向服务器投递一个FIXP密文，要求服务器保存，启动安全通信。<br>
// * 此后数据通信将在加密状态下操作，即客户机加密，服务器解密。加解密由FIXP密文完成。<br>
// * 
// * @param remote 目标站点地址
// * @param cipher FIXP密文
// * @return 启动返回真，否则假
// * @throws SecureException
// * @throws IOException
// */
//public boolean createSecure(SocketHost remote, Cipher cipher) throws SecureException, IOException {
//	// 安全初始化前，不能保存有密文
//	if (isSecured(remote)) {
//		throw new SecureException("cipher existed!");
//	}
//	if (remote == null) {
//		throw new IOException("unknown remote address!");
//	}
//	// 如果没有定义，生成一个
//	if (cipher == null) {
//		cipher = createCipher();
//	}
//
//	// 通过目标站点地址，找到对应的公钥，对数据进行加密
//	Address server = remote.getAddress();
//	ClientToken token = ClientTokenManager.getInstance().find(server);
//	if (token == null) {
//		throw new SecureException(
//				"cannot be find client-token by '%s', please check local.xml", server);
//	}
//	byte[] data = cipher.encase(token.getKey());
//
//	// 通常服务器上的RSA私钥解密都很耗时，所以接收等待时间要延长。保证最少60秒
//	int ms = (receiveTimeout > 60000 ? receiveTimeout : 60000);
//	setSoTimeout(ms);
//
//	// 建立加密初始化数据包
//	Mark cmd = new Mark(Ask.NOTIFY, Ask.SECURE_CREATE);
//	Packet packet = new Packet(remote, cmd);
//	packet.setData(data);
//	// 发送和接受数据包(不要调用swap方法)
//	send(packet);
//	
//	// 接收数据包
//	boolean success = false;
//	while (true) {
//		Packet resp = receive();
//
//		// 取应答码
//		int code = resp.getMark().getAnswer();
//		// 如果是"SECURE_NOTIFY"，这是“askSecure”命令多次发送后，FixpPacketHelper返回的冗余，忽略它！
//		if (code == Answer.SECURE_NOTIFY) {
//			continue;
//		}
//
//		// 判断是安全接受！
//		success = (code == Answer.SECURE_ACCEPTED);
//		// 如果成功，保存这个配置
//		if (success) {
//			addCipher(remote, cipher);
//		} else {
//			Logger.error(this, "createSecure", "encrypt failed! %s to %s, secure code: %d",
//					getLocal(), remote, code);
//		}
//		break;
//	}
//
//	// 恢复为原来的接收时间
//	resetDefaultSoTimeout();
//
//	return success;
//}


///**
// * 向服务器查询它的安全属性，包括四种：<br>
// * 1. 无验证要求 <br>
// * 2. 地址验证 <br>
// * 3. 密钥验证 <br>
// * 4. 地址+密钥的复合验证 <br><br>
// * 
// * @param remote 目标站点地址
// * @return 返回安全属性，不能确定返回-1。
// * @throws SecureException
// * @throws IOException
// */
//public int askSecure(SocketHost remote) throws SecureException, IOException {
//	// 如果密文存在，不能进行查询
//	if (isSecured(remote)) {
//		throw new SecureException("secure existed!");
//	}
//
//	// 设置子包接收超时
//	resetSubPacketSoTimeout();
//
//	// 生成数据包
//	Mark mark = new Mark(Ask.NOTIFY, Ask.SECURE_QUERY);
//	Packet request = new Packet(remote, mark);
//	
//	// 发送和接收数据(不要调用swap方法)，3次测试，防止服务器压力过大，出现不能及时反馈的现象！
//	for (int index = 0; index < 3; index++) {
//		// 发送请求包
//		send(request);
//
//		// 接收
//		Packet resp = null;
//		try {
//			resp = receive();
//		} catch (SocketTimeoutException e) {
//			Logger.error(e, "from %s", remote);
//		}
//
//		// 有效！判断并且返回结果
//		if (resp != null) {
//			mark = resp.getMark();
//			if (mark.getAnswer() == Answer.SECURE_NOTIFY) {
//				Message msg = resp.findMessage(MessageKey.SECURE_FAMILY);
//				boolean success = (msg != null && msg.isInteger());
//				if (success) {
//					// 重置成默认超时
//					resetDefaultSoTimeout();
//					return msg.getInteger();
//				}
//			}
//		}
//	}
//
//	// 弹出错误！
//	throw new SocketTimeoutException("ask secure timeout! " + remote.toString());
//}


///**
// * 向服务器查询它的安全属性，包括四种：<br>
// * 1. 无验证要求 <br>
// * 2. 地址验证 <br>
// * 3. 密钥验证 <br>
// * 4. 地址+密钥的复合验证 <br><br>
// * 
// * @param remote 目标站点地址
// * @return 返回安全属性，不能确定返回-1。
// * @throws SecureException
// * @throws IOException
// */
//public int askSecure(SocketHost remote) throws SecureException, IOException {
//	// 如果密文存在，不能进行查询
//	if (isSecured(remote)) {
//		throw new SecureException("secure existed!");
//	}
//
//	// 设置子包接收超时
//	resetSubPacketSoTimeout();
//
//	// 生成数据包
//	Mark mark = new Mark(Ask.NOTIFY, Ask.SECURE_QUERY);
//	Packet request = new Packet(remote, mark);
//	
//	// 发送和接收数据(不要调用swap方法)，3次测试，防止服务器压力过大，出现不能及时反馈的现象！
//	for (int index = 0; index < 3; index++) {
//		// 发送请求包
//		send(request);
//
//		// 接收
//		Packet resp = null;
//		try {
//			resp = receive();
//		} catch (SocketTimeoutException e) {
//			Logger.error(e, "from %s", remote);
//		}
//
//		// 有效！判断并且返回结果
//		if (resp != null) {
//			mark = resp.getMark();
//			if (mark.getAnswer() == Answer.SECURE_NOTIFY) {
//				// 重置成默认超时
//				resetDefaultSoTimeout();
//				
//				// 保存密钥
//				byte[] b = resp.getData();
//				PublicSecure secure = new PublicSecure(b);
//				ClientSecureTrustor.getInstance().addSiteSecure(remote, secure);
//				return secure.getFamily();
//			}
//		}
//	}
//
//	// 弹出错误！
//	throw new SocketTimeoutException("ask secure timeout! " + remote.toString());
//}

