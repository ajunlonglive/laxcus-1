/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.frame.log;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import com.laxcus.ray.frame.*;
import com.laxcus.ray.pool.*;
import com.laxcus.gui.dialog.message.*;
import com.laxcus.register.*;

/**
 * 日志框架
 * 
 * @author scott.liang
 * @version 1.0 5/26/2021
 * @since laxcus 1.0
 */
public class RayLogFrame extends RayFrame {

	private static final long serialVersionUID = -4266477207257904104L;
	
	static RayLogFrame selfHandle;
	
	/**
	 * 返回实例
	 * @return
	 */
	public static RayLogFrame getInstance() {
		return RayLogFrame.selfHandle;
	}

//	/** 已经启动或者否 **/
//	private static volatile boolean loaded = false;
	
//	/**
//	 * 判断加载或者否
//	 * @return 返回真或者否
//	 */
//	public static boolean isLoaded() {
//		return DesktopLogFrame.loaded;
//	}
//	
//	/**
//	 * 加载窗口
//	 * @param b
//	 */
//	private static void setLoad(boolean b) {
//		DesktopLogFrame.loaded = b;
//	}

	private RayLogPanel panel = new RayLogPanel();
	
	/**
	 * 构造日志框架
	 */
	public RayLogFrame() {
		super();
		
//		setNativeTitlePane();
	}
	
	/**
	 * 设置字体
	 * @param font
	 */
	public void setSelectFont(Font font) {
		panel.setSelectFont(font);
	}

	class WindowCloseAdapter extends InternalFrameAdapter {
		public void internalFrameClosing(InternalFrameEvent e) {
			exit();	
		}
	}
	
	/**
	 * 设置最大显示单元数目。范围在0 - 2000之间
	 * @param n 日志单元数
	 */
	public int setMaxLogs(int n) {
		return panel.setMaxItems(n);
	}
	
	/**
	 * 关闭窗口
	 */
	private void exit() {
		//		String title = findCaption("MessageBox/Exit/Log/Title/title");
		//		String content = findCaption("MessageBox/Exit/Log/Message/title");

		String title = UIManager.getString("LogFrame.ExitTitle");
		String content = UIManager.getString("LogFrame.ExitContent");
		boolean exit = MessageBox.showYesNoDialog(this, title, content);
		// 判断是关闭窗口
		if (!exit) {
			return;
		}

		// 关闭窗口
		closeWindow();
	}
	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.front.desktop.component.DesktopFrame#closeWindow()
//	 */
//	@Override
//	public void closeWindow() {
//		// 保存位置
//		Rectangle rect = getBounds();
////		DesktopProperties.writeLogBound(rect);
//		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "LogFrame/Bound", rect);
//
//		// 设置日志
//		DesktopLogTrustor.getInstance().setLogTransmitter(null);
//		
//		// 停止
//		DesktopLogFrame.setLoad(false);
//		
//		// 关闭窗口
//		setVisible(false);
//		// 销毁窗口
//		dispose();
//	}
	
	
//	/**
//	 * 设置默认窗口
//	 */
//	private void setDefautBounds() {
//		Dimension d = PlatformKit.getPlatformDesktop().getSize();
//		int w = d.width / 2;
//		int h = d.height / 2;
//		int x = (d.width - w) / 2;
//		int y = (d.height - h) / 2;
//		Rectangle r = new Rectangle(x, y, w, h);
//		setBounds(r);
//	}
	
	/**
	 * 设置默认窗口
	 */
	private void setDefautBounds() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int) ((double) d.width * 0.618);
		int h = (int) ((double) d.height * 0.618);
		int x = (d.width - w) / 2;
		int y = (d.height - h) / 2;
		Rectangle r = new Rectangle(x, y, w, h);
		setBounds(r);
	}
	
	private void writeBounds() {
		Rectangle rect = getBounds();
		RTKit.writeBound(RTEnvironment.ENVIRONMENT_SYSTEM, "LogFrame/Bound", rect);
	}
	
	private Rectangle readBounds() {
		return RTKit.readBound(RTEnvironment.ENVIRONMENT_SYSTEM, "LogFrame/Bound");
	}
	
	/**
	 * 初始化窗口
	 */
	public void showWindow() {
		// 确认窗口范围
		Rectangle rect = readBounds();
		if (rect != null) {
			setBounds(rect);
		} else {
			setDefautBounds();
		}
		
		panel.init();
		
		// 设置面板
		setContentPane(panel);

		// 内部事件
		addInternalFrameListener(new WindowCloseAdapter());
		
		// 图标和标题
//		setFrameIcon(findImage("conf/ray/image/frame/log.png", 16, 16));
//		setTitle(findCaption("Window/Frame/Log/title"));

		// 图标和标题
		setFrameIcon(UIManager.getIcon("LogFrame.TitleIcon"));
		setFrameBigIcon(UIManager.getIcon("LogFrame.TitleBigIcon"));
		setTitle(UIManager.getString("LogFrame.Title"));

		// 最小化
		setIconifiable(true);
		// 可关闭
		setClosable(true);
		// 可改变大小
		setResizable(true);
		// 最大化
		setMaximizable(true);
		// 销毁窗口时触发事件
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		RayLogTrustor.getInstance().setLogTransmitter(panel);
		
		// 句柄
		RayLogFrame.selfHandle = this;
		
		// 显示窗口
		setVisible(true);
	}
	
//	private void setNativeTitlePane() {
//		if (!isMetalUI()) {
//			return;
//		}
//
//		javax.swing.plaf.InternalFrameUI ui = getUI();
//		if (Laxkit.isClassFrom(ui, MetalInternalFrameUI.class)) {
//			MetalInternalFrameUI mui = (MetalInternalFrameUI)ui;
//			LogInternalFrameTitlePane title = new LogInternalFrameTitlePane(this);
//			title.setPreferredSize(new Dimension(20, 32));
//			mui.setNorthPane(title);
//		}
//	}

	/* (non-Javadoc)
	 * @see com.laxcus.ui.frame.LightFrame#releaseBefore()
	 */
	@Override
	protected void release0() {
		writeBounds();
		
		// 设置日志
		RayLogTrustor.getInstance().setLogTransmitter(null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.ui.frame.LightFrame#release1()
	 */
	@Override
	protected void release1() {
		RayLogFrame.selfHandle = null;
	}
	
}