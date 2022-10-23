/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.component;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.skin.*;

/**
 * WATCH站点表格渲染器。
 * 注意！不用在这里定义边框，setBorder忽略，由JTable.setIntercellSpacing来决定行和列之间的间隔！
 * 
 * @author scott.liang
 * @version 1.0 2/15/2018
 * @since laxcus 1.0
 */
public class WatchMixedRuntimeCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 4884522276519326610L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;
	
	/**
	 * 构造WATCH站点表格渲染器
	 */
	public WatchMixedRuntimeCellRenderer() {
		super();
		init();
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
	
//	private String format(Color e) {
//		if(e == null) {
//			return "null";
//		}
//		return String.format("%d,%d,%d", e.getRed(), e.getGreen(), e.getBlue());
//	}
	
	/**
	 * 初始颜色
	 */
	private void init() {
		loadColor();
		
//		System.out.printf("text background %s\n", format(textBackground));
//		System.out.printf("text foreground %s\n", format(textForeground));
//		System.out.printf("select text background %s\n", format(textSelectBackground));
//		System.out.printf("select text foreground %s\n", format(textSelectForeground));
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		// 如果是空指针，或者不是ShowItemCell，交给父类去处理
		boolean success = (value != null && Laxkit.isClassFrom(value, ShowItemCell.class));
		if (!success) {
			return super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
		}
		
		setFont(table.getFont());

		ShowItemCell cell = (ShowItemCell) value;

		// 设置图标
		if (Laxkit.isClassFrom(cell, ShowImageCell.class)) {
			ShowImageCell atom = (ShowImageCell) cell;
//			FontKit.setLabelText(this, atom.getText() != null ? atom.getText() : " ");
			setText(atom.getText() != null ? atom.getText() : " ");
			setIcon(atom.getIcon());
		} else {
//			FontKit.setLabelText(this, cell.visible().toString());
			setText(cell.visible().toString());
			setIcon(null);
		}

		// 设置提示文本
		if (cell.getTooltip() != null) {
			FontKit.setToolTipText(this, cell.getTooltip());
		}

		// 选择颜色
		if (isSelected) {
			setBackground(textSelectBackground);
			setForeground(textSelectForeground);
		} else {
			// 标准背景色，忽略自定义背景颜色！
			setBackground(textBackground);

			// 前景自定义！
			Color color = cell.getForeground();
			if (color != null) {
				setForeground(color);
			} else {
				setForeground(textForeground);
			}
		}

		// 左侧留空3个像素
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