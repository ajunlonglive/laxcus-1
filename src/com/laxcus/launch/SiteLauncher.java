/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.interfaces.*;
import java.util.*;

import org.w3c.dom.*;

import com.laxcus.command.login.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.fixp.*;
import com.laxcus.fixp.client.*;
import com.laxcus.fixp.monitor.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.invoke.*;
import com.laxcus.launch.licence.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.remote.client.hub.*;
import com.laxcus.site.*;
import com.laxcus.site.Node;
import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.cyber.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.*;
import com.laxcus.visit.impl.*;
import com.laxcus.xml.*;

/**
 * 站点启动器。<br>
 * 站点启动器是所有站点进程的基类，它在初始化时绑定一个本地的网络通信地址，以后也不再改变。<br><br>
 * 
 * 进程启动器提供的功能：<br>
 * 1. 产生序列号（这个编号是一个长整型数字，从0-Long.MAV_VALUE，可以保证短时间内不重复）。<br>
 * 2. 序列号结合站点地址，为异步命令产生回显地址。<br>
 * 3. FIXP服务器的网络监听、数据接收、数据发送服务（TCP/UDP/RPC）。<br>
 * 4. 向上级管理站点注册/注销，定时激活（HELLO命令，20秒一次）。<br>
 * 5. 切换注册站点（SWITCH HUB）。<br>
 * 6. 接受远程关闭（关闭命令的来源必须是配置中许可的地址）。 <br> <br>
 * 
 * 调用INVOKE/PRODUCE方式的异步重新注册，有两个方法：checkin / touch，checkin用于低延时情况（2秒内），touch用于高延时（1分钟）左右，子类方法调用时，视业务需求选择，touch的时延由当前节点的管理节点决定。
 * 
 * @author scott.liang 
 * @version 1.32 3/5/2015
 * @since laxcus 1.0
 */
public abstract class SiteLauncher extends SitePrecursor {

	/** 当前操作系统平台 **/
	private Platform platform = Platform.NONE;

	/** HUB节点记录子节点更新注册间隔时间 **/
	private HubTimer hubTimer = new HubTimer();

	/** 注册延时触发器，用于非重要场景，如子节点的登录/注销 **/
	protected RegisterTimer registerTimer = new RegisterTimer();

	/** 异步RPC转发器 **/
	private EchoAgent agent = new EchoAgent();

	/** FIXP远程过程调用(RPC)适配器 */
	private VisitAdapter visitAdapter = new VisitAdapter();

	/** FIXP数据包调用接口（由子类在构造时分配） **/
	private PacketInvoker packetInvoker;

	/** FIXP数据流调用接口（由子类在构造时分配） **/
	private StreamInvoker streamAdapter;

	/** FIXP数据流监听服务器 **/
	protected FixpStreamMonitor streamMonitor = new FixpStreamMonitor();

	/** FIXP数据包监听服务器（默认启动3个数据包处理线程） **/
	protected FixpPacketMonitor packetMonitor = new FixpPacketMonitor();

	/** 授权远程关闭的网络地址列表 **/
	private ShutdownSheet shutdownSheet = new ShutdownSheet();

	/** 异步反馈数据接收器，位于数据处理的请求端，读取ReplyDispatcher发来的数据。**/
	protected ReplySucker replySucker = new ReplySucker();

	/** 子级数据包接收器 **/
	private MISucker[] slaveSuckers;

	/** 异步反馈数据分送器，位于数据处理的服务端，向ReplySucker发送数据。 **/
	protected ReplyDispatcher replyDispatcher = new ReplyDispatcher();

	/** 子级数据包接收器 **/
	private MODispatcher[] slaveDispatchers;

	/** 各站点资源配置目录 */
	private File resourcePath;

	/** 序列号生成器，在0 - Long.MAX_VALUE之间循环，每次递增一个长整值。由于长整值范围极大，短时间内不会重复。 **/
	private SerialGenerator generator = new SerialGenerator();

	/** 启动开始时间  **/
	private long launchTime;

	/** 以异步模式重新注册 **/
	private volatile boolean checkin;

	/** 计时器，定期重复发送的操作，通过计时器调用完成 **/
	protected ScheduleTimer timer = new ScheduleTimer();

	/** 内存释放器 **/
	private MemoryReleaser releaser = new MemoryReleaser();

	/** 许可证时间 **/
	private LicenceToken licenceToken = new LicenceToken();

	/**
	 * 进行安全许可检查
	 * @param method 被调用的命令方法名
	 */
	protected static void check(String method) {
		// 安全检查
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			String name = String.format("using.%s", method);
			sm.checkPermission(new SiteLauncherPermission(name));
		}
	}

	/**
	 * 构造站点启动器，指定日志打印器
	 * @param printer 日志打印接口，或者空指针。
	 */
	protected SiteLauncher(LogPrinter printer) {
		super();

		// 日志打印句柄
		if (printer != null) {
			Logger.setLogPrinter(printer);
		}

		// 异步重新注册是FALSE。
		setCheckin(false);

		// 定义启动开始时间
		launchTime = System.currentTimeMillis();

		// 初始化系统配置
		initSystemConfig();
		// 判断平台
		checkPlatform();
		// 资源定时释放/检索
		loadResourceScanner();

		// 设置站点启动器
		getPacketHelper().setSiteLauncher(this);
		getReplyHelper().setSiteLauncher(this);
	}

	/**
	 * 构造站点启动器，指定它的站点属性
	 */
	protected SiteLauncher() {
		this(null);
	}

	/**
	 * 配置系统资源
	 */
	private void initSystemConfig() {
		// 设置管理池的站点启动器句柄
		VirtualPool.setLauncher(this);

		// 设置RPC异步应答接口
		DoubleClient.setEchoAgent(agent);
		DoubleClient.setLauncher(this);
		EchoTraveller.setLauncher(this);

		// 设置RPC异步应答接收接口
		EchoBuffer.setEchoAgent(agent);
		EchoBuffer.setLauncher(this);
		// 走FIXP UDP服务器信道的RPC
		EchoCustomer.setLauncher(this);

		EchoCustomer.setEchoMessenger(packetMonitor.getPacketHelper());
	}

	/**
	 * 判断操作系统
	 */
	private void checkPlatform() {
		if (Laxkit.isLinux()) {
			platform = Platform.LINUX;
		} else if (Laxkit.isWindows()) {
			platform = Platform.WINDOWS;
		}
	}

	/**
	 * 返回当前LAXCUS分布式操作系统版本
	 * @return Version实例
	 */
	public final Version getVersion() {
		return Version.current;
	}

	/**
	 * 加载定时资源检索
	 */
	private void loadResourceScanner() {
		// 间隔60秒触发一次
		timer.schedule(releaser, 10000, 60000);
		// 绑定密钥存储器
		packetMonitor.attachSecureCollector(timer);

		// 判断平台，选择不同的平台检测器
		if (isLinux()) {
			LinuxDevice.getInstance().setSiteLauncher(this);
		} else if (isWindows()) {
			WindowsDevice.getInstance().setSiteLauncher(this);
		}
		ScanEffector.setSiteLauncher(this);
	}

	/**
	 * 判断当前站点是网关
	 * @return 返回真或者假
	 */
	public boolean isGateway() {
		return SiteTag.isGateway(getFamily());
	}

	/**
	 * 返回MASSIVE MIMO的MI单元数量
	 * @return MI单元数量
	 */
	public int getMIMembers() {
		return slaveSuckers == null ? 0 : slaveSuckers.length;
	}

	/**
	 * 返回MASSIVE MIMO的MO单元数量
	 * @return MO单元数量
	 */
	public int getMOMembers() {
		return slaveDispatchers == null ? 0 : slaveDispatchers.length;
	}

	/**
	 * 设置TCP命令流映射端口
	 * @param port 映射端口号
	 */
	public boolean setReflectStreamPort(int port) {
		boolean success = isGateway();
		if (success) {
			getSite().setReflectTCPort(port);
			streamMonitor.setReflectPort(port);
		}
		return success;
	}

	/**
	 * 返回TCP命令流映射端口
	 * @return 映射端口号
	 */
	public int getReflectStreamPort() {
		return streamMonitor.getReflectPort();
	}

	/**
	 * 设置UDP命令流映射端口
	 * @param port 映射端口号
	 */
	public boolean setReflectPacketPort(int port) {
		boolean success = isGateway();
		if (success) {
			getSite().setReflectUDPort(port);
			packetMonitor.setReflectPort(port);
		}
		return success;
	}

	/**
	 * 返回UDP命令流映射端口
	 * @return 映射端口号
	 */
	public int getReflectPacketPort() {
		return packetMonitor.getReflectPort();
	}

	/**
	 * 设置UDP数据流接收映射端口
	 * @param port 映射端口号
	 */
	public boolean setReflectSuckerPort(int port) {
		boolean success = isGateway();
		if (success) {
			replySucker.setReflectPort(port);
		}
		return success;
	}

	/**
	 * 返回UDP数据流接收映射端口
	 * @return 映射端口号
	 */
	public int getReflectSuckerPort() {
		return replySucker.getReflectPort();
	}

	/**
	 * 设置UDP数据流发送映射端口
	 * @param port 映射端口号
	 */
	public boolean setReflectDispatcherPort(int port) {
		boolean success = isGateway();
		if (success) {
			replyDispatcher.setReflectPort(port);
		}
		return success;
	}

	/**
	 * 返回UDP数据流发送映射端口
	 * @return 映射端口号
	 */
	public int getReflectDispatcherPort() {
		return replyDispatcher.getReflectPort();
	}

	/**
	 * 返回节点使用者的签名，匹配许可证中的签名。<br>
	 * 只限于服务端的节点，包括：TOP/BANK/HOME, ACCOUNT/HASH/GATE/ENTRANCE, DATA/WORK/BUILD/CALL, LOG <br>
	 * 不包括：FRONT/WATCH。<br>
	 * 
	 * @return 字符串，没有是空指针。
	 */
	public String getSignature() {
		String name = System.getProperty("laxcus.signature");
		if (name == null || name.trim().isEmpty()) {
			return null;
		}
		return name.trim();
	}

	/**
	 * 设置许可证签名
	 * @param signature 签名
	 */
	public void setSignature(String signature) {
		if (signature != null) {
			System.setProperty("laxcus.signature", signature);
		}
	}

	/**
	 * 设置内存释放间隔时间，大于0有效，小于等于0不启动
	 * @param ms 毫秒
	 */
	public void setReleaseMemoryInterval(long ms) {
		releaser.setInterval(ms);
	}

	/**
	 * 返回内存释放间隔时间
	 * @return 毫秒为单位的内存释放间隔时间
	 */
	public long getReleaseMemoryInterval() {
		return releaser.getInterval();
	}

	/**
	 * 判断是LINUX操作系统
	 * @return 判断成立返回真，否则假
	 */
	public boolean isLinux() {
		return platform == Platform.LINUX;
	}

	/**
	 * 判断是WINDOWS操作系统
	 * @return 判断成立返回真，否则假
	 */
	public boolean isWindows() {
		return platform == Platform.WINDOWS;
	}

	/**
	 * 保存LINUX/WINDOWS的定时检测目录，磁盘空间不足将报警。
	 * @param path 磁盘目录名
	 * @return 成功返回真，否则假
	 */
	protected boolean addDeviceDirectory(String path) {
		if (isLinux()) {
			return LinuxDevice.getInstance().addDirectory(path);
		} else if (isWindows()) {
			return WindowsDevice.getInstance().addDirectory(path);
		}
		return false;
	}

	/**
	 * 保存LINUX/WINDOWS定时检测目录。磁盘空间不足将报警
	 * @param dir 磁盘目录
	 * @return 成功返回真，否则假
	 */
	protected boolean addDeviceDirectory(File dir) {
		if (isLinux()) {
			return LinuxDevice.getInstance().addDirectory(dir);
		} else if (isWindows()) {
			return WindowsDevice.getInstance().addDirectory(dir);
		}
		return false;
	}

	/**
	 * 保存日志设备目录，定时检测磁盘不足，发生则报警
	 * 只有在日志写入本地时才生效。
	 */
	protected void loadLogDeviceDirectory() {
		boolean success = Logger.isSendToDisk();
		if (success) {
			addDeviceDirectory(Logger.getDirectory());
		}
	}

	/**
	 * 保存追踪设备目录，定时检测磁盘不足，发生则报警
	 * 只有在追踪记录写入本地时才生效。
	 */
	protected void loadTigDeviceDirectory() {
		boolean success = Tigger.isSendToDisk();
		if (success) {
			addDeviceDirectory(Tigger.getDirectory());
		}
	}

	/**
	 * 保存消费目录，定时检测磁盘不足，发生则报警
	 * 只有在追踪记录写入本地时才生效。
	 */
	protected void loadBillDeviceDirectory() {
		boolean success = Biller.isSendToDisk();
		if (success) {
			addDeviceDirectory(Biller.getDirectory());
		}
	}

	/**
	 * 返回计时器句柄
	 * @return Timer实例
	 */
	public Timer getTimer() {
		// 安全检查
		SiteLauncher.check("getTimer");
		// 返回实例
		return timer;
	}

	/**
	 * 设置重新注册。
	 * 当这个参数为真时，线程达到条件重新注册
	 * @param b
	 */
	protected void setCheckin(boolean b) {
		checkin = b;
	}

	/**
	 * 判断重新注册
	 * @return 返回真或者假
	 */
	protected boolean isCheckin() {
		return checkin;
	}

	/**
	 * 立即注册。<br>
	 * 
	 * 以异步（INVOKE/PRODUCE）方式，要求当前节点重新注册到管理站点。
	 * 
	 * @param immediately 要求立即唤醒线程执行或者否
	 */
	public void checkin(boolean immediately) {
		setCheckin(true);
		// 如果立即执行，就唤醒线程
		if (immediately) {
			wakeup();
		}
	}

	/**
	 * 立即注册
	 * 以异步（INVOKE/PRODUCE）方式，要求当前节点立即重新注册到管理节点
	 */
	public void checkin() {
		checkin(true);
	}

	/**
	 * 延迟注册。<br>
	 * 
	 * 以异步（INVOKE/PRODUCE）方式，要求当前节点达到规定的超时时间后，重新注册到管理节点。<br>
	 * 
	 * 这个操作用于非重要场景，比如当前节点有新的子节点“加入/退出/更新”，或者元数据发生更新时。
	 */
	public void touch() {
		registerTimer.touch();
	}

	/**
	 * 服务器延规定的一般迟注册间隔时间
	 * @return 以毫秒为单位的时间
	 */
	public long getHubRegisterInterval() {
		return hubTimer.getRegisterInterval();
	}

	/**
	 * 服务器规定的最大延时注册间隔时间
	 * @return 以毫秒为单位的时间
	 */
	public long getHubMaxRegisterInterval() {
		return hubTimer.getMaxRegisterInterval();
	}

	/**
	 * 返回启动开始时间
	 * @return 启动开始时间
	 */
	public final long getLaunchTime() {
		return launchTime;
	}

	/**
	 * 统计运行时间
	 * @return 运行时间
	 */
	public final long getRunTime() {
		return System.currentTimeMillis() - launchTime;
	}

	/**
	 * 返回一个序列编号，在一段时间内唯一。
	 * 序列编号在 0 - Long.MAX_VALUE之间循环。
	 * @return 长整型正数
	 */
	public long nextIterateIndex() {
		return generator.nextSerial();
	}

	/**
	 * 返回启动进程的站点属性
	 * @return 站点类型
	 */
	public final byte getFamily() {
		return getSite().getFamily();
	}

	/**
	 * 返回站点监听地址。<br>
	 * 如果当前站点是网关，是它的内网地址
	 * 
	 * @return Node实例
	 */
	public final Node getListener() {
		return getSite().getNode();
	}

	/**
	 * 返回FIXP数据流服务器监听地址
	 * @return SocketHost实例
	 */
	public final SocketHost getStreamHost() {
		return streamMonitor.getLocal();
	}

	/**
	 * 返回FIXP数据包服务器监听地址
	 * @return SocketHost实例
	 */
	public final SocketHost getPacketHost() {
		return packetMonitor.getLocal();
	}
	
	/**
	 * 返回控制信道的绑定主机地址
	 * @return SocketHost实例
	 */
	public final SocketHost getPacketBindHost() {
		return packetMonitor.getBindHost();
	}

	/**
	 * 返回UDP消息信使
	 * @return PacketMessenger实例类
	 */
	public PacketMessenger getPacketMessenger() {
		return packetMonitor;
	}
	
	/**
	 * 返回数据信道的接收地址，真实地址
	 * @return SocketHost实例
	 */
	public final SocketHost getSuckerBindHost() {
		return replySucker.getBindHost();
	}
	
	/**
	 * 返回数据信道的发送地址，是真实地址
	 * @return SocketHost实例
	 */
	public final SocketHost getDispatcherBindHost() {
		return this.replyDispatcher.getBindHost();
	}

	/**
	 * 设置数据包调用接口
	 * @param e PacketInvoker实现类
	 */
	protected void setPacketInvoker(PacketInvoker e) {
		// 如果没有设置包的转发器，当前即是
		if (e.getPacketTransmitter() == null) {
			e.setPacketTransmitter(packetMonitor);
		}
		packetInvoker = e;
	}

	/**
	 * 返回数据包调用接口
	 * @return PacketInvoker实现类
	 */
	protected PacketInvoker getPacketInvoker() {
		return packetInvoker;
	}

	/**
	 * 设置数据流调用接口
	 * @param e StreamInvoker实现类
	 */
	protected void setStreamInvoker(StreamInvoker e) {
		streamAdapter = e;
	}

	/**
	 * 返回数据流调用接口
	 * @return StreamInvoker实现类
	 */
	protected StreamInvoker getStreamInvoker() {
		return streamAdapter;
	}

	/**
	 * 根据地址删除密文。在注册和注销时删除
	 * @param endpoint SOCKET地址
	 * @return 返回被删除的密钥，没有是空指针
	 */
	public Cipher dropCipher(SocketHost endpoint) {
		return packetMonitor.dropCipher(endpoint);
	}

	/**
	 * 根据节点删除密文
	 * @param node 节点
	 * @return 返回被删除的密钥，没有是空指针
	 */
	public Cipher dropCipher(Node node) {
		return dropCipher(node.getPacketHost());
	}

	/**
	 * 根据地址删除密文。当某个节点停止发送HELO时，宿主管理池在清除注册站点时，同步清除密文记录。
	 * @param endpoint SOCKET地址
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeCipher(SocketHost endpoint) {
		return packetMonitor.removeCipher(endpoint);
	}

	/**
	 * 根据节点删除密文
	 * @param node 节点
	 * @return 删除成功返回真，否则假
	 */
	public boolean removeCipher(Node node) {
		return removeCipher(node.getPacketHost());
	}

	/**
	 * 根据地址查找密文
	 * @param endpoint SOCKET地址
	 * @return 返回密文，或者空指针
	 */
	public Cipher findCipher(SocketHost endpoint) {
		return packetMonitor.findCipher(endpoint);
	}

	/**
	 * 根据节点查找密文
	 * @param node 节点地址
	 * @return 返回密文，或者空指针
	 */
	public Cipher findCipher(Node node) {
		return findCipher(node.getPacketHost());
	}

	/**
	 * 启动异步反馈监听。<br>
	 * 接收器线程优先最大，其它线程为普通优先级。
	 * 
	 * @return 成功返回真，否则假
	 */
	private boolean loadReplyListen() {
		boolean success = replySucker.start(Thread.MAX_PRIORITY);
		if (success) {
			success = replyDispatcher.start();
		}

		// 等待异步反馈监听器进入线程状态
		if (success) {
			do {
				delay(200);
			} while (!replySucker.isRunning());
			do {
				delay(200);
			} while (!replyDispatcher.isRunning());
		}

		// 启动Massive MIMO服务
		if (success) {
			success = loadMIListen();
		}
		if (success) {
			success = loadMOListen();
		}

		return success;
	}

	/**
	 * 启动Massive MIMO的多输入服务
	 * @return 成功返回真，否则假
	 */
	private boolean loadMIListen() {
		final int size = (slaveSuckers != null ? slaveSuckers.length : 0);
		if (size < 1) {
			return true;
		}

		// 循次启动
		for (int n = 0; n < size; n++) {
			// 定义内网地址，只在Massive MIMO只在内网使用
			slaveSuckers[n].setDefinePrivateIP(replySucker.getDefinePrivateIP());
			// 启动
			boolean success = slaveSuckers[n].start(Thread.MAX_PRIORITY);
			if (!success) {
				return false;
			}
			delay(100); // 延时100毫秒
		}

		// 等待结果
		do {
			int count = 0;
			for (int n = 0; n < size; n++) {
				if (slaveSuckers[n].isRunning()) {
					count++;
				} else {
					break;
				}
			}
			// 保存节点
			if (count == size) {
				replySucker.getHelper().addSlaves(slaveSuckers);
				break;
			}
			delay(100);
		} while (true);

		return true;
	}

	/**
	 * 启动Massive MIMO的多输出服务
	 * @return 成功返回真，否则假
	 */
	private boolean loadMOListen() {
		final int size = (slaveDispatchers != null ? slaveDispatchers.length : 0);
		if (size < 1) {
			return true;
		}

		// 循次启动
		for (int n = 0; n < size; n++) {
			// 定义内网地址，只在内网使用
			slaveDispatchers[n].setDefinePrivateIP(replyDispatcher.getDefinePrivateIP());
			// 启动
			boolean success = slaveDispatchers[n].start(Thread.MAX_PRIORITY);
			if (!success) {
				return false;
			}
			delay(100);
		}

		// 等待结果
		do {
			int count = 0;
			for (int n = 0; n < size; n++) {
				if (slaveDispatchers[n].isRunning()) {
					count++;
				} else {
					break;
				}
			}
			if (count == size) {
				replyDispatcher.getWorker().addSlaves(slaveDispatchers);
				break;
			}
			delay(100);
		} while (true);

		return true;
	}

	/**
	 * 关闭异步反馈监听
	 */
	private void stopReplyListen() {
		// 停止Massive MIMO
		stopMODispatchers();
		stopMISuckers();

		//停止 ReplySucker / ReplyDispatcher
		ThreadStick s1 = new ThreadStick();
		ThreadStick s2 = new ThreadStick();

		boolean suckRunning = replySucker.isRunning();
		if (suckRunning) {
			replySucker.stop(s1);
		}
		boolean dispRunning = replyDispatcher.isRunning();
		if (dispRunning) {
			replyDispatcher.stop(s2);
		}

		// 等等FIXP数据流服务器释放
		while (suckRunning) {
			delay(200);
			if (s1.isOkay()) break;
		}
		// 等待FIXP数据包服务器释放
		while (dispRunning) {
			delay(200);
			if (s2.isOkay()) break;
		}
	}

	/**
	 * 停止Massive MIMO的多输入
	 */
	private void stopMISuckers() {
		// 判断成员数目
		final int size = (slaveSuckers != null ? slaveSuckers.length : 0);
		if (size < 1) {
			return;
		}
		ThreadStick[] sticks = new ThreadStick[size];
		for (int index = 0; index < size; index++) {
			sticks[index] = new ThreadStick();
			// 判断是运行状态
			boolean running = slaveSuckers[index].isRunning();
			if (running) {
				slaveSuckers[index].stop(sticks[index]);
			} else {
				sticks[index].wakeup(); // 不在运行状态，唤醒
			}
		}
		// 判断全部线程撤销
		do {
			delay(200);
			// 判断
			int count = 0;
			for (int index = 0; index < size; index++) {
				if (sticks[index].isOkay()) {
					count++;
				} else {
					break;
				}
			}
			// 如果达到规定值，退出，否则延时200毫秒
			if (count == size) {
				break;
			}
		} while (true);
	}

	/**
	 * 停止Massive MIMO的多输出
	 */
	private void stopMODispatchers() {
		// 判断成员数目
		final int size = (slaveDispatchers != null ? slaveDispatchers.length : 0);
		if (size < 1) {
			return;
		}
		// 停止
		ThreadStick[] sticks = new ThreadStick[size];
		for (int index = 0; index < size; index++) {
			sticks[index] = new ThreadStick();
			// 判断是运行状态
			boolean running = slaveDispatchers[index].isRunning();
			if (running) {
				slaveDispatchers[index].stop(sticks[index]);
			} else {
				sticks[index].wakeup(); // 不在运行状态，唤醒
			}
		}
		// 判断全部线程撤销
		do {
			delay(200);
			// 判断
			int count = 0;
			for (int index = 0; index < size; index++) {
				if (sticks[index].isOkay()) {
					count++;
				} else {
					break;
				}
			}
			// 如果达到规定值，退出，否则延时200毫秒
			if (count == size) {
				break;
			}
		} while (true);
	}

	/**
	 * 启动“单模式”网络监听服务。<br><br>
	 * 
	 *“单模式”网络监听必须指定一个有效的本地IP地址（除通配符地址外都可以是有效地址）。
	 * 如果用户指定的是通配符地址或者自回路地址，那么在计算机上选择一个有效的IP地址，
	 * 地址类型（IPv6/IPv4）对应传入的节点地址类型。虽然IP地址可以改变，但是TCP/UDP的监听端口不能够改变。<br><br>
	 * 
	 * 这个方法被“非网关站点”（TOP/HOME/DATA/WORK/BUILD/FRONT/BANK/ACCOUNT/HASH/WATCH/LOG）调用。
	 * @param clazzs Visit接口集合
	 * @param local 本地节点的预定义地址，来自“Site.getNode”方法。
	 * @return 启动成功返回“真”，否则“假”。
	 */
	protected boolean loadSingleListen(Class<?>[] clazzs, Node local) {
		Logger.debug(this, "loadSingleListen", "ready bind %s", local.getHost());

		// 启动网络监听
		boolean success = loadListen(clazzs, local.getAddress(), local.getTCPort(), local.getUDPort());
		if (success) {
			// UDP监听地址
			SocketHost packet = packetMonitor.getLocal();
			// TCP监听地址
			SocketHost stream = streamMonitor.getLocal();

			// 判断绑定的地址一致！
			success = (Laxkit.compareTo(packet.getAddress(), stream.getAddress()) == 0);
			// 在启动成功后重新监听节点和站点节点的地址和端口
			if (success) {
				Address address = packet.getAddress();
				local.setHost(address.duplicate(), stream.getPort(), packet.getPort());

				// 设置节点对象的映射端口，如果存在的情况下...这是针对解析参数时未设置的再次操作
				setReflectStreamPort(getReflectStreamPort());
				setReflectPacketPort(getReflectPacketPort());
			}
		}

		Logger.note(this, "loadSingleListen", success, "real bind '%s'", local);
		// 不成功退出
		if (!success) {
			return false;
		}

		// 如果定义上级站点，去服务器检测本地当前本地站点地址
		if (getHub() != null) {
			// 向服务器发送核对本地IP指令，服务器返回核对后的本地UDP地址，这个核对过的UDP地址网络内有效。
			SocketHost remote = getHub().getPacketHost();
			SocketHost reflect = reflect(remote);

			// 没有获得地址，是错误
			if (reflect == null) {
				Logger.error(this, "loadSingleListen", "cannot be reflect address!");
				return false;
			}

			// 比较地址参数
			Address address = reflect.getAddress();
			// 本地不包含这个地址，那一定是NAT地址，返回错误码
			boolean exists = Address.contains(address);
			if (!exists) {
				Logger.error(this, "loadSingleListen", "cannot be support NAT address!");
				return false;
			}

			// 通配符地址，改为实际主机地址
			if (local.getAddress().isAnyLocalAddress()) {
				// TCP/UDP服务器本地地址
				packetMonitor.setDefineHost(reflect);
				streamMonitor.getDefineHost().setAddress(address);
				// 异步通信私有地址
				replySucker.setDefinePrivateIP(address);
				replyDispatcher.setDefinePrivateIP(address);
				// 节点本地地址
				int tcport = streamMonitor.getBindPort();
				local.setHost(address, tcport, reflect.getPort());

				Logger.info(this, "loadSingleListen", "exchange to %s", address);
			} 
			// 如果地址不匹配，这是错误
			else if (Laxkit.compareTo(local.getAddress(), address) != 0) {
				Logger.error(this, "loadSingleListen", "%s != %s", local.getAddress(), address);
				return false;
			}
		}

		return true;
	}

	/**
	 * 启动网关监听服务。<br><br>
	 * 
	 * 这个方法被网关站点调用，服务器监听地址必须是通配符地址，优先选择IPv4格式地址，
	 * 在没有IPv4地址时，选择IPv6地址。用户传入的内网/外网IP地址必须已经在操作系统中存在，内网和外网的TCP/UDP端口要求一致。
	 * 
	 * @param clazzs Visit接口集合
	 * @param inner 内部节点地址
	 * @param outer 外部节点地址
	 * @return 启动成功返回“真”，否则“假”。
	 */
	protected boolean loadGatewayListen(Class<?>[] clazzs, Node inner, Node outer) {
		// 检查内网/外网IP地址存在，不存在警告提示
		if (!Address.contains(inner.getAddress())) {
			Logger.warning(this, "loadGatewayListen", "illegal inner node %s", inner);
		}
		if (!Address.contains(outer.getAddress())) {
			Logger.warning(this, "loadGatewayListen", "illegal outer node %s", outer);
		}

		// 如果地址不匹配是错误
		if (inner.getTCPort() != outer.getTCPort() || inner.getUDPort() != outer.getUDPort()) {
			Logger.error(this, "loadGatewayListen", "illegal port %s / %s", inner, outer);
			return false;
		}

		// 通配符地址优先选择IPv4格式地址。原因：IPV6计算机可以兼容IPV4，而IPV4计算机却不能支持IPV6，所以IPV4更通用。
		boolean ip4 = (inner.getAddress().isIPv4() || outer.getAddress().isIPv4());

		// 启动服务器监听，地址是通配符，端口必须指定！！
		boolean success = loadListen(clazzs, new Address(ip4), inner.getTCPort(), inner.getUDPort());
		// 检查结果
		if(success) {
			// UDP监听地址
			SocketHost packet = packetMonitor.getBindHost();
			// TCP监听地址
			SocketHost stream = streamMonitor.getBindHost();

			// 实际主机地址
			packetMonitor.setDefineHost(new SocketHost(SocketTag.UDP, inner.getAddress(), packet.getPort()));
			streamMonitor.setDefineHost(new SocketHost(SocketTag.TCP, inner.getAddress(), stream.getPort()));

			// 通配符地址只在本机有用，对外通信仍然需要一个实际地址。
			// 这里采用内网为站点地址，用于注册/注销/切换
			getSite().getNode().setHost(inner.getAddress(), stream.getPort(), packet.getPort());

			// 设置节点对象的映射端口，如果存在的情况下...这是针对解析参数时未设置的再次操作
			setReflectStreamPort(getReflectStreamPort());
			setReflectPacketPort(getReflectPacketPort());
		}

		// 如果HUB定义，去检测本机实际IP地址
		if (getHub() != null) {
			SocketHost reflect = reflect(getHub().getPacketHost());
			success = (reflect != null);

			Logger.note(this, "loadGatewayListen", success, "reflect fixp packet monitor is %s", reflect);

			if (success) {
				Address address = reflect.getAddress();
				// TCP/UDP服务器本地地址
				packetMonitor.setDefineHost(reflect);
				streamMonitor.getDefineHost().setAddress(address);
				// 异步通信私有地址
				replySucker.setDefinePrivateIP(address);
				replyDispatcher.setDefinePrivateIP(address);
				// 内网地址
				getSite().getHost().setAddress(address);
				getSite().getHost().setUDPort(reflect.getPort());
			}
		}

		Logger.note(this, "loadGatewayListen", success,
				"finished! gateway listen '%s' - '%s' - '%s'", getListener(), inner, outer);

		return success;
	}


	/**
	 * 启动主域FIXP服务器监听，包括TCP、UDP、RPC三种模式。
	 * @param clazzes Visit接口实现类集合。如果FIXP服务器接收和判断是RPC调用，将转发给RCP适配器。
	 * @param local 本地IP地址
	 * @param tcport 本地TCP端口
	 * @param udport 本地UDP端口
	 * @return 返回真或者假
	 */
	private boolean loadDomainListen(Class<?>[] clazzes, Address local, int tcport, int udport) {
		SocketHost stream = new SocketHost(SocketTag.TCP, local, tcport);
		SocketHost packet = new SocketHost(SocketTag.UDP, local, udport);

		// 先绑定UDP服务器，再绑定TCP服务器
		boolean success = packetMonitor.bind(packet);
		if (success) {
			success = streamMonitor.bind(stream);
			// 不成功，关闭UDP服务器
			if (!success) {
				packetMonitor.close();
			}
		}
		// 不成功退出！
		if (!success) {
			Logger.error(this, "loadDomainListen", "cannot be load socket monitor!");
			return false;
		}

		// 实际端口
		stream.setPort(streamMonitor.getBindPort());
		packet.setPort(packetMonitor.getBindPort());

		// 设置两种服务器的实际监听地址
		streamMonitor.setDefineHost(stream);
		packetMonitor.setDefineHost(packet);

		// 加载异步RPC转发器(系统加载，每个站点都有)
		success = visitAdapter.addInstance(agent);
		Logger.note(this, "loadDomainListen", success, "load %s", agent.getClass().getName());

		// 加载外部RPC
		if (success) {
			for (int i = 0; clazzes != null && i < clazzes.length; i++) {
				success = visitAdapter.addVisit(clazzes[i]);
				Logger.note(this, "loadDomainListen", success, "load %s", clazzes[i].getName());
				if (!success) return false;
			}
		}

		// 设置FIXP服务器网络服务关联接口
		if (success) {
			streamMonitor.setVisitInvoker(visitAdapter);
			streamMonitor.setStreamInvoker(streamAdapter);

			packetMonitor.setVisitInvoker(visitAdapter);
			packetMonitor.setPacketInvoker(packetInvoker);
		}

		// 启动FIXP数据流监听服务器
		if (success) {
			success = streamMonitor.start();
		}
		// 启动FIXP数据包监听服务器
		if (success) {
			success = packetMonitor.start(Thread.MAX_PRIORITY);
		}
		// 等待FIXP监听器进入线程状态
		if (success) {
			do {
				delay(200);
			} while (!streamMonitor.isRunning());
			do {
				delay(200);
			} while (!packetMonitor.isRunning());
		}

		// 以上不成功，关闭主域监听
		if (!success) {
			stopDomainListen();
		}

		return success;
	}

	/**
	 * 启动FIXP服务器监听，包括TCP、UDP、RPC三种模式。
	 * @param clazzes Visit接口实现类集合。如果FIXP服务器接收和判断是RPC调用，将转发给RCP适配器。
	 * @param local 本地IP地址
	 * @param tcport 本地TCP端口
	 * @param udport 本地UDP端口
	 * @return 返回真或者假
	 */
	private boolean loadListen(Class<?>[] clazzes, Address local, int tcport, int udport) {
		// 启动主域监听服务
		boolean success = loadDomainListen(clazzes, local, tcport, udport);
		// 启动异步监听服务
		if (success) {
			success = loadReplyListen();
		}
		// 以上不成功，关闭它们！
		if (!success) {
			stopReplyListen();
			stopDomainListen();
		}

		return success;
	}

	/**
	 * 关闭主域监听
	 */
	private void stopDomainListen() {
		ThreadStick streamStick = new ThreadStick();
		ThreadStick packetStick = new ThreadStick();

		boolean streamRunning = streamMonitor.isRunning();
		if (streamRunning) {
			streamMonitor.stop(streamStick);
		}
		boolean packetRunning = packetMonitor.isRunning();
		if (packetRunning) {
			packetMonitor.stop(packetStick);
		}

		// 等等FIXP数据流服务器释放
		while (streamRunning) {
			delay(200);
			if (streamStick.isOkay()) break;
		}
		// 等待FIXP数据包服务器释放
		while (packetRunning) {
			delay(200);
			if (packetStick.isOkay()) break;
		}

		// 关闭SOCKET
		if (streamMonitor.isBound()) {
			streamMonitor.close();
		}
		if (packetMonitor.isBound()) {
			packetMonitor.close();
		}
	}

	/**
	 * 停止FIXP监听服务
	 */
	protected void stopListen() {
		// 关闭异步监听
		stopReplyListen();

		// 关闭主域监听
		stopDomainListen();
	}

	/**
	 * 通过FIXP UDP服务器发送数据包
	 * @param packet 数据包实例
	 * @return 发送成功返回真，否则假
	 */
	protected boolean sendPacket(Packet packet) {
		return packetMonitor.notice(packet);
	}

	/**
	 * 向指定的站点发送“HELLO”激活命令
	 * 
	 * @param endpoint 目标站点UDP地址
	 * @return 发送成功返回真，否则假
	 */
	protected boolean hello(SocketHost endpoint) {
		Node node = getListener().duplicate();

		// 如果当前是FRONT节点且投递到公网时，发送两次。因为FRONT在外网中不大可靠，多发提高成功概率。
		int sends = 1;
		if (SiteTag.isFront(getFamily()) && endpoint.getAddress().isWideAddress()) {
			sends = 2;
		}

		// 发送数据包
		Mark mark = new Mark(Ask.NOTIFY, Ask.HELO);
		Packet packet = new Packet(endpoint, mark);
		packet.addMessage(MessageKey.NODE_ADDRESS, node);
		int count = 0;
		// 发送数据包
		for (int i = 0; i < sends; i++) {
			boolean success = sendPacket(packet);
			if (success) {
				count++;
			}
		}

		return count > 0;
	}

	/**
	 * 向默认的注册站点发送“HELLO”激活指令
	 * 
	 * @return 发送成功返回真，否则假
	 */
	protected boolean hello() {
		Node hub = getHub();
		if (hub == null) {
			return false;
		}
		return hello(hub.getPacketHost());
	}

	/**
	 * 从local.xml中解析管理站点地址
	 * @param document XML文档
	 * @return 解析成功返回“真”，否则“假”。
	 */
	protected boolean splitHubSite(Document document) {
		String value = XMLocal.getXMLValue(document.getElementsByTagName(SiteMark.HUB));
		Logger.debug(this, "splitHubSite", "hub is '%s'", value);

		try {
			// 设置HUB地址
			setHub(new Node(value));

			Logger.info(this, "splitHubSite", "real hub is '%s'", getHub());
			return true;
		} catch (UnknownHostException e) {
			Logger.error(e);
		}
		return false;
	}

	/**
	 * 设置用户日志目录（这是一个选项，具体由各节点自己去决定）
	 * @param document XML文档
	 * @return 成功返回真，否则假
	 */
	protected boolean setUserLogPath(Document document) {
		NodeList list = document.getElementsByTagName(EchoMark.MARK_ECHO);
		if (list.getLength() != 1) {
			Logger.error(this, "setUserLogPath", "cannot find \'echo\' tag");
			return false;
		}
		Element element = (Element) list.item(0);


		// 定义的日志块长度，默认5M
		String input = XMLocal.getValue(element, EchoMark.USERLOG_BLOCK_SIZE);
		UserLogPool.getInstance().setLength(ConfigParser.splitInteger(input, 5 * 1024 * 1024));

		// 用户日志目录
		String root = XMLocal.getValue(element, EchoMark.USERLOG_DIRECTORY);
		if (root.isEmpty()) {
			Logger.error(this, "setUserLogPath", "cannot be find %s", EchoMark.USERLOG_DIRECTORY);
			return false;
		}
		boolean success = UserLogPool.getInstance().setRoot(root);

		Logger.debug(this, "setUserLogPath", success, "%s", root);

		return success;
	}

	/**
	 * 解析用户自定义COMMAND/INVOKER资源。<br>
	 * 自定义COMMAND/INVOKER资源属于系统层面，由集群管理员自主定义的数据资源和分布处理，它的存在由集群管理者自主决定。
	 * 这一点不同于分布任务组件，分布任务组件是注册用户定义的资源。
	 * 
	 * @param document XML文档
	 * @return 成功返回真，否则假
	 */
	protected boolean loadCustom(Document document) {
		NodeList nodes = document.getElementsByTagName(CustomMark.CUSTOM);
		// 允许不存在，但是超过1个是错误
		if (nodes.getLength() == 0) {
			Logger.warning(this, "loadCustom", "not defined custom configure!");
			return true;
		} else if (nodes.getLength() > 1) {
			Logger.error(this, "loadCustom", "illegal custom configure!");
			return false;
		}

		// 解析处理自定义配置。注意：先从磁盘加载JAR文件，再解析JAR配置文件。这个顺序不能乱！

		Element element = (Element) nodes.item(0);

		// 1. 自动更新磁盘JAR包
		String autoUpdate = element.getAttribute(CustomMark.CUSTOM_AUTOUPDATE);

		// 2. 自定义JAR存储目录（所有自定义JAR全部放在这个目录）
		String directory = XMLocal.getValue(element, CustomMark.CUSTOM_DIRECTORY);
		if (directory.length() == 0) {
			Logger.warning(this, "loadCustom", "not define Custom Directory");
			return false;
		}

		// 3. COMMAND/INVOKER配置对，是一个XML格式的文件
		String statement = XMLocal.getXMLValue(document.getElementsByTagName(CustomMark.CUSTOM_STATEMENT));
		if (statement.length() == 0) {
			Logger.error(this, "loadCustom", "cannot be resolve: %s", CustomMark.CUSTOM_STATEMENT);
			return false;
		}

		// 保存三个参数，一旦定义不允许修改
		CustomConfig.setAutoUpdate(ConfigParser.splitBoolean(autoUpdate, true)); //默认是真
		CustomConfig.setDirectory(directory);
		CustomConfig.setStatement(statement);
		// 加入到定时检测
		addDeviceDirectory(CustomConfig.getDirectory());

		Logger.info(this, "loadCustom", "auto update:%s, custom jar directory %s, custom statement %s",
				CustomConfig.isAutoUpdate(), CustomConfig.getDirectory(),
				CustomConfig.getStatement());

		return true;
	}

	/**
	 * 解析LINUX/WINDOWS环境最小内存限制，低这个数就报警内存不足
	 * @param input 输入参数
	 * @return 成功返回真，否则假
	 */
	private boolean splitLeastMemory(String input) {
		if (input == null) {
			return false;
		}

		if (isLinux()) {
			return LinuxDevice.getInstance().splitMemory(input);
		} else if (isWindows()) {
			return WindowsDevice.getInstance().splitMemory(input);
		}
		return false;
	}

	/**
	 * 解析LINUX/WINDOWS环境最小磁盘限制，低这个数就报警磁盘不足
	 * @param input 输入参数
	 * @return 成功返回真，否则假
	 */
	private boolean splitLeastDisk(String input) {
		if (input == null) {
			return false;
		}

		if (isLinux()) {
			return LinuxDevice.getInstance().splitDisk(input);
		} else if (isWindows()) {
			return WindowsDevice.getInstance().splitDisk(input);
		}
		return false;
	}

	/**
	 * 启动平台设备检测环境
	 * @param interval 启动间隔值
	 * @return 成功返回真，否则假
	 */
	private void loadDeviceTask(long interval) {
		// 不能低于10秒钟
		if (interval < 10000) {
			interval = 10000;
		}

		// 启动LINUX CPU资源检测器
		if (isLinux()) {
			// 内存检测器放入计时器，1分钟触发一次
			timer.schedule(LinuxDevice.getInstance(), 10000, interval);
		} else if (isWindows()) {
			// 内存检测器放入计时器，1分钟触发一次
			timer.schedule(WindowsDevice.getInstance(), 10000, interval);
		}
	}

	/**
	 * 初始化计算器任务
	 */
	private void loadEffectorTask() {
		// 启动LINUX CPU资源检测器
		if(isLinux()) {
			// 放入计时器队列中，2秒钟触发一次
			timer.schedule(LinuxEffector.getInstance(), 10000, 2000);
		} else if(isWindows()) {
			// 放入计时器队列中，2秒钟触发一次
			timer.schedule(WindowsEffector.getInstance(), 10000, 2000);
		}
	}

	/**
	 * 启动系统的计算器任务。<br><br>
	 * 
	 * 这些要求在运行过程中进行动态检查，被检查的资源包括：内存/硬盘/CPU。<br>
	 * 
	 * 如果“内存/硬盘”的剩余空间低于低于规定阀值时将报警，默认是无限制，关键是：UNLIMIT <br>
	 * 
	 * “内存/硬盘”的阀值限制可以通过命令：“SET LEAST MEMORY”和“SET LEAST DISK”在运行过程中调整设置。<br>
	 * @param document XML文档
	 */
	protected void loadTimerTasks(Document document) {
		String disk = null;
		String memory = null;
		// 间隔值
		long interval = 60000;

		// 最低限制参数
		NodeList list = document.getElementsByTagName(EchoMark.MARK_ECHO);
		if (list != null && list.getLength() == 1) {
			Element element = (Element) list.item(0);
			list = element.getElementsByTagName(EchoMark.LEAST);

			if (list != null && list.getLength() == 1) {
				element = (Element) list.item(0);
				String input = element.getAttribute(EchoMark.LEAST_INTERVAL);
				// 解析时间参数
				interval = ConfigParser.splitTime(input, 60000);


				// 运行环境最低硬盘限制，低于规定值就报警
				disk = XMLocal.getValue(element, EchoMark.LEAST_DISK);
				// 运行环境的最低内存限制，低于规定值就报警。
				memory = XMLocal.getValue(element, EchoMark.LEAST_MEMORY);
			}
		}

		// 运行环境的最低内存限制，低于规定值就报警。
		splitLeastMemory(memory);
		splitLeastDisk(disk);

		// 启动内存/CPU检测
		loadEffectorTask();
		// 启动设备环境，定义间隔时间
		loadDeviceTask(interval);
	}

	/**
	 * 启动运行环境最低资源限制。<br><br>
	 * 
	 * 资源限制包括两个部分：内存/硬盘。当它们的剩余空间低于规定阀值时将报警，默认是无限制，关键是：UNLIMIT <br>
	 * 
	 * 也可以通过命令：“SET LEAST MEMORY”和“SET LEAST DISK”在运行过程中调整设置。<br>
	 * 
	 * @param filename 磁盘文件名
	 */
	protected void loadTimerTasks(String filename) {
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document != null) {
			loadTimerTasks(document);
		}
	}

	/**
	 * 解析命令管理池
	 * @param element XML成员
	 */
	private void splitCommandPool(Element element) {
		// 命令管理池线程堆栈尺寸（属性）
		String input = element.getAttribute(EchoMark.COMMAND_POOL_STACK_SIZE);
		long stackSize = ConfigParser.splitLongCapacity(input, getCommandPool().getStackSize());
		getCommandPool().setStackSize(stackSize);

		// 命令管理池可存储的最多命令数据。默认是-1，无限制。
		input = XMLocal.getValue(element, EchoMark.MAX_COMMANDS);
		int size = ConfigParser.splitInteger(input, EchoTransfer.getMaxCommands());
		EchoTransfer.setMaxCommands(size);

		// 命令管理池延时间隔
		input = XMLocal.getValue(element, EchoMark.COMMAND_POOL_SILENT_TIME);
		long time = ConfigParser.splitTime(input, getCommandPool().getSilentInterval());
		getCommandPool().setSilentInterval(time);

		// 调用器失效检测间隔
		input = XMLocal.getValue(element, EchoMark.DISABLE_CHECK_INTERVAL);
		time = ConfigParser.splitTime(input, getCommandPool().getDisableCheckInterval());
		getCommandPool().setDisableCheckInterval(time);

		// 命令超时时间
		input = XMLocal.getValue(element, EchoMark.COMMAND_TIMEOUT);
		time = ConfigParser.splitTime(input, EchoTransfer.getCommandTimeout());
		EchoTransfer.setCommandTimeout(time); 
	}

	/**
	 * 解析“invoker”标签下的参数
	 * @param element XML成员
	 */
	private void splitInvoker(Element element) {
		// 调用器线程堆栈尺寸（属性！）
		String input = element.getAttribute(EchoMark.INVOKER_STACK_SIZE);
		long stackSize = ConfigParser.splitLongCapacity(input, EchoTransfer.getStackSize());
		EchoTransfer.setStackSize(stackSize);

		// XML标签


		// Launch/Ending, Ending/Next Ending发生交叠后的时间间隔。
		// 当一个阶段的数据处理未完成和释放，下一个阶段的请求已经到来，这时需要延迟下个阶段的处理，在本阶段释放后才能启动。这就是它的延时时间
		input = XMLocal.getValue(element, EchoMark.INVOKER_CROSS_INTERVAL);
		long time = ConfigParser.splitTime(input, EchoTransfer.getCrossInterval());
		EchoTransfer.setCrossInterval(time);

		// 异步调用器数据传输模式，默认是UDP
		input = XMLocal.getValue(element, EchoMark.INVOKER_TRANSFER_MODE);
		EchoTransfer.setTransferMode(ConfigParser.splitSocketFamily(input, SocketTag.UDP));

		// 异步调用器套接字故障重试次数
		input = XMLocal.getValue(element, EchoMark.INVOKER_TRANSFER_MAX_RETRY);
		EchoTransfer.setMaxRetry(ConfigParser.splitInteger(input, EchoTransfer.getMaxRetry()));

		// 异步调用器套接字故障重试间隔时间
		input = XMLocal.getValue(element, EchoMark.INVOKER_TRANSFER_RETRY_INTERVAL);
		time = ConfigParser.splitTime(input, EchoTransfer.getRetryInterval());
		EchoTransfer.setRetryInterval((int) time);

		// 系统规定的异步调用器超时时间
		input = XMLocal.getValue(element, EchoMark.INVOKER_TIMEOUT);
		time = ConfigParser.splitTime(input, EchoTransfer.getInvokerTimeout());
		EchoTransfer.setInvokerTimeout(time);
	}

	/**
	 * 解析调用器管理池
	 * @param element XML成员
	 */
	private void splitInvokerPool(Element element) {
		// 调用器管理池线程堆栈尺寸（属性！）
		String input = element.getAttribute(EchoMark.INVOKER_POOL_STACK_SIZE);
		long stackSize = ConfigParser.splitLongCapacity(input, getInvokerPool().getStackSize());
		getInvokerPool().setStackSize(stackSize);

		// 异步管理池可运行的最多调用器数目。默认是10个
		input = XMLocal.getValue(element, EchoMark.MAX_INVOKERS);
		int size = ConfigParser.splitInteger(input, EchoTransfer.getMaxInvokers());
		EchoTransfer.setMaxInvokers(size);
		// 调用最大限制时间
		input = XMLocal.getAttribute(element, EchoMark.MAX_INVOKERS, EchoMark.MAX_INVOKERS_ATTR_CONFINE_TIME);
		long time = ConfigParser.splitTime(input, EchoTransfer.getMaxConfineTime() );
		EchoTransfer.setMaxConfineTime(time);

		// 调用器管理池延时间隔
		input = XMLocal.getValue(element, EchoMark.INVOKER_POOL_SILENT_TIME);
		time = ConfigParser.splitTime(input, getInvokerPool().getSilentInterval());
		getInvokerPool().setSilentInterval(time);

		// 调用器失效检测间隔
		input = XMLocal.getValue(element, EchoMark.DISABLE_CHECK_INTERVAL);
		getInvokerPool().setDisableCheckInterval(ConfigParser.splitTime(input, getInvokerPool().getDisableCheckInterval()));

		// 调用器
		NodeList nodes = element.getElementsByTagName(EchoMark.MARK_INVOKER);
		if (nodes.getLength() == 1) {
			Element sub = (Element) nodes.item(0);
			splitInvoker(sub);
		}
	}

	/**
	 * 解析回显参数
	 * @param document XML文档实例
	 */
	protected boolean splitEcho(Document document) {
		NodeList list = document.getElementsByTagName(EchoMark.MARK_ECHO);
		if (list.getLength() != 1) {
			Logger.error(this, "splitEcho", "cannot find \'echo\' tag");
			return false;
		}
		Element element = (Element) list.item(0);


		// 回显缓存地址。如果没有定义，在默认目录下建立
		String input = XMLocal.getValue(element, EchoMark.ECHO_DIRECTORY);
		// 设置系统目录。 如果没有指定，用JAVA的临时目录加上站点名称来定义
		if (input.isEmpty()) {
			input = System.getProperty("java.io.tmpdir");
			String family = SiteTag.translate(getFamily());
			File dir = new File(input, family);
			EchoArchive.setDirectory(dir);

			// 保存回显目录，定时检测
			addDeviceDirectory(dir);
		} else {
			input =	ConfigParser.splitPath(input);
			EchoArchive.setDirectory(input);

			// 保存回显目录，定时检测
			addDeviceDirectory(input);
		}

		// 把回显目录设置为系统属性。回显目录用在站点的策略文件“.policy”中，提供给沙箱校验
		try {
			String dir = EchoArchive.getDirectory().getCanonicalPath();
			System.setProperty("laxcus.echo.dir", dir);
		} catch (IOException e) {
			Logger.error(e);
			return false;
		}

		// 命令管理池
		NodeList nodes = element.getElementsByTagName(EchoMark.MARK_COMMAND_POOL);
		if (nodes.getLength() == 1) {
			Element sub = (Element) nodes.item(0);
			splitCommandPool(sub);
		}

		// 调用器管理池
		nodes = element.getElementsByTagName(EchoMark.MARK_INVOKER_POOL);
		if (nodes.getLength() == 1) {
			Element sub = (Element) nodes.item(0);
			splitInvokerPool(sub);
		}

		// CPU最大占用比率。默认只使用60%的内存资源
		input = XMLocal.getValue(element, EchoMark.CPU_RATE);
		EchoTransfer.setMaxCpuRate(ConfigParser.splitRate(input, EchoTransfer.getMaxCpuRate()));

		// 虚拟机内存最大占用比率。这个参数随节点不同来定。默认情况只占用一半内存空间
		input = XMLocal.getValue(element, EchoMark.VM_MEMORY_RATE);
		EchoTransfer.setMaxVMMemoryRate(ConfigParser.splitRate(input, EchoTransfer.getMaxVMMemoryRate()));

		// 打印配置参数
		Logger.debug(this, "splitEcho", "echo directory: \"%s\"", EchoArchive.getDirectory());
		Logger.debug(this, "splitEcho", "command pool silent time:%d ms, invoker pool silent time:%d ms",
				getCommandPool().getSilentInterval(), getInvokerPool().getSilentInterval());
		Logger.debug(this, "splitEcho", "command pool thread stack size:%d, invoker pool thread stack size:%d",
				getCommandPool().getStackSize(), getInvokerPool().getStackSize());
		Logger.debug(this, "splitEcho", "invoker thread stack size:%d",
				EchoTransfer.getStackSize());

		Logger.debug(this, "splitEcho", "max commands: %d, max invokers:%d, confine time:%d ms", 
				EchoTransfer.getMaxCommands(), EchoTransfer.getMaxInvokers(), EchoTransfer.getMaxConfineTime());
		Logger.debug(this, "splitEcho", "max cpu rate: %.2f, max vm memory rate: %.2f",
				EchoTransfer.getMaxCpuRate(), EchoTransfer.getMaxVMMemoryRate());

		Logger.debug(this, "splitEcho", "command disable check interval: %d ms, invoker disable check interval: %d ms",
				getCommandPool().getDisableCheckInterval(), getInvokerPool().getDisableCheckInterval());
		Logger.debug(this, "splitEcho", "command timeout: %d ms, invoker timeout: %d ms",
				EchoTransfer.getCommandTimeout(), EchoTransfer.getInvokerTimeout() );

		Logger.debug(this, "splitEcho", "invoker cross interval: %d ms, invoker transfer mode: %s, transfer max retry:%d, transfer retry interval:%d ms",
				EchoTransfer.getCrossInterval(), SocketTag.translate(EchoTransfer.getTransferMode()),
				EchoTransfer.getMaxRetry(), EchoTransfer.getRetryInterval());

		return true;
	}

	/**
	 * 设置资源管理池的延时间隔时间
	 * @param document 
	 * @param pool 资源管理池
	 */
	protected void setStaffPoolSleepInterval(Document document, VirtualPool pool) {

		// 解析安全配置文件
		String input = XMLocal.getXMLValue(document.getElementsByTagName(OtherMark.STAFFPOOL_SLEEP_INTERVAL));
		// 解析时间，默认是10秒
		long ms = ConfigParser.splitTime(input, 10000);

		Logger.debug(this, "setStaffPoolSleepInterval", "%s sleep time: %d ms",
				pool.getClass().getSimpleName(), ms);

		// 定义时间
		pool.setSleepTimeMillis(ms);
	}

	/**
	 * 返回FIXP数据包协处理器
	 * @return FixpPacketHelper实例
	 */
	public FixpPacketHelper getPacketHelper() {
		return packetMonitor.getPacketHelper();
	}

	/**
	 * 返回异步数据接收辅助器
	 * @return ReplyHelper实例
	 */
	public ReplyHelper getReplyHelper() {
		return replySucker.getHelper();
	}

	/**
	 * 返回异步数据发送辅助器
	 * @return ReplyWorker实例
	 */
	public ReplyWorker getReplyWorker() {
		return replyDispatcher.getWorker();
	}

	/**
	 * 解析FIXP TCP服务器
	 * @param element XML成员
	 */
	private void splitStreamMonitor(Element element) {
		// FIXP TCP服务器堆栈尺寸（属性值）
		String input = element.getAttribute(SiteMark.SERVER_TCP_STACK_SIZE);
		long stackSize = ConfigParser.splitLongCapacity(input, streamMonitor.getStackSize());
		streamMonitor.setStackSize(stackSize);

		// FIXP TCP服务器的映射端口（属性值）
		input = element.getAttribute(SiteMark.REFLECT_PORT);
		int port = ConfigParser.splitPort(input, 0);
		if (port > 0) {
			setReflectStreamPort(port);
		}

		// TCP服务器可以同时接受的SOCKET数目
		input = XMLocal.getValue(element, SiteMark.TCP_BLOCKS);
		int blocks = ConfigParser.splitInteger(input, streamMonitor.getBlocks());
		streamMonitor.setBlocks(blocks);

		// FIXP TCP服务器接收缓存空间
		input = XMLocal.getValue(element, SiteMark.SERVER_TCP_RECEIVE_BUFFERSIZE);
		int size = (int) ConfigParser.splitLongCapacity(input, streamMonitor.getReceiveBufferSize());
		streamMonitor.setReceiveBufferSize(size);
	}

	/**
	 * 解析FIXP UDP服务器
	 * @param element XML成员
	 */
	private void splitPacketMonitor(Element element) {
		// FIXP UDP服务器堆栈尺寸(属性值)
		String input = element.getAttribute(SiteMark.SERVER_UDP_STACK_SIZE);
		long stackSize = ConfigParser.splitLongCapacity(input, packetMonitor.getStackSize());
		packetMonitor.setStackSize(stackSize);

		// FIXP TCP服务器的映射端口（属性值）
		input = element.getAttribute(SiteMark.REFLECT_PORT);
		int port = ConfigParser.splitPort(input, 0);
		if (port > 0) {
			setReflectPacketPort(port);
		}

		// FIXP UDP服务器接收缓存空间
		input = XMLocal.getValue(element, SiteMark.SERVER_UDP_RECEIVER_BUFFERSIZE);
		int size = (int) ConfigParser.splitLongCapacity(input, packetMonitor.getReceiveBufferSize());
		packetMonitor.setReceiveBufferSize(size);

		// FIXP UDP服务器发送缓存空间
		input = XMLocal.getValue(element, SiteMark.SERVER_UDP_SEND_BUFFERSIZE);
		size = (int) ConfigParser.splitLongCapacity(input, packetMonitor.getSendBufferSize());
		packetMonitor.setSendBufferSize(size);

		// UDP任务线程数目
		input = XMLocal.getValue(element, SiteMark.PACKET_TASK_THREADS);
		size = ConfigParser.splitInteger(input, 1); // 默认最少1个，如果不定义的话
		packetMonitor.setTaskThreads(size);
	}

	/**
	 * 解析SOCKET客户机配置
	 * @param element XML单元
	 */
	private void splitSocketClient(Element element) {
		// FIXP客户机发送缓存空间（包括TCP/UDP）
		String input = XMLocal.getValue(element, SiteMark.CLIENT_SEND_BUFFERSIZE);
		int size = (int)ConfigParser.splitLongCapacity(input,  SocketTransfer.getDefaultSendBufferSize());
		SocketTransfer.setDefaultSendBufferSize(size);

		// FIXP客户机接收缓存空间（包括TCP/UDP）
		input = XMLocal.getValue(element, SiteMark.CLIENT_RECEIVE_BUFFERSIZE);
		size = (int)ConfigParser.splitLongCapacity(input, SocketTransfer.getDefaultReceiveBufferSize());
		SocketTransfer.setDefaultReceiveBufferSize(size);

		// FIXP客户机连接超时（包括TCP/UDP）
		input = XMLocal.getValue(element, SiteMark.CLIENT_CONNECT_TIMEOUT);
		int timeout = (int)ConfigParser.splitTime(input, SocketTransfer.getDefaultConnectTimeout() );
		SocketTransfer.setDefaultConnectTimeout(timeout);

		// FIXP客户机接收超时（包括TCP/UDP）
		input = XMLocal.getValue(element, SiteMark.CLIENT_RECEIVE_TIMEOUT);
		timeout = (int)ConfigParser.splitTime(input, SocketTransfer.getDefaultReceiveTimeout());
		SocketTransfer.setDefaultReceiveTimeout(timeout);

		// FIXP UDP客户机子包超时（只适用于UDP的无连接模式）
		input = XMLocal.getValue(element, SiteMark.CLIENT_SUBPACKET_RECEIVE_TIMEOUT);
		timeout = (int) ConfigParser.splitTime(input, FixpPacketClient.getDefaultSubPacketTimeout());
		FixpPacketClient.setDefaultSubPacketTimeout(timeout);

		// FIXP UDP子包尺寸
		input = XMLocal.getValue(element, SiteMark.CLIENT_SUBPACKET_SIZE);
		size = (int) ConfigParser.splitLongCapacity(input, FixpPacketClient.getDefaultSubPacketSize());
		FixpPacketClient.setDefaultSubPacketSize(size);

		// socket通信控制指令超时时间
		input = XMLocal.getValue(element, SiteMark.CHANNEL_TIMEOUT);
		timeout = (int) ConfigParser.splitTime(input, SocketTransfer.getDefaultChannelTimeout());
		SocketTransfer.setDefaultChannelTimeout(timeout);
	}

	/**
	 * 解析FIXP密文
	 * @param element 单元实例
	 */
	private void splitCipher(Element element) {
		// 一个密文在FIXP服务器上的有效存在时间，超过这个时间，密文将被自动删除。
		String input = XMLocal.getValue(element, SiteMark.CIPHER_TIMEOUT);
		long ms = ConfigParser.splitTime(input, Cipher.getTimeout()); // 默认180秒（3分钟）
		Cipher.setTimeout(ms);

		// 客户端密文数位长度
		input = XMLocal.getValue(element, SiteMark.CIPHER_CLIENT_BITS);
		int size = ConfigParser.splitInteger(input, Cipher.getClientWidthWithBits());
		Cipher.setClientWidthWithBits(size);// 密文超时

		// 服务端密文数位长度
		input = XMLocal.getValue(element, SiteMark.CIPHER_SERVER_BITS);
		size = ConfigParser.splitInteger(input, Cipher.getServerWidthWithBits());
		Cipher.setServerWidthWithBits(size);
	}

	/**
	 * 解析主服务器的配置参数
	 * @param element
	 */
	private void splitDomainServer(Element element) {
		// 解析密文参数
		NodeList nodes = element.getElementsByTagName(SiteMark.MARK_DOMAIN_SERVER_CIPHER);
		if (nodes.getLength() == 1) {
			Element sub = (Element) nodes.item(0);
			splitCipher(sub);
		}

		// FIXP TCP服务器
		nodes = element.getElementsByTagName(SiteMark.MARK_DOMAIN_STREAM_MONITOR);
		if (nodes.getLength() == 1) {
			Element sub = (Element) nodes.item(0);
			splitStreamMonitor(sub);
		}

		// FIXP UDP服务器
		nodes = element.getElementsByTagName(SiteMark.MARK_DOMAIN_PACKET_MONITOR);
		if (nodes.getLength() == 1) {
			Element sub = (Element) nodes.item(0);
			splitPacketMonitor(sub);
		}

		// FIXP SOCKET客户机
		nodes = element.getElementsByTagName(SiteMark.MARK_DOMAIN_SOCKET_CLIENT);
		if (nodes.getLength() == 1) {
			Element sub = (Element) nodes.item(0);
			splitSocketClient(sub);
		}

		Logger.debug(this, "splitDomainServer", "tcp server thread stack size:%d, udp server thread stack size:%d",
				streamMonitor.getStackSize(), packetMonitor.getStackSize());
		Logger.debug(this, "splitDomainServer", "tcp server reflect port:%d, udp server reflect port:%d",
				getReflectStreamPort(), getReflectPacketPort());
		Logger.debug(this, "splitDomainServer", "tcp blocks:%d, tcp receive buffer size:%d",
				streamMonitor.getBlocks(), streamMonitor.getReceiveBufferSize());
		Logger.debug(this, "splitDomainServer", "udp receive buffer:%d, udp send buffer:%d",
				packetMonitor.getReceiveBufferSize(), packetMonitor.getSendBufferSize());
		Logger.debug(this, "splitDomainServer", "client receive buffer:%d, send buffer:%d",
				SocketTransfer.getDefaultReceiveBufferSize() , SocketTransfer.getDefaultSendBufferSize());
		Logger.debug(this, "splitDomainServer", "client connect timeout:%d ms, receive timeout:%d ms",
				SocketTransfer.getDefaultConnectTimeout(), SocketTransfer.getDefaultReceiveTimeout());
		Logger.debug(this, "splitDomainServer", "client subpacket timeout:%d ms, subpacket size:%d",
				FixpPacketClient.getDefaultSubPacketTimeout(), FixpPacketClient.getDefaultSubPacketSize());
		Logger.debug(this, "splitDomainServer", "channel timeout: %d ms", SocketTransfer.getDefaultChannelTimeout());
		Logger.debug(this, "splitDomainServer", "packet task threads:%d", packetMonitor.getTaskThreads());
		Logger.debug(this, "splitDomainServer", "cipher timeout:%d ms, cipher client bits:%d, cipher server bits:%d",
				Cipher.getTimeout(), Cipher.getClientWidthWithBits(), Cipher.getServerWidthWithBits());
	}

	/**
	 * 判断支持Masssive MIMO
	 * 说明：除了FRONT/WATCH节点，其它节点都支持Massive MIMO
	 * @return 真或者假
	 */
	private boolean isSupportMIMO() {
		switch(getFamily()) {
		case SiteTag.FRONT_SITE:
		case SiteTag.WATCH_SITE:
			return false;
		}
		return true;
	}

	/**
	 * 解析异步接收器参数
	 * @param element
	 */
	private void splitReplySucker(Element element) {
		String input = element.getAttribute(ReplyMark.DISABLE_TIMEOUT);
		int timeout = (int) ConfigParser.splitTime(input, ReplyHelper.getDisableTimeout());
		ReplyHelper.setDisableTimeout(timeout);

		input = element.getAttribute(ReplyMark.SUBPACKET_RECEIVE_TIMEOUT);
		timeout = (int) ConfigParser.splitTime(input, ReplyHelper.getSubPacketTimeout());
		ReplyHelper.setSubPacketTimeout(timeout);

		// 默认绑定端口，没有定义默认是0
		input = element.getAttribute(ReplyMark.PORT);
		int defaultPort = ConfigParser.splitPort(input, 0);
		replySucker.setDefaultPort(defaultPort);

		// 映射端口，做为属性值
		input = element.getAttribute(SiteMark.REFLECT_PORT);
		defaultPort = ConfigParser.splitPort(input, 0);
		if (defaultPort > 0) {
			setReflectSuckerPort(defaultPort);
		}

		// 线程堆栈尺寸
		input = element.getAttribute(ReplyMark.STACK_SIZE);
		long stackSize = ConfigParser.splitLongCapacity(input, replySucker.getStackSize());
		replySucker.setStackSize(stackSize);

		// 接收缓存
		input = XMLocal.getValue(element, ReplyMark.RECEIVE_BUFFER_SIZE);
		int size = (int) ConfigParser.splitLongCapacity(input, replySucker.getReceiveBufferSize());
		replySucker.setReceiveBufferSize(size);

		// 发送缓存
		input = XMLocal.getValue(element, ReplyMark.SEND_BUFFER_SIZE);
		size = (int) ConfigParser.splitLongCapacity(input, replySucker.getSendBufferSize());
		replySucker.setSendBufferSize(size);

		// Massive MIMO的多入参数
		if (isSupportMIMO()) {
			input = element.getAttribute(ReplyMark.MI);
			int mi = ConfigParser.splitInteger(input, 0); // 默认是0
			if (mi > 0) {
				if (mi > 256) mi = 256; // 最大256个
				slaveSuckers = new MISucker[mi];
				for (int i = 0; i < slaveSuckers.length; i++) {
					slaveSuckers[i] = new MISucker(replySucker.getHelper());
					slaveSuckers[i].setDefaultPort(0);
					slaveSuckers[i].setStackSize(stackSize);
					slaveSuckers[i].setReceiveBufferSize(replySucker.getReceiveBufferSize());
					slaveSuckers[i].setSendBufferSize(replySucker.getSendBufferSize());
				}
			}
		}

		Logger.debug(this, "splitReplySucker",
				"disable timeout: %d ms | sub packet timeout: %d ms | receive buffer:%d | send buffer:%d | local:%s | port:%d | reflect port:%d | thread stack size:%d",
				ReplyHelper.getDisableTimeout(), ReplyHelper.getSubPacketTimeout(), 
				replySucker.getReceiveBufferSize(), replySucker.getSendBufferSize(), 
				replySucker.getDefinePrivateIP(), replySucker.getDefaultPort(), getReflectSuckerPort(), replySucker.getStackSize());
		Logger.debug(this, "splitReplySucker", "Massive MIMO, mi %d", getMIMembers());
	}

	/**
	 * 解析异步发送器参数
	 * @param element
	 */
	private void splitReplyDispatcher(Element element) {
		// 运行/接收超时时间
		String input = element.getAttribute(ReplyMark.DISABLE_TIMEOUT);
		int timeout = (int) ConfigParser.splitTime(input, ReplyWorker.getDisableTimeout());
		ReplyWorker.setDisableTimeout(timeout);

		input = element.getAttribute(ReplyMark.SUBPACKET_RECEIVE_TIMEOUT);
		timeout= (int) ConfigParser.splitTime(input, ReplyWorker.getSubPacketTimeout());
		ReplyWorker.setSubPacketTimeout(timeout);

		// FIXP子包之间的发送间隔，延时可以减少接收端的压力，降低丢包概率。
		input = element.getAttribute(ReplyMark.SEND_INTERVAL);
		timeout = (int) ConfigParser.splitTime(input, ReplyWorker.getSendInterval());
		ReplyWorker.setSendInterval(timeout);

		// 默认绑定端口，如果没有定义，默认是0
		input = element.getAttribute(ReplyMark.PORT);
		int defaultPort = ConfigParser.splitPort(input, 0);
		replyDispatcher.setDefaultPort(defaultPort);

		// 映射端口，做为属性值
		input = element.getAttribute(SiteMark.REFLECT_PORT);
		defaultPort = ConfigParser.splitPort(input, 0);
		if (defaultPort > 0) {
			setReflectDispatcherPort(defaultPort);
		}

		// 线程堆栈尺寸
		input = element.getAttribute(ReplyMark.STACK_SIZE);
		long stackSize = ConfigParser.splitLongCapacity(input, replyDispatcher.getStackSize());
		replyDispatcher.setStackSize(stackSize);

		// 接收缓存
		input = XMLocal.getValue(element, ReplyMark.RECEIVE_BUFFER_SIZE);
		int size = (int) ConfigParser.splitLongCapacity(input, replyDispatcher.getReceiveBufferSize());
		replyDispatcher.setReceiveBufferSize(size);

		// 发送缓存
		input = XMLocal.getValue(element, ReplyMark.SEND_BUFFER_SIZE);
		size = (int) ConfigParser.splitLongCapacity(input, replyDispatcher.getSendBufferSize());
		replyDispatcher.setSendBufferSize(size);

		// massive mimo的MO成员数目
		if (isSupportMIMO()) {
			input = element.getAttribute(ReplyMark.MO);
			int mo = ConfigParser.splitInteger(input, 0);
			if (mo > 0) {
				if (mo > 256) mo = 256; // 最大256个
				slaveDispatchers = new MODispatcher[mo];
				for (int i = 0; i < slaveDispatchers.length; i++) {
					slaveDispatchers[i] = new MODispatcher(replyDispatcher.getWorker());
					slaveDispatchers[i].setDefaultPort(0);
					slaveDispatchers[i].setStackSize(stackSize);
					slaveDispatchers[i].setReceiveBufferSize(replyDispatcher.getReceiveBufferSize());
					slaveDispatchers[i].setSendBufferSize(replyDispatcher.getSendBufferSize());
				}
			}
		}

		Logger.debug(this, "splitReplyDispatcher", 
				"disable timeout: %d ms | sub packet timeout: %d ms | receive buffer:%d | send buffer: %d | send interval: %d ms | local:%s | port:%d | reflect port:%d | thread stack size:%d",
				ReplyWorker.getDisableTimeout(), ReplyWorker.getSubPacketTimeout(),
				replyDispatcher.getReceiveBufferSize(), replyDispatcher.getSendBufferSize(), 
				ReplyWorker.getSendInterval(), replyDispatcher.getDefinePrivateIP(), 
				replyDispatcher.getDefaultPort(), getReflectDispatcherPort(), replyDispatcher.getStackSize());
		Logger.debug(this, "splitReplyDispatcher", "Massive MIMO, mo %d", getMOMembers() );
	}

	/**
	 * 解析异步服务器参数（接收器/发送器）
	 * @param element 参数成员
	 */
	private void splitReplyServer(Element element) {
		// FIXP批量包尺寸，一个批量处理的FIXP包包含任意多个FIXP包。
		String input = element.getAttribute(ReplyMark.PACKET_SIZE);
		int size = (int) ConfigParser.splitLongCapacity(input, ReplyTransfer.getDefaultPacketContentSize());
		ReplyTransfer.setDefaultPacketContentSize(size);
		// 子包长度，接收/发送共用
		input = element.getAttribute(ReplyMark.SUBPACKET_SIZE);
		size = (int) ConfigParser.splitLongCapacity(input, ReplyTransfer.getDefaultSubPacketContentSize());
		ReplyTransfer.setDefaultSubPacketContentSize(size);

		// 通过公网传输时的FIXP批量包尺寸
		input = element.getAttribute(ReplyMark.WIDE_PACKET_SIZE);
		size = (int)ConfigParser.splitLongCapacity(input, ReplyTransfer.getDefaultWidePacketContentSize());
		ReplyTransfer.setDefaultWidePacketContentSize(size);

		// 在公网传输时的FIXP子包尺寸
		input = element.getAttribute(ReplyMark.WIDE_SUBPACKET_SIZE);
		size = (int)ConfigParser.splitLongCapacity(input, ReplyTransfer.getDefaultWideSubPacketContentSize());
		ReplyTransfer.setDefaultWideSubPacketContentSize(size);

		// 流量块
		input = element.getAttribute(ReplyMark.FLOW_BLOCKS);
		size = ConfigParser.splitInteger(input, ReplyTransfer.getDefaultFlowBlocks());
		ReplyTransfer.setDefaultFlowBlocks(size);

		// SOCKET UDP读取一个包的时间
		input = element.getAttribute(ReplyMark.TIME_SLICE);
		int mms = ConfigParser.splitMicroTime(input, ReplyTransfer.getDefaultFlowTimeslice());
		ReplyTransfer.setDefaultFlowTimeslice(mms);

		// 如果是LINUX操作系统，调整包尺寸
		if (isLinux()) {
			ReplyTransfer.doLinuxPacketSize();
		}

		Logger.debug(this, "splitReplyServer", "flow blocks:%d, flow time slice: %d mms", 
				ReplyTransfer.getDefaultFlowBlocks(), ReplyTransfer.getDefaultFlowTimeslice());
		Logger.debug(this, "splitReplyServer", "packet size:%d, sub packet size:%d",
				ReplyTransfer.getDefaultPacketContentSize(), ReplyTransfer.getDefaultSubPacketContentSize());
		Logger.debug(this, "splitReplyServer", "wide packet size:%d, wide sub packet size:%d",
				ReplyTransfer.getDefaultWidePacketContentSize(), ReplyTransfer.getDefaultWideSubPacketContentSize());

		// 异步数据接收
		NodeList nodes = element.getElementsByTagName(ReplyMark.MK_REPLY_SUCKER);
		if (nodes.getLength() == 1) {
			Element sub = (Element) nodes.item(0);
			splitReplySucker(sub);
		}

		// 异步数据发送
		nodes = element.getElementsByTagName(ReplyMark.MK_REPLY_DISPATCHER);
		if (nodes.getLength() == 1) {
			Element sub = (Element) nodes.item(0);
			splitReplyDispatcher(sub);
		}
	}

	/**
	 * 解析本地站点的公共参数
	 * @param root
	 */
	private void splitLocalSite(Element root) {
		String input = root.getAttribute(SiteMark.STACK_SIZE);
		long stackSize = ConfigParser.splitLongCapacity(input, getStackSize());
		setStackSize(stackSize);

		// 解析主域参数
		NodeList nodes = root.getElementsByTagName(SiteMark.MARK_DOMAIN_SERVER);
		if (nodes.getLength() == 1) {
			splitDomainServer((Element) nodes.item(0));
		}

		// 解析异步接收/发送
		nodes = root.getElementsByTagName(SiteMark.MARK_REPLY_SERVER);
		if (nodes.getLength() == 1) {
			splitReplyServer((Element) nodes.item(0));
		}

		// 管理节点规定子节点的一般延时注册时间，只针对TOP/HOME/BANK三类管理节点

		input = XMLocal.getValue(root, SiteMark.HUB_REGISTER_INTERVAL);
		hubTimer.setRegisterInterval(ConfigParser.splitTime(input, 0));

		// 管理节点规定子节点的最大延时注册时间，只针对TOP/HOME/BANK三类管理节点。
		input = XMLocal.getValue(root, SiteMark.HUB_MAX_REGISTER_INTERVAL);
		hubTimer.setMaxRegisterInterval(ConfigParser.splitTime(input, 0));

		// 线程循环单次延时时间
		input = XMLocal.getValue(root, SiteMark.SILENT_TIME);
		setSilentTime(ConfigParser.splitTime(input, getSilentTime())); // 默认2秒间隔

		// 垃圾回收间隔时间
		input = XMLocal.getValue(root, SiteMark.GC_INTERVAL);
		setReleaseMemoryInterval(ConfigParser.splitTime(input, 0)); // 默认0分钟间隔，即不启动

		Logger.info(this, "splitLocalSite", "register interval:%d ms, max register interval:%d ms",
				hubTimer.getRegisterInterval(), hubTimer.getMaxRegisterInterval());

		Logger.info(this, "splitLocalSite", "silent time:%d ms, gc interval:%d ms", 
				getSilentTime(), getReleaseMemoryInterval());
	}

	/**
	 * 解析“单模式”站点地址，并且将参数写入站点配置中。
	 * @param site 当前站点
	 * @param document XML文档
	 * @return 解析和写入成功返回“真”，否则“假”。
	 */
	protected boolean splitSingleSite(Site site, Document document) {
		Element element = (Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);

		// 解析公共参数
		splitLocalSite(element);
		
		try {
			String value = XMLocal.getValue(element, SiteMark.LOCAL_NODE);
			Logger.debug(this, "splitSingleSite", "this is '%s'", value);
			Node e = new Node(value);
			// 判断类型一致
			if (e.getFamily() != getFamily()) {
				throw new UnknownHostException("illegal local " + value);
			}

			// 写入地址配置
			if (site != null) {
				// 解析前，如果原来类型定义，仍然要保留，FRONT节点做出现这种情况
				if (RankTag.isRank(site.getRank())) {
					e.setRank(site.getRank());
				}
				// 保留参数
				site.setNode(e);
			}

			// 内部网络IP地址
			replySucker.setDefinePrivateIP(e.getAddress().duplicate());
			replyDispatcher.setDefinePrivateIP(e.getAddress().duplicate());

			Logger.debug(this, "splitSingleSite", "%s", site);

			return true;
		} catch (UnknownHostException e) {
			Logger.error(e);
		}

		return false;
	}

	/**
	 * 解析网关站点地址，并且将参数写入配置中。
	 * 网关站点在“local.xml”文件提供“inner/outer”两个节点地址，允许IP地址是相同或者不同，TCP/UDP端口必须一致。
	 * @param site 网关站点
	 * @param document XML解析文档
	 * @return 解析和写入成功返回“真”，否则“假”。
	 */
	protected boolean splitGatewaySite(GatewaySite site, Document document) {
		Element element = (Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);

		// 解析公共参数
		splitLocalSite(element);

		try {
			// 内部通信地址
			String value = XMLocal.getValue(element, SiteMark.INNER_NODE);
			Node inner = new Node(value);
			// 外部通信地址
			value = XMLocal.getValue(element, SiteMark.OUTER_NODE);
			Node outer = new Node(value);

			if(inner.getFamily() != getFamily() || outer.getFamily() != getFamily()) {
				throw new UnknownHostException(String.format("illegal address %s,%s", inner,outer));
			}
			if (inner.getTCPort() != outer.getTCPort() || inner.getUDPort() != outer.getUDPort()) {
				throw new IllegalValueException("cannot be match %s,%s", inner, outer);
			}

			// 设置内部网络/外部网络通信地址
			site.setPrivate(inner);
			site.setPublic(outer);

			// 异步接收服务器的内部网络/外部网络IP地址
			replySucker.setDefinePrivateIP(inner.getAddress().duplicate());
			replySucker.setDefinePublicIP(outer.getAddress().duplicate());
			// 异步发送服务器的内部网络/外部网络IP地址
			replyDispatcher.setDefinePrivateIP(inner.getAddress().duplicate());
			replyDispatcher.setDefinePublicIP(outer.getAddress().duplicate());

			Logger.debug(this, "splitGatewaySite", "%s", site);

			return true;
		} catch (UnknownHostException e) {
			Logger.error(e);
		}

		return false;
	}

	/**
	 * 加载解析远程关闭服务
	 * @param document XML文档
	 * @return 成功返回真，否则假
	 */
	protected boolean loadShutdown(Document document) {
		Element element = (Element) document.getElementsByTagName("accept-shutdown-address").item(0);

		String[] items = XMLocal.getXMLValues(element.getElementsByTagName("address"));
		if (items == null) return true;
		try {
			for (int i = 0; i < items.length; i++) {
				Address address = new Address(items[i]);
				shutdownSheet.add(address);
			}
		} catch (UnknownHostException e) {
			Logger.error(e);
			return false;
		}

		Logger.info(this, "loadShutdown", "load successful");
		return true;
	}

	/**
	 * 检查地址是否在停止表集合中
	 * @param e 地址实例
	 * @return 返回真或者假
	 */
	public boolean onShutdowns(Address e) {
		Logger.debug(this, "onShutdowns", "address from %s", e);
		// 1. 远程地址与本地地址一致，证明是在一台主机上，允许退出
		boolean success = e.matchsIn(Address.locales());
		// 2. 检查集合中的地址是否匹配
		if (!success) {
			success = shutdownSheet.contains(e);
		}
		return success;
	}
	
	/**
	 * 生成随机密码
	 * @return 返回字节数组
	 */
	private byte[] createPassword() {
		ClassWriter writer = new ClassWriter();
		
		// 依赖类对象码，生成实例，做为密码处理
		ClassCode code = ClassCodeCreator.create(this, System.currentTimeMillis());
		writer.write(code.toBytes());
		// 生成随机数
		writer.writeLong(System.nanoTime());
		writer.writeLong(Runtime.getRuntime().maxMemory());
		writer.writeLong(Runtime.getRuntime().freeMemory());
		writer.writeLong(Runtime.getRuntime().totalMemory());
		writer.writeLong(Runtime.getRuntime().availableProcessors());

		return writer.effuse();
	}

	/**
	 * 生成默认的密钥令牌
	 */
	protected boolean createDefaultSecureToken(Document document) {
		// 解析安全配置文件
		Element element = (Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);
		String secureKeysize = XMLocal.getXMLValue(element.getElementsByTagName(SiteMark.SECURE_KEYSIZE));
		
		// 密钥大小，不能低于512位，这是RSA规定(RSA key must be at least 512 bits long)。
		int keysize = ConfigParser.splitInteger(secureKeysize, 512);
		if (keysize < 512) {
			keysize = 512;
		}

		// 生成密码
		byte[] pwd = createPassword();
		
		// 生成RSA密钥
		try {
			SecureRandom rnd = new SecureRandom(pwd);
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(keysize, rnd);
			KeyPair kp = kpg.generateKeyPair();
			RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

			// 密钥令牌
			SecureToken token = new SecureToken("DEFAULT-TOKEN", SecureType.CIPHER, SecureMode.COMMON);
			// 服务器密钥
			ServerKey serverKey = new ServerKey();
			serverKey.setKey(privateKey);
			serverKey.setStripe(new PrivateStripe(privateKey.getModulus(), privateKey.getPrivateExponent()));

			// 客户机密钥
			ClientKey clientKey = new ClientKey();
			clientKey.setKey(publicKey);
			clientKey.setStripe(new PublicStripe(publicKey.getModulus(), publicKey.getPublicExponent()));

			// 设置RSA密钥
			token.setServerKey(serverKey);
			token.setClientKey(clientKey);
			
//			Logger.debug(this, "createDefaultSecureToken", "%s & %s", serverKey.toString(), clientKey.toString());

			// 保存密钥令牌
			return SecureController.getInstance().add(token);
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		
		return false;
	}
	
	/**
	 * 解析FIXP服务器网络通信安全配置
	 * 将解析的RSA密钥保存到内存，用于网络通信时的数据加密和解密。
	 * 
	 * @param document XML文档
	 * @return 成功返回真，否则假
	 */
	protected boolean loadSecure(Document document) {
		// 1. 从"conf/security.xml"先取参数
		String bin = System.getProperty("user.dir");
		bin += "/../conf/security.xml";
		File file = new File(bin);
		// 2. 如果默认安全配置不存在，从根目录的"security.xml"文档取参数
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			// 解析安全配置文件
			String filename = XMLocal.getXMLValue(document.getElementsByTagName(OtherMark.SECURITY_NETWORK));
			filename = ConfigParser.splitPath(filename);
			// 判断有效
			success = (filename != null && filename.length() > 0);
			if (success) {
				file = new File(filename);
				success = (file.exists() && file.isFile());
			}
		}
		
		Logger.debug(this, "loadSecure", success, "split %s", file);
		
		// 以上判断成功，解析密钥令牌
		if (success) {
			SecureTokenParser parser = new SecureTokenParser();
			success = parser.split(file);
		}
		
		// 以上不成功，生成默认COMMON令牌
		if (!success) {
			success = createDefaultSecureToken(document);
		}

		Logger.note(this, "loadSecure", success,
				"load secure tokens, element size: %d", SecureController.getInstance().size());

		return success;
	}
	
	/**
	 * 将内存中的密钥令牌写入磁盘
	 * @return 返回真或者假
	 */
	public boolean writeSecure() {
		// 如果没有密钥信息，返回真
		if (SecureController.getInstance().size() == 0) {
			return false;
		}

		// 1. 定义目录文件
		String bin = System.getProperty("user.dir");
		bin += "/../conf/security.xml";
		File file = new File(bin);

		// 2. 生成XML格式的加密数据
		SecureTokenBuilder builder = new SecureTokenBuilder();
		byte[] b = builder.buildTokens();
		// 3. 保存到磁盘
		boolean success = false;
		if (b != null && b.length > 0) {
			try {
				FileOutputStream out = new FileOutputStream(file);
				out.write(b);
				out.close();
				success = true;
			} catch (IOException e) {

			}
		}
		return success;
	}
	
	/**
	 * 返回许可证文件。默认配置在节点的"conf"目录下面
	 * @return File实例
	 */
	protected File buildLicenceFile() {
		String bin = System.getProperty("user.dir");
		bin += "/../conf/licence";
		File file = new File(bin);
		boolean success = (file.exists() && file.isFile());
		return (success ? file : null);
	}

	/**
	 * 许可证支持跨网段通信，如NAT网络
	 * @return 返回真或者假
	 */
	public boolean isSkipcast() {
		return licenceToken.isSkipcast();
	}

	/**
	 * 判断许可证没有时间限制
	 * @return 返回真或者假
	 */
	public boolean isLicenceInfinite() {
		return licenceToken.isInfinite();
	}

	/**
	 * 判断许可证已经超时
	 * @return 返回真或者假
	 */
	public boolean isLicenceTimeout() {
		return licenceToken.isTimeout();
	}

	/**
	 * 判断许可证还有XXX天超期
	 * @param day 日
	 * @return 返回真或者假
	 */
	public boolean isLicenceTimeout(int day) {
		return licenceToken.isTimeout(day);
	}

	/**
	 * 检查用户签名许可。
	 * 返回三种状态：1. 忽略 2. 拒绝 3. 允许
	 * 可以没有部署许可证或者没有录入签名，如果部署且在启动时录入签名时，必须一致！
	 * 
	 * @return 返回三种状态之一
	 */
	protected int checkLicence() {
		// 从指定的磁盘目录构造一个许可证文件，如果没有忽略它
		File file = buildLicenceFile();
		if (file == null) {
			//			Logger.warning(this, "checkLicence", "cannot be find licence file");
			return Licence.LICENCE_IGNORE;
		}
		// 读系统的用户签名输入，如果没有忽略！
		String signature = getSignature();
		if (signature == null) {
			//			Logger.warning(this, "checkLicence", "cannot be find signature!");
			return Licence.LICENCE_IGNORE;
		}

		// 解码许可证文件，如果没有就忽略！
		byte[] b = LicenceRegister.read(file);
		if (b == null) {
			Logger.warning(this, "checkLicence", "cannot resolve %s", file);
			return Licence.LICENCE_REFUSE;
		}

		// 解析文本
		org.w3c.dom.Document document = XMLocal.loadXMLSource(b);
		if (document == null) {
			Logger.warning(this, "checkLicence", "cannot resolve content!");
			return Licence.LICENCE_REFUSE;
		}

		// 有效时间
		org.w3c.dom.NodeList list = document.getElementsByTagName("permit-time");
		if (list.getLength() != 1) {
			return Licence.LICENCE_REFUSE;
		}

		// 许可证有效时间
		org.w3c.dom.Element element = (org.w3c.dom.Element) list.item(0);
		String beginTime = XMLocal.getValue(element, "begin-time");
		String endTime = XMLocal.getValue(element, "end-time");
		// 解析开始/结束时间
		if (!licenceToken.splitBeginTime(beginTime)) {
			return Licence.LICENCE_REFUSE;
		}
		if (!licenceToken.splitEndTime(endTime)) {
			return Licence.LICENCE_REFUSE;
		}

		Logger.info(this, "checkLicence", "\'%s\' to \'%s\'", beginTime, endTime);

		// 跨网段通信
		String value = XMLocal.getXMLValue(document.getElementsByTagName("skipcast"));
		boolean skipcast =	ConfigParser.splitBoolean(value, false);
		licenceToken.setSkipcast(skipcast);

		// 取出签名
		value = XMLocal.getXMLValue(document.getElementsByTagName("signature"));
		// 判断一致或者否
		boolean success = (value != null && signature.compareToIgnoreCase(value) == 0);
		Logger.note(this, "checkLicence", success, "check signature: \'%s\', skipcast %s", 
				signature, (isSkipcast() ? "Yes" : "No"));
		return (success ? Licence.LICENCE_ALLOW : Licence.LICENCE_REFUSE);
	}

	/**
	 * 读磁盘文件
	 * 
	 * @param file 磁盘文件
	 * @return 读取的文件数组
	 */
	public byte[] readFile(File file) {
		if (!file.exists()) {
			return null;
		}
		try {
			byte[] b = new byte[(int) file.length()];
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			return b;
		} catch (IOException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 写磁盘文件
	 * 
	 * @param file 磁盘文件
	 * @param b 字节数组
	 * @return 成功返回真，否则假
	 */
	public boolean flushFile(File file, byte[] b) {
		boolean success = false;
		try {
			FileOutputStream out = new FileOutputStream(file);
			// 判断数据有效，再写入到磁盘
			if (b != null && b.length > 0) {
				out.write(b);
			}
			out.close();
			success = true;
		} catch (IOException e) {
			Logger.error(e);
		}
		return success;
	}

	/**
	 * 返回资源配置目录
	 * @return 配置目录实例
	 */
	public File getResourcePath() {
		return resourcePath;
	}

	/**
	 * 建立配置文件存储目录
	 * @param path 存储目录
	 * @return 成功返回真，否则假
	 */
	protected boolean createResourcePath(String path) {
		path = ConfigParser.splitPath(path);
		File dir = new File(path);
		// 检查目录存在，如果不存在，建立一个新目录
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			success = dir.mkdirs();
		}
		if (success) {
			try {
				resourcePath = dir.getCanonicalFile();
			} catch (IOException e) {
				resourcePath = dir.getAbsoluteFile();
				Logger.error(e);
			}
		}

		// 记录日志写入目录
		if (success) {
			addDeviceDirectory(resourcePath.getAbsolutePath());
		}

		Logger.note(this, "createResourcePath", success, "directory is '%s'", resourcePath);
		return success;
	}

	/**
	 * 生成资源目录下的文件
	 * @param filename 文件名
	 * @return 带目录的文件名
	 */
	public File createResourceFile(String filename) {
		return new File(resourcePath, filename);
	}

	/**
	 * 启动全部管理池
	 * @param pools 管理池数组
	 * @return 全部启动成功返回真，否则假
	 */
	protected boolean startAllPools(VirtualPool[] pools) {
		int index = 0;
		for (; index < pools.length; index++) {
			// 启动管理池
			boolean success = pools[index].start();
			if (!success) {
				Logger.error(this, "startAllPools", "cannot be load %s", pools[index].getClass().getName());
				break;
			}
			// 等待进入状态
			while (!pools[index].isRunning()) {
				delay(200L);
			}
		}
		return index == pools.length;
	}

	/**
	 * 停止全部管理池
	 * @param pools 管理池数组
	 */
	protected void stopAllPools(VirtualPool[] pools) {
		// 顺序停止
		for (int index = 0; index < pools.length; index++) {
			if (index > 0) {
				delay(100L);
			}
			pools[index].stop();
		}
		// 检查全部完成后退出
		for(int index = 0; index < pools.length; index++) {
			// 如果这个线程没有释放，等待它释放再进入下一个
			while(pools[index].isRunning()) {
				delay(100L);
			}
		}
	}

	/**
	 * 启动全部线程
	 * @param threads 线程数组
	 * @return 成功返回真，否则假
	 */
	protected boolean startThreads(VirtualThread[] threads) {
		int index = 0;
		for (; index < threads.length; index++) {
			boolean success = threads[index].start();
			if (!success) {
				Logger.error(this, "s", "cannot be load %s", threads[index].getClass().getName());
				break;
			}

			// 等待线程直到进入状态
			while (!threads[index].isRunning()) {
				delay(100);
			}
		}
		return index == threads.length;
	}

	/**
	 * 停止全部线程
	 * @param threads 线程数组
	 */
	protected void stopThreads(VirtualThread[] threads) {
		// 顺序停止
		for (int index = 0; index < threads.length; index++) {
			if (index > 0) {
				delay(100);
			}
			threads[index].stop();
		}
		// 检查全部完成后退出
		for(int index = 0; index < threads.length; index++) {
			// 如果这个线程没有释放，等待它释放再进入下一个
			while(threads[index].isRunning()) {
				delay(200);
			}
		}
	}

	/**
	 * 从本地"local.xml"中加载日志/追踪/消耗资源配置，做为远程连接的准备参数
	 * @param filename local.xml配置库文件
	 * @return 成功返回真，否则假
	 */
	protected boolean loadLogResourceWithRemote(String filename) {
		// 加载日志配置
		boolean success = Logger.loadXML(filename);
		if (success) {
			loadLogDeviceDirectory();
		}
		// 加载追踪配置
		if (success) {
			success = Tigger.loadXML(filename);
			if (success) {
				loadTigDeviceDirectory();
			}
		}
		// 加载消耗配置
		if (success) {
			success = Biller.loadXML(filename);
			if (success) {
				loadBillDeviceDirectory();
			}
		}
		return success;
	}

	/**
	 * 从本地“”中加载日志/追踪资源配置，同时启动日志/追踪的本地服务
	 * @param filename local.xml配置文件
	 * @return 返回返回真，否则假
	 */
	protected boolean loadLogResourceWithLocal(String filename) {
		// 加载日志
		boolean success = Logger.loadXML(filename);
		if (success) {
			success = Logger.loadService(); // 本地启动服务
		}
		if (success) {
			loadLogDeviceDirectory();
		}
		// 加载追踪
		if (success) {
			success = Tigger.loadXML(filename);
			if (success) {
				success = Tigger.loadService(); // 本地启动服务
			}
			if (success) {
				success = Biller.loadService(); // 启动本地服务
			}
			if (success) {
				loadTigDeviceDirectory();
			}
		}
		return success;
	}

	/**
	 * 获得与上级站点的连接 
	 * @param endpoint 上级站点地址
	 * @return 成功返回HubClient句柄，否则是空指针
	 */
	public HubClient fetchHubClient(SocketHost endpoint) {
		return ClientCreator.createHubClient(endpoint);
	}

	/**
	 * 获得与上级站点的连接 
	 * @param hub 上级站点地址
	 * @return 成功返回HubClient句柄，否则是空指针
	 */
	public HubClient fetchHubClient(SiteHost hub) {
		return ClientCreator.createHubClient(hub);
	}

	/**
	 * 获得与上级站点的连接 
	 * @param hub 上级站点地址
	 * @param stream 流模式
	 * @return 成功返回HubClient句柄，否则是空指针
	 */
	public HubClient fetchHubClient(SiteHost hub, boolean stream) {
		return ClientCreator.createHubClient(hub, stream);
	}

	/**
	 * 获得与上级站点的连接 
	 * @param hub 上级站点地址
	 * @return 成功返回HubClient句柄，否则是空指针
	 */
	public HubClient fetchHubClient(Node hub) {
		return ClientCreator.createHubClient(hub);
	}

	/**
	 * 以异步模式，重新注册到管理节点
	 * @param site 当前站点元数据
	 * @return 注册成功返回真，否则假
	 */
	protected boolean register(Site site) {
		LoginSite cmd = new LoginSite(site);
		cmd.setReply(true); 	// 需要反馈应答
		cmd.setFast(true); 		// 注册是最高优先级，受理服务器马上处理。

		LoginSiteHook hook = new LoginSiteHook();
		ShiftLoginSite shift = new ShiftLoginSite(cmd, hook);
		return getCommandPool().admit(shift);
	}

	/**
	 * 注册到上级管理站点。<br>
	 * 默认采用TCP连接模式，这个通道数据流量小，可靠性高，是专门给注册不允许延时的业务准备的。<br><br>
	 * 
	 * @param site 当前站点元数据信息
	 * @param hub 服务器地址
	 * @return 注册成功返回“真”，否则“假”。
	 */
	protected boolean login(Site site, Node hub) {
		HubClient client = fetchHubClient(hub);
		// 如果是空指针，是错误
		if (client == null) {
			Logger.error(this, "login", "cannot be fetch %s", hub);
			return false;
		}

		// 去上级节点注册
		boolean success = false;
		try {
			success = client.login(site);
			client.close(); // 雅致关闭
		} catch (VisitException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		client.destroy(); // 强制关闭。如果雅致关闭成功，这里不起作用

		// 如果注册成功，删除旧的密钥
		// 同步的，HubVisitOnTop/HubVisitOnHome/HubVisitOnBank也要删除客户机，即这个Site地址密钥
		if (success) {
			Cipher cipher = dropCipher(hub);
			Logger.debug(this, "login", cipher != null, "drop secure cipher %s # %s", hub, cipher);
		}

		// 修改登录状态
		setLogined(success);
		// 如果注册成功，修改强制注销状态为假，和刷新时间。
		if (success) {
			setRoundSuspend(false);
			// 刷新时间
			refreshEndTime();
			// 发送HELO包激活信道UDP通信
			hello(hub.getPacketHost());
		}

		Logger.note(this, "login", success, "to %s", hub);

		// 返回结果
		return success;
	}

	/**
	 * 客户端发起RPC连接通知服务器（管理节点），删除服务器上的客户机记录，退出登录状态
	 * @param local 本地节点地址
	 * @param hub 服务器节点地址（管理节点）
	 * @return 成功返回真，否则假
	 */
	protected boolean logout(Node local, Node hub) {
		HubClient client = fetchHubClient(hub);
		if (client == null) {
			Logger.error(this, "logout", "cannot be git %s", hub);
			return false;
		}
		// 远程注销
		boolean success = false;
		try {
			success = client.logout(local);
			client.close(); // 雅致关闭
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 强制销毁。如果已经关闭，这里不起作用。
		client.destroy();

		// 成功
		if (success) {
			// 客户机删除服务端密钥
			Cipher cipher = dropCipher(hub);
			Logger.debug(this, "logout", cipher != null, "drop secure cipher %s # %s", hub, cipher);
			// 登录为假
			setLogined(false);
		}
		
		Logger.note(this, "logout", success, "from %s", hub);

		// 返回结果
		return success;
	}

	/**
	 * 从指定的上级站点注销当前站点
	 * @param hub 上级站点地址
	 * @return 注销成功返回“真”，否则“假”。
	 */
	public boolean logout(Node hub) {
		return logout(getListener(), hub);
	}

	/**
	 * 从注册站点注销
	 * @return 注销成功返回“真”，否则“假”。
	 */
	public boolean logout() {
		Node hub = getHub();
		boolean success = (hub != null);
		if (success) {
			success = logout(hub);
		}
		return success;
	}

	/**
	 * 通过自己的FIXP PACKET MONITOR信道，删除服务器端的对称密钥。<br>
	 * @return 成功返回真，否则假
	 */
	protected boolean dropSecure() {
		Node hub = getHub();
		return dropSecure(hub);
	}

	/**
	 * 通过自己的FIXP PACKET MONITOR信道，删除服务器端的对称密钥。<br>
	 * 这个操作建立在通信双方采用密文通道传输基础上。
	 * 
	 * @param hub 注册服务器的UDP通信地址
	 * @return 删除服务器密钥成功返回真，否则假
	 */
	protected boolean dropSecure(Node hub) {
		boolean success = (hub != null);
		if (success) {
			SocketHost host = hub.getPacketHost();
			success = dropSecure(host);
		}
		Logger.note(this, "dropSecure", success, "To %s", hub);
		return success;
	}

	/**
	 * 通过自己的FIXP PACKET MONITOR信道，删除服务器端的对称密钥。<br>
	 * 这个操作建立在通信双方采用密文通道传输基础上。
	 * 
	 * @param remote 注册服务器的UDP通信地址
	 * @return 删除服务器密钥成功返回真，否则假
	 */
	protected boolean dropSecure(SocketHost remote) {
		// 私属信道超时时间
		int timeout = SocketTransfer.getDefaultChannelTimeout(); 
		return dropSecure(remote, timeout, 3);
	}

	/**
	 * 通过自己的FIXP PACKET MONITOR信道，删除服务器端的对称密钥。<br>
	 * 这个操作建立在通信双方采用密文通道传输基础上。
	 * 
	 * @param remote 注册服务器的UDP通信地址
	 * @param timeout 超时
	 * @param count 失败尝试重新次数
	 * @return 删除服务器密钥成功返回真，否则假
	 */
	protected boolean dropSecure(SocketHost remote, long timeout, int count) {
		// 如果是空指针，忽略它！
		if (remote == null) {
			Logger.warning(this, "dropSecure", "this is null pointer!");
			return true;
		}
		Cipher cipher = findCipher(remote);
		// 没有密文，认为是正确，退出！
		if (cipher == null) {
			Logger.warning(this, "dropSecure", "cannot be find Cipher!");
			return true;
		}

		// 通过FIXP PACKET MONITOR信道，删除密文
		boolean success = packetMonitor.getPacketHelper().dropPrivateSecure(
				remote, timeout, count, cipher);

		Logger.note(this, "dropSecure", success, "to %s, cipher %s, timeout %d ms",
				remote, cipher, timeout);

		return success;
	}

	/**
	 * 预加载操作 <br><br>
	 * 发生在“init”方法的第一段，做初始化前的准备工作，包括：<br>
	 * 1. 获得日志配置 <br>
	 * 2. 系统规定的站点超时时间 <br>
	 * 3. 协调统一集群时间（时间轴，大量数据处理工作需要时间做为判断标准）。<br>
	 * 其中加载日志是可选项。<br><br>
	 * 
	 * @param hub 服务器地址
	 * @return 成功返回真，否则假
	 */
	protected boolean preload(Node hub) {
		// 在“<log> </log>”中定义的日志传输模式，包括：不输出、输出到磁盘、输出到日志服务器
		boolean sendLogToServer = Logger.isSendToServer();
		// 在 "<tig> </tig>" 中定义的操作传输模式，包括：不输出，输出到磁盘、输出到日志服务器
		boolean sendTigToServer = Tigger.isSendToServer();
		// 在 "<bill> </bill>" 中定义的操作传输模式，包括：不输出，输出到磁盘、输出到日志服务器
		boolean sendBillToServer = Biller.isSendToServer();

		HubClient client = fetchHubClient(hub);
		if (client == null) {
			Logger.error(this, "preload", "cannot find %s", hub);
			return false;
		}

		boolean success = false;
		try {
			// 1. 加载日志服务
			if (sendLogToServer) {
				Node server = client.findLogSite(getFamily());
				success = (server != null);
				Logger.note(this, "preload", success, "send log to %s", server);
				if (success) {
					success = Logger.loadService(server.getHost());
				}
			} else {
				success = Logger.loadService(null);
				Logger.note(this, "preload", success, "load local log");
			}

			// 2. 加载日志追踪服务
			if (sendTigToServer) {
				Node server = client.findTigSite(getFamily());
				success = (server != null);
				Logger.note(this, "preload", success, "send tig to %s", server);
				if (success) {
					success = Tigger.loadService(server.getHost());
				}
			} else {
				success = Tigger.loadService(null);
				Logger.note(this, "preload", success, "load local tig");
			}

			// 3. 加载消费记录服务
			if (sendBillToServer) {
				Node server = client.findBillSite(getFamily());
				success = (server != null);
				Logger.note(this, "preload", success, "send bill to %s", server);
				if (success) {
					success = Biller.loadService(server.getHost());
				}
			} else {
				success = Biller.loadService(null);
				Logger.note(this, "preload", success, "load local bill");
			}

			// 检查版本一致
			if (success) {
				Version other = client.getVersion();
				success = (Laxkit.compareTo(getVersion(), other) == 0);
				Logger.note(this, "preload", success, "check version  \"%s\" - \"%s\"", getVersion(), other);
			}

			// 2. 站点超时时间，单位：毫秒
			if (success) {
				long ms = client.getSiteTimeout(getFamily());
				success = (ms > 0);
				if (success) {
					setSiteTimeoutMillis(ms);
				}
				Logger.note(this, "preload", success, "current site \"%s\" timeout %d",
						SiteTag.translate(getFamily()), getSiteTimeoutMillis());
			}

			// 3. 注册间隔时间
			if (success) {
				long interval = client.getHubRegisterInterval();
				interval = registerTimer.setInterval(interval);
				Logger.note(this, "preload", success, "register interval: %d ms", interval);

				interval = client.getHubMaxRegisterInterval();
				interval = registerTimer.setMaxInterval(interval);
				Logger.note(this, "preload", success, "max register interval: %d ms", interval);
			}
			
			// 同步上级节点的系统时间，默认是“假”。“laxcus.systemtime.synchronization”做为系统属性在site.sh文件中定义
			boolean updateTime = ConfigParser.splitBoolean(
					System.getProperty("laxcus.systemtime.synchronization"), false);

			// 4. 调用JNI设置本地系统时间(ROOT状态下才能生效)
			if (success && updateTime) {
				long time = client.currentTime();
				success = (time != 0L);
				if (success) {
					int ret = SystemTime.set(time);
					success = (ret == 0);
				}
				Logger.note(this, "preload", success, "set time %d", time);
			}
			// 关闭连接
			client.close();
		} catch(VisitException e) {
			success = false;
			Logger.error(e);
		}

		// 强制关闭。如果以上操作失败时；否则这个不起作用。
		client.destroy();

		return success;
	}

	/**
	 * 默认的激活
	 */
	private void defaultHello() {
		int size = 1; // 发送次数
		if (isHurry()) { // 注册站点要求激活
			size = 5;
			unhurry();
		} else if (isMaxTimeout()) { // 发生最大超时
			size = 3;
		} else if (isMinTimeout()) { // 一般性超时
			size = 2;
		}

		for (int index = 0; index < size; index++) {
			boolean success = hello();
			// 如果不成功，尝试重新注册
			if (!success) {
				kiss(false);
				break;
			}
		}
	}

	/**
	 * 诊断服务器激活且有效
	 * @param remote 目标服务器UDP地址
	 * @param receiveTimeout 接收超时
	 * @return 成功返回真，否则假
	 */
	private boolean detect(SocketHost remote, int receiveTimeout) {
		boolean success = false;
		FixpPacketClient client = new FixpPacketClient();
		// 接收超时和子包超时
		client.setReceiveTimeout(receiveTimeout); 
		client.setSubPacketTimeout(receiveTimeout);
		
		try {
			// 绑定本地任意地址和端口
			boolean bound = client.bind();
			if (bound) {
				// 测试服务器，返回本地socket地址
				SocketHost host = client.test(remote, false);
				success = (host != null); // 判断成功！
			}
			// 关闭
			client.close();
		} catch (IOException e) {
//			Logger.error(e.getMessage());
			Logger.error(e);
		} catch(Throwable e) {
			Logger.fatal(e);
		}
		// 销毁
		client.destroy();

		Logger.note(this, "detect", success, "check %s", remote);

		// 成功返回真
		return success;
	}

	/**
	 * 检测失效的服务器节点
	 * @param remote 服务器地址
	 * @param timeout 超时时间
	 * @return 节点生效返回真，否则假
	 */
	private boolean checkDisableHub(SocketHost remote, long timeout) {
		Logger.debug(this, "checkDisableHub", "hub is %s, timeout %d ms", remote, timeout);
		
		final long sleepTime = 2000;
		final long ringTime = 18000;

		// 确定最后时间，循环判断！
		long endTime = System.currentTimeMillis() + timeout;
		// 当前时间小于结束值，进入循环，否则退出！
		do {
			// 计算本次线程睡眠时间,2秒
			long now = System.currentTimeMillis();
			long ms = (endTime - now > sleepTime ? sleepTime : endTime - now);
			delay(ms);

			// 1. 收到中断或者达到最大时间，返回真
			boolean allow = (isInterrupted() || System.currentTimeMillis() >= endTime);
			if (allow) {
				break;
			}
			// 2. 得到外部其它通知：1. 切换到管理节点，2. 进入自循环状态（用户手动处理产生，出现在WATCH/FRONT.TERMINAL节点）
			allow = (isSwitchHub() || isHandStop());
			if (allow) {
				break;
			}

			// 检测服务器激活，18秒接收超时
			now = System.currentTimeMillis();
			ms = (endTime - now > ringTime ? ringTime : endTime - now);
			// 保证最少1秒钟
			if (ms >= 1000) {
				boolean success = detect(remote, (int) ms);
				if (success) {
					return true;
				}
			}
		} while (true);

		// 不成功
		return false;
	}

	/**
	 * 判断是手动停止
	 * @return 返回真或者假
	 */
	private boolean isHandStop() {
		return isLogout() && isRoundSuspend();
	}
	
	/**
	 * 返回检测的主机地址
	 * 先取引导节点地址，如果没有，再取目标节点地址
	 * @return 返回主机地址，或者空指针
	 */
	private SocketHost getDetectHost() {
		if (getRootHub() != null) {
			return getRootHub().getPacketHost();
		}
		if (getHub() != null) {
			return getHub().getPacketHost();
		}
		return null;
	}

	/**
	 * 站点线程循环检测，如果发生站点切换、注册、重新注册的操作，将按照要求执行；
	 * 如果达到触发时间，将启动HELO激活，否则进入睡眠状态。
	 * @param endtime 指定的“HELLO”激活时间
	 * @return 返回下一次的“HELLO”激活时间 
	 */
	protected long silent(long endtime) {
		// 要求切换注册主机，交出“HELLO”触发和延时；或者注销且是用户手动注销时，也延时。
		if (isSwitchHub()) {
			if (isSwitchActive()) {
				setSwitchState(SitePrecursor.SWITCH_LAUNCH);
			}
			endtime = 0L; // 清零
			delay(2000L);
		}
		// 这是用户手动注销时的状态...
		else if (isHandStop()) { // (isLogout() && isRoundSuspend()) {
			endtime = 0L; // 清零
			delay(2000L);
		}
		// 要求重新注册
		else if (isKiss()) {
			// <1> 判断处于登录状态，若不是，去检查服务器节点
			boolean pass = isLogined();
			if (!pass) {
				SocketHost remote = getDetectHost();
				if (remote != null) {
					pass = checkDisableHub(remote, getDisableTimeout()); // 检测服务器节点
				}
			}
			if (pass) {
				// <2> 无论上述是否成功，都进行再次注册！
				boolean success = login();
				// 失败，延时再注册！成功，撤销注册！
				setKiss(success ? false : true);

				// <3> 重新注册成功，撤销重新注册参数。注册不成功，选择一个最大延时再试。
				if (success) {
					setCheckin(false); // 不要重新注册
					refreshEndTime(); // 刷新时间
					hello(); // 立即发送HELO
				} else {
					delay(2000L); // 延时2秒
				}
			}
			endtime = nextTouchTime(); // 下次触发时间
		}
		// 站点失效超时
		else if (isDisableTimeout()) {
			// 交给子类处理
			disableProcess();
			// 要求触发重新注册...
			kiss(false);
			// 下一次触发时间 ...
			endtime = nextTouchTime();
		}
		// 正常超时，或者要求催促握手时...
		else if (isTouchTimeout(endtime) || isHurry()) {
			endtime = nextTouchTime(); // 下次触发时间
			defaultHello(); // 发送HELO命令，保持双方处于连接中
		} 
		// 其它，定时休眠...
		else {
			resting(endtime);
		}

		// 下一次触发时间
		return endtime;
	}

	/**
	 * 默认的线程处理循环，保持与注册站点的通信。直到被要求退出。
	 */
	protected void defaultProcess() {
		Logger.info(this, "defaultProcess", "into ...");

		// 刷新时间和发送HELO激活
		long endtime = refreshEndTime();
		if (isLogined()) {
			hello();
		}

		// 定时2分钟检查一次
		final long scheduleTimeout = 2 * 60 * 1000L;

		// 站点进入轮循状态，直到被要求退出...
		while (!isInterrupted()) {
			// 达到延时注册时间后，或者要求重新注册时，注册到上级站点
			if (registerTimer.isTouch() || isCheckin()) {
				// 恢复重新注册为假
				setCheckin(false);
				// 刷新
				registerTimer.refresh();

				// 必须确认在登录状态，才能重新注册；否则，启动FixpStreamClient注册！
				if (isLogined()) {
					register();
				} else {
					kiss(false); // 启动FixpStreamClient注册
				}
			}

			// 达到超时时间，清除过期任务
			if (timer.isTimeout(scheduleTimeout)) {
				timer.purge();
				timer.refreshTime();
			}

			// 处理子级业务
			defaultSubProcess();

			// 静默延时...
			endtime = silent(endtime);
		}

		// 撤销全部任务
		timer.cancel();

		Logger.info(this, "defaultProcess", "exit ...");
	}

	/**
	 * 子级处理操作，被"defaultProcess"调用，处理子级专属业务。子类如有业务需要，可以派生它！
	 * 注意：被处理的业务不能够消耗太多时间，否则将影响“defaultProcess”方法正常的业务处理。
	 */
	protected void defaultSubProcess() {

	}

	/**
	 * 切换注册到新的站点
	 * @param hub 新的注册站点地址
	 * @return 注册成功返回“真”，否则“假”。
	 */
	public boolean switchHub(Node hub) {
		// 判断原注册地址。如果是空值，不处理
		Node origin = getHub();
		if (origin == null) {
			return false;
		}

		// 设置激活状态，等待线程切换到“进入”状态
		setSwitchState(SitePrecursor.SWITCH_ACTIVE);

		// 停止命令钩子，对应“dropSecure”方法
		getPacketHelper().stopDropSecureHook(origin.getPacketHost());

		// 如果有线程处于睡眠状态，唤醒它（不保证一定有线程睡眠）。
		wakeup();

		// 切换节点！
		Logger.info(this, "switchHub", "%s switch to %s", origin, hub);

		// 等待"silent"方法判断“SWITCH_AVTIVE”，切换到“SWITCH_LAUNCH”状态。本地判断是“SWITCH_LAUNCH”后退出
		while (!isSwitchLaunch()) {
			delay(500);
		}

		// 从指定的节点退出（注销）
		boolean success = logout();
		Logger.note(this, "switchHub", success, "logout from %s", origin);
		// 删除密文
		removeCipher(origin);
		// 设置新的主机地址
		setHub(hub);
		// 注册到新的站点
		success = login();

		// 切换节点！
		Logger.note(this, "switchHub", success, "exchange to %s", hub);

		// 成功切换管理节点，不要再注册！不成功，恢复为原来的注册地址。
		if (success) {
			setKiss(false);
		} else {
			setHub(origin);
		}

		// 恢复原来的状态
		setSwitchState(SitePrecursor.SWITCH_NONE); 
		return success;
	}

	/**
	 * 去注册的FIXP UDP服务器确定当前节点地址
	 * @param hub 注册FIXP UDP服务器地址
	 * @param timeout 超时时间，单位：毫秒
	 * @return 返回自己的FIXP UDP服务器地址，失败返回空指针
	 */
	protected SocketHost reflect(SocketHost hub, int timeout) {
		return packetMonitor.getPacketHelper().checkReflect(hub, timeout);
	}

	/**
	 * 去注册的FIXP UDP服务器确定当前节点地址。超时默认是60秒。
	 * @param hub 注册FIXP UDP服务器地址
	 * @return 返回自己的FIXP UDP服务器的地址
	 */
	protected SocketHost reflect(SocketHost hub) {
		return reflect(hub, SocketTransfer.getDefaultChannelTimeout());
	}

	/**
	 * 判断当前节点运行在内网，通过NAT设备与外网连接。<br>
	 * 条件成立，内网和外网：FixpPacketMonitor/FixpPacketMonitor, ReplySucker/ReplyDispatcher之间已经建立了通道。<br>
	 * 
	 * @return 返回真或者假
	 */
	public boolean isPock() {
		return getPacketHelper().hasPocks();
	}

	/**
	 * 通过网关节点，找到内网节点在NAT设备上的定位地址。
	 * @param remote 网关节点地址
	 * @return SocketHost实例，或者空指针
	 */
	public SocketHost findPockLocal(SocketHost remote) {
		return getPacketHelper().findPockLocal(remote);
	}

	/**
	 * 节点瞬时参数
	 * @return Moment实例
	 */
	protected Moment createMoment() {
		// 返回当前站点状态
		Moment moment = new Moment();

		// 判断LINUX/WINDOWS操作系统，选择参数
		if (isLinux()) {
			DeviceStamp memory = new DeviceStamp(LinuxDevice.getInstance().getSysMaxMemory(),
					LinuxDevice.getInstance().getSysUsedMemory(), LinuxDevice.getInstance().isMemoryMissing());
			DeviceStamp disk = new DeviceStamp(LinuxDevice.getInstance().getSysMaxDisk(),
					LinuxDevice.getInstance().getSysUsedDisk(), LinuxDevice.getInstance().isDiskMissing());

			moment.setSysMemory(memory);
			moment.setSysDisk(disk);
		} else if (isWindows()) {
			DeviceStamp memory = new DeviceStamp(WindowsDevice.getInstance().getSysMaxMemory(),
					WindowsDevice.getInstance().getSysUsedMemory(), WindowsDevice.getInstance().isMemoryMissing());
			DeviceStamp disk = new DeviceStamp(WindowsDevice.getInstance().getSysMaxDisk(),
					WindowsDevice.getInstance().getSysUsedDisk(), WindowsDevice.getInstance().isDiskMissing());

			moment.setSysMemory(memory);
			moment.setSysDisk(disk);
		}

		// 检测虚拟机内存
		Runtime rt = Runtime.getRuntime();
		long usedMemory = (rt.totalMemory() - rt.freeMemory()); // 实际使用的内存
		boolean missing = EchoTransfer.isVMMemoryMissing(usedMemory, rt.maxMemory());
		DeviceStamp vm = new DeviceStamp(rt.maxMemory(), usedMemory, missing);
		moment.setVMMemory(vm);

		return moment;
	}
	
	/**
	 * 返回引导节点地址。<br><br>
	 * 
	 * 这个方法针对FRONT节点，它需要从ENTRANCE引导执行，其他节点目前不需要。<br>
	 * FRONT节点覆盖这个方法。<br><br>
	 * 
	 * @return 返回实例
	 */
	public Node getRootHub() {
		return null;
	}

	/**
	 * 当前站点注册到它的上级站点。<br><br>
	 * 
	 * 发生在以下情况下：<br>
	 * 1. 节点启动的注册。<br>
	 * 2. 发生故障后，"silent"方法定时调用它自动注册。<br><br>
	 * 
	 * @return 注册成功返回“真”，否则“假”。
	 */
	public abstract boolean login();

	/**
	 * 以异步模式（INVOKE/PRODUCE）重新注册到管理节点。
	 * 此方法是保护类型，只被子类实现和内部使用。
	 * 外部接口通过调用“touch”方法实现
	 */
	protected abstract void register();

	/**
	 * 输出注册站点地址。
	 * @return 返回注册站点地址实例。如果是TOP站点，这是空指针。
	 */
	public abstract Node getHub();

	/**
	 * 设置注册站点地址。
	 * @param e 注册站点实例
	 */
	public abstract void setHub(Node e);

	/**
	 * 返回网关的外网节点地址
	 * @return 如果是网关，返回外网地址，否则返回空指针
	 */
	public abstract Node getPublicListener() ;

	/**
	 * 输出当前站点
	 * @return Site子类实例
	 */
	public abstract Site getSite();

	/**
	 * 返回命令管理池
	 * 
	 * @return CommandPool子类实例
	 */
	public abstract CommandPool getCommandPool();

	/**
	 * 返回调用器管理池
	 * 
	 * @return InvokerPool子类实例
	 */
	public abstract InvokerPool getInvokerPool();

	/**
	 * 返回自定义资源代理
	 * 
	 * @return CustomTrustor实例
	 */
	public abstract CustomTrustor getCustomTrustor();

	/**
	 * 站点最大超时失效后，执行故障处理。
	 * 除了TOP站点，其它通常是重新注册。
	 */
	protected abstract void disableProcess();

	/**
	 * 加载许可证。
	 * 许可证的加载操作只限服务端节点。
	 * 这些节点包括：TOP/HOME/BANK, LOG, DATA/WORK/BUILD/CALL, ACCOUNT/HASH/GATE/ENTRANCE
	 * 
	 * @param remote 远程操作操作本地
	 * @return 成功返回真，否则假
	 */
	public abstract boolean loadLicence(boolean remote);

}

//	/**
//	 * 判断当前站点是网关
//	 * @return 返回真或者假
//	 */
//	public abstract boolean isGateway() ;

///**
// * 站点线程循环检测，如果发生站点切换、注册、重新注册的操作，将按照要求执行；
// * 如果达到触发时间，将启动HELO激活，否则进入睡眠状态。
// * @param endtime 指定的“HELLO”激活时间
// * @return 返回下一次的“HELLO”激活时间 
// */
//protected long silent(long endtime) {
//	// 要求切换注册主机，交出“HELLO”触发和延时；或者注销且是用户手动注销时，也延时。
//	if (isSwitchHub()) {
//		if (isSwitchActive()) {
//			setSwitchState(SitePrecursor.SWITCH_LAUNCH);
//		}
//		endtime = 0L; // 清零
//		delay(2000L);
//	} else if (isLogout() && isRoundSuspend()) {
//		endtime = 0L; // 清零
//		delay(2000L);
//		//	Logger.debug(this, "silent" , "sleep ...");
//	} 
//	// 要求重新注册
//	else if (isKiss()) { 
////		// 删除服务器上的密文
////		boolean success = dropSecure();
////		// 注册到上级站点
////		if (success) {
////			success = login();
////		}
////		// 失败，延时再注册！成功，撤销注册！
////		setKiss(success ? false : true);
////		// 重新注册成功，撤销重新注册参数。注册不成功，选择一个最大延时再试。
////		if (success) {
////			setCheckin(false); // 不要重新注册
////			hello(); // 立即发送HELO
////			endtime = nextTouchTime(); // 下次触发时间
////		} else {
////			disableDelay(getDisableTimeout()); // 按照失效时间延时
////			endtime = 0L; // 清零
////		}
//		
//		Logger.error(this, "silent", "我操！去再次注册！");
//		
//		// 删除服务器上的密文
//		dropSecure();
//		// 再次注册
//		boolean success = login();
//		
//		// 失败，延时再注册！成功，撤销注册！
//		setKiss(success ? false : true);
//		// 重新注册成功，撤销重新注册参数。注册不成功，选择一个最大延时再试。
//		if (success) {
//			setCheckin(false); // 不要重新注册
//			hello(); // 立即发送HELO
//			endtime = nextTouchTime(); // 下次触发时间
//		} else {
//			disableDelay(getDisableTimeout()); // 按照失效时间延时
//			endtime = 0L; // 清零
//		}
//	}
//	// 站点失效超时
//	else if (isDisableTimeout()) {
////		// 交给子类处理
////		disableProcess();
////		// 如果子类没有触发，在这里触发
////		if(!isKiss()) kiss();
////		// 延时
////		delay(1000L);
////		endtime = nextTouchTime();
//		
//		Logger.error(this, "silent", "我操！失效超时了！");
//		
//		// 交给子类处理
//		disableProcess();
//		// 要求触发重新注册...
//		kiss(false);
//		
////		// 刷新假的时间，进入下一次...
////		refreshEndTime();
//		
//		// 下一次触发时间...
//		endtime = nextTouchTime();
//	}
//	// 正常超时，或者要求催促握手时...
//	else if (isTouchTimeout(endtime) || isHurry()) {
//		endtime = nextTouchTime(); // 下次触发时间
//		defaultHello(); // 发送HELO命令，保持双方处于连接中
//	} 
//	// 其它，定时休眠...
//	else {
//		resting(endtime); // 定时休眠
//		// 如果要求异步注册时 ...
//		if (isCheckin()) {
//			setCheckin(false);
//			register();
//		}
//	}
//	// 下一次触发时间
//	return endtime;
//}


//
//	/**
//	 * 启动全部管理池
//	 * @param pools 管理池数组
//	 * @return 成功返回真，否则假
//	 */
//	protected boolean startAllPools(VirtualPool[] pools) {
//		int index = 0;
//		while (index < pools.length) {
//			if (index > 0) {
//				delay(500); // 线程延时，防止并发太快
//			}
//			boolean success = pools[index].start();
//			if (!success) {
//				break;
//			}
//			index++;
//		}
//		return index == pools.length;
//	}


//	/**
//	 * 判断操作系统
//	 */
//	private void checkPlatform() {
//		String os = System.getProperty("os.name");
//		if (os == null) {
//			return;
//		}
//
//		if (os.matches("^(.*?)(?i)(LINUX)(.*)$")) {
//			platform = Platform.LINUX;
//		} else if (os.matches("^(.*?)(?i)(WINDOWS)(.*)$")) {
//			platform = Platform.WINDOWS;
//		}
//
//		Logger.debug(this, "checkPlatform", "'%s' is %s", os, platform);
//	}


//	/**
//	 * 选择一个本地地址
//	 * @param wide 首选公网主机地址
//	 * @return 返回Address实例，失败空指针
//	 */
//	private Address select(boolean wide) {
//		InetAddress address = (wide ? Address.getSiteWideAddress(true) : Address.getSiteLocalAddress(true));
//		if (address == null) {
//			address = (wide ? Address.getSiteLocalAddress(true) : Address.getSiteWideAddress(true));
//		}
//		if (address == null) {
//			address = Address.getLoopbackAddress(true);
//		}
//		// 弹出空指针
//		if (address == null) {
//			throw new NullPointerException("cannot be find local address!");
//		}
//		// 返回实例
//		return new Address(address);
//	}

//	/**
//	 * 启动“单模式”网络监听服务。<br><br>
//	 * 
//	 *“单模式”网络监听必须指定一个有效的本地IP地址（除通配符地址外都可以是有效地址）。
//	 * 如果用户指定的是通配符地址或者自回路地址，那么在计算机上选择一个有效的IP地址，
//	 * 地址类型（IPv6/IPv4）对应传入的节点地址类型。虽然IP地址可以改变，但是TCP/UDP的监听端口不能够改变。<br><br>
//	 * 
//	 * 这个方法被“非网关站点”（TOP/HOME/DATA/WORK/BUILD/FRONT/BANK/ACCOUNT/HASH/WATCH/LOG）调用。
//	 * @param clazzs Visit接口集合
//	 * @param local 本地节点的预定义地址，来自“Site.getNode”方法。
//	 * @return 启动成功返回“真”，否则“假”。
//	 */
//	protected boolean loadSingleListen(Class<?>[] clazzs, Node local) {
//		Logger.debug(this, "loadSingleListen", "ready bind %s", local.getHost());
//
//		// 启动网络监听
//		boolean success = loadListen(clazzs, local.getAddress(), local.getTCPort(), local.getUDPort());
//		if (success) {
//			// UDP监听地址
//			SocketHost packet = packetMonitor.getLocal();
//			// TCP监听地址
//			SocketHost stream = streamMonitor.getLocal();
//
//			// 判断绑定的地址一致！
//			success = (Laxkit.compareTo(packet.getAddress(), stream.getAddress()) == 0);
//			// 在启动成功后重新监听节点和站点节点的地址和端口
//			if (success) {
//				Address address = packet.getAddress();
//				local.setHost(address.duplicate(), stream.getPort(), packet.getPort());
//				
////				// 设置隐网地址，如果是通配符，选择本地实际地址
////				if (address.isAnyLocalAddress()) {
////					boolean wide = (local.isFront() ? true : false); // FRONT节点选择公网地址，其他节点在内网运行，选择内网地址
////					address = select(wide);
////					local.setHidden(address.duplicate(), stream.getPort(), packet.getPort());
////				} else {
////					local.setHidden(address.duplicate(), stream.getPort(), packet.getPort());
////				}
//			}
//		}
//
//		Logger.note(this, "loadSingleListen", success, "real bind '%s'", local);
//
//		// 如果定义上级站点，去服务器检测本地当前本地站点地址
//		if (getHub() != null) {
//			// 向服务器发送核对本地IP指令，服务器返回核对后的本地UDP地址，这个核对过的UDP地址网络内有效。
//			SocketHost remote = getHub().getPacketHost();
//			SocketHost reflect = reflect(remote);
//			success = (reflect != null);
//
//			Logger.note(this, "loadSingleListen", success, "current site address %s", reflect);
//
//			// 修改本地的IP地址
//			if (success) {
//				Address address = reflect.getAddress();
//
//				// TCP/UDP服务器本地地址
//				packetMonitor.setDefineHost(reflect);
//				streamMonitor.getDefineHost().setAddress(address);
//				// 异步通信私有地址
//				replySucker.setDefinePrivateIP(address);
//				replyDispatcher.setDefinePrivateIP(address);
//				// 节点本地地址
//				int tcport = streamMonitor.getBindPort();
//				local.setHost(address, tcport, reflect.getPort());
//
//				//	local.setHost(host.getAddress(), streamMonitor.getDefineHost().getPort(), host.getPort());
//			}
//		}
//		
//		return success;
//	}


///**
// * 判断支持Masssive MIMO
// * 说明：除了FRONT/WATCH节点，其它节点都支持Massive MIMO
// * @return 真或者假
// */
//private boolean isSupportMIMO() {
//	boolean success = (SiteTag.isFront(getFamily()) || SiteTag.isWatch(getFamily()));
//	return !success;
//}


///**
//* 判断支持Masssive MIMO <br>
//* 说明：除了FRONT/WATCH节点，三个网关节点（GATE/ENTRANCE/CALL)，其它节点都支持Massive MIMO。<br><br>
//* 
//* 网关节点拒绝使用MIMO的原因：<br>
//* 1. 数据流量不大，不必开辟多通道数据传输。网关主要处理元数据、命令/计算结果数据。<br>
//* 2. 防止黑客通过多个MIMO端口入侵内部网络。<br>
//* 3. 保留一个ReplyDispatcher端口，方便管理员管理。<br>
//* 4. 在NAT网络环境（NAT网络同时存在“公网/内网”和“内网/内网”的可能），
//* 防止ReplyWorker.allTo发送数据给Front节点，因为多MIMO通道出现分发判断错误（比如“内网/内网”的NAT之间会出现判断错误），
//* 索性只使用ReplyDispatcher做为出口拉倒！<br>
//* 
//* @return 真或者假
//*/
//private boolean isSupportMIMO() {
//	// 以下五个节点拒绝启用MIMO通道，其他节点在内网运行，所以可以！
//	switch(getFamily()) {
//	case SiteTag.FRONT_SITE:
//	case SiteTag.WATCH_SITE:
//	case SiteTag.GATE_SITE:
//	case SiteTag.ENTRANCE_SITE:
//	case SiteTag.CALL_SITE:
//		return false;
//	}
//	return true;
//}


///**
// * 失效延时，达到以下条件退出：<br>
// * 1. 收到中断通知 <br>
// * 2. 达到传入的超时时间 <br>
// * 3. 切换到管理节点 <br>
// * 4. 进入自循环状态 <br>
// * 
// * @param timeout 超时时间
// */
//private void disableDelay(long timeout) {
//	// 确定最后时间，循环判断！
//	long lastTime = System.currentTimeMillis() + timeout;
//	do {
//		// 1.收到中断 2. 达到规定时间。退出!
//		if (isInterrupted() || System.currentTimeMillis() >= lastTime) {
//			break;
//		}
//		// 得到外部其它通知：1. 切换管理节点，2. 进入自循环状态。
//		// 发生两种可能，退出循环等待，让SiteLauncher.silent方法自循环
//		boolean success = (isSwitchHub() || (isLogout() && isRoundSuspend()));
//		if (success) break;
//		// 延时500毫秒
//		delay(500);
//	} while(true);
//}

///**
// * 客户端发起RPC连接通知服务器（管理节点），删除服务器上的客户机记录，退出登录状态
// * @param local 本地节点地址
// * @param hub 服务器节点地址（管理节点）
// * @return 成功返回真，否则假
// */
//private boolean disconnect(Node local, Node hub) {
//	HubClient client = fetchHubClient(hub);
//	if (client == null) {
//		Logger.error(this, "disconnect", "cannot be git %s", hub);
//		return false;
//	}
//	boolean success = false;
//	try {
//		success = client.logout(local);
//		client.close(); // 雅致关闭
//	} catch (VisitException e) {
//		Logger.error(e);
//	}
//	// 强制销毁。如果已经关闭，这里不起作用。
//	client.destroy(); 
//	// 返回结果
//	return success;
//}

///**
// * 从上级站点注销自己
// * @param local 本地节点地址
// * @param hub 服务器节点地址（管理节点）
// * @return 成功返回真，否则假
// */
//protected boolean logout(Node local, Node hub) {
//	// 以客户端身份发起RPC连接，通知服务器删除记录，退出登录状态！
//	boolean success = disconnect(local, hub);
//
//	// 注销成功后，退出激活
//	if (success) {
//		// 撤销与服务器的UDP联系，同时删除双方保存的密文（如果有密文的情况下...），HELO/SHINE的定时通信操作停止
//		cancel(hub);
//	}
//
//	Logger.note(this, "logout", success, "logout from %s", hub);
//
//	// 修改登录状态
//	setLogined(!success);
//
//	return success;
//}

//			// <1> 删除服务器上的密文，超时20秒，连续3次。
//			SocketHost remote = getHub().getPacketHost();
//			// 非FRONT节点，撤销密钥；FRONT节点会自行处理
//			boolean on = SiteTag.isFront(getFamily());
//			if (!on) {
//				dropSecure(remote, 20000L, 3);
//			}
//
//			// <2> 无论上述是否成功，都进行再次注册！
//			boolean success = login();
//			// 失败，延时再注册！成功，撤销注册！
//			setKiss(success ? false : true);
//
//			// <3> 重新注册成功，撤销重新注册参数。注册不成功，选择一个最大延时再试。
//			if (success) {
//				setCheckin(false); // 不要重新注册
//				refreshEndTime(); // 刷新时间
//				hello(); // 立即发送HELO
//			} else {
//				disableDelay(remote, getDisableTimeout()); // 按照失效时间延时
//			}
//			endtime = nextTouchTime(); // 下次触发时间


//		// 其它，定时休眠...
//		else {
//			resting(endtime); // 定时休眠
//			// 如果要求异步注册时 ...
//			if (isCheckin()) {
//				setCheckin(false);
//				register();
//			}
//		}

	//			// 达到延时注册时间后，重新注册到上级站点
	//			if (registerTimer.isTouch()) {
	//				// 重新注册
	//				register();
	//				// 刷新
	//				registerTimer.refresh();
	//			}


//// 从指定的站点退出（注销）
//boolean success = logout();
//if (!success) {
//	cancel(origin);
//}		


///**
// * 撤销与服务器的UDP通信
// * @param hub 服务器节点地址
// * @return 发送成功返回真，否则假
// */
//protected boolean cancel(Node hub) {
//	boolean success = (hub != null);
//	if (success) {
//		SocketHost endpoint = hub.getPacketHost();
//		success = cancel(endpoint);
//	}
//	return success;
//}
//
///**
// * 撤销与服务器的UDP通信
// * @param endpoint 目标站点地址，发送成功后，删除本地密文
// * @return 发送成功返回真，否则假。
// */
//protected boolean cancel(SocketHost endpoint) {
//	Mark mark = new Mark(Ask.NOTIFY, Ask.EXIT);
//	Packet packet = new Packet(endpoint, mark);
//	// 单向处理，服务器接收后，不用返回应答数据包
//	packet.addMessage(MessageKey.DIRECT_NOTIFY, true);
//	// 向目标节点发送数据包
//	boolean success = sendPacket(packet);
//
//	Logger.note(this, "cancel", success, "to %s", endpoint);
//
//	// 如果发送成功，删除本地密文（如果有密文时）
//	if (success) {
//		removeCipher(endpoint);
//	}
//	return success;
//}


///**
// * 解析FIXP服务器网络通信安全配置
// * 将解析的RSA密钥保存到内在中，用于网络通信时的数据加密和解密。
// * 
// * @param document XML文档
// * @return 成功返回真，否则假
// */
//protected boolean loadSecure(Document document) {
//
//	// 解析安全配置文件
//	String filename = XMLocal.getXMLValue(document.getElementsByTagName(OtherMark.SECURITY_NETWORK));
//	filename = ConfigParser.splitPath(filename);
//
//	boolean success = (filename != null && filename.length() > 0);
//	// 解析和加载密钥令牌
//	if (success) {
//		SecureTokenParser parser = new SecureTokenParser();
//		success = parser.split(filename);
//	}
//
//	Logger.note(this, "loadSecure", success, "load key tokens");
//	return success;
//}


///**
// * 解析FIXP服务器网络通信安全配置
// * 将解析的RSA密钥保存到内存，用于网络通信时的数据加密和解密。
// * 
// * @param document XML文档
// * @return 成功返回真，否则假
// */
//protected boolean loadSecure(Document document) {
//	// 1. 先取参数
//	String bin = System.getProperty("user.dir");
//	bin += "/../conf/security.xml";
//	File file = new File(bin);
//	// 2. 如果默认安全配置不存在，忽略它
//	if (!file.exists()) {
//		// 解析安全配置文件
//		String filename = XMLocal.getXMLValue(document.getElementsByTagName(OtherMark.SECURITY_NETWORK));
//		filename = ConfigParser.splitPath(filename);
//		// 判断有效
//		boolean success = (filename != null && filename.length() > 0);
//		if (success) {
//			file = new File(filename);
//		}
//	}
//	if (!file.exists()) {
//		Logger.error(this, "loadSecure", "cannot be load security configure!");
//		return false;
//	}
//	
//	// 解析密钥令牌
//	SecureTokenParser parser = new SecureTokenParser();
//	boolean success = parser.split(file);
//
//	Logger.note(this, "loadSecure", success, "load key tokens");
//	return success;
//}

