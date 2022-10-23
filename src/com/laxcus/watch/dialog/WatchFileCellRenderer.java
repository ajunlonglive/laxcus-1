/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.dialog;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.skin.*;

/**
 * 提示信息单元。
 * 
 * @author scott.liang
 * @version 1.0 10/11/2019
 * @since laxcus 1.0
 */
public class WatchFileCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -6902998553047263017L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;
	
	/**
	 * 构造消息显示单元
	 */
	public WatchFileCellRenderer() {
		super();
		init();
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		textSelectForeground = Skins.findListTextSelectForeground();
		textSelectBackground = Skins.findListTextSelectBackground();
		textForeground = Skins.findListForeground();
		textBackground = Skins.findListBackground(); 
		
		setOpaque(true);
		// 图标和文本的间隔尺寸，8个像素
		setIconTextGap(8);
		setBorder(new EmptyBorder(1, 1, 8, 1));
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		// 传入对象是String
		if (value == null || value.getClass() != String.class) {
			setIcon(null);
			setText("");
			return this;
		}
		
		// 设置字体
		setFont(list.getFont());

		String item = (String) value;

		// 文字和图标
//		FontKit.setLabelText(this, item);
		setText(item);

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

		setEnabled(list.isEnabled());
		setOpaque(true); // 这行一定要有，在刷新时使用

		return this;
	}

}