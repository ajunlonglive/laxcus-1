/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.front.terminal.*;
import com.laxcus.site.*;
import com.laxcus.thread.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.net.*;
import com.laxcus.util.sound.*;

/**
 * 状态栏面板
 * 
 * @author scott.liang
 * @version 1.1 8/25/2012
 * @since laxcus 1.0
 */
public class TerminalStatusPanel extends JPanel {

	private static final long serialVersionUID = 9218985720382755478L;

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
	private ImageIcon dba, user, unbody;

	/** 提示 **/
	private JLabel lblStatus = new JLabel();

	/** 线程数目 **/
	private JLabel lblInvoker = new JLabel();
	
	/** 边缘容器地址 **/
	private JLabel lblTub = new JLabel();

	/** 图标 **/
	private ImageIcon invokerLight, invokerDark;
	
//	/** 当前线程数目 **/
//	private int currentThreads;

	/** 线程前缀字符 **/
	private String invokerPrefix;

	/** 数据库管理/用户/没有账号 **/
	private String dbaTip, userTip, unuserTip;

//	/** 定义刷新线程 **/
//	private ThreadTask threadTask = new ThreadTask();

	/** 守护线程，处理定时显示的参数 **/
	private MonitorThread monitor = new MonitorThread();
	
	/**
	 * 构造标准的状态栏面板
	 */
	public TerminalStatusPanel() {
		super();
//		currentThreads = 0;
	}

	/**
	 * 启动任务线程
	 */
	public void startDaemonThread() {
		monitor.start();
	}

	/**
	 * 停止任务线程
	 */
	public void stopDaemonThread() {
		monitor.stop();
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}

	/**
	 * 状态面板鼠标适配器
	 *
	 * @author scott.liang
	 * @version 1.0 9/8/2019
	 * @since laxcus 1.0
	 */
	public class StatusMouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			// 判断是声音图标
			if (e.getSource() == lblSound) {
				monitor.addEvent(new SoundExchangeThread());
			}
		}
	}

	/**
	 * 取出图标
	 * @param name 图标名
	 * @return 返回图标实例
	 */
	private ImageIcon findImage(String name) {
		// 图标资源，在JAR文档中
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/window/status/");
		return loader.findImage(name, 16, 16);
	}

	/**
	 * 标题内容
	 * @param name 名称
	 * @return 返回文本描述
	 */
	private String findCaption(String name) {
		return TerminalLauncher.getInstance().findCaption(name); // "ToolTip/DBA/title");
	}

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
		rollingCount = rollingCount + 1;
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

		boolean login = TerminalLauncher.getInstance().isLogined();
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
		
		// 修改图标
		if (icon != null) {
			lblInvoker.setIcon(icon);
		}
		// 只在不闪烁时更新
		if (!rolling) {
			updateInvokerToolTipText();
		}
	}
	
	class RollingThread extends SwingEvent {
		boolean rolling;

		RollingThread(boolean b) {
			super(true);
			rolling = b;
		}
		public void process() {
			doRolling(rolling);
		}
	}
	
	/**
	 * 收到信息后，电灯图标闪烁
	 */
	public void rolling() {
		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();

		// 线程之间的间隔时间是800毫秒
		final int interval = 800;
		long touchTime = System.currentTimeMillis();

		// 播放动画效果
		RollingThread light = new RollingThread(true);
//		light.setIntervalTime(interval);
		light.setTouchTime(touchTime);
		array.add(light);

		// 下一次触发时间
		touchTime += interval;

		// 多延时200毫秒，效果更好！
		RollingThread dark = new RollingThread(false);
//		dark.setIntervalTime(interval + 200);
		dark.setTouchTime(touchTime + 200);
		array.add(dark);

		// 一批线程放入队列
		SwingDispatcher.invokeThreads(array);
	}
	
//	class InvokerThread extends SwingEvent {
//		ImageIcon icon;
//		boolean rolling;
//		boolean update;
//
//		public InvokerThread(ImageIcon e, boolean roll) {
//			super();
//			icon = e;
//			rolling = roll;
//			update = false;
//		}
//		
//		/**
//		 * 默认更新
//		 */
//		public InvokerThread() {
//			super();
//			update = true;
//		}
//
//		public void process() {
//			if (update) {
////				int threads = Thread.activeCount();
////				int runTasks = InvokerTrustor.getThreadCount();
////				String text = String.format(invokerPrefix, threads, runTasks, rollingCount);
////				FontKit.setToolTipText(lblInvoker, text);
//				
//				updateInvokerToolTipText();
//			} else {
//				lblInvoker.setIcon(icon);
//				// 只在闪烁时更新
//				if (rolling) {
////					int threads = Thread.activeCount();
////					int runTasks = InvokerTrustor.getThreadCount();
////					String text = String.format(invokerPrefix, threads, runTasks, rollingCount);
////					FontKit.setToolTipText(lblInvoker, text);
//					
//					updateInvokerToolTipText();
//				}
//			}
//		}
//	}
	
	class InvokerThread extends SwingEvent {
		
		public InvokerThread() {
			super(true);
		}

		public void process() {
			updateInvokerToolTipText();
		}
	}
	
	private void updateInvokerToolTipText() {
		String rf = ConfigParser.splitCapacity(receiveFlows);
		String sf = ConfigParser.splitCapacity(sendFlows);
		int threads = Thread.activeCount();
		int runTasks = InvokerTrustor.getThreadCount();
		String tooltip = String.format(invokerPrefix, threads, runTasks, rollingCount, rf, sf);
		lblInvoker.setToolTipText(tooltip);
	}

	/**
	 * 设置中断图标
	 */
	public void setDisconnectIcon() {
		// 设置无效图标
		ShowHubThread thread = new ShowHubThread(disconnect, disconnectTip);
		monitor.addEvent(thread);
	}

	/**
	 * 设置已经连接图标
	 */
	public void setConnectIcon() {
		Node hub = TerminalLauncher.getInstance().getHub();
		ShowHubThread thread = new ShowHubThread(connect, hub.toString());
		monitor.addEvent(thread);
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
		monitor.addEvent(thread);
	}

	/**
	 * 设置文本提示
	 * @param tipText
	 */
	public void setTip(String tipText) {
		ShowStatusStatus thread = new ShowStatusStatus(tipText == null ? "" : tipText);
		monitor.addEvent(thread);
	}
	
	/**
	 * 设置管理员图标
	 */
	public void setAdministratorIcon() {
		ShowRankThread thread = new ShowRankThread(dba, dbaTip);
		monitor.addEvent(thread);
	}

	/**
	 * 设置普通注册用户图标
	 */
	public void setUserIcon() {
		ShowRankThread thread = new ShowRankThread(user, userTip);
		monitor.addEvent(thread);
	}

	/**
	 * 无定义的身份图标
	 */
	public void setNobodyIcon() {
		ShowRankThread thread = new ShowRankThread(unbody, unuserTip);
		monitor.addEvent(thread);
	}

//	/**
//	 * 闪动图标
//	 * @param who 图标状态编号
//	 * @param login 节点登录或者否
//	 */
//	public void flash(int who, boolean login) {
//		ImageIcon icon = null;
//		
//		// 选择图标
//		if (who == TerminalFlashTag.FLASH_START) {
//			icon = flashLight;
//		} else if (who == TerminalFlashTag.FLASH_NEXT) {
//			icon = flashDrak;
//		} else if (who == TerminalFlashTag.FLASH_STOP) {
//			if (login) {
//				setConnectIcon(); // 还原为连接图标
//			} else {
//				setDisconnectIcon();
//			}
//		}
//
//		if (icon != null) {
//			ShowHubThread thread = new ShowHubThread(icon, null);
//			addThread(thread);
//		}
//	}
	
	/**
	 * 这个方法直接被TerminalWindow的事件线程调用
	 * @param who 图标编号
	 */
	private void doThreadFlash(int who) {
		ImageIcon icon = null;
		String tooltip = null;

		// 判断处于连网状态
		boolean login = TerminalLauncher.getInstance().isLogined();
		// 选择图标
		if (login) {
			if (who == TerminalFlashTag.FLASH_START) {
				icon = flashLight;
			} else if (who == TerminalFlashTag.FLASH_NEXT) {
				icon = flashDrak;
			} else if (who == TerminalFlashTag.FLASH_STOP) {
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
			lblHub.setToolTipText(tooltip);
		}
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
			super(true);
			id = who;
		}

		/* (non-Javadoc)
		 * @see com.laxcus.util.display.SwingEvent#process()
		 */
		@Override
		public void process() {
			doThreadFlash(id);
		}
	}
	
	/**
	 * 被TerminalLauncher调用，显示动态图标！
	 */
	public void flash() {
		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();

		// 图标编号
		int[] icons = new int[] { TerminalFlashTag.FLASH_START, TerminalFlashTag.FLASH_NEXT };

		// 线程之间的间隔时间是800毫秒
		final int interval = 800;
		long touchTime = System.currentTimeMillis();

		// 播放动画效果
		for (int iterate = 0; iterate < 1; iterate++) {
			for (int i = 0; i < icons.length; i++) {
				FlashThread event = new FlashThread(icons[i]);
//				event.setIntervalTime(interval);
				event.setTouchTime(touchTime);
				array.add(event);
				// 下一次触发时间
				touchTime += interval;
			}
		}

		// 多延时200毫秒，效果更好！
		FlashThread last = new FlashThread(TerminalFlashTag.FLASH_STOP);
//		last.setIntervalTime(interval + 200);
		last.setTouchTime(touchTime + 200);
		array.add(last);

		// 一批线程放入队列
		SwingDispatcher.invokeThreads(array);
	}
	
//	class ShowHubThread extends SwingEvent {
//		private Icon icon;
//		private String tooltip;
//		
//		public ShowHubThread(Icon e, String s) {
//			super();
//			icon = e;
//			tooltip = s;
//		}
//		
//		public void process() {
//			if (icon != null) {
//				lblHub.setIcon(icon);
//			}
//			if (tooltip != null) {
//				lblHub.setToolTipText(tooltip);
//			}
//		}
//	}

	/**
	 * 初始化图标
	 */
	private void createImages() {
		// 用户图标
		dba = findImage("dba.png");
		user = findImage("client.png");
		unbody = findImage("unclient.png");
		// HUB闪烁
		flashLight = findImage("light.png");
		flashDrak = findImage("drak.png");
		// 网络连接状态
		connect = findImage("connect.png");
		disconnect = findImage("disconnect.png");
		// 声音
		soundPlay = findImage("sound_play.png");
		soundStop = findImage("sound_stop.png");
		// 调用器
		invokerLight = findImage("invoker_on.png");
		invokerDark = findImage("invoker_off.png");
		
		// 登录站点
		String tooltip = findCaption("ToolTip/Disconnect/title");
		lblHub.setIcon(disconnect);
		lblHub.setToolTipText(tooltip);

		// 级别
		tooltip = findCaption("ToolTip/Unuser/title");
		lblRank.setIcon(unbody);
		lblRank.setToolTipText(tooltip);

//		// 线程
//		ImageIcon icon = findImage("threads.png");
//		invokerPrefix = findCaption("ToolTip/Threads/title");
//		tooltip = String.format(invokerPrefix, 0);
//		lblInvoker.setToolTipText(tooltip);
//		lblInvoker.setIcon(icon);

		// 线程
//		ImageIcon icon = findImage("threads.png");
		invokerPrefix = findCaption("ToolTip/Invoker/title");
//		tooltip = String.format(invokerPrefix, 0);
//		lblInvoker.setToolTipText(tooltip);
		lblInvoker.setIcon(invokerDark);
		
		// 边缘容器监听端口
		ImageIcon icon = findImage("tub.png");
		lblTub.setToolTipText(findCaption("ToolTip/Tub/title"));
		lblTub.setIcon(icon);

		// 声音
		String tip = findCaption("ToolTip/Sound/title");
		FontKit.setToolTipText(lblSound, tip);
		boolean play = SoundPlayer.getInstance().isPlay();
		lblSound.setIcon(play ? soundPlay : soundStop);
	}

	/**
	 * 初始化状态栏界面
	 */
	public void init() {
		disconnectTip = findCaption("ToolTip/Disconnect/title");
		dbaTip = findCaption("ToolTip/DBA/title");
		userTip = findCaption("ToolTip/User/title");
		unuserTip = findCaption("ToolTip/Unuser/title");
		
		lblStatus.setVerticalAlignment(SwingConstants.CENTER);
		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);

		// 鼠标点击事件
		lblSound.addMouseListener(new StatusMouseAdapter());
		// 初始化图标
		createImages();

		// 右侧
		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, 5, 4, 0));
		right.add(lblTub);
		right.add(lblInvoker);
		right.add(lblSound);
		right.add(lblRank);
		right.add(lblHub);
		right.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		
//		EtchBorder outside = new EtchBorder(Color.GRAY, Color.WHITE);
//		EmptyBorder inside = new EmptyBorder(2, 4, 2, 4);
//		right.setBorder(new CompoundBorder(outside, inside));
		
		// 左侧
		JPanel left = new JPanel();
		left.setLayout(new BorderLayout(5, 0));
		left.add(lblStatus, BorderLayout.CENTER);
		left.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);

		// 显示底栏
		setLayout(new BorderLayout(5, 0));
		setBorder(new EmptyBorder(4, 6, 0, 2));
		add(left, BorderLayout.CENTER);
		add(right, BorderLayout.EAST);

//		// 使到计算器句柄
//		Timer timer = TerminalLauncher.getInstance().getTimer();
//		// 当上个任务完成后，间隔5秒触发
//		timer.schedule(new ThreadTask(), 0, 2000);
	}

	/**
	 * 设置声音图标
	 * @param play 播放状态
	 */
	public void setSoundIcon(boolean play) {
		monitor.addEvent(new SetSoundIconThread(play));
	}
	
	class UpdateTubThread extends SwingEvent {
		UpdateTubThread(){
			super(true);
		}
		public void process() {
			updateTub();
		}
	}

//	/**
//	 * 显示线程
//	 *
//	 * @author scott.liang
//	 * @version 1.0 8/25/2012
//	 * @since laxcus 1.0
//	 */
//	class ThreadTask extends TimerTask {
//		long scaleUpdateTub;
//
//		long scaleInvoker;
//
//		public ThreadTask() {
//			super();
//			scaleUpdateTub = scaleInvoker = System.currentTimeMillis();
//		}
//
//		public void run() {
//			// 边缘容器
//			long time = System.currentTimeMillis();
//			if (time - scaleUpdateTub >= 6000) {
//				addThread(new UpdateTubThread());
//				scaleUpdateTub = time;
//			}
//			// 更新调整器
//			if (time - scaleInvoker >= 6000) {
//				addThread(new InvokerThread());
//				scaleInvoker = time;
//			}
//		}
//	}
	
	private void updateTub() {
		// 边缘容器
		SocketHost host = TerminalLauncher.getInstance().getTubHost();
		int members = TubPool.getInstance().getRunTubs();
		String text = String.format("%s - %s", (host == null ? "" : host.toString()), members);
		FontKit.setToolTipText(lblTub, text);
		
//		addThread(new ShowTubTipTub(text));
	}

	/**
	 * 显示数目
	 * @author scott.liang
	 * @version 1.0 8/25/2012
	 * @since laxcus 1.0
	 */
	class ShowStatusStatus extends SwingEvent {
		String text;

		ShowStatusStatus(String s) {
			super(true);
			text = s;
		}

		public void process() {
			FontKit.setLabelText(lblStatus, text);
			FontKit.setToolTipText(lblStatus, text);
		}
	}

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
				lblHub.setToolTipText(tooltip);
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
				lblRank.setToolTipText(tooltip);
			}
		}
	}

	/**
	 * 显示线程数目
	 * @author scott.liang
	 * @version 1.0 8/25/2012
	 * @since laxcus 1.0
	 */
	class ShowThreadTipThread extends SwingEvent {
		String text;

		ShowThreadTipThread(String s) {
			super(true);
			text = s;
		}

		public void process() {
			FontKit.setToolTipText(lblInvoker, text);
		}
	}

//	/**
//	 * 显示边缘容器的连接
//	 * @author scott.liang
//	 * @version 1.0 8/25/2012
//	 * @since laxcus 1.0
//	 */
//	class ShowTubTipTub extends SwingEvent {
//		String text;
//
//		ShowTubTipTub(String s) {
//			super();
//			text = s;
//		}
//
//		public void process() {
//			FontKit.setToolTipText(lblTub, text);
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

		public SoundExchangeThread() {
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
	class SetSoundIconThread extends SwingEvent {

		/** 播放声音 **/
		boolean play;

		public SetSoundIconThread(boolean b) {
			super(true);
			play = b;
		}

		public void process() {
			lblSound.setIcon(play ? soundPlay : soundStop);
			SoundPlayer.getInstance().setPlay(play);
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
		
		long scaleInvoker;
		long scaleUpdateTub;
		
		ArrayList<SwingEvent> threads = new ArrayList<SwingEvent>();

		public MonitorThread() {
			super();
			scaleInvoker = scaleUpdateTub = System.currentTimeMillis();
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
//			// 更新时间
//			long time = System.currentTimeMillis();
//			// 更新调用器
//			if (time - scaleInvoker >= 10000) {
//				addThread(new InvokerThread());
//				scaleInvoker = time;
//			}
			
			// 边缘容器
			long time = System.currentTimeMillis();
			if (time - scaleUpdateTub >= 10000) {
				addThread(new UpdateTubThread());
				scaleUpdateTub = time;
			}
			// 更新调整器
			if (time - scaleInvoker >= 10000) {
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

		/* (non-Javadoc)
		 * @see com.laxcus.thread.VirtualThread#process()
		 */
		@Override
		public void process() {
			while (!isInterrupted()) {
				done();
				delay(5000L);
			}
		}

		/* (non-Javadoc)
		 * @see com.laxcus.thread.VirtualThread#finish()
		 */
		@Override
		public void finish() { }
	}
}