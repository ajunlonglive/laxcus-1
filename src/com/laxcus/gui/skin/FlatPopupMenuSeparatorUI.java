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
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

import com.laxcus.util.skin.*;

/**
 * 菜单分隔符UI
 * 
 * @author scott.liang
 * @version 1.0 6/29/2022
 * @since laxcus 1.0
 */
public class FlatPopupMenuSeparatorUI extends BasicPopupMenuSeparatorUI {

	/**
	 * 构造默认的菜单分隔符UI
	 */
	public FlatPopupMenuSeparatorUI() {
		super();
	}

	public static ComponentUI createUI(JComponent c) {
		return new FlatPopupMenuSeparatorUI();
	}

	public Dimension getPreferredSize(JComponent c) {
		// 默认高度是6个像素
		return new Dimension(0, 6);
	}
	
	private Color getBackground(JComponent c) {
		Color color = UIManager.getColor("PopupMenuSeparator.background");
		if (color == null) {
			color = c.getBackground();
		}
		return new Color(color.getRGB());
	}

	private Color getForeground(JComponent c) {
		Color color = UIManager.getColor("PopupMenuSeparator.foreground");
		if (color == null) {
			color = c.getForeground();
		}
		return new Color(color.getRGB());
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicPopupMenuSeparatorUI#paint(java.awt.Graphics, javax.swing.JComponent)
	 */
	@Override
	public void paint(Graphics g, JComponent c) {
		Dimension d = c.getSize();
		
		int y = d.height / 2 - 1;
		if (y < 0) y = 0;
		
		Color background = getBackground(c);
		Color foreground = getForeground(c);

		if (Skins.isGraySkin()) {
			g.setColor(foreground);
			g.drawLine(0, y, d.width, y);

			g.setColor(background);
			g.drawLine(0, y + 1, d.width, y + 1);
		} else {
			g.setColor(background);
			g.drawLine(0, y, d.width, y);

			g.setColor(foreground);
			g.drawLine(0, y + 1, d.width, y + 1);
		}
	}
	
}


//protected void installDefaults(JSeparator s) {
//	super.installDefaults(s);
//
//	// 定义最小尺寸
//	if (s.getOrientation() == JSeparator.HORIZONTAL) {
//		Dimension d = s.getSize();
//		if (d.height < 6) {
//			d.height = 6;
//			s.setSize(d);
//		}
//		System.out.printf("menu seprator w:%d, h:%d\n", d.width, d.height);
//	}
//}


///*
// * (non-Javadoc)
// * @see javax.swing.plaf.basic.BasicPopupMenuSeparatorUI#paint(java.awt.Graphics, javax.swing.JComponent)
// */
//@Override
//public void paint(Graphics g, JComponent c) {
//	Dimension d = c.getSize();
//	
//	System.out.printf("darw menu seprator w:%d, h:%d\n", d.width, d.height);
//	
//	
//	if (Skins.isGraySkin()) {
//		g.setColor(c.getForeground());
//		g.drawLine(0, 0, d.width, 0);
//
//		g.setColor(c.getBackground());
//		g.drawLine(0, 1, d.width, 1);
//	} else {
//		g.setColor(c.getBackground());
//		g.drawLine(0, 0, d.width, 0);
//
//		g.setColor(c.getForeground());
//		g.drawLine(0, 1, d.width, 1);
//	}
//}
