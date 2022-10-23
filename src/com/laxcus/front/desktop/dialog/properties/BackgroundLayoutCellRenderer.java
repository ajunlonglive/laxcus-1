/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.properties;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 背景布局单元
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
class BackgroundLayoutCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 3273083088398301587L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;


	public BackgroundLayoutCellRenderer(){
		super();
		init();
	}

	private void init() {
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
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
		FontKit.setDefaultFont(this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		//		if (value == null) {
		//			setIcon(null);
		//			setText("");
		//			return this;
		//		}
		//		if (value.getClass() != BackgroundLayoutItem.class) {
		//			setIcon(null);
		//			setText("");
		//			return this;
		//		}

		// 判断
		boolean success = (value != null && value.getClass() == BackgroundLayoutItem.class);
		if (!success) {
			setIcon(null);
			setText("");
			return this;
		}

		setFont(list.getFont());

		BackgroundLayoutItem item = (BackgroundLayoutItem)value;

		// 设置标签字体
//		FontKit.setLabelText(this, item.getText());
		
		String text = item.getText();
		if (text == null) {
			text = "";
		}
		setText(text);

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
		setBorder(new EmptyBorder(2, 2, 2, 2));
		setEnabled(list.isEnabled());

		return this;
	}

}