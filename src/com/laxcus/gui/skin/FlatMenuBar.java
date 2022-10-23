/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import java.awt.*;

import javax.swing.*;

import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 菜单条 <br>
 * 
 * 在底部显示一条横线
 * 
 * @author scott.liang
 * @version 1.0 1/12/2022
 * @since laxcus 1.0
 */
public class FlatMenuBar extends JMenuBar {

	private static final long serialVersionUID = 6460189174353310327L;

	/**
	 * 构造菜单条
	 */
	public FlatMenuBar() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
		Color old = g.getColor();
		int width = getWidth();
		int y = getHeight() - 1;

		if (Skins.isGraySkin()) {
			Color c = Color.GRAY;
			g.setColor(c);
			g.drawLine(0, y, width, y);
		} else {
			Color c = UIManager.getColor("MenuBar.background");
			if (c == null) {
				c = getBackground();
			}
			ESL esl = new ESL(c);
			esl.brighter(50);
			c = esl.toColor();
			g.setColor(c);
			g.drawLine(0, y, width, y);
		}
		g.setColor(old);
	}

}