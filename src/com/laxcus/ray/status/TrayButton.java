/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.status;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.tray.*;

/**
 * 切换按纽
 * 
 * @author scott.liang
 * @version 1.0 2/25/2022
 * @since laxcus 1.0
 */
class TrayButton extends JButton {

	private static final long serialVersionUID = 6955396971691502522L;

	/** 索引，从0开始 **/
	private int index;

	/** 托盘实例 **/
	private Tray tray;

	/**
	 * 构造实例
	 * @param index 索引编号
	 * @param tray 托盘
	 */
	public TrayButton(int index, Tray tray) {
		super();
		init();
		setIndex(index);
		setTray(tray);
		// 设置默认参数
		doDefault();
	}

	/**
	 * 设置默认值
	 */
	private void doDefault() {
		setToolTipText(tray.getTooltip());

		Icon icon = tray.getIcon();

		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		boolean match = (width == 16 && height == 16);
		if (!match) {
			throw new IllegalArgumentException("width != 16 || height != 16");
		}

		// 设置图标
		setIcon(icon);
		// 其它图标
		setRolloverIcon(tray.getRolloverIcon());
		setPressedIcon(tray.getPressedIcon());
		setSelectedIcon(tray.getPressedIcon());
	}

	/**
	 * 初始化
	 */
	private void init() {
		setIconTextGap(0);

		setContentAreaFilled(false); // 平面
		setFocusPainted(false); // 不绘制焦点边框
		setRolloverEnabled(false); // 反转...
		setBorderPainted(false); // 不绘制边框

		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
		
//		setBorder(new EmptyBorder(4, 4, 4, 4));
//		Dimension d = new Dimension(34, 34); 
		
		setBorder(new EmptyBorder(0, 0, 0, 0));
		Dimension d = new Dimension(34, 34);

		setPreferredSize(d);
		setMaximumSize(d);
		setMinimumSize(d);

		setSelected(false);
	}

	/**
	 * 设置索引值
	 * @param i
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * 返回索引值
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * 设置托盘
	 * @param e
	 */
	public void setTray(Tray e) {
		tray = e;
	}

	/**
	 * 返回托盘
	 * @return
	 */
	public Tray getTray() {
		return tray;
	}

	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.AbstractButton#paintBorder(java.awt.Graphics)
	//	 */
	//	@Override
	//	protected void paintBorder(Graphics g) {
	//		if (!isSelected()) {
	//			super.paintBorder(g);
	//			return;
	//		}
	//
	//		int width = getWidth();
	//		int height = getHeight();
	//
	//		Color old = g.getColor();
	//
	//		if (Skins.isGraySkin()) {
	//			g.setColor(Color.DARK_GRAY);
	//		} else {
	//			Color c = UIManager.getColor("PopupMenu.background");
	//			if (c == null) {
	//				c = getBackground();
	//			}
	//			// 调亮
	//			ESL esl = new ESL(c);
	//			esl.brighter(50);
	//			c = esl.toColor();
	//			g.setColor(c);
	//		}
	//
	//		// 绘制边框
	//		int x = 0;
	//		int y = 0;
	//		int thickness = 2;
	//		for (int i = 0; i < thickness; i++) {
	//			g.drawRect(x + i, y + i, width - i - i - 1, height - i - i - 1);
	//		}
	//		
	//		// 恢复
	//		g.setColor(old);
	//	}


	//	/*
	//	 * (non-Javadoc)
	//	 * @see javax.swing.AbstractButton#setIcon(javax.swing.Icon)
	//	 */
	//	@Override
	//	public void setIcon(Icon icon) {
	//		int width = icon.getIconWidth();
	//		int height = icon.getIconHeight();
	//		boolean match = (width == 16 && height == 16);
	//		if (!match) {
	//			throw new IllegalArgumentException("width != 16 || height != 16");
	//		}
	//
	//		// 设置图标
	//		super.setIcon(icon);
	//		
	//		setRolloverIcon(tray.);
	//		setPressedIcon(dark);
	//		setSelectedIcon(dark);
	//
	//		// 指定的图标
	//		if (icon.getClass() == ImageIcon.class) {
	//			ImageIcon img = (ImageIcon) icon;
	//			ImageIcon dark = ImageUtil.dark(img, -30);
	//			ImageIcon light = ImageUtil.brighter(img, 20);
	//			setRolloverIcon(light);
	//			setPressedIcon(dark);
	//			setSelectedIcon(dark);
	//		}
	//	}

}