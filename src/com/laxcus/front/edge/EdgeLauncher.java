/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.edge;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.*;
import com.laxcus.front.edge.mission.*;
import com.laxcus.front.edge.pool.*;
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
import com.laxcus.util.login.*;
import com.laxcus.xml.*;

/**
 * FRONT.EDGE启动器。<br>
 * 边缘计算的服务端，在“云/边/端”之间，做为中间层存在。
 * 接受来自边缘计算客户的请求，在需要时候，把本地的数据上传到云端，反馈结果给边缘计算客户端。<br><br>
 * 
 * 在“local.xml”文件中，增加标签组，与TERMINAL/WATCH配置一致，格式：
 * <login auto="yes"> 
 * 	<hub-site> entrance://localhost:8700_8999 </hub-site>  
 * 	<account> <username> ... </username> <password> ... </password> </account>
 * </login>
 * 
 * @author scott.liang
 * @version 1.0 7/3/2019
 * @since laxcus 1.0
 */
public class EdgeLauncher extends FrontLauncher implements LogPrinter {

	/** 边缘计算启动句柄 **/
	private static EdgeLauncher selfHandle = new EdgeLauncher();

	/** 用户自定义日志存储接口 **/
	private LogPrinter logPrinter;

	/** 边缘计算任务生成器 **/
	private EdgeMissionCreator creator = new EdgeMissionCreator();

	/**
	 * 返回FRONT站点的边缘计算句柄
	 * @return 边缘计算实例
	 */
	public static EdgeLauncher getInstance() {
		return EdgeLauncher.selfHandle;
	}

	/**
	 * 构造默认和私有的FRONT边缘计算启动器
	 */
	private EdgeLauncher() {
		// 关闭JVM，不打印日志
		super(RankTag.EDGE, true, true, null);
	}

	/**
	 * 返回边缘计算任务生成器
	 * @return EdgeMissionCreator实例
	 */
	public EdgeMissionCreator getMissionCreator() {
		return creator;
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

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#ticking()
	 */
	@Override
	public void ticking() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isDriver()
	 */
	@Override
	public boolean isDriver() {
		return false;
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

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isEdge()
	 */
	@Override
	public boolean isEdge() {
		return true;
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

	/**
	 * 忽略这个方法，返回假
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
	 * @see com.laxcus.front.FrontLauncher#showStatusText(java.lang.String)
	 */
	@Override
	public void showStatusText(String text) {

	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#showGrade(int)
	 */
	@Override
	public void showGrade(int grade) {

	}

	/* (non-Javadoc)
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
	 *边缘节点默认不支持播放声音
	 * @param who 声音编号
	 */
	@Override
	public void playSound(int who){
		
	}
	

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#getStaffPool()
	 */
	@Override
	public StaffOnFrontPool getStaffPool() {
		return StaffOnEdgePool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return EdgeCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return EdgeInvokerPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return EdgeCustomTrustor.getInstance();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {
		super.kiss();
	}

	/**
	 * 根据命令生成任务实例
	 * @param cmd 命令实例
	 * @return EdgeMission实例
	 */
	public EdgeMission create(Command cmd) throws MissionException {
		return creator.create(cmd);
	}

	/**
	 * 根据命令语句生成任务实例
	 * @param input 输入语句
	 * @return EdgeMission实例
	 */
	public EdgeMission create(String input) throws MissionException {
		Command cmd = creator.create(input);
		return create(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
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
		// 3. 调用上级的“login”方法，注册ENTRANCE站点，重定向到GATE站点。
		if (success) {
			success = launch();
		}
		Logger.note(this, "init", "launch", success);

		// 不成功，关闭退出
		if (!success) {
			stopPool();
			stopListen();
			stopLog();
		}

		// 返回结果
		return success;
	}

	/* (non-Javadoc)
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
		// 关闭日志
		stopLog();
	}

	/**
	 * 启动注册
	 * @return 成功返回真，否则假
	 */
	private boolean launch() {
		Node hub = getInitHub();
		int who = login(hub, false, null);
		boolean success = FrontEntryFlag.isSuccessful(who);
		return success;
	}

	/**
	 * 启动管理池
	 * @return 返回真或者假
	 */
	private boolean loadPool() {
		// 设置事件监听器
		PutTaskPool.getInstance().setTaskListener(this);
		EndTaskPool.getInstance().setTaskListener(this);
		NearTaskPool.getInstance().setTaskListener(this);

		// CONDUCT.PUT / ESTABLISH.END / CONTACT.NEAR阶段资源代理
		PutTaskPool.getInstance().setPutTrustor(getStaffPool());
		EndTaskPool.getInstance().setEndTrustor(getStaffPool());
		NearTaskPool.getInstance().setNearTrustor(getStaffPool());

		// 业务池
		VirtualPool[] pools = new VirtualPool[] { GuideTaskPool.getInstance(),
				PutTaskPool.getInstance(), EndTaskPool.getInstance(), 
				NearTaskPool.getInstance(),
				TubPool.getInstance(),
				EdgeInvokerPool.getInstance(), EdgeCommandPool.getInstance(),
				StaffOnEdgePool.getInstance(),
				AuthroizerGateOnFrontPool.getInstance(), CallOnFrontPool.getInstance() };

		// 启动它们
		return startAllPools(pools);
	}

	/**
	 * 停止管理池
	 */
	private void stopPool() {
		VirtualPool[] pools = new VirtualPool[] {
				EdgeCommandPool.getInstance(), EdgeInvokerPool.getInstance(), 
				CallOnFrontPool.getInstance(), AuthroizerGateOnFrontPool.getInstance(),
				StaffOnEdgePool.getInstance(), 
				TubPool.getInstance(),
				PutTaskPool.getInstance(), EndTaskPool.getInstance(),
				NearTaskPool.getInstance(), GuideTaskPool.getInstance() };

		// 停止它们！
		stopAllPools(pools);
	}

	/**
	 * 解析私有参数
	 * @param document
	 */
	private void splitPrivate(org.w3c.dom.Document document){
		// 命令模式
		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);
		String input = XMLocal.getValue(element, SiteMark.COMMAND_MODE);
		setMemory(input);
		// 命令超时
		input = XMLocal.getValue(element, SiteMark.COMMAND_TIMEOUT);
		setCommandTimeout(input);

		// 内网节点检测NAT设备地址的间隔时间
		input = XMLocal.getValue(element, SiteMark.POCK_INTERVAL);
		setPockTimeout(input);

		// CALL节点检查间隔时间
		input = XMLocal.getValue(element, FrontMark.CALLSITE_CHECK_INTERVAL);
		long interval = ConfigParser.splitTime(input, getStaffPool().getCheckInterval());
		getStaffPool().setCheckInterval(interval);
	}

	//	/**
	//	 * 设置PUT/END任务组件共同的根目录
	//	 * @param document
	//	 * @param pool
	//	 * @param subpath
	//	 * @return
	//	 */
	//	private boolean setTaskPoo(org.w3c.dom.Document document, TaskPool pool, String subpath){
	//		XMLocal xml = new XMLocal();
	//		// 解析安全配置文件
	//		String path = XMLocal.getXMLValue(document.getElementsByTagName(OtherMark.TASK_DIRECTORY));
	//		// 判断目录有效
	//		if (path == null || path.isEmpty()) {
	//			Logger.error(this, "setTaskPool", "cannot find get %s", OtherMark.TASK_DIRECTORY);
	//			return false;
	//		}
	//
	//		// 如果在这个目录下指定子目录时
	//		return pool.setRoot(path, subpath);
	//	}

	/**
	 * 设置PUT/END任务组件共同的根目录
	 * @param document XML文档
	 * @param pool PUT/END组件管理池
	 * @param subpath 子目录
	 * @return 成功返回真，否则假
	 */
	private boolean setTaskPool(org.w3c.dom.Document document, DiskPool pool, String subpath) {
		org.w3c.dom.NodeList list = document.getElementsByTagName(OtherMark.TASK_DIRECTORY);
		if (list.getLength() != 1) {
			Logger.error(this, "setTaskPool", "not found %s", OtherMark.TASK_DIRECTORY);
			return false;
		}
		// 超时时间
		org.w3c.dom.Element element = (org.w3c.dom.Element) list.item(0);
		String input = element.getAttribute(OtherMark.ATTRIBUTE_TASK_SCANTIMEOUT);
		long timeout = ConfigParser.splitTime(input, 120000); // 默认2分钟检查一次
		pool.setSleepTimeMillis(timeout);
		// 目录
		String path = element.getTextContent();
		if (path == null || path.trim().isEmpty()) {
			Logger.error(this, "setTaskPool", "%s is null!", OtherMark.TASK_DIRECTORY);
			return false;
		}

		// 如果在这个目录下指定子目录时
		return pool.setRoot(path, subpath);
	}
	
	/**
	 * 解析许可证签名
	 * @param element
	 */
	private void splitLicenceSignature(org.w3c.dom.Element element) {
		// 设置签名
		String signature = XMLocal.getValue(element, LoginMark.SIGNATURE);
		if (signature != null && signature.trim().length() > 0) {
			setSignature(signature.trim());
		}
	}

	/**
	 * 解析登录服务器地址
	 * @param element 
	 * @return 返回服务器节点
	 */
	private Node splitHubSite(org.w3c.dom.Element element) {
		String input = XMLocal.getValue(element, LoginMark.HUB_SITE);
		try {
			if (input != null) {
				return new Node(input);
			}
		} catch (java.net.UnknownHostException e) {
			Logger.error(e);
		}
		return null;
	}

	/**
	 * 解析账号
	 * @param root
	 * @return 成功返回账号，否则是空指针
	 */
	private User splitUser(org.w3c.dom.Element root) {
		org.w3c.dom.NodeList nodes = root.getElementsByTagName(LoginMark.MARK_ACCOUNT);
		if (nodes == null || nodes.getLength() != 1) {
			return null;
		}

		org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
		String username = XMLocal.getValue(element, LoginMark.USERNAME);
		String password = XMLocal.getValue(element, LoginMark.PASSWORD);

		// 判断是SHA数字，或者是明文
		if (Siger.validate(username) && SHA512Hash.validate(password)) {
			// 生成签名
			return new User(new Siger(username), new SHA512Hash(password));
		} else {
			return new User(username, password);
		}
	}

	/**
	 * 解析登录参数
	 * @param document
	 * @return 返回实例，或者空指针
	 */
	private LoginToken splitLogin(org.w3c.dom.Document document) {
		// 登录参数
		org.w3c.dom.NodeList nodes = document.getElementsByTagName(LoginMark.MARK_LOGIN);
		if (nodes.getLength() == 1) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);

			splitLicenceSignature(element);
			Node hub = splitHubSite(element);
			User user = splitUser(element);

			// 参数有效，并且是ENTRANCE节点
			boolean success = (hub != null && user != null);
			if (success) {
				success = hub.isEntrance();
			}
			if (success) {
				return new LoginToken(hub, user);
			}
		}
		return null;
	}

	/**
	 * 从输入文件中获取配置参数
	 * @param filename 文件名
	 * @return 返回真或者假
	 */
	protected boolean loadLocal(String filename) {
		filename = ConfigParser.splitPath(filename);
		if (!Laxkit.hasFile(filename)) {
			Logger.error(this, "localLocal", "not found %s", filename);
			return false;
		}
		
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return false;
		}

		// 私有参数：命令模式、命令超时、
		splitPrivate(document);

		// 解析边缘容器监听
		splitTubListen(document);

		// 解析登录参数，这个必须有！
		LoginToken token = splitLogin(document);
		boolean success = (token != null);
		if (success) {
			setInitHub(token.getHub());
			User user = token.getUser();
			local.setUser(user.getUsername(), user.getPassword());
		}

		// 解析站点配置
		if (success) {
			success = splitSingleSite(local, document);
		}
		// 解析和设置回显配置
		if (success) {
			success = splitEcho(document);
		}
		// 生成默认的RSA密钥令牌
		if (success) {
			success = createDefaultSecureToken(document);
		}
		// 加载自定义配置
		if (success) {
			success = loadCustom(document);
		}
		// 设置PUT/END发布目录，目录在local.xml文件中设置
		if (success) {
			success = setTaskPool(document, PutTaskPool.getInstance(), "put");
		}
		if (success) {
			success = setTaskPool(document, EndTaskPool.getInstance(), "end");
		}
		if (success) {
			success = setTaskPool(document, NearTaskPool.getInstance(), "near");
		}
		if (success) {
			success = setTaskPool(document, GuideTaskPool.getInstance(), "guide");
		}
		// 加载多文本提示
		if (success) {
			success = loadTips();
		}

		Logger.debug(this, "loadLocal", success, "load configure");

		// 设置边缘容器管理池的目录和更新检查参数
		if (success) {
			splitTubPool(document);
		}

		// 解析“<least interval="60s"> <memory> 20% </memory> <disk> 2096mb </disk> </least>”属性
		if (success) {
			loadTimerTasks(document);
		}

		// 加载日志并且启动
		if (success) {
			success = loadLogResourceWithLocal(filename);
		}

		// 加载许可证
		if (success) {
			loadLicence(false);
		}

		//		// 启动日志服务。日志写入本地，文件目录本地配置（FRONT特点）
		//		if (success) {
		//			success = Logger.loadXML(filename);
		//			if (success) {
		//				success = Logger.loadService();
		//				if (success) {
		//					loadLogDeviceDirectory();
		//				}
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
	 * Edge站点启动器入口
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		if (System.getSecurityManager() != null) {
			Logger.info("EdgeLauncher.main, sandbox loaded!");
		}

		// 将本地链接库目录导入“java.library.path”队列，加载本地目录下面的库文件
		JNILoader.init();

		// 加载配置文件
		String filename = args[0];
		boolean success = EdgeLauncher.getInstance().loadLocal(filename);
		Logger.note("EdgeLauncher.main, load local", success);
		// 启动线程
		if (success) {
			success = EdgeLauncher.getInstance().start();
			Logger.note("EdgeLauncher.main, start service", success);
		}
		// 不成功，退出！
		if (!success) {
			Logger.gushing();
			// 关闭日志服务
			EdgeLauncher.getInstance().stopLog();
			// 退出！
			System.exit(0);
		}
	}

}