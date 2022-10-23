/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import java.awt.*;

import javax.swing.border.*;
import javax.swing.plaf.metal.*;
import javax.swing.plaf.*;

/**
 * 编辑器
 * 
 * @author scott.liang
 * @version 1.0 6/6/2022
 * @since laxcus 1.0
 */
public class FlatComboBoxEditor extends MetalComboBoxEditor implements UIResource {

	class FlatEditorBorder extends AbstractBorder { // implements javax.swing.plaf.UIResource {

		private static final long serialVersionUID = 1L;

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();
			g.translate(x, y);

			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			g.drawLine(0, 0, w - 1, 0);
			g.drawLine(0, 0, 0, h - 1);
			g.drawLine(0, h - 1, w - 1, h - 1);

			g.translate(-x, -y);
			g.setColor(old);
		}
		
		public Insets getBorderInsets(Component c) {
			return new Insets(2, 2, 2, 0);
		}
	}
    
	/**
	 * 
	 */
	public FlatComboBoxEditor() {
		super();
		resetBorder();
	}

	/**
	 * 修改边框
	 */
	private void resetBorder() {
		//		editor.setBorder(new CompoundBorder(new ComboBoxTextFieldBorder(), new EmptyBorder(2, 2, 2, 2)));

		editor.setBorder(new FlatEditorBorder());
	}

//	public static class UIResource extends FlatComboBoxEditor implements javax.swing.plaf.UIResource {
//
//	}
	
}


//	protected static Insets editorBorderInsets = new Insets( 2, 2, 2, 0 );
//    private static final Insets SAFE_EDITOR_BORDER_INSETS = new Insets( 2, 2, 2, 0 );


//	class EditorBorder extends AbstractBorder {
//        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//            g.translate( x, y );
//
//            if (MetalLookAndFeel.usingOcean()) {
//                g.setColor(MetalLookAndFeel.getControlDarkShadow());
//                g.drawRect(0, 0, w, h - 1);
//                g.setColor(MetalLookAndFeel.getControlShadow());
//                g.drawRect(1, 1, w - 2, h - 3);
//            }
//            else {
//                g.setColor( MetalLookAndFeel.getControlDarkShadow() );
//                g.drawLine( 0, 0, w-1, 0 );
//                g.drawLine( 0, 0, 0, h-2 );
//                g.drawLine( 0, h-2, w-1, h-2 );
//                g.setColor( MetalLookAndFeel.getControlHighlight() );
//                g.drawLine( 1, 1, w-1, 1 );
//                g.drawLine( 1, 1, 1, h-1 );
//                g.drawLine( 1, h-1, w-1, h-1 );
//                g.setColor( MetalLookAndFeel.getControl() );
//                g.drawLine( 1, h-2, 1, h-2 );
//            }
//
//            g.translate( -x, -y );
//        }
//
//        public Insets getBorderInsets( Component c ) {
//            if (System.getSecurityManager() != null) {
//                return SAFE_EDITOR_BORDER_INSETS;
//            } else {
//                return editorBorderInsets;
//            }
//        }
//    }


//class ComboBoxTextFieldBorder extends AbstractBorder  {
//
//	private static final long serialVersionUID = 1L;
//
//	/**
//	 * 按纽外侧边框
//	 */
//	public ComboBoxTextFieldBorder() {
//		super();
//	}
//
//	/* (non-Javadoc)
//	 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
//	 */
//	@Override
//	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//		// 原色
//		Color old = g.getColor();
//
//		// 背景色
//		g.setColor(MetalLookAndFeel.getControlDarkShadow());
//		g.drawLine(0, 0, w - 1, 0); // 上
//		g.drawLine(0, 0, 0, h - 1); // 左
//		g.drawLine(0, h - 1, w - 1, h - 1); // 下
//
//		// 设置颜色
//		g.setColor(old);
//	}
//
//	public Insets getBorderInsets(Component c) {
//		if (System.getSecurityManager() != null) {
//			return new Insets(2, 2, 2, 0);
//		} else {
//			return new Insets(2, 2, 2, 0);
//		}
//	}
//}

//class ComboBoxTextFieldBorder extends AbstractBorder  {
//
//	private static final long serialVersionUID = 1L;
//
//	/**
//	 * 按纽外侧边框
//	 */
//	public ComboBoxTextFieldBorder() {
//		super();
//	}
//
//	/* (non-Javadoc)
//	 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
//	 */
//	@Override
//	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//		// 原色
//		Color old = g.getColor();
//
//		// 背景色
//		g.setColor(MetalLookAndFeel.getControlDarkShadow());
//		g.drawLine(0, 0, w - 1, 0); // 上
//		g.drawLine(0, 0, 0, h - 1); // 左
//		g.drawLine(0, h - 1, w - 1, h - 1); // 下
//
//		// 设置颜色
//		g.setColor(old);
//	}
//
//	public Insets getBorderInsets(Component c) {
//		if (System.getSecurityManager() != null) {
//			return new Insets(2, 2, 2, 1);
//		} else {
//			return new Insets(2, 2, 2, 1);
//		}
//	}
//}

