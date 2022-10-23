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
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 平面菜单栏UI
 * 
 * @author scott.liang
 * @version 1.0 7/2/2022
 * @since laxcus 1.0
 */
public class FlatMenuBarUI extends MetalMenuBarUI {

	class FlatMenuBarBorder extends AbstractBorder implements UIResource {

		private static final long serialVersionUID = 1938986431730405247L;

		/**
		 * 按纽外侧边框
		 */
		public FlatMenuBarBorder() {
			super();
		}

		/* (non-Javadoc)
		 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
		 */
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			// 不绘制底线
			if (!isDrawBottomLine(c)) {
				return;
			}
			
			Color old = g.getColor();

			Color color = Color.GRAY;
			// 不是亮色
			if (!Skins.isGraySkin()) {
				color = UIManager.getColor("MenuBar.background");
				if (color == null) {
					color = c.getBackground();
				}
				ESL esl = new ESL(color);
				esl.brighter(50);
				color = esl.toColor();
			}
			
			g.setColor(color);
			g.drawLine(0, h - 1, w, h - 1);
			// 恢复
			g.setColor(old);
		}
		
		private boolean isDrawBottomLine(Component c) {
			return FlatUtil.isDrawBottomLine(c, true);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component)
		 */
		@Override
		public Insets getBorderInsets(Component c) {
			// 不定义边框
			if (isDrawBottomLine(c)) {
				return new Insets(2, 4, 3, 4);
			} else {
				return new Insets(2, 4, 2, 4);
			}
		}
	}

	/**
	 * 构造默认的平面菜单栏UI
	 */
	public FlatMenuBarUI() {
		super();
	}

	public static ComponentUI createUI(JComponent c) {
		return new FlatMenuBarUI();
	}
	
	public void installUI(JComponent c){
		super.installUI(c);

		c.setBorder(new FlatMenuBarBorder());
	}

}