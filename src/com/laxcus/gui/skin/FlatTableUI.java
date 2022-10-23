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
 * 平面表格UI
 * 
 * @author scott.liang
 * @version 1.0 6/21/2022
 * @since laxcus 1.0
 */
public class FlatTableUI extends BasicTableUI {
	
//	public static class FlatScrollPaneBorder extends AbstractBorder implements UIResource {
//
//		private static final long serialVersionUID = -5969913613085217944L;
//
//		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {			
//			Color old = g.getColor();
//			g.translate(x, y);
//
//			g.setColor(MetalLookAndFeel.getControlDarkShadow());
//			g.drawRect(0, 0, w - 1, h - 1);
//
//			g.translate(-x, -y);
//			g.setColor(old);
//		}
//
//		public Insets getBorderInsets(Component c)       {
//			return new Insets(1, 1, 1, 1);
//		}
//	}

//	public static class FlatScrollPaneBorder extends AbstractBorder implements UIResource {
//
//		private static final long serialVersionUID = -5969913613085217944L;
//
//		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {			
//			Color old = g.getColor();
//			g.translate(x, y);
//
//			g.setColor(MetalLookAndFeel.getControlDarkShadow());
////			g.setColor(Color.YELLOW);
//			g.drawRect(0, 0, w - 2, h - 2);
//
//			g.translate(-x, -y);
//			g.setColor(old);
//		}
//
//		public Insets getBorderInsets(Component c)       {
//			return new Insets(1, 1, 2, 2);
//		}
//	}
	
//	public static class ScrollPaneBorder extends AbstractBorder implements UIResource {
//
//		private static final Insets insets = new Insets(1, 1, 2, 2);
//
//		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//
//			JScrollPane scroll = (JScrollPane)c;
//			JComponent colHeader = scroll.getColumnHeader();
//			int colHeaderHeight = 0;
//			if (colHeader != null) colHeaderHeight = colHeader.getHeight();
//
//			JComponent rowHeader = scroll.getRowHeader();
//			int rowHeaderWidth = 0;
//			if (rowHeader != null) rowHeaderWidth = rowHeader.getWidth();
//
//			g.translate( x, y);
//
//			g.setColor( MetalLookAndFeel.getControlDarkShadow() );
//			g.drawRect( 0, 0, w-2, h-2 );
//			g.setColor( MetalLookAndFeel.getControlHighlight() );
//
//			g.drawLine( w-1, 1, w-1, h-1);
//			g.drawLine( 1, h-1, w-1, h-1);
//
//			g.setColor( MetalLookAndFeel.getControl() );
//			g.drawLine( w-2, 2+colHeaderHeight, w-2, 2+colHeaderHeight );
//			g.drawLine( 1+rowHeaderWidth, h-2, 1+rowHeaderWidth, h-2 );
//
//			g.translate( -x, -y);
//
//		}
//
//		public Insets getBorderInsets(Component c)       {
//			return insets;
//		}
//	}
	
	/**
	 * 构造默认的平面表格UI
	 */
	public FlatTableUI() {
		super();
	}

	/**
	 * 生成平面表格UI实例
	 * @param c 句柄
	 * @return 返回平面表格UI实例
	 */
	public static ComponentUI createUI(JComponent c) {
        return new FlatTableUI();
    }
	
//	public void installUI(JComponent c) {
//		super.installUI(c);
//
//		//		table.setBorder(new EmptyBorder(8,8,8,8));
//	}
	
	/**
	 * 更新边框
	 */
	private void updateBorder() {
		// install the scrollpane border
		Container parent = table.getParent(); // should be viewport
		if (parent != null) {
			parent = parent.getParent(); // should be the scrollpane
			if (parent != null && parent instanceof JScrollPane) {
				JScrollPane jsp = (JScrollPane) parent;
				jsp.setBorder(new FlatScrollPaneUI.FlatScrollPaneBorder()); 
			}
		}
	}
	
	protected void installDefaults() {
		super.installDefaults();
		// 更新边框
		updateBorder();
	}

}
