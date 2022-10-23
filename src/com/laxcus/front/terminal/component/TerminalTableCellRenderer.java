/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.component;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.skin.*;

/**
 * FRONT.TERMINAL表格渲染器
 * 
 * @author scott.liang
 * @version 1.0 2/15/2018
 * @since laxcus 1.0
 */
public class TerminalTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -5500369591274815541L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/**
	 * 构造FRONT.TERMINAL表格渲染器
	 */
	public TerminalTableCellRenderer() {
		super();
		init();
	}
	
	/**
	 * 加载颜色!
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
	}

	/**
	 * 判断是字符
	 * @param w
	 * @return
	 */
	private boolean isAlpha(char w) {
		return 'a' <= w && w <= 'z' || 'A' <= w && w <= 'Z';
	}

	/**
	 * 判断是数字
	 * @param w
	 * @return
	 */
	private boolean isDigit(char w) {
		return '0' <= w && w <= '9';
	}

	/**
	 * 格式化
	 * @param w 字符
	 * @return 返回结果
	 */
	private String format(char w) {
		// 是普通字符
		if (isAlpha(w) || isDigit(w)) {
			return String.format("%c", w);
		} else if (w > 0xFF) {
			return "&#" + String.format("%d", (int) (w)) + ";";
		}

		switch (w) {
		case '\t':
			return "&nbsp;&nbsp;&nbsp;&nbsp;";
		case 0x20:
			return "&nbsp;&nbsp;";
		case '<':
			return "&lt;";
		case '>':
			return "&gt;";
		case '&':
			return "&amp;";
		case '\"':
			return "&quot;";
		case '.':
			return "&middot;";
		}

		return "&#" + String.format("%d", (int) (w)) + ";";
	}

	/**
	 * 格式化空格
	 * @param text
	 * @return
	 */
	private String format(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		StringBuilder bf = new StringBuilder();
		int len = text.length();
		for (int i = 0; i < len; i++) {
			char w = text.charAt(i);
			bf.append(format(w));
		}

		return "<HTML><BODY>" + bf.toString() + "</BODY></HTML>";
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		// 判断有效
		boolean success = (value != null && Laxkit.isClassFrom(value, ShowItemCell.class));
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
//			FontKit.setLabelText(this, text);
			setText(text);
			setIcon(image.getIcon());
		} else {
			String text = cell.visible().toString();
			text = format(text);
//			FontKit.setLabelText(this, text);
			setText(text);
			setIcon(null);
		}

		// 设置提示文本
		if (cell.getTooltip() != null) {
			setToolTipText(cell.getTooltip());
		}

//		// 选择颜色
//		if (isSelected) {
//			setBackground(table.getSelectionBackground());
//			setForeground(table.getSelectionForeground());
//		} else {
//			// 背景
//			Color color = cell.getBackground();
//			if (color != null) {
//				setBackground(color);
//			} else {
//				setBackground(table.getBackground());
//			}
//			// 前景
//			color = cell.getForeground();
//			if (color != null) {
//				setForeground(color);
//			} else {
//				setForeground(table.getForeground());
//			}
//		}
		
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

		setOpaque(true);

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