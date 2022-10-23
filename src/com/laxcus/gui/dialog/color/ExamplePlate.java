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

/**
 * 示例面板
 * 
 * @author scott.liang
 * @version 1.0 9/1/2021
 * @since laxcus 1.0
 */
public class ExamplePlate extends JButton {

	private static final long serialVersionUID = -5552759940861694577L;

	/** 选择颜色 **/
	private Color selectColor;

	/** 范围 **/
//	private Dimension dim = new Dimension(62,62);
	private Dimension dim = new Dimension(78,78);

	/**
	 * 构造示例面板
	 */
	public ExamplePlate() {
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

		selectColor = Color.WHITE; // new Color(0, 0, 0);
	}

	/**
	 * 设置选择颜色
	 * @param c
	 */
	public void setSelectColor(Color c) {
		selectColor = c;
	}

	/**
	 * 返回选择颜色
	 * @return
	 */
	public Color getSelectColor(){
		return selectColor;
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
			g.setColor(selectColor);
			g.fillRect(0, 0, width - 1, height - 1);

			g.setColor(back);
		} else {
			super.paintComponent(g);
		}
	}

}
