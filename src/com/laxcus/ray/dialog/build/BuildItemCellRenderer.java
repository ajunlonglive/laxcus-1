/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.build;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
* 文件列表单元
* 
* @author scott.liang
* @version 1.0 7/26/2021
* @since laxcus 1.0
*/
class BuildItemCellRenderer extends JLabel implements ListCellRenderer { 

	private static final long serialVersionUID = 1640135684768488975L;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 显示图片 **/
	private Icon imageIcon, jarIcon, libraryIcon, textIcon, otherIcon;

	/**
	 * 分布站点参数
	 */
	public BuildItemCellRenderer() {
		super();
		init();
	}

	/**
	 * 初始化界面参数
	 */
	private void init() {
		loadColor();
		loadIcons();
		setHorizontalAlignment(JLabel.LEFT);
		setIconTextGap(5);
		setOpaque(true);
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
	
	/**
	 * 加载图片
	 */
	private void loadIcons() {
		jarIcon = UIManager.getIcon("BuildDialog.ListJarImage");
		imageIcon = UIManager.getIcon("BuildDialog.ListIconImage");
		libraryIcon = UIManager.getIcon("BuildDialog.ListLibImage");
		textIcon = UIManager.getIcon("BuildDialog.ListTextImage");
		otherIcon = UIManager.getIcon("BuildDialog.ListOtherImage");
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		loadColor();
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
		if (value.getClass() != BuildItem.class) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());

		BuildItem node = (BuildItem) value;

		// 文本
		FontKit.setLabelText(this, node.getFilename());
		FontKit.setToolTipText(this, node.getFilename());
		
		// 定义图标
		Icon icon = node.getIcon();
		if (icon != null) {
			setIcon(icon);
		} else {
			// 设置图标
			if (node.isJar()) {
				setIcon(jarIcon);
			} else if (node.isLibrary()) {
				setIcon(libraryIcon);
			} else if (node.isIcon()) {
				setIcon(imageIcon);
			} else if (node.isText()) {
				setIcon(textIcon);
			} else {
				setIcon(otherIcon);
			}
		}

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

		return this;
	}

}