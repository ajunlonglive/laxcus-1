/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.status;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.front.desktop.*;
import com.laxcus.front.desktop.panel.*;
import com.laxcus.gui.component.*;
import com.laxcus.gui.frame.*;
import com.laxcus.platform.*;
import com.laxcus.site.*;
import com.laxcus.thread.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.net.*;
import com.laxcus.util.skin.*;
import com.laxcus.util.sound.*;

/**
 * 状态栏面板
 * 
 * @author scott.liang
 * @version 1.1 8/25/2012
 * @since laxcus 1.0
 */
public class DesktopStatusBar extends DesktopPanel implements FrameBar, PlatformStatusBar {

	private static final long serialVersionUID = 9218985720382755478L;

	/** 启动按纽，位于左侧 **/
	private StartButton cmdStart = new StartButton();

	/** 最小化桌面按纽 **/
	private MiniButton cmdMini = new MiniButton();

	/** 播放声音 **/
	private JLabel lblSound = new JLabel();

	/** 声音图标，播放/停止 **/
	private ImageIcon soundPlay, soundStop;

	/** 服务器登录状态 **/
	private JLabel lblHub = new JLabel();

	/** 图标：闪烁两个/连接/失效，和HUB相关的图标 **/
	private ImageIcon flashLight, flashDrak, connect, disconnect;

	/** 失效文本提示 **/
	private String disconnectTip;

	/** 级别 **/
	private JLabel lblRank = new JLabel();

	/** 状态对应图标 **/
	private ImageIcon dba, user, offline;

	/** 应用窗口条，窗口界面在状态条的适配 **/
	private DesktopFrameBar frameBar;

	/** 边缘容器地址 **/
	private JLabel lblTub = new JLabel();
	
	/** 应用托盘 **/
	private JLabel lblTray = new JLabel();
	
	/** 托管唤醒器 **/
	private TrayController trayController;

	/** invoker图标 **/
	private ImageIcon invokerLight, invokerDark;

	/** invoker执行发生这个信息 **/
	private JLabel lblInvoker = new JLabel();

	/** 时间，显示“小时：分钟”，定时刷新 **/
	private JLabel lblTime = new JLabel();

	/** 集群任务统计前缀字符 **/
	private String invokerPrefix;

	/** 数据库管理/用户/没有账号 **/
	private String dbaTip, userTip, offlineTip;

	/** 守护线程，处理定时显示的参数 **/
	private MonitorThread monitorThread = new MonitorThread();

	/**
	 * 构造标准的状态栏面板
	 */
	public DesktopStatusBar() {
		super();
		frameBar = new DesktopFrameBar();
		trayController = new TrayController(this);
	}
	
	/**
	 * 输出全部运行中的窗口句柄
	 * @return LightFrame数组，没有是0长度LightFrame数组
	 */
	public LightFrame[] getLightFrames() {
		return frameBar.getLightFrames();
	}

	/**
	 * 启动任务线程
	 */
	public void startDaemonThread() {
		monitorThread.start();
	}

	/**
	 * 停止任务线程
	 */
	public void stopDaemonThread() {
		monitorThread.stop();
	}

	//	/**
	//	 * 取出图标
	//	 * @param jarPath 图片在JAR的路径
	//	 * @param width 宽度
	//	 * @param height 高度
	//	 * @return 返回图标对象
	//	 */
	//	private ImageIcon findImage(String jarPath, int width, int height) {
	//		// 图标资源，在JAR文档中
	//		ResourceLoader rs = new ResourceLoader("conf/desktop/image/window/status/");
	//		return rs.findImage(jarPath, width, height);
	//	}
	//
	//	/**
	//	 * 取出图标，默认是16*16的图标
	//	 * @param jarPath 图标名
	//	 * @return 返回图标实例
	//	 */
	//	private ImageIcon findImage(String jarPath) {
	//		return findImage(jarPath, 16, 16);
	//	}

	/**
	 * 声音鼠标适配器
	 *
	 * @author scott.liang
	 * @version 1.0 9/8/2019
	 * @since laxcus 1.0
	 */
	class SoundMouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			// 判断是声音图标
			if (e.getSource() == lblSound) {
				addThread(new SoundExchangeThread());
			}
		}
	}

	/**
	 * 设置中断图标
	 */
	public void setDisconnectIcon() {
		// 设置无效图标
		ShowHubThread thread = new ShowHubThread(disconnect, disconnectTip);
		monitorThread.addEvent(thread);
	}

	/**
	 * 设置已经连接图标
	 */
	public void setConnectIcon() {
		Node hub = DesktopLauncher.getInstance().getHub();
		ShowHubThread thread = new ShowHubThread(connect, hub.toString());
		monitorThread.addEvent(thread);
	}

	/**
	 * 设置HUB图标提示
	 * @param text
	 */
	public void setHubTip(String text) {
		String tip = "";
		if (text != null) {
			tip = text;
		}
		ShowHubThread thread = new ShowHubThread(null, tip);
		monitorThread.addEvent(thread);
	}


	//	/**
	//	 * 设置状态栏显示文本，先保存到缓存再显示...
	//	 * @param text 输入文本，允许空指针
	//	 */
	//	public void setStatusText(String text) {
	//		ShowStatusText thread = new ShowStatusText(text == null ? "" : text);
	//		monitorThread.addEvent(thread);
	//	}

	/**
	 * 设置管理员图标
	 */
	public void setAdministratorIcon() {
		ShowRankThread thread = new ShowRankThread(dba, dbaTip);
		monitorThread.addEvent(thread);
	}

	/**
	 * 设置普通注册用户图标
	 */
	public void setUserIcon() {
		ShowRankThread thread = new ShowRankThread(user, userTip);
		monitorThread.addEvent(thread);
	}

	/**
	 * 无定义的身份图标
	 */
	public void setOfflineIcon() {
		ShowRankThread thread = new ShowRankThread(offline, offlineTip);
		monitorThread.addEvent(thread);
	}

	/**
	 * 状态栏图标闪烁线程
	 *
	 * @author scott.liang
	 * @version 1.0 3/2/2020
	 * @since laxcus 1.0
	 */
	class FlashThread extends SwingEvent {
		/** 图标ID **/
		int id;

		public FlashThread(int who) {
			super(true); // // 以"SwingUtilities.invokeAndWait"同步方式处理
			id = who;
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.display.SwingEvent#process()
		 */
		@Override
		public void process() {
			doFlash(id);
		}
	}

	/**
	 * 这个方法直接被DesktopWindow的事件线程调用
	 * @param who 图标编号
	 */
	private void doFlash(int who) {
		ImageIcon icon = null;
		String tooltip = null;

		// 判断处于连网状态
		boolean login = DesktopLauncher.getInstance().isLogined();
		// 选择图标
		if (login) {
			if (who == DesktopFlashTag.FLASH_START) {
				icon = flashLight;
			} else if (who == DesktopFlashTag.FLASH_NEXT) {
				icon = flashDrak;
			} else if (who == DesktopFlashTag.FLASH_STOP) {
				// 连接有效图标
				icon = connect;
			}
		} else {
			// 连接失效图和文字
			icon = disconnect;
			tooltip = disconnectTip;
		}
		if (icon != null) {
			lblHub.setIcon(icon);
		}
		if (tooltip != null) {
			FontKit.setToolTipText(lblHub, tooltip);
		}
	}

	/**
	 * 闪烁图标
	 */
	public void flash() {
		// 不显示时...
		if (!isShowing() || !isVisible()) {
			return;
		}

		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();

		// 图标编号
		int[] icons = new int[] { DesktopFlashTag.FLASH_START, DesktopFlashTag.FLASH_NEXT };

		// 线程之间的间隔时间是800毫秒
		final int interval = 800;
		long touchTime = System.currentTimeMillis();

		// 播放动画效果，只产生一次迭代
		for (int iterate = 0; iterate < 1; iterate++) {
			for (int i = 0; i < icons.length; i++) {
				FlashThread event = new FlashThread(icons[i]);
				event.setTouchTime(touchTime);
				array.add(event);
				// 下一次触发时间
				touchTime += interval;
			}
		}

		// 多延时200毫秒，效果更好！
		FlashThread last = new FlashThread(DesktopFlashTag.FLASH_STOP);
		last.setTouchTime(touchTime + 200);
		array.add(last);

		// 一批线程放入队列
		SwingDispatcher.invokeThreads(array);
	}

	//	/**
	//	 * 闪烁图标
	//	 */
	//	public void flash() {
	//		// 不显示时...
	//		if (!isShowing() || !isVisible()) {
	//			return;
	//		}
	//		
	////		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();
	//
	//		// 图标编号
	//		int[] icons = new int[] { DesktopFlashTag.FLASH_START, DesktopFlashTag.FLASH_NEXT };
	//
	//		// 线程之间的间隔时间是800毫秒
	//		final int interval = 800;
	//		long touchTime = System.currentTimeMillis();
	//
	//		// 播放动画效果，只产生一次迭代
	//		for (int iterate = 0; iterate < 1; iterate++) {
	//			for (int i = 0; i < icons.length; i++) {
	//				FlashThread event = new FlashThread(icons[i]);
	//				event.setIntervalTime(interval);
	//				event.setTouchTime(touchTime);
	////				array.add(event);
	//				SwingDispatcher.invokeThread(event);
	//				// 下一次触发时间
	//				touchTime += interval;
	//			}
	//		}
	//
	//		// 多延时200毫秒，效果更好！
	//		FlashThread last = new FlashThread(DesktopFlashTag.FLASH_STOP);
	//		last.setIntervalTime(interval + 200);
	//		last.setTouchTime(touchTime + 200);
	//		SwingDispatcher.invokeThread(last);
	//		
	////		array.add(last);
	//
	////		// 一批线程放入队列
	////		SwingDispatcher.invokeThreads(array);
	//	}

	/** 滚动 **/
	private volatile boolean invokerRolling = false;

	/** INVOKER IO统计 **/
	private volatile long rollingCount = 0;

	/** 接收的数据流量 **/
	private volatile long receiveFlows = 0;

	/** 发送的数据流量 **/
	private volatile long sendFlows = 0;

	/**
	 * 判断是光标切换中
	 * @return 返回真或者假
	 */
	public boolean isInvokerRolling() {
		return invokerRolling;
	}

	/**
	 * 增加一个任务统计数
	 */
	public void addInvokerRolling() {
		rollingCount++;
	}

	/**
	 * 增加数据流
	 * @param rf
	 * @param sf
	 */
	public void addFlows(long rf, long sf) {
		if (rf > 0) {
			receiveFlows += rf;
		}
		if (sf > 0) {
			sendFlows += sf;
		}
	}

	/**
	 * 调用器闪烁图标
	 * @param rolling 闪烁
	 */
	private void doRolling(boolean rolling) {
		ImageIcon icon = null;

		boolean login = DesktopLauncher.getInstance().isLogined();
		if (login) {
			if (rolling) {
				icon = invokerLight;
				invokerRolling = true;
			} else {
				icon = invokerDark;
				invokerRolling = false;
			}
		} else {
			// 连接失效图和文字
			icon = invokerDark;
			invokerRolling = false;
		}

		if (icon != null) {
			lblInvoker.setIcon(icon);
		}
		// 只在不闪烁时更新
		if (!rolling) {
			updateInvokerToolTipText();
		}
	}

	class ResetRollingThread extends SwingEvent {

		ResetRollingThread() {
			super(true);
		}

		public void process() {
			rollingCount = 0;
			receiveFlows = 0;
			sendFlows = 0;
		}
	}

	class RollingThread extends SwingEvent {
		boolean rolling;

		RollingThread(boolean b) {
			super(true); // 将调用"SwingUtilities.invokeAndWait"方法
			rolling = b;
		}
		public void process() {
			doRolling(rolling);
		}
	}

	//	/**
	//	 * 流量图标闪烁
	//	 */
	//	public void rolling() {
	//		// 不显示，忽略
	//		if (!isShowing() || !isVisible()) {
	//			return;
	//		}
	//
	//		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();
	//
	//		// 线程之间的间隔时间是800毫秒
	//		final int interval = 800;
	//		long touchTime = System.currentTimeMillis();
	//
	//		// 播放动画效果
	//		RollingThread light = new RollingThread(true);
	//		light.setTouchTime(touchTime);
	//		array.add(light);
	//
	//		// 下一次触发时间
	//		touchTime += interval;
	//
	//		// 多延时200毫秒，效果更好！
	//		RollingThread dark = new RollingThread(false);
	//		dark.setTouchTime(touchTime + 200);
	//		array.add(dark);
	//
	//		// 一批线程放入队列
	//		SwingDispatcher.invokeThreads(array);
	//	}

	//	private RollingThread lightRoll = new RollingThread(true);
	//	private RollingThread darkRoll = new RollingThread(false);

	/** 进入闪烁状态 **/
	private volatile boolean intoRolling = false;

	class DarkRollingAdapter extends SwingStageAdapter {
		@Override
		public void callExit(EventObject e) {
			intoRolling = false;
		}
	}

	/**
	 * 流量图标闪烁
	 */
	public void rolling() {
		// 不显示，忽略
		if (!isShowing() || !isVisible()) {
			return;
		}
		// 已经进入状态时...
		if (intoRolling) {
			return;
		}

		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();

		RollingThread light = new RollingThread(true);
		RollingThread dark = new RollingThread(false);
		dark.setSwingStageListener(new DarkRollingAdapter()); // 增加事件监听接口

		long now = System.currentTimeMillis();
		// 播放动画效果，点亮
		light.setTouchTime(now);
		// 延时1000毫秒，恢复原来的状态
		dark.setTouchTime(now + 1000);

		// 保存到数组里
		array.add(light);
		array.add(dark);

		// 进入闪烁状态，把线程放入队列
		intoRolling = true;
		SwingDispatcher.invokeThreads(array);
	}


	//	/**
	//	 * 流量图标闪烁
	//	 */
	//	public void rolling() {
	//		// 不显示，忽略
	//		if (!isShowing() || !isVisible()) {
	//			return;
	//		}
	//
	//		//		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();
	//
	//		// 线程之间的间隔时间是800毫秒
	//		final int interval = 800;
	//		long touchTime = System.currentTimeMillis();
	//
	//		// 播放动画效果
	//		RollingThread light = new RollingThread(true);
	//		light.setIntervalTime(interval);
	//		light.setTouchTime(touchTime);
	//		SwingDispatcher.invokeThread(light);
	//		//		array.add(light);
	//
	//		// 下一次触发时间
	//		touchTime += interval;
	//
	//		// 多延时200毫秒，效果更好！
	//		RollingThread dark = new RollingThread(false);
	//		dark.setIntervalTime(interval + 200);
	//		dark.setTouchTime(touchTime + 200);
	//		SwingDispatcher.invokeThread(dark);
	//
	//		//		array.add(dark);
	//
	//		//		// 一批线程放入队列
	//		//		SwingDispatcher.invokeThreads(array);
	//	}

	/**
	 * 重置参数
	 */
	public void resetRolling() {
		monitorThread.addEvent(new ResetRollingThread());
	}

	private void updateInvokerToolTipText() {
		String rf = ConfigParser.splitCapacity(receiveFlows);
		String sf = ConfigParser.splitCapacity(sendFlows);
		int threads = Thread.activeCount();
		int runTasks = InvokerTrustor.getThreadCount();
		String tooltip = String.format(invokerPrefix, threads, runTasks, rollingCount, rf, sf);
		FontKit.setToolTipText(lblInvoker, tooltip);
	}

	/**
	 * 增加启动按纽的单击事件，由DesktopWindow设置
	 * @param listener
	 */
	public void addStartButtonActionListener(ActionListener listener) {
		cmdStart.addActionListener(listener);
	}

	/**
	 * 增加最小按纽的单击事件，由DesktopWindow设置
	 * @param listener
	 */
	public void addMiniButtonActionListener(ActionListener listener) {
		cmdMini.addActionListener(listener);
	}

	/**
	 * 初始化启动按纽
	 */
	private void initStartButton() {
		// 启动按纽
		ImageIcon icon = (ImageIcon) UIManager.getIcon("StatusBar.StartButtonIcon");
		icon = ImageUtil.dark(icon, -12); // 颜色调暗
		String text = UIManager.getString("StatusBar.StartButtonText");
		String tooltip = UIManager.getString("StatusBar.StartButtonTitle");
		cmdStart.setBorderPainted(false);
		cmdStart.setFocusPainted(false);
		cmdStart.setContentAreaFilled(false);
		cmdStart.setIcon(icon, 16);
		cmdStart.setText(text);
		FontKit.setToolTipText(cmdStart, tooltip);

		cmdStart.setIconTextGap(5);
		cmdStart.setBorder(new EmptyBorder(8, 8, 8, 8));
		updateStartFont();
		
		//		cmdStart.setBorder(new EmptyBorder(6, 8, 6, 8));
		//		// 背景透明
		//		cmdStart.setOpaque(false);
	}

	/**
	 * 初始化最小按纽
	 */
	private void initMiniButton() {
		// 最小化按纽
		ImageIcon icon = (ImageIcon) UIManager.getIcon("StatusBar.SwitchButtonIcon");
		icon = ImageUtil.dark(icon, -8); // 这个图标偏向，需要调暗一些，调暗是负数
		String tooltip = UIManager.getString("StatusBar.SwitchButtonTitle");
		cmdMini.setBorderPainted(false);
		cmdMini.setFocusPainted(false);
		cmdMini.setContentAreaFilled(false);
		cmdMini.setIcon(icon, 12);
		FontKit.setToolTipText(cmdMini, tooltip);
		cmdMini.setBorder(new EmptyBorder(6, 8, 6, 8));
	}
	
	class TrayMouseAdapter extends MouseAdapter {

		public void mouseClicked(MouseEvent e) {
			// 单击左键一次，打开托盘窗口
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
				// doTrayWindow();
				switchTrayWindow();
			}
		}
	}
	
	/**
	 * 切换托盘窗口
	 */
	public void switchTrayWindow() {
		addThread(new SwitchTrayWindow());
	}

	/**
	 * 显示或者隐藏托盘
	 * @param e 鼠标事件
	 */
	private void doTrayWindow() {
		// 如果是显示状态，那么隐藏；如果没有显示，那么打开
		if (trayController.isShowing()) {
			trayController.hide();
			boolean hide = trayController.isHided();
			setTrayStatus(hide ? true : false); // 要求显示
		} else if (trayController.isHided()) {
			trayController.show(lblTray);
			boolean show = trayController.isShowing();
			setTrayStatus(show ? false : true); // 要求隐藏
		}
	}
	
	class SwitchTrayWindow extends SwingEvent {
		SwitchTrayWindow() {
			super(true);
		}

		public void process() {
			doTrayWindow();
		}
	}
	
	/**
	 * 关闭托盘窗口
	 */
	public void closeTrayWindow() {
		if (trayController.isShowing()) {
			trayController.hide();
			boolean hide = trayController.isHided();
			setTrayStatus(hide ? true : false); // 要求显示
		} 
	}

	/**
	 * 显示托盘状态
	 * @param show
	 */
	protected void setTrayStatus(boolean show) {
		String key = (show ? "StatusBar.ShowTrayTitle" : "StatusBar.HideTrayTitle");
		String icon = (show ? "StatusBar.ShowTrayIcon" : "StatusBar.HideTrayIcon");
		// 修改图标和工具提示
		lblTray.setIcon(UIManager.getIcon(icon));
		lblTray.setToolTipText(UIManager.getString(key));
	}
	
//	/**
//	 * 显示弹出菜单
//	 * @param event
//	 */
//	private void showTray(MouseEvent event) {
//		trayBar.show(event, this);
//	}
//	/**
//	 * 隐藏托盘窗口
//	 */
//	public void hideTrayWindow() {
//		trayController.hide();
//	}

	
	/**
	 * 初始化图标
	 */
	private void createImages() {
		flashLight = (ImageIcon)UIManager.getIcon("StatusBar.FlashLightIcon");
		flashDrak = (ImageIcon)UIManager.getIcon("StatusBar.FlashDarkIcon");
		connect = (ImageIcon)UIManager.getIcon("StatusBar.ConnectIcon");
		disconnect = (ImageIcon)UIManager.getIcon("StatusBar.DisconnectIcon");
		invokerLight = (ImageIcon)UIManager.getIcon("StatusBar.InvokerLigthIcon");
		invokerLight = ImageUtil.dark(invokerLight, -26); // 调暗一些，原图标太亮了
		invokerDark = (ImageIcon)UIManager.getIcon("StatusBar.InvokerDarkIcon");

		// 登录站点
		//		String tooltip = findCaption("ToolTip/Disconnect/title");
		String tooltip = UIManager.getString("StatusBar.DisconnectTitle");
		lblHub.setIcon(disconnect);
		// lblHub.setToolTipText(tooltip);
		FontKit.setToolTipText(lblHub, tooltip);

		// 级别
		//		dba = findImage("dba.png");
		//		user = findImage("client.png");
		//		unbody = findImage("unclient.png");

		dba = (ImageIcon) UIManager.getIcon("StatusBar.AdministratorIcon");
		user = (ImageIcon) UIManager.getIcon("StatusBar.UserIcon");
		offline = (ImageIcon) UIManager.getIcon("StatusBar.UnbodyIcon");

		//		tooltip = findCaption("ToolTip/Unuser/title");
		tooltip = UIManager.getString("StatusBar.UnuserTitle");
		lblRank.setIcon(offline);
		//		lblRank.setToolTipText(tooltip);
		FontKit.setToolTipText(lblRank, tooltip);

		// 边缘容器监听端口
		//		ImageIcon icon = findImage("tub.png");

		// 托盘图标
		//		ImageIcon icon = (ImageIcon) UIManager.getIcon("StatusBar.TrayIcon");
		//		FontKit.setToolTipText(lblTray, UIManager.getString("StatusBar.TrayTitle"));
		//		lblTray.setIcon(icon);
		
		lblTray.addMouseListener(new TrayMouseAdapter());
		setTrayStatus(true);
		

		// 边缘容器
		ImageIcon icon = (ImageIcon) UIManager.getIcon("StatusBar.TubIcon");
		icon = ImageUtil.brighter(icon, 8); // 调亮一些
		lblTub.setIcon(icon);
		FontKit.setToolTipText(lblTub, UIManager.getString("StatusBar.TubTitle")); // findCaption("ToolTip/Tub/title"));

		// 调用器
		lblInvoker.setIcon(invokerDark);
		//		invokerPrefix = findCaption("ToolTip/Invoker/title");
		invokerPrefix = UIManager.getString("StatusBar.InvokerTitle");

		// 声音
		soundPlay = (ImageIcon)UIManager.getIcon("StatusBar.PlaySoundIcon"); // findImage("sound_play.png");
		//		soundPlay = ImageUtil.dark(soundPlay, -20); // 颜色偏亮，调暗一些
		soundStop = (ImageIcon)UIManager.getIcon("StatusBar.StopSoundIcon"); // findImage("sound_stop.png");

		//		soundStop = ImageUtil.disable(soundPlay, Color.GRAY);

		//		soundStop = ImageUtil.dark(soundStop, -50); // 颜色偏亮，调暗一些
		String tip = UIManager.getString("StatusBar.SoundTitle"); // findCaption("ToolTip/Sound/title");
		FontKit.setToolTipText(lblSound, tip);

		//		StatusBar.SoundTitle 声音
		//		StatusBar.PlaySoundIcon [ICON 16*16] conf/desktop/image/bar/status/sound_play.png
		//		StatusBar.StopSoundIcon [ICON 16*16] conf/desktop/image/bar/status/sound_stop.png

		boolean play = SoundPlayer.getInstance().isPlay();
		lblSound.setIcon(play ? soundPlay : soundStop);

		//		// 时间格式
		//		timeFormatPrefix = UIManager.getString("StatusBar.CalendarTitle"); 

		//		timeFormatPrefix = findCaption("ToolTip/Calendar/title"); 

	}

	//	/**
	//	 * 初始化图标
	//	 */
	//	private void createImages() {
	//		flashLight = findImage("light.png");
	//		flashDrak = findImage("drak.png");
	//		connect = findImage("connect.png");
	//		disconnect = findImage("disconnect.png");
	//		invokerLight = findImage("invoker_on.png");
	//		invokerDark = findImage("invoker_off.png");
	//
	//		// 登录站点
	//		String tooltip = findCaption("ToolTip/Disconnect/title");
	//		lblHub.setIcon(disconnect);
	//		// lblHub.setToolTipText(tooltip);
	//		FontKit.setToolTipText(lblHub, tooltip);
	//
	//		// 级别
	//		dba = findImage("dba.png");
	//		user = findImage("client.png");
	//		unbody = findImage("unclient.png");
	//		tooltip = findCaption("ToolTip/Unuser/title");
	//		lblRank.setIcon(unbody);
	////		lblRank.setToolTipText(tooltip);
	//		FontKit.setToolTipText(lblRank, tooltip);
	//
	//		// 边缘容器监听端口
	//		ImageIcon icon = findImage("tub.png");
	////		lblTub.setToolTipText(findCaption("ToolTip/Tub/title"));
	//		FontKit.setToolTipText(lblTub, findCaption("ToolTip/Tub/title"));
	//		lblTub.setIcon(icon);
	//
	//		// 调用器
	//		lblInvoker.setIcon(invokerDark);
	//		invokerPrefix = findCaption("ToolTip/Invoker/title");
	//
	//		// 声音
	//		soundPlay = (ImageIcon)UIManager.getIcon("StatusBar.PlaySoundIcon"); // findImage("sound_play.png");
	//		soundStop = (ImageIcon)UIManager.getIcon("StatusBar.StopSoundIcon"); // findImage("sound_stop.png");
	//		String tip = UIManager.getString("StatusBar.SoundTitle"); // findCaption("ToolTip/Sound/title");
	//		FontKit.setToolTipText(lblSound, tip);
	//		
	////		StatusBar.SoundTitle 声音
	////		StatusBar.PlaySoundIcon [ICON 16*16] conf/desktop/image/bar/status/sound_play.png
	////		StatusBar.StopSoundIcon [ICON 16*16] conf/desktop/image/bar/status/sound_stop.png
	//		
	//		boolean play = SoundPlayer.getInstance().isPlay();
	//		lblSound.setIcon(play ? soundPlay : soundStop);
	//
	//		// 时间格式
	//		timeFormatPrefix = findCaption("ToolTip/Calendar/title"); //
	//	}

	private JPanel createRightBar() {
		JLabel[] labels = new JLabel[] { lblTray, lblTub, lblInvoker, lblSound, lblRank, lblHub };
		for (int i = 0; i < labels.length; i++) {
			labels[i].setHorizontalAlignment(SwingConstants.CENTER);
			labels[i].setVerticalAlignment(SwingConstants.CENTER);
		}
		lblTime.setHorizontalAlignment(SwingConstants.CENTER);
		lblTime.setVerticalAlignment(SwingConstants.CENTER);
		lblTime.setBorder(new EmptyBorder(0, 8, 0, 8));

		// 右侧
		JPanel left = new JPanel();
		left.setLayout(new GridLayout(1, 4, 8, 0));
		left.setBorder(new EmptyBorder(0, 8, 0, 8));
		left.add(lblTray);
		left.add(lblTub);
		left.add(lblSound);
		left.add(lblRank);

		JPanel leftX = new JPanel();
		leftX.setLayout(new BoxLayout(leftX, BoxLayout.X_AXIS));
		leftX.add(new JSeparator(SwingConstants.VERTICAL));
		leftX.add(left);
		leftX.add(new JSeparator(SwingConstants.VERTICAL));

		// 右侧
		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, 2, 8, 0));
		right.setBorder(new EmptyBorder(0, 8, 0, 10));
		right.add(lblInvoker);
		right.add(lblHub);

		JPanel rightX = new JPanel();
		rightX.setLayout(new BoxLayout(rightX, BoxLayout.X_AXIS));
		rightX.add(new JSeparator(SwingConstants.VERTICAL));
		rightX.add(right);

		JPanel p1 = new JPanel();
		p1.setBorder(new EmptyBorder(0, 0, 0, 0));
		p1.setLayout(new BorderLayout());
		p1.add(leftX, BorderLayout.WEST);
		p1.add(lblTime, BorderLayout.CENTER);
		p1.add(rightX, BorderLayout.EAST);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout(2, 0));
		p2.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);
		p2.add(cmdMini, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(0, 0, 0, 2));
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(p1, BorderLayout.CENTER);
		panel.add(p2, BorderLayout.EAST); // east 东侧

		return panel;
	}

//	private JPanel createLeftBar() {
//		JPanel rightX = new JPanel();
//		rightX.setLayout(new BoxLayout(rightX, BoxLayout.X_AXIS));
//		//		rightX.setBorder(new EmptyBorder(0, 0, 0, 0));
//		rightX.add(new JSeparator(SwingConstants.VERTICAL));
//		rightX.add(cmdMini);
//		rightX.add(new JSeparator(SwingConstants.VERTICAL));
//
//		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout(10, 0));
//		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
//		panel.add(cmdStart, BorderLayout.CENTER);
//		panel.add(rightX, BorderLayout.EAST);
//		return panel;
//	}

	private JPanel createLeftBar() {
		//		JPanel rightX = new JPanel();
		//		rightX.setLayout(new BoxLayout(rightX, BoxLayout.X_AXIS));
		//		rightX.add(new JSeparator(SwingConstants.VERTICAL));
		//
		//		//		rightX.add(cmdMini);
		//		//		rightX.add(new JSeparator(SwingConstants.VERTICAL));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 0));
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel.add(cmdStart, BorderLayout.CENTER);
		panel.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);
		return panel;
	}
	
	/**
	 * 初始化状态栏界面
	 */
	public void init() {

		//		disconnectTip = findCaption("ToolTip/Disconnect/title");
		//		dbaTip = findCaption("ToolTip/DBA/title");
		//		userTip = findCaption("ToolTip/User/title");
		//		unuserTip = findCaption("ToolTip/Unuser/title");

		disconnectTip = UIManager.getString("StatusBar.DisconnectTitle");
		dbaTip = UIManager.getString("StatusBar.DBATitle");
		userTip = UIManager.getString("StatusBar.UserTitle");
		offlineTip = UIManager.getString("StatusBar.UnuserTitle");

		//		lblStatus.setVerticalAlignment(SwingConstants.CENTER);
		//		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);

		// 声音加鼠标点击事件
		lblSound.addMouseListener(new SoundMouseAdapter());
		// 初始化图标
		createImages();

		// 不启用线程更新
		updateTime();

		//		// 右侧
		//		JPanel center = new JPanel();
		//		center.setLayout(new GridLayout(1, 5, 4, 0));
		//		center.add(lblTub);
		//		center.add(lblInvoker);
		//		center.add(lblSound);
		//		center.add(lblRank);
		//		center.add(lblHub);
		//		center.setBorder(new EmptyBorder(0, 0, 0, 0));
		//
		//		JPanel right = new JPanel();
		//		right.setLayout(new BorderLayout(8, 0));
		//		right.add(center, BorderLayout.CENTER);
		//		right.add(lblTime, BorderLayout.EAST);
		//
		//		// 右下角的边框
		//		EtchBorder outside = new EtchBorder(false);
		//		EmptyBorder inside = new EmptyBorder(4, 4, 4, 5);
		//		right.setBorder(new CompoundBorder(outside, inside));

		//		// 中间的面板
		//		JPanel middle = new JPanel();
		//		middle.setLayout(new BorderLayout(5, 0));
		//		middle.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);
		//		middle.add(lblStatus, BorderLayout.CENTER);
		////		middle.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);

		// 初始化启动按纽
		initStartButton();
		// 初始化最小按纽
		initMiniButton();

		// 显示底栏
		setLayout(new BorderLayout(1, 0));
		setBorder(new EmptyBorder(4, 6, 4, 2));
		add(createLeftBar(), BorderLayout.WEST);
		add(frameBar, BorderLayout.CENTER);
		add(createRightBar(), BorderLayout.EAST);

		// 设置句柄
		DesktopTrayManager tm = (DesktopTrayManager) PlatformKit.getTrayManager();
		if (tm != null) {
			tm.setTrayController(trayController);
		}
		
		//		// 使到计算器句柄
		//		Timer timer = DesktopLauncher.getInstance().getTimer();
		//		// 间隔1秒钟触发
		//		timer.schedule(new ThreadTask(), 0, 1000);
	}

	//	/**
	//	 * 初始化状态栏界面
	//	 */
	//	private void init2() {
	////		disconnectTip = findCaption("ToolTip/Disconnect/title");
	////		dbaTip = findCaption("ToolTip/DBA/title");
	////		userTip = findCaption("ToolTip/User/title");
	////		unuserTip = findCaption("ToolTip/Unuser/title");
	//		
	//		disconnectTip = UIManager.getString("StatusBar.DisconnectTitle");
	//		dbaTip = UIManager.getString("StatusBar.DBATitle");
	//		userTip = UIManager.getString("StatusBar.UserTitle");
	//		unuserTip = UIManager.getString("StatusBar.UnuserTitle");
	//
	////		lblStatus.setVerticalAlignment(SwingConstants.CENTER);
	////		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
	//
	//		// 声音加鼠标点击事件
	//		lblSound.addMouseListener(new SoundMouseAdapter());
	//		// 初始化图标
	//		createImages();
	//
	//		// 不启用线程更新
	//		updateTime();
	//
	//		// 右侧
	//		JPanel center = new JPanel();
	//		center.setLayout(new GridLayout(1, 5, 4, 0));
	//		center.add(lblTub);
	//		center.add(lblInvoker);
	//		center.add(lblSound);
	//		center.add(lblRank);
	//		center.add(lblHub);
	//		center.setBorder(new EmptyBorder(0, 0, 0, 0));
	//
	//		JPanel right = new JPanel();
	//		right.setLayout(new BorderLayout(8, 0));
	//		right.add(center, BorderLayout.CENTER);
	//		right.add(lblTime, BorderLayout.EAST);
	//
	//		// 右下角的边框
	//		EtchBorder outside = new EtchBorder(false);
	//		EmptyBorder inside = new EmptyBorder(4, 4, 4, 5);
	//		right.setBorder(new CompoundBorder(outside, inside));
	//
	//		// 中间的面板
	//		JPanel middle = new JPanel();
	//		middle.setLayout(new BorderLayout(5, 0));
	//		middle.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);
	//		middle.add(lblStatus, BorderLayout.CENTER);
	//		middle.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);
	//
	//		// 初始化启动按纽
	//		initStartButton();
	//
	//		// 显示底栏
	//		setLayout(new BorderLayout(10, 0));
	//		setBorder(new EmptyBorder(4, 8, 4, 4));
	//		add(cmdStart, BorderLayout.WEST);
	//		add(middle, BorderLayout.CENTER);
	//		add(right, BorderLayout.EAST);
	//
	////		// 使到计算器句柄
	////		Timer timer = DesktopLauncher.getInstance().getTimer();
	////		// 间隔1秒钟触发
	////		timer.schedule(new ThreadTask(), 0, 1000);
	//	}

	/**
	 * 设置声音图标
	 * @param play 播放状态
	 */
	public void setSoundIcon(boolean play) {
		monitorThread.addEvent(new PlaySoundThread(play));
	}

	class UpdateTimeThread extends SwingEvent {
		UpdateTimeThread() {
			super(true);
		}

		public void process() {
			updateTime();
		}
	}

	class UpdateTubThread extends SwingEvent {
		UpdateTubThread() {
			super(true);
		}

		public void process() {
			updateTub();
		}
	}

	class InvokerThread extends SwingEvent {

		public InvokerThread() {
			super(true);
		}

		public void process() {
			updateInvokerToolTipText();
		}
	}

	/**
	 * 监视器线程
	 *
	 * @author scott.liang
	 * @version 1.0 8/9/2021
	 * @since laxcus 1.0
	 */
	class MonitorThread extends MutexThread {

		long scaleTime;

		long scaleUpdateTub;

		long scaleInvoker;

		ArrayList<SwingEvent> threads = new ArrayList<SwingEvent>();

		public MonitorThread() {
			super();
			reset();
		}

		/**
		 * 重置时间
		 */
		private void reset() {
			scaleTime = scaleUpdateTub = scaleInvoker = System.currentTimeMillis();
		}

		/**
		 * 增加一个事件线程
		 * @param event
		 */
		void addEvent(SwingEvent event) {
			super.lockSingle();
			try {
				threads.add(event);
			} catch (Throwable e) {

			} finally {
				super.unlockSingle();
			}
			// 在运行中，唤醒
			if (isRunning()) {
				wakeup();
			}
		}

		/* (non-Javadoc)
		 * @see com.laxcus.thread.VirtualThread#init()
		 */
		@Override
		public boolean init() {
			return true;
		}

		private void done() {
			// 没有显示，忽略它
			if (!isShowing() || !isVisible()) {
				return;
			}

			// 更新时间
			long time = System.currentTimeMillis();
			if (time >= scaleTime) {
				addThread(new UpdateTimeThread());

				//				SimpleDateFormat style = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
				//				System.out.printf("更新了时间: %s\n", style.format(new Date(time)));
			}

			// 更新容器
			if (time - scaleUpdateTub >= 6000) {
				addThread(new UpdateTubThread());
				scaleUpdateTub = time;
			}
			// 更新调用器
			if (time - scaleInvoker >= 6000) {
				addThread(new InvokerThread());
				scaleInvoker = time;
			}

			// 锁定取出参数
			super.lockSingle();
			try {
				for (SwingEvent thread : threads) {
					addThread(thread);
				}
				threads.clear();
			} catch (Throwable e) {

			} finally {
				super.unlockSingle();
			}
		}

		//		/* (non-Javadoc)
		//		 * @see com.laxcus.thread.VirtualThread#process()
		//		 */
		//		@Override
		//		public void process() {
		//			long interval = 2000;
		//			// 间隔
		//			while (!isInterrupted()) {
		//				done();
		//				
		//				// 延时...
		//				long now = System.currentTimeMillis();
		//				// 小于当前时间，或者超过1分钟时
		//				if (now < scaleTime || scaleTime + 60000 < now) {
		//					helo();
		//					done();
		//					scaleTime = getNextTouchTime(scaleTime);
		//				}
		//				// 正常延时
		//				else {
		//					long ds = (scaleTime - now >= interval ? interval : scaleTime - now);
		//					delay(ds);
		//					// 当前时间达到最后时间时
		//					if (now >= scaleTime) {
		//						scaleTime = getNextTouchTime(scaleTime);
		//					}
		//				}
		//			}
		//		}

		/* (non-Javadoc)
		 * @see com.laxcus.thread.VirtualThread#process()
		 */
		@Override
		public void process() {
			long interval = 2000;
			int minute = 60000;
			// 间隔
			while (!isInterrupted()) {
				done();

				// 延时...
				long now = System.currentTimeMillis();
				if (now + minute < scaleTime || scaleTime + minute < now) {
					reset();
					continue;
				}

				// 更新时间
				long ds = (scaleTime - now >= interval ? interval : scaleTime - now);
				delay(ds);
				if (now >= scaleTime) {
					scaleTime = getNextTouchTime(scaleTime);
				}
			}
		}

		/* (non-Javadoc)
		 * @see com.laxcus.thread.VirtualThread#finish()
		 */
		@Override
		public void finish() {

		}

		/**
		 * 返回下个阶段的时间
		 * @return
		 */
		private long getNextTouchTime(long now) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(now));
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.add(Calendar.MINUTE, 1);
			return calendar.getTime().getTime();
		}
	}


	//	/**
	//	 * 格式化时间
	//	 * @param hm 小时/分钟
	//	 * @return 返回格式化字符串
	//	 */
	//	private final String formatHour(int hm) {
	//		if (hm < 10) {
	//			return String.format(" %d", hm);
	//		} else {
	//			return String.format("%d", hm);
	//		}
	//	}
	//
	//	/**
	//	 * 格式化
	//	 * @param minute
	//	 * @return
	//	 */
	//	private final String formatMinute(int minute) {
	//		if (minute < 10) {
	//			return "0" + String.format("%d", minute);
	//		} else {
	//			return String.format("%d", minute);
	//		}
	//	}

	private String getDayOfWeek(Calendar calendar) {
		int value = calendar.get(Calendar.DAY_OF_WEEK); // 周几..
		switch (value) {
		case Calendar.SUNDAY:
			return UIManager.getString("StatusBar.Calendar.SUNDAY");
		case Calendar.MONDAY:
			return UIManager.getString("StatusBar.Calendar.MONDAY");
		case Calendar.TUESDAY:
			return UIManager.getString("StatusBar.Calendar.TUESDAY");
		case Calendar.WEDNESDAY:
			return UIManager.getString("StatusBar.Calendar.WEDNESDAY");
		case Calendar.THURSDAY:
			return UIManager.getString("StatusBar.Calendar.THURSDAY");
		case Calendar.FRIDAY:
			return UIManager.getString("StatusBar.Calendar.FRIDAY");
		case Calendar.SATURDAY:
			return UIManager.getString("StatusBar.Calendar.SATURDAY");
		}
		return null;
	}

	/**
	 * 启用线程更新
	 * @param thread
	 */
	private void updateTime() {
		Date date = new Date(System.currentTimeMillis());
		// 当前时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		//		// 取出分钟，不一致就更新
		//		int num = calendar.get(Calendar.MINUTE);
		//		if (num != minute) {
		//			minute = num;

		String value = UIManager.getString("StatusBar.DateStyleText");
		SimpleDateFormat style = new SimpleDateFormat(value, Locale.ENGLISH);
		String d = style.format(date);
		// 周几
		String dayOfWeek = getDayOfWeek(calendar);
		if (dayOfWeek != null) {
			d = String.format("%s %s", d, dayOfWeek);
		}

		value = UIManager.getString("StatusBar.TimeStyleText");
		style = new SimpleDateFormat(value, Locale.ENGLISH);
		String t = style.format(date);

		String text = String.format("<html><body><center><b>%s</b><br>%s</center></body></html>", t, d);
		String tooltip = String.format("%s %s", d, t);
		FontKit.setLabelText(lblTime, text);
		FontKit.setToolTipText(lblTime, tooltip);
		//		}
	}

	//	/**
	//	 * 启用线程更新
	//	 * @param thread
	//	 */
	//	private void updateTime() {
	//		Date date = new Date(System.currentTimeMillis());
	//		// 当前时间
	//		Calendar calendar = Calendar.getInstance();
	//		calendar.setTime(date);
	//		// 取出分钟，不一致就更新
	//		int num = calendar.get(Calendar.MINUTE);
	//		if (num != minute) {
	//			minute = num;
	//			int hour = calendar.get(Calendar.HOUR_OF_DAY);
	//			String text = formatHour(hour) + ":" + formatMinute(minute);
	//
	//			//	SimpleDateFormat timeStyle = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
	//
	//			// 生成格式化的时间
	//			SimpleDateFormat style = new SimpleDateFormat(timeFormatPrefix, Locale.ENGLISH);
	//			String tooltip = style.format(date);
	//			
	//			// 小时:分钟
	//			FontKit.setLabelText(lblTime, text);
	//			// 日期
	//			FontKit.setToolTipText(lblTime, tooltip);
	//		}
	//	}

	/**
	 * 更新容器
	 */
	private void updateTub() {
		// 边缘容器
		SocketHost host = DesktopLauncher.getInstance().getTubHost();
		int members = TubPool.getInstance().getRunTubs();
		String text = String.format("%s - %s", (host == null ? "" : host.toString()), members);
		FontKit.setToolTipText(lblTub, text);
	}

	//	/**
	//	 * 显示数目
	//	 * @author scott.liang
	//	 * @version 1.0 8/25/2012
	//	 * @since laxcus 1.0
	//	 */
	//	class ShowStatusText extends SwingEvent {
	//		String text;
	//
	//		ShowStatusText(String s) {
	//			super(true); // 同步处理
	//			text = s;
	//		}
	//
	//		public void process() {
	//			FontKit.setLabelText(lblStatus, text);
	//			FontKit.setToolTipText(lblStatus, text);
	//		}
	//	}

	/**
	 * 线程中显示图标
	 * @author scott.liang
	 * @version 1.0 8/25/2012
	 * @since laxcus 1.0
	 */
	class ShowHubThread extends SwingEvent {
		Icon icon;
		String tooltip;

		ShowHubThread(Icon e, String tip) {
			super(true);
			icon = e;
			tooltip = tip;
		}

		public void process() {
			if (icon != null) {
				lblHub.setIcon(icon);
			}
			if (tooltip != null) {
				FontKit.setToolTipText(lblHub, tooltip);
			}
		}
	}

	/**
	 * 显示级别
	 * @author scott.liang
	 * @version 1.0 8/25/2012
	 * @since laxcus 1.0
	 */
	class ShowRankThread extends SwingEvent {
		Icon icon;
		String tooltip;

		ShowRankThread(Icon e, String tip) {
			super(true);
			icon = e;
			tooltip = tip;
		}

		public void process() {
			if (icon != null) {
				lblRank.setIcon(icon);
			}
			if (tooltip != null) {
				FontKit.setToolTipText(lblRank, tooltip);
			}
		}
	}

	//	/**
	//	 * 显示时间
	//	 *
	//	 * @author scott.liang
	//	 * @version 1.0 6/2/2021
	//	 * @since laxcus 1.0
	//	 */
	//	class ShowTimeThread extends SwingEvent {
	//		String text;
	//
	//		String tooltip;
	//
	//		ShowTimeThread(String s1, String s2) {
	//			super();
	//			text = s1;
	//			tooltip = s2;
	//		}
	//
	//		public void process() {
	//			FontKit.setLabelText(lblTime, text);
	//			FontKit.setToolTipText(lblTime, tooltip);
	//		}
	//	}

	/**
	 * 调整声音图标线程
	 * 
	 * @author scott.liang
	 * @version 1.0 9/7/2019
	 * @since laxcus 1.0
	 */
	class SoundExchangeThread extends SwingEvent {

		SoundExchangeThread() {
			super(true);
		}

		public void process() {
			boolean play = SoundPlayer.getInstance().isPlay();
			// 反向图标
			lblSound.setIcon(play ? soundStop : soundPlay);
			// 修改状态
			SoundPlayer.getInstance().setPlay(!play);
		}
	}

	/**
	 * 设置正确的声音图标
	 *
	 * @author scott.liang
	 * @version 1.0 9/8/2019
	 * @since laxcus 1.0
	 */
	class PlaySoundThread extends SwingEvent {

		/** 播放声音 **/
		boolean play;

		PlaySoundThread(boolean b) {
			super(true);
			play = b;
		}

		public void process() {
			lblSound.setIcon(play ? soundPlay : soundStop);
			SoundPlayer.getInstance().setPlay(play);
		}
	}

	private void updateStartFont() {
		Font font = UIManager.getFont("Label.font");
		if (font != null && cmdStart != null) {
			font = new Font(font.getName(), Font.BOLD, font.getSize());
			cmdStart.setFont(font);
			FontKit.updateToolTipText(cmdStart);
		}
	}
	
	class UpdateFonts extends SwingEvent {

		UpdateFonts() {
			super(true);
		}

//		public void process() {
//			Font font = UIManager.getFont("Label.font");
//			if (font == null) {
//				return;
//			}
//			// 更新标签字体
//			Font rs = new Font(font.getName(), font.getStyle(), 12);
//
//			//			// 设置新的字体
//			//			JComponent[] labels = new JComponent[] { cmdStart, lblStatus,
//			//					lblInvoker, lblSound, lblHub, lblRank, lblTub, lblTime };
//
//			// 设置新的字体
//			JComponent[] labels = new JComponent[] { cmdStart, lblInvoker,
//					lblSound, lblHub, lblRank, lblTub, lblTime };
//
//			// 设置字体和工具提示
//			for (int i = 0; i < labels.length; i++) {
//				labels[i].setFont(rs);
//				// 更新提示文本的字体
//				FontKit.updateToolTipText(labels[i]);
//
//				//				// 从可访问描述中取出提示
//				//				if (labels[i].getAccessibleContext() != null) {
//				//					String tooltip = labels[i].getAccessibleContext().getAccessibleDescription();
//				//					FontKit.setToolTipText(labels[i], tooltip);
//				//				}
//			}
//		}
		
		public void process() {
			updateStartFont();
			
			// 设置新的字体
			JComponent[] members = new JComponent[] { lblInvoker,
					lblSound, lblHub, lblRank, lblTray, lblTub, lblTime };
			// 设置字体和工具提示
			for (int i = 0; i < members.length; i++) {
				FontKit.updateDefaultFonts(members[i]);
				// 更新提示文本的字体
				FontKit.updateToolTipText(members[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();

		//		System.out.println("更新DesktopStatusBar UI!");

		// if (cmdMini != null) {
		// cmdMini.updateUI();
		// }
		// if (frameBar != null) {
		// frameBar.updateUI();
		// }

		if (monitorThread != null) {
			monitorThread.addEvent(new UpdateFonts());
		}

		//		if (frameBar != null) {
		//			frameBar.updateUI();
		//		}
	}

//	/**
//	 * 返回桌面窗口条
//	 * @return
//	 */
//	public DesktopFrameBar getFrameBar() {
//		return frameBar;
//	}
	
	/**
	 * 设置选择窗口监听器
	 * @param l
	 */
	public void setSelectFrameListener(DesktopSelectFrameListener l) {
		frameBar.setSelectFrameListener(l);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.platform.FrameBar#activate(com.laxcus.ui.frame.LightFrame)
	 */
	@Override
	public void activate(LightFrame frame) {
		frameBar.activate(frame);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.platform.FrameBar#deactivate(com.laxcus.ui.frame.LightFrame)
	 */
	@Override
	public void deactivate(LightFrame frame) {
		frameBar.deactivate(frame);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.platform.PlatformPane#register(com.laxcus.ui.frame.LightFrame)
	 */
	@Override
	public void register(LightFrame frame) {
		frameBar.register(frame);
		
//		// 注册到托盘
//		if (frame.isShowTray()) {
//			trayController.addFrame(frame);
//		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.platform.PlatformPane#unregister(com.laxcus.ui.frame.LightFrame)
	 */
	@Override
	public void unregister(LightFrame frame) {
		// monitorThread.isInterrupted();

		// 中断
		boolean immediately = SwingDispatcher.getInstance().isRefuseEvent();
		// 注销，立即或者否
		frameBar.unregister(frame, immediately);
		
//		// 从托盘中注销
//		trayController.removeFrame(frame);
	}

	//	private void paintNimbusComponent(Graphics g) {
	//		// 宽度和高度
	//		int width = getWidth();
	//		int height = getHeight();
	//
	//		ImageIcon icon = (ImageIcon) UIManager.getIcon("DockBar.WallIcon");
	//		if (icon == null) {
	//			return;
	//		}
	//
	//		Image image = icon.getImage();
	//		int imgWidth = image.getWidth(null);
	//		int imgHeight = image.getHeight(null);
	//		
	//		for (int y = 0; y < height; y += imgHeight) {
	//			for (int x = 0; x < width; x += imgWidth) {
	//				g.drawImage(image, x, y, null); // 从指定坐标绘制，不拉伸！
	//			}
	//		}
	//	}
	//	
	//	protected void paintComponent(Graphics g) {
	//		if (isNimbusUI()) {
	//			paintNimbusComponent(g);
	//		} else {
	//			super.paintComponent(g);
	//		}
	//	}

}