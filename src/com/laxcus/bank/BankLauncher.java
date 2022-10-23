/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank;

import java.io.*;

import com.laxcus.echo.invoker.custom.*;
import com.laxcus.access.diagram.*;
import com.laxcus.bank.pool.*;
import com.laxcus.launch.*;
import com.laxcus.launch.hub.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.bank.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.net.*;
import com.laxcus.visit.impl.bank.*;
import com.laxcus.xml.*;

/**
 * BANK节点启动器。 <br><br>
 * 
 * 管理ACCOUNT/HASH/GATE/ENTRANCE节点，注册到TOP节点。<br><br>
 * 
 * BANK子节点登录顺序，要求必须是某一类节点全部登录注册完成后，再一类节点再启动登录注册。顺序是：<br>
 * 1. ACCOUNT节点 <br>
 * 2. HASH节点 <br>
 * 3. GATE节点 <br>
 * 4. ENTRANCE节点。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/26/2018
 * @since laxcus 1.0
 */
public final class BankLauncher extends HubLauncher {

	/** BANK节点句柄 **/
	private static BankLauncher selfHandle = new BankLauncher();

	/** 管理员账号(超级用户) */
	private Administrator admin = new Administrator();

	/** TOP节点地址 **/
	private Node hub;

	/** 本地节点绑定地址 **/
	private BankSite local = new BankSite();

	/**
	 * 构造BANK节点启动器
	 */
	private BankLauncher() {
		super();
		// 退出JVM
		setExitVM(true);
		// 打印故障
		setPrintFault(true);
		// BANK节点监听
		setStreamInvoker(new BankStreamAdapter());
		setPacketInvoker(new BankPacketAdapter());
	}

	/**
	 * 返回BANK节点静态句柄
	 * @return BankLauncher实例
	 */
	public static BankLauncher getInstance() {
		return BankLauncher.selfHandle;
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
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public BankCommandPool getCommandPool() {
		return BankCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public BankInvokerPool getInvokerPool() {
		return BankInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return BankCustomTrustor.getInstance();
	}
	
	/**
	 * 判断是注册站点地址
	 * @param e 节点实例
	 * @return 返回真或者假
	 */
	public boolean isHub(Node e) {
		return Laxkit.compareTo(hub, e) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getHub()
	 */
	@Override
	public Node getHub() {
		return hub;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#setHub(com.laxcus.site.Node)
	 */
	@Override
	public void setHub(Node e) {
		Laxkit.nullabled(e);

		hub = e;
	}

	/**
	 * 返回TOP节点主机地址
	 * @return SiteHost实例
	 */
	public SiteHost getHubHost() {
		return hub.getHost();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getSite()
	 */
	@Override
	public Site getSite() {
		return local;
	}

	/**
	 * 设置管理员账号
	 * @param username 用户签名（SHA256）
	 * @param password 用户密码（SHA512）
	 * @param members 同时在线登录节点数目
	 */
	private void setAdministrator(Siger username, SHA512Hash password, int members) {
		admin.setUsername(username);
		admin.setPassword(password);
		admin.setMembers(members);
	}

	//	/**
	//	 * 设置管理员账号
	//	 * @param hexuser 16进制用户名（SHA256）
	//	 * @param hexpwd 16进制密码（SHA512）
	//	 * @param members 同时在线登录节点数目
	//	 */
	//	private void setAdministrator(String hexuser, String hexpwd, int members) {
	//		admin.setHexUsername(hexuser);
	//		admin.setHexPassword(hexpwd);
	//		admin.setMembers(members);
	//	}

	/**
	 * 返回管理员账号
	 * @return
	 */
	public Administrator getAdministrator() {
		return admin;
	}

	/**
	 * 启动工作节点管理池服务
	 * @return
	 */
	private boolean loadPool() {
		Logger.info(this, "loadPool", "into...");

		VirtualPool[] pools = new VirtualPool[] {
				// INVOKE/PRODUCE管理池
				BankInvokerPool.getInstance(),
				BankCommandPool.getInstance(),
				// 串行命令管理池
				SerialCommandPool.getInstance(),
				// COMMAND/INVOKER配置资源
				CustomClassPool.getInstance(),
				// 站点管理池
				LogOnBankPool.getInstance(), AccountOnBankPool.getInstance(),
				HashOnBankPool.getInstance(), EntranceOnBankPool.getInstance(),
				GateOnBankPool.getInstance(), WatchOnBankPool.getInstance(),
				// 监视站点管理池
				MonitorOnBankPool.getInstance() };
		// 启动全部管理池
		return startAllPools(pools);
	}

	/**
	 * 停止管理池
	 */
	private void stopPool() {
		Logger.info(this, "stopPool", "into...");
		VirtualPool[] pools = new VirtualPool[] {
				// 工作站点管理池
				WatchOnBankPool.getInstance(),
				EntranceOnBankPool.getInstance(), GateOnBankPool.getInstance(),
				HashOnBankPool.getInstance(), AccountOnBankPool.getInstance(),
				LogOnBankPool.getInstance(),
				
				// COMMAND/INVOKER配置资源
				CustomClassPool.getInstance(),
				// 串行命令管理池
				SerialCommandPool.getInstance(),
				// INVOKE/PRODUCE管理池
				BankCommandPool.getInstance(), BankInvokerPool.getInstance(),
				// 监视站点管理池
				MonitorOnBankPool.getInstance() };
		// 关闭全部
		stopAllPools(pools);
	}

	/**
	 * 停止本地日志服务
	 */
	private void stopLog() {
		Logger.stopService();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		//1. 预加载操作（从TOP站点获得日志服务器节点）
		boolean success = preload(hub);
		Logger.note(this, "init", success, "preload");
		// 3. 启动FIXP服务器
		if (success) {
			Class<?>[] clazzs = { HitVisitOnBank.class, HubVisitOnBank.class, CommandVisitOnBank.class };
			success = loadSingleListen(clazzs, local.getNode());
		}
		Logger.note(this, "init", success, "load fixp listen");
		// 4. 启动管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", success, "load pool");
		// 5. 检测“local.xml”列表中指定的监视站点，其中任何一个站点成为“管理站点”，当前站点即为“监视站点”。
		if (success) {
			// 发现管理站点
			Node manager = consult();
			// 设置管理站点地址
			setManager(manager);
		}
		Logger.note(this, "init", success, "i am '%s'", (isManager() ? "run site" : "backup site"));
		// 6. 注册到服务器
		if (success) {
			success = login();
		}
		Logger.note(this, "init", success, "login");

		// 不成功取消操作
		if (!success) {
			// 停止全部管理池
			stopPool();
			// 停止FIXP监听
			stopListen();
			// 停止日志服务
			stopLog();
		}

		return success;
	}

	/**
	 * 重置BANK站点参数
	 */
	private Site reset() {
//		List<Refer> array = StaffOnBankPool.getInstance().getRefers();

//		Logger.debug(this, "reset", "refer size: %d", array.size());

		super.lockSingle();
		try {
			
			// 设置管理站点标识
			local.setManager(isManager());
//			// 只有管理站点才设置参数
//			if (local.isManager()) {
//				for(Refer refer : array) {
//					// 建立账号签名
//					Siger siger = refer.getUsername();
//					local.create(siger);
//					// 建立表空间
//					for (Space space : refer.getTables()) {
//						Logger.debug(this, "reset", "add table:%s", space);
//						local.addSpace(siger, space);
//					}
//				}
//			}

//			Logger.debug(this, "reset", "member size: %d", local.size());
//			Logger.debug(this, "reset", "space size: %d", local.getSpaces().size());

			return local.duplicate();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return null;
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
		Site site = reset();
		boolean success = (site != null);
		if (success) {
			success = login(site, hub);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#register()
	 */
	@Override
	protected void register() {
		Site site = reset();
		register(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 注销
		logout();
		// 停止全部管理池
		stopPool();
		// 停止FIXP监听(必须在全部管理池停止之后)
		stopListen();
		// 停止日志服务
		stopLog();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");

		// 当前站点是管理站点，与TOP保持握手；否则，监视运行的BANK站点
		while (!super.isInterrupted()) {
			if (isManager()) {
				lookout();
			} else {
				lookin();
			}
		}

		Logger.info(this, "process", "exit");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {

	}

	/**
	 * BANK站点进入“管理站点”状态时，与TOP保持定时激活状态
	 */
	private void lookout() {
		// 启动资源管理线程
		FaultOnBankPool.getInstance().start();

		// 与TOP保持激活，直到要求站点退出
		super.defaultProcess();

		// 停止资源管理线程
		FaultOnBankPool.getInstance().stop();
		while(FaultOnBankPool.getInstance().isRunning()) {
			delay(200);
		}
	}

	/**
	 * BANK站点进入“监视站点”状态
	 */
	private void lookin() {
		// 启动备份管理池
		BankMonitor.getInstance().setHubLauncher(this);
		BankMonitor.getInstance().setCommandPool(BankCommandPool.getInstance());
		BankMonitor.getInstance().start();

		// 与TOP保持激活
		long endtime = refreshEndTime();
		hello();

		// 定时2分钟检查一次
		final long scheduleTimeout = 2 * 60 * 1000L;

		// 在与TOP保持激活的同时，判断BANK监视器退出
		while (!isInterrupted()) {
			if (BankMonitor.getInstance().isInterrupted()) {
				break;
			}
			
			// 达到延时注册时间后，或者要求重新注册时，注册到上级站点
			if (registerTimer.isTouch() || isCheckin()) {
				// 恢复重新注册为假
				setCheckin(false);
				// 刷新
				registerTimer.refresh();

				// 判断监视器节点在登录状态时，才启动注册
				if (BankMonitor.getInstance().isLogined()) {
					register();
				}

				//				// 必须确认在登录状态，才能重新注册；否则，启动FixpStreamClient注册！
				//				if (isLogined()) {
				//					register();
				//				} else {
				//					kiss(false); // 启动FixpStreamClient注册
				//				}
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

		// 关闭备份管理池，并且等待它退出
		BankMonitor.getInstance().stop();
		while (BankMonitor.getInstance().isRunning()) {
			delay(200);
		}
	}

	/**
	 * 加载数据库管理员账号。
	 * 管理员账号在每个GATE存在。
	 * 
	 * 账号有两种状态：
	 * 1. 16进制字符串（用户名和密码都是）
	 * 2. 以上不成立，是明文
	 * 
	 * @param filename 磁盘文件名
	 * @return 成功返回真，否则假
	 */
	private boolean loadAdministrator(String filename) {
		filename = ConfigParser.splitPath(filename);
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return false;
		}

		String input = XMLocal.getXMLValue(document.getElementsByTagName("members"));
		int members = ConfigParser.splitInteger(input, 1);

		// 管理员账号的用户名(SHA256)和密码(SHA512)
		String username = XMLocal.getXMLValue(document.getElementsByTagName("username"));
		String password = XMLocal.getXMLValue(document.getElementsByTagName("password"));
		
		Logger.debug(this, "loadAdministrator", "%s # %s", username, password);

		// 两种可能：<1> 数字签名，<2>明文字符
		if (Siger.validate(username) && SHA512Hash.validate(password)) {
			Siger siger = new Siger(username);
			SHA512Hash pwd = new SHA512Hash(password);
			setAdministrator(siger, pwd, members);
		} else {
			Siger siger = SHAUser.doUsername(username);
			SHA512Hash pwd = SHAUser.doPassword(password);
			setAdministrator(siger, pwd, members);
		}

		return true;
	}

	/**
	 * 加载BANK节点许可证
	 * 
	 * @param remote 来自远程
	 * @return 返回真或者假
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

		// 生成许可证文件，判断许可证存在
		File file = buildLicenceFile();
		if (file == null) {
			// 如果是远程操作，返回假（失败），否则是真（正确）！
			return (remote ? false : true);
		}

		// 取出“sites-limit”对应的XML成员
		org.w3c.dom.Element limit = fatchLimitElement(file, "bank");
		boolean success = (limit != null);
		// 设置注册节点限制
		if (success) {
			success = setMaxMember(limit, "account", AccountOnBankPool.getInstance());
		}
		if (success) {
			success = setMaxMember(limit, "hash", HashOnBankPool.getInstance());
		}
		if (success) {
			success = setMaxMember(limit, "gate", GateOnBankPool.getInstance());
		}
		if (success) {
			success = setMaxMember(limit, "entrance", EntranceOnBankPool.getInstance());
		}

		return success;
	}
	
	/**
	 * 加载并且设置本地管理配置
	 * @param filename
	 * @return
	 */
	private boolean loadLocal(String filename) {
		filename = ConfigParser.splitPath(filename);
		if (!Laxkit.hasFile(filename)) {
			Logger.error(this, "localLocal", "not found %s", filename);
			return false;
		}
		
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			Logger.error(this, "loadLocal", "cannot resolve %s", filename);
			return false;
		}

		// 解析TOP站点服务器地址
		boolean success = splitHubSite(document);
		if (!success) {
			Logger.error(this, "loadLocal", "cannot be find top site");
			return false;
		}
		// 解析BANK节点的本地绑定地址
		success = splitSingleSite(local, document);
		// 解析和设置回显配置
		if (success) {
			success = splitEcho(document);
		}
		// 解析并且设置FIXP监视器安全配置
		if (success) {
			success = loadSecure(document);
		}
		// 解析自定义资源
		if (success) {
			success = loadCustom(document);
		}
		// 解析备用BANK节点，它备份和监视BANK运行节点。
		if (success) {
			success = loadMonitor(document);
			if (!success) Logger.error(this, "loadLocal", "cannot load monitor");
		}
		// 加载许可证，本地执行
		if (success) {
			success = loadLicence(false);
			if (!success) Logger.error(this, "localLocal", "load licence, failed!");
		}
		
		if (!success) return false;

		// WATCH站点账号
		String value = XMLocal.getXMLValue(document.getElementsByTagName("watch-account"));
		if (value != null) {
			WatchOnBankPool.getInstance().setFile(value);
		}

		// 数据库管理员账号目录
		value = XMLocal.getXMLValue(document.getElementsByTagName("dba-account"));
		if (!loadAdministrator(value)) {
			Logger.error(this, "loadLocal", "cannot resolve dba configure");
			return false;
		}

		// 全部资源管理池
		HubPool[] pools = new HubPool[] { MonitorOnBankPool.getInstance(),
				LogOnBankPool.getInstance(), WatchOnBankPool.getInstance(),
				AccountOnBankPool.getInstance(), GateOnBankPool.getInstance(),
				EntranceOnBankPool.getInstance(), HashOnBankPool.getInstance() };

		// 管理池激活时间（休息一个时间后重新触发，检查注册站点超时）
		setSleepTimeout(pools, document);
		// 管理池内的子站点超时时间
		setActiveTimeout(pools, document);
		// 管理池内的子站点超时删除时间
		setDeleteTimeout(pools, document);

		// 解析“least-disk”属性
		if (success) {
			loadTimerTasks(document);
		}
		
		// 加载Logger/Tigger记录
		if (success) {
			success = loadLogResourceWithRemote(filename);
		}
		
		return success;
	}

	/**
	 * BANK启动接口
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		if (System.getSecurityManager() != null) {
			Logger.info("BankLauncher.main, sandbox loaded!");
		}
		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		String filename = args[0];
		boolean success = BankLauncher.getInstance().loadLocal(filename);
		Logger.note("BankLauncher.main, load local", success);
		if (success) {
			success = BankLauncher.getInstance().start();
			Logger.note("BankLauncher.main, start service", success);
		}

		if (!success) {
			Logger.gushing();
			System.exit(0);
		}
	}

}