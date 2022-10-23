/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.choice;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.gui.dialog.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 根单元渲染器
 * 
 * @author scott.liang
 * @version 1.0 9/3/2021
 * @since laxcus 1.0
 */
class FileFilterRenderer extends JLabel implements ListCellRenderer {
	
	private static final long serialVersionUID = 3273083088398301587L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/**
	 * 构造根单元渲染器
	 */
	public FileFilterRenderer(){
		super();
		init();
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		setIconTextGap(4);
		loadTextColor();
		setOpaque(true);
	}
	
	/**
	 * 加载颜色
	 */
	private void loadTextColor() {
		textSelectForeground = Skins.findListTextSelectForeground();
		textSelectBackground = Skins.findListTextSelectBackground();
		textForeground = Skins.findListForeground();
		textBackground = Skins.findListBackground();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		loadTextColor();
		// 更改字体
		FontKit.setDefaultFont(this);
	}
	
	/*
	 * (non-Javadoc)
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
		else if (!(value instanceof FileMatcher)) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());

		// 文本...
		FileMatcher ff = (FileMatcher) value;
		String des = ff.getDescription();
		FontKit.setLabelText(this, des);
		
		// 颜色
		Color foreground = textForeground;
		
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
			setForeground(foreground);
		}
		
		// top, left, bottom, right
//		setBorder(new EmptyBorder(4, 4, 4, 4));
		
		if (index < 0) {
			setBorder(new EmptyBorder(6, 1, 6, 6));
		} else {
			setBorder(new EmptyBorder(6, 6, 6, 6));
		}
		
//		setEnabled(list.isEnabled());
//		setOpaque(true);
		
		return this;
	}

}