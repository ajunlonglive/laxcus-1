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
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 异步数据接收代理器。
 * 接收来自REPLY WORKER发送的数据。
 * 
 * 根据地址来源，解码数据，缓存各个异步调用器缓存。
 * 
 * @author scott.liang
 * @version 1.0 7/20/2018
 * @since laxcus 1.0
 */
public class ReplyHelper extends ReplyAgent {

	/** 异步收/发任务的失效时间 **/
	private static volatile int disableTimeout = 120000;

	/** 异步收/发任务的子包超时时间 **/
	private static volatile int subPacketTimeout = 10000;

	/**
	 * 设置异步收/发任务的失效时间，以毫秒为单位，最小30秒。超时将被删除。
	 * @param ms 毫秒
	 */
	public static void setDisableTimeout(int ms) {
		if(ms >= 30000) disableTimeout = ms;
	}

	/**
	 * 返回异步收/发任务的失效时间。达到时间队列中的发送/接收单元将被删除。
	 * @return 以毫秒为单位的时间
	 */
	public static int getDisableTimeout() {
		return disableTimeout;
	}

	/**
	 * 设置异步收/发任务的子包超时时间，以毫秒为单位，最小10秒钟。超时重发数据包。
	 * @param ms 毫秒
	 */
	public static void setSubPacketTimeout(int ms) {
		if(ms >= 2000) subPacketTimeout = ms;
	}

	/**
	 * 返回异步收/发任务的子包超时时间。超时重发数据包。
	 * @return 以毫秒为单位的时间
	 */
	public static int getSubPacketTimeout() {
		return subPacketTimeout;
	}

	/** 从REPLY MONITOR接收的包 */
	private ArrayList<PrimitivePacket> packets = new ArrayList<PrimitivePacket>(5120);

	/** 处于等待中的接收器，接收CAST HELO包后转入。异步通信标识 -> 反馈数据接收器 **/
	private Map<CastFlag, ReplyReceiver> readyTasks = new TreeMap<CastFlag, ReplyReceiver>();

	/** 交互中的接收器 **/
	private Map<ReplyFlag, ReplyReceiver> runTasks = new TreeMap<ReplyFlag, ReplyReceiver>();

	/** 异步数据主接收器 **/
	private ReplySucker sucker;
	
	/** 保存一个子节点 **/
	private int iterateIndex = 0;

	private ArrayList<MISucker> slaves = new ArrayList<MISucker>();

	/** 激活标记 **/
	private TreeSet<FlowFlag> activeFlags = new TreeSet<FlowFlag>();
	
	/** 当前站点启动 **/
	private SiteLauncher siteLauncher;

	/**
	 * 构造默认的异步反馈数据辅助器
	 */
	public ReplyHelper(ReplySucker e) {
		super();
		sucker = e;
		setSleepTimeMillis(5000);
		// 初始化参数
		iterateIndex = 0;
	}
	
	/**
	 * 设置站点启动器
	 * @param e 站点启动器
	 */
	public void setSiteLauncher(SiteLauncher e) {
		siteLauncher = e;
	}
	
	/**
	 * 保存一个从接收器
	 * @param e 
	 */
	public void addSlave(MISucker e) {
		Laxkit.nullabled(e);
		slaves.add(e);
	}

	/**
	 * 保存一批从接收器
	 * @param a 节点
	 */
	public void addSlaves(MISucker[] a) {
		for (MISucker e : a) {
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

		// 内网保持PING通信间隔时间，间隔1秒
		PockTimer timer = new PockTimer();

		while (!isInterrupted()) {
			// 处理各种数据
			int count = subprocess();

			// 定时检测内网定位，让NAT保存这个端口，保持PING通状态
			if (timer.isTimeout()) {
				count += checkPockTimeout();
				timer.refreshTime(); // 刷新时间
			}

			// 以上没有发生，延时1秒
			if (count == 0) {
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
		packets.clear();
		readyTasks.clear();
		packets.clear();
	}

	/**
	 * 判断有内网定位定时检测单元
	 * @return 返回真或者假
	 */
	public boolean hasPocks() {
		super.lockMulti();
		try {
			return pocks.size() > 0;
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 返回ReplySucker在NAT设备上的定位地址
	 * @param remote 网关站点
	 * @return 返回内网的NAT地址
	 */
	public SocketHost findPockLocal(SocketHost remote) {
		// 锁定
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
	 * 返回外部网络主机地址
	 * @return SocketHost实例
	 */
	public SocketHost getDefinePublicHost() {
		return sucker.getDefinePublicHost();
	}

	/**
	 * 返回内部网络主机地址
	 * @return SocketHost实例
	 */
	public SocketHost getDefinePrivateHost() {
		return sucker.getDefinePrivateHost();
	}
	
//	/**
//	 * 判断IP来自公网，并且本地没有这个公网IP地址
//	 * @param from 来源地址
//	 * @return 是公网地址返回真，否则假
//	 */
//	private final boolean isWideAddress(Address from) {
//		return from.isWideAddress() && !Address.contains(from);
//	}

	/**
	 * 以同步加上循环，均匀取出主机地址
	 * @return SocketHost实例
	 */
	private synchronized SocketHost nextSocket() {
		if (slaves.size() == 0) {
			return null;
		}
		if (iterateIndex >= slaves.size()) {
			iterateIndex = 0;
		}
		MISucker mi = slaves.get(iterateIndex++);
		return mi.getDefinePrivateHost();
	}
	
	/**
	 * 根据来源的客户端地址，返回一个适配的接收地址。以下几种情况：
	 * 1. 如果client是公网地址，并且这个地址本地没有，返回ReplySucker的Master地址
	 * 2. 如果是来自内网，返回以轮循方式，分配ReplySucker的子地址
	 * @param client 客户端主机
	 * @return 返回ReplySucker主机地址。
	 */
	public SocketHost findDefinePrivateHost(Address client) {
		boolean wide = ReplyUtil.isWideAddress(client);
		if (wide) {
			return sucker.getDefinePrivateHost();
		}

		// 取内网地址，如果有效的话！
		SocketHost host = nextSocket();
		if (host != null) {
			// Logger.debug(this, "findDefinePrivateHost", "MISucker is %s", host);
			return host;
		}

		// 内网地址
		return sucker.getDefinePrivateHost();
	}

//	/**
//	 * 由REPLY MONTIOR转发一个待处理的FIXP数据包
//	 * @param from 数据来源
//	 * @param b 待发送的数据
//	 * @param off 数据开始下标
//	 * @param len 数据有效长度
//	 * @return 保存成功返回真，否则假
//	 */
//	protected boolean add(SocketHost from, byte[] b, int off, int len) {
//		// 保存原始包
//		PrimitivePacket packet = null;
//		// 防止内存溢出
//		try {
//			packet = new PrimitivePacket(from, b, off, len);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		}
//		if (packet == null) {
//			return false;
//		}
//
//		boolean success = false;
//		boolean empty = false;
//		// 锁定保存
//		super.lockSingle();
//		try {
//			empty = packets.isEmpty();
//			success = packets.add(packet);
//		} catch (Throwable e) {
//			Logger.fatal(e);
//		} finally {
//			super.unlockSingle();
//		}
//
//		// 唤醒线程
//		if (empty && success) {
//			wakeup();
//		}
//
//		return success;
//	}

	/**
	 * 由REPLY MONTIOR转发一个待处理的FIXP数据包
	 * @param from 数据来源
	 * @param b 待发送的数据
	 * @param off 数据开始下标
	 * @param len 数据有效长度
	 * @return 保存成功返回真，否则假
	 */
	protected boolean add(SocketHost from, byte[] b, int off, int len) {
		// 保存原始包
		PrimitivePacket packet = null;
		// 防止内存溢出
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
		PrimitivePacket primitive = null;
		// 锁定
		super.lockSingle();
		try {
			if (packets.size() > 0) {
				primitive = packets.remove(0);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		// 返回FIXP包实例，或者空指针
		if (primitive != null) {
			return primitive.regress();
		}
		return null;
	}

	/**
	 * 处理FIXP数据包
	 * @return 返回包处理统计
	 */
	private int subprocess() {
		int count = 0;

		// 检查接收的包
		int size = packets.size();		
		for (int index = 0; index < size; index++) {
			// 弹出一个辅助包
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
		
		// 激活数据流
		activeFlow();

		// 检查处于等待的任务
		size = checkReadyTasks();
		count += size;

		// 大于0，是处理了数据
		return count;
	}

	/**
	 * 向目标地址连续反馈N个FIXP包
	 * @param packet FIXP数据包
	 * @param count 发送次数
	 * @return 成功发送数据包的次数
	 */
	protected int replyTo(Packet packet, int count) {
		SocketHost remote = packet.getRemote();
		byte[] b = packet.build();

		// 客户机目标地址
		SocketAddress address = remote.getSocketAddress();
		// 生成UDP数据包
		DatagramPacket udp = new DatagramPacket(b, 0, b.length);
		udp.setSocketAddress(address);
		
		int units = 0;
		for (int i = 0; i < count; i++) {
			boolean success = sucker.replyTo(udp);
			if (success) units++;
		}
		return units;

		//		// 生成UDP数据包
		//		int units = 0;
		//		try {
		//			DatagramPacket udp = new DatagramPacket(b, 0, b.length, address);
		//			for (int i = 0; i < count; i++) {
		//				boolean success = sucker.replyTo(udp);
		//				if (success) units++;
		//			}
		//		} catch (SocketException e) {
		//			Logger.error(e);
		//		}
		//		return units;
	}

	/**
	 * 向目标地址反馈一个FIXP包
	 * @param packet FIXP数据包
	 * @return 发送成功返回真，否则假
	 */
	protected boolean replyTo(Packet packet) {
		int size = replyTo(packet, 1);
		return size == 1;
	}

	/**
	 * 注册一个接收器。接收器必须包括有CastToken
	 * @param receiver
	 * @return 保存成功返回真，否则假
	 */
	public boolean push(ReplyReceiver receiver) {
		CastFlag flag = receiver.getCastFlag();

		boolean success = false;
		// 单向锁定，保存！
		super.lockSingle();
		try {
			// 判断不存在
			if (!readyTasks.containsKey(flag)) {
				receiver.setHelper(this);
				success = (readyTasks.put(flag, receiver) == null);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 根据编号，删除异步接收器
	 * @param flag 传输标识
	 * @return 删除成功返回真，否则假
	 */
	protected boolean removeReceiver(ReplyFlag flag) {
		ReplyReceiver receiver = null;
		super.lockSingle();
		try {
			receiver = runTasks.remove(flag);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		boolean success = (receiver != null);
		if (success) {
			Logger.debug(this, "removeReceiver", "receive bytes:%d, send bytes:%d, usedtime:%d ms",
					receiver.getReceiveFlowSize(), receiver.getSendFlowSize(),
					receiver.getRunTime());
		}
		return success;
	}

	/**
	 * 根据标识，找到对应的接收器
	 * @param flag 异步快速传输标识
	 * @return 返回实例，或者空指针
	 */
	private ReplyReceiver findReceiver(ReplyFlag flag) {
		super.lockMulti();
		try {
			return runTasks.get(flag);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 从就绪状态切换到运行状态
	 * @param from 来源地址
	 * @param flag 异步通信标识
	 * @return 切换成功返回真，否则假
	 */
	private boolean switchTo(SocketHost from, CastFlag flag) {
		boolean success = false;
		ReplyFlag target = new ReplyFlag(from, flag.getCode());

		ReplyReceiver receiver = null;
		// 锁定
		super.lockSingle();
		try {
			receiver = readyTasks.remove(flag);
			if (receiver != null) {
				receiver.setReplyFlag(target); // 设置异步通信标识
				success = (runTasks.put(target, receiver) == null);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		//		// 启动线程
		//		if (success && receiver != null) {
		//			receiver.start();
		//		}
		
		// 启动线程
		if (success) {
			success = (receiver != null);
			if (success) {
				success = receiver.start();
			}
		}

//		Logger.debug(this, "switchTo", success, "from [%s], cast flag:[%s]", from, flag);

		return success;
	}

	/**
	 * 反馈HELO OKAY包
	 * @param from 来源地址
	 * @param flag 异步通信标识
	 */
	private void replyHeloOkay(SocketHost from, CastFlag flag) {
		Packet reply = new Packet(Answer.CASTHELO_OKAY);
		reply.setRemote(from);
		reply.addMessage(MessageKey.CAST_FLAG, flag);

		// 建立UDP流量方案，设置流量参数给FIXP应答数据包
		FlowSketch sketch = FlowMonitor.getInstance().create(from.getAddress(), flag.getCode());
		if (sketch != null) {
			sketch.influx(reply);
		}
		
		// 发送包
		replyTo(reply, 3);

//		// 发送包
//		int count = replyTo(reply, 3);
//		Logger.debug(this, "replyHeloOkay", "local [%s] send to [%s], count %d", 
//				sucker.getBindHost(), from, count);
	}

	/**
	 * 分发处理 <br><br>
	 * 
	 * 4种情况：<br>
	 * 1. 内网定位（用于FRONT->GATE之间）。<br>
	 * 2. 是CAST_HELO包，接收器切换到运行状态。 <br>
	 * 3. FIXP TEST，测试网络连通
	 * 4. 其它包，找到运行状的接收器处理。<br>
	 * 
	 * @param packet FIXP数据包
	 */
	private void distribute(Packet packet) {
		Mark mark = packet.getMark();

		// 判断是内网定位
		if (Answer.isShineAccepted(mark)) {
			replyPock(packet);
		} else if(Assert.isShine(mark)) {
			shine(packet);
		}
		// UDP网络通信测试，无条件接受！
		else if(Assert.isTest(mark)) {
			test(packet);
		}
		// 两种通信
		else if (Assert.isCastHelo(mark)) {
			// 找到标记，切换到运行状态
			CastFlag flag = readFlag(packet);
			boolean success = (flag != null);
			if (success) {
				success = switchTo(packet.getRemote(), flag);
			}
			// 生成应答包，投递给来源地址
			if (success) {
				replyHeloOkay(packet.getRemote(), flag);
			}
		}
		// 其它情况，从包中拿出异步通信码，进而找到异步接收器
		else {
			SocketHost remote = packet.getRemote();
			CastCode code = readCode(packet);
			if (code == null) {
				Logger.error(this, "distribute", "must be cast code!");
				return;
			}
			ReplyFlag flag = new ReplyFlag(remote, code);
			ReplyReceiver receiver = findReceiver(flag);
			// 把数据包推送给接收器处理
			if (receiver != null) {
				// 保存包
				receiver.add(packet);
				
				// 保存激活流
				addActiveFlow(remote.getAddress(), code);
			}
		}
	}
	
	/**
	 * 保存激活的数据流实例
	 * @param address
	 * @param code
	 */
	private void addActiveFlow(Address address, CastCode code) {
		activeFlags.add(new FlowFlag(address, code));
	}

	/**
	 * 激活数据流
	 */
	private void activeFlow() {
		if (activeFlags.isEmpty()) {
			return;
		}

		for (FlowFlag flag : activeFlags) {
			FlowMonitor.getInstance().active(flag.getAddress(), flag.getCode());
		}
		// 清除全部
		activeFlags.clear();
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
		super.lockSingle();
		try {
			Iterator<Map.Entry<CastFlag, ReplyReceiver>> iterator = readyTasks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<CastFlag, ReplyReceiver> entry = iterator.next();
				ReplyReceiver receiver = entry.getValue();
				// 达到最大静默时间，删除！
				boolean success = receiver.isPacketTimeout(getDisableTimeout());
				if (success) {
					array.add(entry.getKey());
					// 停止线程
					receiver.stop();
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

		int count = array.size();
		// 删除
		if (count > 0) {
			for (CastFlag flag : array) {
				Logger.error(this, "checkReadyTasks", "delete timeout ReplyReceiver %s", flag);
			}
		}
		
		return count;
	}

	/** 以下是内网检测定位代码 **/

	/** 内网检测超时，默认是10秒 **/
	private long pockTimeout = 10000;

	/** 目标地址 -> 命令钩子 **/
	private Map<SocketHost, HostHook> hooks = new TreeMap<SocketHost, HostHook>();

	/** 网关站点 -> 内网定位定时检测单元 **/
	private Map<SocketHost, PockItem> pocks = new TreeMap<SocketHost, PockItem>();

	/**
	 * 设置内网检测超时
	 * @param ms 毫秒
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
	 * 向目标地址发送SHINE通知，定位自己的ReplySucker地址
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

		// 发送数据包到网关的ReplyDispatcher地址
		return replyTo(request, count);
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
		// 锁定
		super.lockSingle();
		try {
			pocks.put(item.getRemote(), item);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 向客户端反馈它内网的出口IP地址和端口
	 * @param request 请求包
	 */
	private void shine(Packet request) {
		// 来源地址
		SocketHost remote = request.getRemote();
		// 取出来源主机地址
		byte[] b = request.findRaw(	MessageKey.HOST);

		// 生成应答包
		Packet resp = new Packet(remote, Answer.SHINE_ACCEPTED);
		resp.addMessage(MessageKey.HOST, b); // 原样返回
		resp.addMessage(MessageKey.SPEAK, "re:shine!");
		// 把来源客户端地址返送回去
		resp.setData(remote.build());
		// 发送应答包
		replyTo(resp);

		Logger.debug(this, "shine", "from %s", remote);
	}

	/**
	 * FIXP UDP网络连通测试
	 * @param request 请求包
	 */
	private void test(Packet request) {
		// 取来源地址
		SocketHost remote = request.getRemote();
		// 生成应答包包
		Packet resp = new Packet(remote, Answer.ACCEPTED);
		resp.addMessage(MessageKey.SPEAK, "RE:TEST!");
		// 返回来源主机地址
		byte[] b = remote.build();
		resp.addMessage(MessageKey.HOST, b);
		// 发送应答包
		replyTo(resp);

		Logger.debug(this, "test", "from %s", remote);
	}

	/**
	 * 处理返回的内网定位
	 * @param resp 应答包
	 */
	private void replyPock(Packet resp) {
		// 目标方地址
		byte[] a = resp.findRaw(MessageKey.HOST);
		SocketHost remote = new SocketHost(new ClassReader(a));

		// 本地主机，或者NAT设备出口地址
		byte[] b = resp.getData();
		// 本地主机
		SocketHost local = new SocketHost(new ClassReader(b));

		// 锁定处理
		super.lockSingle();
		try {
			// 钩子必须存在！
			HostHook hook = hooks.get(remote);
			if (hook != null) {
				hook.setLocal(local);
				// 撤销它
				hooks.remove(remote);
			}

			// 检测单元有效
			PockItem item = pocks.get(remote);
			if (item != null) {
				// 更新时间
				item.refreshTime();
				
				/**
				 * 注意，存在两种可能：
				 * 1. 如果没有定义服务器socket地址，保存！
				 * 2. 前后服务器socket地址不匹配，说明NAT/路由设备已经更新了端口，本处同步更新！
				 */
				if (item.getLocalNAT() == null) {
					item.setLocalNAT(local);
					Logger.warning(this, "replyPock", "set pock socket! %s", local);
				} else if (Laxkit.compareTo(item.getLocalNAT(), local) != 0) {
					item.setLocalNAT(local);
					Logger.warning(this, "replyPock", "replace to pock socket! %s", local);
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "replyPock",  "remote %s, local %s", remote, local);
	}

	/**
	 * 检测内网定位超时
	 * @return 返回PIN统计
	 */
	private int checkPockTimeout() {
		// 空集，忽略
		if (pocks.isEmpty()) {
			return 0;
		}
		
		ArrayList<SocketHost> deletes = new ArrayList<SocketHost>();
		ArrayList<SocketHost> disables = new ArrayList<SocketHost>();
		// 检测超时单元
		ArrayList<SocketHost> timoues = new ArrayList<SocketHost>();
		// 检测超时
		super.lockMulti();
		try {
			Iterator<Map.Entry<SocketHost, PockItem>> iterator = pocks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, PockItem> entry = iterator.next();
				PockItem item = entry.getValue();
				// 三种情况：1. 删除超时 2. 失效超时 3.一般超时
				if (item.isTimeout(pockTimeout * 5)) {
					deletes.add(entry.getKey());
				} else if (item.isTimeout(pockTimeout * 2)) {
					disables.add(entry.getKey());
				} else if (item.isTimeout(pockTimeout)) {
					item.refreshTime();
					timoues.add(entry.getKey());
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
		// 正常超时，发送1个数据包
		for (SocketHost remote : timoues) {
			sendPockPacket(remote, 1);
		}

		return timoues.size();
	}

	/**
	 * 本地ReplySucker地址被远程ReplyDispatcher确认
	 * @param remote ReplyDispatcher节点地址
	 * @param timeout 超时时间，单位：毫秒
	 * @param count 测试统计
	 * @return 返回被ReplyDispatcher测试的本地ReplySucker地址
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
	 * 可能位于内网节点（FRONT）向网关上的ReplyDispatcher地址发送数据包，确定自己在NAT设备上的地址。
	 * 
	 * @param remote 目标站点地址
	 * @param timeout 超时时间，单位：毫秒
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
//	 * 内网REPLY SUCKER服务器建立与网关的REPLY DISPATCHER服务器的映射，定位内网REPLY SUCKER服务器在NAT设备上的地址。
//	 * 
//	 * @param remote 网关上的ReplyDispatcher地址
//	 * @param local 内网的ReplySucker地址，由网关上的ReplyDispatcher提供。
//	 */
//	public void addPock(SocketHost remote, SocketHost local) {
//		// 空指针检查
//		Laxkit.nullabled(remote);
//		Laxkit.nullabled(local);
//
//		// 保存到内存
//		PockItem item = new PockItem(remote.duplicate(), local.duplicate());
//		addPock(item);
//
//		// 发送一个包
//		sendPockPacket(remote, 1);
//
//		Logger.debug(this, "addPock", "save remote %s , local %s", remote, local);
//	}

	/**
	 * 内网REPLY SUCKER服务器建立与网关的REPLY DISPATCHER服务器的映射，定位内网REPLY SUCKER服务器在NAT设备上的地址。
	 * 
	 * @param hub 服务器主机地址
	 * @param remote 网关上的ReplyDispatcher地址
	 * @param localNAT 内网的ReplySucker出口地址（NAT地址），由网关上的ReplyDispatcher提供。
	 */
	public void addPock(Node hub, SocketHost remote, SocketHost localNAT) {
		// 空指针检查
		Laxkit.nullabled(remote);
		Laxkit.nullabled(localNAT);

		// 保存到内存
		PockItem item = new PockItem(hub, remote, localNAT);
		addPock(item);

		// 发送一个包
		sendPockPacket(remote, 1);

		Logger.debug(this, "addPock", "save server node %s # server reply dispatcher %s # local nat %s",
				hub, remote, localNAT);
	}

	
	/**
	 * 删除内网检测单元
	 * @param remote ReplyDispather服务器地址
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
	 * 根据服务端节点地址，查找匹配的ReplyDispatcher主机地址
	 * @param hub 服务器节点
	 * @return 返回真或者假
	 */
	public SocketHost findReplyDispatcherByHub(Node hub) {
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<SocketHost, PockItem>> iterator = pocks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, PockItem> entry = iterator.next();
				PockItem item = entry.getValue();
				// 判断一致！
				if (item.getHub() != null && Laxkit.compareTo(item.getHub(), hub) == 0) {
					return item.getRemote(); // 返回ReplyDispatcher主机地址
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		return null;
	}
	
	/**
	 * 判断有匹配的内网检测单元
	 * @param hub 服务器节点，允许空指针
	 * @param remote ReplyDispatcher主机地址
	 * @return 返回真或者假
	 */
	public boolean hasPock(Node hub, SocketHost remote) {
		// 空指针检查
		Laxkit.nullabled(remote);

		boolean success = false;
		// 锁定
		super.lockMulti();
		try {
			Iterator<Map.Entry<SocketHost, PockItem>> iterator = pocks.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<SocketHost, PockItem> entry = iterator.next();
				PockItem item = entry.getValue();
				success = (Laxkit.compareTo(item.getHub(), hub) == 0)
						&& (Laxkit.compareTo(item.getRemote(), remote) == 0);
				if (success) {
					break;
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "hasPock", success, "check %s - %s", hub, remote);

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
		List<PockItem> list = getPocks() ;
		PockItem[] a = new PockItem[list.size()];
		return list.toArray(a);
	}

	/**
	 * 清除UDP包数据
	 */
	public void reset() {
		Logger.info(this, "reset", "hook count: %d, pock count: %d",
				hooks.size(), pocks.size());

		// 清除本地保存的记录
		super.lockSingle();
		try {
			Iterator<Map.Entry<SocketHost, HostHook>> iterator = hooks.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<SocketHost, HostHook> entry = iterator.next();
				// 唤醒钩子，释放资源
				entry.getValue().done();
			}
			// 清除记录
			hooks.clear();
			pocks.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

}