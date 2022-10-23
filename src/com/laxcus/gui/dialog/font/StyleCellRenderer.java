/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.font;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.skin.*;

/**
 * 字体名称单元
 * 
 * @author scott.liang
 * @version 1.0 4/24/2022
 * @since laxcus 1.0
 */
final class StyleCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -4151775993480714520L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;
	
	/**
	 * 构造字体名称单元
	 */
	public StyleCellRenderer() {
		super();
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		// 图标和文本的间隔尺寸，5个像素
		setIconTextGap(5);

		// 加载颜色
		loadColor();

		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		setOpaque(true); // 这行一定要有，在刷新时使用
	}
	
	/**
	 * 加载颜色参数
	 */
	private void loadColor() {
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

		boolean success = (value != null && value.getClass() == FontStyle.class);
		if (!success) {
			setText("");
			return this;
		}
	
		setFont(list.getFont());
		FontStyle fs = (FontStyle) value;

		// 设置文字
		setText(fs.getName());
		setToolTipText(fs.getName());
		
		// 前景/背景颜色
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
			setForeground(this.textForeground);
		}

		setBorder(new EmptyBorder(1, 1, 1, 1));

		return this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		loadColor();
		super.updateUI();
	}

}