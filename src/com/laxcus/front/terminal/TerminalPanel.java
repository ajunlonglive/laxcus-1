/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal;


import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.front.terminal.component.*;

/**
 * 中央面板，包括所有！
 * 
 * @author scott.liang
 * @version 1.0 3/22/2020
 * @since laxcus 1.0
 */
class TerminalPanel extends JPanel {

	private static final long serialVersionUID = -4960799707635596004L;
	
	/**
	 * 构造默认的中央面板
	 */
	public TerminalPanel() {
		super();
	}

	/** 分割窗口 **/
	private JSplitPane sub;

	/**
	 * 更新边框颜色
	 */
	private void updateSubBorder() {
		if (sub == null) {
			return;
		}
		// 边框背景色，选择暗色
		Color color = UIManager.getColor("controlDkShadow"); 
		if (color == null) {
			color = getBackground();
		}
		sub.setBorder(new LineBorder(color, 1, true));
	}
	
	/**
	 * 初始化面板
	 * @param rect 窗口范围
	 * @param toolbar
	 * @param left
	 * @param right
	 * @param bottom
	 */
	public void init(Rectangle rect, JToolBar toolbar, TerminalLeftPanel left,
			TerminalRightPanel right, TerminalStatusPanel bottom) {

		// 滚动面板
		sub = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		sub.setContinuousLayout(true);
		sub.setOneTouchExpandable(false);
		sub.setDividerSize(4); // 间隔条用4个像素
		// 设置分割线位置
		Integer pixel = TerminalProperties.readBrowserPaneDeviderLocation();
		if (pixel != null && pixel.intValue() > 0) {
			sub.setDividerLocation(pixel.intValue());
		} else {
//			sub.setResizeWeight(0.1);
//			sub.setDividerLocation( 235  );
			
			// 定义一个默认的宽度
			int width = rect.width / 4;
			if(width < 128) width = 128;
			sub.setDividerLocation(width);
		}
		updateSubBorder();

		// 放置组件到面板
		setLayout(new BorderLayout(0, 0));
		add(toolbar, BorderLayout.PAGE_START);
		add(sub, BorderLayout.CENTER);
		add(bottom, BorderLayout.PAGE_END);
//		setBorder(new EmptyBorder(0, 0, 0, 0));
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		updateSubBorder();
		super.updateUI();
	}

}