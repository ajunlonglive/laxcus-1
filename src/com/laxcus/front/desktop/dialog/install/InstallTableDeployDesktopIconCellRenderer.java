/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dialog.install;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.laxcus.util.display.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.skin.*;

/**
 * 表格界面上的部署图标
 * 
 * @author scott.liang
 * @version 1.0 7/18/2021
 * @since laxcus 1.0
 */
class InstallTableDeployDesktopIconCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 6253191384998598606L;
	
	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;


	private String yesText;
	private String noText;

	public InstallTableDeployDesktopIconCellRenderer() {
		super();
		init();
	}
	
	/**
	 * 初始化界面参数
	 */
	private void init() {
		loadColor();
		loadText();
		setHorizontalAlignment(JLabel.LEFT);
		setIconTextGap(5);
	}
	
	/**
	 * 显示文本
	 */
	private void loadText() {
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
	 * 从内存中加载颜色
	 */
	private void loadColor() {
		textForeground = Skins.findTreeTextForeground();
		textBackground = Skins.findTreeTextBackground();
		textSelectForeground = Skins.findTreeTextSelectForeground();
		textSelectBackground = Skins.findTreeTextSelectBackground();
	}
	
//	public void setValue(Object value) {
//		System.out.printf("desktop icon class is %s\n", value.getClass().getName());
//		
//		boolean success = (value!=null && Laxkit.isClassFrom(value, ShowBooleanCell.class));
//		
//		if (success) {
//			ShowBooleanCell cell = (ShowBooleanCell) value;
//
//			this.setText(cell.getValue() ? yesText : noText);
//		} else {
//
//			super.setValue(value);
//		}
//	}
	
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		// 传入对象是ShowBooleanCell
		boolean success = (value != null && value.getClass() == ShowBooleanCell.class);
		if (!success) {
			return super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
		}

		// 设置字体
		setFont(table.getFont());

		ShowBooleanCell cell = (ShowBooleanCell) value;

		// 文本
		FontKit.setLabelText(this, cell.getValue() ? yesText : noText);

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
		
//		setBorder(new EmptyBorder(8, 6, 8, 6));
		setBorder(new EmptyBorder(4, 4, 4, 4));
//		setEnabled(list.isEnabled());
		setOpaque(true); // 这行一定要有，在刷新时使用
		
		return this;
	}
	
	public void updateUI() {
		this.loadColor();
		super.updateUI();
	}
}
