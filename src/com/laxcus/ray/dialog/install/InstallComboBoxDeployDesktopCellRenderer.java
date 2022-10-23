/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.install;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
* 下拉框部署桌面图标
* 
* @author scott.liang
* @version 1.0 10/7/2021
* @since laxcus 1.0
*/
class InstallComboBoxDeployDesktopCellRenderer extends JLabel implements ListCellRenderer { 

	private static final long serialVersionUID = 2909452960955966987L;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/**
	 * 分布站点参数
	 */
	public InstallComboBoxDeployDesktopCellRenderer() {
		super();
		init();
	}

	/**
	 * 初始化界面参数
	 */
	private void init() {
		loadColor();
		setHorizontalAlignment(JLabel.LEFT);
		setIconTextGap(5);
		setOpaque(true); // 这行一定要有，在刷新时使用
	}
	
	/**
	 * 从内存中加载颜色
	 */
	private void loadColor() {
		textForeground = Skins.findTreeTextForeground();
		textBackground = Skins.findTreeTextBackground();
		textSelectForeground = Skins.findTreeTextSelectForeground();
		textSelectBackground = Skins.findTreeTextSelectBackground();
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
		// 传入对象是String
		if (value.getClass() != InstallDesktopIcon.class) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());

		// 部署图标到桌面
		InstallDesktopIcon node = (InstallDesktopIcon) value;
		// 文本
		FontKit.setLabelText(this, node.getText());

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
		loadColor();
		super.updateUI();
	}

}