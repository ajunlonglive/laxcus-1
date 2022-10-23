/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet;

import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.*;
import com.laxcus.front.meet.pool.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.help.*;
import com.laxcus.util.local.*;
import com.laxcus.xml.*;

/**
 * FRONT交互站点启动器 <br>
 * 
 * 它是图形界面的终端和字符界面的控制台的父类。
 * 
 * @author scott.liang
 * @version 1.0 12/17/2015
 * @since laxcus 1.0
 */
public abstract class MeetLauncher extends FrontLauncher {

	/** 命令解释语境 **/
	protected CommentContext context = new CommentContext();

	/**
	 * 构造一个FRONT交互站点启动器，指定它的参数
	 * @param type FRONT节点类型
	 * @param exitVM 退出JAVA虚拟机
	 * @param printFault 出错退出打印日志
	 */
	public MeetLauncher(byte type, boolean exitVM, boolean printFault, LogPrinter printer) {
		super(type, exitVM, printFault, printer);

		// 给命令转发器设置FRONT站点句柄
		MeetCommandDispatcher.setMeetLauncher(this);

		// 向自定义资源接口设置FRONT交互站点启动器句柄
		MeetCustomTrustor.getInstance().setMeetLauncher(this);

		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		return MeetCustomTrustor.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#init()
	 */
	@Override
	public boolean init() {
		// 1. 预初始化
		boolean success = preinit();
		Logger.note(this, "init", "preinit", success);
		// 2. 启动FIXP服务器
		if(success) {
			success = loadListen();
		}
		Logger.note(this, "init", "load listen", success);
		// 3. 启动管理池
		if (success) {
			success = loadPool();
		}
		Logger.note(this, "init", "load pool", success);
		// 4. 调用子类"launch"方法，启动图形/字符中的一种服务，注册ENTRANCE站点，重定向到GATE站点。
		if (success) {
			success = launch();
		}
		Logger.note(this, "init", "launch", success);
		// 5. 登录成功，启动任务管理池
		if (success) {
			success = loadTaskPool();
		}
		Logger.note(this, "init", "load task pool", success);

		// 不成功，关闭退出
		if (!success) {
			stopPool();
			stopListen();
			// 销毁
			destroy();
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
		// 销毁子类资源
		destroy();
		// 销毁日志
		stopLog();
	}

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
	 * 解析私有参数
	 * @param document
	 */
	private void splitPrivate(org.w3c.dom.Document document){
		// CONSOLE/TERMINAL的命令模式
		org.w3c.dom.Element element = (org.w3c.dom.Element) document.getElementsByTagName(SiteMark.MARK_LOCAL_SITE).item(0);
		String input = XMLocal.getValue(element, SiteMark.COMMAND_MODE);
		setMemory(input);
		// CONSOLE/TERMINAL的命令超时
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

		splitPrivate(document);
		
		// 解析边缘容器监听
		splitTubListen(document);

		// 解析站点配置
		boolean success = splitSingleSite(local, document);
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
		// 设置PUT/END/NEAR/GUIDE发布目录，目录在local.xml文件中设置
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
		// 设置边缘容器管理池的目录和更新检查参数
		if (success) {
			splitTubPool(document);
		}

		// 加载日志并且启动
		if (success) {
			success = loadLogResourceWithLocal(filename);
		}
		
		// 只能是CONSOLE节点，才加载许可证，TERMINAL在登录前进行
		if (success && isConsole()) {
			loadLicence(false);
		}

		return success;
	}

	/**
	 * 从总配置文件中选出所关联语言的命令帮助文件路径，加载上下文
	 */
	protected void loadCommentContext(boolean highScreen) {
		LocalSelector selector = new LocalSelector("conf/front/help/config.xml");		
		String path = selector.findPath("resource");
		context.load(path, highScreen);
	}

	/**
	 * 返回命令解释语境
	 * @return 命令语境
	 */
	public CommentContext getCommentContext() {
		return context;
	}

	/**
	 * 预初始化。发生在"init"方法的开始。
	 */
	protected abstract boolean preinit();
	
	
	/**
	 * 启动参数输入界面，注册到GATE站点。<br>
	 * 图形前端是打开登录窗口输入登录参数，字符前端是在控制台输入登录参数。
	 * @return 成功返回“真”，失败“假”。
	 */
	protected abstract boolean launch();

	/**
	 * 启动管理池
	 * @return 成功返回真，否则假
	 */
	protected abstract boolean loadPool();

	/**
	 * 启动分布组件任务管理池，在登录成功后启动!
	 * 
	 * @return 成功返回真，否则假
	 */
	protected abstract boolean loadTaskPool();

	/**
	 * 停止管理池
	 */
	protected abstract void stopPool();

	/**
	 * 销毁全部资源。<br>
	 * 图形前端释放图形窗口，字符前端退出控制台输入，驱动程序是空操作。
	 */
	protected abstract void destroy();

	/**
	 * 注册激活超时，关闭与GATE站点的连接，和显示断开连接的标记。<br>
	 * 在图形前端中，状态栏图标灰色。控制台是打印关闭信息。
	 */
	public abstract void disconnect();

//	/**
//	 * 显示许可证信息
//	 * @param content 文本内容
//	 * @return 接受返回真，否则假
//	 */
//	public abstract boolean showLicence(String content);
}