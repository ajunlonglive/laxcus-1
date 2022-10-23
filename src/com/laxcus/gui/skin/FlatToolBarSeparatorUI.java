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

/**
 * 工具栏分隔符
 * 
 * @author scott.liang
 * @version 1.0 6/29/2022
 * @since laxcus 1.0
 */
public class FlatToolBarSeparatorUI extends BasicToolBarSeparatorUI {

	/**
	 * 构造默认的工具栏分隔符
	 */
	public FlatToolBarSeparatorUI() {
		super();
	}

	public static ComponentUI createUI(JComponent c) {
		return new FlatToolBarSeparatorUI();
	}

	public Dimension getPreferredSize(JComponent c) {
		return new FlatSeparatorUI().getPreferredSize(c);
	}

	public void paint(Graphics g, JComponent c) {
		new FlatSeparatorUI().paint(g, c);
	}

}


//	protected void installDefaults(JSeparator c) {
//		super.installDefaults(c);
//		// System.out.println("构造工具栏分隔符UI");
//		
////		// 默认的宽度
////		Dimension d = getPreferredSize(c);
////		c.setPreferredSize(d);
//	}

//	public Dimension getPreferredSize(JComponent c) {
//		if (c != null && c instanceof JSeparator) {
//			JSeparator js = (JSeparator) c;
//			Container parent = js.getParent();
//			int width = (parent != null ? parent.getWidth() : 32);
//			int height = (parent != null ? parent.getHeight() : 32);
//
//			if (js.getOrientation() == JSeparator.HORIZONTAL) {
//				return new Dimension(width, 6); // 水平状态，宽度不限，高度6个像素
//			} else if (js.getOrientation() == JSeparator.VERTICAL) {
//				return new Dimension(6, height); // 垂直状态，宽度6个像素，高度不限
//			}
//		}
//		return super.getPreferredSize(c);
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.plaf.metal.MetalSeparatorUI#paint(java.awt.Graphics, javax.swing.JComponent)
//	 */
//	@Override
//	public void paint(Graphics g, JComponent c) {
////		// 如果是来自工具栏，绘制它
////		if (isToolBar(c)) {
////			FlatToolBarSeparatorUI.draw(g, c);
////			return;
////		}
//		
//		
//		// 来自其他组件的分隔符
//		Dimension d = c.getSize();
//		
//		System.out.printf("FlatToolBarSeparatorUI.paint, paint %s, w:%d,h:%d\n", c.getClass().getName(), d.width, d.height);
//
//		if (((JSeparator) c).getOrientation() == JSeparator.VERTICAL) {
//			int x = d.width / 2 - 1;
//			if (x < 0) x = 0;
//
//			g.setColor(c.getForeground());
//			g.drawLine(x, 0, x, d.height);
//
//			g.setColor(c.getBackground());
//			g.drawLine(x + 1, 0, x + 1, d.height);
//		}
//		// HORIZONTAL
//		else {
//			int y = d.height / 2 - 1;
//			if (y < 0) y = 0;
//
//			g.setColor(c.getForeground());
//			g.drawLine(0, y, d.width, y);
//
//			g.setColor(c.getBackground());
//			g.drawLine(0, y + 1, d.width, y + 1);
//		}
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.plaf.metal.MetalSeparatorUI#paint(java.awt.Graphics, javax.swing.JComponent)
//	 */
//	@Override
//	public void paint(Graphics g, JComponent c) {
//		FlatToolBarSeparatorUI.draw(g, c);
//	}

///**
// * 绘制分隔符
// * @param g
// * @param c
// */
//public void draw(Graphics g, JComponent c) {
//	Dimension d = c.getSize();
//	// 灰色
//	boolean gray = Skins.isGraySkin();
//	//		System.out.printf("FlatToolBarSeparatorUI.draw, paint %s\n", c.getClass().getName());
//
//	if (((JSeparator) c).getOrientation() == JSeparator.VERTICAL) {
//		int x = d.width / 2 - 1;
//		if (x < 0) x = 0;
//
//		if (gray) {
//			g.setColor(c.getForeground());
//			g.drawLine(x, 0, x, d.height);
//
//			g.setColor(c.getBackground());
//			g.drawLine(x + 1, 0, x + 1, d.height);
//		} else {
//			g.setColor(c.getBackground());
//			g.drawLine(x, 0, x, d.height);
//			g.setColor(c.getForeground());
//			g.drawLine(x + 1, 0, x + 1, d.height);
//		}
//	}
//	// HORIZONTAL
//	else {
//		int y = d.height / 2 - 1;
//		if (y < 0) y = 0;
//
//		if (gray) {
//			g.setColor(c.getForeground());
//			g.drawLine(0, y, d.width, y);
//
//			g.setColor(c.getBackground());
//			g.drawLine(0, y + 1, d.width, y + 1);
//		} else {
//			g.setColor(c.getBackground());
//			g.drawLine(0, y, d.width, y);
//
//			g.setColor(c.getForeground());
//			g.drawLine(0, y + 1, d.width, y + 1);
//		}
//	}
//}
