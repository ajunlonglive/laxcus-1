/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account;

import java.io.*;

import org.w3c.dom.*;

import com.laxcus.account.dict.*;
import com.laxcus.account.pool.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.launch.*;
import com.laxcus.launch.licence.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.pool.schedule.*;
import com.laxcus.site.*;
import com.laxcus.site.account.*;
import com.laxcus.util.*;
import com.laxcus.util.cyber.*;
import com.laxcus.visit.impl.account.*;
import com.laxcus.xml.*;

/**
 * 账号站点启动器。<br>
 * 账号站点保存用户资源，包括：账号、分布任务组件、码位计算器、快捷组件。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class AccountLauncher extends MemberLauncher {

	/** 启动器句柄 **/
	private static AccountLauncher selfHandle = new AccountLauncher();

	/** 当前账号站点配置 */
	private AccountSite local = new AccountSite();

	/**
	 * 构造账号站点启动器
	 */
	private AccountLauncher() {
		super();
		// 打印故障
		setPrintFault(true);
		// 账号站点监听
		setPacketInvoker(new AccountPacketAdapter());
		setStreamInvoker(new AccountStreamAdapter());
		// 退出JVM
		setExitVM(true);
	}

	/**
	 * 返回账号站点静态句柄
	 * @return AccountLauncher实例
	 */
	public static AccountLauncher getInstance() {
		return AccountLauncher.selfHandle;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#createMoment()
	 */
	@Override
	public Moment createMoment() {
		Moment moment = super.createMoment();
		MemberCyber m = getMemberCyber();
		moment.setMember(new PersonStamp(m.getPersons(), m.getThreshold(),
				StaffOnAccountPool.getInstance().size()));
		return moment;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getPublicListener()
	 */
	@Override
	public com.laxcus.site.Node getPublicListener() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return AccountCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return AccountInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return AccountCustomTrustor.getInstance();
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
	 * 设置编号
	 * @param who
	 */
	public void setNo(int who) {
		Logger.info(this, "setNo", "current site no:%d", who);
		local.setNo(who);
	}

	/**
	 * 返回编号
	 * @return
	 */
	public int getNo() {
		return local.getNo();
	}

	/**
	 * 重置参数
	 * @return
	 */
	private Site reset() {
		Moment moment = createMoment();

		// 锁定
		super.lockSingle();
		try {
			local.setMoment(moment); // 瞬时记录
			local.setMembers(StaffOnAccountPool.getInstance().size());
			local.setTasks(TaskOnAccountPool.getInstance().size());
			// 输出副本
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
		
		// 生成地址
		Site site = reset();
		boolean success = (site != null);
		if (success) {			
			success = super.login(site);
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
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// 1. 启动预处理
		boolean success = preload();
		Logger.note(this, "init", "preload ", success);
		// 2. 启动FIXP服务
		if (success) {
			Class<?>[] clazzs = { CommandVisitOnAccount.class };
			success = loadSingleListen(clazzs, local.getNode());
		}
		Logger.note(this, "init", "load listen", success);
		// 3. 启动管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", "load pool", success);
		// 4. 注册
		if (success) {
			success = login();
		}
		Logger.note(this, "init", "login", success);
		// 5. 取当前站点编号
		if (success) {
			success = applySerial();
		}

		// 成功，重新注册。不成功，退出！
		if (success) {
			register();
			// 注册成功后向BANK推送节点
			StaffOnAccountPool.getInstance().pushRegisterMember();
		} else {
			stopPool();
			stopListen();
			stopLog();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into ...");

		// 进入循环
		defaultProcess();

		Logger.info(this, "process", "exit ...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 注销
		logout();
		// 停止管理池
		stopPool();
		// 停止监听
		stopListen();
		// 关闭日志
		stopLog();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {

	}

	/**
	 * 向BANK站点申请当前站点的机器编号
	 * @return 成功返回真，否则假
	 */
	private boolean applySerial() {
		TakeSiteSerial cmd = new TakeSiteSerial(getFamily());
		TakeSiteSerialHook hook = new TakeSiteSerialHook();
		ShiftTakeSiteSerial shift = new ShiftTakeSiteSerial(cmd, hook);
		shift.setFast(true); // 跳过检查，BANK优先处理

		// 交给命令管理池
		boolean success = getCommandPool().press(shift);
		if (!success) {
			Logger.error(this, "applySerial", "cannot be admit!");
			return false;
		}
		// 钩子等待，直到被唤醒
		hook.await();

		// 返回处理结果
		TakeSiteSerialProduct product = hook.getProduct();
		// 判断成功
		success = (product != null && product.getNo() > -1);
		if (success) {
			setNo(product.getNo());
		}

		Logger.note(this, "applySerial", success, "current site no:%d", getNo());

		return success;
	}

	/**
	 * 启动管理池
	 * @return 成功返回真，否则假
	 */
	private boolean loadPool() {
		Logger.info(this, "loadPool", "into...");		
		// 启动管理池
		VirtualPool[] pools = new VirtualPool[] {
				// 分布组件资源管理池
				TaskOnAccountPool.getInstance(),
				// 软件包管理池
				WareOnAccountPool.getInstance(),
				// 账号资源管理池
				StaffOnAccountPool.getInstance(),
				// 命令/调用器管理池
				AccountCommandPool.getInstance() ,
				AccountInvokerPool.getInstance(),
				// 自定义COMMAND/INVOKER资源
				CustomClassPool.getInstance(),
				// 串行管理池
				SerialSchedulePool.getInstance() };
		// 启动全部
		return startAllPools(pools);
	}

	/**
	 * 停止管理池
	 */
	private void stopPool() {
		Logger.info(this, "stopPool", "...");
		VirtualPool[] pools = new VirtualPool[] {
				// 串行管理池
				SerialSchedulePool.getInstance(),
				// 自定义类资源管理池
				CustomClassPool.getInstance(),
				// 命令/调用器管理池
				AccountCommandPool.getInstance(),
				AccountInvokerPool.getInstance(),
				// 分布组件资源管理池
				TaskOnAccountPool.getInstance(),
				// 软件包管理池
				WareOnAccountPool.getInstance(),
				// 账号资源管理池
				StaffOnAccountPool.getInstance(), };
		// 关闭全部
		stopAllPools(pools);
	}
	
	/**
	 * 读单元
	 * @param root
	 * @param tag
	 * @return
	 */
	private Element readElement(Element root, String tag) {
		NodeList list = root.getElementsByTagName(tag);
		if (list.getLength() != 1) {
			return null;
		}
		return (Element) list.item(0);
	}

	/**
	 * 分析“resources”块属性
	 * @param document XML档案
	 */
	private boolean loadResources(Document document) {
		NodeList list = document.getElementsByTagName("resources");
		Element root = (Element) list.item(0);

		Element tasks = readElement(root, "tasks"); // (Element) root.getElementsByTagName("tasks").item(0);
		if (tasks == null) {
			Logger.error(this, "loadResources", "not found \'tasks\' element");
			return false;
		}

		// 分布任务组件参数（组件目录和磁盘扫描间隔时间）
		String input = XMLocal.getValue(tasks, "directory");
		boolean success = TaskOnAccountPool.getInstance().setRoot(input);
		if (!success) {
			Logger.error(this, "loadResources", "cannot be set '%s'", input);
			return false;
		}
		// 保存目录，定时检测，防止磁盘空间不足
		addDeviceDirectory(input);
		// 检测间隔
		input = tasks.getAttribute("interval");
		long ms = ConfigParser.splitTime(input, 300000); // 默认5分钟
		TaskOnAccountPool.getInstance().setInterval(ms);

		// 软件目录
//		Element wares = (Element) root.getElementsByTagName("wares").item(0);
		Element wares = readElement(root, "wares");
		if (wares == null) {
			Logger.error(this, "loadResources", "not found \'wares\' element");
			return false;
		}
		input = XMLocal.getValue(wares, "directory");
		success = WareOnAccountPool.getInstance().setRoot(input);
		if (!success) {
			Logger.error(this, "loadResources", "cannot be set '%s'", input);
			return false;
		}
		// 保存目录，定时检测，防止磁盘空间不足
		addDeviceDirectory(input);


		// 账号目录，支持任意多个
//		Element accounts = (Element)root.getElementsByTagName("accounts").item(0);
		Element accounts = readElement(root, "accounts");
		if (accounts == null) {
			Logger.error(this, "loadResources", "not found \'accounts\' element");
			return false;
		}
		String[] roots = XMLocal.getValues(accounts, "directory");
		if (roots.length == 0) {
			Logger.error(this, "loadResources", "not found account directory!");
			return false;
		}
		for (int i = 0; i < roots.length; i++) {
			StaffOnAccountPool.getInstance().addManager(roots[i]);
			// 保存目录，定时检测，防止磁盘空间不足
			addDeviceDirectory(roots[i]);
		}

		// 账号超时时间
		input = accounts.getAttribute("account-timeout");
		ms = ConfigParser.splitTime(input, AccountManager.getTimeout()); // 默认10分钟
		AccountManager.setTimeout(ms);

		// 一个账号单元规定空间
		input = accounts.getAttribute("unit-size");
		int size = (int) ConfigParser.splitLongCapacity(input, AccountManager.getAccountSize()); //默认每个账号单元尺寸128K
		AccountManager.setAccountSize(size);

		// 账号文件块尺寸
		input = accounts.getAttribute("block-size");
		size = (int) ConfigParser.splitLongCapacity(input, AccountManager.getBlockSize()); // 默认每个账号文件32M
		AccountManager.setBlockSize(size);

		// 成功 ！
		return true;
	}
	
	/**
	 * 加载许可证
	 * 
	 * @param remote 远程加载许可证
	 * @return 成功返回真，否则假
	 */
	@Override
	public boolean loadLicence(boolean remote) {
		int who = checkLicence();
		if (who == Licence.LICENCE_REFUSE) {
			return false;
		} else if (remote && who == Licence.LICENCE_IGNORE) {
			return false;
		}

		// 生成许可证文件，判断文件存在
		File file = buildLicenceFile();
		if (file == null) {
			// 如果是远程操作，返回假（失败），否则是真（正确）！
			return (remote ? false : true);
		}

		// 根实例
		org.w3c.dom.Element element = LicenceRegister.readXMLElement(file, "account-configure");
		if (element == null) {
			Logger.error(this, "loadLicence", "cannot resolve \"account\"!");
			return false;
		}

		// "max-users"是最大用户数目
		String input = XMLocal.getValue(element, "max-users");
		int value = ConfigParser.splitInteger(input, AccountConfig.getMaxUsers());
		AccountConfig.setMaxUsers(value);

		// "max-members"用户并行成员数目
		input = XMLocal.getValue(element, "max-members");
		value = ConfigParser.splitInteger(input, AccountConfig.getMaxMembers());
		AccountConfig.setMaxMembers(value);
		
		// "max-jobs"用户并行成员数目
		input = XMLocal.getValue(element, "max-jobs");
		value = ConfigParser.splitInteger(input, AccountConfig.getMaxJobs());
		AccountConfig.setMaxJobs(value);

		Logger.info(this, "loadLicence", "max users: %d, max members: %d, max jobs: %d",
				AccountConfig.getMaxUsers(), AccountConfig.getMaxMembers(), AccountConfig.getMaxJobs());

		return true;
	}

	/**
	 * 加载本地资源配置
	 * @param filename
	 * @return
	 */
	private boolean loadLocal(String filename) {
		filename = ConfigParser.splitPath(filename);
		if (!Laxkit.hasFile(filename)) {
			Logger.error(this, "localLocal", "not found %s", filename);
			return false;
		}
		
		Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return false;
		}
		
		// 解析成员限值
		splitMemberCyber(document);

		// 解析和设置TOP站点地址
		boolean success = splitHubSite(document);
		// 解析和设置本地站点地址
		if (success) {
			success = splitSingleSite(local, document);
		}
		// 解析回显配置
		if (success) {
			success = splitEcho(document);
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
		// 解析“resources”属性
		if(success) {
			success = loadResources(document);
		}
		// 加载许可证服务
		if (success) {
			success = loadLicence(false);
		}
		
		// 解析“least-disk”属性
		if (success) {
			loadTimerTasks(document);
		}
		
		// 加载日志/追踪记录
		if (success) {
			success = loadLogResourceWithRemote(filename);
		}

		//		// 加载日志目录配置
		//		if (success) {
		//			success = Logger.loadXML(filename);
		//			if (success) {
		//				loadLogDeviceDirectory();
		//			}
		//		}
		//		// 加载追踪服务配置
		//		if (success) {
		//			success = Tigger.loadXML(filename);
		//			if (success) {
		//				loadTigDeviceDirectory();
		//			}
		//		}
		
		return success;
	}

	/**
	 * 启动账号进程
	 * @param args 参数
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		// 判断安全管理器已经加载，沙箱依托它实现安全管理
		if (System.getSecurityManager() != null) {
			Logger.info("AccountLauncher.main, sandbox loaded!");
		}
		// 将LAXCUS链接库目录下的系统链接库文件（WINDOWS/LINUX）加载进JVM
		JNILoader.init();

		// 加载配置文件
		String filename = args[0];
		boolean success = AccountLauncher.getInstance().loadLocal(filename);
		Logger.note("AccountLauncher.main, load local", success);
		// 启动线程
		if (success) {
			success = AccountLauncher.getInstance().start();
			Logger.note("AccountLauncher.main, start service", success);
		}
		if (!success) {
			Logger.gushing();
			System.exit(0);
		}
	}

}