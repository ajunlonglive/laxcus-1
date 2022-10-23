/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

import java.io.*;
import java.nio.*;
import java.util.*;

import com.laxcus.fixp.*;
import com.laxcus.fixp.client.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.invoke.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.*;
import com.laxcus.security.*;
import com.laxcus.site.*;
import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * FIXP数据包辅助处理器
 * 
 * @author scott.liang
 * @version 1.1 5/16/2012
 * @since laxcus 1.0
 */
public final class FixpPacketHelper extends MutexThread implements EchoMessenger{

	/** 序列号生成器 **/
	private SerialGenerator generator = new SerialGenerator();

	/** UDP数据报文存储器  */
	private ArrayList<PrimitivePacket> primitivePackets = new ArrayList<PrimitivePacket>();

	/** RPC调用接口 **/
	private VisitInvoker visitInvoker;

	/** FIXP包调用接口 **/
	private PacketInvoker packetInvoker;

	/** 客户地址 -> 请求数据包集合. 基于KEEP UDP。**/
	private TreeMap<SocketHost, PacketBucket> requests = new TreeMap<SocketHost, PacketBucket>();

	/** 客户地址 ->应答数据包集合 , 基于KEEP UDP **/
	private TreeMap<SocketHost, PacketBucket> resps = new TreeMap<SocketHost, PacketBucket>();

	/** 线程循环分配索引编号 **/
	private int taskIndex;

	/** UDP数据包处理线程实例的数组集合 **/
	private PacketTask[] tasks;

	/** FIXP数据包服务器句柄 **/
	private FixpPacketMonitor packetMonitor;

	/** 密文存储器 **/
	private SecureCollector secureCollector;

	/**
	 * 构造FIXP UDP监听器的辅助器，并且设置相关的参数
	 * @param monitor FIXP UDP监听管理器
	 * @param collector FIXP密文存储器
	 */
	protected FixpPacketHelper(FixpPacketMonitor monitor, SecureCollector collector) {
		super();
		packetMonitor = monitor;
		secureCollector = collector;
		taskIndex = 0;
	}

	/**
	 * 设置任务线程数目。只允许设置一次
	 * @param num 线程数目
	 * @return 成功返回真，否则假
	 */
	public boolean setTaskThreads(int num) {
		if (tasks == null && num > 0) {
			tasks = new PacketTask[num];
			return true;
		}
		return false;
	}

	/**
	 * 返回任务线程数目，如果没有定义，返回-1.
	 * @return 线程数目
	 */
	public int getTaskThreads() {
		if (tasks == null) {
			return -1;
		}
		return tasks.length;
	}

	/**
	 * 保存地址/密文对
	 * @param endpoint 客户机站点地址
	 * @param cipher 密文实例
	 * @return 成功返回真，否则假
	 */
	private boolean addCipher(SocketHost endpoint, Cipher cipher) {
		return secureCollector.add(endpoint, cipher);
	}

	/**
	 * 删除指定站点的密文
	 * @param endpoint 站点地址
	 * @return 删除成功返回真，否则假
	 */
	private boolean removeCipher(SocketHost endpoint) {
		return secureCollector.remove(endpoint);
	}

	/**
	 * 根据地址查找密文
	 * @param endpoint 站点地址
	 * @return 返回密文，没有返回空指针。
	 */
	private Cipher findCipher(SocketHost endpoint) {
		return secureCollector.find(endpoint);
	}

	/**
	 * 根据地址，判断密文存在
	 * @param endpoint 站点地址
	 * @return 返回真或者假
	 */
	private boolean hasCipher(SocketHost endpoint) {
		return secureCollector.contains(endpoint);
	}

	/**
	 * 设置RPC处理接口
	 * @param e VisitInvoker接口
	 */
	public void setVisitInvoker(VisitInvoker e) {
		visitInvoker = e;
	}

	/**
	 * 返回RPC处理接口
	 * @return VisitInvoker句柄
	 */
	public VisitInvoker getVisitInvoker() {
		return visitInvoker;
	}

	/**
	 * 设置FIXP数据包分派接口
	 * @param e PacketInvoker实例
	 */
	public void setPacketInvoker(PacketInvoker e) {
		packetInvoker = e;
	}

	/**
	 * 返回FIXP数据包分派接口
	 * @return PacketInvoker实例
	 */
	public PacketInvoker getPacketInvoker() {
		return packetInvoker;
	}

	/**
	 * 连接发送N个数据包到客户端
	 * @param packet 数据包
	 * @param count 发送次数
	 */
	protected int send(Packet packet, int count) {
		return packetMonitor.send(packet.getRemote(), packet, count);
	}

	/**
	 * 发送数据到客户端
	 * @param packet FIXP数据包
	 */
	protected boolean send(Packet packet) {
		return packetMonitor.send(packet.getRemote(), packet);
	}

	/**
	 * 发送数据到指定地址的客户端
	 * @param remote 客户端地址
	 * @param packet FIXP数据包
	 */
	protected void send(SocketHost remote, Packet packet) {
		packetMonitor.send(remote, packet);
	}

	/**
	 * 将字节数组解析成指定类后，调用本地接口处理
	 * @param data 数据包中的字节数组
	 * @return 返回经过本地RPC处理后的字节数组
	 */
	private byte[] callRPCLocal(byte[] data) {
		// 解析请求数据
		PatternConstructor apply = null;
		PatternExtractor reply = null;
		try {
			if (data == null) {
				throw new NullPointerException("RPC data is null");
			}
			// 生成解析器
			apply = PatternConstructor.resolve(data);
		} catch (Throwable e) {
			Logger.fatal(e);
			reply = new PatternExtractor(e);
		}
		// 调用RPC服务器接口
		if (reply == null) {
			reply = visitInvoker.invoke(apply);
		}
		// 输出结果
		return reply.build();
	}

	/**
	 * 执行RPC本地操作：根据请求包调用本地RPC接口，返回处理后的FIXP数据包。
	 * @param request FIXP请求包
	 * @return FIXP应答包
	 * @throws IOException
	 */
	protected Packet callRPC(Packet request) throws IOException {
		// 将包的字节数组解析，调用本地RPC服务，返回处理后的字节数组
		byte[] data = callRPCLocal(request.getData());

		Packet resp = new Packet(request.getRemote(), Answer.OKAY);
		resp.addMessage(MessageKey.CONTENT_TYPE, MessageKey.RAW_DATA);
		resp.setData(data, 0, data.length);
		return resp;
	}	

	/**
	 * 净数据包交给数据包接口处理
	 * @param packet FIXP UDP数据包，可以是请求/应答中的任何一种
	 * @return  FIXP UDP应答包
	 */
	protected Packet callMethod(Packet packet) {
		if (packetInvoker != null) {
			Packet resp = packetInvoker.invoke(packet);
			return resp;
		}
		return null;
	}

	/**
	 * 发送一个服务器处理错误的应答给请求端
	 */
	protected Packet invalid() throws FixpProtocolException {
		Mark mark = new Mark(Answer.SERVER_ERROR);
		Packet packet = new Packet(mark);
		packet.addMessage(MessageKey.SPEAK, "sorry, invalid!");
		return packet;
	}

	/* 以下是KEEP UDP数据包 */

	/**
	 * 根据远程地址，返回一个应答包集
	 * @param remote 目标地址
	 * @return 返回应答包集
	 */
	private PacketBucket findReplyBucket(SocketHost remote) {
		super.lockMulti();
		try {
			return resps.get(remote);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 保存一个应答包集
	 * @param remote 目标地址
	 * @param bucket 应答包集
	 */
	private void addReplyBucket(SocketHost remote, PacketBucket bucket) {
		// 锁定
		super.lockSingle();
		try {
			resps.put(remote, bucket);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 删除应答包
	 * @param remote 目标地址
	 * @param packetId 包编号
	 * @return 成功返回真，否则假
	 */
	private boolean removeReplyBucket(SocketHost remote, int packetId) {
		// 锁定
		super.lockSingle();
		try {
			PacketBucket bucket = resps.get(remote);
			// 判断一致，删除它
			boolean success = (bucket != null && bucket.getPacketId() == packetId);
			if (success) {
				resps.remove(remote);
				return true;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 失败，返回空指针
		return false;
	}

	/**
	 * 从请求包中，取出全部子包序号，逐一发送子包给请求端
	 * @param request 请求包
	 */
	private void sendReplySubPacket(Packet request) {
		SocketHost remote = request.getRemote();
		//		PacketBucket bucket = resps.get(remote);

		// 找到应答包集
		PacketBucket bucket = findReplyBucket(remote);
		// 不存在，忽略
		if (bucket == null) {
			return;
		}

		// 找到重发子包序号
		List<Integer> serials = request.findIntegers(MessageKey.SUBPACKET_SERIAL);
		// 逐一发送子包
		for (int index : serials) {
			Packet sub = bucket.findPacket(index);
			if (sub != null) {
				packetMonitor.send(remote, sub);
			}
		}
	}

	/**
	 * 发送一个请求取消双方操作的数据包给客户端，结束一次会话操作
	 * @param request FIXP请求包
	 */
	private void sendRequestCancelPacket(Packet request) {
		SocketHost remote = request.getRemote();
		Integer packetId = request.findInteger(MessageKey.PACKET_IDENTIFY);
		// 没有找到编号，忽略！
		if (packetId == null) {
			Logger.error(this, "sendRequestCancelPacket", "cannot be find packet identity, from %s", remote);
			return;
		}

		// 删除客户端发来的请求包，不管它是否存在，这是一个冗余的操作
		requests.remove(remote);

		// 删除反馈包，保证线程不再处理它
		boolean success = removeReplyBucket(remote, packetId.intValue());

		// 生成应答包
		short code = (success ? Answer.CANCEL_OKAY : Answer.CANCEL_NOTFOUND);
		Packet resp = new Packet(remote, code);
		resp.addMessage(MessageKey.SPEAK, "cancel io packet bucket!");

		//		Logger.debug(this, "sendRequestCancelPacket", "撤销来自 %s 包编号：%d, 本地应答包：%s,  %s", remote,
		//				 packetId.intValue(), (success ? "存在" : "不存在"), (success ? "成功！" : "不成功！"));

		packetMonitor.send(remote, resp);
	}

	/**
	 * 保存一个FIXP UDP请求子包（采用KEEP UDP模式）
	 * @param packet 子包实例
	 */
	private void addRequestSubPacket(Packet packet) { // throws FixpProtocolException {
		SocketHost remote = packet.getRemote();
		PacketBucket bucket = requests.get(remote);
		if (bucket == null) {
			bucket = new PacketBucket();
			requests.put(remote, bucket);
		}
		bucket.add(packet);

		// 子包填满时，转给调用任务处理(以循环方式分配给各处理线程)
		if (bucket.isFull()) {
			// 通知客户端，当前子包集已经受理！
			acceptSubPackets(bucket.getRemote(), bucket.getPacketId());

			// 从队列中清除子包集
			requests.remove(remote);
			// 分配一个任务句柄
			if (taskIndex >= tasks.length) {
				taskIndex = 0;
			}
			tasks[taskIndex++].add(bucket);
		}
	}

	/**
	 * 向目标地址发送数据包，数据包采用子包集模式发送，即先拆分成多个小于IP包尺寸的数据，再发送给目标地址。
	 * @param remote 目标地址
	 * @param resp 应答包
	 */
	protected void reply(SocketHost remote, Packet resp, int packetId) {
		PacketBucket bucket = split(remote, resp, packetId);

		// 以单向锁定方式，保存子包集合，预防再次发送
		addReplyBucket(remote, bucket);

		// 发送子包集合
		send(bucket);
	}

	/**
	 * 发送一批应答子包到客户端地址
	 * @param bucket 子包集
	 */
	private void send(PacketBucket bucket) {
		// 输出全部子包
		for (Packet resp : bucket.list()) {
			send(resp);
		}
	}

	/**
	 * 切割数据
	 * @param data 字节数组
	 * @return 字节数组段
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
				int size = Laxkit.limit(seek, end, FixpPacketClient.getDefaultSubPacketSize());
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
	 * 将一个FIXP数据包分割成多个子包
	 * @param remote 目标地址
	 * @param resp FIXP应答包
	 * @param packetId 包编号
	 * @return 子包集
	 */
	private PacketBucket split(SocketHost remote, Packet resp, int packetId) {
		byte[] data = resp.getData();
		List<ByteBuffer> buffs = split(data);

		PacketBucket bucket = new PacketBucket();

		int blocks = buffs.size();
		for (int index = 0; index < blocks; index++) {
			Packet sub = new Packet(remote, resp.getMark());
			sub.addMessage(MessageKey.PACKET_IDENTIFY, packetId); // 数据包编号
			sub.addMessage(MessageKey.SUBPACKET_COUNT, blocks); // 数据子包数目
			sub.addMessage(MessageKey.SUBPACKET_SERIAL, index); // 子包序号，从0开始
			sub.addMessage(MessageKey.SUBPACKET_TIMEOUT, 6000); // 子包超时
			sub.addMessage(MessageKey.SUBPACKET_DISABLE_TIMEOUT, 60000); //失效超时
			sub.addMessages(resp.getMessages()); // 复制全部消息到子包
			// 保存数据
			ByteBuffer buf = buffs.get(index);
			sub.setData(buf.array());
			// 保存数据包
			bucket.add(sub);
		}

		//		Logger.debug(this, "split", "to %s, packet id:%d, sub packets:%d", remote, packetId, bucket.size());

		// 保存数据包
		return bucket;
	}

	/**
	 * 服务器端通知请求方，再次发送一个指定子包编号的FIXP UDP数据子包。<br>
	 * 这个情况发生在客户端以子集包发送的过程中，某一个子包在发送过程中丢失，服务端判断到丢失，要求客户端再次发送。
	 * 
	 * @param remote 目标站点地址
	 * @param packetId 大包编号
	 * @param serials 子包序号集
	 */
	private void sendRetryPacket(SocketHost remote, int packetId, List<Integer> serials) throws FixpProtocolException {
		int end = serials.size();
		for (int seek = 0; seek < end;) {
			// 截取一段子包序号
			int size = Laxkit.limit(seek, end, PacketBucket.RETRY_SERIALS);
			List<Integer> subs = serials.subList(seek, seek + size);
			// 移到下标
			seek += size;
			// 生成数据包
			Mark mark = new Mark(Ask.NOTIFY, Ask.RETRY_SUBPACKET);
			Packet packet = new Packet(remote, mark);
			packet.addMessage(MessageKey.PACKET_IDENTIFY, packetId); // 包编号
			for (int index : subs) {
				packet.addMessage(MessageKey.SUBPACKET_SERIAL, index); // 子包序号，从0开始
			}
			// 发送数据包
			send(packet);
		}
	}

	/**
	 * 检查已经失效的请求包组
	 * @return 返回清除的FIXP包组数目
	 */
	private int checkDisableRequestBucket() {
		int size = requests.size();
		if (size == 0) {
			return 0;
		}

		ArrayList<SocketHost> array = new ArrayList<SocketHost>(size);

		// 枚举客户端的请求包
		super.lockSingle();
		try {
			Iterator<Map.Entry<SocketHost, PacketBucket>> iterator = requests.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, PacketBucket> entry = iterator.next();
				// 判断失效超时
				if (entry.getValue().isDisableTimeout()) {
					array.add(entry.getKey());
				}
			}
			for (SocketHost remote : array) {
				requests.remove(remote);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return array.size();
	}

	/**
	 * 检查已经失效的服务端发送包
	 * @return 返回清除的FIXP包组数目
	 */
	private int checkDisableReponseBucket() {
		int size = resps.size();
		if (size == 0) {
			return 0;
		}

		ArrayList<SocketHost> array = new ArrayList<SocketHost>(size);

		// 枚举服务端的发送包
		super.lockSingle();
		try {
			Iterator<Map.Entry<SocketHost, PacketBucket>> iterator = resps.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, PacketBucket> entry = iterator.next();
				// 判断失效超时
				if (entry.getValue().isDisableTimeout()) {
					array.add(entry.getKey());
				}
			}
			for (SocketHost remote : array) {
				resps.remove(remote);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return array.size();
	}

	/**
	 * 检查重传的数据请包
	 * @return 返回重次的包集数目
	 */
	private int checkRetryRquestBucket() {
		// 如果是空集合，退出
		int size = requests.size();
		if(size == 0) {
			return 0;
		}

		int count = 0;

		// 锁定处理
		super.lockSingle();
		try {
			Iterator<Map.Entry<SocketHost, PacketBucket>> iterator = requests.entrySet().iterator();
			// 循环处理
			while (iterator.hasNext()) {
				// 下一个包
				Map.Entry<SocketHost, PacketBucket> entry = iterator.next();
				PacketBucket bucket = entry.getValue();

				// 子包更新超时
				if (bucket.isSubPacketTimeout()) {
					bucket.refreshSubPacketTime(); // 更新子包时间
				}
				// 发生断裂，且超时1秒时...
				else if (bucket.isBreak() && bucket.isRetryTimeout(1000)) {
					bucket.refreshRetryTime(); // 更新重试时间
				}
				// 不达到以上条件，忽略...
				else {
					continue;
				}

				// 要求客户端重新发送子包
				List<Integer> serials = bucket.getMissSerials();
				SocketHost remote = entry.getKey();
				sendRetryPacket(remote, bucket.getPacketId(), serials);
				count++;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return count;
	}

	/**
	 * 删除密文。<br>
	 * 删除密文必须在加密情况下进行，为防止黑客恶意操作（RSA泄露会发生此情况），要求数据来源和被删除地址一致。
	 * 
	 * @param request 请求包
	 */
	private void dropSecure(Packet request) {
		SocketHost remote = request.getRemote();
		// 取出字节数组
		byte[] b = request.getData();

		// 用可类化解析参数
		ClassReader reader = new ClassReader(b);
		SocketHost host = new SocketHost(reader); // 注册地址
		Cipher cipher = new Cipher(reader);	// 注册的密文

		// 判断地址和注册地址一致
		boolean success = (remote.getAddress().compareTo(host.getAddress()) == 0);
		// 如果不匹配，可能是黑客行为，忽略它。
		if (!success) {
			Logger.warning(this, "dropSecure", "illegal drop secure, from %s:%s", remote, host);
			return;
		}

		// 查找内存中的密文
		Cipher memory = findCipher(host);
		// 判断密文一致
		success = (memory != null && cipher.compareTo(memory) == 0);
		// 删除内存中的地址/密文对
		if (success) {
			removeCipher(host);
		}
		// 反馈结果给客户端
		Packet resp = new Packet(remote, (success ? Answer.SECURE_ACCEPTED : Answer.SECURE_REFUSE));
		resp.addMessage(MessageKey.SPEAK, (success ? "DROP SECURE, OK!" : "NOT FOUND SECURE"));
		// 反馈结果
		send(resp);

		Logger.note(this, "dropSecure", success,
				"remote:[%s], cipher host:[%s], cipher type:%s", 
				remote, host, cipher.getFamilyText());
	}
	
	/**
	 * 建立安全密文服务。此操作数据包是非加密状态，但是包中数据用RSA加密。<br><br>
	 * 
	 * 服务器<b>“解密和保存”</b>客户机的安全初始化RSA密文。<br>
	 * 请求包数据，已经被客户端用RSA公钥加密，这里做为服务器，用RSA私钥解密，然后保存密文到内存。<br>
	 * 
	 * @param request 请求包
	 */
	private void createSecure(Packet request) {
		// 如果地址已经存在，返回一个错误提示
		SocketHost remote = request.getRemote();
		if (hasCipher(remote)) {
			Packet resp = new Packet(remote, Answer.SECURE_EXISTED);
			send(resp);
			return;
		}

		// RSA私钥解密
		short code = 0;
		Cipher cipher = null;
		byte[] data = request.getData();
		if (data != null) {
			try {
				cipher = decryptCipher(remote, data, 0, data.length);
			} catch (SecureException e) {
				code = Answer.SECURE_REFUSE;
				Logger.error(e);
			} catch (Throwable e) {
				code = Answer.SECURE_REFUSE;
				Logger.fatal(e);
			}
		}

		if (code == 0) {
			code = (cipher != null ? Answer.SECURE_ACCEPTED : Answer.SECURE_REFUSE);
		}

		Packet resp = new Packet(remote, code);
		if (code == Answer.SECURE_ACCEPTED) {
			resp.addMessage(MessageKey.SPEAK, "CREATE SECURY, OK!");
		} else {
			resp.addMessage(MessageKey.SPEAK, "CANNOT BE CREATE SECURY!");
		}

		// 返回数据包给客户端
		send(resp, 1);
		// 保存配置
		addCipher(remote, cipher);
	}

	/**
	 * 根据客户机来源地址，找到它关联的服务器私钥令牌，对已经加密的密文进行解密，返回原始数据
	 * @param remote 来源地址
	 * @param b  FIXP密文字节数组
	 * @param off 密文开始下标
	 * @param len 密文长度
	 * @return 解密后的Cipher实例
	 * @throws SecureException
	 */
	private Cipher decryptCipher(SocketHost remote, byte[] b, int off, int len) throws SecureException {
		Address client = remote.getAddress();
		SecureToken token = SecureController.getInstance().find(client);
		if (token == null) {
			throw new SecureException(
					"cannot be find secure token, from %s!", client);
		}
		// 用RSA私钥解密
		Cipher cipher = new Cipher();
		cipher.decase(token.getServerKey().getKey(), b, off, len);
		return cipher;
	}

	/**
	 * 对应客户端的“Ask.NOTIFY, Ask.EXIT”命令， 客户端要求退出。
	 * 本处服务器收到这个请求后，向客户机返回“再见”，并且删除本地保存的密钥。
	 */
	private void exit(Packet request) {
		Integer packetId = request.findInteger(MessageKey.PACKET_IDENTIFY);

		// 单向处理，不返回通知结果
		Boolean b = request.findBoolean(MessageKey.DIRECT_NOTIFY);
		boolean direct = (b != null && b.booleanValue());

		// 来源地址
		SocketHost remote = request.getRemote();

		// 如果不是单向处理，发送返回结果
		if (!direct) {
			Packet resp = new Packet(remote, Answer.GOODBYE);
			resp.addMessage(MessageKey.SPEAK, "SEE YOU NEXT TIME!");
			// 如果有包编号
			if (packetId != null) {
				resp.addMessage(MessageKey.SUBPACKET_COUNT, 1);
				resp.addMessage(MessageKey.SUBPACKET_SERIAL, 0);
				resp.addMessage(MessageKey.PACKET_IDENTIFY, packetId);
				resp.addMessage(MessageKey.SUBPACKET_TIMEOUT, 3000);
			}
			// 返回结果！
			send(resp, 1);
		}

		// 释放密文（如果服务器启动密文通信的情况下）
		removeCipher(remote);
	}

	/**
	 * 反馈测试结果，是无条件接受！
	 * @param request 请求包
	 */
	private void test(Packet request) {
		// 取来源地址
		SocketHost remote = request.getRemote();
		// 生成应答包包
		Packet resp = new Packet(remote, Answer.ACCEPTED);
		resp.addMessage(MessageKey.SPEAK, "RE:TEST!");
		// 记录来源主机地址，返回给请求端
		byte[] b = remote.build();
		resp.addMessage(MessageKey.HOST, b);
		// 发送请求包
		send(resp);

		//		Logger.debug(this, "test", "from %s", remote);
	}

	/**
	 * 向客户端反馈它的地址
	 * @param request 请求包
	 */
	private void reflect(Packet request) {
		// 取来源地址
		SocketHost remote = request.getRemote();
		// 取出来源主机地址
		byte[] b = request.findRaw(MessageKey.HOST);

		// 生成应答包包
		Packet resp = new Packet(remote, Answer.REFLECT_ACCEPTED);
		resp.addMessage(MessageKey.HOST, b); // 原样返回
		resp.addMessage(MessageKey.SPEAK, "re:reflect!");

		// 把客户端地址返送回去
		resp.setData(remote.build());
		// 发送请求包
		send(resp);

		Logger.debug(this, "reflect", "from %s", remote);
	}

	/**
	 * 向客户端反馈它内网的出口IP地址和端口
	 * @param request 请求包
	 */
	private void shine(Packet request) {
		// 来源地址
		SocketHost remote = request.getRemote();
		// 取出来源主机地址
		byte[] b = request.findRaw(MessageKey.HOST);

		// 生成应答包
		Packet resp = new Packet(remote, Answer.SHINE_ACCEPTED);
		resp.addMessage(MessageKey.HOST, b); // 原样返回
		resp.addMessage(MessageKey.SPEAK, "re:shine!");
		// 把来源客户端地址返送回去
		resp.setData(remote.build());
		// 发送应答包
		send(resp);

		Logger.debug(this, "shine", "from %s", remote);
	}

	/**
	 * 通知客户机，由于没有遵守安全通信规则，要求客户机按照服务器规定的安全验证要求进行连接和通信。<br>
	 * 服务器的安全验证选项包括：地址验证、密文验证、地址/密文复合验证。
	 * 
	 * @param request 请求包
	 */
	private void notifySecure(Packet request) throws SecureException {
		SocketHost client = request.getRemote();
		Address address = client.getAddress();
		// 查找RSA公钥，提供给客户机
		PublicSecure token = SecureController.getInstance().findPublicSecure(address);
		if (token == null) {			
			Logger.error(this, "notifySecure",
					"cannot be find security-token, check security configure, from %s", client);
			return;
		}

		// 反馈的数据包
		Packet resp = new Packet(client, Answer.SECURE_NOTIFY);
//		resp.addMessage(MessageKey.SECURE_FAMILY, token.getFamily());
		resp.setData(token.build());

		// 发送给客户机，只一次！
		send(resp, 1);
	}

	/**
	 * 要求加密数据内容
	 * @param client
	 * @throws SecureException
	 */
	private void notifyEncryptContent(SocketHost client) throws SecureException {
		Address address = client.getAddress();
		PublicSecure token = SecureController.getInstance().findPublicSecure(address);
		if (token == null) {
			Logger.error(this, "notifyEncryptContent",
					"cannot be find security-token, check security configure, from %s", client);
			return;
		}

		// 反馈的数据包
		Packet resp = new Packet(client, Answer.ENCRYPT_CONTENT_NOTIFY);
//		resp.addMessage(MessageKey.SECURE_FAMILY, token.getFamily());
		resp.setData(token.build());

		// 发送给客户机
		send(resp);
	}

	/**
	 * 通知客户端，已经受理数据包
	 * @param client 客户端地址
	 * @param packetId 包编号
	 * @throws SecureException
	 */
	private void acceptSubPackets(SocketHost client, int packetId) { // throws FixpProtocolException {
		Mark mark = new Mark(Answer.SUBPACKETS_ACCEPTED);
		Packet resp = new Packet(client, mark);
		resp.addMessage(MessageKey.PACKET_IDENTIFY, packetId); // 包编号

		//		Logger.debug(this, "acceptSubPackets", "发送受理子包集反馈到：%s", client);

		// 发送反馈包，连个3个
		send(resp, 3);
	}

	/**
	 * 释放一个FIXP数据包任务线程。<br>
	 * 这个方法是FIXP数据包任务在退出线程前调用。
	 * 
	 * @param task FIXP数据包任务
	 * @return 成功返回真，否则假
	 */
	protected boolean release(PacketTask task) {
		for (int i = 0; i < tasks.length; i++) {
			if (tasks[i] == task) {
				tasks[i] = null;
				return true;
			}
		}
		return false;
	}

	/**
	 * 重置全部参数，当重新登录时...
	 */
	public void reset() {
		Logger.debug(this, "reset", "hook count: %d, pock count: %d",
				hooks.size(), pocks.size());

		// 清除监视器保存的
		packetMonitor.reset();
		// 清除本地参数
		resetAskSecureHook();
		resetCreateSecureHook();
		resetDropSecureHook();
		resetEchoHook();
		// 清除本地保存的记录
		super.lockSingle();
		try {
			Iterator<Map.Entry<SocketHost, HostHook>> iterator = hooks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, HostHook> entry = iterator.next();
				// 唤醒钩子
				entry.getValue().done();
			}
			// 清除
			hooks.clear();
			pocks.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 再次清除一次，因为上面撤销结果可能会保存进来！
		packetMonitor.reset();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		primitivePackets.clear();

		// 等待释放全部线程，直到判断全部清空后，退出
		while (true) {
			// 统计清空的线程数目
			int count = 0;
			for (int i = 0; i < tasks.length; i++) {
				if (tasks[i] == null) {
					count++;
				}
			}
			// 判断全部清空，退出
			if (count == tasks.length) {
				break;
			}
			// 延时
			delay(500);
		}

		Logger.debug(this, "finish", "Request Packet Size:%d, Reply Packet Size:%d",
				requests.size(), resps.size());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 如果没有定义，设置最少一个！
		if(tasks == null) {
			tasks = new PacketTask[1];
		}
		// 启动子线程
		for (int i = 0; i < tasks.length; i++) {
			tasks[i] = new PacketTask(this);
			tasks[i].start();
			// 延时
			delay(200);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.debug(this, "process", "into...");

		// 内网保持PING通信间隔时间
		PockTimer timer = new PockTimer();

		// 循环
		while (!isInterrupted()) {
			// 原始数据包子处理
			int count = subprocess();
			// 检查超时反馈、请求、重试包
			count += checkDisableReponseBucket();
			count += checkDisableRequestBucket();
			count += checkRetryRquestBucket();

			// 定时检测内网定位，让NAT保存这个端口，保持PING通状态
			if (timer.isTimeout()) {
				count += checkPockTimeout();
				timer.refreshTime();
			}

			// 以上没有发生，延时
			if (count == 0) {
				delay(1000);
			}
		}

		// 退出前，停止全部子线程
		for (int i = 0; i < tasks.length; i++) {
			tasks[i].stop();
		}

		Logger.debug(this, "process", "exit...");
	}

	/**
	 * 建立密文的应答包没有加密，如果加密就忽略它
	 * @param remote 来源地址
	 * @param b 删除前缀符的字节数组
	 */
	private void doReturnCreatePrivateSecure(SocketHost remote, byte[] b) throws FixpProtocolException {
		Cipher cipher = findCipher(remote);
		if (cipher != null) {
			//			Logger.warning(this, "doReturnCreatePrivateSecure", "saved cipher! from %s", remote);
			return;
		}

		// 解析数据包
		Packet packet = new Packet(remote, b, 0, b.length);
		Mark mark = packet.getMark();

		// 判断两种应答结果
		if (Answer.isPrivateCreateSecureAccepted(mark)) {
			doCreatePrivateSecureAccepted(packet);
		} else if (Answer.isPrivateCreateSecureRefuse(mark)) {
			doCreatePrivateSecureRefuse(packet);
		} else {
			Logger.warning(this, "doReturnCreatePrivateSecure", "illegal mark! %s", mark);
		}
	}

	/**
	 * 删除密文的应答包是加密状态，如果没有密钥，就忽略它
	 * @param remote 来源地址
	 * @param b 删除前缀符的字节数组
	 */
	private void doReturnDropPrivateSecure(SocketHost remote, byte[] b) throws FixpProtocolException {
		Cipher cipher = findCipher(remote);
		if (cipher == null) {
			//			Logger.warning(this, "doReturnDropPrivateSecure", "deleted cipher! from %s", remote);
			return;
		}

		// 解密数据！
		try {
			b = cipher.decrypt(b, 0, b.length);
		} catch (SecureException e) {
			Logger.error(e, "from %s", remote);
			return;
		}

		// 解析数据包
		Packet packet = new Packet(remote, b, 0, b.length);
		Mark mark = packet.getMark();

		if (Answer.isPrivateDropSecureAccepted(mark)) {
			doDropPrivateSecureAccepted(packet);
		} else if (Answer.isPrivateDropSecureRefuse(mark)) {
			doDropPrivateSecureRefuse(packet);
		} else {
			//			Logger.warning(this, "doReturnDropPrivateSecure", "illegal mark! %s", mark);
		}
	}

	/**
	 * 处理数据包，每次检查开始确定的量
	 * @return 返回处理的子包数目
	 */
	private int subprocess() {
		int size = primitivePackets.size();
		for (int index = 0; index < size; index++) {
			// 弹出一个辅助包
			PrimitivePacket packet = poll();
			if (packet == null) {
				continue;
			}
			// 来源地址
			SocketHost remote = packet.getRemote();

			/**
			 * 判断是加密或者解密应答
			 * 如果是，去掉前缀符，再交到方法中去判断
			 */
			try {
				byte[] b = packet.getBuffer();
				// 判断是应答
				if (isPrefix(CREATE_SECURE, b)) {
					b = Arrays.copyOfRange(b, CREATE_SECURE.length, b.length);
					doReturnCreatePrivateSecure(remote, b);
					continue;
				} else if (isPrefix(DROP_SECURE, b)) {
					b = Arrays.copyOfRange(b, DROP_SECURE.length, b.length);
					doReturnDropPrivateSecure(remote, b);
					continue;
				}
			} catch (FixpProtocolException e) {
				Logger.error(e, "from %s", remote);
			} catch (Throwable e) {
				Logger.fatal(e, "from %s", remote);
			}

			/** 
			 * 如果有密文，先解密再分析。如果没有直接分析。
			 * 
			 * 如果发生安全/FIXP协议异常，故障源肯定是客户机，为保证服务器安全，删除这个地址的密文。
			 * 故障可能有：
			 * 1. 伪造实际客户机地址通信。
			 * 2. 客户机发生故障，启用了新密文，但是服务器密文仍然是旧的，发生不匹配现象。
			 * 3. 密文已经删除，又发送密文加密包，解密出错误。
			 */
			try {
				// 根据地址查找密文
				Cipher cipher = findCipher(remote);
				if (cipher != null) {
					decrypt(cipher, packet);
				} else {
					undecrypt(packet);
				}
			} catch (FixpProtocolException e) {
				Logger.error(e, "from %s", remote);
			} catch (Throwable e) {
				Logger.fatal(e, "from %s", remote);
			}
		}

		return size;
	}

	/**
	 * 由FixpPacketMonitor操作，保存一个待处理的数据包
	 * @param from 数据来源
	 * @param b 待发送的数据
	 * @param off 数据开始下标
	 * @param len 数据有效长度
	 * @return 保存成功返回真，否则假
	 */
	protected boolean add(SocketHost from, byte[] b, int off, int len) {
		// 生成数据包，防止内存溢出
		PrimitivePacket packet = null;
		try {
			packet = new PrimitivePacket(from, b, off, len);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		if (packet == null) {
			return false;
		}

		boolean success = false;
		boolean empty = false;
		// 锁定保存
		super.lockSingle();
		try {
			empty = primitivePackets.isEmpty();
			success = primitivePackets.add(packet);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 唤醒线程
		if (empty && success) {
			wakeup();
		}
		return success;
	}

	/**
	 * 弹出一个待处理的数据包
	 * @return 返回HelpPacket实例，没有返回空指针
	 */
	private PrimitivePacket poll() {
		super.lockSingle();
		try {
			if (primitivePackets.size() > 0) {
				return primitivePackets.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	/**
	 * 处理一个数据包
	 * @param packet 待处理的数据包
	 */
	private void todo(Packet packet) {
		Mark mark = packet.getMark();

		// 判断它们的类型
		if (Assert.isRetrySubPacket(mark)) {
			sendReplySubPacket(packet);
		} else if (Assert.isCancelPacket(mark)) {
			sendRequestCancelPacket(packet);
		} else if (packet.isSubPacket()) {
			addRequestSubPacket(packet); // 如果是子包，先保存，收齐后再处理
		}
		// 空操作，不做任何处理。保证SOCKET畅通和密钥存在有效！
		else if (Assert.isEmptyOperate(mark)) {
			//	Logger.debug(this, "todo", "收到空包！来自：%s", packet.getRemote());
		}
		// 普通的FIXP UDP包，按照循环分配给各处理线程
		else {
			if (taskIndex >= tasks.length) {
				taskIndex = 0;
			}
			tasks[taskIndex++].add(packet);
		}
	}

	/**
	 * 处理加密的数据包。先把数据内容解密，再做操作判断和处理数据业务。
	 * 
	 * @param cipher FIXP密文
	 * @param help 辅助包
	 * @throws SecureException
	 */
	private void decrypt(Cipher cipher, PrimitivePacket help) throws FixpProtocolException {
		SocketHost remote = help.getRemote();
		// 使用FIXP密文解密数据
		byte[] b = help.getBuffer();

		// 解密数据！
		try {
			b = cipher.decrypt(b, 0, b.length);
		} catch (SecureException e) {
			Logger.error(e, "from %s # %s", remote, cipher);
			return;
		}

		// 解析数据包
		Packet packet = new Packet(remote, b, 0, b.length);
		Mark mark = packet.getMark();

		if (Assert.isExit(mark)) {
			// 收到“退出”命令，这时节点处于服务器状态，先发送“再见”命令，再删除保持的密文
			exit(packet);
		} else if (Assert.isGoodbye(mark)) {
			// 收到“再见”应答反馈，这里节点处于客户机状态。删除密文
			removeCipher(remote);
		} else if(Assert.isSecureDrop(mark)) {
			dropSecure(packet); // 删除旧密文
		}
		// 私有密文的删除和反馈确认（走FixpPacketMonitor -> FixpPacketMonitor信道）
		else if (Assert.isPrivateSecureDrop(mark)) {
			doDropPrivateSecure(packet);
		}
		// 各种检测
		else if (Answer.isShineAccepted(mark)) {
			replyPock(packet); // 外网检测内网的NAT出口地址
		} else if (Assert.isShine(mark)) {
			shine(packet); // 内网请求外网检测NAT出品地址
		} else if (Answer.isReflectAccepted(mark)) {
			recallReflect(packet); // 集群内节点间地址检测反馈
		} else if (Assert.isReflect(mark)) {
			reflect(packet); // 集群内网节点请求检测本地地址
		} else if (Assert.isTest(mark)) {
			test(packet); // 反馈测试包
		}
		// 单包远程调用
		else if (Answer.isVisitAccepted(mark)) {
			recallVisit(packet);
		} else if (Assert.isVisit(mark)) {
			visit(packet);
		}
		// 执行实际业务
		else {
			todo(packet);
		}
	}

	/**
	 * 处理非加密数据包
	 * @param help FIXP辅助包
	 * @throws SecureException
	 */
	private void undecrypt(PrimitivePacket help) throws FixpProtocolException, SecureException {
		// 明文，解析数据
		SocketHost remote = help.getRemote();
		byte[] b = help.getBuffer();

		Packet request = new Packet(remote, b, 0, b.length);
		Mark mark = request.getMark();

		// 当前服务器是加密模型，有来自客户端的非加密业务，判断业务类型，通知客户端加密
		boolean missing = SecureController.getInstance().isCipher(remote.getAddress());
		if (missing) {
			// 各种检测
			boolean refuse = (Assert.isShine(mark) || Assert.isReflect(mark)
					|| Assert.isTest(mark) || Assert.isVisit(mark) || Assert.isRPCall(mark));
			// 条件成立，通知客户端加密处理
			if (refuse) {
//				System.out.printf("我操，拒绝！%s\n", mark.toString());
				notifyEncryptContent(remote);
				return;
			}
		}

		// 建立安全密文包
		if (Assert.isSecureCreate(mark)) {
			createSecure(request);
		}
		// 私有密文的建立和反馈确认，在非加密状态下进行
		else if (Assert.isPrivateSecureCreate(mark)) {
			doCreatePrivateSecure(request);
		}
		// 删除密文，本处忽略
		else if (Assert.isSecureDrop(mark)) {
			Logger.error(this, "undecrypt", "illegal drop secure");
		}
		// 安全查询
		else if (Assert.isSecureQuery(mark)) {
			// 如果是安全询问返回要求加密的应答
			notifySecure(request);
		}
		// 私密安全检查和接受反馈
		else if(Assert.isPrivateSecureQuery(mark)) {
			doQueryPrivateSecure(request);
		} else if(Answer.isPrivateSecureNotify(mark)) {
			doQueryPrivateSecureAccpeted(request);
		} 
		// 收到“退出”命令，这时节点属于服务端，它向客户机发送“再见”应答，同时判断有密文和删除
		else if (Assert.isExit(mark)) {
			exit(request);
		}
		// 收到“再见”应答，这时节点属于客户机，在确认后删除密文
		else if (Assert.isGoodbye(mark)) {
			removeCipher(remote);
		}
		// 各种检测
		else if (Assert.isShine(mark)) {
			shine(request);
		} else if (Answer.isShineAccepted(mark)) {
			replyPock(request);
		} else if (Assert.isReflect(mark)) {
			reflect(request); // 反馈客户的地址
		} else if (Answer.isReflectAccepted(mark)) {
			recallReflect(request);
		} else if (Assert.isTest(mark)) {
			test(request); // 反馈测试包
		}
		// 单包远程调用
		else if (Assert.isVisit(mark)) {
			visit(request);
		} else if (Answer.isVisitAccepted(mark)) {
			recallVisit(request);
		}
		// RPC或者其它操作
		else {
			todo(request);
		}
	}

	/** 以下是内网定位代码 **/

	/** 当前站点启动 **/
	private SiteLauncher siteLauncher;

	/** 内网检测超时，默认是10秒 **/
	private long pockTimeout = 10000;

	/** 网关地址 -> 定位检测钩子 **/
	private Map<SocketHost, HostHook> hooks = new TreeMap<SocketHost, HostHook>();

	/** 网关站点 -> 内网定位定时检测单元 **/
	private Map<SocketHost, PockItem> pocks = new TreeMap<SocketHost, PockItem>();

	/**
	 * 设置站点启动器
	 * @param e 站点启动器
	 */
	public void setSiteLauncher(SiteLauncher e) {
		siteLauncher = e;
	}

	/**
	 * 设置内网检测超时
	 * @param ms
	 */
	public long setPockTimeout(long ms) {
		if (ms >= 10000) {
			pockTimeout = ms;
		}
		return pockTimeout;
	}

	/**
	 * 返回内网检测超时
	 * @return 以毫秒为单位的数值
	 */
	public long getPockTimeout() {
		return pockTimeout;
	}

	/**
	 * 返回内网节点在NAT设备上的定位地址
	 * @param remote 网关站点
	 * @return 返回内网的NAT地址
	 */
	public SocketHost findPockLocal(SocketHost remote) {
		super.lockMulti();
		try {
			PockItem item = pocks.get(remote);
			if (item != null) {
				return item.getLocalNAT();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	/**
	 * 判断有内网定位定时检测单元
	 * @return 返回真或者假
	 */
	public boolean hasPocks() {
		// 锁定
		super.lockMulti();
		try {
			return pocks.size() > 0;
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 保存检测钩子
	 * @param remote 目标地址
	 * @param hook 检测钩子
	 */
	private void addHook(SocketHost remote, HostHook hook) {
		super.lockSingle();
		try {
			hooks.put(remote, hook);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 删除检测钩子
	 * @param remote 目标地址
	 * @return 删除成功返回真，否则假
	 */
	private boolean removeHook(SocketHost remote) {
		super.lockSingle();
		try {
			return (hooks.remove(remote) != null);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 保存内网定位定时检测单元
	 * @param remote 目标地址
	 * @param item 内网定位定时检测单元
	 */
	private void addPock(PockItem item) {
		super.lockSingle();
		try {
			pocks.put(item.getRemote(), item);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 向目标地址发送SHINE通知
	 * @param remote 目标地址
	 * @param count 发送统计
	 * @return 返回发送成功次数
	 */
	private int sendPockPacket(SocketHost remote, int count) {
		Mark mark = new Mark(Ask.NOTIFY, Ask.SHINE);
		Packet request = new Packet(remote, mark);
		request.addMessage(MessageKey.HOST, remote.build());
		request.addMessage(MessageKey.SPEAK, "shine!");

		Logger.debug(this, "sendPockPacket", "to %s", remote);

		// 按照要求发送数据包
		int units = 0;
		for (int i = 0; i < count; i++) {
			boolean success = packetMonitor.notice(request);
			if (success) units++;
		}
		return units;
	}

	/**
	 * 处理返回的内网定位
	 * @param resp 应答包
	 */
	private void replyPock(Packet resp) {
		// 目标方地址
		byte[] b = resp.findRaw(MessageKey.HOST);
		SocketHost remote = new SocketHost(new ClassReader(b));

		// 本地主机，或者NAT设备出口地址
		b = resp.getData();
		// 本地主机
		SocketHost local = new SocketHost(new ClassReader(b));

		// 首先检查钩子，如果没有找定时检测单元
		super.lockSingle();
		try {
			// 钩子必须存在！
			HostHook hook = hooks.get(remote);
			if (hook != null) {
				hook.setLocal(local);
				// 撤销它
				hooks.remove(remote);
			}

			// 查找关联的单元
			PockItem item = pocks.get(remote);

			if (item != null) {
				item.refreshTime();

				// 两种情况：1. 第一次收到返回地址，设置它；2. 地址发生变化后，节点重新注册！
				if (item.getLocalNAT() == null) {
					item.setLocalNAT(local);
					Logger.info(this, "replyPock", "first! local is: %s", local);
				} else if (Laxkit.compareTo(item.getLocalNAT(), local) != 0) {
					item.setLocalNAT(local);
					Logger.warning(this, "replyPock", "replace to %s", local);

					// 通知主线程，重新注册（非立即！）
					siteLauncher.kiss(false);

					//					// 通知重新注册
					//					siteLauncher.logout();
					//					siteLauncher.login();
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "replyPock", "remote: %s, local:%s", remote, local);
	}

	/**
	 * 检测在队列中且超时的内网定位单元
	 * @return 返回处理数目
	 */
	private int checkPockTimeout() {
		// 空集，忽略
		if (pocks.isEmpty()) {
			return 0;
		}

		// 检测超时单元
		ArrayList<SocketHost> deletes = new ArrayList<SocketHost>();
		ArrayList<SocketHost> disables = new ArrayList<SocketHost>();
		ArrayList<SocketHost> timeouts = new ArrayList<SocketHost>();
		// 检测超时
		super.lockMulti();
		try {
			Iterator<Map.Entry<SocketHost, PockItem>> iterator = pocks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, PockItem> entry = iterator.next();
				PockItem item = entry.getValue();
				if (item.isTimeout(pockTimeout * 5)) {
					deletes.add(entry.getKey());
				} else if (item.isTimeout(pockTimeout * 2)) {
					disables.add(entry.getKey());
				} else if (item.isTimeout(pockTimeout)) {
					timeouts.add(entry.getKey());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		// 重新注册
		if (deletes.size() > 0) {
			siteLauncher.kiss();
			return deletes.size();
		}
		// 出现失效，发送3个包
		for(SocketHost remote : disables) {
			sendPockPacket(remote, 3);
		}
		// 发送数据包
		for (SocketHost remote : timeouts) {
			sendPockPacket(remote, 1);
		}

		// 返回处理数目
		return disables.size() + timeouts.size();
	}

	/**
	 * 本地FixpPacketMonitor出口地址，被远程FixpPacketMonitor检测确认
	 * @param remote 目标FixpPacketMonitor主机地址
	 * @param timeout 超时时间，单位：毫秒
	 * @param count 测试次数，前一次不成功，继续下一次
	 * @return 返回经远程FixpPacketMonitor确认的本地FixpPacketMonitor地址
	 */
	public SocketHost checkPock(SocketHost remote, long timeout, int count) {
		for (int i = 0; i < count; i++) {
			SocketHost host = checkPock(remote, timeout);
			if (host != null) {
				return host;
			}
		}
		return null;
	}

	/**
	 * 执行一个本地FixpPakcetMonitor的地址检测
	 * @param remote 目标FixpPacketMonitor主机地址
	 * @param timeout 超时
	 * @return 返回本地FixpPacketMonitor主机地址
	 */
	private SocketHost checkPock(SocketHost remote, long timeout) {
		// 最多等待两分钟
		HostHook hook = new HostHook(timeout);
		addHook(remote, hook);

		// 连续发送3次，防止丢包
		int count = sendPockPacket(remote, 3);

		Logger.debug(this, "checkPock", "send %s, count %d", remote, count);

		// 启动钩子
		boolean success = (count > 0);
		if (success) {
			hook.await();
		}
		// 返回结果后删除....
		removeHook(remote);

		// 返回检测后的本地FixpPacketMonitor地址
		return hook.getLocal();
	}

//	/**
//	 * 内网FIXP UDP服务器定位自己在NAT设备上的地址
//	 * 
//	 * @param remote 网关的FixpPacketMonitor服务器地址
//	 * @param localNAT 本地的FixpPacketMonitor服务器出口地址（NAT地址）
//	 * @param 成功返回真，否则假
//	 */
//	public void addPock(SocketHost remote, SocketHost localNAT) {
//		// 空指针检查
//		Laxkit.nullabled(remote);
//		Laxkit.nullabled(localNAT);
//
//		// 保存到内存
//		PockItem item = new PockItem(remote, localNAT);
//		addPock(item);
//
//		// 发送一个包
//		sendPockPacket(remote, 1);
//
//		Logger.debug(this, "addPock", "save server %s, local NAT %s", remote, localNAT);
//	}

	/**
	 * 内网FIXP UDP服务器定位自己在NAT设备上的地址
	 * 
	 * @param hub 服务器主机地址
	 * @param remote 网关的FixpPacketMonitor服务器地址
	 * @param localNAT 本地的FixpPacketMonitor服务器出口地址（NAT地址）
	 * @param 成功返回真，否则假
	 */
	public void addPock(Node hub, SocketHost remote, SocketHost localNAT) {
		// 空指针检查
		Laxkit.nullabled(remote);
		Laxkit.nullabled(localNAT);

		// 保存到内存
		PockItem item = new PockItem(remote, localNAT);
		item.setHub(hub);
		addPock(item);

		// 发送一个包
		sendPockPacket(remote, 1);

		Logger.debug(this, "addPock", "save server %s, local NAT %s", remote, localNAT);
	}

	
	/**
	 * 删除内网检测单元
	 * @param remote FIXP UDP服务器地址
	 * @return 成功返回真，否则假
	 */
	public boolean removePock(SocketHost remote) {
		// 空指针检查
		Laxkit.nullabled(remote);

		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			PockItem item = pocks.remove(remote);
			success = (item != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "removePock", success, "drop %s", remote);

		return success;
	}

	/**
	 * 判断有内网检测单元
	 * @param remote 远程地址
	 * @return 返回真或者假
	 */
	public boolean hasPock(SocketHost remote) {
		// 空指针检查
		Laxkit.nullabled(remote);

		boolean success = false;
		// 锁定
		super.lockMulti();
		try {
			PockItem item = pocks.get(remote);
			success = (item != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "hasPock", success, "check %s", remote);

		return success;
	}

	/**
	 * 输出全部内网定位单元
	 * @return PockItem数组
	 */
	public List<PockItem> getPocks() {
		ArrayList<PockItem> a = new ArrayList<PockItem>();
		super.lockMulti();
		try {
			Iterator<Map.Entry<SocketHost, PockItem>> iterator = pocks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, PockItem> entry = iterator.next();
				PockItem item = entry.getValue();
				a.add(item.duplicate());
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return a;
	}

	/**
	 * 输出全部内网定位单元
	 * @return PockItem数组
	 */
	public PockItem[] getPockItems() {
		List<PockItem> list = getPocks();
		PockItem[] a = new PockItem[list.size()];
		return list.toArray(a);
	}

	/**
	 * 处理服务端返回的结果
	 * @param resp 应答包
	 */
	private void recallReflect(Packet resp) {
		// 目标方地址
		byte[] b = resp.findRaw(MessageKey.HOST);
		SocketHost remote = new SocketHost(new ClassReader(b));

		// 本地主机
		b = resp.getData();
		SocketHost local= new SocketHost(new ClassReader(b));

		// 首先检查钩子，如果没有找定时检测单元
		super.lockSingle();
		try {
			// 钩子必须存在！
			HostHook hook = hooks.get(remote);
			if (hook != null) {
				hook.setLocal(local);
				// 撤销它
				hooks.remove(remote);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "recallReflect", "remote: %s, local:%s", remote, local);
	}

	/**
	 * 以客户机的身份，向服务端发送检测包，并且等待和返回结果
	 * @param remote 服务器地址
	 * @param timeout 超时时间
	 * @return 返回当前服务器的地址，失败返回空指针
	 */
	public SocketHost checkReflect(SocketHost remote, long timeout) {
		// 最多等待两分钟
		HostHook hook = new HostHook(timeout);
		addHook(remote, hook);

		Mark mark = new Mark(Ask.NOTIFY, Ask.REFLECT);
		Packet request = new Packet(remote, mark);
		byte[] b = remote.build(); // 目标主机地址
		request.addMessage(MessageKey.HOST, b);
		request.addMessage(MessageKey.SPEAK, "reflect!");

		Logger.debug(this, "checkReflect", "begin! send to %s, timeout:%d ms", remote, timeout);

		// 连续发送3次，防止丢包
		int count = 0;
		for (int i = 0; i < 3; i++) {
			boolean success = packetMonitor.notice(request);
			if (success) count++;
		}
		// 判断成功
		boolean success = (count > 0);

		Logger.note(this, "checkReflect", success, "end! send to %s, timeout:%d ms, count %d", remote, timeout, count);

		// 启动钩子
		if (success) {
			hook.await();
		}
		// 返回结果后删除....
		removeHook(remote);

		// 返回检测后的本地FixpPacketMonitor地址
		return hook.getLocal();
	}

	/** 以下是代用__notice信道，执行RPC VISIT远程访问 **/


	/** 编号 -> 异步钩子 **/
	private Map<Long, EchoHook> echoHooks = new TreeMap<Long , EchoHook>();

	/**
	 * 向客户端反馈它内网的出口IP地址和端口
	 * @param request 请求包
	 */
	protected Packet callVisit(Packet request) {
		// 来源地址
		SocketHost remote = request.getRemote();
		// 取出序列号，必须保证唯一性！
		Long serial = request.findLong(MessageKey.SERIAL_NUMBER);
		if (serial == null) {
			Logger.error(this, "callVisit", "cannot be find serial number!");
			return null;
		}

		// 从包中取出数据，交给本地接口处理，返回处理后的字节数组
		byte[] data = callRPCLocal(request.getData());

		Logger.debug(this, "callVisit", "from %s", remote);

		// 生成应答包包
		Packet resp = new Packet(remote, Answer.VISIT_ACCEPTED);
		resp.addMessage(MessageKey.SERIAL_NUMBER, serial); // 原样返回
		// 把处理的数据生成字节数组
		resp.setData(data);
		return resp;
	}

	/**
	 * 交给线程队列去处理
	 * @param request 请求包
	 */
	private void visit(Packet request) {
		if (taskIndex >= tasks.length) {
			taskIndex = 0;
		}
		tasks[taskIndex++].add(request);
	}

	/**
	 * 处理VISIT命令应答，数据包来自“visit”函数。
	 * @param resp 应答包
	 */
	private void recallVisit(Packet resp) {
		// 目标方地址
		Long serial = resp.findLong(MessageKey.SERIAL_NUMBER);
		if (serial == null) {
			Logger.error(this, "recallVisit", "cannot be find serial number!");
			return;
		}

		// 根据标记找到钩子，把包传给它并且唤醒！
		super.lockSingle();
		try {
			EchoHook hook = echoHooks.get(serial);
			if (hook != null) {
				// 设置返回数据包
				hook.setPacket(resp);
				// 删除内存记录！
				echoHooks.remove(serial);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "recallVisit", "from %s", resp.getRemote());
	}

	/**
	 * 重置钩子
	 */
	private void resetEchoHook() {
		super.lockSingle();
		try {
			Iterator<Map.Entry<Long, EchoHook>> iterator = echoHooks.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<Long, EchoHook> entry = iterator.next();
				entry.getValue().done();
			}
			createSecures.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 保存检测钩子
	 * @param serial 序列号
	 * @param hook 检测钩子
	 */
	private void addHook(long serial, EchoHook hook) {
		super.lockSingle();
		try {
			echoHooks.put(serial, hook);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 删除检测钩子
	 * @param serial 序列号
	 * @return 删除成功返回真，否则假
	 */
	private boolean removeHook(long serial) {
		super.lockSingle();
		try {
			return (echoHooks.remove(serial) != null);
		} finally {
			super.unlockSingle();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.monitor.EchoMessenger#mailing(com.laxcus.fixp.Packet, long)
	 */
	@Override
	public Packet mailing(Packet request, long timeout) {
		// 生成一个序列号，这个标识是证明身份的唯一凭证
		long serial = generator.nextSerial();
		// 保存这个序列号，且是唯一的存在！
		request.replaceMessage(MessageKey.SERIAL_NUMBER, serial);

		// 生成命令钩子
		EchoHook hook = new EchoHook(timeout);
		// 保存它
		addHook(serial, hook);

		// 向目标站点的Fixp Packet Monitor 发送数据包，一个！
		boolean success = packetMonitor.notice(request);

		Logger.debug(this, "mailing", success, "send to %s, timeout:%d ms", request.getRemote(), hook.getTimeout());

		// 启动钩子
		if (success) {
			hook.await();
		}
		// 返回结果后删除....
		removeHook(serial);

		// 返回服务端反馈的应答包
		return hook.getPacket();
	}

	/** 以下是私有安全查询，通过FixpPacketMonitor信道产生 **/

	/** 编号 -> 异步钩子 **/
	private Map<Long, AskSecureHook> askSecures = new TreeMap<Long , AskSecureHook>();

	/**
	 * 重置命令钩子
	 */
	private void resetAskSecureHook() {
		super.lockSingle();
		try {
			Iterator<Map.Entry<Long, AskSecureHook>> iterator = askSecures.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<Long, AskSecureHook> entry = iterator.next();
				entry.getValue().interrupt();
			}
			askSecures.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 保存检测钩子
	 * @param serial 序列号
	 * @param hook 检测钩子
	 */
	private void addAskSecureHook(long serial, AskSecureHook hook) {
		super.lockSingle();
		try {
			askSecures.put(serial, hook);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 删除检测钩子
	 * @param serial 序列号
	 * @return 删除成功返回真，否则假
	 */
	private boolean removeAskSecureHook(long serial) {
		super.lockSingle();
		try {
			return (askSecures.remove(serial) != null);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 通知客户机，由于没有遵守安全通信规则，要求客户机按照服务器规定的安全验证要求进行连接和通信。<br>
	 * 服务器的安全验证选项包括：地址验证、密文验证、地址/密文复合验证。
	 * 
	 * @param client 客户机地址
	 */
	private void doQueryPrivateSecure(Packet request) throws SecureException {
		Long serial = request.findLong(MessageKey.SERIAL_NUMBER);
		if (serial == null) {
			Logger.error(this, "doQueryPrivateSecure", "cannot be find serial number!");
			return;
		}

		// 客户端主机地址
		SocketHost client = request.getRemote();
		Address address = client.getAddress();
		// 根据地址找到对应的本地密文类型
		PublicSecure token = SecureController.getInstance().findPublicSecure(address);
		// 如果没有找到，不返回结果，让客户机超时后退出！
		if (token == null) {
			Logger.error(this, "doQueryPrivateSecure",
					"cannot be find security-token, check security configure, from %s", client);
			return;
		}

		// 反馈的数据包
		Packet resp = new Packet(client, Answer.PRIVATE_SECURE_NOTIFY);
//		resp.addMessage(MessageKey.SECURE_FAMILY, token.getFamily());	// 密文类型
		resp.addMessage(MessageKey.SERIAL_NUMBER, serial); 	// 序列号
		resp.setData(token.build());

		Logger.debug(this, "doQueryPrivateSecure",
				"current site secure type: %s, send to client %s",
				SecureType.translate(token.getFamily()), client);

		// 发送给请求端的FixpPacketMonitor
		send(resp);
	}

	/**
	 * 处理返回的应答
	 * @param resp
	 */
	private void doQueryPrivateSecureAccpeted(Packet resp) {
		Long serial = resp.findLong(MessageKey.SERIAL_NUMBER);
		if (serial == null) {
			Logger.error(this, "doQueryPrivateSecureAccpeted", "cannot be find serial number!");
			return;
		}

		// 解析数据
		PublicSecure key = null;
		try {
			byte[] b = resp.getData();
			if (b != null && b.length > 0) {
				key = new PublicSecure(b);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		}

		// 锁定处理
		super.lockSingle();
		try {
			AskSecureHook hook = askSecures.get(serial);
			if (hook != null) {
				hook.setSecure(key);
				// 从内存删除它
				askSecures.remove(serial);
			}
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}

		Logger.info(this, "doQueryPrivateSecureAccpeted", "server %s type %s",
				resp.getRemote(), SecureType.translate(key != null ? key.getFamily() : SecureType.INVALID));
	}

	/**
	 * 被SecureTrustor调用，通过FixpPacketMonitor信道，查询服务端的加密类型
	 * @param remote 远程服务器地址
	 * @param timeout 超时时间
	 * @return 返回服务器保存的客户机公钥
	 */
	private PublicSecure queryPrivateSecure(SocketHost remote, long timeout) {
		// 产生一个系列号，做为数据包的身份标识
		long serial = generator.nextSerial();

		// 连续检测3次
		final int count = 3;
		long subTimeout = timeout / count;
		if (subTimeout < 10000) subTimeout = 10000; // 最少10秒

		// 连续3次
		for (int i = 0; i < count; i++) {
			Logger.debug(this, "queryPrivateSecure", "index %d, check %s, hook timeout:%d ms", 
					i + 1, remote, subTimeout);

			// 生成钩子
			AskSecureHook hook = new AskSecureHook(subTimeout);
			// 保存它
			addAskSecureHook(serial, hook);

			// 生成请求数据包
			Mark mark = new Mark(Ask.NOTIFY, Ask.PRIVATE_SECURE_QUERY);
			Packet request = new Packet(remote, mark);
			// 序列号
			request.addMessage(MessageKey.SERIAL_NUMBER, serial);
			// 利用FixpPacketMonitor信道发送出去，让内网NAT里的节点可以收到请求包！这一点非常重要！！！
			send(request);

			// 钩子等待，直到超时...
			hook.await();

			// 返回结果后删除....
			removeAskSecureHook(serial);

			// 判断是强制中断
			if (hook.isInterrupted()) {
				break;
			}

			// 判断有效，返回服务器提供的RSA公钥
			PublicSecure secure = hook.getSecure();
			if (secure != null) {
				return secure;
			}

			//			// 是有效的类型，返回结果
			//			int family = hook.getFamily();
			//			if (SecureType.isFamily(family)) {
			//				return family;
			//			}
		}

		// 结果无效
		return null;
	}


	/**
	 * 被SecureTrustor调用，通过FixpPacketMonitor信道，查询服务端的加密类型
	 * @param remote 远程服务器地址
	 * @param timeout 超时时间
	 * @return 返回服务器提供的RSA公钥和加密类型
	 */
	protected PublicSecure askPrivateSecure(SocketHost remote, long timeout) {
		// 发送命令，检查服务端加密类型
		return queryPrivateSecure(remote, timeout);

		// if (SecureType.isFamily(family)) {
		// return family;
		// }
		// return SecureType.INVALID;
	}


	/** 建立密文前缀，它们不加密 **/
	private byte[] CREATE_SECURE = new byte[] { (byte) 0x0, (byte) 0x11,
			(byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66,
			(byte) 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB,
			(byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };

	/** 删除密文前缀，它们不加密 **/
	private byte[] DROP_SECURE = new byte[] { (byte) 0xFF, (byte) 0xEE,
			(byte) 0xDD, (byte) 0xCC, (byte) 0xBB, (byte) 0xAA, (byte) 0x99,
			(byte) 0x88, (byte) 0x77, (byte) 0x66, (byte) 0x55, (byte) 0x44,
			(byte) 0x33, (byte) 0x22, (byte) 0x11, (byte) 0x0 };

	/**
	 * 判断匹配前缀字符
	 * @param prefix 前缀符
	 * @param src 源字节数组
	 * @return 返回真或者假
	 */
	private boolean isPrefix(byte[] prefix, byte[] src) {
		boolean success = (src != null && src.length >= prefix.length);
		if (success) {
			int i = 0;
			for (; i < prefix.length; i++) {
				if (src[i] != prefix[i]) break;
			}
			// 达到最后，是匹配
			success = (i == prefix.length);
		}
		return success;
	}

	/**
	 * 发送密文应答包，根据密文存在判断是否加密
	 * @param prefix 前缀符
	 * @param resp 应答包
	 * @return 发送成功返回真，否则假
	 */
	private boolean sendReturnSecure(byte[] prefix, Packet resp) {
		SocketHost remote = resp.getRemote();
		byte[] b = resp.build();

		// 找到密文
		Cipher cipher = findCipher(remote);
		if (cipher != null) {
			try {
				b = cipher.encrypt(b, 0, b.length);
			} catch (SecureException e) {
				Logger.error(e);
				return false;
			}
		}

		// 输出字符组合字节数组
		ClassWriter writer = new ClassWriter();
		writer.write(prefix, 0, prefix.length);
		writer.write(b, 0, b.length);
		b = writer.effuse();

		// 跳过加密接口，直接通过SOCKET发送
		int count = packetMonitor.sendTo(remote, b, 0, b.length, 5);
		return (count > 0);
	}

	/** 以下删除私有密文的相关方法 **/

	/** 编号 -> 异步钩子 **/
	private Map<Long, DropSecureHook> dropSecures = new TreeMap<Long , DropSecureHook>();

	/**
	 * 重置命令钩子
	 */
	private void resetDropSecureHook() {
		super.lockSingle();
		try {
			Iterator<Map.Entry<Long, DropSecureHook>> iterator = dropSecures.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<Long, DropSecureHook> entry = iterator.next();
				entry.getValue().interrupt(); // 强制中止
			}
			createSecures.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 中断指定节点的命令钩子
	 * @param remote 目标地址
	 * @return 返回中止数目
	 */
	public int stopDropSecureHook(SocketHost remote) {
		ArrayList<Long> array = new ArrayList<Long>();
		// 锁定处理
		super.lockSingle();
		try {
			Iterator<Map.Entry<Long, DropSecureHook>> iterator = dropSecures.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Long, DropSecureHook> entry = iterator.next();
				DropSecureHook hook = entry.getValue();
				if (Laxkit.compareTo(hook.getRemote(), remote) == 0) {
					array.add(entry.getKey());
				}
			}
			for (long serial : array) {
				DropSecureHook next = dropSecures.get(serial);
				next.interrupt(); // 强制中止
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.info(this, "stopDropSecureHook", "stop count: %d", array.size());

		// 返回释放数目
		return array.size();
	}

	/**
	 * 保存删除钩子
	 * @param serial 序列号
	 * @param hook 删除钩子
	 */
	private void addDropSecureHook(long serial, DropSecureHook hook) {
		super.lockSingle();
		try {
			dropSecures.put(serial, hook);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 删除删除钩子
	 * @param serial 序列号
	 * @return 删除成功返回真，否则假
	 */
	private boolean removeDropSecureHook(long serial) {
		super.lockSingle();
		try {
			return (dropSecures.remove(serial) != null);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 更新密文。
	 * 更新密文必须在加密情况下进行，为了防止黑客等恶意行为，要求数据来源和被替换地址一致，内存中的密文和客户机发来的旧密文一致。
	 * 
	 * @param request 请求包
	 */
	private void doDropPrivateSecure(Packet request) {
		// 查找序列号
		Long serial = request.findLong(MessageKey.SERIAL_NUMBER);
		if (serial == null) {
			Logger.error(this, "doDropPrivateSecure", "cannot be find serial number!");
			return;
		}

		// FixpPacketMonitor来源地址！
		SocketHost remote = request.getRemote();

		// 取出字节数组，用可类化解析参数
		byte[] b = request.getData();
		ClassReader reader = new ClassReader(b);
		Cipher cipher = new Cipher(reader);	// 注册的密文

		// 查找内存中的密文
		Cipher memory = findCipher(remote);
		// 判断密文一致
		boolean success = (memory != null && cipher.compareTo(memory) == 0);

		// 反馈结果给客户端
		short code = (success ? Answer.PRIVATE_DROP_SECURE_ACCEPTED : Answer.PRIVATE_DROP_SECURE_REFUSE);
		Packet resp = new Packet(remote, code);
		resp.addMessage(MessageKey.SERIAL_NUMBER, serial);
		// 发送返回包，此时仍然处于加密状态，数据包要经过加密后才发送
		success = sendReturnSecure(DROP_SECURE, resp);

		// 应答仍然要加密后发送，因为客户端在确认前，仍然是加密状态！
		if (success) {
			removeCipher(remote);
		}

		if (success) {
			Logger.info(this, "doDropPrivateSecure", "from [%s], cipher %s",
					remote, cipher);
		} else {
			//			Logger.warning(this, "doDropPrivateSecure", "from [%s], cipher %s",remote, cipher);
		}
	}

	/**
	 * 处理删除密文接受的反馈包
	 * @param resp 反馈包
	 */
	private void doDropPrivateSecureAccepted(Packet resp) {
		// 查找序列号
		Long serial = resp.findLong(MessageKey.SERIAL_NUMBER);
		if (serial == null) {
			Logger.error(this, "doDropPrivateSecureAccepted", "cannot be find serial number!");
			return;
		}

		boolean success = false;
		// 锁定处理
		super.lockSingle();
		try {
			DropSecureHook hook = dropSecures.get(serial);
			success = (hook != null);
			if (success) {
				hook.setSuccessful(true);
				// 从内存删除它
				dropSecures.remove(serial);
			}
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}

		// 提示
		if (success) {
			Logger.info(this, "doDropPrivateSecureAccepted", "from %s", resp.getRemote());
		} else {
			// Logger.warning(this, "doDropPrivateSecureAccepted", "from %s", resp.getRemote());
		}
	}

	/**
	 * 处理删除密文拒绝的反馈包
	 * @param resp 反馈包
	 */
	private void doDropPrivateSecureRefuse(Packet resp) {
		// 查找序列号
		Long serial = resp.findLong(MessageKey.SERIAL_NUMBER);
		if (serial == null) {
			Logger.error(this, "doDropPrivateSecureRefuse", "cannot be find serial number!");
			return;
		}

		boolean success = false;
		// 锁定处理
		super.lockSingle();
		try {
			DropSecureHook hook = dropSecures.get(serial);
			success = (hook != null);
			if (success) {
				hook.setSuccessful(false);
				// 从内存删除它
				dropSecures.remove(serial);
			}
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}

		if (success) {
			Logger.info(this, "doDropPrivateSecureRefuse", "from %s", resp.getRemote());
		} else {
			//			Logger.warning(this, "doDropPrivateSecureRefuse", "from %s", resp.getRemote());
		}
	}

	/**
	 * 删除私有密文
	 * @param remote
	 * @param serial
	 * @param cipher
	 * @return
	 */
	private boolean __dropPrivateSecure(SocketHost remote, long serial, Cipher cipher) {
		// 可类化数据
		ClassWriter writer = new ClassWriter();
		writer.writeObject(cipher);
		byte[] b = writer.effuse();

		// 撤销加密安全数据包
		Mark mark = new Mark(Ask.NOTIFY, Ask.PRIVATE_SECURE_DROP);

		Packet packet = new Packet(remote, mark);
		packet.addMessage(MessageKey.SERIAL_NUMBER, serial);
		packet.setData(b);
		// 发送数据包
		return send(packet);
	}

	/**
	 * 走私密信道，删除密文
	 * @param remote 目标FixpPacketMonitor地址
	 * @param timeout 超时时间，单位：毫秒
	 * @param count 如果不成功，连续重试次数，最少1次
	 * @param cipher 密文
	 * @return 成功返回真，否则假
	 */
	public boolean dropPrivateSecure(SocketHost remote, long timeout, int count, Cipher cipher) {
		// 如果少于一次，最小一次！
		if (count < 1) {
			count = 1;
		}
		// 生成序列号
		long serial = generator.nextSerial();

		// 删除
		boolean success = false;
		for (int i = 0; i < count; i++) {
			DropSecureHook hook = new DropSecureHook(remote, serial, timeout);
			// 保存钩子
			addDropSecureHook(serial, hook);

			// 生成数据包，发送到目标站点
			boolean sended = __dropPrivateSecure(remote, serial, cipher);
			if (sended) {
				hook.await();
			}
			// 删除钩子
			removeDropSecureHook(serial);

			// 判断被强制中止，无条件退出
			if (hook.isInterrupted()) {
				break;
			}

			// 判断接收应答后的触发，返回处理结果
			if (hook.isTouched()) {
				success = hook.isSuccessful();
				break;
			}
		}

		// 确认服务器删除成功，才能删除本地保存的密文
		if (success) {
			packetMonitor.removeCipher(remote);
		}

		// 提示
		Logger.note(this, "dropPrivateSecure", success, "remote %s, cipher %s", remote, cipher);

		return success;		
	}

	/** 建立私有密文 **/

	/** 编号 -> 异步钩子 **/
	private Map<Long, CreateSecureHook> createSecures = new TreeMap<Long , CreateSecureHook>();

	/**
	 * 重置命令钩子
	 */
	private void resetCreateSecureHook() {
		super.lockSingle();
		try {
			Iterator<Map.Entry<Long, CreateSecureHook>> iterator = createSecures.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<Long, CreateSecureHook> entry = iterator.next();
				entry.getValue().interrupt(); // 强制中止 
			}
			createSecures.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 保存建立钩子
	 * @param serial 序列号
	 * @param hook 建立钩子
	 */
	private void addCreateSecureHook(long serial, CreateSecureHook hook) {
		super.lockSingle();
		try {
			createSecures.put(serial, hook);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 建立建立钩子
	 * @param serial 序列号
	 * @return 建立成功返回真，否则假
	 */
	private boolean removeCreateSecureHook(long serial) {
		super.lockSingle();
		try {
			return (createSecures.remove(serial) != null);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 建立安全密文服务。此操作数据包是非加密状态，但是包中数据用RSA加密。<br><br>
	 * 
	 * 服务器<b>“解密和保存”</b>客户机的安全初始化RSA密文。<br>
	 * 请求包数据，已经被客户端用RSA公钥加密，这里做为服务器，用RSA私钥解密，然后保存密文到内存。<br>
	 * 
	 * @param request 请求包
	 */
	private void doCreatePrivateSecure(Packet request) {
		Long serial = request.findLong(MessageKey.SERIAL_NUMBER);
		if (serial == null) {
			Logger.error(this, "doCreatePrivateSecure", "cannot be find serial!");
			return;
		}

		// 取出来源地址！
		SocketHost remote = request.getRemote();
		// 如果地址已经存在，返回一个错误提示
		if (hasCipher(remote)) {
			Logger.error(this, "doCreatePrivateSecure", "cipher existed! from %s", remote);

			Packet resp = new Packet(remote, Answer.PRIVATE_CREATE_SECURE_REFUSE);
			send(resp);
			return;
		}

		// RSA私钥解密
		Cipher cipher = null;
		byte[] data = request.getData();
		if (data != null) {
			try {
				cipher = decryptCipher(remote, data, 0, data.length);
			} catch (SecureException e) {
				Logger.error(e);
			} catch (Throwable e) {
				Logger.fatal(e);
			}
		}

		// 判断成功
		boolean success = (cipher != null);
		// 应答码
		short code = (success ? Answer.PRIVATE_CREATE_SECURE_ACCEPTED : Answer.PRIVATE_CREATE_SECURE_REFUSE);

		// 应答包
		Packet resp = new Packet(remote, code);
		resp.addMessage(MessageKey.SERIAL_NUMBER, serial);
		// 增加前缀符，返回非加密数据（此时没有保存密文，发送的是明文）
		success = sendReturnSecure(CREATE_SECURE, resp);

		// 保存配置。注意：一定要在把包发出后再保存密文，如果发送前就保存，发出的数据包会加密，客户端会解析失败！
		if (success ) {
			addCipher(remote, cipher);
		}

		// 日志
		if (success) {
			Logger.info(this, "doCreatePrivateSecure",
					"remote [%s], cipher: %s", remote, cipher);
		} else {
			//			Logger.warning(this, "doCreatePrivateSecure", "remote [%s], cipher: %s", remote, cipher);
		}
	}

	/**
	 * 处理建立密文接受的反馈包
	 * @param resp 反馈包
	 */
	private void doCreatePrivateSecureAccepted(Packet resp) {
		// 查找序列号
		Long serial = resp.findLong(MessageKey.SERIAL_NUMBER);
		if (serial == null) {
			Logger.error(this, "doCreatePrivateSecureAccepted", "cannot be find serial number!");
			return;
		}

		boolean success = false;
		// 锁定处理
		super.lockSingle();
		try {
			CreateSecureHook hook = createSecures.get(serial);
			success = (hook != null) ;
			if (success) {
				hook.setSuccessful(true);
				// 从内存建立它
				createSecures.remove(serial);
			}
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}

		if (success) {
			Logger.info(this, "doCreatePrivateSecureAccepted", "from %s, serial %d", resp.getRemote(), serial);
		} else {
			//			Logger.warning(this, "doCreatePrivateSecureAccepted", "from %s, serial %d", resp.getRemote(), serial);
		}
	}

	/**
	 * 处理建立密文拒绝的反馈包
	 * @param resp 反馈包
	 */
	private void doCreatePrivateSecureRefuse(Packet resp) {
		// 查找序列号
		Long serial = resp.findLong(MessageKey.SERIAL_NUMBER);
		if (serial == null) {
			Logger.error(this, "doCreatePrivateSecureRefuse", "cannot be find serial number!");
			return;
		}

		boolean success = false;
		// 锁定处理
		super.lockSingle();
		try {
			CreateSecureHook hook = createSecures.get(serial);
			success = (hook != null);
			if (success) {
				hook.setSuccessful(false);
				// 从内存建立它
				createSecures.remove(serial);
			}
		} catch (Throwable ex) {
			Logger.fatal(ex);
		} finally {
			super.unlockSingle();
		}

		Logger.error(this, "doCreatePrivateSecureRefuse", 
				"from %s, serial %d %s", resp.getRemote(), serial, (success ? "passed" : "refuse!"));
	}
	

	/**
	 * 向目标地址投递私有密文
	 * @param remote 目标站点
	 * @param serial 序列号
	 * @param cipher 密文数组
	 * @return 发送成功返回真，否则假
	 */
	private boolean __createPrivateSecure(SocketHost remote, long serial, byte[] cipher) {
		// 建立加密初始化数据包
		Mark cmd = new Mark(Ask.NOTIFY, Ask.PRIVATE_SECURE_CREATE);
		Packet packet = new Packet(remote, cmd);
		packet.addMessage(MessageKey.SERIAL_NUMBER, serial);
		packet.setData(cipher);
		// 直接发送数据包
		byte[] b = packet.build();

		// 向目标地址发包
		return packetMonitor.send(remote, b, 0, b.length);
	}

	/**
	 * 建立密文通信<br>
	 * 
	 * 这个FIXP服务器使用自己的信道，以客户机的身份，向真实的FIXP服务器，投递RSA密文。
	 * 要求对方服务器保存这个RSA密文，做为此后UDP安全通信依据。<br>
	 * 
	 * 对应FixpPacketHelper.createSecure方法。<br>
	 * 
	 * @param remote 目标站点地址
	 * @param timeout 超时时间
	 * @param count 如果失败，允许重试次数
	 * @param cipher FIXP密文
	 * @return 加密且发送成功返回真，或者假
	 */
	protected boolean createPrivateSecure(SocketHost remote, long timeout, int count, Cipher cipher) {
		// 用公钥对密文进行加密
		byte[] data = null;
		try {
			ClientSecure token = ClientSecureTrustor.getInstance().findSiteSecure(remote);
			if (token == null) {
				Logger.error(this, "createPrivateSecure",
						"cannot be find client-token by '%s', please check local.xml", remote);
				return false;
			}
			// 对密文用RAS公钥加密
			data = cipher.encase(token.getKey());
		} catch (SecureException e) {
			Logger.error(e);
			return false;
		}

		// 生成序列号
		long serial = generator.nextSerial();

		// 最少一次
		if (count < 1) {
			count = 1;
		}

		// 删除
		boolean success = false;
		for (int i = 0; i < count; i++) {
			CreateSecureHook hook = new CreateSecureHook(serial, timeout);
			// 保存钩子
			addCreateSecureHook(serial, hook);

			// 生成数据包，发送到目标站点
			boolean sended = __createPrivateSecure(remote, serial, data);
			if (sended) {
				hook.await();
			}
			// 删除钩子
			removeCreateSecureHook(serial);

			// 判断被强制中止，无条件退出
			if (hook.isInterrupted()) {
				break;
			}
			// 判断接收应答后的触发，返回处理结果
			if (hook.isTouched()) {
				success = hook.isSuccessful();
				break;
			}
		}

		// 成功，保存密文
		if (success) {
			addCipher(remote, cipher);
		}

		Logger.note(this, "createPrivateSecure", success, "create \'%s\' to %s", cipher, remote);

		// 返回结果
		return success;
	}

}

///**
// * 发送一个请求取消双方操作的数据包给客户端
// * @param request FIXP请求包
// */
//private void sendRequestCancelPacket(Packet request) {
//	SocketHost remote = request.getRemote();
//
//	boolean success = requests.containsKey(remote);
//	short code = (success ? Answer.CANCEL_OKAY : Answer.CANCEL_NOTFOUND);
//	Packet resp = new Packet(remote, code);
//	resp.addMessage(MessageKey.SPEAK, "cancel sub packet");
//
//	packetMonitor.send(remote, resp);
//}


///**
// * 更新密文。
// * 更新密文必须在加密情况下进行，为了防止黑客等恶意行为，要求数据来源和被替换地址一致，内存中的密文和客户机发来的旧密文一致。
// * 
// * @param request 请求包
// */
//private void replaceSecure(Packet request) {
//	SocketHost remote = request.getRemote();
//	// 取出字节数组
//	byte[] b = request.getData();
//
//	// 用可类化解析参数
//	ClassReader reader = new ClassReader(b);
//	SocketHost host = new SocketHost(reader); 	// 注册地址
//	Cipher oldCipher = new Cipher(reader); 		// 旧的密文
//	Cipher newCipher = new Cipher(reader); 		// 新的的密文
//
//	// 判断地址和注册地址一致
//	boolean success = (remote.getAddress().compareTo(host.getAddress()) == 0);
//	// 查找内存中的密文
//	if(success){
//		// 取出内存中的密文
//		Cipher memory = findCipher(host);
//		// 判断网络发来的旧密文，和内存中的密文一致
//		success = (memory != null && oldCipher.compareTo(memory) == 0);
//		// 保存新的密文
//		if (success) {
//			addCipher(host, newCipher);
//		} else {
//			Logger.error(this, "replaceSecure", 
//					"cipher host:[%s], memory cipher:%s | old cipher:%s | new cipher:%s",
//					host, memory, oldCipher, newCipher);
//		}
//	}
//
//	// 反馈结果给客户端
//	Packet resp = new Packet(remote, (success ? Answer.SECURE_ACCEPTED : Answer.SECURE_REFUSE));
//	resp.addMessage(MessageKey.SPEAK, (success ? "REPLACE SECURE, OK!" : "NOT FOUND SECURE"));
//	// 反馈结果
//	send(resp);
//
//	// 保存记录
//	if (success) {
//		Logger.info(this, "replaceSecure", "remote:[%s], cipher host:[%s], old cipher:%s, new cipher:%s",
//				remote, host, oldCipher, newCipher);
//	} else {
//		Logger.error(this, "replaceSecure", "remote:[%s], cipher host:[%s], old cipher:%s, new cipher:%s",
//				remote, host, oldCipher, newCipher);
//	}
//}


///**
// * 查找内网定位定时检测单元
// * @param remote 服务端节点地址
// * @return 返回PockItem实例，或者空指针
// */
//public PockItem findPockItem(SocketHost remote) {
//	// 锁定
//	super.lockMulti();
//	try {
//		PockItem item = pocks.get(remote);
//		if (item != null) {
//			return item.duplicate();
//		}
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockMulti();
//	}
//	return null;
//}


///**
// * 被SecureTrustor调用，通过FixpPacketMonitor信道，查询服务端的加密类型
// * @param remote 远程服务器地址
// * @param timeout 超时时间
// * @return 返回服务端的密文类型，失败返回-1。
// */
//private int __askPrivateSecure(SocketHost remote, long timeout) {
//	// 产生一个系列号，做为数据包的身份标识
//	long serial = generator.nextSerial();
//
//	// 生成钩子
//	AskSecureHook hook = new AskSecureHook(timeout);
//	// 保存它
//	addAskSecureHook(serial, hook);
//
//	// 生成请求数据包
//	Mark mark = new Mark(Ask.NOTIFY, Ask.PRIVATE_SECURE_QUERY);
//	Packet request = new Packet(remote, mark);
//	request.addMessage(MessageKey.SERIAL_NUMBER, serial);
//	// 利用FixpPacketMonitor信道发送出去，让内网NAT里的节点可以收到请求包！这一点非常重要！！！
//	send(request);
//
//	// 钩子等待，直到超时...
//	hook.await();
//
//	// 返回结果后删除....
//	removeAskSecureHook(serial);
//
//	return hook.getFamily();
//}


//else if(Assert.isSecureReplace(mark)) {
//	replaceSecure(packet); // 替换密文（删除旧的，保存新的）
//}


///**
// * 根据客户机来源地址，找到它关联的服务器私钥令牌，对已经加密的密文进行解密，返回原始数据
// * @param remote 来源地址
// * @param b  FIXP密文字节数组
// * @param off 密文开始下标
// * @param len 密文长度
// * @return 解密后的Cipher实例
// * @throws SecureException
// */
//private Cipher decryptCipher(SocketHost remote, byte[] b, int off, int len) throws SecureException {
//	Address client = remote.getAddress();
//	ServerToken token = ServerTokenManager.getInstance().find(client);
//	if (token == null) {
//		throw new SecureException("cannot be find server-token by %s, please check local.xml", client);
//	}
//	// 用RSA私钥解密
//	Cipher cipher = new Cipher();
//	cipher.decase(token.getKey(), b, off, len);
//	return cipher;
//}


///**
// * 通知客户机，由于没有遵守安全通信规则，要求客户机按照服务器规定的安全验证要求进行连接和通信。<br>
// * 服务器的安全验证选项包括：地址验证、密文验证、地址/密文复合验证。
// * 
// * @param client 客户机地址
// */
//private void notifySecure(SocketHost client) throws SecureException {
//	Address address = client.getAddress();
//	ServerToken token = ServerTokenManager.getInstance().find(address);
//	if (token == null) {
//		throw new SecureException(
//				"cannot be find server-toekn by '%s', please check local.xml", address);
//	}
//	int family = token.getFamily();
//
//	// 反馈的数据包
//	Packet resp = new Packet(client, Answer.SECURE_NOTIFY);
//	resp.addMessage(MessageKey.SECURE_FAMILY, family);
//
//	// 发送给客户机，只一次！
//	send(resp, 1);
//}


///**
// * 要求加密数据内容
// * @param client
// * @throws SecureException
// */
//private void notifyEncryptContent(SocketHost client) throws SecureException {
//	Address address = client.getAddress();
//	ServerToken token = ServerTokenManager.getInstance().find(address);
//	if (token == null) {
//		throw new SecureException(
//				"cannot be find server-toekn by '%s', please check local.xml", address);
//	}
//	int family = token.getFamily();
//
//	// 反馈的数据包
//	Packet resp = new Packet(client, Answer.ENCRYPT_CONTENT_NOTIFY);
//	resp.addMessage(MessageKey.SECURE_FAMILY, family);
//
//	// 发送给客户机
//	send(resp);
//}


///**
// * 处理非加密数据包
// * @param help FIXP辅助包
// * @throws SecureException
// */
//private void undecrypt(PrimitivePacket help) throws FixpProtocolException, SecureException {
//	// 明文，解析数据
//	SocketHost remote = help.getRemote();
//	byte[] b = help.getBuffer();
//
//	Packet packet = new Packet(remote, b, 0, b.length);
//	Mark mark = packet.getMark();
//
//	// 建立安全密文包
//	if (Assert.isSecureCreate(mark)) {
//		createSecure(packet);
//	}
//	// 私有密文的建立和反馈确认，在非加密状态下进行
//	else if (Assert.isPrivateSecureCreate(mark)) {
//		doCreatePrivateSecure(packet);
//	}
//	// 删除密文，本处忽略
//	else if (Assert.isSecureDrop(mark)) {
//		Logger.error(this, "undecrypt", "illegal drop secure");
//	}
//	// 安全查询
//	else if (Assert.isSecureQuery(mark)) {
//		// 如果是安全询问返回要求加密的应答
//		notifySecure(remote);
//	} 
//	// 私密安全检查和接受反馈
//	else if(Assert.isPrivateSecureQuery(mark)) {
//		doQueryPrivateSecure(packet);
//	} else if(Answer.isPrivateSecureNotify(mark)) {
//		doQueryPrivateSecureAccpeted(packet);
//	} 
//	// 收到“退出”命令，这时节点属于服务端，它向客户机发送“再见”应答，同时判断有密文和删除
//	else if (Assert.isExit(mark)) {
//		exit(packet);
//	}
//	// 收到“再见”应答，这时节点属于客户机，在确认后删除密文
//	else if (Assert.isGoodbye(mark)) {
//		removeCipher(remote);
//	}
////	// 客户机发送是明文，但是当前服务器要求加密时，返回要求加密的应答
////	else if (ServerTokenManager.getInstance().isCipher(remote.getAddress())) {
////		notifyEncryptContent(remote);
////	}
//	
//	// 检测应答！
//	else if (Answer.isShineAccepted(mark)) {
//		replyPock(packet);
//	} else if (Answer.isReflectAccepted(mark)) {
//		recallReflect(packet);
//	}
//	
//	// 客户机发送是明文，但是当前服务器要求加密时，返回要求加密的应答
//	else if(SecureController.getInstance().isCipher(remote.getAddress())) {
//		notifyEncryptContent(remote);
//	}
//	
////	// 各种检测
////	else if (Answer.isShineAccepted(mark)) {
////		replyPock(packet);
////	} else if (Assert.isShine(mark)) {
////		shine(packet);
////	} else if(Answer.isReflectAccepted(mark)) {
////		recallReflect(packet);
////	} else if (Assert.isReflect(mark)) {
////		reflect(packet); // 反馈客户的地址
////	} else if(Assert.isTest(mark)) {
////		test(packet); // 反馈测试包
////	}
//	
//	// 各种检测
//	else if (Assert.isShine(mark)) {
//		shine(packet);
//	} else if (Assert.isReflect(mark)) {
//		reflect(packet); // 反馈客户的地址
//	} else if(Assert.isTest(mark)) {
//		test(packet); // 反馈测试包
//	}
//	
//	// 单包远程调用
//	else if (Answer.isVisitAccepted(mark)) {
//		recallVisit(packet);
//	} else if (Assert.isVisit(mark)) {
//		visit(packet);
//	}
//	// RPC或者其它操作
//	else {
//		todo(packet);
//	}
//}

///**
// * 处理非加密数据包
// * @param help FIXP辅助包
// * @throws SecureException
// */
//private void undecrypt(PrimitivePacket help) throws FixpProtocolException, SecureException {
//	// 明文，解析数据
//	SocketHost remote = help.getRemote();
//	byte[] b = help.getBuffer();
//
//	Packet packet = new Packet(remote, b, 0, b.length);
//	Mark mark = packet.getMark();
//
//	// 建立安全密文包
//	if (Assert.isSecureCreate(mark)) {
//		createSecure(packet);
//	}
//	// 私有密文的建立和反馈确认，在非加密状态下进行
//	else if (Assert.isPrivateSecureCreate(mark)) {
//		doCreatePrivateSecure(packet);
//	}
//	// 删除密文，本处忽略
//	else if (Assert.isSecureDrop(mark)) {
//		Logger.error(this, "undecrypt", "illegal drop secure");
//	}
//	// 安全查询
//	else if (Assert.isSecureQuery(mark)) {
//		// 如果是安全询问返回要求加密的应答
//		notifySecure(remote);
//	} 
//	// 私密安全检查和接受反馈
//	else if(Assert.isPrivateSecureQuery(mark)) {
//		doQueryPrivateSecure(packet);
//	} else if(Answer.isPrivateSecureNotify(mark)) {
//		doQueryPrivateSecureAccpeted(packet);
//	} 
//	// 收到“退出”命令，这时节点属于服务端，它向客户机发送“再见”应答，同时判断有密文和删除
//	else if (Assert.isExit(mark)) {
//		exit(packet);
//	}
//	// 收到“再见”应答，这时节点属于客户机，在确认后删除密文
//	else if (Assert.isGoodbye(mark)) {
//		removeCipher(remote);
//	}
////	// 客户机发送是明文，但是当前服务器要求加密时，返回要求加密的应答
////	else if (ServerTokenManager.getInstance().isCipher(remote.getAddress())) {
////		notifyEncryptContent(remote);
////	}
//	
//	// 检测应答！
//	else if (Answer.isShineAccepted(mark)) {
//		replyPock(packet);
//	} else if (Answer.isReflectAccepted(mark)) {
//		recallReflect(packet);
//	} else if (Answer.isVisitAccepted(mark)) {
//		recallVisit(packet);
//	}
//	
//	// 客户机发送是明文，但是当前服务器要求加密时，返回要求加密的应答
//	else if(SecureController.getInstance().isCipher(remote.getAddress())) {
//		notifyEncryptContent(remote);
//	}
//	
////	// 各种检测
////	else if (Answer.isShineAccepted(mark)) {
////		replyPock(packet);
////	} else if (Assert.isShine(mark)) {
////		shine(packet);
////	} else if(Answer.isReflectAccepted(mark)) {
////		recallReflect(packet);
////	} else if (Assert.isReflect(mark)) {
////		reflect(packet); // 反馈客户的地址
////	} else if(Assert.isTest(mark)) {
////		test(packet); // 反馈测试包
////	}
//	
//	// 各种检测
//	else if (Assert.isShine(mark)) {
//		shine(packet);
//	} else if (Assert.isReflect(mark)) {
//		reflect(packet); // 反馈客户的地址
//	} else if(Assert.isTest(mark)) {
//		test(packet); // 反馈测试包
//	}
//	// 单包远程调用
//	else if (Assert.isVisit(mark)) {
//		visit(packet);
//	}
//	// RPC或者其它操作
//	else {
//		todo(packet);
//	}
//}


///**
// * 通知客户机，由于没有遵守安全通信规则，要求客户机按照服务器规定的安全验证要求进行连接和通信。<br>
// * 服务器的安全验证选项包括：地址验证、密文验证、地址/密文复合验证。
// * 
// * @param client 客户机地址
// */
//private void doQueryPrivateSecure(Packet request) throws SecureException {
//	Long serial = request.findLong(MessageKey.SERIAL_NUMBER);
//	if (serial == null) {
//		Logger.error(this, "doQueryPrivateSecure", "cannot be find serial number!");
//		return;
//	}
//
//	// 客户端主机地址
//	SocketHost client = request.getRemote();
//	Address address = client.getAddress();
//	// 根据地址找到对应的本地密文类型
//	ServerToken token = ServerTokenManager.getInstance().find(address);
//	// 如果没有找到，不返回结果，让客户机超时后退出！
//	if (token == null) {
//		throw new SecureException("cannot be find server-toekn by '%s', please check local.xml",
//				address);
//	}
//	int family = token.getFamily();
//
//	// 反馈的数据包
//	Packet resp = new Packet(client, Answer.PRIVATE_SECURE_NOTIFY);
//	resp.addMessage(MessageKey.SECURE_FAMILY, family);	// 密文类型
//	resp.addMessage(MessageKey.SERIAL_NUMBER, serial); 	// 序列号
//
//	Logger.debug(this, "doQueryPrivateSecure",
//			"current site secure type: %s, send to client %s",
//			SecureType.translate(family), client);
//
//	// 发送给请求端的FixpPacketMonitor
//	send(resp);
//}


///**
// * 建立密文通信<br>
// * 
// * 这个FIXP服务器使用自己的信道，以客户机的身份，向真实的FIXP服务器，投递RSA密文。
// * 要求对方服务器保存这个RSA密文，做为此后UDP安全通信依据。<br>
// * 
// * 对应FixpPacketHelper.createSecure方法。<br>
// * 
// * @param remote 目标站点地址
// * @param timeout 超时时间
// * @param count 如果失败，允许重试次数
// * @param cipher FIXP密文
// * @return 加密且发送成功返回真，或者假
// */
//protected boolean createPrivateSecure(SocketHost remote, long timeout, int count, Cipher cipher) {
//	// 用公钥对密文进行加密
//	byte[] data = null;
//	try {
//		Address address = remote.getAddress();
//		ClientToken token = ClientTokenManager.getInstance().find(address);
//		if (token == null) {
//			Logger.error(this, "createPrivateSecure",
//					"cannot be find client-token by '%s', please check local.xml", address);
//			return false;
//		}
//		// 对密文用RAS公钥加密
//		data = cipher.encase(token.getKey());
//	} catch (SecureException e) {
//		Logger.error(e);
//		return false;
//	}
//
//	// 生成序列号
//	long serial = generator.nextSerial();
//
//	// 最少一次
//	if (count < 1) {
//		count = 1;
//	}
//
//	// 删除
//	boolean success = false;
//	for (int i = 0; i < count; i++) {
//		CreateSecureHook hook = new CreateSecureHook(serial, timeout);
//		// 保存钩子
//		addCreateSecureHook(serial, hook);
//
//		// 生成数据包，发送到目标站点
//		boolean sended = __createPrivateSecure(remote, serial, data);
//		if (sended) {
//			hook.await();
//		}
//		// 删除钩子
//		removeCreateSecureHook(serial);
//
//		// 判断被强制中止，无条件退出
//		if (hook.isInterrupted()) {
//			break;
//		}
//		// 判断接收应答后的触发，返回处理结果
//		if (hook.isTouched()) {
//			success = hook.isSuccessful();
//			break;
//		}
//	}
//
//	// 成功，保存密文
//	if (success) {
//		addCipher(remote, cipher);
//	}
//
//	Logger.note(this, "createPrivateSecure", success, "create \'%s\' to %s", cipher, remote);
//
//	// 返回结果
//	return success;
//}


///**
// * 被SecureTrustor调用，通过FixpPacketMonitor信道，查询服务端的加密类型
// * @param remote 远程服务器地址
// * @param timeout 超时时间
// * @return 返回服务端的密文类型，失败返回-1。
// */
//protected int askPrivateSecure(SocketHost remote, long timeout) {
//	// 发送命令，检查服务端加密类型
//	int family = queryPrivateSecure(remote, timeout);
//	if (SecureType.isFamily(family)) {
//		return family;
//	}
//	return SecureType.INVALID;
//}


///**
// * 被SecureTrustor调用，通过FixpPacketMonitor信道，查询服务端的加密类型
// * @param remote 远程服务器地址
// * @param timeout 超时时间
// * @return 返回服务端的密文类型，失败返回-1。
// */
//private int queryPrivateSecure(SocketHost remote, long timeout) {
//	// 产生一个系列号，做为数据包的身份标识
//	long serial = generator.nextSerial();
//
//	// 连续检测3次
//	final int count = 3;
//	long subTimeout = timeout / count;
//	if (subTimeout < 10000) subTimeout = 10000; // 最少10秒
//
//	// 连续3次
//	for (int i = 0; i < count; i++) {
//		Logger.debug(this, "queryPrivateSecure", "index %d, check %s, hook timeout:%d ms", 
//				i + 1, remote, subTimeout);
//
//		// 生成钩子
//		AskSecureHook hook = new AskSecureHook(subTimeout);
//		// 保存它
//		addAskSecureHook(serial, hook);
//
//		// 生成请求数据包
//		Mark mark = new Mark(Ask.NOTIFY, Ask.PRIVATE_SECURE_QUERY);
//		Packet request = new Packet(remote, mark);
//		request.addMessage(MessageKey.SERIAL_NUMBER, serial);
//		// 利用FixpPacketMonitor信道发送出去，让内网NAT里的节点可以收到请求包！这一点非常重要！！！
//		send(request);
//
//		// 钩子等待，直到超时...
//		hook.await();
//
//		// 返回结果后删除....
//		removeAskSecureHook(serial);
//
//		// 判断是强制中断
//		if (hook.isInterrupted()) {
//			break;
//		}
//
//		// 是有效的类型，返回结果
//		int family = hook.getFamily();
//		if (SecureType.isFamily(family)) {
//			return family;
//		}
//	}
//
//	// 结果无效
//	return SecureType.INVALID;
//}
