/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.splash;

import java.awt.*;

import javax.swing.*;

/**
 * 启动屏窗口。
 * 在登录窗口/主窗口之前显示，为加载资源给用户一个暂时的过渡显示效果。
 * 
 * @author scott.liang
 * @version 1.0 1/16/2021
 * @since laxcus 1.0
 */
public class SplashWindow extends JWindow implements Runnable {

	private static final long serialVersionUID = -5725606364325303155L;

	/** 工作线程 **/
	private Thread thread;

	/** 停止 **/
	private volatile boolean interrupted;

	/** 运行状态 **/
	private volatile boolean running;

	/**
	 * 构造窗口
	 */
	public SplashWindow() {
		super();
		interrupted = false;
		running = false;
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

		}
	}

	/**
	 * 唤醒线程
	 */
	public synchronized void wakeup() {
		try {
			notify();
		}catch(IllegalMonitorStateException e) {

		}
	}

	/**
	 * 判断线程被要求中断。中断标记为“真”时，即是要求中断
	 * @return 返回真或者假
	 */
	public boolean isInterrupted() {
		return interrupted;
	}
	
	/**
	 * 判断处于运行状态
	 * @return
	 */
	public boolean isRunnin() {
		return running;
	}

	/**
	 * 停止线程运行
	 */
	public void stop() {
		// 没有运行，忽略！
		if (!isRunnin()) {
			return;
		}
		// 中断，唤醒线程
		interrupted = true;
		wakeup();
		// 判断是运行中，直到退出！
		while (isRunnin()) {
			delay(100);
		}
	}

	/**
	 * 建立窗口
	 * @param icon
	 */
	public void createWindow(ImageIcon icon) {
		//		int width = icon.getIconWidth();
		//		int height = icon.getIconHeight();

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // 设置光标

		JLabel label = new JLabel();
		label.setIcon(icon);

		// 设置界面
		getContentPane().add(label, BorderLayout.CENTER);

		Dimension screen = getToolkit().getScreenSize(); // 得到屏幕尺寸
		Point point = new Point((screen.width - icon.getIconWidth()) / 2, (screen.height - icon.getIconHeight()) / 2);
		pack(); // 窗口适应组件尺寸
		setLocation(point); // 设置窗口位置
	}

	/**
	 * 启动界面
	 */
	public void start() {
		toFront(); // 窗口前端显示
		
		thread = new Thread(this); // 实例化线程
		thread.start(); // 开始运行线程
		
		// 直到进入运行状态
		while(!isRunnin()) {
			delay(100);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// 中断为假
		interrupted = false;
		// 进入运行状态
		running = true;
		// 显示窗口
		setVisible(true);

		// 判断中断
		while (!isInterrupted()) {
			delay(500);
		}
		// 释放窗口
		dispose();
		// 释放参数
		running = false;
		thread = null;
	}


	//	JProgressBar progress; // 进度条
	// 
	//	public JWindowDemo() {
	//		Container container = getContentPane(); // getContentPane(); // 得到容器
	//		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // 设置光标
	//		URL url = getClass().getResource("login.jpg"); // 图片的位置
	//		if (url != null) {
	//			container.add(new JLabel(new ImageIcon(url)), BorderLayout.CENTER); // 增加图片
	//		}
	//		progress = new JProgressBar(1, 100); // 实例化进度条
	//		progress.setStringPainted(true); // 描绘文字
	//		progress.setString("加载程序中,请稍候......"); // 设置显示文字
	//		progress.setBackground(Color.white); // 设置背景色
	//		container.add(progress, BorderLayout.SOUTH); // 增加进度条到容器上
	// 
	//		Dimension screen = getToolkit().getScreenSize(); // 得到屏幕尺寸
	//		pack(); // 窗口适应组件尺寸
	//		setLocation((screen.width - getSize().width) / 2,
	//				(screen.height - getSize().height) / 2); // 设置窗口位置
	//	}

	//	public void run() {
	//		running = true;
	//		setVisible(true); // 显示窗口
	//		// try {
	//		// for (int i = 0; i < 100; i++) {
	//		// delay(100); // 线程休眠
	//		// // progress.setValue(progress.getValue() + 1); // 设置进度条值
	//		// }
	//		// } catch (Exception ex) {
	//		// ex.printStackTrace();
	//		// }
	//
	//		// 判断中断
	//		while (!isInterrupted()) {
	//			delay(500);
	//		}
	//
	//		dispose(); // 释放窗口
	//		
	//		running = false;
	//		thread = null;
	//		// showFrame(); // 运行主程序
	//	}

	//	static void showFrame() {
	//		JFrame frame = new JFrame("程序启动界面演示"); // 实例化JFrame对象
	//		frame.setSize(300, 200); // 设置窗口尺寸
	//		frame.setVisible(true); // 窗口可视
	//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 关闭窗口时退出程序
	//	}
	// 
	//	public static void main(String[] args) {
	//		SplashWindow splash = new SplashWindow();
	//		splash.start(); // 运行启动界面
	//	}

}