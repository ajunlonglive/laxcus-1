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
 * 文本边框UI
 * @author scott.liang
 * @version 1.0 6/6/2022
 * @since laxcus 1.0
 */
public class FlatPasswordFieldUI extends BasicPasswordFieldUI {

	class PasswordFieldOutsideBorder extends AbstractBorder implements UIResource {

		private static final long serialVersionUID = 6035639325599384067L;

		/**
		 * 按纽外侧边框
		 */
		public PasswordFieldOutsideBorder() {
			super();
		}

		/* (non-Javadoc)
		 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
		 */
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			// 原色
			Color old = g.getColor();

			// 边框颜色
			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			g.drawRect(0, 0, width - 1, height - 1);

			// 设置颜色
			g.setColor(old);
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
	public FlatPasswordFieldUI() {
		super();
	}

	public static ComponentUI createUI(JComponent c) {
		return new FlatPasswordFieldUI();
	}

	public void installUI(JComponent c){
		super.installUI(c);
		c.setBorder(new CompoundBorderUIResource(new PasswordFieldOutsideBorder(), new EmptyBorder(2,2,2,2)));

		//		Border border = c.getBorder();
		//
		//		boolean success = (border != null && Laxkit.isClassFrom(border, CompoundBorder.class));
		//		if (success) {
		//			c.setBorder(new CompoundBorderUIResource(new FlatTextFieldOutsideBorder(), new EmptyBorder(2,2,2,2)));
		//		}
	}

}