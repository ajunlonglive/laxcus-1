/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.color;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.color.*;

/**
 * 滑动面板
 * 
 * @author scott.liang
 * @version 1.0 9/1/2021
 * @since laxcus 1.0
 */
public class SildePlate extends JButton {

	private static final long serialVersionUID = 6636606850716127346L;

	/** 选择颜色 **/
	private ESL selectColor;

	/** 范围 **/
	private Dimension dim = new Dimension(28, 241);

	/**
	 * 构造滑动面板
	 */
	public SildePlate() {
		super();
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		setPreferredSize(dim);
		setMaximumSize(dim);
		setMinimumSize(dim);

		setContentAreaFilled(false); // 平面
		setBorderPainted(false); // 不绘制边框

		setBorder(new EmptyBorder(0, 0, 0, 0));
		
		selectColor = new ESL(0, 0, 0);
	}

	/**
	 * 设置选择颜色
	 * @param c
	 */
	public void setSelectColor(Color c) {
		if (c != null) {
			selectColor = new RGB(c).toESL();
		}
	}

	/**
	 * 返回选择颜色
	 * @return
	 */
	public Color getSelectColor() {
		if (selectColor == null) {
			return null;
		}
		return selectColor.toColor();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		// 如果定义颜色...
		if (selectColor != null) {
			Color back = g.getColor();

			int width = getWidth();
			int height = getHeight();

			// 设置颜色
			int L = 240;
			double E = selectColor.getH();
			double S = selectColor.getS();
			for (int y = 0; y < height; y++) {
				ESL esl = new ESL(E, S, L);
				g.setColor(esl.toColor());
				g.drawLine(0, y, width - 1, y);
				L--;
			}

			g.setColor(back);
		} else {
			super.paintComponent(g);
		}
	}
	
}
