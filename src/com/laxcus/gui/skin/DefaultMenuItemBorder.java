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
import javax.swing.plaf.*;

/**
 * 默认的菜单边框，包括JMenu、JMenuItem
 * 
 * @author scott.liang
 * @version 1.0 6/29/2022
 * @since laxcus 1.0
 */
public class DefaultMenuItemBorder extends AbstractBorder implements UIResource {

	private static final long serialVersionUID = -9050478362121566980L;
	
//	private Insets insets = new Insets(2,0,2,0);
	
	private Insets insets;
	
	/**
	 * 构造LAXCUS桌面的菜单边框
	 * @param s
	 */
	public DefaultMenuItemBorder(Insets s) {
		super();
		insets = s;
	}

	/**
	 * 构造LAXCUS桌面的默认菜单边框
	 */
	public DefaultMenuItemBorder() {
		this(new Insets(3, 0, 3, 0));
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		// 不用绘制
	}
	
	public Insets getBorderInsets(Component c) {
		return insets;
	}

	public Insets getBorderInsets(Component c, Insets newInsets) {
		newInsets.top = insets.top;
		newInsets.left = insets.left;
		newInsets.bottom = insets.bottom;
		newInsets.right = insets.right;
		return newInsets;
	}
}
