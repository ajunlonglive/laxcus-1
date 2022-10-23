/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.properties;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 主题单元
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
class ThemeCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 3273083088398301587L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;


	public ThemeCellRenderer(){
		super();
		init();
	}

	private void init() {
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		loadTextColor();
		setIconTextGap(6);
		// 让组件不透明，这样上面的颜色才能显示出来
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
		FontKit.setDefaultFont(this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		//
		//		if (value == null) {
		//			setIcon(null);
		//			setText("");
		//			return this;
		//		}
		//		if (value.getClass() != ThemeItem.class) {
		//			setIcon(null);
		//			setText("");
		//			return this;
		//		}

		// 判断
		boolean success = (value != null && value.getClass() == ThemeItem.class);
		if (!success) {
			setIcon(null);
			setText("");
			return this;
		}

		setFont(list.getFont());

		ThemeItem item = (ThemeItem)value;
		SkinToken token = item.getToken();

		// 设置标签字体
		String text = token.getTitle();
		if (text == null) {
			text = "";
		}
		setText(text);
		
//		FontKit.setLabelText(this, token.getTitle());

		// 设置图标
		String icon = token.getIcon();
		if (icon != null && icon.trim().length() > 0) {
			ResourceLoader res = new ResourceLoader();
			ImageIcon image = res.findImage(icon, 16, 16); // 固定是16*16的像素
			if (image != null) {
				setIcon(image);
			}
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

		// top, left, bottom, right
		setBorder(new EmptyBorder(4, 4, 4, 4));
		return this;
	}

}
