/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.awt.*;

import javax.swing.*;

import com.laxcus.front.desktop.util.*;

/**
 * 磁吸轮廓窗口
 * 
 * @author scott.liang
 * @version 1.0 5/20/2022
 * @since laxcus 1.0
 */
final class DesktopMagneticWindow extends JPopupMenu {

	private static final long serialVersionUID = -6353061439066754823L;

	/**
	 * 构造默认的磁吸轮廓窗口
	 */
	public DesktopMagneticWindow() {
		super();
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		Dimension dim = DesktopUtil.getDesktopButtonSize(); // 桌面按纽尺寸
		setPopupSize(new Dimension(dim.width, dim.height));
		// 透明
		setOpaque(false);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
		Color old = g.getColor();

		// 固定为白色，对应DesktopButton的焦点点阵边框
		g.setColor(Color.WHITE);

		// 绘制点阵
		int x1 = 0;
		int y1 = 0;
		int x2 = x1 + getWidth() - 1;
		int y2 = y1 + getHeight() - 1;
		for (int x = 0; x <= x2; x += 2) {
			g.drawLine(x, y1, x, y1); // 上横线点
			g.drawLine(x, y2, x, y2); // 下横线点
		}
		for (int y = 0; y <= y2; y += 2) {
			g.drawLine(x1, y, x1, y); // 左侧线点
			g.drawLine(x2, y, x2, y); // 右侧线点
		}
	
		// 还原颜色
		g.setColor(old);
	}

}