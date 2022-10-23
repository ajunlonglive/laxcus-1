/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.console;

import java.io.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.*;
import com.laxcus.front.console.pool.*;
import com.laxcus.front.meet.*;
import com.laxcus.front.meet.pool.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.local.*;

/**
 * 控制台启动器 <br>
 * 
 * 控制台启动器以字符界面显示，属于交互操作方式。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2009
 * @since laxcus 1.0
 */
public final class ConsoleLauncher extends MeetLauncher implements LocalMatcher {

	/** 控制台启动器实例 **/
	private static ConsoleLauncher selfHandle = new ConsoleLauncher();

	/** 字符操作接口 **/
	private ConsoleInputter console = new ConsoleInputter();

	/** 窗口界面资源解析器 **/
	private SurfaceLoader loader = new SurfaceLoader();

	/**
	 * 构造控制台启动器
	 */
	private ConsoleLauncher() {
		// 退出JAVA虚拟机和出错退出时打印日志
		super(RankTag.CONSOLE, true, false, null);
		
		// 加载帮助上下文
		loadCommentContext(false);
	}

	/**
	 * 返回控制台启动器静态句柄
	 * @return
	 */
	public static ConsoleLauncher getInstance() {
		return ConsoleLauncher.selfHandle;
	}

	/**
	 * 返回控制台句柄
	 * @return
	 */
	public Console getConsole() {
		return console.getConsole();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.local.LocalMatcher#findCaption(java.lang.String)
	 */
	@Override
	public String findCaption(String xmlPath) {
		if(!loader.isLoaded()) {
			loader.load(getSurfacePath());
		}
		return loader.getAttribute(xmlPath);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.local.LocalMatcher#findContent(java.lang.String)
	 */
	@Override
	public String findContent(String xmlPath) {
		if (!loader.isLoaded()) {
			loader.load(getSurfacePath());
		}
		return loader.getContent(xmlPath);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#disconnect()
	 */
	@Override
	public void disconnect() {
		// 注销与GATE服务器的联系
		logout();

		// 在屏幕上打印：“网络服务中断！”
		String text = ConsoleLauncher.getInstance().findContent("console/disconnect");
		showStatusText(text);

		// // 强制注销
		// setRoundSuspend(true);
		// // 注销服务器
		// logout();
		//
		// // 离线状态
		// local.setGrade(GradeTag.OFFLINE);
		//
		// // 在屏幕上打印：“网络服务中断！”
		// String text =
		// ConsoleLauncher.getInstance().findContent("console/disconnect");
		// showStatusText(text);
	}

	/**
	 * 控制台是空方法，不处理声音提示
	 * @param who 声音编号
	 */
	@Override
	public void playSound(int who){
		
	}
	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.front.meet.MeetLauncher#showLicence(java.lang.String)
//	 */
//	@Override
//	public boolean showLicence(String content) {
//		return console.showLicence(content);
//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#confirm(int)
	 */
	@Override
	public boolean confirm(int no) {
		String content = message(no);
		return console.confirm(content) ;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.tip.TipPrinter#confirm(int)
	 */
	@Override
	public boolean confirm(int no, Object... params) {
		String content = message(no, params);
		return console.confirm(content) ;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#preinit()
	 */
	@Override
	protected boolean preinit() {
		boolean success = console.initialize();
		if (success) {
			// 设置前端显示器
			PutTaskPool.getInstance().setDisplay(console);
			EndTaskPool.getInstance().setDisplay(console);
			NearTaskPool.getInstance().setDisplay(console);
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#launch()
	 */
	@Override
	protected boolean launch() {
		// 通过ENTRANCE重定向，注册到GATE站点
		boolean success = console.login();
		// 注册成功，启动控制台线程
		if(success) {
			success = console.start();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#destroy()
	 */
	@Override
	protected void destroy() {
		// 停止控制台
		console.stop();
		// 判断线程结束
		while(console.isRunning()) {
			delay(200);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {
		super.kiss();
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
		return true;
	}

	/*
	 * (non-Javadoc)
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

	/**
	 * 从总配置文件中选出所关联语言的界面配置文件路径
	 * @return JAR文件中的资源文件路径
	 */
	public String getSurfacePath() {
		LocalSelector selector = new LocalSelector("conf/front/console/config.xml");		
		return selector.findPath("resource");
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#loadPool()
	 */
	@Override
	protected boolean loadPool() {
		
		VirtualPool[] pools = new VirtualPool[] { 
				
				TubPool.getInstance(),
				MeetInvokerPool.getInstance(), MeetCommandPool.getInstance(),
				StaffOnConsolePool.getInstance(),
				AuthroizerGateOnFrontPool.getInstance(), CallOnFrontPool.getInstance(),
				CustomClassPool.getInstance(), GuideTaskPool.getInstance()};
		
		// 注意！GuideTaskPool在这里启动

		return startAllPools(pools);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#loadTaskPool()
	 */
	@Override
	protected boolean loadTaskPool() {
		// 设置事件监听器
		PutTaskPool.getInstance().setTaskListener(this);
		EndTaskPool.getInstance().setTaskListener(this);
		NearTaskPool.getInstance().setTaskListener(this);
		
		// CONDUCT.PUT阶段资源代理
		PutTaskPool.getInstance().setPutTrustor(getStaffPool());
		// ESTABLISH.END阶段资源代理
		EndTaskPool.getInstance().setEndTrustor(getStaffPool());
		// CONTACT.NEAR阶段资源代理
		NearTaskPool.getInstance().setNearTrustor(getStaffPool());

		VirtualPool[] pools = new VirtualPool[] {
				PutTaskPool.getInstance(), EndTaskPool.getInstance(),
				NearTaskPool.getInstance() };

		return startAllPools(pools);
	}
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.MeetLauncher#stopPool()
	 */
	@Override
	protected void stopPool() {
		VirtualPool[] pools = new VirtualPool[] {
				CustomClassPool.getInstance(),
				MeetCommandPool.getInstance(), MeetInvokerPool.getInstance(), 
				CallOnFrontPool.getInstance(), AuthroizerGateOnFrontPool.getInstance(),
				StaffOnConsolePool.getInstance(), 
				TubPool.getInstance(),
				PutTaskPool.getInstance(), EndTaskPool.getInstance(),
				NearTaskPool.getInstance(), GuideTaskPool.getInstance()};
		stopAllPools(pools);
	}
	
	/**
	 * 重新自动登录，覆盖“FrontLauncher.login”方法
	 * 
	 * @see com.laxcus.front.FrontLauncher#login()
	 */
	@Override
	public boolean login() {
		// 如果没有达到GATE节点要求的重新注册间隔时间，忽略！
		if (!canAutoReloginInterval()) {
			return false;
		}
		// 确定初始HUB站点地址（ENTRANCE站点）
		com.laxcus.site.Node entrance = getInitHub();
		if (entrance == null) {
			Logger.error(this, "login", "entrance site is null!");
			return false;
		}

		// 自动注销，SiteLauncher.defaultProcess方法不进入强制自循环！
		__logout(true);
		
		// 在重新注册前，刷新最后调用的时间
		refreshEndTime();
		
		// 显示自动登录
		console.printXML("console/auto-login/retry");
		
		// 调用父类进行自动登录
		int who = super.login(entrance.getHost(), true, null);
		// 判断成功
		boolean success = FrontEntryFlag.isSuccessful(who);
		// 不成功，保存初始HUB地址
		if (!success) {
			ConsoleLauncher.getInstance().setInitHub(entrance);
			console.printXML("console/auto-login/failed");
		} else {
			console.printXML("console/auto-login/successful");
			Logger.debug(this, "login", "login successful! entrance hub is %s", entrance);
		}

		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#ticking()
	 */
	@Override
	public void ticking() {
		// TODO Auto-generated method stub

	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#showStatusText(java.lang.String)
	 */
	@Override
	public void showStatusText(String text) {
		System.out.println(text);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#showGrade(int)
	 */
	@Override
	public void showGrade(int grade) {
		if (GradeTag.isAdministrator(grade)) {
			String e = ConsoleLauncher.getInstance().findContent("console/grade/administrator");
			showStatusText(e);
		} else if (GradeTag.isUser(grade)) {
			String e = ConsoleLauncher.getInstance().findContent("console/grade/user");
			showStatusText(e);
		} else {
			String e = ConsoleLauncher.getInstance().findContent("console/grade/undefined");
			showStatusText(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#forsake()
	 */
	@Override
	public void forsake() {
		disconnect();
	}


	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#setMaxLogs(int)
	 */
	@Override
	public int setMaxLogs(int n) {
		return 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#getStaffPool()
	 */
	@Override
	public StaffOnFrontPool getStaffPool() {
		return StaffOnConsolePool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public CommandPool getCommandPool() {
		return MeetCommandPool.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public InvokerPool getInvokerPool() {
		return MeetInvokerPool.getInstance();
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.front.meet.MeetLauncher#finish()
//	 */
//	@Override
//	public void finish() {
//		super.finish();
//		// 关闭日志
//		super.stopLog();
//	}

	/**
	 * FRONT.CONSOLE启动入口
	 * @param args 参数
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		// 加载“bin”目录下面和“laxcus.library”目录下面的库文件
		JNILoader.init();

		// 加载配置文件
		String filename = args[0];
		
		// 加载本地资源
		boolean success = ConsoleLauncher.getInstance().loadLocal(filename);
		Logger.note("ConsoleLauncher.main, load local", success);
		// 启动线程
		if (success) {
			success = ConsoleLauncher.getInstance().start();
			Logger.note("ConsoleLauncher.main, start service", success);
		}

		// 成功启动限制规划，不成功退出
		if (success) {
			ConsoleLauncher.getInstance().loadTimerTasks(filename);
		} else {
			Logger.gushing();
			// 关闭日志
			ConsoleLauncher.getInstance().stopLog();
			System.exit(0);
		}
	}

}


///**
// * 加载本地日志配置
// * @param filename
// * @return 成功返回真，否则假
// */
//private boolean loadLog(String filename) {
//	filename = ConfigParser.splitPath(filename);
//
//	// 启动日志服务
//	boolean success = Logger.loadXML(filename);
//	if (success) {
//		success = Logger.loadService(null);
//	}
//	return success;
//}
