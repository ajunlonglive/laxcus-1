/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.start;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.application.factory.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 提示信息单元。
 * 
 * @author scott.liang
 * @version 1.0 1/28/2021
 * @since laxcus 1.0
 */
class StartFileCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -5328143322180357917L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;
	
	/**
	 * 构造消息显示单元
	 */
	public StartFileCellRenderer() {
		super();
		init();
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		loadTextColor();
		// 图标和文本的间隔尺寸，5个像素
		setIconTextGap(5);
		setOpaque(true); // 这行一定要有，在刷新时使用
	}
	
	/**
	 * 加载文本颜色
	 */
	private void loadTextColor() {
		textSelectForeground = Skins.findListTextSelectForeground();
		textSelectBackground = Skins.findListTextSelectBackground();
		textForeground = Skins.findListForeground();
		textBackground = Skins.findListBackground();
	}


	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if (value == null) {
			setIcon(null);
			setText("");
			return this;
		}
		// 传入对象是StartToken
		if (value.getClass() != StartToken.class) {
			setIcon(null);
			setText("");
			return this;
		}

		// 设置字体
		setFont(list.getFont());

		StartToken token = (StartToken)value;

		// 设置背景和前景色
		if (isSelected) {
			setBackground(textSelectBackground);
			setForeground(textSelectForeground);
		} else {
			/** 
			 * 注意！这个一个JAVA的BUG！
			 * 情况：textBackground和Color.WHITE相等情况下，用setBackground(textBackground)
			 * 而不是setBackground(Color.WHITE)，背景变灰（不正常！！！），setBackground(Color.WHITE)则是显示正常。
			 * 所以采用下面办法避免故障！！！
			 **/
			if (Color.WHITE.equals(textBackground)) {
				setBackground(Color.WHITE);
			} else {
				setBackground(textBackground);
			}
			setForeground(textForeground);
		}
		
		// 图标
		Icon icon = token.getIcon();
		if (icon != null) {
			setIcon(icon);
		}
		// 显示文本
		String text = token.getTitle();
		if (text != null) {
			setText(text);
		}
		// 提示
		text = token.getToolTip();
		if (text != null) {
			setToolTipText(text);
		}
		
		setBorder(new EmptyBorder(4, 4, 4, 4));
		setEnabled(list.isEnabled());

		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		loadTextColor();
		FontKit.setDefaultFont(this);
	}
}