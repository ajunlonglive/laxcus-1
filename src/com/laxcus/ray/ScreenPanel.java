/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import java.awt.*;

import javax.swing.*;

import com.laxcus.ray.status.*;

/**
 * 图形桌面面板
 * 
 * @author scott.liang
 * @version 1.0 3/22/2020
 * @since laxcus 1.0
 */
class ScreenPanel extends JPanel {

	private static final long serialVersionUID = -6632994997917755326L;

	/**
	 * 构造默认的图形桌面面板
	 */
	public ScreenPanel() {
		super();
	}
	
	/**
	 * 初始化参数
	 * @param pane
	 * @param bottom
	 */
	public void init(JDesktopPane pane, RayStatusBar bottom) {
		// 放置组件到面板
		setLayout(new BorderLayout(0, 0));
		add(pane, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
	}

}