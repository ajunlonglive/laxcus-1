/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.cloud.choice;

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
class SRLRootRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 3273083088398301587L;

	private Icon icon;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/**
	 * 构造根单元渲染器
	 */
	public SRLRootRenderer(){
		super();
		init();
	}

	private void init() {
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		loadTextColor();
		setIconTextGap(4);
		setOpaque(true);
		// 云端服务器图标
		icon = UIManager.getIcon("CloudChoiceDialog.SiteIcon");
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

		// 有效一致时
		boolean success = (value != null && value.getClass() == SRLRoot.class);
		if (!success) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());

		SRLRoot root = (SRLRoot) value;
		SRL srl = root.getSRL();

		// 显示文本
		if (srl != null) {
			FontKit.setLabelText(this, srl.toString());
		} else {
			FontKit.setLabelText(this, "");
		}
		// 显示图标
		if (icon != null) {
			setIcon(icon);
		}

		// 提示字
		String tooltip = root.getDescription();
		if (tooltip != null) {
			FontKit.setToolTipText(this, tooltip);
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

		// 边框
		setBorder(new EmptyBorder(8, 4, 8, 6));

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