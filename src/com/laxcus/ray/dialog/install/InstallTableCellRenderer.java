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
import javax.swing.table.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.skin.*;

/**
 * WATCH站点表格渲染器
 * 
 * @author scott.liang
 * @version 1.0 12/25/2019
 * @since laxcus 1.0
 */
class InstallTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 4884522276519326610L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/** 是/否 **/
	private String yesText, noText;

	/**
	 * 构造WATCH站点表格渲染器
	 */
	public InstallTableCellRenderer() {
		super();
		init();
	}

	public void loadText() {
		yesText = UIManager.getString("InstallDialog.yesDeployDesktopIconText");
		if (yesText == null) {
			yesText = "Yes";
		}
		noText = UIManager.getString("InstallDialog.noDeployDesktopIconText");
		if (noText == null) {
			noText = "No";
		}
	}
	/**
	 * 加载颜色
	 */
	private void loadColor() {
		textSelectForeground = Skins.findTableTextSelectForeground();
		textSelectBackground = Skins.findTableTextSelectBackground();
		textForeground = Skins.findTableTextForeground();
		textBackground = Skins.findTableTextBackground();
	}

	/**
	 * 初始颜色
	 */
	private void init() {
		loadColor();
		loadText();
		setOpaque(true);
	}
	
	public void setValue(Object value) {
		super.setValue(value);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		// 判断有效
		boolean success = (value!=null && Laxkit.isClassFrom(value, ShowItemCell.class));
		if (!success) {
			return super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
		}

		setFont(table.getFont());
		
		ShowItemCell cell = (ShowItemCell) value;
		// 设置图标
		if (Laxkit.isClassFrom(cell, ShowImageCell.class)) {
			ShowImageCell image = (ShowImageCell) cell;
			String text = (image.getText() == null ? "" : image.getText());
			FontKit.setLabelText(this, text);
			setIcon(image.getIcon());
		} else if (Laxkit.isClassFrom(cell, ShowBooleanCell.class)) {
			ShowBooleanCell bool = (ShowBooleanCell) cell;
			String text = (bool.getValue() ? yesText : noText);
			FontKit.setLabelText(this, text);
			setIcon(null);
		} else {
			String text = cell.visible().toString();
			FontKit.setLabelText(this, text);
			setIcon(null);
		}

		// 设置提示文本
		String tooltip = cell.getTooltip();
		if (tooltip != null && tooltip.trim().length() > 0) {
			FontKit.setToolTipText(this, tooltip);
		} else {
			setToolTipText(null);
		}

		// 选择颜色
		if (isSelected) {
			setBackground(textSelectBackground);
			setForeground(textSelectForeground);
		} else {
			// 背景
			Color color = cell.getBackground();
			if (color != null) {
				setBackground(color);
			} else {
				setBackground(textBackground);
			}
			// 前景
			color = cell.getForeground();
			if (color != null) {
				setForeground(color);
			} else {
				setForeground(textForeground);
			}
		}
		
		// 左侧留空2个像素
		setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		
		// 返回实例
		return this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#updateUI()
	 */
	@Override
	public void updateUI() {
		loadColor();
		super.updateUI();
	}

}