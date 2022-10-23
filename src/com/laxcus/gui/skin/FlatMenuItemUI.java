/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import javax.swing.*;
import javax.swing.plaf.basic.*;

/**
 * 平面菜单项UI
 * 
 * @author scott.liang
 * @version 1.0 2022-6-28
 * @since laxcus 1.0
 */
public class FlatMenuItemUI extends BasicMenuItemUI {

//	class FlatMenuItemBorder extends AbstractBorder implements UIResource {
//
//		private static final long serialVersionUID = -7574496474708393995L;
//
//		// protected Insets borderInsets = new Insets(2, 2, 2, 2);
//
////		protected Insets borderInsets = new Insets(2, 2, 2, 2);
//		
//		protected Insets borderInsets = new Insets(2,0,2,0);
//		
//		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
////			Color old = g.getColor();
////			g.translate(x, y);
////			
////			g.setColor(Color.red); // c.getBackground()); // MetalLookAndFeel.getControl());
////			g.drawRect(0, 0, w-1, h-1);
////			
////			g.translate(-x, -y);
////			g.setColor(old);
//		}
//
//		public void paintBorderX(Component c, Graphics g, int x, int y, int w, int h) {
//			JMenuItem b = (JMenuItem) c;
//			ButtonModel model = b.getModel();
//			
//			Color old = g.getColor();
//			g.translate(x, y);
//
//			if (c.getParent() instanceof JMenuBar) {
//				if (model.isArmed() || model.isSelected()) {
//					g.setColor(MetalLookAndFeel.getControlDarkShadow());
//					g.drawLine(0, 0, w - 2, 0);
//					g.drawLine(0, 0, 0, h - 1);
//					g.drawLine(w - 2, 2, w - 2, h - 1);
//
//					g.setColor(MetalLookAndFeel.getPrimaryControlHighlight());
//					g.drawLine(w - 1, 1, w - 1, h - 1);
//
//					g.setColor(MetalLookAndFeel.getMenuBackground());
//					g.drawLine(w - 1, 0, w - 1, 0);
//				}
//			} else {
//				if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
//					g.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
//					g.drawLine(0, 0, w - 1, 0);
//
//					g.setColor(MetalLookAndFeel.getPrimaryControlHighlight());
//					g.drawLine(0, h - 1, w - 1, h - 1);
//				} else {
//					g.setColor(MetalLookAndFeel.getPrimaryControlHighlight());
//					g.drawLine(0, 0, 0, h - 1);
//				}
//			}
//
//			g.translate(-x, -y);
//			g.setColor(old);
//		}
//
////		protected Insets borderInsets = new Insets(1,1,1,1);
////
////		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//////			JMenuItem b = (JMenuItem) c;
//////			ButtonModel model = b.getModel();
////			
////			Color old = g.getColor();
//////			g.translate(x, y);
////
////			g.setColor(MetalLookAndFeel.getControl());
////			g.drawRect(0, 0, w - 1, h - 1);
////
//////			if (c.getParent() instanceof JMenuBar) {
//////				if (model.isArmed() || model.isSelected()) {
//////					g.setColor(MetalLookAndFeel.getControlDarkShadow());
//////					g.drawLine(0, 0, w - 2, 0);
//////					g.drawLine(0, 0, 0, h - 1);
//////					g.drawLine(w - 2, 2, w - 2, h - 1);
//////
//////					g.setColor(MetalLookAndFeel.getPrimaryControlHighlight());
//////					g.drawLine(w - 1, 1, w - 1, h - 1);
//////
//////					g.setColor(MetalLookAndFeel.getMenuBackground());
//////					g.drawLine(w - 1, 0, w - 1, 0);
//////				}
//////			} else {
//////				if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
//////					g.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
//////					g.drawLine(0, 0, w - 1, 0);
//////
//////					g.setColor(MetalLookAndFeel.getPrimaryControlHighlight());
//////					g.drawLine(0, h - 1, w - 1, h - 1);
//////				} else {
//////					g.setColor(MetalLookAndFeel.getPrimaryControlHighlight());
//////					g.drawLine(0, 0, 0, h - 1);
//////				}
//////			}
////
//////			g.translate(-x, -y);
////			g.setColor(old);
////		}
//		
//		public Insets getBorderInsets(Component c) {
//			return borderInsets;
//		}
//
//		public Insets getBorderInsets(Component c, Insets newInsets) {
//			newInsets.top = borderInsets.top;
//			newInsets.left = borderInsets.left;
//			newInsets.bottom = borderInsets.bottom;
//			newInsets.right = borderInsets.right;
//			return newInsets;
//		}
//	}

	/**
	 * 
	 */
	public FlatMenuItemUI() {
		super();
	}

	public static FlatMenuItemUI createUI(JComponent x) {
		return new FlatMenuItemUI();
	}

	public void installDefaults() {
		super.installDefaults();

		if (menuItem != null) {
			menuItem.setBorder(FlatUtil.createMenuItemBorder());
		}
	}

}
