/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.splash;

import java.awt.*;
import java.awt.event.*;

import javax.swing.border.*;

import com.laxcus.gui.*;
import com.laxcus.log.client.*;
import com.laxcus.platform.*;

/**
 * 启动封面窗口。<br><br>
 * 
 * 在应用启动时显示，启动成功后退出。显示开发者、版本号等信息。<br><br>
 * 
 * 系统提供基本的图片显示，开发者可以基于这个类自定义其它内容和显示。<br><br>
 * 
 * 注意：显示启动封面窗口，要放在SwingEvent.process中处理，否则会有阻塞现象，不能正常显示。关闭也是同样处理。
 * 
 * @author scott.liang
 * @version 1.0 6/13/2022
 * @since laxcus 1.0
 */
public class SplashForm extends LightForm {

	private static final long serialVersionUID = 2601145096423379111L;

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

	/** 面板 **/
	private SplashPanel panel;

	/**
	 * 构造默认的启动封面窗口
	 */
	public SplashForm() {
		super();
		// 隐藏标题栏
		hideTitlePane();
	}

	/**
	 * 构造启动封面窗口
	 * @param image 图片
	 */
	public SplashForm(Image image) {
		this();
		// 图片
		setImage(image);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.gui.LightForm#showTitlePane()
	 */
	@Override
	public void showTitlePane() {
		// 不要调用父类
	}

	/**
	 * 设置背景图片
	 * @param image
	 */
	public void setImage(Image image) {
		if (image != null) {
			panel = new SplashPanel(image);
		}
	}

	/**
	 * 显示窗口
	 */
	public void showWindow() {
		if (panel != null) {
			// 无边框
			panel.setBorder(new EmptyBorder(0, 0, 0, 0));

			Dimension r = panel.getSize();

			// 窗口尺寸
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			// 自定义窗口
			PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
			if (desktop != null) {
				d = desktop.getSize();
			}

			int x = (d.width - r.width) / 2;
			int y = (d.height - r.height) / 2 - 20; // 向上提20个像素
			if (x < 0) x = 0;
			if (y < 0) y = 0;
			setBounds(x, y, r.width, r.height);

			// 鼠标事件
			panel.addMouseListener(mouseListener);
			panel.addMouseMotionListener(mouseListener);

			// 根对象
			setContentPane(panel);
			setBorder(new EmptyBorder(0, 0, 0, 0));
		}

		// 取消注册
		PlatformDesktop desktop = PlatformKit.getPlatformDesktop();
		if (desktop != null) {
			desktop.add(this, new Integer(FormLayer.TOP_WINDOW));
		}

		try {
			setSelected(true);
		} catch (java.beans.PropertyVetoException e) {
			Logger.error(e);
		}

		// 显示
		setVisible(true);
	}

	/**
	 * 显示窗口
	 * @param image 背景图片
	 */
	public void showWindow(Image image) {
		// 设置图片
		setImage(image);
		// 显示
		showWindow();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.gui.LightForm#closeWindow()
	 */
	@Override
	public void closeWindow() {
		// 父类实例
		Container c = getParent();
		// 取出窗口范围
		Rectangle r = (isVisible() ? getBounds() : null);
		if (r != null) {
			r = new Rectangle(r.x, r.y, r.width, r.height);
		}

		// 结束可视和销毁
		setVisible(false);
		dispose(); // 销毁

		// 重新绘制窗口
		if (c != null && r != null) {
			c.repaint(r.x, r.y, r.width, r.height);
		}
	}

}