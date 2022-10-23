/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.login;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.laxcus.front.*;
import com.laxcus.thread.*;
import com.laxcus.util.border.*;
import com.laxcus.util.event.*;
import com.laxcus.util.net.*;

/**
 * 登录追踪接口
 * 
 * @author scott.liang
 * @version 1.0 5/21/2021
 * @since laxcus 1.0
 */
public class DesktopTrackDialog extends JDialog implements FrontLoginTracker, Runnable {
	
	private static final long serialVersionUID = -4549009601475515233L;

	class MouseDragAdapter extends MouseAdapter {

		/** 拖放 **/
		private boolean dragged;

		/** 坐标 **/
		private Point axis;
		
		public MouseDragAdapter(){
			super();
			dragged = false;
		}

		public void mousePressed(MouseEvent e) {
			dragged = true;
			axis = new Point(e.getX(), e.getY());
			setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}

		public void mouseReleased(MouseEvent e) {
			dragged = false;
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		public void mouseDragged(MouseEvent e) {
			if (dragged) {
				int x = e.getXOnScreen() - axis.x;
				int y = e.getYOnScreen() - axis.y;
				setLocation(x, y);
			}
		}
	}
	
	/** 鼠标事件 **/
	private MouseDragAdapter mouseListener = new MouseDragAdapter();
	
	/** 位置 **/
	private int seek;
	
	/** 显示图标 **/
	private Icon icon;

	/** 启动10个 **/
	private JLabel[] lables;
	
	/** 线程句柄  **/
	private Thread thread;

	/** 停止 **/
	private volatile boolean stopped;

	/** 运行标记 **/
	private volatile boolean running;
	
	/**
	 * 构造窗口
	 * @param owner
	 * @param modal
	 */
	public DesktopTrackDialog(JDialog owner, boolean modal) {
		super(owner, modal);
		
		init();
	}
	
	/**
	 * 初始化参数
	 */
	private void init() {
		stopped = false;
		running = false;
		
		icon = UIManager.getIcon("LoginDialog.BallIcon");
		
//		ResourceLoader loader = new ResourceLoader();
//		icon = loader.findImage("conf/desktop/image/login/ball.png");
	}

	/**
	 * 线程加入分派器
	 * @param thread
	 */
	private void addThread(SwingEvent thread) {
		SwingDispatcher.invokeThread(thread);
	}
	
	class StopThread extends SwingEvent {
		StopThread() {
			super(true); // 调用SwingUtilities.invokeAndWait同步处理
		}

		public void process() {
			destroy();
		}
	}

	class FlashThread extends SwingEvent {
		FlashThread() {
			super(true); // 调用SwingUtilities.invokeAndWait同步处理
		}

		public void process() {
			flash();
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.LoginTracker#start()
	 */
	@Override
	public void start() {
		if (isRunning()) {
			return;
		}

		thread = new Thread(this);
		thread.start();

		// 直到启动!
		while (!isRunning()) {
			delay(1000);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		running = true;
		
		// 打开窗口
		showDialog();
		
		// 清除参数
		running = false;
		thread = null;
	}
	
	/** 光标点线程 **/
	private HopRunner runner;
	
	/**
	 * 负责光标点显示和稳定的线程
	 *
	 * @author scott.liang
	 * @version 1.0 1/20/2020
	 * @since laxcus 1.0
	 */
	class HopRunner extends VirtualThread {
		
		public HopRunner() {
			super();
		}

		/* (non-Javadoc)
		 * @see com.laxcus.thread.VirtualThread#init()
		 */
		@Override
		public boolean init() {
			return true;
		}

		/* (non-Javadoc)
		 * @see com.laxcus.thread.VirtualThread#process()
		 */
		@Override
		public void process() {
			// 延时500毫秒，等待窗口显示出来
			delay(500L);
			// 稳定和显示光标点，一秒钟出现移到一个
			while (!stopped) {
				addThread(new FlashThread());
				delay(1000L);
			}
			// 准备退出
			setInterrupted(true);
			
			// 销毁窗口，让showDialog.setVisible结束！
			addThread(new StopThread());
		}

		/* (non-Javadoc)
		 * @see com.laxcus.thread.VirtualThread#finish()
		 */
		@Override
		public void finish() { }
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.LoginTracker#stop()
	 */
	@Override
	public void stop() {
		// 这个方法是被调用，负责停止显示
		
		// 停止和唤醒线程！
		stopped = true;
		if (runner != null) {
			runner.wakeup();
		}

		// 在运行状态，等待！
		while (isRunning()) {
			delay(100);
		}
	}

	/**
	 * 线程延时等待。单位：毫秒。
	 * @param timeout 超时时间
	 */
	public synchronized void delay(long timeout) {
		try {
			if (timeout > 0L) {
				wait(timeout);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 唤醒线程
	 */
	public synchronized void wakeup() {
		try {
			notify();
		}catch(IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}
	
	/**
	 * 判断处于运行状态
	 * @return 返回真或者假
	 */
	public boolean isRunning() {
		return running && thread != null;
	}
	
	/**
	 * 销毁窗口
	 */
	private void destroy() {
		dispose();
	}
	
	/**
	 * 设置默认的边框
	 */
	private void setDefaultBounds() {
		Window owner = getOwner();
		Rectangle frm = owner.getBounds();

		// 这两值不变
		final int width = 330;
		final int height = 68;

		// 计算相对于父窗口的X/Y间隔值
		int gapx = (width < frm.width ? (frm.width - width) / 2 : -((width - frm.width) / 2));
		int gapy = (height < frm.height ? (frm.height - height) / 2 : -((height - frm.height) / 2));
		int x = frm.x + gapx;
		int y = frm.y + gapy;

		// 最小是0
		if (x < 0) x = 0;
		if (y < 0) y = 0;

		// 超过显示范围时...
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		if (x + width > dim.width) {
			x = dim.width - width;
		}
		if (y + height > dim.height) {
			y = dim.height - height;
		}
		
		setBounds(new Rectangle(x, y, width, height));
	}
	
	/**
	 * 初始化和显示界面
	 */
	public void showDialog() {
		// 分配句柄
		seek = 0;
		lables = new JLabel[12];
		for (int i = 0; i < lables.length; i++) {
			lables[i] = new JLabel();
			lables[i].setHorizontalAlignment(SwingConstants.CENTER);
		}

//		// 浮雕
//		EmptyBorder inside = new EmptyBorder(2, 10, 2, 10);
//		BevelBorder outside =new BevelBorder(BevelBorder.RAISED);
//		CompoundBorder border = new CompoundBorder(outside, inside);

		// 放到面板上
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, lables.length));
		panel.setBorder(new ShadownBorder());
		for (int i = 0; i < lables.length; i++) {
			panel.add(lables[i]);
		}

		//		Container canvas = getContentPane();
		//		canvas.setLayout(new BorderLayout(0, 0));
		//		canvas.add(panel, BorderLayout.CENTER);

		// 鼠标事件...
		panel.addMouseListener(mouseListener);
		panel.addMouseMotionListener(mouseListener);
		
		// 设置
		setContentPane(panel);
		
		// 无边框
		setUndecorated(true);
		
		// 调整尺寸
		pack();
		// 用户不能调整窗口大小
		setResizable(false);
		
//		// 设置对话窗口范围
//		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
//		int width = 330;
//		int height = 68;
//		int x = (size.width - width) / 2;
//		int y = (size.height - height) / 2;
//		setBounds(new Rectangle(x, y, width, height));
		
		// 设置对话窗口范围
		setDefaultBounds();

		// 启动显示线程，有显示前
		runner = new HopRunner();
		runner.start();
		
		// 阻塞在这里
		setVisible(true);
	}

	/**
	 * 图标显示
	 */
	private void flash() {
		if (seek >= lables.length) {
			seek = 0;
		}

		for (int i = 0; i <= seek; i++) {
			lables[i].setIcon(icon);
		}
		for (int i = seek + 1; i < lables.length; i++) {
			lables[i].setIcon(null);
		}

		seek++;
	}
	
	/** 获取码 **/
	private int pitchId;
	
	/** 登录地址 **/
	private SiteHost pitchHub;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLoginTracker#setPitchId(int)
	 */
	@Override
	public void setPitchId(int who) {
		pitchId = who;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLoginTracker#getPitchId()
	 */
	@Override
	public int getPitchId() {
		return pitchId;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLoginTracker#setPitchHub(com.laxcus.util.net.SiteHost)
	 */
	@Override
	public void setPitchHub(SiteHost host) {
		pitchHub = host;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLoginTracker#getPitchHub()
	 */
	@Override
	public SiteHost getPitchHub() {
		return pitchHub;
	}	
}