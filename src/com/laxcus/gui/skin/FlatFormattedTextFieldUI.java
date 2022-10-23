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
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;

/**
 * 格式化文本边框UI
 * @author scott.liang
 * @version 1.0 6/16/2022
 * @since laxcus 1.0
 */
public class FlatFormattedTextFieldUI extends BasicFormattedTextFieldUI {

	class TextFieldOutsideBorder extends AbstractBorder implements UIResource {

		private static final long serialVersionUID = 4534225516079955268L;

		/**
		 * 按纽外侧边框
		 */
		public TextFieldOutsideBorder() {
			super();
		}

		/* (non-Javadoc)
		 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
		 */
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			// 不定义边框
			if (FlatUtil.isNotBorder(c)) {
				return;
			}
			
			// 原色
			Color old = g.getColor();
			g.translate(x, y);

			// 边框颜色
			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			g.drawRect(0, 0, w - 1, h - 1);

			// 恢复颜色
			g.translate(-x, -y);
			g.setColor(old);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component)
		 */
		@Override
		public Insets getBorderInsets(Component c) {
			// 不定义边框
			if (FlatUtil.isNotBorder(c)) {
				return new Insets(0, 0, 0, 0);
			} else {
				return new Insets(1, 1, 1, 1);
			}
		}
	}

	class CompoundBorderUIResource extends CompoundBorder implements UIResource {

		private static final long serialVersionUID = 1L;

		public CompoundBorderUIResource(Border outsideBorder, Border insideBorder) {
			super(outsideBorder, insideBorder);
		}
	}

	/**
	 * 
	 */
	public FlatFormattedTextFieldUI() {
		super();
	}

	public static ComponentUI createUI(JComponent c) {
		return new FlatFormattedTextFieldUI();
	}
	
	public void installUI(JComponent c){
		super.installUI(c);

		c.setBorder(new CompoundBorderUIResource(new TextFieldOutsideBorder(), new EmptyBorder(2,2,2,2)));

		//		Border border = c.getBorder();
		//		
		//		boolean success = (border != null && Laxkit.isClassFrom(border, CompoundBorder.class));
		//		if (success) {
		//			c.setBorder(new CompoundBorderUIResource(new TextFieldOutsideBorder(), new EmptyBorder(2,2,2,2)));
		//		}
	}

}