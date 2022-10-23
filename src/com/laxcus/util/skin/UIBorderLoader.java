/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.skin;

import javax.swing.*;

import com.laxcus.gui.skin.*;

/**
 * UI组件边框加载器
 * 
 * @author scott.liang
 * @version 1.0 7/2/2022
 * @since laxcus 1.0
 */
public class UIBorderLoader {

	/**
	 * 加载更新默认的边框
	 */
	public static void loadDefault() {
		UIDefaults def = UIManager.getDefaults();
		// 设置组件
		def.put("Table.scrollPaneBorder",
				new FlatScrollPaneUI.FlatScrollPaneBorder());
	}

}