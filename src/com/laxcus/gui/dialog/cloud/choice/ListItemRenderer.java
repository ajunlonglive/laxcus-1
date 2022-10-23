/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.cloud.choice;

import java.awt.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 根单元渲染器
 * 
 * @author scott.liang
 * @version 1.0 9/3/2021
 * @since laxcus 1.0
 */
final class ListItemRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 3273083088398301587L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/** 磁盘图标 **/
	private Icon fileIcon;

	/** 目录图标 **/
	private Icon dirIcon;

	/**
	 * 构造根单元渲染器
	 */
	public ListItemRenderer(){
		super();
		init();
	}

	private void init() {
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		setIconTextGap(4);
		loadTextColor();
		loadIcons();
		setOpaque(true);
	}

	/**
	 * 加载图标
	 */
	private void loadIcons() {
		fileIcon = UIManager.getIcon("CloudChoiceDialog.FileIcon");
		dirIcon = UIManager.getIcon("CloudChoiceDialog.DirectoryIcon");
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

		// 必须有效且匹配，
		boolean success = (value != null && value.getClass() == SRLItem.class);
		if (!success) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());

		SRLItem item = (SRLItem) value;

		// 文本
		String name = item.getName();
		FontKit.setLabelText(this, name);
		// 提示信息
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
		String lastModified = sdf.format(new java.util.Date(item.lastModified));

		// 图标
		Icon icon = item.getIcon();
		if (icon != null) {
			setIcon(icon);
		} else {
			if (item.isFile()) {
				setIcon(fileIcon);
			} else if (item.isDirectory()) {
				setIcon(dirIcon);
			}
		}

		// 提示
		if (item.isFile()) {
			String length = ConfigParser.splitCapacity(item.length, 2);
			String tooltip = String.format("%s<br>%s<br>%s<br>%s", item.srl, item.typeDescription, length, lastModified);
			FontKit.setToolTipText(this, tooltip);
		} else if (item.isDirectory()) {
			String tooltip = String.format("%s<br>%s", item.srl, lastModified);
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

		// top, left, bottom, right
		setBorder(new EmptyBorder(3, 3, 3, 3));
		
		return this;
	}

}