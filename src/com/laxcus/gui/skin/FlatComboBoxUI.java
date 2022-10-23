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

import com.laxcus.util.*;

/**
 * 复选框UI
 * 
 * @author scott.liang
 * @version 1.0 6/6/2022
 * @since laxcus 1.0
 */
public class FlatComboBoxUI extends MetalComboBoxUI {

	public static class ComboPopupBorder extends AbstractBorder implements UIResource {

		private static final long serialVersionUID = 1L;
		
		public ComboPopupBorder() {
			super();
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();
			g.translate(x, y);
			
			// 控件颜色
			g.setColor(MetalLookAndFeel.getControl());
			g.drawLine(0, 0, w - 1, 0);

			// 边框颜色，X=0, Y=1
			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			g.drawRect(0, 1, w - 1, h - 2);

			g.translate(-x, -y);
			g.setColor(old);
		}
		
		public Insets getBorderInsets(Component c) {
			// TOP两个像素，其它位置1个
			return new Insets(2, 1, 1, 1);
		}
	}
	
	class ComboBoxButtonBorder extends AbstractBorder  {

		private static final long serialVersionUID = 1L;

		/**
		 * 按纽外侧边框
		 */
		public ComboBoxButtonBorder() {
			super();
		}

		/* (non-Javadoc)
		 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
		 */
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
			Color old = g.getColor();
			
			g.setColor(MetalLookAndFeel.getControlDarkShadow());
			g.drawRect(0, 0, w - 1, h - 1);
	        
			g.setColor(old);
		}
		
		public Insets getBorderInsets(Component c) {
			return new Insets(3, 3, 3, 3);
		}
	}
	
	/**
	 * 构造复选框UI
	 */
	public FlatComboBoxUI() {
		super();
	}

	/**
	 * 生成UI
	 * @param c
	 * @return 返回实例
	 */
	public static ComponentUI createUI(JComponent c) {
		return new FlatComboBoxUI();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalComboBoxUI#createEditor()
	 */
	@Override
	protected ComboBoxEditor createEditor() {
		return new FlatComboBoxEditor();//.UIResource();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.metal.MetalComboBoxUI#createPopup()
	 */
	@Override
	protected ComboPopup createPopup() {
		ComboPopup cp = super.createPopup();
		
		if (cp != null && Laxkit.isClassFrom(cp, JComponent.class)) {
			JComponent jc = (JComponent) cp;
			jc.setBorder(new ComboPopupBorder());
		}
		
		return cp;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicComboBoxUI#installComponents()
	 */
	@Override
	protected void installComponents() {
		super.installComponents();

		// outside javax.swing.plaf.metal.MetalBorders$ButtonBorder@1036651
		// inside javax.swing.plaf.basic.BasicBorders$MarginBorder@327236

		arrowButton.setBorder(new CompoundBorder(new ComboBoxButtonBorder(), new EmptyBorder(1, 1, 1, 3)));
	}

}