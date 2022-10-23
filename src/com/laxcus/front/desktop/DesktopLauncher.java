/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import com.laxcus.access.diagram.*;
import com.laxcus.application.manage.*;
import com.laxcus.container.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.custom.*;
import com.laxcus.front.*;
import com.laxcus.front.desktop.dispatcher.*;
import com.laxcus.front.desktop.pool.*;
import com.laxcus.front.desktop.status.*;
import com.laxcus.front.pool.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.launch.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.pool.*;
import com.laxcus.register.*;
import com.laxcus.site.*;
import com.laxcus.site.front.*;
import com.laxcus.task.conduct.put.*;
import com.laxcus.task.contact.near.*;
import com.laxcus.task.establish.end.*;
import com.laxcus.task.guide.pool.*;
import com.laxcus.thread.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.display.splash.*;
import com.laxcus.util.event.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.help.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.local.*;
import com.laxcus.util.login.*;
import com.laxcus.util.skin.*;
import com.laxcus.util.sound.*;
import com.laxcus.xml.*;

/**
 * FRONT虚拟化桌面
 * 
 * @author scott.liang
 * @version 1.0 5/18/2021
 * @since laxcus 1.0
 */
public class DesktopLauncher extends FrontLauncher implements InvokerMessenger { 

	/** 构造桌面实例 **/
	private static DesktopLauncher selfHandler = new DesktopLauncher();

	/** 命令解释语境 **/
	protected CommentContext context = new CommentContext();

	/** 桌面窗口 **/
	private DesktopWindow window = new DesktopWindow();

	/** 皮肤颜色加载器 **/
	private SkinTokenLoader skinLoader = new SkinTokenLoader();

	/** 启动屏窗口 **/
	private SplashWindow splash = new SplashWindow();

	/**
	 * 构造一个虚拟桌面
	 */
	public DesktopLauncher() {
		super(RankTag.DESKTOP, true, true, DesktopLogTrustor.getInstance());
		// 初始化
		initial();
	}

	/**
	 * 初始化基础参数
	 */
	private void initial() {
		// 给命令转发器设置FRONT站点句柄
		DesktopCommandDispatcher.setDesktopLauncher(this);
		// 托盘管理器
		PlatformKit.setTrayManager(new DesktopTrayManager());

		// 向自定义资源接口设置FRONT交互站点启动器句柄
		DesktopCustomTrustor.getInstance().setDesktopLauncher(this);

		// 将颜色加载到内存集合中
		loadColors();
		// 加载帮助上下文
		loadCommentContext();
		// 加载显示在UI界面上的文字和它的配置
		loadUIText();

		// 设置INVOKER句柄
		InvokerTrustor.setInvokerMessenger(this);
	}

	/**
	 * 返回桌面启动器静态实例
	 * @return DesktopLauncher实例
	 */
	public static DesktopLauncher getInstance() {
		return DesktopLauncher.selfHandler;
	}

	/**
	 * 返回窗口界面
	 * @return DesktopWindow实例
	 */
	public DesktopWindow getWindow() {
		return window;
	}

	/**
	 * 从总配置文件中选出所关联语言的命令帮助文件路径，加载上下文
	 */
	protected void loadCommentContext() {
		LocalSelector selector = new LocalSelector("conf/desktop/help/config.xml");		
		String path = selector.findPath("resource");
		context.load(path, GUIKit.isHighScreen());
	}

	/**
	 * 从指定的文本中加载那些公共显示命令
	 * 适用于所有图形界面的UI
	 */
	private void loadUIText() {
		String xmlPath = "conf/desktop/ui/config.xml";
		LocalSelector selector = new LocalSelector(xmlPath);		
		String path = selector.findPath("resource");

		// 解析参数
		try {
			UISplitter splitter = new UISplitter();
			splitter.load(path);
			java.util.List<String> keys = splitter.getKeys();
			for(String key : keys) {
				Object value = splitter.find(key);
				UIManager.put(key, value);
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	/**
	 * 返回命令解释语境
	 * @return 命令语境
	 */
	public CommentContext getCommentContext() {
		return context;
	}

	/**
	 * 检查平台字体
	 */
	private boolean checkPlatformFont() {
		FontRector rector = new FontRector();
		return rector.checkPlatformFont();
	}

	/**
	 * 退出登录
	 */
	public void disconnect() {
		// 退出登录
		logout();
	}

	/**
	 * 播放声音
	 * @param who
	 */
	public void playSound(int who){
		SoundKit.play(who);
	}

	/**
	 * 加载声音参数
	 */
	private void loadSound() {
		ResourceLoader loader = new ResourceLoader("conf/desktop/sound");
		// 警告声音
		byte[] stream = loader.findStream("warning.wav");
		SoundItem item = SoundItem.create(SoundTag.WARNING, stream);
		if (item != null) {
			SoundPlayer.getInstance().add(item);
		}
		// 错误声音
		stream = loader.findStream("error.wav");
		item = SoundItem.create(SoundTag.ERROR, stream);
		if (item != null) {
			SoundPlayer.getInstance().add(item);
		}
		// 消息提示声音
		stream = loader.findStream("message.wav");
		item = SoundItem.create(SoundTag.MESSAGE, stream);
		if (item != null) {
			SoundPlayer.getInstance().add(item);
		}
	}

	/**
	 * 自动登录，覆盖FrontLauncher.login方法
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
		Node entrance = getInitHub();
		if (entrance == null) {
			Logger.error(this, "login", "entrance site is null!");
			return false;
		}

		// 自动注销，SiteLauncher.defaultProcess方法不进入强制自循环！
		__logout(true);

		// 在重新注册前，刷新最后调用的时间
		refreshEndTime();

		//		// 故障声音
		//		SoundKit.playError();

		// 清除窗口，自动登录，返回结果
		return window.__auto_login(entrance);
	}

	/**
	 * 弹出确认窗口
	 * @param content 显示文本
	 * @return 接受返回真，否则假
	 */
	private boolean showConfirmDialog(String content) {		
		String title = window.getTitle();
		return MessageBox.showYesNoDialog(window, title, content);
	}

	/**
	 * 弹出对话框提示信息
	 */
	public boolean confirm(int no) {
		String content = message(no);
		return showConfirmDialog(content);
	}

	/**
	 * 弹出对话窗口
	 */
	public boolean confirm(int no, Object... params) {
		String content = message(no, params);
		return showConfirmDialog(content);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#ticking()
	 */
	@Override
	public void ticking() {
		// 触发动画显示
		window.flash();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerMessenger#startInvoker(com.laxcus.echo.invoke.EchoInvoker)
	 */
	@Override
	public void startInvoker(EchoInvoker invoker) {
		// 如果不是远程执行的，忽略它
		if (!invoker.isDistributed()) {
			return;
		}
		// 发出
		window.rolling(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerMessenger#stopInvoker(com.laxcus.echo.invoke.EchoInvoker, boolean)
	 */
	@Override
	public void stopInvoker(EchoInvoker invoker, boolean success) {
		// 如果不是远程执行的，忽略它
		if (!invoker.isDistributed()) {
			return;
		}

		// 不成功或者退出时，记录流量
		if (!success || invoker.isQuit()) {
			long rs = invoker.getReceiveFlowSize();
			long ss = invoker.getSendFlowSize();
			window.addFlows(rs, ss);
		}
		window.rolling(false);
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

	/*
	 * (non-Javadoc)
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
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isDesktop()
	 */
	@Override
	public boolean isDesktop() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#isApplication()
	 */
	@Override
	public boolean isApplication() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#showStatusText(java.lang.String)
	 */
	@Override
	public void showStatusText(String text) {
		window.setStatusText(text);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#showGrade(int)
	 */
	@Override
	public void showGrade(int grade) {
		if (GradeTag.isAdministrator(grade)) {
			String e = UIManager.getString("grade.administrator"); 
			showStatusText(e);
			window.setAdministratorIcon();
		} else if (GradeTag.isUser(grade)) {
			String e = UIManager.getString("grade.user"); 
			showStatusText(e);
			window.setUserIcon();
		} else {
			String e = UIManager.getString("grade.undefined");
			showStatusText(e);
			window.setOfflineIcon();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#getStaffPool()
	 */
	@Override
	public StaffOnDesktopPool getStaffPool() {
		return StaffOnDesktopPool.getInstance();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#forsake()
	 */
	@Override
	public void forsake() {
		DesktopForsakeThread e = new DesktopForsakeThread(this);
		e.start();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#setMaxLogs(int)
	 */
	@Override
	public int setMaxLogs(int n) {
		int logs = window.setMaxLogs(n);

		// 返回结果!
		return logs;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCommandPool()
	 */
	@Override
	public DesktopCommandPool getCommandPool() {
		return DesktopCommandPool.getInstance();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getInvokerPool()
	 */
	@Override
	public DesktopInvokerPool getInvokerPool() {
		return DesktopInvokerPool.getInstance();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#getCustomTrustor()
	 */
	@Override
	public CustomTrustor getCustomTrustor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.launch.SiteLauncher#disableProcess()
	 */
	@Override
	protected void disableProcess() {
		// 设置图标为中断
		window.setOnlineIcon(false);
	}

	/**
	 * 继承自SiteLauncher.defaultSubProcess，定时检测CPU压力，控制日志更新频率
	 * @see com.laxcus.launch.SiteLauncher#defaultSubProcess()
	 */
	@Override
	protected void defaultSubProcess() {
		double rate = 100.0f;
		if (isLinux()) {
			rate = LinuxEffector.getInstance().getRate();
		} else if (isWindows()) {
			rate = WindowsEffector.getInstance().getRate();
		}

		//		// 修改日志刷新频率
		//		// 1. 达到60%比率，日志发送调整到最低值
		//		// 2. 超过30%比率，降低日志发送
		//		// 3. 低于15%比率，提高日志发送
		//		if (rate >= 60.0f) {
		//			DesktopLogTrustor.getInstance().low();
		//		} else if (rate >= 30.0f) {
		//			DesktopLogTrustor.getInstance().descent();
		//		} else if (rate < 15.0f) {
		//			DesktopLogTrustor.getInstance().rise();
		//		}

		// 修改日志刷新频率
		// 1. 达到90%比率，日志发送调整到最低值
		// 2. 超过50%比率，降低日志发送
		// 3. 低于30%比率，提高日志发送
		if (rate >= 90.0f) {
			DesktopLogTrustor.getInstance().low();
		} else if (rate > 50.0f) {
			DesktopLogTrustor.getInstance().descent();
		} else if (rate < 30.0f) {
			DesktopLogTrustor.getInstance().rise();
		}

		// 写入单元
		writeApplications(false);
		writeDesktopButtons(false);
		// 写入环境变量
		writeEnvironement(false);
		
		//		testFire();
	}

	//	long nowTime = System.currentTimeMillis();
	//	private void testFire() {
	//		if(System.currentTimeMillis() - nowTime <= 60000) {
	//			return;
	//		}
	//		nowTime = System.currentTimeMillis();
	//		// 测试，显示窗口
	//		MemoryMissing mm = new MemoryMissing(getListener());
	//		getCommandPool().admit(mm);
	//		getCommandPool().admit(new VMMemoryMissing(getListener()));
	//		getCommandPool().admit(new DiskMissing(this.getListener()));
	//	}

	/**
	 * 写单元配置
	 */
	private void writeApplications(boolean force) {
		// 不可用，忽略
		if (!RTManager.getInstance().isUsabled()) {
			return;
		}
		// 判断更新...
		if (!force) {
			if (!RTManager.getInstance().isUpdated()) {
				return;
			}
		}

		// 导入资源
		File dir = DesktopSystem.createRuntimeDirectory();
		File file = new File(dir, "applications.conf");
		// 不写系统应用
		int elements = RTManager.getInstance().writeRoots(file, true);

		RTManager.getInstance().resetUpdate();

		Logger.info(this, "writeApplications", "write applications %d", elements);
	}

	/**
	 * 写入按纽
	 * @param force
	 */
	private void writeDesktopButtons(boolean force) {
		// 不可用时，忽略
		if (!DesktopController.getInstance().isUsabled()) {
			return;
		}
		if (!force) {
			if (!DesktopController.getInstance().isUpdated()) {
				return;
			}
		}

		DesktopController.getInstance().writeButtons();
		DesktopController.getInstance().resetUpdate();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLauncher#init()
	 */
	@Override
	public boolean init() {
		Logger.debug(this, "init", "local is %s", local);

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
		// 销毁子类资源
		destroy();
		// 销毁日志
		stopLog();
	}

	/**
	 * 预加载
	 * @return
	 */
	protected boolean preinit() {
		// 1. 加载声音
		loadSound();
		// 2. 预处理加载资源
		DesktopReduceSpeeder rs = new DesktopReduceSpeeder();
		rs.start();
		
		// 返回成功
		return true;
	}

	/**
	 * 启动
	 * @return 返回真或者假
	 */
	protected boolean launch() {
		// 启动窗口，注册到GATE节点
		boolean success = window.showWindow();

		// 设置显示前端。不要设置显示界面，由各个运行任务来设置。
		if (success) {
			//			PutTaskPool.getInstance().setDisplay(window.getImplementPanel().getTabPanel());
			//			EndTaskPool.getInstance().setDisplay(window.getImplementPanel().getTabPanel());
			//			NearTaskPool.getInstance().setDisplay(window.getImplementPanel().getTabPanel());

			// 边缘容器管理池启动（必须保证在图形界面显示成功后才能启动，否则在没有启动图形界面前，TubPool调用图形界面会出错）
			TubPool.getInstance().start();

			// 启动日志代理（保证在窗口可视情况下输出日志，否则会有UI卡死现象出现）
			DesktopLogTrustor.getInstance().start();
		}

		// 加载配置
		if (success && CustomConfig.isValidate()) {
			// 加载类定义
			CustomClassLoader loader = new CustomClassLoader();
			loader.load();
			// 加载自定义命令关键字
			//			String path = CustomConfig.getTokenPath();
			//			window.addCommandTokens(path);
		}

		return success;
	}	

	/**
	 * 销毁
	 */
	protected void destroy() {
		// 写入应用参数
		writeApplications(true);
		// 写入桌面按纽
		writeDesktopButtons(true);
		// 写入环境变量
		writeEnvironement(true);
		
		// 销毁窗口
		window.dispose();
	}

	/**
	 * 加载服务池
	 * @return
	 */
	protected boolean loadPool() {
		// 委托线程
		VirtualThread[] threads = new VirtualThread[] {
				SwingDispatcher.getInstance(), SoundPlayer.getInstance() };
		startThreads(threads);

		// 业务管理池
		VirtualPool[] pools = new VirtualPool[] {
				ApplicationPool.getInstance(),
				DesktopInvokerPool.getInstance(), DesktopCommandPool.getInstance(),
				StaffOnDesktopPool.getInstance(),
				AuthroizerGateOnFrontPool.getInstance(),
				CallOnFrontPool.getInstance() , GuideTaskPool.getInstance()};

		// 注意！GuideTaskPool在这里启动

		// 启动全部管理池
		return startAllPools(pools);
	}

	/**
	 * 加载任务池
	 * @return
	 */
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

		// 业务管理池
		VirtualPool[] pools = new VirtualPool[] { 
				PutTaskPool.getInstance(), EndTaskPool.getInstance(),
				NearTaskPool.getInstance() };

		// 启动全部管理池
		return startAllPools(pools);
	}

	/**
	 * 停止任务池
	 */
	protected void stopPool() {
		// 管理池
		VirtualPool[] pools = new VirtualPool[] {
				// 应用容器池
				ApplicationPool.getInstance(),
				// DESKTOP命令管理池
				DesktopCommandPool.getInstance(),
				// DESKTOP调用器管理池
				DesktopInvokerPool.getInstance(), CallOnFrontPool.getInstance(),
				AuthroizerGateOnFrontPool.getInstance(),
				StaffOnDesktopPool.getInstance(), TubPool.getInstance(),
				PutTaskPool.getInstance(), EndTaskPool.getInstance(), 
				NearTaskPool.getInstance(), GuideTaskPool.getInstance()};

		// 线程
		VirtualThread[] threads = new VirtualThread[] {
				SoundPlayer.getInstance(),
				SwingDispatcher.getInstance(),
				DesktopLogTrustor.getInstance() };

		// 先关闭线程，再关闭管理池
		stopThreads(threads);
		// 关闭全部管理池
		stopAllPools(pools);
	}

	/**
	 * 返回皮肤加载器
	 * @return SkinTokenLoader实例句柄
	 */
	public SkinTokenLoader getSkinLoader() {
		return skinLoader;
	}

	/**
	 * 加载颜色参数
	 */
	private void loadColors() {
		String xmlPath = "conf/desktop/color/color.txt";
		ColorTemplate.load(xmlPath);
	}

//	/**
//	 * 建立目录
//	 * @return
//	 */
//	private File createDirectory() {
//		String bin = System.getProperty("user.dir");
//		bin += "/../conf";
//		File file = new File(bin);
//		boolean success = (file.exists() && file.isDirectory());
//		if (!success) {
//			success = file.mkdirs();
//		}
//		return (success ? file : null);
//	}

	/**
	 * 读取环境变量
	 * @return
	 */
	private boolean readEnvironement() {
		File dir = DesktopSystem.createRuntimeDirectory();
		File file = new File(dir, "environment.conf");
		RTEnvironment evnironment = PlatformKit.getRTEnvironment();
		boolean success = false;
		if (file.exists() && file.isFile()) {
			try {
				evnironment.infuse(file);
				success = true;
			} catch (IOException e) {
				Logger.error(e);
			}
		} else {
			evnironment.createDefault();// 生成默认值
			success = true;
		}

		if (success) {
			// 声音
			String paths = "Sound/Play";
			if (RTKit.hasBoolean(RTEnvironment.ENVIRONMENT_SYSTEM, paths)) {
				boolean play = RTKit.readBoolean(RTEnvironment.ENVIRONMENT_SYSTEM, paths);
				SoundPlayer.getInstance().setPlay(play);
			}
			// 系统字体
			Font font = RTKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "Font/System");
			if (font != null) {
				UITools.updateSystemFonts(font);
			}
		}
		return success;
	}

	/**
	 * 写环境变量
	 */
	private void writeEnvironement(boolean force) {
		RTEnvironment evnironment = PlatformKit.getRTEnvironment();
		// 不可用，忽略
		if (!evnironment.isUsabled()) {
			return;
		}
		// 非强制状态，且没有更新时，退出
		if (!force) {
			if (!evnironment.isUpdated()) {
				return;
			}
		}

		// 界面皮肤颜色名称
		SkinToken token = skinLoader.findCheckedSkinToken();
		if (token != null) {
			RTKit.writeString(RTEnvironment.ENVIRONMENT_SYSTEM, "Skin/Name", token.getName());
		}

		// 声音
		boolean play = SoundPlayer.getInstance().isPlay();
		RTKit.writeBoolean(RTEnvironment.ENVIRONMENT_SYSTEM, "Sound/Play", play);

		File dir = DesktopSystem.createRuntimeDirectory();
		File file = new File(dir, "environment.conf");

		// 写入磁盘
		try {
			evnironment.effuse(file);
			evnironment.setUpdated(false); // 恢复状态
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	/**
	 * 从环境变量读取皮肤配置名称
	 * @return 返回字符串或者空指针
	 */
	private String readSkinName() {
		return RTKit.readString(RTEnvironment.ENVIRONMENT_SYSTEM, "Skin/Name");
	}

	//	private void testJSliderIcon() {
	//		Icon icon = UIManager.getIcon( "Slider.horizontalThumbIcon" );
	//		if(icon == null) {
	//			System.out.println("null icon");
	//		}
	//		System.out.printf("icon is w:%d h:%d\n", icon.getIconWidth(), icon.getIconHeight());
	//		System.out.printf("icon class is %s\n", icon.getClass().getName());
	//	}

	//	/**
	//	 * 加载皮肤配置，同是更新外观界面
	//	 * @return 成功返回真，否则假
	//	 */
	//	private boolean loadSkins() {
	//		// 1. 从配置文件中加载皮肤参数，不成功退出
	//		boolean success = skinLoader.load("conf/desktop/skin/config.xml");
	//		if (!success) {
	//			return false;
	//		}
	//		// 2. 读取"环境变量"配置文件，把"skin.name"参数读取出来
	//		String name = readSkinName();
	//		// 如果没有找到皮肤名字，默认是"normal"，对应Nimbus外观
	//		if (name == null) {
	//			name = "normal"; // normal关键字在config.xml配置文件里定义
	//		}
	//
	//		//		name = "dark";
	//		//		name = "cyano";
	//		//		name = "bronze";
	//
	//		// 3. 找到匹配的皮肤方案
	//		success = false;
	//		SkinToken token = skinLoader.findSkinTokenByName(name);
	//		// 存在，加载皮肤界面
	//		if (token != null) {
	//			// 3. 切换到主题界面，同时更新组件UI
	//			success = token.updateTheme(true);
	//			if (success) {
	////				testJSliderIcon();
	//				// 选择中
	//				skinLoader.exchangeCheckedSkinToken(token.getName());
	//				// 定义外观
	//				Skins.setLookAndFeel(token.getLookAndFeel());
	//				Skins.setSkinName(token.getName());
	//			}
	//		} else {
	//			// 设置为默认的“Nimbus”外观，“Metal”为暗黑！
	//			success = UITools.updateLookAndFeel("Nimbus", null, null);
	//			if (success) {
	//				int count = SkinToken.loadSkins("conf/desktop/skin/nimbus_normal.txt");
	//				success = (count > 0);
	//			}
	//			if (success) {
	//				skinLoader.exchangeCheckedSkinToken("Nimbus");
	//				Skins.setLookAndFeel(Skins.Nimbus);
	//				Skins.setSkinName("normal"); // "normal"是脚本中的定义
	//			}
	//		}
	//
	//		//		UIDefaults defs = UIManager.getDefaults();
	//		//		defs.put("TextPane.background", new ColorUIResource(Color.BLACK));
	//		//		defs.put("TextPane.inactiveBackground", new ColorUIResource(Color.BLACK));
	//
	//		return success;
	//	}

	/**
	 * 加载皮肤配置，同是更新外观界面
	 * @return 成功返回真，否则假
	 */
	private boolean loadSkins() {
		// 1. 从配置文件中加载皮肤参数，不成功退出
		boolean success = skinLoader.load("conf/desktop/skin/config.xml");
		if (!success) {
			return false;
		}
		// 2. 读取"环境变量"配置文件，把"skin.name"参数读取出来
		String name = readSkinName();
		// 如果没有找到皮肤名字，默认是"normal"，对应Nimbus外观
		if (name == null) {
			name = "gray"; // gray关键字在config.xml配置文件里定义
		}

		//		name = "dark";
		//		name = "cyano";
		//		name = "bronze";

		// 3. 找到匹配的皮肤方案
		success = false;
		SkinToken token = skinLoader.findSkinTokenByName(name);
		// 存在，加载皮肤界面
		if (token != null) {
			// 3. 切换到主题界面，同时更新组件UI
			success = token.updateTheme(true);
			if (success) {
				//				testJSliderIcon();
				// 选择中
				skinLoader.exchangeCheckedSkinToken(token.getName());
				// 定义外观
				Skins.setLookAndFeel(token.getLookAndFeel());
				Skins.setSkinName(token.getName());
			}
		} else {
			// 设置为默认的“Metal”外观，白色
			String clazz = "com.laxcus.util.skin.GrayMetalTheme";
			SkinSheet sheet = new FlatSkinSheet();
			success = UITools.updateLookAndFeel("Metal", clazz, sheet);
			if (success) {
				int count = SkinToken.loadSkins("conf/desktop/skin/metal_gray.txt");
				success = (count > 0);
			}
			if (success) {
				skinLoader.exchangeCheckedSkinToken("gray");
				Skins.setLookAndFeel(Skins.Metal);
				Skins.setSkinName("gray"); // "gray"是脚本中的定义
			}
		}

		return success;
	}

	/**
	 * 解析许可证签名
	 * @param element
	 */
	private void splitLicenceSignature(org.w3c.dom.Element element) {
		// 如果已经定义，local.xml脚本中的忽略
		String str = getSignature();
		if (str != null) {
			return;
		}
		// 2. 不成立，从配置脚本"local.xml"中取得许可证签名
		str = XMLocal.getValue(element, LoginMark.SIGNATURE);
		if (str != null && str.trim().length() > 0) {
			setSignature(str.trim());
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
	 * 解析自动注册登录参数
	 * @param filename
	 */
	private void splitLogin(String filename) {
		org.w3c.dom.Document document = XMLocal.loadXMLSource(filename);
		if (document == null) {
			return ;
		}

		// 登录参数
		org.w3c.dom.NodeList nodes = document.getElementsByTagName(LoginMark.MARK_LOGIN);
		if (nodes.getLength() == 1) {
			org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(0);
			String yes = element.getAttribute(LoginMark.AUTO);
			boolean auto = yes.matches("^\\s*(?i)(?:YES|TRUE)\\s*$");

			// 在“YES”情况下，解析许可证
			if (auto) {
				splitLicenceSignature(element); // 解析许可证签名
			}
			Node hub = splitHubSite(element);
			User user = splitUser(element);

			// 判断有效且是ENTRANCE节点
			boolean success = (hub != null && user != null);
			if (success) {
				success = hub.isEntrance();
			}
			if (success) {
				LoginToken token = new LoginToken(auto, hub, user);
				window.setLoginToken(token);
			}
		}
	}

	/**
	 * 读取环境中的签名
	 */
	private void splitSignature() {
		String str = System.getProperty("laxcus.signature");
		if (str != null) {
			str = str.trim();
		}
		// 设置签名
		if (str != null && str.length() > 0) {
			super.setSignature(str);
		}
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
	 * 选择目录
	 * @return
	 */
	private String choiceDirectory() {
		String dir = System.getProperty("java.io.tmpdir");
		if (dir == null) {
			dir = System.getProperty("user.dir");
		}
		return dir;
	}

	/**
	 * 加载应用目录
	 * @return 
	 */
	private boolean loadApplicationDirectory() {
		// 系统应用运行目录
		String root = System.getProperty("laxcus.run.system");
		if (root != null) {
			boolean success = ApplicationPool.getInstance().setSystemRoot(root);
			if (!success) {
				return false;
			}
		} else {
			root = choiceDirectory();
			String path = "desktop" + File.separator + "run" + File.separator + "system";
			boolean success = ApplicationPool.getInstance().setSystemRoot(root, path);
			if (!success) {
				return false;
			}
		}
		// 用户应用运行目录
		root = System.getProperty("laxcus.run.user");
		if (root != null) {
			boolean success = ApplicationPool.getInstance().setUserRoot(root);
			if (!success) {
				return false;
			}
		} else {
			root = choiceDirectory();
			String path = "desktop" + File.separator + "run" + File.separator + "user";
			boolean success = ApplicationPool.getInstance().setUserRoot(root, path);
			if (!success) {
				return false;
			}
		}
		// 系统应用存储目录
		root = System.getProperty("laxcus.store.system");
		if (root != null) {
			boolean success = PlatformKit.setSystemStoreRoot(root);
			if (!success) {
				return false;
			}
		} else {
			root = choiceDirectory();
			String path = "desktop" + File.separator + "store" + File.separator + "system";
			boolean success = PlatformKit.setSystemStoreRoot(root, path);
			if (!success) {
				return false;
			}
		}
		// 用户应用存储目录
		root = System.getProperty("laxcus.store.user");
		if (root != null) {
			boolean success = PlatformKit.setUserStoreRoot(root);
			if (!success) {
				return false;
			}
		} else {
			root = choiceDirectory();
			String path = "desktop" + File.separator + "store" + File.separator + "user";
			boolean success = PlatformKit.setUserStoreRoot(root, path);
			if (!success) {
				return false;
			}
		}
		// 系统临时存储目录
		root = System.getProperty("laxcus.store.temp");
		if (root != null) {
			boolean success = PlatformKit.setSystemTemporaryRoot(root);
			if (!success) {
				return false;
			}
		} else {
			root = choiceDirectory();
			String path = "desktop" + File.separator + "store" + File.separator + "temp";
			boolean success = PlatformKit.setSystemTemporaryRoot(root, path);
			if (!success) {
				return false;
			}
		}
		
		return true;
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

		// 私有参数
		splitPrivate(document);

		// 解析边缘容器监听
		splitTubListen(document);

		// 应用目录
		boolean success = loadApplicationDirectory();
		// 解析站点配置
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
			success = loadTips("conf/desktop/tip/config.xml");
		}
		// 设置边缘容器管理池的目录和更新检查参数
		if (success) {
			splitTubPool(document);
		}

		// 加载日志并且启动
		if (success) {
			success = loadLogResourceWithLocal(filename);
		}


		// 只能是CONSOLE节点，才加载许可证，TERMINAL/DESKTOP在登录前进行
		if (success && isConsole()) {
			loadLicence(false);
		}

		return success;
	}

	/**
	 * 启动闪屏
	 */
	public void startSplash() {
		ResourceLoader loader = new ResourceLoader("conf/desktop/image/splash/");
		ImageIcon icon = null;
		// 判断中文或者其它，显示！
		if (Laxkit.isSimplfiedChinese()) {
			icon = loader.findImage("zh_CN/splash.jpg");
		} else {
			icon = loader.findImage("en_US/splash.jpg");
		}

		// 设置界面
		splash.createWindow(icon);
		// 启动界面
		splash.start();
	}

	/**
	 * 启动闪屏
	 */
	public void stopSplash() {
		splash.stop();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}

		// 如果有两个以上时. 判断有没有reset参数，有就清除旧记录
		if (args.length >= 2) {
			// 从第一个开始
			for (int i = 1; i < args.length; i++) {
				String param = args[i];
				if (param.matches("^\\s*(?i)(?:-RESET|RESET)\\s*$")) {
					ResourceReleaser rs = new ResourceReleaser();
					rs.deleteResource();
				}
			}
		}

		// 删除过期的垃圾文件
		RubbishReleaser rs = new RubbishReleaser();
		rs.deleteRubbishs();

		// 取句柄
		DesktopLauncher launcher = DesktopLauncher.getInstance();

		// 启动闪屏
		launcher.startSplash();

		// 加载“bin”目录下面和“laxcus.library”目录下面的库文件
		JNILoader.init();

		// 读取本地的环境变量
		if (!launcher.readEnvironement()) {
			launcher.stopSplash();
			Logger.error("environement parameter error!");
			Logger.gushing();
			System.exit(0);
			return;
		}

		// 加载外观和皮肤
		if (!launcher.loadSkins()) {
			launcher.stopSplash();
			Logger.error("cannot be load skin theme!");
			Logger.gushing();
			System.exit(0);
			return;
		}

		// 初始化界面UI字体
		launcher.checkPlatformFont();

		// 解析登录参数，只在Desktop有用
		String filename = args[0];
		// 解析环境中的签名，再解析"local.xml"中的登录参数
		launcher.splitSignature();
		launcher.splitLogin(filename);
		// 解析
		boolean success = launcher.loadLocal(filename);
		Logger.note("DesktopLauncher.main, load local", success);
		// 启动线程
		if (success) {
			success = launcher.start();
			Logger.note("DesktopLauncher.main, start service", success);
		}

		// 启动限制参数
		if (success) {
			launcher.loadTimerTasks(filename);
		} else {
			launcher.stopSplash();
			Logger.gushing();
			// 关闭日志
			launcher.stopLog();
			System.exit(0);
		}
	}
}


///** 窗口界面资源解析器 **/
//private SurfaceLoader surfaceLoader = new SurfaceLoader();


///**
// * 处理平台字体
// */
//public void doPlatformFont() {
//	// 检查平台字体
//	checkPlatformFont();
////	// 消息对话框文本
////	checkMessageDialog();
////	checkFileChooser();
//}

///**
// * 解析一行字体
// * @param input 输入参数
// * @return 返回字体或者空指针
// */
//private Font readFont(String input) {
//	final String regex = "^\\s*(.+?)\\s*\\,\\s*(?i)([PLAIN|BOLD|ITALIC]+)\\s*\\,\\s*([\\d]+)\\s*$";
//
//	Pattern pattern = Pattern.compile(regex);
//	Matcher matcher = pattern.matcher(input);
//	if (!matcher.matches()) {
//		return null;
//	}
//
//	String family = matcher.group(1);
//	String styleName = matcher.group(2);
//	int size = Integer.parseInt(matcher.group(3));
//
//	// 字体样式
//	int style = Font.PLAIN;
//	if (styleName.matches("^\\s*(?i)(PLAIN)\\s*$")) {
//		style = Font.PLAIN;
//	} else if (styleName.matches("^\\s*(?i)(BOLD)\\s*$")) {
//		style = Font.BOLD;
//	} else if (styleName.matches("^\\s*(?i)(ITALIC)\\s*$")) {
//		style = Font.ITALIC;
//	}
//
//	// 生成字体
//	return new Font(family, style, size);
//}

///**
// * 建立目录
// * @return
// */
//public File createConfigDirectory() {
//	String bin = System.getProperty("user.dir");
//	bin += "/../conf";
//	File file = new File(bin);
//	boolean success = (file.exists() && file.isDirectory());
//	if (!success) {
//		success = file.mkdirs();
//	}
//	return (success ? file : null);
//}

///**
// * 读平台定义字体
// * @return
// */
//private Font[] readPlatformFont() {
//	File dir = createConfigDirectory();
//	if (dir == null) {
//		return null;
//	}
//	// 配置目录下的字体文件
//	File file = new File(dir, "fonts.conf");
//	// 没有这个文件，忽略它
//	if(!(file.exists() && file.isFile())) {
//		return null;
//	}
//
//	// 从配置文件中读取全部配置
//	ArrayList<Font> array = new ArrayList<Font>();
//	try {
//		FileInputStream in = new FileInputStream(file);
//		InputStreamReader is = new InputStreamReader(in, "UTF-8");
//		BufferedReader bf = new BufferedReader(is);
//		do {
//			String line = bf.readLine();
//			if (line == null) {
//				break;
//			}
//			Font font = readFont(line);
//			if (font != null) {
//				array.add(font);
//			}
//		} while (true);
//		bf.close();
//		is.close();
//		in.close();
//	} catch (IOException e) {
//
//	}
//
//	if(array.isEmpty()) {
//		return null;
//	}
//	// 输出全部字体
//	Font[] fonts = new Font[array.size()];
//	return array.toArray(fonts);
//}

///**
// * 找到首个匹配的字体
// * @param fonts
// * @return 首选字体
// */
//private Font choiceFirst(Font[] defaultFonts, Font[] fonts) {
//	for (Font hot : defaultFonts) {
//		String hotName = hot.getName();
//		for (Font font : fonts) {
//			String name = font.getName();
//			if (name.indexOf(hotName) > -1) {
//				return hot;
//			}
//		}
//	}
//
//	return fonts[0];
//}

///**
// * 检查平台字体
// */
//private void checkPlatformFont() {
//	// 读平台上定义的字体
//	Font[] defaultFonts = readPlatformFont();
//	if (defaultFonts == null) {
//		return;
//	}
//
//	// 取出例子文本
//	String text = findContent("grade/administrator");
//	if (text == null) {
//		return;
//	}
//	// 找到合适的字体
//	Font[] fonts = FontKit.findFonts(text);
//	if (fonts == null) {
//		return;
//	}
//
//	// 首选字体
//	Font font = choiceFirst(defaultFonts, fonts);
//	FontUIResource res = new FontUIResource(font);
//	// 设置匹配的字体
//	Enumeration<Object> keys = UIManager.getDefaults().keys();
//	int index = 0;
//	int count = 0;
//	StringBuilder bf = new StringBuilder();
//	while (keys.hasMoreElements()) {
//		Object key = keys.nextElement();
//		Object value = UIManager.get(key);
//
//		if (value instanceof FontUIResource) {
//			UIManager.put(key, res);
////			System.out.printf("UI更新：%s\n", key.toString());
//		}
//		
////		if (value.getClass() == FontUIResource.class) {
////			if (bf.length() > 0) {
////				bf.append(",");
////			}
////			bf.append(key.toString());
////			
////			System.out.printf("%d FontUIResource: %s\n", index++, key.toString());
////		} else if(value.getClass() == Font.class) {
////			System.out.printf("%d Font: %s\n", index++, key.toString());
////		} else {
////			System.out.printf("%s -> %s\n", key.toString(), value.getClass().getName());
////		}
////		count++;
//		
//		if (value != null && value.getClass() == FontUIResource.class) {
//			if (bf.length() > 0) {
//				bf.append(",");
//			}
//			bf.append(key.toString());
//			index++;
//		} 
//		count++;
//	}
//	System.out.printf("All UIResource: %d, Font Resources:%d\n", count, index);
//	System.out.println(bf.toString());
//	
//	try {
//		FileWriter out = new FileWriter("c:/fonts.txt");
//		out.write(bf.toString());
//		out.close();
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	
////	// nimbus界面字体
////	doNimbusFont(res);
//}

///**
// * 检查平台字体
// */
//private void checkPlatformFont() {
//	// 读平台上定义的字体
//	Font[] defaultFonts = readPlatformFont();
//	if (defaultFonts == null) {
//		Font font = UIKit.readFont(RTEnvironment.ENVIRONMENT_SYSTEM, "FONT/SYSTEM");
//		if (font != null) {
//			System.out.printf("当前系统环境字体是：%s\n", font.getName());
//			defaultFonts = new Font[] { font };
//		}
//	}
//	if (defaultFonts == null) {
//		return;
//	}
//
//	// 取出例子文本
//	String text = findContent("grade/administrator");
//	if (text == null) {
//		return;
//	}
//	// 找到合适的字体
//	Font[] fonts = FontKit.findFonts(text);
//	if (fonts == null) {
//		return;
//	}
//
//	// 首选字体
//	Font font = choiceFirst(defaultFonts, fonts);
//	UITools.updateSystemFonts(font);
//	if (Skins.isNimbus()) {
//		UITools.updateNimbusSystemFonts(font);
//	} else {
//		UITools.updateMetalSystemFonts(font);
//	}
//	
//////	FontUIResource res = new FontUIResource(font);
////	FontUIResource res = new FontUIResource(font.getName(), font.getStyle(), font.getSize());
////	// 设置匹配的字体
////	Enumeration<Object> keys = UIManager.getDefaults().keys();
////	while (keys.hasMoreElements()) {
////		Object key = keys.nextElement();
////		Object value = UIManager.get(key);
////
////		if (value instanceof FontUIResource) {
////			UIManager.put(key, res);
//////			System.out.printf("UI更新：%s\n", key.toString());
////		}
////	}
////	
//////	// nimbus界面字体
//////	doNimbusFont(res);
//}

//public void doNimbusFont(FontUIResource res) {
//	// 这些是NIUMBUS界面的字体KEY
//	String input = "Table.font,Label.font,OptionPane.font,List.font,MenuItem.acceleratorFont,RadioButtonMenuItem.acceleratorFont,Menu.acceleratorFont,Spinner.font,ToolBar.font,TableHeader.font,CheckBox.font,FormattedTextField.font,Viewport.font,MenuItem.font,Button.font,MenuBar.font,ScrollPane.font,ProgressBar.font,RadioButton.font,CheckBoxMenuItem.font,ToggleButton.font,TextArea.font,InternalFrame.titleFont,PasswordField.font,CheckBoxMenuItem.acceleratorFont,EditorPane.font,TextPane.font,TabbedPane.font,Menu.font,DesktopIcon.font,TextField.font,ColorChooser.font,PopupMenu.font,RadioButtonMenuItem.font,ToolTip.font,TitledBorder.font,Panel.font,Tree.font,Slider.font,ComboBox.font,Panel.font,FormattedTextField.font,ScrollPane.font,Table.font,CheckBoxMenuItem.font,FileChooser.font,TextArea.font,PopupMenu.font,TitledBorder.font,SplitPane.font,EditorPane.font,Spinner.font,SliderTrack.font,CheckBox.font,MenuBar.font,Tree.font,ArrowButton.font,MenuItem.font,Menu.font,DesktopIcon.font,ScrollBar.font,ComboBox.font,InternalFrameTitlePane.font,Viewport.font,DesktopPane.font,ColorChooser.font,TabbedPane.font,PopupMenuSeparator.font,SliderThumb.font,RadioButtonMenuItem.font,TextField.font,Separator.font,PasswordField.font,ToolBar.font,ToolTip.font,InternalFrame.font,RootPane.font,ToggleButton.font,List.font,Label.font,defaultFont,ScrollBarTrack.font,TextPane.font,OptionPane.font,ProgressBar.font,Slider.font,RadioButton.font,ScrollBarThumb.font,InternalFrame.titleFont,TableHeader.font,Button.font";
//	String[] texts = input.split(",");
//	for (String key : texts) {
//		UIManager.put(key, res);
////		System.out.printf("set：%s\n", key);
//	}
//}

///**
// * 消息对话框按纽文本
// */
//private void checkMessageDialog() {
//	String text = findContent("MessageDialog/Button/Okay");
//	UIManager.put("OptionPane.okButtonText", text);
//	text = findContent("MessageDialog/Button/Yes");
//	UIManager.put("OptionPane.yesButtonText", text);
//	text = findContent("MessageDialog/Button/No");
//	UIManager.put("OptionPane.noButtonText", text);
//	text = findContent("MessageDialog/Button/Cancel");
//	UIManager.put("OptionPane.cancelButtonText", text);
//
//	ResourceLoader loader = new ResourceLoader();
//	ImageIcon icon = loader.findImage("conf/desktop/image/message/question.png", 32, 32);
//	UIManager.put("OptionPane.questionIcon", icon);
//	icon = loader.findImage("conf/desktop/image/message/info.png", 32, 32);
//	UIManager.put("OptionPane.informationIcon", icon);
//	icon = loader.findImage("conf/desktop/image/message/error.png", 32, 32);
//	UIManager.put("OptionPane.errorIcon", icon);
//	icon = loader.findImage("conf/desktop/image/message/warning.png", 32, 32);
//	UIManager.put("OptionPane.warningIcon", icon);
//}
//
///**
// * 文件选择窗口
// */
//private void checkFileChooser() {
//	String text = findContent("FileChooser/Button/Save");
//	UIManager.put("FileChooser.saveButtonText", text);
//
//	text = findContent("FileChooser/Button/Open");
//	UIManager.put("FileChooser.openButtonText", text);
//
//	text = findContent("FileChooser/Button/Cancel");
//	UIManager.put("FileChooser.cancelButtonText", text);
//
//	text = findContent("FileChooser/Button/Help");
//	UIManager.put("FileChooser.helpButtonText", text);
//
//	text = findContent("FileChooser/Button/Update");
//	UIManager.put("FileChooser.updateButtonText", text);
//}


///**
// * 从总配置文件中选出所关联语言的界面配置文件路径
// * @return JAR文件中的资源文件路径
// */
//public String getSurfacePath() {
//	LocalSelector selector = new LocalSelector("conf/desktop/lang/config.xml");
//	return selector.findPath("resource");
//}

///*
// * (non-Javadoc)
// * @see com.laxcus.util.local.LocalMatcher#findCaption(java.lang.String)
// */
//@Override
//public String findCaption(String xmlPath) {
//	if (!surfaceLoader.isLoaded()) {
//		surfaceLoader.load(getSurfacePath());
//	}
//	return surfaceLoader.getAttribute(xmlPath);
//}
//
///*
// * (non-Javadoc)
// * @see com.laxcus.util.local.LocalMatcher#findContent(java.lang.String)
// */
//@Override
//public String findContent(String xmlPath) {
//	if (!surfaceLoader.isLoaded()) {
//		surfaceLoader.load(getSurfacePath());
//	}
//	return surfaceLoader.getContent(xmlPath);
//}


//	/**
//	 * 显示许可证
//	 * @param content
//	 * @return
//	 */
//	public boolean showLicence(String content) {
//		TerminalLicenceDialog dialog = new TerminalLicenceDialog(window, true, content);
//		dialog.showDialog();
//		return dialog.isAccpeted();
//	}


//	/**
//	 * 限制闪烁
//	 * @return
//	 */
//	private boolean isLimitLight() {
//		// 规定必须登录成功1分钟之后，才允许闪烁图标
//		// 通过这个限制，判断GUI界面卡死的问题是不是SWING线程造成的？
//		long time = getLoginTime();
//		return (time > 0 && System.currentTimeMillis() >= time + 60000);
//	}

//		if (!isLimitLight()) {
//			return;
//		}


//		// 记录最大日志数目
//		DesktopProperties.writeLogElements(logs);


//	@SuppressWarnings("unchecked")
//	private void printFromClass() throws SecurityException,
//			NoSuchFieldException, IllegalArgumentException,
//			IllegalAccessException {
//		Field field = ClassLoader.class.getDeclaredField("nativeLibraries");
//		field.setAccessible(true);
//
//		Vector<Object> objects = (Vector<Object>) field.get(getClass().getClassLoader());
//		Iterator<Object> iterator = objects.iterator();
//
//		while (iterator.hasNext()) {
//			Object object = iterator.next();
//			Field[] fields = object.getClass().getDeclaredFields();
//			
//			// 判断成员数
//			int size = (fields == null ? 0 : fields.length);
//			for (int i = 0; i < size; i++) {
//				Field element = fields[i];
//				
//				// 只有变量名“fromClass”，其它忽略
//				if (element.getName().equals("fromClass")) {
//					element.setAccessible(true);
//					Object obj = element.get(object);
//					System.out.printf("DesktopLauncher, fromClass is %s\n", obj.toString());
//				}
//			}
//		}
//	}


//	/**
//	 * 从本地配置中读取皮肤配置名称
//	 * @return 返回字符串或者空指针
//	 */
//	private String readSkinName() {
//		// 建立配置目录
//		File dir = createDirectory();
//
//		File file = new File(dir, "desktop.conf");
//		// 读磁盘文件
//		UITacker tracker = new UITacker();
//		int who = tracker.read(file);
//		if (who < 1) {
//			return null;
//		}
//		// 找到"skin.name"对应值
//		return tracker.getString(DesktopProperties.skinName); // "skin.name");
//	}


//	/**
//	 * 写入FRONT.Desktop节点参数配置
//	 */
//	private void writeConfigure() {
//		UITacker tracker = new UITacker();
//
//		// 子窗口范围
//		tracker.put(DesktopProperties.ConsoleBound, DesktopProperties.readConsoleBound());
//		tracker.put(DesktopProperties.LogBound, DesktopProperties.readLogBound());
//		tracker.put(DesktopProperties.HelpBound, DesktopProperties.readHelpBound());
//
//		//		// 窗口范围
//		//		tracker.put(DesktopProperties.boundFrame, DesktopProperties.readWindowBound());
//
//		// 分割线
//		tracker.put(DesktopProperties.dividerBrowserPane, DesktopProperties.readBrowserPaneDeviderLocation());
//		tracker.put(DesktopProperties.dividerCenterPane, DesktopProperties.readCenterPaneDeviderLocation());
//		// 云端浏览面板分割线
//		tracker.put(DesktopProperties.dividerRemoteDataPane, DesktopProperties.readRemoteDataPaneDeviderLocation());
//		// 云端应用软件面板分割线
//		tracker.put(DesktopProperties.dividerRemoteSoftwarePane, DesktopProperties.readRemoteSoftwarePaneDeviderLocation());
//		// 本地浏览面板分割线
//		tracker.put(DesktopProperties.dividerLocalBrowserPane, DesktopProperties.readLocalBrowserPaneDeviderLocation());
//
//		// 系统字体
//		tracker.put(DesktopProperties.fontSystem, DesktopProperties.readSystemFont()); 
//
//		// 云端数据字体
//		tracker.put(DesktopProperties.fontRemoteData, DesktopProperties.readRemoteDataFont());
//		// 云端应用软件字体
//		tracker.put(DesktopProperties.fontRemoteSoftware, DesktopProperties.readRemoteSoftwareFont());
//		// 本地浏览窗口
//		tracker.put(DesktopProperties.fontLocalBrowser, DesktopProperties.readLocalBrowserFont());
//
//		tracker.put(DesktopProperties.fontTabbed, DesktopProperties.readTabbedFont());
//		tracker.put(DesktopProperties.fontCommand, DesktopProperties.readCommandPaneFont());
//		tracker.put(DesktopProperties.fontMessage, DesktopProperties.readTabbedMessageFont());
//		tracker.put(DesktopProperties.fontTable, DesktopProperties.readTabbedTableFont());
//		tracker.put(DesktopProperties.fontGraph, DesktopProperties.readTabbedGraphFont());
//		tracker.put(DesktopProperties.fontLog, DesktopProperties.readTabbedLogFont());
//		// 主菜单字体
//		tracker.put(DesktopProperties.fontMenu,DesktopProperties.readMainMenuFont());
//
//		// 帮助字体类型
//		String helpFamily = context.getTemplateFontFamily();
//		tracker.put(DesktopProperties.fontHelp, helpFamily);
//		// 播放声音
//		boolean play = SoundPlayer.getInstance().isPlay();
//		tracker.put(DesktopProperties.soundPlay, play);
//		// 显示的日志数目
//		tracker.put(DesktopProperties.logElements, DesktopProperties.readLogElements());
//		// 拒绝显示日志
//		tracker.put(DesktopProperties.logForbid, DesktopProperties.readLogForbid());
//
//		// 界面皮肤颜色名称
//		SkinToken token = skinLoader.findCheckedSkinToken();
//		if (token != null) {
//			tracker.put(DesktopProperties.skinName, token.getName());
//		}
//
//		// 配置参数写入指定的目录
//		File dir = createDirectory();
//		if (dir != null) {
//			File file = new File(dir, "desktop.conf");
//			// 写入磁盘
//			tracker.write(file);
//		}
//	}


///**
// * 读写FRONT.Desktop节点参数配置
// * @return 成功返回真，否则假
// */
//private boolean readConfigure() {
//	// 选择默认字体做为显示字体
//	Font[] defaultFonts = readPlatformFont();
//	if (defaultFonts != null) {
//		//			context.setTemplateFontFamily(defaultFonts[0].getFamily());
//	}
//
//	// 读本地配置文件
//	File dir = createDirectory();
//	if (dir == null) {
//		return false;
//	}
//	File file = new File(dir, "desktop.conf");
//
//	// 读磁盘文件
//	UITacker tracker = new UITacker();
//	int who = tracker.read(file);
//	if (who < 1) {
//		return false;
//	}
//
//	// 子窗口范围
//	Rectangle rect = tracker.getRectangle(DesktopProperties.ConsoleBound);
//	if (rect != null) {
//		DesktopProperties.writeConsoleBound(rect);
//	}
//	rect = tracker.getRectangle(DesktopProperties.LogBound);
//	if (rect != null) {
//		DesktopProperties.writeLogBound(rect);
//	}
//	rect = tracker.getRectangle(DesktopProperties.HelpBound);
//	if (rect != null) {
//		DesktopProperties.writeHelpBound(rect);
//	}
//
//	//		// 窗口范围
//	//		Rectangle rect = tracker.getRectangle(DesktopProperties.boundFrame); 
//	//		if (rect != null) {
//	//			DesktopProperties.writeWindowBound(rect);
//	//		}
//
//	// 中央面板分割线位置
//	Integer pixel = tracker.getInteger(DesktopProperties.dividerCenterPane);
//	if (pixel != null) {
//		DesktopProperties.writeCenterPaneDeviderLocation(pixel.intValue());
//	}
//	// 浏览面板分割线位置
//	pixel = tracker.getInteger(DesktopProperties.dividerBrowserPane);
//	if (pixel != null) {
//		DesktopProperties.writeBrowserPaneDeviderLocation(pixel.intValue());
//	}
//
//	// 云浏览器面板上下分割线
//	pixel = tracker.getInteger(DesktopProperties.dividerRemoteDataPane);
//	if (pixel != null) {
//		DesktopProperties.writeRemoteDataPaneDeviderLocation(pixel.intValue());
//	}
//	// 云浏览器面板上下分割线
//	pixel = tracker.getInteger(DesktopProperties.dividerRemoteSoftwarePane);
//	if (pixel != null) {
//		DesktopProperties.writeRemoteSoftwarePaneDeviderLocation(pixel.intValue());
//	}
//	// 本地浏览面板上下分割线
//	pixel = tracker.getInteger(DesktopProperties.dividerLocalBrowserPane);
//	if (pixel != null) {
//		DesktopProperties.writeLocalBrowserPaneDeviderLocation(pixel.intValue());
//	}
//
//	// 系统环境字体
//	Font font = tracker.getFont(DesktopProperties.fontSystem);
//	if (font != null) {
//		// 更新系统环境字体
//		UITools.updateSystemFonts(font);
//		DesktopProperties.writeSystemFont(font);
//	}
//	// 云端数据窗口字体
//	font = tracker.getFont(DesktopProperties.fontRemoteData);
//	if (font != null) {
//		DesktopProperties.writeRemoteDataFont(font);
//	}
//	// 云端软件窗口字体
//	font = tracker.getFont(DesktopProperties.fontRemoteSoftware);
//	if (font != null) {
//		DesktopProperties.writeRemoteSoftwareFont(font);
//	}
//	// 选项卡字体
//	font = tracker.getFont(DesktopProperties.fontTabbed);
//	if (font != null) {
//		DesktopProperties.writeTabbedFont(font);
//	}
//	// 命令字体
//	font = tracker.getFont(DesktopProperties.fontCommand); 
//	if (font != null) {
//		DesktopProperties.writeCommandPaneFont(font);
//	}
//	// 消息字体
//	font = tracker.getFont(DesktopProperties.fontMessage);
//	if (font != null) {
//		DesktopProperties.writeTabbedMessageFont(font);
//	}
//	// 表格字体
//	font = tracker.getFont(DesktopProperties.fontTable); 
//	if (font != null) {
//		DesktopProperties.writeTabbedTableFont(font);
//	}
//	// 图像状态字体
//	font = tracker.getFont(DesktopProperties.fontGraph); 
//	if (font != null) {
//		DesktopProperties.writeTabbedGraphFont(font);
//	}
//	// 日志字体
//	font = tracker.getFont(DesktopProperties.fontLog); 
//	if (font != null) {
//		DesktopProperties.writeTabbedLogFont(font);
//	}
//	// 主菜单字体
//	font = tracker.getFont(DesktopProperties.fontMenu); 
//	if (font != null) {
//		DesktopProperties.writeMainMenuFont(font);
//	}
//
//	/**以下可用 **/
//
//	// 帮助字体类型
//	String helpFamily = tracker.getString(DesktopProperties.fontHelp);
//	if (helpFamily != null) {
//		DesktopProperties.writeHelpMenuFontFamily(helpFamily);
//		//			DesktopLauncher.getInstance().getCommentContext().setTemplateFontFamily(helpFamily);
//	}
//	// 声音
//	Boolean play = tracker.getBoolean(DesktopProperties.soundPlay); 
//	if (play != null) {
//		boolean yes = play.booleanValue();
//		DesktopProperties.writeSoundPlay(yes);
//		SoundPlayer.getInstance().setPlay(yes);
//	}
//	// 日志显示数目
//	Integer logs = tracker.getInteger(DesktopProperties.logElements);
//	if (logs != null) {
//		DesktopProperties.writeLogElements(logs.intValue());
//	}
//	// 拒绝显示日志
//	Boolean forbid = tracker.getBoolean(DesktopProperties.logForbid);
//	if (forbid != null) {
//		boolean yes = forbid.booleanValue();
//		DesktopProperties.writeLogForbid(yes);
//	}
//
//	return true;
//}


//	private void testLibraries() {
//		try {
//			BasketSystem.showLibrary();
//			
////			BasketSystem.freeLibrary("ruba.dll");
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}

//private void testFont() {
//	Font font = new Font("新宋体", Font.PLAIN, 12);
//	FontUIResource res = new FontUIResource(font);
//	// 设置匹配的字体
//	Enumeration<Object> keys = UIManager.getDefaults().keys();
//	while (keys.hasMoreElements()) {
//		Object key = keys.nextElement();
//		Object value = UIManager.get(key);
//
//		if (value instanceof FontUIResource) {
//			UIManager.put(key, res);
//		}
//	}
//}


// 特别注意：conf/fonts.conf文件在存在，给环境设置一个默认的字体，否则GUI界面会出现这种小BUG

//		// 测试加载的动态链接库
//		launcher.testLibraries();

//	// 将颜色加载到内存集合中
//	launcher.loadColors();


//	/**
//	 * 消息对话框按纽文本
//	 */
//	private void checkMessageDialog() {
//		String text = findContent("MessageDialog/Button/Okay");
//		UIManager.put("OptionPane.okButtonText", text);
//		text = findContent("MessageDialog/Button/Yes");
//		UIManager.put("OptionPane.yesButtonText", text);
//		text = findContent("MessageDialog/Button/No");
//		UIManager.put("OptionPane.noButtonText", text);
//		text = findContent("MessageDialog/Button/Cancel");
//		UIManager.put("OptionPane.cancelButtonText", text);
//
//		ResourceLoader loader = new ResourceLoader();
//		ImageIcon icon = loader.findImage("conf/front/terminal/image/message/question.png", 32, 32);
//		UIManager.put("OptionPane.questionIcon", icon);
//		icon = loader.findImage("conf/front/terminal/image/message/info.png", 32, 32);
//		UIManager.put("OptionPane.informationIcon", icon);
//		icon = loader.findImage("conf/front/terminal/image/message/error.png", 32, 32);
//		UIManager.put("OptionPane.errorIcon", icon);
//		icon = loader.findImage("conf/front/terminal/image/message/warning.png", 32, 32);
//		UIManager.put("OptionPane.warningIcon", icon);
//	}


//	/**
//	 * 弹出确认窗口
//	 * @param content 显示文本
//	 * @return 接受返回真，否则假
//	 */
//	private boolean showConfirmDialog(String content) {		
//		String title = window.getTitle();
////		public static boolean showYesNoDialog(Component frame, String title, String content) {
//		
//		int who = MessageBox.showYesNoDialog(window, title, content);
//		
////		int who = MessageDialog.showMessageBox(window, title,
////				JOptionPane.QUESTION_MESSAGE, content, JOptionPane.YES_NO_OPTION);
//		
//		return (who == JOptionPane.YES_OPTION);
//	}

