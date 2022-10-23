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

import com.laxcus.util.*;

/**
 * 选择菜单项
 * 
 * @author scott.liang
 * @version 1.0 2/9/2022
 * @since laxcus 1.0
 */
public class FlatCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {

	public static ComponentUI createUI(JComponent c) {
		return new FlatCheckBoxMenuItemUI();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicMenuItemUI#installDefaults()
	 */
	@Override
	protected void installDefaults() {
		super.installDefaults();
		checkIcon = UIManager.getIcon("CheckBoxMenuItem.SelectedIcon");
		
		menuItem.setBorder(FlatUtil.createMenuItemBorder());
		
//		arrowIcon = UIManager.getIcon("MetalMinimizeIcon");
//		checkIcon = null;
//		arrowIcon = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicMenuItemUI#paintMenuItem(java.awt.Graphics, javax.swing.JComponent, javax.swing.Icon, javax.swing.Icon, java.awt.Color, java.awt.Color, int)
	 */
	@Override
	protected void paintMenuItem(Graphics g, JComponent c,
            Icon checkIcon, Icon arrowIcon,
            Color background, Color foreground, int defaultTextIconGap) {
		
		// 判断
		boolean success = Laxkit.isClassFrom(c, JCheckBoxMenuItem.class);
		if (success) {
			JCheckBoxMenuItem mi = (JCheckBoxMenuItem) c;
			boolean on = mi.getState();
			if (on) {
				super.paintMenuItem(g, c, this.checkIcon, null, background,
						foreground, defaultTextIconGap);
			} else {
				super.paintMenuItem(g, c, null, null, background, foreground,
						defaultTextIconGap);
			}
		} else {
			super.paintMenuItem(g, c, checkIcon, arrowIcon, background, foreground,
					defaultTextIconGap);
		}

	}

}