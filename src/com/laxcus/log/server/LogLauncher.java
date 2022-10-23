/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.server;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.fixp.monitor.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.log.server.pool.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.Node;
import com.laxcus.site.log.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.impl.log.*;
import com.laxcus.xml.*;

/**
 * 日志站点启动器<br><br>
 * 
 * 根据日志监听端口启动日志接收服务器，接受和保存各工作站点上传的日志。<br>
 * 
 * @author scott.liang
 * @version 1.2 1/12/2013
 * @since laxcus 1.0
 */
public final class LogLauncher extends SlaveLauncher { 

	/** 日志管理器静态句柄 **/
	private static LogLauncher selfHandle = new LogLauncher();

	/** 各站点日志接收器集合  **/
	private ArrayList<FixpPacketMonitor> array = new ArrayList<FixpPacketMonitor>();

	/** LOG 文件根目录 **/
	private File logRoot;

	/** TIG 文件根目录 **/
	private File tigRoot;
	
	/** bil 文件根目录 **/
	private File billRoot;

	/** 本地服务器地址 **/
	private LogSite local = new LogSite();

	/**
	 * 初始化日志启动器
	 */
	private LogLauncher() {
		super();
		setExitVM(true);
		setPrintFault(true);
		// 日志站点监听，但是不包括日志接收
		setPacketInvoker(new LogPacketAdapter());
		setStreamInvoker(new LogStreamAdapter());
	}

	/**
	 * 返回日志启动器静态句柄
	 * @return LogLauncher实例
	 */
	public static LogLauncher getInstance() {
		return LogLauncher.selfHandle;
	}
	
	/**
	 * 返回日志根目录
	 * @return
	 */
	public File getLogRoot() {
		return logRoot;
	}

	/**
	 * 返回目录
	 * @return
	 */
	public File getTigRoot() {
		return tigRoot;
	}

	/**
	 * 返回消费日志根目录
	 * @return
	 */
	public File getBillRoot() {
		return billRoot;
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
	 * @see com.laxcus.launch.SiteLauncher#getSite()
	 */
	@Override
	public Site getSite() {
		return local;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#login()
	 */
	@Override
	public boolean login() {
//		// 如果是登录状态，先注销
//		if (isLogined()) {
//			logout();
//		}
		
		// 再注册
		LogSite site = local.duplicate();
		return login(site);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#register()
	 */
	@Override
	protected void register() {
		register(local);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return LogCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return LogInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return LogCustomTrustor.getInstance();
	}

	/**
	 * 建立日志文件存储目录
	 * @param path 存储目录
	 * @return 成功返回真，否则假
	 */
	private boolean createLogRoot(String path) {
		if (path == null || path.trim().isEmpty()) {
			return false;
		}

		path = ConfigParser.splitPath(path);
		File dir = new File(path);
		// 检查目录存在，如果不存在，建立一个新目录
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			success = dir.mkdirs();
		}
		if (success) {
			try {
				logRoot = dir.getCanonicalFile();
			} catch (IOException e) {
				logRoot = dir.getAbsoluteFile();
				Logger.error(e);
			}
		}

		// 记录日志写入目录
		if (success) {
			addDeviceDirectory(logRoot.getAbsolutePath());
		}

		Logger.note(this, "createLogRoot", success, "directory is '%s'", logRoot);
		return success;
	}

	/**
	 * 建立日志文件存储目录
	 * @param path 存储目录
	 * @return 成功返回真，否则假
	 */
	private boolean createTigRoot(String path) {
		if (path == null || path.trim().isEmpty()) {
			return false;
		}

		path = ConfigParser.splitPath(path);
		File dir = new File(path);
		// 检查目录存在，如果不存在，建立一个新目录
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			success = dir.mkdirs();
		}
		if (success) {
			try {
				tigRoot = dir.getCanonicalFile();
			} catch (IOException e) {
				tigRoot = dir.getAbsoluteFile();
				Logger.error(e);
			}
		}

		// 记录日志写入目录
		if (success) {
			addDeviceDirectory(tigRoot.getAbsolutePath());
		}

		Logger.note(this, "createTigRoot", success, "directory is '%s'", tigRoot);
		return success;
	}

	/**
	 * 建立BILL文件存储目录
	 * @param path 存储目录
	 * @return 成功返回真，否则假
	 */
	private boolean createBillRoot(String path) {
		if (path == null || path.trim().isEmpty()) {
			return false;
		}

		path = ConfigParser.splitPath(path);
		File dir = new File(path);
		// 检查目录存在，如果不存在，建立一个新目录
		boolean success = (dir.exists() && dir.isDirectory());
		if (!success) {
			success = dir.mkdirs();
		}
		if (success) {
			try {
				billRoot = dir.getCanonicalFile();
			} catch (IOException e) {
				billRoot = dir.getAbsoluteFile();
				Logger.error(e);
			}
		}

		// 记录日志写入目录
		if (success) {
			addDeviceDirectory(billRoot.getAbsolutePath());
		}

		Logger.note(this, "createBillRoot", success, "directory is '%s'", billRoot);
		return success;
	}

	/**
	 * 启动日志服务
	 * @return 成功返回真，否则假
	 */
	private boolean startLogService() {
		/** 
		 * 判断条件：
		 * 1. 如果通配符地址，必须绑定一个实际的本机地址(首先选择内网地址，再选择公网地址，如果没有是自回路)
		 * 2. 如果是自回路地址127.0.0.1，是允许的，这时应该是用于测试环境。
		 */

		// 取出地址，判断是通配符地址，选择一个本地地址。
		Address address = local.getAddress();
		if (address.isAnyLocalAddress()) {
			boolean ipv4 = address.isIPv4();
			address = new Address(Address.select(ipv4));
			local.setAddress(address);
		}

		// 接收缓存
		int readSize = packetMonitor.getReceiveBufferSize();

		//		// 判断日志资源目录
		//		File root = getResourcePath();
		//		// 如果不存在，或者不是目录，建立一个目录
		//		if (!(root.exists() && root.isDirectory())) {
		//			boolean success = root.mkdirs();
		//			if (!success) {
		//				Logger.debug(this, "startLogService", "cannot be create directory!");
		//				return false;
		//			}
		//		}

		Logger.debug(this, "startLogService", "active threads:%d", Thread.activeCount());

		int count = 0;
		LogNode[] nodes = local.toLogNodeArray();
		// 逐一绑定和启动日志监视器
		for (LogNode node : nodes) {
			String tag = node.getTag();
			int port = node.getPort();
			// 日志服务主机地址
			SocketHost host = new SocketHost(SocketTag.UDP, address, port);

			File dir = new File(logRoot, tag);
			LogPacketWriter writer = new LogPacketWriter(dir);
			FixpPacketMonitor monitor = new FixpPacketMonitor();
			// 1个包业务线程
			monitor.setTaskThreads(1);
			// UDP接收缓存
			monitor.setReceiveBufferSize(readSize);

			// 绑定一个本地监听地址
			boolean success = monitor.bind(host);
			if (!success) {
				Logger.error(this, "startLogService", "cannot be bind %s", host);
				break;
			}
			// 设置服务器地址
			monitor.setDefineHost(host);

			// 设置调用器和启动线程
			monitor.setPacketInvoker(writer);
			success = monitor.start();
			// 失败退出！
			if (!success) {
				Logger.error(this, "startLogService", "cannot be start thread! %s", local);
				break;
			}
			// 保存到队列
			array.add(monitor);
			// 等待进入线程循环
			while (!monitor.isRunning()) {
				delay(200);
			}
			count++;
		}

		// 判断成功
		boolean success = (count == nodes.length);

		Logger.note(this, "startLogService", success, "log nodes:%d", local.size());
		Logger.note(this, "startLogService", success, "last! active threads:%d", Thread.activeCount());

		return success;
	}

	/**
	 * 启动操作服务
	 * @return 成功返回真，否则假
	 */
	private boolean startTigService() {
		/** 
		 * 判断条件：
		 * 1. 如果通配符地址，必须绑定一个实际的本机地址(首先选择内网地址，再选择公网地址，如果没有是自回路)
		 * 2. 如果是自回路地址127.0.0.1，是允许的，这时应该是用于测试环境。
		 */

		// 取出地址，判断是通配符地址，选择一个本地地址。
		Address address = local.getAddress();
		if (address.isAnyLocalAddress()) {
			boolean ipv4 = address.isIPv4();
			address = new Address(Address.select(ipv4));
			local.setAddress(address);
		}

		// 接收缓存
		int readSize = packetMonitor.getReceiveBufferSize();

		//		// 判断操作资源目录
		//		File root = getResourcePath();
		//		// 如果不存在，或者不是目录，建立一个目录
		//		if (!(root.exists() && root.isDirectory())) {
		//			boolean success = root.mkdirs();
		//			if (!success) {
		//				Logger.debug(this, "startTigService", "cannot be create directory!");
		//				return false;
		//			}
		//		}

		Logger.debug(this, "startTigService", "active threads:%d", Thread.activeCount());

		int count = 0;
		TigNode[] nodes = local.toTigNodeArray();
		// 逐一绑定和启动操作监视器
		for (TigNode node : nodes) {
			String tag = node.getTag();
			int port = node.getPort();
			// 操作服务主机地址
			SocketHost host = new SocketHost(SocketTag.UDP, address, port);

			File dir = new File(tigRoot, tag);
			TigPacketWriter writer = new TigPacketWriter(dir);
			FixpPacketMonitor monitor = new FixpPacketMonitor();
			// 1个包业务线程
			monitor.setTaskThreads(1);
			// UDP接收缓存
			monitor.setReceiveBufferSize(readSize);

			// 绑定一个本地监听地址
			boolean success = monitor.bind(host);
			if (!success) {
				Logger.error(this, "startTigService", "cannot be bind %s", host);
				break;
			}
			// 设置服务器地址
			monitor.setDefineHost(host);

			// 设置调用器和启动线程
			monitor.setPacketInvoker(writer);
			success = monitor.start();
			// 失败退出！
			if (!success) {
				Logger.error(this, "startTigService", "cannot be start thread! %s", local);
				break;
			}
			// 保存到队列
			array.add(monitor);
			// 循环等待进入线程
			while (!monitor.isRunning()) {
				delay(200);
			}
			count++;
		}

		// 判断成功
		boolean success = (count == nodes.length);

		Logger.note(this, "startTigService", success, "log nodes:%d", local.size());
		Logger.note(this, "startTigService", success, "last! active threads:%d", Thread.activeCount());

		return success;
	}

	/**
	 * 启动BILL服务
	 * @return 成功返回真，否则假
	 */
	private boolean startBillService() {
		/** 
		 * 判断条件：
		 * 1. 如果通配符地址，必须绑定一个实际的本机地址(首先选择内网地址，再选择公网地址，如果没有是自回路)
		 * 2. 如果是自回路地址127.0.0.1，是允许的，这时应该是用于测试环境。
		 */

		// 取出地址，判断是通配符地址，选择一个本地地址。
		Address address = local.getAddress();
		if (address.isAnyLocalAddress()) {
			boolean ipv4 = address.isIPv4();
			address = new Address(Address.select(ipv4));
			local.setAddress(address);
		}

		// 接收缓存
		int readSize = packetMonitor.getReceiveBufferSize();

		//		// 判断日志资源目录
		//		File root = getResourcePath();
		//		// 如果不存在，或者不是目录，建立一个目录
		//		if (!(root.exists() && root.isDirectory())) {
		//			boolean success = root.mkdirs();
		//			if (!success) {
		//				Billger.debug(this, "startBillService", "cannot be create directory!");
		//				return false;
		//			}
		//		}

		Logger.debug(this, "startBillService", "active threads:%d", Thread.activeCount());

		int count = 0;
		BillNode[] nodes = local.toBillNodeArray();
		// 逐一绑定和启动日志监视器
		for (BillNode node : nodes) {
			String tag = node.getTag();
			int port = node.getPort();
			// 日志服务主机地址
			SocketHost host = new SocketHost(SocketTag.UDP, address, port);

			File dir = new File(billRoot, tag);
			BillPacketWriter writer = new BillPacketWriter(dir);
			FixpPacketMonitor monitor = new FixpPacketMonitor();
			// 1个包业务线程
			monitor.setTaskThreads(1);
			// UDP接收缓存
			monitor.setReceiveBufferSize(readSize);

			// 绑定一个本地监听地址
			boolean success = monitor.bind(host);
			if (!success) {
				Logger.error(this, "startBillService", "cannot be bind %s", host);
				break;
			}
			// 设置服务器地址
			monitor.setDefineHost(host);

			// 设置调用器和启动线程
			monitor.setPacketInvoker(writer);
			success = monitor.start();
			// 失败退出！
			if (!success) {
				Logger.error(this, "startBillService", "cannot be start thread! %s", local);
				break;
			}
			// 保存到队列
			array.add(monitor);
			// 等待进入线程循环
			while (!monitor.isRunning()) {
				delay(200);
			}
			count++;
		}

		// 判断成功
		boolean success = (count == nodes.length);

		Logger.note(this, "startBillService", success, "log nodes:%d", local.size());
		Logger.note(this, "startBillService", success, "last! active threads:%d", Thread.activeCount());

		return success;
	}
	
	/**
	 * 停止日志服务
	 */
	private void stopService() {
		// 关闭全部日志接收器
		for (FixpPacketMonitor monitor : array) {
			monitor.stop();
		}
		// 判断运行，等待它退出
		for (FixpPacketMonitor monitor : array) {
			while (monitor.isRunning()) {
				delay(200);
			}
		}
	}

	/**
	 * 启动管理池
	 * @return
	 */
	private boolean loadPool() {		
		// 启动异步操作池
		VirtualPool[] pools = new VirtualPool[] { LogCommandPool.getInstance(),
				LogInvokerPool.getInstance(), CustomClassPool.getInstance() };
		return startAllPools(pools);
	}

	/**
	 * 停止管理池
	 */
	private void stopPool() {
		VirtualPool[] pools = new VirtualPool[] {
				CustomClassPool.getInstance(),
				LogCommandPool.getInstance(),
				LogInvokerPool.getInstance() };
		stopAllPools(pools);
	}

	/**
	 * 初始化日志服务
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 客户端日志默认不处理
		LogConfigure logCfg = Logger.getLogConfigure();

		// 不在终端打印日志
		//		config.setConsolePrint(false);
		//		config.setConsolePrint(true);

		// 缓存模式，本地产生的日志暂时保存，定时清空
		logCfg.setTransferMode(LogConfigure.SENDTO_BUFFER);

		// 1. 预处理，包括确定日志站点超时时间和统一系统时间
		boolean success = preload();
		Logger.note(this, "init", success, "preload");
		// 2. 启动FIXP服务器(保持与HOME站点的通信)
		if (success) {
			Class<?>[] clazzs = { CommandVisitOnLog.class };
			success = loadSingleListen(clazzs, local.getNode());
		}
		Logger.note(this, "init", success, "load listen");
		// 3. 启动日志服务
		if (success) {
			success = startLogService();
		}
		Logger.note(this, "init", success, "start log service");
		// 4. 启动追踪服务
		if (success) {
			success = startTigService();
		}
		Logger.note(this, "init", success, "start tig service");
		// 5. 启动消费服务
		if (success) {
			success = startBillService();
		}
		Logger.note(this, "init", success, "start bill service");
		
		// 6. 加载管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", success, "load pool");
		// 7. 注册到HOME站点
		if (success) {
			success = login();
		}
		Logger.note(this, "init", success, "login to %s", getHubHost());
		Logger.note(this, "init", success, "active threads:%d", Thread.activeCount());

		// 不成功
		if (!success) {
			stopPool();
			stopListen();
			stopService();
		}

		//				printThreads();

		return success;
	}

	/**
	 * 释放日志服务资源
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 从HOME站点注销
		logout();
		// 停止管理池
		stopPool();
		// 停止FIXP监听
		stopListen();
		// 停止日志服务
		stopService();
	}

	/**
	 * 处理日志运行中的任务
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		defaultProcess();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {

	}

	/**
	 * 解析并且加载站点日志配置
	 * 
	 * @param document
	 * @return 成功返回真，否则假
	 */
	private boolean loadLogNodes(Document document) {
		// 各站点在本地的监听地址
		Element root = (Element)document.getElementsByTagName("log-nodes").item(0);

		// 日志在终端在打
		boolean yes = ConfigParser.splitBoolean(root.getAttribute("console-print"), false);
		Logger.getLogConfigure().setConsolePrint(yes);

		// 取出写入目录
		String dir = XMLocal.getValue(root, "write-directory");
		boolean success = createLogRoot(dir);
		if (!success) {
			Logger.error(this, "loadLogNodes", "cannot be create %s", dir);
			return false;
		}

		// 节点地址
		NodeList nodes = root.getElementsByTagName("node");
		int size = nodes.getLength();
		if (size < 1) {
			return false;
		}

		for (int index = 0; index < size; index++) {
			Element element = (Element) nodes.item(index);
			// 解析节点名称
			String xmlName = XMLocal.getValue(element, "name");
			// 解析端口号
			String xmlPort = XMLocal.getValue(element, "port");
			int port = ConfigParser.splitPort(xmlPort, -1);

			// 解析站点日志配置
			byte family = SiteTag.translate(xmlName);
			// 如果没有定义或者是日志站点，这是错误
			if (family == 0) {
				Logger.error(this, "loadLogNodes", "cannot resolve '%s'", xmlName);
				return false;
			} else if (SiteTag.isLog(family)) {
				Logger.error(this, "loadLogNodes", "illegal type:%s", xmlName);
				return false;
			}
			// 判断端口号正确
			if (!(port > 0 && port < 0xFFFF)) {
				Logger.error(this, "loadLogNodes", "illegal port %s %s", xmlName, xmlPort);
				return false;
			}

			// 保存LOG地址
			local.addLog(new LogNode(family, port));
		}

		return true;
	}

	/**
	 * 解析并且加载站点操作事件配置
	 * 
	 * @param document
	 * @return 成功返回真，否则假
	 */
	private boolean loadTigNodes(Document document) {
		// 各站点在本地的监听地址
		Element root = (Element)document.getElementsByTagName("tig-nodes").item(0);

		// 操作事件在终端在打
		boolean yes = ConfigParser.splitBoolean(root.getAttribute("console-print"), false);
		Tigger.getTigConfigure().setConsolePrint(yes);

		// 取出写入目录
		String dir = XMLocal.getValue(root, "write-directory");
		boolean success = createTigRoot(dir);
		if (!success) {
			Logger.error(this, "loadTigNodes", "cannot be create %s", dir);
			return false;
		}

		// 节点地址
		NodeList nodes = root.getElementsByTagName("node");
		int size = nodes.getLength();
		if(size < 1) return false;

		for (int index = 0; index < size; index++) {
			Element element = (Element) nodes.item(index);
			// 解析节点名称
			String xmlName = XMLocal.getValue(element, "name");
			// 解析端口号
			String xmlPort = XMLocal.getValue(element, "port");
			int port = ConfigParser.splitPort(xmlPort, -1);

			// 解析站点操作事件配置
			byte family = SiteTag.translate(xmlName);
			// 如果没有定义或者是操作事件站点，这是错误
			if (family == 0) {
				Logger.error(this, "loadTigNodes", "cannot resolve '%s'", xmlName);
				return false;
			} else if (SiteTag.isLog(family)) {
				Logger.error(this, "loadTigNodes", "illegal type:%s", xmlName);
				return false;
			}
			// 判断端口号正确
			if (!(port > 0 && port < 0xFFFF)) {
				Logger.error(this, "loadTigNodes", "illegal port %s %s", xmlName, xmlPort);
				return false;
			}

			// 保存TIG地址
			local.addTig(new TigNode(family, port));
		}

		return true;
	}
	
	/**
	 * 解析并且加载站点操作事件配置
	 * 
	 * @param document
	 * @return 成功返回真，否则假
	 */
	private boolean loadBillNodes(Document document) {
		// 各站点在本地的监听地址
		Element root = (Element) document.getElementsByTagName("bill-nodes").item(0);

		// 操作事件在终端在打
		boolean yes = ConfigParser.splitBoolean(root.getAttribute("console-print"), false);
		Biller.getBillConfigure().setConsolePrint(yes);

		// 取出写入目录
		String dir = XMLocal.getValue(root, "write-directory");
		boolean success = createBillRoot(dir);
		if (!success) {
			Logger.error(this, "loadBillNodes", "cannot be create %s", dir);
			return false;
		}

		// 节点地址
		NodeList nodes = root.getElementsByTagName("node");
		int size = nodes.getLength();
		if(size < 1) return false;

		for (int index = 0; index < size; index++) {
			Element element = (Element) nodes.item(index);
			// 解析节点名称
			String xmlName = XMLocal.getValue(element, "name");
			// 解析端口号
			String xmlPort = XMLocal.getValue(element, "port");
			int port = ConfigParser.splitPort(xmlPort, -1);

			// 解析站点操作事件配置
			byte family = SiteTag.translate(xmlName);
			// 如果没有定义或者是操作事件站点，这是错误
			if (family == 0) {
				Logger.error(this, "loadBillNodes", "cannot resolve '%s'", xmlName);
				return false;
			} else if (SiteTag.isLog(family)) {
				Logger.error(this, "loadBillNodes", "illegal type:%s", xmlName);
				return false;
			}
			// 判断端口号正确
			if (!(port > 0 && port < 0xFFFF)) {
				Logger.error(this, "loadBillNodes", "illegal port %s %s", xmlName, xmlPort);
				return false;
			}

			// 保存BILL地址
			local.addBill(new BillNode(family, port));
		}

		return true;
	}

	/**
	 * 加载日志节点许可证
	 * 
	 * @param remote 来自远程
	 * @return 成功返回真，否则假
	 */
	@Override
	public boolean loadLicence(boolean remote) {
		// 判断签名一致，如果是拒绝就不执行
		int who = checkLicence();
		if (who == Licence.LICENCE_REFUSE) {
			return false;
		} else if (remote && who == Licence.LICENCE_IGNORE) {
			return false;
		}

		return true;
	}

	/**
	 * 加载本地配置
	 * @param filename 磁盘文件名
	 * @return 成功返回真，否则假
	 */
	private boolean loadLocal(String filename) {
		filename = ConfigParser.splitPath(filename);
		if (!Laxkit.hasFile(filename)) {
			Logger.error(this, "localLocal", "not found %s", filename);
			return false;
		}
		
		Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			Logger.error(this, "loadLocal", "cannot load %s", filename);
			return false;
		}

		//		// 日志写入目录
		//		String dirname = XMLocal.getXMLValue(document.getElementsByTagName("write-directory"));
		//		// 建立这个目录
		//		createResourcePath(dirname);

		// 解析HOME站点地址
		boolean success = splitHubSite(document);
		// 解析本地主机地址
		if (success) {
			success = splitSingleSite(local, document);
		}
		// 解析停止运行任务监听配置
		if (success) {
			success = loadShutdown(document);
		}
		// 解析FIXP安全通信配置资源
		if (success) {
			success = loadSecure(document);
		}
		// 解析自定义资源
		if (success) {
			success = loadCustom(document);
		}
		// 解析并且加载站点日志配置
		if (success) {
			success = loadLogNodes(document);
		}
		// 解析并且加载站点操作消息配置
		if (success) {
			success = loadTigNodes(document);
		}
		// 解析并且加载站点消耗资源配置
		if (success) {
			success = loadBillNodes(document);
		}
		
		// 本地加载许可证
		if (success) {
			success = loadLicence(false);
		}

		// 解析“least-disk”属性
		if (success) {
			loadTimerTasks(document);
		}

		return success;
	}

	/**
	 * 启动日志服务器
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		// 判断沙箱启动
		if (System.getSecurityManager() != null) {
			Logger.info("sandbox loaded!");
		}
		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		String filename = args[0];
		boolean success = LogLauncher.getInstance().loadLocal(filename);
		if (success) {
			success = LogLauncher.getInstance().start();
		}
		if (!success) {
			Logger.gushing();
			System.exit(0);
		}
	}

}