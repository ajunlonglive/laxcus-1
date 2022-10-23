/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
 * 平面菜单项
 * 
 * @author scott.liang
 * @version 1.0 6/29/2022
 * @since laxcus 1.0
 */
public class FlatMenuUI extends BasicMenuUI {

	/**
	 * 
	 */
	public FlatMenuUI() {
		super();
	}
	
	public static ComponentUI createUI(JComponent x) {
		return new FlatMenuUI();
	}

	protected void installDefaults() {
		super.installDefaults();

		if (menuItem != null) {
			menuItem.setBorder(FlatUtil.createMenuBorder());
		}
	}

}