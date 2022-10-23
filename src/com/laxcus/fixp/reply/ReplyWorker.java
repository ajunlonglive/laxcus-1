/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import java.net.*;
import java.util.*;

import com.laxcus.echo.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 异步数据发送代理器。
 * 向REPLY HELPER发送数据。
 * 
 * @author scott.liang
 * @version 1.0 7/22/2018
 * @since laxcus 1.0
 */
public class ReplyWorker extends ReplyAgent {

	/** 异步收/发任务的失效时间 **/
	private static volatile int disableTimeout = 120000;

	/** 异步收/发任务的子包超时时间 **/
	private static volatile int subPacketTimeout = 10000;

	/** FIXP子包之间的发送间隔，以毫秒为单位，默认1毫秒。通过发送端的迟延发送，降低接收端的接收/分发压力，减少丢包概率 **/
	private static volatile int sendInterval = 1;

	/**
	 * 设置异步收/发任务的失效时间，以毫秒为单位，最小30秒。超时将被删除。
	 * @param ms 毫秒
	 */
	public static void setDisableTimeout(int ms) {
		if(ms >= 30000) ReplyWorker.disableTimeout = ms;
	}

	/**
	 * 返回异步收/发任务的失效时间。达到时间队列中的发送/接收单元将被删除。
	 * @return 以毫秒为单位的时间
	 */
	public static int getDisableTimeout() {
		return ReplyWorker.disableTimeout;
	}

	/**
	 * 设置异步收/发任务的子包超时时间，以毫秒为单位，最小10秒钟。超时重发数据包。
	 * @param ms 毫秒
	 */
	public static void setSubPacketTimeout(int ms) {
		if(ms >= 2000) ReplyWorker.subPacketTimeout = ms;
	}

	/**
	 * 返回异步收/发任务的子包超时时间。超时重发数据包。
	 * @return 以毫秒为单位的时间
	 */
	public static int getSubPacketTimeout() {
		return ReplyWorker.subPacketTimeout;
	}

	/**
	 * 设置FIXP子包之间的发送间隔，以毫秒为单位。
	 * 通过迟延发送，避免接收端压力过大，减少丢包概率
	 * @param ms 毫秒
	 */
	public static void setSendInterval(int ms) {
		if (ms <= 0) {
			ReplyWorker.sendInterval = 0;
		} else {
			ReplyWorker.sendInterval = ms;
		}
	}

	/**
	 * 返回FIXP子包之间的发送间隔，以毫秒为单位。
	 * 通过迟延发送，避免接收端压力过大，减少丢包概率
	 * @return 以毫秒为单位的时间
	 */
	public static int getSendInterval() {
		return ReplyWorker.sendInterval;
	}

	/** 从REPLY DISPATCHER接收的原始数据包 */
	private ArrayList<PrimitivePacket> packets = new ArrayList<PrimitivePacket>(5120);

	/** 处于等待中的发送器，接收CAST HELO包后转入。异步通信标识 -> 反馈数据发送器 **/
	private Map<CastFlag, ReplySender> readyTasks = new TreeMap<CastFlag, ReplySender>();

	/** 交互中的发送器 **/
	private Map<ReplyFlag, ReplySender> runTasks = new TreeMap<ReplyFlag, ReplySender>();

	/** 发送器 **/
	private ReplyDispatcher dispatcher;

	/** 循环迭代编号 **/
	private int iterateIndex;

	/** 从服务 **/
	private ArrayList<MODispatcher> slaves = new ArrayList<MODispatcher>();

	/**
	 * 构造默认的异步数据发送代理器
	 */
	public ReplyWorker(ReplyDispatcher e) {
		super();
		dispatcher = e;
		iterateIndex = 0;
		//		stayTime = System.currentTimeMillis();
	}

	/**
	 * 保存一个从发送器
	 * @param e MODispatcher实例
	 */
	public void addSlave(MODispatcher e) {
		Laxkit.nullabled(e);
		slaves.add(e);
	}

	/**
	 * 保存一批从发送器
	 * @param a MODispatcher数组
	 */
	public void addSlaves(MODispatcher[] a) {
		for (MODispatcher e : a) {
			addSlave(e);
		}
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
		Logger.info(this, "process", "into...");
		while (!isInterrupted()) {
			// 处理参数
			boolean success = subprocess();
			// 没有处理数据，延时1秒
			if (!success) {
				delay(1000);
			}
		}
		Logger.info(this, "process", "exit!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		stopReadyTasks();
		stopRunTasks();
		packets.clear();
	}

	/**
	 * 注册发送器。<br>
	 * 异步数据发送器在注册前，必须已经定义CastToken。
	 * 
	 * @param sender 异步数据发送器
	 * @return 保存成功返回真，否则假
	 */
	public boolean push(ReplySender sender) {
		CastToken token = sender.getToken();
		CastFlag flag = token.getFlag();

		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			// 判断不存在
			if (!readyTasks.containsKey(flag)) {
				sender.setWorker(this);
				success = (readyTasks.put(flag, sender) == null);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 以上处理成功，生成HELO FIXP数据包，发送到REPLY MONITOR，成功返回真，否则假
		if (success) {
			success = sender.helo();
		}

//		Logger.debug(this, "push", success, "ReplySender helo! cast flag:[%s]", flag);

		return success;
	}

	/**
	 * 循环提取下一个
	 * @return
	 */
	private synchronized MODispatcher nextDispatcher() {
		if (slaves.size() == 0) {
			return null;
		}
		// 超过范围，重新指向0
		if (iterateIndex >= slaves.size()) {
			iterateIndex = 0;
		}
		return slaves.get(iterateIndex++);
	}

	/**
	 * 生成UDP数据包
	 * @param remote
	 * @param packets
	 * @return
	 */
	private List<DatagramPacket> createPackets(SocketHost remote, Collection<Packet> packets) {
		SocketAddress address = remote.getSocketAddress();
		ArrayList<DatagramPacket> array = new ArrayList<DatagramPacket>(); 
		// 生成UDP数据包
		for (Packet sub : packets) {
			byte[] b = sub.build();
			DatagramPacket udp = new DatagramPacket(b, 0, b.length);
			udp.setSocketAddress(address);
			array.add(udp);
		}
		return array;

		//		try {
		//			for (Packet sub : packets) {
		//				byte[] b = sub.build();
		//				DatagramPacket udp = new DatagramPacket(b, 0, b.length, address);
		//				array.add(udp);
		//			}
		//			return array;
		//		} catch (SocketException e) {
		//			Logger.error(e);
		//		} catch (Throwable e) {
		//			Logger.fatal(e);
		//		}
		//		return null;
	}

	/**
	 * 判断是受到限制的节点，限制主要针对FRONT节点，也包括WATCH节点。<br>
	 * FRONT节点是“外网/内网”都可以存在的节点。<br><br>
	 * 
	 * NAT网络同时存在“公网/内网”和“内网/内网”的可能，<br>
	 * 如果FRONT节点和集群其它节点之间存在NAT网络，那么必须通过主管道（ReplyDispatcher）发送出去。<br>
	 * 如果用MO通道（MODispatcher）发送就会出现被NAT拒绝分发且ReplyReceiver收不到的可能。<br>
	 * 
	 * @param serverFamily
	 * @return
	 */
	private boolean isLimitReceiver(byte serverFamily) {
		switch(serverFamily){
		case SiteTag.FRONT_SITE:
		case SiteTag.WATCH_SITE:
			return true;
		}
		return false;
	}

	/**
	 * 发送一批数据包到目标地址
	 * @param serverFamily 服务器（ReplyReceiver）节点类型，这个参数在EchoBuffer/DoubleClient中设置。
	 * @param remote 服务器主机地址
	 * @param packets 数据包
	 * @return 返回发包数目
	 */
	protected int allTo(byte serverFamily, SocketHost remote, Collection<Packet> packets) {
		List<DatagramPacket> udps = createPackets(remote, packets);

		// 如果目标主机是FRONT/WATCH节点，或者是公网地址，只能用ReplyDispatcher发送
		boolean limit = (isLimitReceiver(serverFamily) || ReplyUtil.isWideAddress(remote.getAddress()));
		if (limit) {
			return dispatcher.allTo(udps);
		}
		// 循环选择一个MODispatcher，发送到目标地址
		MODispatcher mo = nextDispatcher();
		if (mo != null) {
			return mo.allTo(udps);
		}

		// 以上不成立，仍然用ReplyDispatcher发送
		return dispatcher.allTo(udps);
	}

	/**
	 * 向目标地址连续发送N个FIXP包
	 * @param packet FIXP数据包
	 * @param count 发送次数
	 * @return 返回发送成功数目
	 */
	protected int sendTo(Packet packet, int count) {
		if (count < 1) {
			count = 1;
		}
		SocketHost remote = packet.getRemote();
		byte[] b = packet.build();

		// 客户机目标地址
		SocketAddress address = remote.getSocketAddress();
		// 生成UDP数据包
		DatagramPacket udp = new DatagramPacket(b, 0, b.length);
		udp.setSocketAddress(address);
		
		int total = 0;
		for (int i = 0; i < count; i++) {
			// 发送UDP包
			boolean success = dispatcher.sendTo(udp);
			if (success) total++;
		}
		return total;

		//		// 生成UDP数据包
		//		int total = 0;
		//		try {
		//			DatagramPacket udp = new DatagramPacket(b, 0, b.length, address);
		//			for (int i = 0; i < count; i++) {
		//				// 发送UDP包
		//				boolean success = dispatcher.sendTo(udp);
		//				if (success) total++;
		//			}
		//		} catch (SocketException e) {
		//			Logger.error(e);
		//		} catch (Throwable e) {
		//			Logger.fatal(e);
		//		}
		//		return total;
	}

	/**
	 * 向目标地址连接1个数据包
	 * @param packet FIXP数据包
	 * @return 返回发送成功数目
	 */
	protected int sendTo(Packet packet) {
		return sendTo(packet, 1);
	}

	/**
	 * 由REPLY DISPATCHER转发一个待处理的FIXP数据包
	 * @param from 数据来源
	 * @param b 待发送的数据
	 * @param off 数据开始下标
	 * @param len 数据有效长度
	 * @return 保存成功返回真，否则假
	 */
	protected boolean add(SocketHost from, byte[] b, int off, int len) {
		// 保存原始包
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
		// 锁定保存
		super.lockSingle();
		try {
			success = packets.add(packet);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 唤醒线程
		if (success) {
			wakeup();
		}
		return success;
	}

	/**
	 * 弹出一个接收到的数据包
	 * @return 返回FIXP包实例，没有返回空指针
	 */
	private Packet popup() {
		PrimitivePacket packet = null;
		// 锁定，取出！
		super.lockSingle();
		try {
			if (packets.size() > 0) {
				packet = packets.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return (packet != null ? packet.regress() : null);
	}

	/**
	 * 处理数据
	 * @return 有数据处理返回真，否则假
	 */
	private boolean subprocess() {
		int count = 0;
		// 检查接收的包，找到对应的发送器去处理。
		int size = packets.size();
		for (int index = 0; index < size; index++) {
			Packet packet = popup();
			if (packet != null) {
				try {
					distribute(packet);
				} catch (Throwable e) {
					Logger.fatal(e);
				}
			}
		}
		count += size;

		// 检查处于等待的任务
		size = checkReadyTasks();
		count += size;

		return (count > 0);
	}

	/**
	 * 从就绪状态切换到运行状态
	 * @param from 来源地址
	 * @param flag 异步通信标识
	 * @return 成功返回真，否则假
	 */
	private boolean switchTo(SocketHost from, CastFlag flag) {
		boolean success = false;

		ReplyFlag target = new ReplyFlag(from, flag.getCode());

		// 锁定，从就绪状态转换到运行状态
		ReplySender sender = null;
		// 锁定
		super.lockSingle();
		try {
			sender = readyTasks.remove(flag);
			if (sender != null) {
				sender.setReplyFlag(target);
				success = (runTasks.put(target, sender) == null);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		//		// 启动线程
		//		if (success && sender != null) {
		//			sender.start();
		//		}

		// 启动线程
		if (success) {
			success = (sender != null);
			if (success) {
				success = sender.start();
			}
		}

//		Logger.debug(this, "switchTo", success, "from [%s], cast flag:[%s]", from, flag);

		return success;
	}

	/**
	 * 根据标识，找到对应的发送器
	 * @param flag 异步快速传输标识
	 * @return 返回实例，或者空指针
	 */
	private ReplySender findSender(ReplyFlag flag) {
		super.lockMulti();
		try {
			return runTasks.get(flag);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 删除发送器
	 * @param flag 异步快速传输标识
	 * @return 删除成功返回真，否则假
	 */
	protected boolean removeSender(ReplyFlag flag) {
		ReplySender sender = null;
		// 锁定
		super.lockSingle();
		try {
			sender = runTasks.remove(flag);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		boolean success = (sender != null);
		if (success) {
			Logger.debug(this, "removeSender", "receive bytes: %d, send bytes: %d, usedtime: %d ms",
					sender.getReceiveFlowSize(), sender.getSendFlowSize(),
					sender.getRunTime());
		}
		return success;
	}

	/**
	 * 分发处理 <br><br>
	 * 
	 * 以下情况：<br>
	 * 1. 请求的反馈判断 <br>
	 * 2. FIXP TEST，测试网络连通 <br>
	 * 3. CAST HELO 反馈判断 <br>
	 * 4. 其它通信 <br><br>
	 * 
	 * @param packet FIXP数据包
	 */
	private void distribute(Packet packet) {
		Mark mark = packet.getMark();

		// 判断是外网对内网的定位
		if (Answer.isShineAccepted(mark)) {
			replyShine(packet);
		} else if(Assert.isShine(mark)) {
			shine(packet);
		}
		// FIXP UDP网络通信测试，无条件接受！
		else if(Assert.isTest(mark)){
			test(packet);
		}
		// 其它通信
		else if(Answer.isCastHeloOkay(mark)) {
			CastFlag flag =	readFlag(packet); 
			boolean success = (flag != null);
			if (success) {
				switchTo(packet.getRemote(), flag);
			}
//			else {
//				Logger.error(this, "distribute", "cannot be find CastFlag! from %s", packet.getRemote());
//			}
		} else {
			CastCode code = readCode(packet);
			if (code == null) {
				Logger.error(this, "distribute", "must be cast code!");
				return;
			}

			ReplyFlag flag = new ReplyFlag(packet.getRemote(), code);
			ReplySender sender = findSender(flag);
			if (sender != null) {
				sender.add(packet);
			} 
			//			else {
			//				Logger.error(this, "distribute", "not found %s", flag);
			//			}
		}
	}

	/**
	 * 检查等待队列中的成员
	 * @return 返回处理的成员数目
	 */
	private int checkReadyTasks() {
		int size = readyTasks.size();
		if (size == 0) {
			return 0;
		}

		// 保存参数
		ArrayList<CastFlag> array = new ArrayList<CastFlag>(size);
		// 锁定
		super.lockSingle();
		try {
			Iterator<Map.Entry<CastFlag, ReplySender>> iterator = readyTasks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<CastFlag, ReplySender> entry = iterator.next();
				ReplySender sender = entry.getValue();
				boolean success = sender.isPacketTimeout(ReplyWorker.getDisableTimeout()); // 达到最大超时时间，删除！
				if (success) {
					array.add(entry.getKey());
					// 停止线程
					sender.stop();
					sender.done();
				}
			}
			// 删除超时
			for (CastFlag flag : array) {
				readyTasks.remove(flag);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 记录
		int count = array.size();
		if (count > 0) {
			for (CastFlag flag : array) {
				Logger.error(this, "checkReadyTasks", "delete timeout ReplySender! %s", flag);
			}
		}

		// 返回删除的成员数
		return count;
	}

	/**
	 * 停止全部运行状态的发送器
	 * 通知停止线程运行
	 */
	private void stopRunTasks() {
		super.lockSingle();
		try {
			Iterator<Map.Entry<ReplyFlag, ReplySender>> iterator = runTasks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<ReplyFlag, ReplySender> entry = iterator.next();
				ReplySender sender = entry.getValue();
				sender.stop();
				sender.done();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 停止全部等待状态的发送器
	 */
	private void stopReadyTasks() {
		super.lockSingle();
		try {
			Iterator<Map.Entry<CastFlag, ReplySender>> iterator = readyTasks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<CastFlag, ReplySender> entry = iterator.next();
				ReplySender sender = entry.getValue();
				// 两个操作，停止线程同时停止外部等待的线程
				sender.stop();
				sender.done();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 返回外部网络主机地址
	 * @return SocketHost实例
	 */
	public SocketHost getDefinePublicHost() {
		return dispatcher.getDefinePublicHost();
	}

	/**
	 * 返回内部网络主机地址
	 * @return SocketHost实例
	 */
	public SocketHost getDefinePrivateHost() {
		return dispatcher.getDefinePrivateHost();
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
		int count = sendTo(resp, 1);
		// 判断成功/失败
		boolean success = (count > 0);

		//		// 生成数据流，发送出去！
		//		b = resp.build();
		//		boolean success = dispatcher.sendTo(remote, b, 0, b.length);

		Logger.debug(this, "shine", success, "from %s", remote);
	}

	/**
	 * FIXP UDP网络连通测试
	 * @param request 请求包
	 */
	private void test(Packet request) {
		// 来源地址
		SocketHost remote = request.getRemote();
		// 生成应答包包
		Packet resp = new Packet(remote, Answer.ACCEPTED);
		resp.addMessage(MessageKey.SPEAK, "RE:TEST!");
		// 记录来源主机地址，返回给调用方
		byte[] b = remote.build();
		resp.addMessage(MessageKey.HOST, b);

		// 反馈结果
		sendTo(resp, 1);

		Logger.debug(this, "test", "from %s", remote);
	}

	/**
	 * 处理返回的内网定位
	 * @param resp 应答包
	 */
	private void replyShine(Packet resp) {
		// 目标方地址
		byte[] a = resp.findRaw(MessageKey.HOST);
		SocketHost remote = new SocketHost(new ClassReader(a));

		// 本地主机，或者NAT设备出口地址
		byte[] b = resp.getData();
		// 本地主机
		SocketHost local = new SocketHost(new ClassReader(b));

		//		// 锁定处理
		//		super.lockSingle();
		//		try {
		//			// 钩子必须存在！
		//			HostHook hook = hooks.get(remote);
		//			if (hook != null) {
		//				hook.setLocal(local);
		//				// 撤销它
		//				hooks.remove(remote);
		//			}
		//
		//			// 检测单元有效
		//			boolean success = (pitch != null);
		//			if (success) {
		//				success = (Laxkit.compareTo(pitch.getRemote(), remote) == 0);
		//			}
		//			if (success) {
		//				// 如果没有定义，或者定位地址不匹配时，更新
		//				if (pitch.getLocal() == null) {
		//					pitch.setLocal(local);
		//				} else if (Laxkit.compareTo(pitch.getLocal(), local) != 0) {
		//					pitch.setLocal(local);
		//					Logger.warning(this, "replyShine", "replace to %s", local);
		//				}
		//			}
		//
		//		} catch (Throwable e) {
		//			Logger.fatal(e);
		//		} finally {
		//			super.unlockSingle();
		//		}

		Logger.debug(this, "replyShine",  "remote: %s, local:%s",
				remote, local);
	}


}


///** 最后驻留时间 **/
//private volatile long stayTime;


///**
// * 由REPLY DISPATCHER转发一个待处理的FIXP数据包
// * @param from 数据来源
// * @param b 待发送的数据
// * @param off 数据开始下标
// * @param len 数据有效长度
// * @return 保存成功返回真，否则假
// */
//protected boolean add(SocketHost from, byte[] b, int off, int len) {
//	// 保存原始包
//	PrimitivePacket packet = null;
//	try {
//		packet = new PrimitivePacket(from, b, off, len);
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	}
//	if (packet == null) {
//		return false;
//	}
//
//	boolean success = false;
//	boolean empty = false;
//	// 锁定保存
//	super.lockSingle();
//	try {
//		empty = packets.isEmpty();
//		success = packets.add(packet);
//	} catch (Throwable e) {
//		Logger.fatal(e);
//	} finally {
//		super.unlockSingle();
//	}
//	// 唤醒线程
//	if (empty && success) {
//		wakeup();
//	}
//	return success;
//}


//	/**
//	 * 通过代理发送HELO操作给接收器
//	 * @param token 异步通信令牌
//	 */
//	private void helo(CastToken token) {
//		Packet packet = new Packet(Ask.NOTIFY, Ask.CASTHELO);
//		packet.setRemote(token.getListener()); // 目标站点地址
//		packet.addMessage(MessageKey.CAST_FLAG, token.getFlag());
//		// 直接发送，不要进入缓存池
//		sendTo(packet, 5);
//	}



///**
// * 判断IP来自公网，并且本地没有这个公网IP地址
// * @param from 来源地址
// * @return 是公网地址返回真，否则假
// */
//private final boolean isWideAddress(Address from) {
//	return from.isWideAddress() && !Address.contains(from);
//}


//	/**
//	 * 向目标地址发送一个FIXP包报文。先送前，做延时处理，避免发生丢包现象。
//	 * 
//	 * @param remote 目标地址
//	 * @param b 字节数组
//	 * @param off 下标
//	 * @param len 指定长度
//	 * @return 发送成功返回真，否则假
//	 */
//	protected boolean sendTo(SocketHost remote, byte[] b, int off, int len) {
//		stay();
//		return dispatcher.sendTo(remote, b, off, len);
//	}

///**
// * 所有输出的数据包都要经过这个方法延时
// */
//private void stay() {
//	// 最后触发时间小于时间间隔，等待！
//	if (System.currentTimeMillis() - stayTime < ReplyWorker.sendInterval) {
//		delay(ReplyWorker.sendInterval);
//		stayTime = System.currentTimeMillis();
//	}
//}

///**
// * 向目标地址连续发送N个FIXP包
// * @param packet FIXP数据包
// * @param count 发送次数
// * @param skipStay 跳过延时
// * @return 返回发送成功数目
// */
//protected int sendTo(Packet packet, int count, boolean skipStay) {
//	if (count < 1) {
//		count = 1;
//	}
//	SocketHost remote = packet.getRemote();
//	byte[] b = packet.build();
//
//	// 客户机目标地址
//	SocketAddress address = remote.getSocketAddress();
//	// 生成UDP数据包
//	int total = 0;
//	try {
//		DatagramPacket udp = new DatagramPacket(b, 0, b.length, address);
//		for (int i = 0; i < count; i++) {
//			if (skipStay) {
//				stay();
//			}
//			// 发送UDP包
//			boolean success = dispatcher.sendTo(udp);
//			if (success) total++;
//		}
//	} catch (SocketException e) {
//		Logger.error(e);
//	}
//	return total;
//}

///**
// * 向目标地址连接N个数据包
// * @param packet FIXP数据包
// * @param count 发送次数
// * @return 返回发送成功数目
// */
//protected int sendTo(Packet packet, int count) {
//	return sendTo(packet, count, false);
//}
