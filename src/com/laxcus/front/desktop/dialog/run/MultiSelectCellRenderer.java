/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.run;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 同样命令单元
 * 
 * @author scott.liang
 * @version 1.0 8/13/2021
 * @since laxcus 1.0
 */
class MultiSelectCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -4941199257997858214L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/**
	 * 构造同样命令单元
	 */
	public MultiSelectCellRenderer(){
		super();
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		loadTextColor();
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		setIconTextGap(6);
		// 让组件不透明，这样上面的面板颜色才能显示出来
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

		if (value == null) {
			setIcon(null);
			setText("");
			return this;
		}
		if (value.getClass() != MultiSelectCommand.class) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());

		MultiSelectCommand element = (MultiSelectCommand) value;

		// 设置标签字体
		FontKit.setLabelText(this, element.getTitle());
		FontKit.setToolTipText(this, element.getToolTip());

		// 显示图标
		ImageIcon icon = element.getIcon();
		if (icon != null) {
			if (icon.getIconHeight() == 32 && icon.getIconWidth() == 32) {
				setIcon(icon);
			} else {
				ImageIcon image = ImageUtil.scale(icon.getImage(), 32, 32, true);
				setIcon(image);
			}
		}

		// 显示颜色
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

		// top, left, bottom, right
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