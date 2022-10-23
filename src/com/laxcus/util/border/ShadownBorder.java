/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.border;

import java.awt.*;

import javax.swing.border.AbstractBorder;

import com.laxcus.util.skin.*;

/**
 * 具有阴影的字体边框
 * 
 * @author scott.liang
 * @version 1.0 2/27/2020
 * @since laxcus 1.0
 */
public class ShadownBorder extends AbstractBorder {

	private static final long serialVersionUID = 9053638341317544136L;

	private Insets editorBorderInsets;

	/**
	 * 构造默认的具有阴影的字体边框
	 */
	public ShadownBorder(int gap) {
		super();
		createInsets(gap);
	}
	
	/**
	 * 构造默认的具有阴影的字体边框
	 */
	public ShadownBorder() {
		this(2);
	}

	/**
	 * 设置
	 * @param gap
	 */
	private void createInsets(int gap) {
		if (gap < 1) gap = 1;
		editorBorderInsets = new Insets(gap, gap, gap, 0);
	}

	/**
	 * 重绘边框
	 * @param c
	 * @param g
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);

		// 暗黑和光亮！
		Color shadown = Skins.findBorderShadownLine(); // Skins.findControlDarkShadown();
		if (shadown == null) {
			shadown = Color.BLACK;
		}
		Color highlight = Skins.findBorderLightLine(); // Skins.findControlHighlight();
		if (highlight == null) {
			highlight = Color.WHITE;
		}
		
		g.setColor(shadown);
		g.drawLine(w - 1, 0, w - 1, h - 1);
		g.drawLine(1, h - 1, w - 1, h - 1);
		g.setColor(highlight);
		g.drawLine(0, 0, w - 2, 0);
		g.drawLine(0, 0, 0, h - 2);

		g.translate(-x, -y);
	}

	public Insets getBorderInsets(Component c) {
		return editorBorderInsets;
	}
	
}