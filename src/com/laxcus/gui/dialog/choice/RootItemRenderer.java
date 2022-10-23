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

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 根单元渲染器
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
class RootItemRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 3273083088398301587L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/**
	 * 构造根单元渲染器
	 */
	public RootItemRenderer(){
		super();
		init();
	}

	private void init() {
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		loadTextColor();
		setIconTextGap(4);
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
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		boolean success = (value != null && Laxkit.isClassFrom(value, RootItem.class));
		if (!success) {
			setIcon(null);
			setText("");
			return this;
		}

		setFont(list.getFont());
		
		RootItem item = (RootItem) value;

		String text = item.getDisplayName();
		if (text == null) {
			text = item.getDescription();
		}

		//		System.out.printf("%s | index:%d, select:%s, focus:%s\n", text, index, isSelected, cellHasFocus);

		// 显示文本
		if (text != null) {
			FontKit.setLabelText(this, text);
		}
		// 显示图标
		Icon icon = item.getIcon();
		if (icon != null) {
			setIcon(icon);
		}

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

		//		// 如果索引是-1，显示在ComboxBox界面上
		//		if (index == -1) {
		//			setBorder(new EmptyBorder(4, 4, 4, 4));
		//		} else {
		//			// top, left, bottom, right
		//			int tab = item.getTab();
		//			int left = (tab * 8) + 4;
		//			// 左侧空置位
		//			setBorder(new EmptyBorder(4, left, 4, 4));
		//		}

		// 如果索引是-1，显示在ComboxBox界面上
		if (index == -1) {
			setBorder(new EmptyBorder(6, 6, 6, 6));
		} else {
			// top, left, bottom, right
			int tab = item.getTab();
			int left = (tab * 8) + 6;
			// 左侧空置位
			setBorder(new EmptyBorder(6, left, 6, 6));
		}

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
		// 更改字体
		FontKit.setDefaultFont(this);
	}
}