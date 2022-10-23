/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.event.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.sound.*;
import com.laxcus.watch.*;
import com.laxcus.watch.window.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.Node;

/**
 * 状态栏面板
 * 
 * @author scott.liang
 * @version 1.2 9/8/2019
 * @since laxcus 1.0
 * 
 * 1.2 增加声音显示
 */
public class WatchStatusPanel extends JPanel {

	private static final long serialVersionUID = -2238761282169985797L;

	/** 播放声音 **/
	private JLabel lblSound = new JLabel();

	/** 声音图标，播放/停止 **/
	private ImageIcon soundPlay, soundStop;

	/** 服务器地址和登录状态 **/
	private JLabel lblHub = new JLabel();

	/** 图标：闪烁两个/连接/失效 **/
	private ImageIcon flashLight, flashDrak, connect, disconnect;

	/** 连接失效揭示文本 **/
	private String disconnectTip;
	
	/** 集群节点数目 **/
	private JLabel lblSites = new JLabel();
	
	/** 节点数目 **/
	private int m_sites;
	
	/** 节点前缀字符 **/
	private String sitesPrefix;
	
	/** 集群成员数目 **/
	private JLabel lblMembers = new JLabel();

	/** 注册账号、在线账号、在线人数数目 **/
	private int m_registers;

	private int m_onlines;
	
	private int m_persons;
	
	/** 成员前缀字符 **/
	private String membersPrefix;

	private ImageIcon invokerLight, invokerDark;
	
	/** 调用器 **/
	private JLabel lblInvoker = new JLabel();
	
	/** 线程前缀字符 **/
	private String invokerPrefix;

	/** 状态提示 **/
	private JLabel lblStatus = new JLabel();

	/** 守护线程，处理定时显示的参数 **/
	private MonitorThread monitor = new MonitorThread();
	
	/**
	 * 构造默认的状态栏面板
	 */
	public WatchStatusPanel() {
		super();
		m_sites = -1;
		m_registers = -1;
		m_onlines = -1;
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
	 * 加入线程
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
	 * 清除全部参数
	 */
	public void clear() {
		setTip("");
		setHub(null);
		setMembers(0, 0, 0);
		setSites(0);
		setDisconnectIcon();
	}

	/**
	 * 设置注册站点地址
	 * @param node
	 */
	public void setHub(Node node) {
		String tip = "";
		if (node != null) {
			tip = node.toString();
		}
		ShowHubThread e = new ShowHubThread(null, tip);
		monitor.addEvent(e);
	}
	
	
	/**
	 * 显示注册成员和在线成员
	 * @param registers 注册数目
	 * @param onlines 在线数目
	 */
	public void setMembers(int registers, int onlines, int persons) {
		boolean passed = (m_registers != registers || m_onlines != onlines || m_persons != persons);
		if (passed) {
			m_registers = registers;
			m_onlines = onlines;
			m_persons = persons;
			String tip = String.format(membersPrefix, registers, onlines, persons);
			ShowMembersThread e = new ShowMembersThread(tip);
			monitor.addEvent(e);
		}
	}
	
	/**
	 * 显示节点数目
	 * @param sites 节点数目
	 */
	public void setSites(int sites) {
		if (m_sites != sites) {
			m_sites = sites;
			String tip = String.format(sitesPrefix, sites);
			ShowSitesThread e = new ShowSitesThread(tip);
			monitor.addEvent(e);
		}
	}

	/**
	 * 设置提示文本
	 * @param tipText
	 */
	public void setTip(String tipText) {
		String str = (tipText == null ? "" : tipText);
		ShowStatusStatus thread = new ShowStatusStatus(str);
		monitor.addEvent(thread);
	}

	/**
	 * 设置提示文本
	 * @param format
	 * @param args
	 */
	public void setTip(String format, Object... args) {
		String text = String.format(format, args);
		setTip(text);
	}

	/**
	 * 图标名字
	 * @param name
	 * @return 返回图标实例
	 */
	private ImageIcon findImage(String name) {
		// 资源加载器，保存图标
		ResourceLoader loader = new ResourceLoader("conf/watch/image/window/status");
		return loader.findImage(name);
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
		/**
		 * 默认更新
		 */
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
	 * 根据状态，切换显示的图标，包括启动、下一个、停止。如果断网就中止图标出现。
	 * 这个方法被WatchWindow的线程调用，直接显示图标和显示文本！
	 * 
	 * @param who 图标状态编号
	 */
	private void doFlash(int who) {
		ImageIcon icon = null;
		String tooltip = null;
		// 判断登录
		boolean login = WatchLauncher.getInstance().isLogined();
		// 中断连接图标
		if (login) {
			if (who == WatchFlashTag.FLASH_START) {
				icon = flashLight;
			} else if (who == WatchFlashTag.FLASH_NEXT) {
				icon = flashDrak;
			} else if (who == WatchFlashTag.FLASH_STOP) {
				icon = connect;
			}
		} else {
			icon = disconnect;
			tooltip = disconnectTip;
		}

		// 修改图标
		if (icon != null) {
			lblHub.setIcon(icon);
		}
		// 修改提示文本
		if (tooltip != null) {
			FontKit.setToolTipText(lblHub, tooltip);
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
			super(true); // 同步方式处理
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
	 * 闪烁圆点图标
	 */
	public void flash() {
		ArrayList<SwingEvent> array = new ArrayList<SwingEvent>();

		// 图标编号
		int[] icons = new int[] { WatchFlashTag.FLASH_START, WatchFlashTag.FLASH_NEXT };

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

		// 结束时，多延长200毫秒，效果更好！
		FlashThread last = new FlashThread(WatchFlashTag.FLASH_STOP);
//		last.setIntervalTime(interval + 200);
		last.setTouchTime(touchTime + 200);
		array.add(last);

		// 一批线程放入队列
		SwingDispatcher.invokeThreads(array);
	}

	/**
	 * 调用器闪烁图标
	 * @param rolling 闪烁
	 */
	private void doRolling(boolean rolling) {
		ImageIcon icon = null;

		boolean login = WatchLauncher.getInstance().isLogined();
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
	
	class RollingThread extends SwingEvent {
		boolean rolling;

		RollingThread(boolean b) {
			super(true); // 将调用"SwingUtilities.invokeAndWait"方法，同步方式处理
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
	
	/**
	 * 设置连接状态的图标
	 */
	public void setConnectIcon() {
		ShowHubThread thread = new ShowHubThread(connect, null);
		monitor.addEvent(thread);
	}

	/**
	 * 标题内容
	 * @param name 名称
	 * @return 返回文本描述
	 */
	private String findCaption(String name) {
		return WatchLauncher.getInstance().findCaption(name);
	}

	/**
	 * 设置非连接状态
	 */
	public void setDisconnectIcon() {
		ShowHubThread thread = new ShowHubThread(disconnect, disconnectTip);
		monitor.addEvent(thread);
	}

	/**
	 * 初始化图标
	 */
	private void initIcons() {
		disconnectTip = findCaption("ToolTip/Disconnect/title");

		// 服务器状态
		flashLight = findImage("light.png");
		flashDrak = findImage("drak.png");
		connect = findImage("connect.png");
		disconnect = findImage("disconnect.png");
		soundPlay = findImage("sound_play.png");
		soundStop = findImage("sound_stop.png");
		invokerLight = findImage("invoker_on.png");
		invokerDark = findImage("invoker_off.png");
		
		lblHub.setIcon(disconnect);

		// 线程
//		ImageIcon threadsIcon = findImage("threads.png");
		invokerPrefix = WatchLauncher.getInstance().findCaption("ToolTip/Invoker/title");
		lblInvoker.setIcon(invokerDark);
//		String tip = String.format(threadsPrefix, 0);
//		FontKit.setToolTipText(lblThreads, tip);
//		lblThreads.setIcon(threadsIcon);
		
		// 声音
		String tip = WatchLauncher.getInstance().findCaption("ToolTip/Sound/title");
		FontKit.setToolTipText(lblSound, tip);
		boolean play = SoundPlayer.getInstance().isPlay();
		lblSound.setIcon(play ? soundPlay : soundStop);
		
		// 集群节点
		ImageIcon sitesIcon = findImage("sites.png");
		sitesPrefix = WatchLauncher.getInstance().findCaption("ToolTip/Sites/title");
		tip = String.format(sitesPrefix, 0);
		FontKit.setToolTipText(lblSites, tip);
		lblSites.setIcon(sitesIcon);
		
		// 集群用户
		ImageIcon membersIcon = findImage("members.png");
		membersPrefix = WatchLauncher.getInstance().findCaption("ToolTip/Members/title");
		tip = String.format(membersPrefix, 0, 0, 0);
		FontKit.setToolTipText(lblMembers, tip);
		lblMembers.setIcon(membersIcon);
	}

	/**
	 * 设置声音图标
	 * @param play 播放状态
	 */
	public void setSoundIcon(boolean play) {
		monitor.addEvent(new SetSoundIconThread(play));
	}

	/**
	 * 初始化状态栏
	 */
	public void init() {
		// 鼠标点击事件
		lblSound.addMouseListener(new StatusMouseAdapter());

		// 初始化图标参数
		initIcons();

		lblStatus.setVerticalAlignment(SwingConstants.CENTER);
		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);

		// 右侧
		JPanel right = new JPanel();
		right.setLayout(new GridLayout(1, 5, 5, 0));
		right.add(lblInvoker);
		right.add(lblSound);
		right.add(lblSites);
		right.add(lblMembers);
		right.add(lblHub);
		right.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		
//		EtchBorder outside = new EtchBorder(Color.GRAY, Color.WHITE);
//		EmptyBorder inside = new EmptyBorder(3, 4, 2, 4);
//		right.setBorder(new CompoundBorder(outside, inside));
		
		// 左侧
		JPanel left = new JPanel();
		left.setLayout(new BorderLayout(6, 0));
		left.add(lblStatus, BorderLayout.CENTER);
		left.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);

		// 显示底栏
		setLayout(new BorderLayout(5, 0));
		setBorder(new EmptyBorder(4, 6, 0, 2));
		add(left, BorderLayout.CENTER);
		add(right, BorderLayout.EAST);

//		// 使到计算器句柄
//		Timer timer = WatchLauncher.getInstance().getTimer();
//		// 当上个任务完成后，间隔5秒触发
//		timer.schedule(new ThreadTask(), 0, 2000);

		// 播放声音，或者否
		Boolean b = WatchProperties.readSoundPlay();
		boolean play = (b == null ? true : b.booleanValue());
		lblSound.setIcon(play ? soundPlay : soundStop);
		SoundPlayer.getInstance().setPlay(play);
		
//		if (play != null) {
//			setSoundIcon(play.booleanValue());
//		}
	}

//	/**
//	 * 显示线程
//	 *
//	 * @author scott.liang
//	 * @version 1.0 7/12/2012
//	 * @since laxcus 1.0
//	 */
//	class ThreadTask extends TimerTask {
//		long scaleInvoker;
//
//		public ThreadTask() {
//			super();
//			scaleInvoker = System.currentTimeMillis();
//		}
//
//		public void run() {
//			long time = System.currentTimeMillis();
//			// 更新调整器
//			if (time - scaleInvoker >= 6000) {
//				addThread(new InvokerThread());
//				scaleInvoker = time;
//			}
//		}
//	}

	/**
	 * 显示数目
	 * @author scott.liang
	 * @version 1.0 7/12/2012
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
	 * 显示线程数目
	 * 
	 * @author scott.liang
	 * @version 1.0 7/12/2012
	 * @since laxcus 1.0
	 */
	class ShowHubThread extends SwingEvent {
		ImageIcon icon;
		String tooltip;

		ShowHubThread(ImageIcon e, String tip) {
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
	 * 显示节点数目线程
	 *
	 * @author scott.liang
	 * @version 1.0 1/14/2020
	 * @since laxcus 1.0
	 */
	class ShowSitesThread extends SwingEvent {
		String tooltip;

		ShowSitesThread(String e) {
			super(true);
			tooltip = e;
		}

		public void process() {
			if (tooltip != null) {
				FontKit.setToolTipText(lblSites, tooltip);
			}
		}
	}

	/**
	 * 显示集群成员数目
	 *
	 * @author scott.liang
	 * @version 1.0 1/14/2020
	 * @since laxcus 1.0
	 */
	class ShowMembersThread extends SwingEvent {
		String tooltip;

		ShowMembersThread(String e) {
			super(true);
			tooltip = e;
		}

		public void process() {
			if (tooltip != null) {
				FontKit.setToolTipText(lblMembers, tooltip);
			}
		}
	}

//	/**
//	 * 显示线程数目
//	 * @author scott.liang
//	 * @version 1.0 7/12/2012
//	 * @since laxcus 1.0
//	 */
//	class ShowThreadTipThread extends SwingEvent {
//		String text;
//
//		ShowThreadTipThread(String s) {
//			super();
//			text = s;
//		}
//
//		public void process() {
//			FontKit.setToolTipText(lblInvoker, text);
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
		
		ArrayList<SwingEvent> threads = new ArrayList<SwingEvent>();

		public MonitorThread() {
			super();
			scaleInvoker = System.currentTimeMillis();
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
			// 更新时间
			long time = System.currentTimeMillis();
			// 更新调用器
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
		public void finish() {

		}
	}
	
}