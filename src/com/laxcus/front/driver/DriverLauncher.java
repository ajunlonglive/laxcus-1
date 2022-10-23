/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver;

import java.util.regex.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.front.driver.pool.*;
import com.laxcus.front.pool.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.mission.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.hash.*;
import com.laxcus.xml.*;

/**
 * FRONT.DRIVER启动器。<br><br>
 * 
 * 驱动程序是一个接口服务，提供远程访问LAXCUS集群的能力。它需要用户绑定到自己的API中，与用户应用启动。<br>
 * 启动驱动器是：DriverLauncher.getInstance().launch(String params); 停止命令是：DriverLauncher.getInstance().stop(); <br><br>
 * 
 * launch方法的输入参数格式由多个键值对组成，键忽略大小写，之间用分号分隔，键值之间用等号分隔。即：[KEY]=[VALUE];[KEY]=[VALUE];
 * 
 * @author scott.liang
 * @version 1.12 7/23/2015
 * @since laxcus 1.0
 */
public final class DriverLauncher extends FrontLauncher implements LogPrinter {

	/** 驱动程序启动句柄 **/
	private static DriverLauncher selfHandle = new DriverLauncher();

	/** 用户自定义日志存储接口 **/
	private LogPrinter logPrinter;

	/** 驱动任务生成器 **/
	private DriverMissionCreator creator = new DriverMissionCreator();

	/**
	 * 返回FRONT站点的驱动句柄
	 * @return 驱动实例
	 */
	public static DriverLauncher getInstance() {
		return DriverLauncher.selfHandle;
	}

	/**
	 * 构造默认和私有的FRONT驱动程序启动器
	 */
	private DriverLauncher() {
		// 不关闭JVM，不打印日志
		super(RankTag.DRIVER, false, false, null);
		// 定义控制台打印和本地的日志打印接口，二者配合，输出日志
		setConsolePrint(false);
		Logger.setLogPrinter(this);
		// 驱动程序不发送日志
		Logger.setTransferMode(LogConfigure.NOT_SEND);
	}

	/**
	 * 设置控制台打印日志
	 * @param b 控制台打印日志
	 */
	public void setConsolePrint(boolean b) {
		Logger.setConsolePrint(b);
	}
	
	/**
	 * 返回驱动任务生成器
	 * @return MissionCreator实例
	 */
	public DriverMissionCreator getMissionCreator() {
		return creator;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return DriverCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return DriverInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return DriverCustomTrustor.getInstance();
	}

	/**
	 * 注册到站点
	 * @return 成功返回真，否则假
	 */
	private boolean launch() {
		// 获得服务器地址
		Node hub = getHub();
		int who = login(hub, false, null);
		boolean success = FrontEntryFlag.isSuccessful(who);

		return success;
	}

	/**
	 * 启动管理池
	 * @return 返回真或者假
	 */
	private boolean loadPool() {
//		// 延时
//		PutTaskPool.getInstance().setSleepTime(30);
//		EndTaskPool.getInstance().setSleepTime(30);

		// 设置事件监听器
		PutTaskPool.getInstance().setTaskListener(this);
		EndTaskPool.getInstance().setTaskListener(this);
		NearTaskPool.getInstance().setTaskListener(this);
		
		// CONDUCT.PUT / ESTABLISH.END / CONTACT.NEAR阶段资源代理
		PutTaskPool.getInstance().setPutTrustor(getStaffPool());
		EndTaskPool.getInstance().setEndTrustor(getStaffPool());
		NearTaskPool.getInstance().setNearTrustor(getStaffPool());

		VirtualPool[] pools = new VirtualPool[] { GuideTaskPool.getInstance(),
				PutTaskPool.getInstance(), EndTaskPool.getInstance(),
				NearTaskPool.getInstance(),
				TubPool.getInstance(),
				DriverInvokerPool.getInstance(), DriverCommandPool.getInstance(),
				StaffOnDriverPool.getInstance(),
				AuthroizerGateOnFrontPool.getInstance(), CallOnFrontPool.getInstance() };

		// 启动它们
		return startAllPools(pools);
	}

	/**
	 * 停止管理池
	 */
	private void stopPool() {
		VirtualPool[] pools = new VirtualPool[] {
				DriverCommandPool.getInstance(), DriverInvokerPool.getInstance(), 
				CallOnFrontPool.getInstance(), AuthroizerGateOnFrontPool.getInstance(),
				StaffOnDriverPool.getInstance(), 
				TubPool.getInstance(),
				PutTaskPool.getInstance(), EndTaskPool.getInstance(), 
				NearTaskPool.getInstance(), GuideTaskPool.getInstance() };
		stopAllPools(pools);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isDriver()
	 */
	@Override
	public boolean isDriver() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isConsole()
	 */
	@Override
	public boolean isConsole() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isTerminal()
	 */
	@Override
	public boolean isTerminal() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isEdge()
	 */
	@Override
	public boolean isEdge() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isDesktop()
	 */
	@Override
	public boolean isDesktop() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isApplication()
	 */
	@Override
	public boolean isApplication() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#confirm(int)
	 */
	@Override
	public boolean confirm(int no) {
		return false;
	}

	/**
	 * 忽略这个方法，返回假
	 * @see com.laxcus.util.tip.TipPrinter#confirm(int, java.lang.Object[])
	 */
	@Override
	public boolean confirm(int no, Object... params) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.log.client.LogPrinter#print(java.lang.String)
	 */
	@Override
	public void print(String e) {
		// 如果用户定义了日志转发接口，将输出给用户的日志接口。否则将忽略
		if(logPrinter != null) {
			logPrinter.print(e);
		}
	}

	/**
	 * 设置打印日志接口。<br>
	 * 这个接口由使用驱动程序的用户提供。如果用户不设置，将忽略所有日志。
	 * 
	 * @param e LogPrinter实例
	 */
	public void setLogPrinter(LogPrinter e) {
		logPrinter = e;
	}

	/**
	 * 返回打印日志接口。
	 * @return LogPrinter实例
	 */
	public LogPrinter getLogPrinter() {
		return logPrinter;
	}

	/**
	 * 解析明文格式的用户账号
	 * @param input 输入语句
	 * @return User实例
	 */
	private User splitTextUser(String input) {
		if (input == null || input.isEmpty()) {
			return null;
		}
		final String regex = "^\\s*(\\S+)\\/(\\S+)\\s*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，返回空
		if(!matcher.matches()) {
			return null;
		}
		// 取出明文
		String username = matcher.group(1);
		String password = matcher.group(2);
		return new User(username, password);
	}

	/**
	 * 解析SHA格式的用户账号
	 * @param input 输入语句
	 * @return User实例
	 */
	private User splitSHAUser(String input) {
		if (input == null || input.isEmpty()) {
			return null;
		}
		String[] items = input.split("/");
		// 两组数据
		boolean success = (items.length == 2);
		// 判断是文本格式SHA256编码
		if (success) {
			success = (Siger.validate(items[0]) && SHA512Hash.validate(items[1]));
		}
		if (!success) {
			return null;
		}
		// 生成签名
		Siger username = new Siger(items[0]);
		SHA512Hash pwd = new SHA512Hash(items[1]);
		return new User(username, pwd);
	}

	/**
	 * 解析参数，做为系统键值对保存到内存中
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	private boolean splitParams(String input) {
		final String regex = "^\\s*([\\w\\W]+?)\\s*\\=\\s*([\\w\\W]+?)\\s*$";
		Pattern pattern = Pattern.compile(regex);

		// 分割参数
		String[] cmds = input.split(";");
		for (String item : cmds) {
			Matcher matcher = pattern.matcher(item);
			// 必须严格遵守格式规定，否则是错误！
			if (!matcher.matches()) {
				return false;
			}
			// 检查参数，设置为系统属性
			String key = matcher.group(1);
			String value = matcher.group(2);
			System.setProperty(key, value);
		}
		return true;
	}

	/**
	 * 提取“log-file”参数，加载本地日志配置
	 * @return 成功返回真，否则假
	 */
	private boolean loadLog() {
		String filename = System.getProperty(DriverMark.LOG_FILE);
		if (filename == null) {
			return true;
		}

		// 加载日志并且启动
		boolean success = loadLogResourceWithLocal(filename);

		//		filename = ConfigParser.splitPath(filename);
		//		// 启动日志服务
		//		boolean success = Logger.loadXML(filename);
		//		if (success) {
		//			success = Logger.loadService();
		//			if (success) {
		//				loadLogDeviceDirectory();
		//			}
		//		}
		//		if (success) {
		//			success = Tigger.loadXML(filename);
		//			if (success) {
		//				success = Tigger.loadService();
		//				if (success) {
		//					loadTigDeviceDirectory();
		//				}
		//			}
		//		}

		return success;
	}

	/**
	 * 解析驱动账号参数
	 * @return 返回真或者假
	 */
	private boolean splitAccount() {
		// DRIVER账号（明文或者SHA256码）
		String value = System.getProperty(DriverMark.ACCOUNT);
		if (value != null) {
			User user = splitTextUser(value);
			if (user != null) {
				local.setUser(user.getUsername(), user.getPassword()); // 明文账号
			}
		}
		if (local.getUser() == null) {
			value = System.getProperty(DriverMark.SHA_ACCOUNT);
			User user = splitSHAUser(value);
			if (user != null) {
				local.setUser(user.getUsername(), user.getPassword()); // 密文账号
			}
		}

		// 判断账号有效
		return local.getUser() != null;
	}
	
	/**
	 * 解析一些私有参数
	 * @param document
	 */
	private void splitPrivate(org.w3c.dom.Document document) {
		// 命令模式
		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);
		String input = XMLocal.getValue(element, SiteMark.COMMAND_MODE);
		setMemory(input);
		// 命令超时
		input = XMLocal.getValue(element, SiteMark.COMMAND_TIMEOUT);
		setCommandTimeout(input);
		
		// 内网节点检测NAT地址的时间间隔
		input = XMLocal.getValue(element, SiteMark.POCK_INTERVAL);
		setPockTimeout(input);
		
		// CALL节点检查间隔时间
		input = XMLocal.getValue(element, FrontMark.CALLSITE_CHECK_INTERVAL);
		long interval = ConfigParser.splitTime(input, getStaffPool().getCheckInterval());
		getStaffPool().setCheckInterval(interval);		
	}

	/**
	 * 解析配置参数
	 * @return 成功返回真，否则假
	 */
	private boolean splitConfig() {
		String filename = System.getProperty(DriverMark.CONF_FILE);
		if (filename == null) {
			Logger.error(this, "splitConfig", "cannot be find config!");
			return false;
		}

		// 解析路径，转换符号参数
		filename = ConfigParser.splitPath(filename);

		// 读取XML文档参数
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			Logger.error(this, "splitConfig", "cannot be resolve config!");
			return false;
		}

		// 解析私有参数
		splitPrivate(document);
		
		// 解析边缘容器监听
		splitTubListen(document);

		// 解析ENTRANCE服务器地址
		boolean success = splitHubSite(document);
		// 解析参数
		if (success) {
			success = splitSingleSite(local, document);
		}
		// 解析和设置回显配置
		if (success) {
			success = splitEcho(document);
		}
		// 生成RSA密钥令牌
		if (success) {
			success = createDefaultSecureToken(document);
		}
		// 加载自定义配置
		if (success) {
			success = loadCustom(document);
		}
		// 解析分布任务组件目录
		if (success) {
			success = splitTaskPool(document);
		}
		// 解析和设置边缘容器管理池目录
		if (success) {
			splitTubPool(document);
		}

		// 解析“<least interval="60s"> <memory> 20% </memory> <disk> 2096mb </disk> </least>”属性
		if (success) {
			loadTimerTasks(document);
		}
		
		// 加载许可证
		if (success) {
			loadLicence(false);
		}

		return success;
	}
	
	/**
	 * 解析分布任务组件存放目录
	 * @return 成功返回真，否则假
	 */
	private boolean splitTaskPool(org.w3c.dom.Document document) {		
		org.w3c.dom.NodeList list = document.getElementsByTagName(OtherMark.TASK_DIRECTORY);
		if (list.getLength() != 1) {
			Logger.error(this, "splitTaskPool", "not found %s", OtherMark.TASK_DIRECTORY);
			return false;
		}
		// 超时时间
		org.w3c.dom.Element element = (org.w3c.dom.Element) list.item(0);
		String input = element.getAttribute(OtherMark.ATTRIBUTE_TASK_SCANTIMEOUT);
		long timeout = ConfigParser.splitTime(input, 120000); // 默认2分钟检查一次
		// 磁盘目录
		String path = element.getTextContent();
		// 判断目录有效
		boolean success = (path != null && path.length() > 0);
		if (success) {
			PutTaskPool.getInstance().setSleepTimeMillis(timeout);
			success = PutTaskPool.getInstance().setRoot(path, "put");
		}
		if (success) {
			EndTaskPool.getInstance().setSleepTimeMillis(timeout);
			success = EndTaskPool.getInstance().setRoot(path, "end");
		}
		if (success) {
			NearTaskPool.getInstance().setSleepTimeMillis(timeout);
			success = NearTaskPool.getInstance().setRoot(path, "near");
		}
		if (success) {
			GuideTaskPool.getInstance().setSleepTimeMillis(timeout);
			success = GuideTaskPool.getInstance().setRoot(path, "guide");
		}
		
		Logger.debug(this, "splitTaskPool", success, "task path %s", path);

		return success;
	}

//	/**
//	 * 解析分布任务组件存放目录
//	 * @return 成功返回真，否则假
//	 */
//	private boolean splitTask(org.w3c.dom.Document document) {
//		XMLocal xml = new XMLocal();
//		
//		org.w3c.dom.NodeList list = document.getElementsByTagName(OtherMark.TASK_DIRECTORY);
//		if (list.getLength() != 1) {
//			Logger.error(this, "setTaskPool", "not found %s", OtherMark.TASK_DIRECTORY);
//			return false;
//		}
//		// 超时时间
//		org.w3c.dom.Element element = (org.w3c.dom.Element) list.item(0);
//		String input = element.getAttribute(OtherMark.ATTRIBUTE_TASK_SCANTIMEOUT);
//		long timeout = ConfigParser.splitTime(input, 120000); // 默认2分钟检查一次
//		// 磁盘目录
//		String path = XMLocal.getValue(element, OtherMark.TASK_DIRECTORY);
//		
////		// 解析安全配置文件
////		String path = XMLocal.getXMLValue(document.getElementsByTagName(OtherMark.TASK_DIRECTORY));
//
//		// 判断目录有效
//		boolean success = (path != null && path.length() > 0);
//		if (success) {
//			PutTaskPool.getInstance().setSleepTimeMillis(timeout);
//			success = PutTaskPool.getInstance().setRoot(path, "put");
//		}
//		if (success) {
//			EndTaskPool.getInstance().setSleepTimeMillis(timeout);
//			success = EndTaskPool.getInstance().setRoot(path, "end");
//		}
//
//		Logger.debug(this, "splitTask", success, "task path:%s", path);
//
//		return success;
//	}
	

	/**
	 * 根据输入参数启动驱动程序进程。<br><br>
	 * 
	 * 说明：<br>
	 * 一个驱动程序参数由“键”和“值”两个域组成，中间用等号（=）分开，参数之间由分号（;）分隔。<br>
	 * 
	 * <br>
	 * 
	 * 必须的参数键值包括：<br>
	 * 1. driver-user / driver-shauser，二选一 <br>
	 * 2. log-file，日志配置文件 <br>
	 * 3. config-file，启动配置文件 <br>
	 * 4. laxcus.signature，用户签名 <br>
	 * 
	 * @param params 驱动参数
	 * @return 成功返回真，否则假。
	 */
	public boolean launch(String params) {
		// 加载动态链接库
		JNILoader.init();

		// 解析传入参数，放到系统属性中
		boolean success = splitParams(params);
		// 加载日志
		if (success) {
			success = loadLog();
		}
		// 解析驱动程序账号
		if (success) {
			success = splitAccount();
		}
		// 解析配置文件
		if (success) {
			success = splitConfig();
		}

		// FRONT.DRIVER启动
		if (success) {
			success = start();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#init()
	 */
	@Override
	public boolean init() {
		// 1. 启动FIXP服务器
		boolean success = loadListen();
		Logger.note(this, "init", "load listen", success);
		// 2. 启动管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", "load pool", success);
		// 3. 启动注册
		if (success) {
			success = launch();
		}
		Logger.note(this, "init", "launch", success);

		// 不成功，关闭退出
		if (!success) {
			stopPool();
			stopListen();
		}

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 注销
		if (isLogined()) {
			logout();
		}
		// 关闭管理池
		stopPool();
		// 停止FIXP服务器
		stopListen();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#ticking()
	 */
	@Override
	public void ticking() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {

	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#getStaffPool()
	 */
	@Override
	public StaffOnFrontPool getStaffPool() {
		return StaffOnDriverPool.getInstance();
	}

	/**
	 * 根据命令生成任务实例
	 * @param cmd 命令实例
	 * @return DriverMission实例
	 */
	public DriverMission create(Command cmd) throws MissionException {
		return creator.create(cmd);
	}

	/**
	 * 根据命令语句生成任务实例
	 * @param input 输入语句
	 * @return DriverMission实例
	 */
	public DriverMission create(String input) throws MissionException {
		Command cmd = creator.create(input);
		return create(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#showStatusText(java.lang.String)
	 */
	@Override
	public void showStatusText(String text) {
		// 忽略
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#showGrade(int)
	 */
	@Override
	public void showGrade(int grade) {
		// 忽略
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#forsake()
	 */
	@Override
	public void forsake() {
		// 强制注销，通知SiteLauncher线程停止工作
		setRoundSuspend(true);
		// 取出ENTRANCE站点
		Node entrance = getInitHub();
		// 注销
		logout();
		// 重新注册
		boolean success = login();
		// 注册不成功，恢复初始ENTRANCE站点
		if (!success) {
			setInitHub(entrance);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#setMaxLogs(int)
	 */
	@Override
	public int setMaxLogs(int n) {
		return 0;
	}
	
	/**
	 * 驱动默认不声音播放声音
	 * @see com.laxcus.front.FrontLauncher#playSound(int)
	 */
	@Override
	public void playSound(int who){
		
	}
	
}