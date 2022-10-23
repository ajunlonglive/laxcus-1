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

/**
 *
 * @author scott.liang
 * @version 1.0 2022-6-21
 * @since laxcus 1.0
 */
public class FlatCellRendererPane extends CellRendererPane {

	private static final long serialVersionUID = 890840055621254144L;

	public class CellRendererBorder extends AbstractBorder implements UIResource {

		private static final long serialVersionUID = -5969913613085217944L;

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {			
			Color old = g.getColor();
			g.translate(x, y);

			g.setColor(MetalLookAndFeel.getControlDarkShadow());
//			g.setColor(Color.YELLOW);
			g.drawRect(0, 0, w - 1, h - 1);

			g.translate(-x, -y);
			g.setColor(old);
		}

		public Insets getBorderInsets(Component c)       {
			return new Insets(1, 1, 1, 1);
		}
	}
	
	/**
	 * 
	 */
	public FlatCellRendererPane() {
		super();
	}
	
//	private void printIndex( Component c ) {
//		int index = -1;
//		int count = this.getComponentCount();
//		for(int i =0; i < count; i++) {
//			Component sub =	this.getComponent(i);
//			this.getComponentZOrder(arg0)
//		}
//	}

	public void paintComponent(Graphics g, Component c, Container p, int x, int y, int w, int h, boolean shouldValidate) {
		if (c == null) {
			if (p != null) {
				Color oldColor = g.getColor();
				g.setColor(p.getBackground());
				g.fillRect(x, y, w, h);
				g.setColor(oldColor);
			}
			return;
		}
		
		// 设置边框
		if (c != null && c instanceof JComponent) {
//			// 这个组件的下标位置
//			int index = getComponentZOrder(c);
//			int count = this.getComponentCount();
//			System.out.printf("Index is %d - %d\n",  index, count );
			
//			((JComponent) c).setBorder(new LineBorder(Color.RED, 1));
//			((JComponent) c).setBorder(new CellRendererBorder());
		}
		
//		System.out.printf("Table Header Component is %s\n", c.getClass().getName());

		if (c.getParent() != this) {
			this.add(c);
		}

		c.setBounds(x, y, w, h);

		if (shouldValidate) {
			c.validate();
		}

		boolean wasDoubleBuffered = false;
		if ((c instanceof JComponent) && ((JComponent)c).isDoubleBuffered()) {
			wasDoubleBuffered = true;
			((JComponent)c).setDoubleBuffered(false);
		}

		Graphics cg = g.create(x, y, w, h);
		try {
			c.paint(cg);
		} finally {
			cg.dispose();
		}

		if (wasDoubleBuffered && (c instanceof JComponent)) {
			((JComponent) c).setDoubleBuffered(true);
		}

		c.setBounds(-w, -h, 0, 0);
	}
}
