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
 *
 * @author scott.liang
 * @version 1.0 2/9/2022
 * @since laxcus 1.0
 */
public class FlatRadioButtonMenuItemUI extends BasicRadioButtonMenuItemUI {

	public static ComponentUI createUI(JComponent c) {
		return new FlatRadioButtonMenuItemUI();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicMenuItemUI#installDefaults()
	 */
	@Override
	protected void installDefaults() {
		super.installDefaults();
		checkIcon = UIManager.getIcon("RadioButtonMenuItem.SelectedIcon");
		
		menuItem.setBorder(FlatUtil.createMenuItemBorder());
		
//		arrowIcon = UIManager.getIcon("MetalMinimizeIcon");
//		checkIcon = null;
//		arrowIcon = null;
	}

	protected void paintMenuItem(Graphics g, JComponent c,
            Icon checkIcon, Icon arrowIcon,
            Color background, Color foreground, int defaultTextIconGap) {
	
		boolean success = Laxkit.isClassFrom(c, JRadioButtonMenuItem.class);
		if(success) {
			JRadioButtonMenuItem mi = (JRadioButtonMenuItem)c;
			boolean on = mi.isSelected();
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