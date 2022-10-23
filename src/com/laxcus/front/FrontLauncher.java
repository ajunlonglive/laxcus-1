/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

import java.io.*;

import com.laxcus.access.parse.*;
import com.laxcus.command.site.front.*;
import com.laxcus.fixp.client.*;
import com.laxcus.fixp.monitor.*;
import com.laxcus.fixp.reply.*;
import com.laxcus.fixp.secure.*;
import com.laxcus.front.pool.*;
import com.laxcus.front.tub.*;
import com.laxcus.front.tub.mission.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.mission.*;
import com.laxcus.remote.client.*;
import com.laxcus.remote.client.hub.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.thread.*;
import com.laxcus.tub.method.*;
import com.laxcus.tub.monitor.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.net.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hub.*;
import com.laxcus.visit.impl.front.*;
import com.laxcus.xml.*;

/**
 * 前端站点启动器。<br>
 * 这是一个基础类，它的子类包括图形前端、字符前端、驱动程序前端（绑定到第三方）。<br><br>
 * 
 * FRONT首先登录到ENTRANCE站点，通过ENTRANCE重定向，把每个FRONT站点分配到它实际需要登录的GATE站点。
 * 
 * FRONT登录隐性规定：<br>
 * 每个LAXCUS站点都有TCP/UDP两个端口。FRONT登录ENTRANCE时，只定义ENTRANCE一个端口，这样就要求ENTRANCE的TCP/UDP端口相同。<br>
 * 
 * @author scott.liang
 * @version 1.12 11/23/2015
 * @since laxcus 1.0
 */
public abstract class FrontLauncher extends SlaveLauncher implements UserListener, TaskListener, TipPrinter {

	/** 前端站点配置 **/
	protected FrontSite local = new FrontSite();

	/** FRONT初始登录的ENTRANCE节点 **/
	private Node initHub;

	/** TUB数据流监听服务器 **/
	protected TubStreamMonitor tubMonitor = new TubStreamMonitor();

	/** 数据处理模式。默认采用磁盘为做中间存取，内存模式是假。 **/
	private boolean memory;

	/** 命令超时时间 **/
	private long commandTimeout;

	/** 消息提示池 **/
	private TipLoader messages = new TipLoader();

	/** 故障提示池 **/
	private TipLoader faults = new TipLoader();

	/** 警告提示池 **/
	private TipLoader warnings = new TipLoader();

	/** 回显码 **/
	private TipLoader echos = new TipLoader();

	/** 连接中断后，GATE站点的重试间隔时间 **/
	private long autoRetryInterval;

	/** 执行"pitch"由网关(entrance/gate)返回的front节点出口地址 **/
	private SocketHost doorHost;
	
	/** 注册成功时间 **/
	private volatile long loginTime;

	/**
	 * 构造一个FRONT站点启动器，指定它的参数
	 * @param rank FRONT节点类型
	 * @param exitVM 退出JAVA虚拟机
	 * @param printFault 出错退出打印日志
	 * @param printer 日志打印器
	 */
	protected FrontLauncher(byte rank, boolean exitVM, boolean printFault, LogPrinter printer) {
		super(printer);
		
		loginTime = 0;
		
		// 节点类型
		local.setRank(rank);
		
		// 在退出时关闭JAVA虚拟机
		setExitVM(exitVM);
		// 出错退出打印日志
		setPrintFault(printFault);
		// 前端监听
		setStreamInvoker(new FrontStreamAdapter());
		setPacketInvoker(new FrontPacketAdapter(this));

		// 远程访问控测接口
		SyntaxParser.setVisitRobot(getStaffPool());
		// 给语法解析器分配资源检索接口。
		SyntaxParser.setResourceChooser(getStaffPool());

		// 提示打印接口
		SyntaxParser.setTipPrinter(this);

		// 默认是磁盘处理模式（速度慢，安全性高）！
		setMemory(false);
		// 默认无限制超时
		setCommandTimeout(-1L);

		// 设置容器句柄
		Mission.setFrontLauncher(this);
		TubPool.getInstance().setHelper(getStaffPool());
		TubServlet.setChannel(new TubChannelDispatcher(this));
		((FrontInvokerPool) getInvokerPool()).setTubMissionCreator(new TubMissionCreator());

		// 运行器
		TubCommandRunner.setLauncher(this);

		// 用户接口
		PutTaskPool.getInstance().setUserListener(this);
		NearTaskPool.getInstance().setUserListener(this);
		EndTaskPool.getInstance().setUserListener(this);
		GuideTaskPool.getInstance().setUserListener(this);
		
		// 最大两分钟
		setAutoRetryInterval(120000);
		
		// 设置散列码
		local.setHash(ClassCodeCreator.create(this));
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#isWatch()
	 */
	@Override
	public boolean isWatch() {
		return false;
	}

	/**
	 * 返回边缘容器服务器监听地址
	 * @return ScoketHost实例
	 */
	public SocketHost getTubHost() {
		return tubMonitor.getLocal();
	}

//	/**
//	 * 加载多语言的提示文本
//	 * @return 成功返回真，否则假
//	 */
//	protected boolean loadTips() {
//		TipSelector selector = new TipSelector("conf/front/tip/config.xml");
//		try {
//			String jpath = selector.getMessagePath();
//			messages.loadXMLFromJar(jpath);
//
//			jpath = selector.getFaultPath();
//			faults.loadXMLFromJar(jpath);
//
//			jpath = selector.getWarningPath();
//			warnings.loadXMLFromJar(jpath);
//
//			jpath = selector.getEchoPath();
//			echos.loadXMLFromJar(jpath);
//			return true;
//		} catch (IOException e) {
//			e.printStackTrace();
//			Logger.error(e);
//		}
//		return false;
//	}

	/**
	 * 加载多语言的提示文本
	 * @return 成功返回真，否则假
	 */
	protected boolean loadTips(String xmlPath) {
		TipSelector selector = new TipSelector(xmlPath);
		try {
			String jpath = selector.getMessagePath();
			messages.loadXMLFromJar(jpath);

			jpath = selector.getFaultPath();
			faults.loadXMLFromJar(jpath);

			jpath = selector.getWarningPath();
			warnings.loadXMLFromJar(jpath);

			jpath = selector.getEchoPath();
			echos.loadXMLFromJar(jpath);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			Logger.error(e);
		}
		return false;
	}

	/**
	 * 加载多语文的提示文本
	 * @return 成功返回真，否则假
	 */
	protected boolean loadTips() {
		return loadTips("conf/front/tip/config.xml");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String message(int no) {
		return messages.format(no);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int, java.lang.Object[])
	 */
	@Override
	public String message(int no, Object... params) {
		return messages.format(no, params);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String warning(int no) {
		return warnings.format(no);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String warning(int no, Object... params) {
		return warnings.format(no, params);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String fault(int no) {
		return faults.format(no);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#message(int)
	 */
	@Override
	public String fault(int no, Object... params) {
		return faults.format(no, params);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#echo(int)
	 */
	@Override
	public String echo(int no) {
		return echos.format(no);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#echo(int, java.lang.Object[])
	 */
	@Override
	public String echo(int no, Object... params) {
		return echos.format(no, params);
	}

	/**
	 * 设置初始登录站点
	 * @param e 节点实例
	 */
	public void setInitHub(Node e) {
		initHub = e;
	}

	/**
	 * 设置初始登录站点
	 * @return 节点实例
	 */
	public Node getInitHub() {
		return initHub;
	}
	
	/**
	 * 返回ENTRANCE节点实例
	 * @see com.laxcus.launch.SiteLauncher#getRootHub()
	 */
	@Override
	public Node getRootHub() {
		return initHub;
	}

	/**
	 * 设置由网关节点定义的FRONT节点出口地址
	 * @param e 主机地址
	 */
	private void setDoorHost(SocketHost e) {
		if (e == null) {
			doorHost = null;
		} else {
			doorHost = e.duplicate();
		}
	}

	/**
	 * 返回网关节点（ENTRANCE/GATE）定义的FRONT节点出口地址
	 * @return
	 */
	public SocketHost getDoorHost() {
		if (doorHost == null) {
			return null;
		}
		return doorHost.duplicate();
	}

	/**
	 * 判断front的出口是公网IP
	 * @return 返回真或者假
	 */
	public boolean isWideAddress() {
		return (doorHost != null && doorHost.getAddress().isWideAddress());
	}

	/**
	 * 设置内存处理模式
	 * @param b 内存模式
	 */
	public void setMemory(boolean b) {
		memory = b;
	}

	/**
	 * 设置内存处理模式
	 * @param input 输入语句
	 */
	public void setMemory(String input) {
		boolean b = (input != null && input.matches("^\\s*(?i)(?:MEMORY)\\s*$"));
		setMemory(b);
	}

	/**
	 * 判断是内存处理模式
	 * @return 返回真或者假
	 */
	public boolean isMemory() {
		return memory;
	}

	/**
	 * 判断是磁盘处理模式
	 * @return 返回真或者假
	 */
	public boolean isDisk() {
		return !memory;
	}

	/**
	 * 设置命令超时，单位：毫秒
	 * 
	 * @param ms 毫秒
	 */
	public void setCommandTimeout(long ms) {
		commandTimeout = ms;
	}

	/**
	 * 设置命令超时时间，单位：毫秒。
	 * @param input 输入语句
	 */
	public void setCommandTimeout(String input) {
		long ms = ConfigParser.splitTime(input, -1);
		if (ms > 0) {
			setCommandTimeout(ms);
		}
	}

	/**
	 * 返回命令超时
	 * 
	 * @return 超时时间
	 */
	public long getCommandTimeout() {
		return commandTimeout;
	}

	/**
	 * 设置内网检测超时
	 * @param input 输入语句
	 */
	public void setPockTimeout(String input) {
		// 默认是10秒
		long ms = ConfigParser.splitTime(input, 10000);
		getReplyHelper().setPockTimeout(ms);
		getPacketHelper().setPockTimeout(ms);
	}

	/**
	 * 返回内网检测超时
	 * @return 以毫秒为单位的数值
	 */
	public long getShineTimeout() {
		return getPacketHelper().getPockTimeout();
	}

	/**
	 * 返回FRONT站点
	 * @see com.laxcus.launch.SiteLauncher#getSite()
	 */
	@Override
	public FrontSite getSite() {
		return local;
	}

	/**
	 * 返回注册用户名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return local.getUsername();
	}

	/**
	 * 返回当前的用户签名。如果是管理员或者离线状态，返回空指针，否则是SHA256签名
	 * @return Siger实例或者空指针
	 */
	@Override
	public Siger getIssuer() {
		if (local.isOffline() || local.isAdministrator()) {
			return null;
		} else {
			return local.getUsername();
		}
	}

	/**
	 * 判断是离线状态。
	 * 
	 * @return 返回真或者假
	 */
	
	/*
	 * 判断是离线状态
	 * @see com.laxcus.task.UserListener#isOffline()
	 */
	@Override
	public boolean isOffline() {
		return local.isOffline();
	}

	/**
	 * 判断是管理员状态并且已经登录成功。
	 * 
	 * @return 返回真或者假
	 */
	@Override
	public boolean isAdministrator() {
		return local.isAdministrator();
	}

	/**
	 * 判断是普通用户并且已经登录成功。
	 * 
	 * @return 返回真或者假
	 */
	@Override
	public boolean isUser() {
		return local.isUser();
	}

	/**
	 * 返回账号等级
	 * @return 账号等级
	 */
	public int getGrade() {
		return local.getGrade();
	}

	/**
	 * 设置连网失败后，自动重新登录的最大间隔时间。
	 * 时间值分别来自ENTRANCE / GATE / CALL三类节点，以它们中间的最大为准。
	 * @param max 最大时间
	 */
	public long setAutoRetryInterval(long max) {
		if (max > 0) {
			max = Math.max(max, autoRetryInterval);
			// 最大1分钟
			if(max > Laxkit.MINUTE) {
				autoRetryInterval = Laxkit.MINUTE;
			} else {
				autoRetryInterval = max;
			}
		}
		return autoRetryInterval;
	}

	/**
	 * 返回连网失败后，FRONT节点自动重新登录的最大间隔时间
	 * @return 以毫秒为间隔的时间
	 */
	public long getAutoRetryInterval() {
		return autoRetryInterval;
	}

	/**
	 * 启动边缘容器/FIXP监听，包括RPC、包、流三种模式
	 * @return
	 */
	protected boolean loadListen() {
		boolean success = loadTubListen();
		if (!success) {
			return false;
		}

		// 设置命令管理池
		CommandVisitOnFront.setCommandPool((FrontCommandPool) getCommandPool());
		// 启动本地监听
		Class<?>[] clazzs = { CommandVisitOnFront.class };
		success = loadSingleListen(clazzs, local.getNode());

		Logger.note(this, "loadListen", success, "bind to %s", local.getNode());

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#stopListen()
	 */
	@Override
	protected void stopListen() {
		// 关闭边缘服务器
		stopTubListen();
		// 调用父类的服务器
		super.stopListen();
	}

	/**
	 * 停止边缘容器服务器
	 */
	private void stopTubListen() {
		ThreadStick stick = new ThreadStick();
		// 判断运行中
		boolean success = tubMonitor.isRunning();
		if (success) {
			tubMonitor.stop(stick);

			do {
				if (stick.isOkay()) break;
				delay(200);
			} while (true);
		}
		// 关闭SOCKET
		if (tubMonitor.isBound()) {
			tubMonitor.close();
		}
	}

	/**
	 * 启动边缘容器服务器监听
	 * @param host 本地SOCKET地址
	 * @return 成功返回真，否则假
	 */
	private boolean loadTubListen() {
		SocketHost host = local.getTub();
		if (host == null) {
			host = new SocketHost(SocketTag.TCP);
		}

		// 绑定本地监听地址
		boolean success = tubMonitor.bind(host);
		if (!success) {
			Logger.error(this, "loadTubListen", "cannot be load tub monitor! %s", host);
			return false;
		}

		// RPC调用接口
		Class<?>[] clazzs = { TubVisitOnFront.class };
		TubVisitAdapter adapter = new TubVisitAdapter();
		for (int i = 0; clazzs != null && i < clazzs.length; i++) {
			success = adapter.addTubVisit(clazzs[i]);
			Logger.note(this, "loadTubListen", success, "load %s", clazzs[i].getName());
			if (!success) {
				stopTubListen();
				return false;
			}
		}
		// 设置RPC调用接口
		tubMonitor.setVisitInvoker(adapter);
		// 方法调用器
		tubMonitor.setMethodInvoker(new TubMethodAdapter());

		// 启动线程
		success = tubMonitor.start();
		if (success) {
			do {
				delay(200);
			} while (!tubMonitor.isRunning());

			// 设置监听地址
			host = tubMonitor.getBindHost();
			local.setTub(host);
		}

		Logger.note(this, "loadTubListen", success, "bind to %s", host);

		return success;
	}

	/**
	 * 判断当前是影子站点。
	 * 即实际的FixpPacketMonitor和ReplySucker的监听地址/端口，和服务器端反馈来的地址不一致
	 * 
	 * @return 返回真或者假
	 */
	public boolean isShadow() {
		SocketHost local = packetMonitor.getLocal();
		Address address = (local != null ? local.getAddress() : null);
		// 如果地址有效且不存在，即是影子站点
		return address != null && !Address.contains(address);
	}

	/**
	 * 登录到ENTRANCE站点
	 * @param hub ENTRANCE站点地址
	 * @param client 客户端
	 * @return 返回重定向的GATE站点地址
	 * @throws VisitException
	 */
	private EntryStatus doEntrance(SiteHost hub) {
		// 以UDP方式连接到ENTRANCE节点
		FrontClient client = ClientCreator.createFrontClient(hub, false);
		// 连接不成功，退出！
		if (client == null) {
			Logger.error(this, "doEntrance", "cannot be find %s", hub);
			return new EntryStatus(FrontEntryFlag.NETWORK_FAULT);
		}

		Logger.debug(this, "doEntrance", "socket timeout %d ms", client.getReceiveTimeout());

		// 重定向站点
		SiteHost redirect = null;

		int who = FrontEntryFlag.ENTRANCE_FAULT;
		try {
			Node server = null;
			// 1. 获得注册站点的类型，必须是ENTRANCE站点。
			byte family = client.getHubFamily();
			boolean success = SiteTag.isEntrance(family);
			if (success) {
				server = new Node(family, hub.duplicate());
				setInitHub(server);
			}

			// 2. 判断FRONT节点可能在内网！
			if (success) {
				SocketHost dispatcher = client.getHubDispatcher(isWideAddress());
				success = (dispatcher != null); // 判断有效
				
				Logger.note(this, "doEntrance", success, "Entrance Server %s, Entrance ReplyDispatcher %s", server, dispatcher);
				if (success) {
					success = decideEntrance(server, dispatcher);
					// 出错，是NAT设备检测问题
					if (!success) {
						who = FrontEntryFlag.NAT_FAULT;
					}
				}
			}

			//3. 比较版本
			if (success) {
				Version other = client.getVersion();
				success = (Laxkit.compareTo(getVersion(), other) == 0);
				Logger.note(this, "doEntrance", success, "check version \"%s\" -> \"%s\"", getVersion(), other);
				if (!success) {
					who = FrontEntryFlag.VERSION_NOTMATCH;
				}
			}

			//4. 取站点激活超时时间！
			if (success) {
				long timeout = client.getTimeout();
				success = (timeout > 0);
				setSiteTimeoutMillis(timeout);
			}

			// 5. 取在线延时时间，单位：毫秒
			long lingerTimeout = 0;
			if (success) {
				lingerTimeout = client.getLingerTimeout();
				success = (lingerTimeout > 0);
			}

			// 6. 取重试最大时间
			if (success) {
				long max = client.getAutoReloginInterval();
				setAutoRetryInterval(max);
				Logger.info(this, "doEntrance", "auto relogin interval: %d -> %d ms", max, getAutoRetryInterval());
			}

			long endTime = System.currentTimeMillis() + SocketTransfer.getDefaultConnectTimeout();

			// 7. 注册，循环等待
			while (success) {
				// 重新注册
				FrontReport report = client.login(local);

				if (report.isLinger()) {
					// 达到最大连接延时
					if (System.currentTimeMillis() >= endTime) {
						who = FrontEntryFlag.LOGIN_TIMEOUT;
						break;
					}
					// 延时
					delay(lingerTimeout);
				} else if (report.isRedirect()) { // 发生重定向
					redirect = report.getRedirect().getHost(); // 获得重定向站点地址
					who = (redirect != null ? FrontEntryFlag.SUCCESSFUL : FrontEntryFlag.CANNOT_REDIRECT);
					break;
				} else {
					who = FrontEntryFlag.CANNOT_REDIRECT;
					break; // 其他情况，失败！
				}
			}
			// 关闭主机
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
			who = FrontEntryFlag.ENTRANCE_FAULT;
		} catch (Throwable e) {
			Logger.fatal(e);
			who = FrontEntryFlag.ENTRANCE_FAULT;
		}

		// 销毁
		client.destroy();

		if (redirect != null) {
			Logger.info(this, "doEntrance", "redirect to: %s", redirect);
		}

		Logger.info(this, "doEntrance", "Device Environment: \'%s\' Network!", (isPock() ? "NAT" : "Single"));

		return new EntryStatus(who, redirect);
	}

	/**
	 * 采用TCP通信，检测服务器。主要是确认服务器TCP存在且有效
	 * @param remote 目标地址
	 * @param timeout 超时时间
	 * @return 成功返回真，否则假
	 */
	private boolean checkStreamHub(SocketHost remote, int timeout) {
		SocketHost host = null;
		FixpStreamClient client = new FixpStreamClient();
		try {
			client.setConnectTimeout(timeout);
			client.setReceiveTimeout(timeout);
			// 连接
			client.connect(remote);
			// 发送数据包
			host = client.test();
			// 关闭socket
			client.close();
		} catch (IOException e) {
			Logger.error(e);
		} catch(Throwable e) {
			Logger.fatal(e);
		}
		// 销毁
		client.destroy();

		// 判断成功!
		boolean success = (host != null);

		Logger.note(this, "checkStreamHub", success, "check %s, local is %s, timeout %d ms", remote, host, timeout);

		return success;
	}

	/**
	 * 采用UDP通信，检测服务器。主要是确认服务存在且有效
	 * @param remote 目标地址
	 * @param timeout 超时时间，单位：毫秒
	 * @return 成功返回真，否则假
	 */
	private boolean checkPacketHub(SocketHost remote, int timeout) {
		FixpPacketClient client = new FixpPacketClient();
		client.setReceiveTimeout(timeout);
		client.setConnectTimeout(timeout);

		SocketHost host = null;
		try {
			// 1. 以通配符绑定本地任意端口
			boolean success = client.bind();
			if (success) {
				// 2. 询问服务器安全状态
				int type = client.askSecure(remote);
				// 3. 判断是否加密
				boolean secure = SecureType.isCipher(type);
				// 4. 以服务器规定方式，选择加密/非加密，检测服务器
				host = client.test(remote, secure);
			}
			// 关闭SOCKET
			client.close();
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		// 销毁
		client.destroy();

		// 判断成功!
		boolean success = (host != null);

		Logger.note(this, "checkPacketHub", success, "check %s, local is %s, timeout %d ms", remote, host, timeout);

		return success;
	}

	/**
	 * 检测服务器
	 * @param hub 服务器主机
	 * @param count 检测次数
	 * @return 成功返回真，否则假
	 */
	private boolean checkHub(SiteHost hub, int count) {
		if (count < 1) {
			count = 1;
		}
		// 信道超时
		int timeout = SocketTransfer.getDefaultChannelTimeout();

		// 1. 首先检测TCP模式
		boolean check = checkStreamHub(hub.getStreamHost(), timeout);
		if (check) {
			return true;
		}
		// 2. 不成功，检测UDP模式
		// 发包分时
		int subTimeout = timeout / count;
		if (subTimeout < 20000) {
			subTimeout = 20000; // 最少20秒
		}
		// 连续检测，直到最后
		for (int i = 0; i < count; i++) {
			boolean success = checkPacketHub(hub.getPacketHost(), subTimeout);
			if (success) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 定位FRONT站点主机地址
	 * @param hub ENTRANCE站点地址
	 * @return 成功返回真，否则假
	 */
	private int pitch(SocketHost hub) {
		// 向ENTRANCE FIXP UDP服务器求证自己的实际IP地址
		SocketHost reflect = reflect(hub);
		// 失败，返回没有找到提示
		if (reflect == null) {
			Logger.error(this, "pitch", "cannot be reflect! %s", hub);
			return FrontPitch.NOT_FOUND; // 没有找到服务器
		}

		// 设置
		setDoorHost(reflect);

		Logger.info(this, "pitch", "door host %s", reflect);

		// 取出绑定的主机地址
		SocketHost host = packetMonitor.getBindHost();
		// 服务端返回的地址
		Address address = reflect.getAddress();

		// 可能条件：
		// 1. 本地不包含这个地址，那一定是NAT地址，要修改
		// 2. 是通配符地址，要修改
		boolean exists = Address.contains(address);
		boolean nat = (!exists || host.getAddress().isAnyLocalAddress());
		// 成功，修改FRONT站点地址！
		// FIXP TCP 监听服务器忽略，所有服务全部从UDP进出！
		if (nat) {
			// 定义监听地址
			packetMonitor.setDefineHost(reflect);
			streamMonitor.getDefineHost().setAddress(address);
			// 私有主机地址
			replySucker.setDefinePrivateIP(address);
			replyDispatcher.setDefinePrivateIP(address);
			// 站点地址
			local.getHost().setAddress(address);
			local.getHost().setUDPort(reflect.getPort());
		}
		// 地址存在，但是reflect与本地地址不一致，也要修改
		else if (exists && Laxkit.compareTo(address, host.getAddress()) != 0) {
			// 定义监听地址
			packetMonitor.setDefineHost(reflect);
			streamMonitor.getDefineHost().setAddress(address);
			// 私有主机地址
			replySucker.setDefinePrivateIP(address);
			replyDispatcher.setDefinePrivateIP(address);
			// 站点地址
			local.getHost().setAddress(address);
			local.getHost().setUDPort(reflect.getPort());
		}

		Logger.info(this, "pitch", "pitch to %s, local %s", hub, local);

		return FrontPitch.SUCCESSFUL;
	}
	
	/**
	 * 释放服务器资源。
	 * 这个方法在"pitch"方法之前进行。
	 * 
	 * @param hub 服务器主机地址
	 * @return 删除成功返回真，否则假
	 */
	private boolean release(SiteHost hub) {
		// 以UDP方式连接到GATE节点
		FrontClient client = ClientCreator.createFrontClient(hub, false);
		// 连接不成功，退出！
		if (client == null) {
			Logger.error(this, "release", "cannot be find:%s", hub);
			return false;
		}
		
		boolean success = false;
		try {
			success = client.release(local);
			// 关闭socket
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.fatal(e);
		}
		// 销毁
		client.destroy();
		
		Logger.debug(this, "release", success, "drop history, from %s", hub);
		
		// 删除与服务器关联的密钥！
		removeCipher(hub.getPacketHost());
		
		return success;
	}

	/**
	 * 定位主机
	 * @param hub 服务端节点地址
	 * @param tracker 追踪器
	 * @return 成功返回真，否则假
	 */
	private boolean pitch(SiteHost hub, FrontLoginTracker tracker) {
		int pitchId = pitch(hub.getPacketHost());
		// 有效时...
		if (tracker != null) {
			tracker.setPitchId(pitchId);
			tracker.setPitchHub(hub);
		}
		// 判断成功
		return (pitchId == FrontPitch.SUCCESSFUL);
	}

	/**
	 * 通过ENTRANCE站点确定FRONT站点的出口地址
	 * 
	 * @param server ETRANCE服务器节点地址
	 * @param dispatcher ENTRANCE站点的ReplyDispatcher公网地址
	 */
	private boolean decideEntrance(Node server, SocketHost dispatcher) {
		SocketHost remote = server.getPacketHost();
		ReplyHelper replyHelper = getReplyHelper();
		FixpPacketHelper packetHelper = getPacketHelper();

		int timeout = SocketTransfer.getDefaultChannelTimeout();

		// FixpPacketMonitor向ENTRANCE站点的FixpPacketMonitor站点询问自己内网的出口IP地址和端口
		SocketHost localNAT = packetHelper.checkPock(remote, timeout, 3);
		// ReplySucker向ENTRANCE站点的ReplyDispatcher询问自己内网的出口IP地址和端口
		SocketHost localSuckerNAT = replyHelper.checkPock(dispatcher, timeout, 3);

		Logger.note(this, "decideEntrance", localNAT!=null, "Front FixpPacketMonitor NAT %s -> Entrance FixpPacketMonitor %s ", localNAT, remote);
		Logger.note(this, "decideEntrance", localSuckerNAT!=null, "Front ReplySucker NAT %s -> Entrance ReplyDispatcher %s", localSuckerNAT, dispatcher);

		// 出错
		if (localNAT == null || localSuckerNAT == null) {
			Logger.error(this, "decideEntrance", "cannot be git NAT address!");
			return false;
		}

		// 如果ENTRANCE服务器反馈的地址存在于系统地址集，这是一个真实站点，后续不需要处理了
		if (Address.contains(localNAT.getAddress())) {
			Logger.info(this, "decideEntrance", "匹配！地址集包含地址 %s", localNAT);

			// 非NAT内网环境，通知ENTRANCE节点，销毁密钥，退出！
			boolean success = dropSecure(remote, 20000L, 1);
			Logger.note(this, "decideEntrance", success, "drop cipher, from %s", remote);	
			return true; // 调试完成后恢复这一行！
		}

		// FixpPacketMonitor启动定时定位命令
		packetHelper.addPock(server, remote, localNAT);
		// ReplySucker启动定时定位命令
		replyHelper.addPock(server, dispatcher, localSuckerNAT);
		
//		// 保存Entrance ReplyDispatcher地址
//		entranceDispatcher = dispatcher;

		return true;
	}

	/**
	 * 通过GATE站点确定FRONT站点的出口地址
	 * @param server GATE节点地址
	 * @param dispatcher GATE站点的ReplyDispatcher公网地址
	 * @return 成功返回真，否则假
	 */
	private boolean decideGate(Node server, SocketHost dispatcher) {
		SocketHost remote = server.getPacketHost();
		FixpPacketHelper packetHelper = getPacketHelper();
		ReplyHelper replyHelper = getReplyHelper();

		long timeout = SocketTransfer.getDefaultChannelTimeout();

		// FixpPacketMonitor向GATE站点询问自己内网的出口IP地址和端口
		SocketHost localNAT = packetHelper.checkPock(remote, timeout, 3);
		// ReplySucker向GATE站点询问自己内网的出口IP地址和端口
		SocketHost localSuckerNAT = replyHelper.checkPock(dispatcher, timeout, 3);

		Logger.note(this, "decideGate", localNAT != null, "Front FixpPacketMonitor NAT %s -> Gate FixpPacketMonitor %s ", localNAT, remote);
		Logger.note(this, "decideGate", localSuckerNAT != null, "Front ReplySucker NAT %s -> Gate ReplyDispatcher %s ", localSuckerNAT, dispatcher);

		// 出错
		if (localNAT == null || localSuckerNAT == null) {
			Logger.error(this, "decideGate", "cannot be git nat address!");
			return false;
		}

		// 出口地址不匹配，是当前计算机环境问题，但是不影响运行！
		if (Laxkit.compareTo(localNAT.getAddress(), localSuckerNAT.getAddress()) != 0) {
			Logger.warning(this, "decideGate",
					"not match! current fixp packet monitor %s , current reply monitor %s", localNAT, localSuckerNAT);
		}

		// 如果GATE服务器反馈的地址存在于系统地址集，这是一个真实站点，后续不需要处理了
		if (Address.contains(localNAT.getAddress())) {
			Logger.info(this, "decideGate", "匹配！地址集包含地址 %s", localNAT);
			return true; // 调试完成后恢复这一行
		}

		/** 修改当前节点参数，以FixpPacketMonitor返回的NAT参数为准 **/

		Address address = localNAT.getAddress();
		// UDP/TCP服务器的定义主机
		packetMonitor.setDefineHost(localNAT);
		streamMonitor.getDefineHost().setAddress(address);
		// 修改参数
		local.getHost().setAddress(address);
		local.getHost().setUDPort(localNAT.getPort());

		// 异步通信服务器的内网IP地址，以ReplySucker检测的地址为准
		replySucker.setDefinePrivateIP(localSuckerNAT.getAddress());
		replyDispatcher.setDefinePrivateIP(localSuckerNAT.getAddress());

		// FixpPacketMonitor启动定时定位命令
		packetHelper.addPock(server, remote, localNAT);
		// ReplySucker启动定时定位命令
		replyHelper.addPock(server, dispatcher, localSuckerNAT);

		//		// 保存Gate ReplyDispatcher地址
		//		gateDispatcher = dispatcher;

		return true;
	}

	/**
	 * 登录GATE站点
	 * @param hub GATE站点
	 * @param client 客户机
	 * @return 成功返回真，否则假
	 * @throws VisitException
	 */
	private EntryStatus doGate(SiteHost hub) {
		// 以UDP方式连接到GATE节点
		FrontClient client = ClientCreator.createFrontClient(hub, false);
		// 连接不成功，退出！
		if (client == null) {
			Logger.error(this, "doGate", "cannot be find:%s", hub);
			return new EntryStatus(FrontEntryFlag.NETWORK_FAULT);
		}

		Logger.debug(this, "doGate", "receive timeout %d ms", client.getReceiveTimeout());

		int who = FrontEntryFlag.GATE_FAULT;
		try {
			// 1. 获得注册站点的类型，注册到GATE站点。
			byte family = client.getHubFamily();
			boolean success = SiteTag.isGate(family);
			// 2. 根据Front所处位置（公网/内网），取出GATE站点的ReplySucker对应地址
			if (success) {
				Node server = new Node(SiteTag.GATE_SITE, hub); //GATE节点地址
				
				SocketHost dispatcher = client.getHubDispatcher(isWideAddress());
				success = (dispatcher != null); // 判断有效！
				
				Logger.note(this, "doGate", success, "Gate Server %s, Gate ReplyDispatcher %s", server, dispatcher);
				// 通过GATE站点，定位FRONT站点地址
				if (success) {
					success = decideGate(server, dispatcher);
					// 出错，是NAT设备检测问题
					if (!success) {
						who = FrontEntryFlag.NAT_FAULT;
					}
				}
			}

			// 2. 比较版本
			if (success) {
				Version other = client.getVersion();
				success = (Laxkit.compareTo(getVersion(), other) == 0);
				Logger.note(this, "doGate", success, "check version \"%s\" -> \"%s\"", getVersion(), other);
				if (!success) {
					who = FrontEntryFlag.VERSION_NOTMATCH;
				}
			}

			// 3. 取站点激活超时时间
			if (success) {
				long timeout = client.getTimeout();
				success = (timeout > 0);
				setSiteTimeoutMillis(timeout);
			}

			// 4. 取在线延时时间，时间单位：毫秒
			long lingerTimeout = 0;
			if (success) {
				lingerTimeout = client.getLingerTimeout();
				success = (lingerTimeout > 0);
			}

			// 5. 取出自动重新注册的时间
			if (success) {
				long max = client.getAutoReloginInterval();
				setAutoRetryInterval(max);
				Logger.info(this, "doGate", "auto relogin interval: %d -> %d ms", max, getAutoRetryInterval());
			}

			// 等待2分钟超时
			long endTime = System.currentTimeMillis() + SocketTransfer.getDefaultConnectTimeout();

			// 6. 注册，循环等待
			while (success) {
				// 重新注册
				FrontReport report = client.login(local);
				if (report.isLinger()) {
					if (System.currentTimeMillis() >= endTime) {
						who = FrontEntryFlag.LOGIN_TIMEOUT;
						break;
					}
					delay(lingerTimeout); // 延时再试
				} else if (report.isLogined()) {
					who = FrontEntryFlag.SUCCESSFUL; // 注册成功
					break;
				} else if (report.isMaxUser()) {
					who = FrontEntryFlag.MAX_USER; // 达到最大用户数
					break;
				} else if (report.isServiceMissing()) {
					who = FrontEntryFlag.SERVICE_MISSING; // 服务不足！
					break;
				} else if(report.isMaxRetry()) {
					who = FrontEntryFlag.MAX_RETRY; // 最大重试次数
					break;
				} else if (report.isFailed()) {
					who = FrontEntryFlag.GATE_FAULT;
					break; // 注册失败
				}
			}
			// 关闭主机
			client.close();
		} catch (VisitException e) {
			Logger.error(e);
			who = FrontEntryFlag.GATE_FAULT;
		} catch (Throwable e) {
			Logger.fatal(e);
			who = FrontEntryFlag.GATE_FAULT;
		}

		// 关闭
		client.destroy();
		
		// 成功，发送HELO包
		if (who == FrontEntryFlag.SUCCESSFUL) {
			// 刷新最后时间
			refreshEndTime();
			// 发送HELP握手数据包
			hello();
		}

		return new EntryStatus(who);
	}

	/**
	 * 启动追踪
	 * @param tracker
	 */
	private void startTracker(FrontLoginTracker tracker) {
		// 启动追踪
		if (tracker != null) {
			tracker.start();
		}
	}

	/**
	 * 关闭追踪
	 * @param tracker
	 */
	private void stopTracker(FrontLoginTracker tracker) {
		// 关闭线程，停止追踪
		if (tracker != null) {
			tracker.stop();
		}
	}

	/**
	 * 解析边缘容器监听端口
	 * @param site FRONT节点
	 * @param document
	 */
	protected void splitTubListen(org.w3c.dom.Document document) {
		org.w3c.dom.Element root = (org.w3c.dom.Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);

		org.w3c.dom.NodeList nodes = root.getElementsByTagName(SiteMark.MARK_TUB_SERVER);
		// 设置一个默认的主机地址，全0
		if (nodes.getLength() != 1) {
			local.setTub(new SocketHost(SocketTag.TCP));
			return;
		}

		org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
		String input = XMLocal.getAttribute(element, SiteMark.MARK_TUB_STREAM_MONITOR, SiteMark.MARK_TUB_STREAM_MONITOR_LISTEN); 
		// 判断主机地址，设置它
		boolean success = false;
		if (SocketHost.validate(input)) {
			try {
				SocketHost host = new SocketHost(input);
				if (!host.isStream()) {
					throw new java.net.UnknownHostException(input);
				}
				local.setTub(host);
				success = true;
			} catch (java.net.UnknownHostException e) {
				Logger.error(e);
			}
		}

		// 以上不成功，设置默认的主机地址
		if (!success) {
			local.setTub(new SocketHost(SocketTag.TCP)); 
		}

		// 线程堆栈
		input = XMLocal.getAttribute(element, SiteMark.MARK_TUB_STREAM_MONITOR, SiteMark.STACK_SIZE);
		long stackSize = ConfigParser.splitLongCapacity(input, tubMonitor.getStackSize());
		tubMonitor.setStackSize(stackSize);

		// TCP服务器可以同时接受的SOCKET数目
		input = XMLocal.getValue(element, SiteMark.TCP_BLOCKS);
		int blocks = ConfigParser.splitInteger(input, tubMonitor.getBlocks());
		tubMonitor.setBlocks(blocks);

		// FIXP TCP服务器接收缓存空间
		input = XMLocal.getValue(element, SiteMark.SERVER_TCP_RECEIVE_BUFFERSIZE);
		int size = (int) ConfigParser.splitLongCapacity(input, tubMonitor.getReceiveBufferSize());
		tubMonitor.setReceiveBufferSize(size);

		Logger.debug(this, "splitTubListen", "tub host: %s, stack size: %s, blocks: %d, receive buffer: %s",
				local.getTub(), ConfigParser.splitCapacity(tubMonitor.getStackSize()),
				tubMonitor.getBlocks(), ConfigParser.splitCapacity(tubMonitor.getReceiveBufferSize()));
	}

	/**
	 * 设置边缘容器管理池的存取目录。
	 * 边缘容器是一个可选项。
	 * 
	 * @param document XML文档
	 */
	protected void splitTubPool(org.w3c.dom.Document document) {
		org.w3c.dom.NodeList nodes = document.getElementsByTagName(OtherMark.TUB_DIRECTORY);
		if (nodes.getLength() != 1) {
			return;
		}

		// 解析安全配置文件
		String path = XMLocal.getXMLValue(nodes);

		// 判断目录有效
		boolean success = (path != null && path.length() > 0);
		if (success) {
			TubPool.getInstance().setRoot(path);
		}

		// 属性
		org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
		String input = element.getAttribute(OtherMark.TUB_UPDATE_TIMEOUT);
		long timeout = ConfigParser.splitTime(input, 60000); // 默认一分钟
		TubPool.getInstance().setSleepTimeMillis(timeout);

		Logger.debug(this, "splitTubPool", success, "tub path:%s", path);
	}
	
	/**
	 * 返回注册的时间
	 * 
	 * @return 长整型
	 */
	public long getLoginTime() {
		return loginTime;
	}

	/**
	 * 登录操作 <br>
	 * 所有FRONT站点，首先登录到ENTRANCE站点，通过ENTRANCE站点定位到GATE站点，最后登录GATE站点。
	 * 
	 * @param hub ENTRANCE站点地址
	 * @param tracker 登录追踪器
	 * @return 注册成功返回“真”，否则“假”。
	 */
	public int login(SiteHost hub, boolean auto, FrontLoginTracker tracker) {
		Logger.debug(this, "login", "login to %s", hub);
		
		// 不论外部接口如何定义，内部强制所有通信模式是UDP。UDP保证内网穿透时正常使用。
		EchoTransfer.setTransferMode(SocketTag.UDP);

		// 重置UDP包参数
		getPacketHelper().reset();
		getReplyHelper().reset();

		// 清空始初登录站点
		setInitHub(null);
		// 清空出口地址
		setDoorHost(null);
		// 启动追踪
		startTracker(tracker);

		// 检测服务器，判断有效，原因：
		// 1. 借用JVM启动TCP/IP堆栈，启动时间超长！FixpStreamClient/FixpPacketClient可以长时间等待
		if (!checkHub(hub, 1)) {
			stopTracker(tracker); // 停止追踪
			return FrontEntryFlag.REFLECT_FAULT; // 错误退出！
		}

		// 在"pitch"方法前，销毁ENTRANCE节点上的front记录
		release(hub);
		// 通过ENTRANCE节点，定位本机地址
		boolean success = pitch(hub, tracker);
		if (!success) {
			// 撤销与ENTRANCE服务器的对称密钥
			dropSecure(hub.getPacketHost());
			// 删除本地密文
			removeCipher(hub.getPacketHost());
			// 停止追踪
			stopTracker(tracker);
			return FrontEntryFlag.REFLECT_FAULT; // 错误退出！
		}

		// 如果位于NAT网络，许可证不支持跨网段通信时...
		if (isPock()) {
			if (!isSkipcast()) {
				// 撤销与ENTRANCE服务器的对称密钥
				dropSecure(hub.getPacketHost());
				// 删除本地密文
				removeCipher(hub.getPacketHost());
				// 停止追踪
				stopTracker(tracker);
				return FrontEntryFlag.LICENCE_NAT_REFUSE;
			}
		}

		// 1. 登录到ENTRANCE站点
		EntryStatus status = doEntrance(hub);
		// 不成功，退出
		success = status.isSuccessful();
		if (!success) {
			// 撤销与ENTRANCE服务器的对称密钥
			dropSecure(hub.getPacketHost());
			// 删除本地密文
			removeCipher(hub.getPacketHost());
			// 停止追踪
			stopTracker(tracker);

			Logger.error(this, "login", "entrance failed! status code:%d", status.getFamily());
			// 返回错误码
			return status.getFamily();
		}

		// 2. 拿到重定向的GATE站点，注册到GATE站点
		SiteHost redirect = status.getRedirect();

		// 在"pitch"之前，销毁GATE服务器可能存在的FRONT节点记录
		release(redirect);
		// 通过GATE节点，定位本机地址
		success = pitch(redirect, tracker);
		if (!success) {
			// 撤销与ENTRANCE站点的对称密钥
			dropSecure(getInitHub());
			// 撤销与GATE站点的对称密钥
			dropSecure(redirect.getPacketHost());
			// 删除本地密文
			removeCipher(getInitHub().getPacketHost());
			removeCipher(redirect.getPacketHost());

			// 停止追踪
			stopTracker(tracker);

			return FrontEntryFlag.REFLECT_FAULT; // 错误退出！
		}

		// 登录GATE站点
		status = doGate(redirect);
		success = status.isSuccessful();
		// 不成功，撤销与ENTRANCE/GATE站点可能建立的密钥，然后退出
		if (!success) {
			// 撤销与ENTRANCE站点的对称密钥
			dropSecure(getInitHub());
			// 撤销与GATE站点的对称密钥
			dropSecure(redirect.getPacketHost());
			// 删除本地密文
			removeCipher(getInitHub().getPacketHost());
			removeCipher(redirect.getPacketHost());
			// 停止追踪
			stopTracker(tracker);

			Logger.error(this, "login", "gate failed! status code:%d", status.getFamily());
			// 返回错误码
			return status.getFamily();
		}

		// 停止追踪
		stopTracker(tracker);

		// 登录成功
		setLogined(true);
		// 自动状态，不设置强制循环
		if (auto) {
			setRoundSuspend(false);
		} 
		// 手动状态，如果登录成功，强制循环退出，否则仍然是强制循环
		else {
			if (success) {
				setRoundSuspend(false);
			} else {
				setRoundSuspend(true);
			}
		}
		
		// 记录注册成功的时间
		loginTime = System.currentTimeMillis();

		// 保存GATE站点地址
		setHub(new Node(SiteTag.GATE_SITE, redirect)); // 这是重定向的GATE站点
		// 刷新最后更新时间，连续发送3个数据包
		refreshEndTime();
		hello(3);

		/** 
		 * 注册成功后，加载分布资源
		 * 从网络上（GATE/CALL节点）加载用户的网络数据，包括：
		 * 1. 用户账号
		 * 2. 被授权表，授权人的GATE站点地址，关联的CALL节点地址 
		 ***/
		getCommandPool().admit(new LoadSchedule());

		// 返回成功
		return FrontEntryFlag.SUCCESSFUL;
	}
	
	/**
	 * 注册到指定地址
	 * @param hub 初始登录站点地址（ENTRANCE站点地址）
	 * @param tracker 登录追踪器
	 * @return 返回状态码
	 */
	public int login(Node hub, boolean auto, FrontLoginTracker tracker) {
		return login(hub.getHost(), auto, tracker);
	}

	/**
	 * 以客户端身份发起RPC操作通知ENTRANCE/GATE服务器，删除ENTRANCE/GATE服务器保存的记录，退出登录状态
	 * @param hub ENTRANCE/GATE服务器主机地址
	 * @return 成功返回真，否则假。
	 */
	private boolean disconnect(Node hub) {
		// 建立RCP连接
		FrontClient client = ClientCreator.createFrontClient(hub, false); // UDP连接
		if (client == null) {
			Logger.error(this, "disconnect", "cannot be connect %s", hub);
			return false;
		}

		// 客户端退出登录状态
		Node local = getListener().duplicate();
		// 如果FRONT节点在内网，节点的UDP端口修改为NAT的UDP端口
		switchTo(hub.getPacketHost(), local);
		
		// 注销登录
		boolean success = false;
		try {
			success = client.logout(local);
			client.close(); // 正常柔性关闭
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 销毁（防止故障时残留数据）
		client.destroy();
		
		// 本地FixpPacketHelper删除"ENTRANCE/GATE" FixpPacketMonitor主机地址
		getPacketHelper().removePock(hub.getPacketHost());
		
		// 删除与服务器通信的密钥
		removeCipher(hub);
		
		Logger.debug(this, "disconnect", success, "drop local %s from %s", local, hub);

		return success;
	}
	
	/**
	 * 区分自动/手动，注销登录
	 * @param auto 自动或者否
	 * @return 成功返回真，否则假
	 */
	protected void __logout(boolean auto) {
		// 如果已经退出，就不要再执行了！
		if (isLogout()) {
			Logger.warning(this, "__logout", "duplicate logout!");
			return;
		}

		// 改为注销状态，强制进入自循环
		setLogined(false);
		// 如果是自动，SiteLauncher.defaultProcess不进入强制自循环；如果是手动，必须进入强制自循环！
		if (auto) {
			setRoundSuspend(false);
		} else {
			setRoundSuspend(true);
		}
		// 不要重新注册，防止线程并发要求重新注册时...
		setKiss(false);

		/**
		 * 注意：为保持 FRONT->ENTRANCE/GATE加密和信道端口有效性，以备不时之需，
		 * FRONT需要与ENTRANCE/GATE定时进行"POCK"通信，
		 * 这个操作在“FixpPacketHelper”中定时发生，调用“sendPockPacket”方法。
		 * 退出登录时，调用“cancel”方法撤销连接！
		 * 
		 * 注销在ENTRANCE节点的注册，FRONT保持与ENTRANCE的“POCK”通信。
		 */

		// 1. 撤销ENTRANCE节点联系，条件是它必须在内网。
		// 先撤销FixpPacketHelper/ReplyHelper中的检测单元，避免激活密钥。
		// 再发起远程通信，删除Entrance FixpPacketMonitor POCK加密信道的密钥，然后删除本地密钥。
		Node entrance = getInitHub();
		if (entrance != null) {
			disconnect(entrance);

			// ReplyHelper删除ENTRANCE ReplyDispatcher主机地址(数据信道的PockItem)
			SocketHost dispatcher = getReplyHelper().findReplyDispatcherByHub(entrance);
			if (dispatcher != null) {
				getReplyHelper().removePock(dispatcher);
			}

//			// ReplyHelper删除ENTRANCE ReplyDispatcher主机地址(数据信道的PockItem)
//			if (entranceDispatcher != null) {
//				getReplyHelper().removePock(entranceDispatcher);
//			}
		}

		// 2. FRONT以客户端身份退出与Gate登录状态
		// 两种可能：1. 正常的注销；2. 账号被删除，本处次再注销，应该不会成功！
		Node gate = getHub();
		if (gate != null) {
			disconnect(gate);
			
			// ReplyHelper删除Gate ReplyDispatcher主机地址（数据信道的PockItem）
			SocketHost dispatcher = getReplyHelper().findReplyDispatcherByHub(gate);
			if (dispatcher != null) {
				getReplyHelper().removePock(dispatcher);
			}

//			// 如果在网网，删除内网定时检测单元（删除PockItem是避免FixpPacketHelper/ReplyHelper激活密钥）
//			// FixpPacketHelper和ReplyHelper定时向外网发送UDP包，确定NAT转换端口有效！
//			
//			// ReplyHelper删除关联Gate节点的PockItem
//			if (gateDispatcher != null) {
//				getReplyHelper().removePock(gateDispatcher);
//			}
		}

		// 4. 注销与全部授权人的GATE站点的连接
		AuthroizerGateOnFrontPool.getInstance().logoutAll();
		// 注销与全部CALL站点的连接
		CallOnFrontPool.getInstance().logoutAll();

		// 重置
		getReplyHelper().reset();
		getPacketHelper().reset();

//		// 清除entrance/gate reply dispatcher主机地址！
//		entranceDispatcher = null;
//		gateDispatcher = null;

		// 离线状态，这个参数很重要！
		local.setGrade(GradeTag.OFFLINE);

		// 重置全部参数
		GuideTaskPool.getInstance().reset();

		// 释放资源
		getStaffPool().clear();
		getStaffPool().reveal();
		
		// 注册时间恢复到0
		loginTime = 0;
	}
	
	/*
	 * 手动注销
	 * @see com.laxcus.launch.SiteLauncher#logout()
	 */
	@Override
	public boolean logout() {
		// 不进入强制自循环
		__logout(false);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TaskListener#refreshTask(int)
	 */
	@Override
	public void refreshTask(int family) {
		Logger.debug(this, "refreshTask", "this is %s", PhaseTag.translate(family));
		if (isLogined()) {
			checkin(false);
		} else {
			kiss();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.TaskListener#hasTaskUser(com.laxcus.util.Siger)
	 */
	@Override
	public boolean hasTaskUser(Siger siger) {
		return (Laxkit.compareTo(getUsername(), siger) == 0);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#()
	 */
	@Override
	public void process() {
		defaultProcess();
		Logger.debug(this, "process", "exit");
	}

	/**
	 * 判断达到GATE节点要求的重新注册间隔时间
	 * @return 返回真或者假
	 */
	protected final boolean canAutoReloginInterval() {
		long begin = getRefreshEndTime();
		return System.currentTimeMillis() - begin >= autoRetryInterval;
	}

	/**
	 * 切换UDP端口
	 * @param remote 目标地址
	 * @param node 本地节点
	 * @return 成功返回真，否则假
	 */
	private boolean switchTo(SocketHost remote, Node localNode) {
		// 判断是内网
		boolean success = isPock();
		// 不成立，忽略它!
		if (!success) {
			return true;
		}

		// 找到NAT地址
		SocketHost nat = getPacketHelper().findPockLocal(remote);
		if (nat == null) {
			Logger.error(this, "switchTo", "cannot be find nat! from %s", remote);
			return false;
		}
		// 修改NAT出口地址
		localNode.getHost().setUDPort(nat.getPort());
		Logger.info(this, "switchTo", "exchange nat %s! from %s", localNode, remote);
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#login()
	 */
	@Override
	public boolean login() {
		// 如果没有达到GATE节点要求的重新注册间隔时间，忽略！
		if (!canAutoReloginInterval()) {
			return false;
		}

		// 确定初始HUB站点地址（ENTRANCE站点）
		Node hub = getInitHub();
		if (hub == null) {
			Logger.error(this, "login", "entrance site is null!");
			return false;
		}
		// 自动注销，SiteLauncher.defaultProcess方法不进入强制自循环！
		__logout(true);

		// 在重新注册前，刷新最后调用的时间
		refreshEndTime();
		
		int who = login(hub, true, null);
		// 返回真或者假
		boolean success = FrontEntryFlag.isSuccessful(who);
		// 如果不成功，重新设置ENTRANCE站点地址
		if (!success) {
			setInitHub(hub);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#register()
	 */
	@Override
	protected void register() {
		// FRONT节点不支持这个操作
	}

	/**
	 * 接受远程站点通知，关闭和退出进程。
	 */
	public void shutdown() {
		stop();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getPublicListener()
	 */
	@Override
	public Node getPublicListener() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#loadLicence(boolean)
	 */
	@Override
	public boolean loadLicence(boolean remote) {
		// 不加载许可证
		int who = checkLicence();

		return (who == Licence.LICENCE_IGNORE || who == Licence.LICENCE_ALLOW);
	}

	/**
	 * HELO激活反馈。<br>
	 * FrontPacketAdapter收到来自GATE站点的激活反馈通知时，调用这个接口。
	 * 图形终端以可视化的形式在底栏显示动画图标，控制台和驱动程序无显示。
	 */
	public abstract void ticking();

	/**
	 * 判断是绑定到用户应用、无操作界面的驱动程序站点
	 * @return 返回真或者假
	 */
	public abstract boolean isDriver();

	/**
	 * 判断是字符界面的控制台站点
	 * @return 返回真或者假
	 */
	public abstract boolean isConsole();

	/**
	 * 判断是图形界面的终端站点
	 * @return 返回真或者假
	 */
	public abstract boolean isTerminal();

	/**
	 * 判断是用于边缘计算的服务端节点（在后台运行）
	 * @return 返回真或者假
	 */
	public abstract boolean isEdge(); 
	
	/**
	 * 判断用于桌面
	 * @return 返回真或者假
	 */
	public abstract boolean isDesktop();
	
	/**
	 * 判断是独立的应用程序
	 * @return 返回真或者假
	 */
	public abstract boolean isApplication();

	/**
	 * 在状态栏显示信息
	 * @param text 文本信息
	 */
	public abstract void showStatusText(String text);

	/**
	 * 显示账号等级
	 * @param grade 账号等级
	 */
	public abstract void showGrade(int grade);

	/**
	 * 返回FRONT资源管理池句柄
	 * @return StaffOnFrontPool实例
	 */
	public abstract StaffOnFrontPool getStaffPool();

	/**
	 * 收到GATE服务端发来的通知，FRONT节点没有注册，调用这个方法。 在此处理的内容: <br>
	 * 1. 图形终端显示中断图标，状态栏显示“登录中断，尝试重新连接...” <br>
	 * 2. 字符控制台显示“登录中断，尝试重新连接...” <br>
	 * 3. 驱动程序啥也不会 <br>
	 */
	public abstract void forsake();

	/**
	 * 设置可以显示的日志单元数。只在FRONT.TERMINAL节点有效
	 * @param n 日志单元数
	 * @return 返回更新后的结果值
	 */
	public abstract int setMaxLogs(int n);

	/**
	 * 根据编号播放声音
	 * @param who 声音编号
	 */
	public abstract void playSound(int who);
}

//	/**
//	 * 定位FRONT站点主机地址
//	 * @param hub ENTRANCE站点地址
//	 * @return 成功返回真，否则假
//	 */
//	private int pitch(SocketHost hub, boolean save) {
//		// 向ENTRANCE FIXP UDP服务器求证自己的实际IP地址
//		SocketHost reflect = reflect(hub);
//		// 失败，返回没有找到提示
//		if (reflect == null) {
//			return FrontPitch.NOT_FOUND; // 没有找到服务器
//		}
//
//		// 取出绑定的主机地址
//		SocketHost host = packetMonitor.getBindHost();
//		// 比较地址一致
//		boolean matching = (Laxkit.compareTo(reflect, host) == 0);
//		// 地址不一致，且要保存时...
//		if (!matching && save) {
//			// 服务端返回的地址
//			Address address = reflect.getAddress();
//
//			packetMonitor.setDefineHost(reflect);
//			streamMonitor.getDefineHost().setAddress(address);
//			// 私有主机地址
//			replySucker.setDefinePrivateIP(address);
//			replyDispatcher.setDefinePrivateIP(address);
//			// 站点地址
//			local.getHost().setAddress(address);
//			local.getHost().setUDPort(reflect.getPort());
//		}
//
//		Logger.info(this, "pitch", "pitch to %s, local %s", hub, local);
//
//		return FrontPitch.SUCCESSFUL;
//	}


//	/**
//	 * 定位FRONT站点主地址
//	 * @param hub ENTRANCE站点地址
//	 * @return 成功返回真，否则假
//	 */
//	private boolean pitch1(SocketHost hub) {
//		// 取出绑定的主机地址
//		SocketHost host = packetMonitor.getBindHost();
//		// 不是通配符地址，退出！
//		if (!host.getAddress().isAnyLocalAddress()) {
//			return true;
//		}
//
//		// 如果绑定通配符地址，向ENTRANCE FIXP UDP服务器求证自己的实际IP地址
//		SocketHost reflect = reflect(hub);
//		boolean success = (reflect != null);
//
//		Logger.note(this, "pitch", success, "to hub %s, local is %s", hub, reflect);
//
//		// 成功，修改FRONT站点地址！
//		// FIXP TCP 监听服务器忽略，所有服务全部从UDP进出！
//		if (success) {
//			Address address = reflect.getAddress();
//			// 定义监听地址
//			packetMonitor.setDefineHost(reflect);
//			streamMonitor.getDefineHost().setAddress(address);
//			// 私有主机地址
//			replySucker.setDefinePrivateIP(address);
//			replyDispatcher.setDefinePrivateIP(address);
//			// 站点地址
//			local.getHost().setAddress(address);
//			local.getHost().setUDPort(reflect.getPort());
//		}
//
//		Logger.note(this, "pitch", success, "pitch to %s, local %s", hub, local);
//
//		return success;
//	}


///**
// * 以客户端身份发起RPC操作通知GATE服务器，删除GATE服务器保存的记录，退出登录状态
// * @param hub GATE服务器UDP地址
// * @return 成功返回真，否则假。
// */
//private boolean disconnect(SocketHost hub) {
//	// 建立RCP连接
//	FrontClient client = ClientCreator.createFrontClient(hub); // UDP连接
//	if (client == null) {
//		Logger.error(this, "disconnect", "cannot be connect %s", hub);
//		return false;
//	}
//
//	// 客户端退出登录状态
//	Node local = getListener();
//	boolean success = false;
//	try {
//		success = client.logout(local);
//		client.close(); // 正常柔性关闭
//	} catch (VisitException e) {
//		Logger.error(e);
//	}
//	// 销毁（防止故障时残留数据）
//	client.destroy();
//	
//	// 删除与服务器通信的密钥
//	removeCipher(hub);
//
//	return success;
//}

///*
// * 手动注销
// * @see com.laxcus.launch.SiteLauncher#logout()
// */
//@Override
//public boolean logout() {
//	// 如果已经退出，就不要再执行了！
//	if (isLogout()) {
//		Logger.warning(this, "logout", "duplicate logout!");
//		return true;
//	}
//
////	// 首先切换到注销状态
////	switchStatus(false);
//	
//	// 改为注销状态，强制进入自循环
//	setLogined(false);
//	setRoundSuspend(true);
//	// 不要重新注册，防止线程并发要求重新注册时...
//	setKiss(false);
//
//	ReplyHelper replyHelper = getReplyHelper();
//	FixpPacketHelper packetHelper = getPacketHelper();
//	final long timeout = SocketTransfer.getDefaultChannelTimeout();
//
//	/**
//	 * 注意：为保持 FRONT->ENTRANCE/GATE加密和信道端口有效性，以备不时之需，
//	 * FRONT需要与ENTRANCE/GATE定时进行"POCK"通信，
//	 * 这个操作在“FixpPacketHelper”中定时发生，调用“sendPockPacket”方法。
//	 * 退出登录时，调用“cancel”方法撤销连接！
//	 * 
//	 * 注销在ENTRANCE节点的注册，FRONT保持与ENTRANCE的“POCK”通信。
//	 */
//
//	// 1. 撤销ENTRANCE节点联系，条件是它必须在内网。
//	// 先撤销FixpPacketHelper/ReplyHelper中的检测单元，避免激活密钥。
//	// 再发起远程通信，删除Entrance FixpPacketMonitor POCK加密信道的密钥，然后删除本地密钥。
//	Node entrance = getInitHub();
//	if (entrance != null && isPock()) {
//		// 如果在内网中，删除内网定位定时检测单元。这个单元的作用是定时向外网发送UDP包，保证NAT端口有效！
//		SocketHost remote = entrance.getPacketHost();
//
//		// 先删除PockItem，避免xxxHelper线程发送Shine数据包激活密钥
//
//		// ReplyHelper删除ENTRANCE ReplyDispatcher主机地址
//		if (entranceDispatcher != null) {
//			replyHelper.removePock(entranceDispatcher);
//		}
//		// PacketHelper删除ENTRANCE FixpPacketMonitor主机地址
//		packetHelper.removePock(remote);
//
//		// 删除私属密文
//		boolean success = dropSecure(remote, timeout, 1);
//		Logger.note(this, "logout", success, "drop secure from %s", entrance);
//
//		// 如果不成功，执行“NOTIFY.EXIT”方式撤销UDP通信
//		if (!success) {
//			success = cancel(entrance);
//			// 删除密文（冗余操作）
//			removeCipher(entrance); 
//			Logger.note(this, "logout", success, "cancel from %s", entrance);
//		}
//	}
//
//	// 2. FRONT以客户端身份退出登录状态
//	// 两种可能：1. 正常的注销；2. 账号被删除，本处次再注销，应该不会成功！
//	Node gate = getHub();
//	SocketHost remote = gate.getPacketHost();
//	boolean success = disconnect(remote);
//	Logger.note(this, "logout", success, "logout from %s", gate);
//
//	// 如果在网网，删除内网定时检测单元（删除PockItem是避免xxxHelper激活密钥）
//	// xxxHelper定时向外网发送UDP包，确定NAT转换端口有效！
//	if (gateDispatcher != null) {
//		replyHelper.removePock(gateDispatcher);
//	}
//	packetHelper.removePock(remote);
//
//	// 3. 撤销GATE节点联系，包括：POCK加密信道、HELO信道(Gate FixpPacketMonitor信道)，删除双方协议密文
//	success = dropSecure(remote, timeout, 1);
//	Logger.note(this, "logout", success, "drop secure from %s", gate);
//	// 如果不成功，采用“NOTIFY.EXIT”方式结束UDP通信
//	if(!success) {
//		success = cancel(gate);
//		removeCipher(gate); // 删除密文
//		Logger.note(this, "logout", success, "cancel from %s", gate);
//	}
//
//	// 4. 注销与全部授权人的GATE站点的连接
//	AuthroizerGateOnFrontPool.getInstance().logoutAll();
//	// 注销与全部CALL站点的连接
//	CallOnFrontPool.getInstance().logoutAll();
//
//	// 重置
//	getReplyHelper().reset();
//	getPacketHelper().reset();
//
//	// 清除entrance/gate reply dispatcher主机地址！
//	entranceDispatcher = null;
//	gateDispatcher = null;
//
//	// 离线状态
//	local.setGrade(GradeTag.OFFLINE);
//
//	// 重置全部参数
//	GuideTaskPool.getInstance().reset();
//
//	// 释放资源
//	getStaffPool().clear();
//	getStaffPool().reveal();
//
//	return true;
//}


///*
// * (non-Javadoc)
// * @see com.laxcus.launch.SiteLauncher#login()
// */
//@Override
//public boolean login() {
//	// 如果没有达到GATE节点要求的重新注册间隔时间，忽略！
//	if (!canAutoReloginInterval()) {
//		return false;
//	}
//	// 确定初始HUB站点地址（ENTRANCE站点）
//	Node hub = getInitHub();
//	if (hub == null) {
//		Logger.error(this, "login", "entrance site is null!");
//		return false;
//	}
//	
//	// 先注销!
//	logout();
//
//	// 在重新注册前，刷新最后调用的时间
//	refreshEndTime();
//	// 只有当GATE站点上的黑名单时间和FIXP密钥超时后，才启动重新注册！
//
//	// 尝试登录，不显示登录状态
//	int who = login(hub, true, null);
//	// 返回真或者假
//	boolean success = FrontEntryFlag.isSuccessful(who);
//	// 如果不成功，重新设置ENTRANCE站点地址
//	if (!success) {
//		setInitHub(hub);
//	}
//	return success;
//}


///**
// * 注销与ENTRANCE通信
// * @param hub
// * @return
// */
//private boolean __logoutEntrance(SocketHost hub) {
//	// 连接注册的CALL站点地址
//	FrontClient client = ClientCreator.createFrontClient(hub); // UDP连接
//	if (client == null) {
//		Logger.error(this, "__logoutEntrance", "cannot be git %s", hub);
//		return false;
//	}
//
//	// 生成副本，注意！必须是副本
//	Node node = getListener().duplicate();
//	
//	// 如果FRONT节点在内网，节点的UDP端口修改为NAT的UDP端口
//	switchTo(hub, node);
//	
//	boolean success = false;
//	try {
//		success = client.logout(node);
//		client.close();
//	} catch (VisitException e) {
//		Logger.error(e);
//	}
//	// 彻底销毁
//	client.destroy();
//	
////	ReplyHelper replyHelper = getReplyHelper();
////	FixpPacketHelper packetHelper = getPacketHelper();
//	
//	// ReplyHelper删除ENTRANCE ReplyDispatcher主机地址
//	if (entranceDispatcher != null) {
//		getReplyHelper().removePock(entranceDispatcher);
//	}
//	// PacketHelper删除ENTRANCE FixpPacketMonitor主机地址
//	getPacketHelper().removePock(hub);
//	
//	// FRONT删除本地保存，与ENTRANCE节点通信的密钥
//	removeCipher(hub);
//	
//	Logger.debug(this, "__logoutEntrance", success, "from %s", hub);
//	
//	return success;
//}

//private boolean __logoutGate(Node hub) {
//	// 以被授权人身份，注销与全部授权人的联系
//	AuthroizerGateOnFrontPool.getInstance().logoutAll();
//	
//	// 建立与GATE节点连接
//	FrontClient client = ClientCreator.createFrontClient(hub, false); // UDP连接
//	if (client == null) {
//		Logger.error(this, "__logoutGate", "cannot be git %s", hub);
//		return false;
//	}
//
//	// 生成副本，注意！必须是副本
//	Node node = getListener().duplicate();
//	
//	// 如果FRONT节点在内网，节点的UDP端口修改为NAT的UDP端口
//	switchTo(hub.getPacketHost(), node);
//	
//	// 与GATE远程通信，注销！
//	boolean success = false;
//	try {
//		success = client.logout(node);
//		client.close();
//	} catch (VisitException e) {
//		Logger.error(e);
//	}
//	// 彻底销毁
//	client.destroy();
//	
//	// 删除NAT内网与GATE节点通信的端口
////	ReplyHelper replyHelper = getReplyHelper();
////	FixpPacketHelper packetHelper = getPacketHelper();
////	if (gateDispatcher != null) {
////		getReplyHelper().removePock(gateDispatcher);
////	}
////	getPacketHelper().removePock(node.getPacketHost());
//	
//	// ReplyHelper删除GATE ReplyDispatcher主机地址
//	if (gateDispatcher != null) {
//		getReplyHelper().removePock(gateDispatcher);
//	}
//	// PacketHelper删除GATE FixpPacketMonitor主机地址
//	getPacketHelper().removePock(node.getPacketHost());
//	
//	// FRONT删除本地保存，与GATE节点通信的密钥
//	removeCipher(hub);
//	
//	Logger.debug(this, "__logoutGate", success, "from %s", hub);
//	
//	return success;
//}

///**
// * 自动注销
// * @return
// */
//private boolean __auto_logout() {
//	// 如果已经退出，就不要再执行了！
//	if (isLogout()) {
//		Logger.warning(this, "logout", "duplicate logout!");
//		return true;
//	}
//
////	// 首先切换到注销状态
////	switchStatus(false);
//	
//	// 改为注销状态
//	setLogined(false);
//	// 自动注销，SiteLauncher.defaultProcess不要进入强制自循环状态。这个非常重要
//	setRoundSuspend(false); // 
//	
//	// 不要重新注册，防止线程并发要求重新注册时...
//	setKiss(false);
//
////	ReplyHelper replyHelper = getReplyHelper();
////	FixpPacketHelper packetHelper = getPacketHelper();
////	final long timeout = SocketTransfer.getDefaultChannelTimeout();
////
////	/**
////	 * 注意：为保持 FRONT->ENTRANCE/GATE加密和信道端口有效性，以备不时之需，
////	 * FRONT需要与ENTRANCE/GATE定时进行"POCK"通信，
////	 * 这个操作在“FixpPacketHelper”中定时发生，调用“sendPockPacket”方法。
////	 * 退出登录时，调用“cancel”方法撤销连接！
////	 * 
////	 * 注销在ENTRANCE节点的注册，FRONT保持与ENTRANCE的“POCK”通信。
////	 */
////
////	// 1. 撤销ENTRANCE节点联系，条件是它必须在内网。
////	// 先撤销FixpPacketHelper/ReplyHelper中的检测单元，避免激活密钥。
////	// 再发起远程通信，删除Entrance FixpPacketMonitor POCK加密信道的密钥，然后删除本地密钥。
////	Node entrance = getInitHub();
////	if (entrance != null && isPock()) {
////		// 如果在内网中，删除内网定位定时检测单元。这个单元的作用是定时向外网发送UDP包，保证NAT端口有效！
////		SocketHost remote = entrance.getPacketHost();
////
////		// 先删除PockItem，避免xxxHelper线程发送Shine数据包激活密钥
////
////		// ReplyHelper删除ENTRANCE ReplyDispatcher主机地址
////		if (entranceDispatcher != null) {
////			replyHelper.removePock(entranceDispatcher);
////		}
////		// PacketHelper删除ENTRANCE FixpPacketMonitor主机地址
////		packetHelper.removePock(remote);
////
////		// 删除私属密文
////		boolean success = dropSecure(remote, timeout, 1);
////		Logger.note(this, "logout", success, "drop secure from %s", entrance);
////
////		// 如果不成功，执行“NOTIFY.EXIT”方式撤销UDP通信
////		if (!success) {
////			success = cancel(entrance);
////			// 删除密文（冗余操作）
////			removeCipher(entrance); 
////			Logger.note(this, "logout", success, "cancel from %s", entrance);
////		}
////	}
////
////	// 2. FRONT以客户端身份退出登录状态
////	// 两种可能：1. 正常的注销；2. 账号被删除，本处次再注销，应该不会成功！
////	Node gate = getHub();
////	SocketHost remote = gate.getPacketHost();
////	boolean success = disconnect(remote);
////	Logger.note(this, "logout", success, "logout from %s", gate);
////
////	// 如果在网网，删除内网定时检测单元（删除PockItem是避免xxxHelper激活密钥）
////	// xxxHelper定时向外网发送UDP包，确定NAT转换端口有效！
////	if (gateDispatcher != null) {
////		replyHelper.removePock(gateDispatcher);
////	}
////	packetHelper.removePock(remote);
////
////	// 3. 撤销GATE节点联系，包括：POCK加密信道、HELO信道(Gate FixpPacketMonitor信道)，删除双方协议密文
////	success = dropSecure(remote, timeout, 1);
////	Logger.note(this, "logout", success, "drop secure from %s", gate);
////	// 如果不成功，采用“NOTIFY.EXIT”方式结束UDP通信
////	if(!success) {
////		success = cancel(gate);
////		removeCipher(gate); // 删除密文
////		Logger.note(this, "logout", success, "cancel from %s", gate);
////	}
//	
//	Node entrance = getInitHub();
//	Node gate = getHub();
//	
//	// 注销与ENTRANCE连接
//	__logoutEntrance(entrance.getPacketHost());
//	// 注销与GATE连接
//	__logoutGate(gate);
//
//	// 4. 注销与全部授权人的GATE站点的连接
//	AuthroizerGateOnFrontPool.getInstance().logoutAll();
//	// 注销与全部CALL站点的连接
//	CallOnFrontPool.getInstance().logoutAll();
//
//	// 重置
//	getReplyHelper().reset();
//	getPacketHelper().reset();
//
//	// 清除entrance/gate reply dispatcher主机地址！
//	entranceDispatcher = null;
//	gateDispatcher = null;
//
//	// 离线状态
//	local.setGrade(GradeTag.OFFLINE);
//
//	// 重置全部参数
//	GuideTaskPool.getInstance().reset();
//
//	// 释放资源
//	getStaffPool().clear();
//	getStaffPool().reveal();
//	
//	// 注销
//	setLogined(false);
//
//	return true;
//}


///**
// * 切换状态
// * @param logined 注册成功
// */
//private void switchStatus(boolean logined) {
//	// 注册成功，修改状态
//	setLogined(logined);
//	// 撤销强制注销
//	setRoundSuspend(!logined);
//
//	Logger.info(this, "switchStatus", "switch to [%s] status ...", (logined ? "Login" : "Logout"));
//}

///**
// * 登录操作 <br>
// * 所有FRONT站点，首先登录到ENTRANCE站点，通过ENTRANCE站点定位到GATE站点，最后登录GATE站点。
// * 
// * @param hub ENTRANCE站点地址
// * @param tracker 登录追踪器
// * @return 注册成功返回“真”，否则“假”。
// */
//public int __login(SiteHost hub, boolean auto, FrontLoginTracker tracker) {
//	// 不论外部接口如何定义，内部强制所有通信模式是UDP。UDP保证内网穿透时正常使用。
//	EchoTransfer.setTransferMode(SocketTag.UDP);
//
//	// 重置UDP包参数
//	getPacketHelper().reset();
//	getReplyHelper().reset();
//
//	// 清空始初登录站点
//	setInitHub(null);
//	// 清空出口地址
//	setDoorHost(null);
//
//	// 启动追踪
//	startTracker(tracker);
//
//	// 检测服务器，判断有效，原因：
//	// 1. 借用JVM启动TCP/IP堆栈，启动时间超长！FixpStreamClient/FixpPacketClient可以长时间等待
//	if (!checkHub(hub, 3)) {
//		stopTracker(tracker); // 停止追踪
//		return FrontEntryFlag.REFLECT_FAULT; // 错误退出！
//	}
//
//	// 通过ENTRANCE节点，定位本机地址
//	boolean success = pitch(hub, tracker);
//	if (!success) {
//		// 撤销与ENTRANCE服务器的对称密钥
//		dropSecure(hub.getPacketHost());
//		// 删除本地密文
//		removeCipher(hub.getPacketHost());
//		// 停止追踪
//		stopTracker(tracker);
//		return FrontEntryFlag.REFLECT_FAULT; // 错误退出！
//	}
//
//	// 如果位于NAT网络，许可证不支持跨网段通信时...
//	if (isPock()) {
//		if (!isSkipcast()) {
//			// 撤销与ENTRANCE服务器的对称密钥
//			dropSecure(hub.getPacketHost());
//			// 删除本地密文
//			removeCipher(hub.getPacketHost());
//			// 停止追踪
//			stopTracker(tracker);
//			return FrontEntryFlag.LICENCE_NAT_REFUSE;
//		}
//	}
//
//	// 1. 登录到ENTRANCE站点
//	EntryStatus status = doEntrance(hub);
//	// 不成功，退出
//	if (!status.isSuccessful()) {
//		// 撤销与ENTRANCE服务器的对称密钥
//		dropSecure(hub.getPacketHost());
//		// 删除本地密文
//		removeCipher(hub.getPacketHost());
//		// 停止追踪
//		stopTracker(tracker);
//
//		Logger.error(this, "login", "entrance failed! status code:%d", status.getFamily());
//		// 返回错误码
//		return status.getFamily();
//	}
//
//	// 2. 拿到重定向的GATE站点，注册到GATE站点
//	SiteHost redirect = status.getRedirect();
//
//	// 通过GATE节点，定位本机地址
//	success = pitch(redirect, tracker);
//	if (!success) {
//		// 撤销与ENTRANCE站点的对称密钥
//		dropSecure(getInitHub());
//		// 撤销与GATE站点的对称密钥
//		dropSecure(redirect.getPacketHost());
//		// 删除本地密文
//		removeCipher(getInitHub().getPacketHost());
//		removeCipher(redirect.getPacketHost());
//
//		// 停止追踪
//		stopTracker(tracker);
//
//		return FrontEntryFlag.REFLECT_FAULT; // 错误退出！
//	}
//
//	// 登录GATE站点
//	status = doGate(redirect);
//	// 不成功，撤销与ENTRANCE/GATE站点可能建立的密钥，然后退出
//	if (!status.isSuccessful()) {
//		// 撤销与ENTRANCE站点的对称密钥
//		dropSecure(getInitHub());
//		// 撤销与GATE站点的对称密钥
//		dropSecure(redirect.getPacketHost());
//		// 删除本地密文
//		removeCipher(getInitHub().getPacketHost());
//		removeCipher(redirect.getPacketHost());
//		// 停止追踪
//		stopTracker(tracker);
//
//		Logger.error(this, "login", "gate failed! status code:%d", status.getFamily());
//		// 返回错误码
//		return status.getFamily();
//	}
//
//	// 停止追踪
//	stopTracker(tracker);
//
//	// 切换到注册成功状态
//	switchStatus(true);
//	// 保存GATE站点地址
//	setHub(new Node(SiteTag.GATE_SITE, redirect)); // 这是重定向的GATE站点
//	// 连续发送3个数据包
//	hello(3);
//
//	/** 
//	 * 注册成功后，加载分布资源
//	 * 从网络上（GATE/CALL节点）加载用户的网络数据，包括：
//	 * 1. 用户账号
//	 * 2. 被授权表，授权人的GATE站点地址，关联的CALL节点地址 
//	 ***/
//	getCommandPool().admit(new ScheduleLoad());
//
//	// 返回成功
//	return FrontEntryFlag.SUCCESSFUL;
//}