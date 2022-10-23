/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.shutdown;

import java.awt.*;
import javax.swing.*;

import com.laxcus.gui.component.*;

/**
 * 关闭按纽
 * 
 * @author scott.liang
 * @version 1.0 6/14/2021
 * @since laxcus 1.0
 */
class ShutdownButton extends CraftButton {

	private static final long serialVersionUID = -3434773393456452415L;

	/**
	 * 构造默认的桌面图标按纽
	 */
	public ShutdownButton() {
		super();
		init();
	}

	/**
	 * 构造桌面图标按纽，按定文本和图标
	 * @param text
	 * @param icon
	 */
	public ShutdownButton(String text) {
		super(text);
		// 初始化
		init();
	}
	
	/**
	 * 设置图标
	 * @param icon
	 * @param light 高亮增加值
	 */
	public void setIcon(ImageIcon icon, int light) {
		super.setIcon(icon);

		// ESL的亮度增加
		if (light > 0) {
			ImageIcon image = brighter(icon, light);
			if (image != null) {
				super.setPressedIcon(image);
				super.setSelectedIcon(image);
				super.setRolloverIcon(image);
				super.setRolloverSelectedIcon(image);
			}
		}
	}
	
	/**
	 * 初始化参数
	 */
	private void init() {
		setContentAreaFilled(false);
		setBorderPainted(false);
		setOpaque(true);
		setFocusPainted(false);
		
		setVerticalTextPosition(SwingConstants.BOTTOM); // 文本在图标的下面
		setHorizontalTextPosition(SwingConstants.CENTER); // 居中布置
		setVerticalAlignment(SwingConstants.TOP); // 图文从下向下
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (hasFocus()) {
			Color back = g.getColor();

			int x1 = 0;
			int y1 = 0;
			int x2 = x1 + getWidth() - 1;
			int y2 = y1 + getHeight() - 1;

			// NIMBUS界面或者其它
//			if (Skins.isNimbus()) {
//				g.setColor(Color.BLACK);
//			} else {
//				g.setColor(Color.WHITE);
//			}
			
			g.setColor(Color.WHITE);
			for (int x = 0; x < x2; x += 2) {
				g.drawLine(x, y1, x, y1); // 上横线
				g.drawLine(x, y2, x, y2); // 下横线
			}
			for (int y = 0; y < y2; y += 2) {
				g.drawLine(x1, y, x1, y); // 左侧线
				g.drawLine(x2, y, x2, y); // 右侧线
			}
			
			// 恢复原来的颜色
			g.setColor(back);
		}

	}

}
