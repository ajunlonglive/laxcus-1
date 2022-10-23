/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.status;

import java.awt.*;

import javax.swing.*;

import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 悬浮窗口，继续自菜单
 * 
 * @author scott.liang
 * @version 1.0 1/2/2022
 * @since laxcus 1.0
 */
class SnapshotWindow extends JPopupMenu {

	private static final long serialVersionUID = -3024410559463766213L;

	/** 绘制边框 **/
	private boolean paintBorder;

	/** 当前的图像 **/
	private Image image;

	/** 圆弧 **/
	private boolean round;

	/** 圆弧角度 **/
	private int roundARC;

	/**
	 * 弹出窗口菜单
	 */
	public SnapshotWindow() {
		super();
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		String value = UIManager.getString("StatusBar.FrameButton.SnapshotWindow.Round");
		round = (value != null && value.equalsIgnoreCase("YES"));
		if (round) {
			value = UIManager.getString("StatusBar.FrameButton.SnapshotWindow.RoundARC");
			roundARC = ConfigParser.splitInteger(value, 20);
		}

		// 窗口尺寸
		value = UIManager.getString("StatusBar.FrameButton.SnapshotWindow.Width");
		int width = ConfigParser.splitInteger(value, 280);
		value = UIManager.getString("StatusBar.FrameButton.SnapshotWindow.Height");
		int height = ConfigParser.splitInteger(value, 173);

		setPopupSize(new Dimension(width, height));
		// 透明
		setOpaque(false);

		// 默认是绘制边框
		paintBorder = true;
	}

	/**
	 * 设置边框
	 * @param b
	 */
	public void setPaintBorder(boolean b) {
		paintBorder = b;
	}

	/**
	 * 设置当前图像
	 * @param e
	 */
	public void setCurrentImage(Image e) {
		image = e;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (image != null) {
			Dimension d = getPreferredSize();
			int width = image.getWidth(null);
			int height = image.getHeight(null);
			if (width > 0 && height > 0) {
				int x = (d.width - width) / 2;
				int y = (d.height - height) / 2;
				if (x < 0) x = 0;
				if (y < 0) y = 0;
				g.drawImage(image, x, y, width, height, null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
		// 如果不绘制，退出
		if (!paintBorder) {
			return;
		}

		Color old = g.getColor();
		int width = getWidth();
		int height = getHeight();

		Color c = new Color(198, 198, 198); 
		// 不是灰色，选择弹出菜单的背景色调整加深处理
		if (!Skins.isGraySkin()) {
			c = UIManager.getColor("PopupMenu.background");
			if (c == null) {
				c = getBackground();
			}
			ESL esl = new ESL(c);
			esl.darker(22); // 调暗
			c = esl.toColor();
		}
		// 设置颜色
		g.setColor(c);

		// 圆角
		if (round) {
			int thickness = 1;
			int x = 0;
			int y = 0;
			for (int i = 0; i < thickness; i++) {
				g.drawRoundRect(x + i, y + i, width - (i * 2) - 1, height
						- (i * 2) - 1, roundARC, roundARC);
			}
		}
		// 矩形
		else {
			g.drawRect(0, 0, width - 1, height - 1);
		}

		// 还原颜色
		g.setColor(old);
	}

}
