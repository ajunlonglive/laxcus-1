/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.cycle;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.frame.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 切换按纽
 * 
 * @author scott.liang
 * @version 1.0 2/11/2022
 * @since laxcus 1.0
 */
class CycleButton extends JButton {
	
	private static final long serialVersionUID = 6955396971691502522L;

	/** 索引，从0开始 **/
	private int index;

	/** 标题 **/
	private String title;
	
	/** 窗口句柄 **/
	private LightFrame frame;

//	/**
//	 * 
//	 */
//	public CycleButton() {
//		super();
//	}

//	/**
//	 * @param arg0
//	 */
//	public CycleButton(Icon icon) {
//		super(icon);
//	}

//	/**
//	 * @param arg0
//	 */
//	public CycleButton(String arg0) {
//		super(arg0);
//		// TODO Auto-generated constructor stub
//	}

//	/**
//	 * @param arg0
//	 */
//	public CycleButton(Action arg0) {
//		super(arg0);
//		// TODO Auto-generated constructor stub
//	}

//	/**
//	 * @param arg0
//	 * @param arg1
//	 */
//	public CycleButton(int index , String title, Icon icon) {
//		super();
//		init();
//		setIndex(index);
//		setTitle(title);
//		setIcon(icon);
//	}
	
	/**
	 * 构造实例
	 * @param index
	 * @param frame
	 */
	public CycleButton(int index, LightFrame frame) {
		super();
		init();
		setIndex(index);
		setLightFrame(frame);
		setTitle(frame.getTitle());
		setIcon(frame.getFrameBigIcon());
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		setIconTextGap(0);

		setContentAreaFilled(false); // 平面
		setFocusPainted(false); // 不绘制焦点边框
		setRolloverEnabled(false); // 反转...

		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);

		setBorder(new EmptyBorder(4, 4, 4, 4));
		
		Dimension d = new Dimension(52,52);
		setPreferredSize(d);
		setMaximumSize(d);
		setMinimumSize(d);
		
		setSelected(false);
	}

	public void setIndex(int i) {
		index = i;
	}

	public int getIndex() {
		return index;
	}

	public void setTitle(String s) {
		title = s;
	}

	public String getTitle() {
		return title;
	}

	public void setLightFrame(LightFrame e) {
		frame = e;
	}

	public LightFrame getLightFrame() {
		return frame;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
		if (!isSelected()) {
			super.paintBorder(g);
			return;
		}

		int width = getWidth();
		int height = getHeight();

		Color old = g.getColor();

		if (Skins.isGraySkin()) {
			g.setColor(Color.DARK_GRAY);
		} else {
			Color c = UIManager.getColor("PopupMenu.background");
			if (c == null) {
				c = getBackground();
			}
			// 调亮
			ESL esl = new ESL(c);
			esl.brighter(50);
			c = esl.toColor();
			g.setColor(c);
		}

		// 绘制边框
		int x = 0;
		int y = 0;
		int thickness = 2;
		for (int i = 0; i < thickness; i++) {
			g.drawRect(x + i, y + i, width - i - i - 1, height - i - i - 1);
		}
		
		// 恢复
		g.setColor(old);
	}

}