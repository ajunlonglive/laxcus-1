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

import com.laxcus.gui.component.*;
import com.laxcus.util.*;

/**
 * 弹出菜单
 * @author scott.liang
 * @version 1.0 6/28/2022
 * @since laxcus 1.0
 */
public class FlatPopupMenuUI extends BasicPopupMenuUI {
	
	class FlatPopupMenuBorder extends AbstractBorder implements UIResource {
		
		private static final long serialVersionUID = -8753071994667474405L;
		
//		protected  Insets borderInsets = new Insets(3, 1, 2, 1);
//		protected Insets borderInsets = new Insets(2, 1, 2, 1);
		
//		Insets borderInsets = new Insets(1, 1, 1, 1);
//
//		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//			Color old = g.getColor();
////			g.translate(x, y);
//
//			g.setColor(MetalLookAndFeel.getControlDarkShadow());
//			g.drawRect(0, 0, w - 1, h - 1);
//
////			g.translate(-x, -y);
//			g.setColor(old);
//		}
		
//		protected Insets borderInsets = new Insets(3, 1, 2, 1);
//
//		public void paintBorder( Component c, Graphics g, int x, int y, int w, int h ) {
//			g.translate( x, y );
//
//			g.setColor( MetalLookAndFeel.getPrimaryControlDarkShadow() );
////			g.setColor(MetalLookAndFeel.getControlDarkShadow());
//			g.drawRect(0, 0, w - 1, h - 1);
//
////			g.setColor( MetalLookAndFeel.getPrimaryControlHighlight() );
//			g.drawLine(1, 1, w - 2, 1);
//			g.drawLine(1, 2, 1, 2);
//			g.drawLine(1, h - 2, 1, h - 2);
//
//			g.translate( -x, -y );
//
//		}
		
//		protected  Insets borderInsets = new Insets( 3, 1, 2, 1 );
//
//		public void paintBorder( Component c, Graphics g, int x, int y, int w, int h ) {
//			g.translate( x, y );
//
//			g.setColor( MetalLookAndFeel.getPrimaryControlDarkShadow() );
//			g.drawRect( 0, 0, w - 1, h - 1 );
//
//			g.setColor( MetalLookAndFeel.getPrimaryControlHighlight() );
//			g.drawLine( 1, 1, w - 2, 1 );
//			g.drawLine( 1, 2, 1, 2 );
//			g.drawLine( 1, h - 2, 1, h - 2 );
//
//			g.translate( -x, -y );
//		}

		protected Insets borderInsets = new Insets(4, 4, 4, 4);

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();
			g.translate(x, y);

			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			g.drawRect(0, 0, w - 1, h - 1);
			
			// 菜单背景色
			Color color = UIManager.getColor("MenuItem.background");
			if (color == null) {
				color = c.getBackground();
			}

			g.setColor( new Color(color.getRGB())); // c.getBackground()); // MetalLookAndFeel.getControl());
			for (int i = 1; i < 4; i++) {
				g.drawRect(x + i, y + i, w - i - i - 1, h - i - i - 1);
			}
			
//			g.setColor(Color.RED); // MetalLookAndFeel.getControl());
//			g.drawRect(1, 1, w - 5, h - 5);
//			g.drawRect(2, 2, w - 4, h - 4);
//			g.drawRect(3, 3, w - 5, h - 5);
			
			// g.drawLine( 1, 2, 1, 2 );
			// g.drawLine( 1, h - 2, 1, h - 2 );

			g.translate(-x, -y);
			g.setColor(old);
		}

		public Insets getBorderInsets(Component c) {
			return borderInsets;
		}

		public Insets getBorderInsets(Component c, Insets newInsets) {
			newInsets.top = borderInsets.top;
			newInsets.left = borderInsets.left;
			newInsets.bottom = borderInsets.bottom;
			newInsets.right = borderInsets.right;
			return newInsets;
		}
	}
	
	public FlatPopupMenuUI() {
		super();
	}

	public static ComponentUI createUI(JComponent x) {
		return new FlatPopupMenuUI();
	}
	
	/**
	 * 判断是ComboBox的弹出菜单
	 * @param c 接口实例
	 * @return 返回真或者假
	 */
	private boolean isComboPopup(JComponent c) {
		if (c != null && Laxkit.isInterfaceFrom(c, ComboPopup.class)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是启动菜单
	 * @param c 接口实例
	 * @return 返回真或者假
	 */
	private boolean isLaunchMenu(JComponent c) {
		if (c != null && Laxkit.isInterfaceFrom(c, LaunchMenu.class)) {
			return true;
		}
		return false;
	}

	public void installDefaults() {
		super.installDefaults();

		if (isComboPopup(popupMenu)) {
			popupMenu.setBorder(new FlatComboBoxUI.ComboPopupBorder());
		} else if (isLaunchMenu(popupMenu)) {
			// 忽略，使用默认的
		} else {
			popupMenu.setBorder(new FlatPopupMenuBorder());
		}

		//		// 不是限制对象，设置边框
		//		if (!isRefuse(popupMenu)) {
		//			popupMenu.setBorder(new FlatPopupMenuBorder());
		//		}

		//		Border b = popupMenu.getBorder();
		//		if (b != null) {
		//			// System.out.printf("border is %s\n", b.getClass().getName());
		//
		//			popupMenu.setBorder(new FlatPopupMenuBorder());
		//		}
	}

}


///**
// * 判断是被限制对象
// * @param c 组件实例
// * @return 被拒绝对象返回真，否则假
// */
//private boolean isRefuse(JComponent c) {
//	if(c == null){
//		return true;
//	}
//	
//	if(Laxkit.isInterfaceFrom(c, ComboPopup.class)){
//		return true;
//	}
//	return false;
//}
