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
 * 滚动面板UI
 * 
 * @author scott.liang
 * @version 1.0 6/7/2022
 * @since laxcus 1.0
 */
public class FlatScrollPaneUI extends MetalScrollPaneUI { // BasicScrollPaneUI {

//	public class OutScrollPaneBorder extends AbstractBorder implements UIResource {
//
//		private static final long serialVersionUID = -5969913613085217944L;
//
//		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {			
//			Color old = g.getColor();
//			g.translate(x, y);
//
////			g.setColor(MetalLookAndFeel.getControlDarkShadow());
//			g.setColor(Color.RED);
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
	
	public static class FlatScrollPaneBorder extends AbstractBorder implements UIResource {

		private static final long serialVersionUID = 5928647966127548282L;

		/**
		 * 按纽外侧边框
		 */
		public FlatScrollPaneBorder() {
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

			g.setColor(MetalLookAndFeel.getControlDarkShadow());
//			g.drawRect(0, 0, w - 1, h - 1);
			g.drawRect(x, y, w - 1, h - 1);

			g.translate(-x, -y);
			// 设置颜色
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
	
	/**
	 * 构造滚动面板UI
	 */
	public FlatScrollPaneUI() {
		super();
	}

	/**
	 * 生成滚动面板UI实例
	 * @param pane 滚动面板UI
	 * @return 返回实例
	 */
	public static ComponentUI createUI(JComponent pane) {
		return new FlatScrollPaneUI();
	}
	
	public void installUI(JComponent c) {
		super.installUI(c);
		
		JScrollPane jsp = (JScrollPane) c;
		jsp.setBorder(new FlatScrollPaneBorder());
		
//		updateScrollbarsFreeStanding();
	}
	
//	  private void updateScrollbarsFreeStanding() {
//	        if (scrollpane == null) {
//	            return;
//	        }
////	        Border border = scrollpane.getBorder();
////	        Object value;
////
////	        if (border instanceof MetalBorders.ScrollPaneBorder) {
////	            value = Boolean.FALSE;
////	        }
////	        else {
////	            value = Boolean.TRUE;
////	        }
//	        
//	        Object value = Boolean.TRUE;
//	        
//	        JScrollBar sb = scrollpane.getHorizontalScrollBar();
//	        if (sb != null) {
//	            sb.putClientProperty
//	                   (MetalScrollBarUI.FREE_STANDING_PROP, value);
//	        }
//	        sb = scrollpane.getVerticalScrollBar();
//	        if (sb != null) {
//	            sb.putClientProperty
//	                   (MetalScrollBarUI.FREE_STANDING_PROP, value);
//	        }
//	    }
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicScrollPaneUI#installDefaults(javax.swing.JScrollPane)
	 */
	@Override
	protected void installDefaults(JScrollPane jsp) {
		super.installDefaults(jsp);

		//		Border b = jsp.getBorder();
		//		System.out.printf("JScrollPane is %s\n", b.getClass().getName());

		// 重新定义
		// 设置这个，不要设置scrollpane.setViewportBorder
//				jsp.setBorder(new FlatScrollPaneBorder());
//				
//				// 边框
//				jsp.setViewportBorder( new FlatScrollPaneBorder());
		
		// ScrollPane Border javax.swing.plaf.metal.MetalBorders$ScrollPaneBorder, ViewportBorder Null
		
		// ScrollPane.viewportBorder
//		Border b1 = jsp.getBorder();
//		Border b2 = jsp.getViewportBorder();
//		
//		System.out.printf("ScrollPane Border %s, Viewport Border %s\n", 
//				(b1 != null ? b1.getClass().getName(): "Null"), (b2 !=null ? b2.getClass().getName() : "Null"));

		jsp.setBorder(new FlatScrollPaneBorder());
		
//		// 边框
//		jsp.setViewportBorder( new EmptyBorder(0,0,0,0));

		
//		// 两种可能，如果包含一个表时，边框以FlatTableUI的定义为准，否则自己的
//		if (isTable(jsp)) {
//			jsp.setBorder(new OutScrollPaneBorder()); // new FlatTableUI.FlatScrollPaneBorder());
//			System.out.println("红色边框!");
//		} else {
//			jsp.setBorder(new FlatScrollPaneBorder());
//		}
	}
	
//	public void paint(Graphics g, JComponent c) {
//		
//	}
	
//	/**
//	 * 判断包含表
//	 * @param jsp
//	 * @return
//	 */
//	private boolean isTable(JScrollPane jsp) {
//		JViewport jv = jsp.getViewport();
//		if (jv != null) {
//			Component sub = jv.getView();
//			if (sub != null && sub instanceof JTable) {
//				// System.out.println("是表格");
//				return true;
//			}
//		}
//		return false;
//	}

}