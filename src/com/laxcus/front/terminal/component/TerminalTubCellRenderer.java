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
import javax.swing.border.*;

import com.laxcus.tub.servlet.*;
import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 边缘应用显示单元。
 * 
 * @author scott.liang
 * @version 1.0 9/26/2020
 * @since laxcus 1.0
 */
public class TerminalTubCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 987871799148924041L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;
	
	/** 默认图标，当传入对象没有定义图标时显示 **/
	private Icon defaultIcon;
	
	/**
	 * 构造日志单元委托
	 */
	public TerminalTubCellRenderer() {
		super();
		init();
	}
	
	/**
	 * 加载图标
	 */
	private void loadImages() {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/object");
		defaultIcon = loader.findImage("tub32.png");
	}

	/**
	 * 加载颜色
	 */
	private void loadColors() {
		textSelectForeground = Skins.findListTextSelectForeground();
		textSelectBackground = Skins.findListTextSelectBackground();
		textForeground = Skins.findListForeground();
		textBackground = Skins.findListBackground();
	}
	
	/**
	 * 初始化参数！
	 */
	private void init() {
		// 图标和文本的间隔尺寸，5个像素
		setIconTextGap(5);
		loadImages();
		loadColors();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if (value == null || value.getClass() != TubItem.class) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());
		TubItem item = (TubItem) value;
		TubTag tag = item.getTubTag();

		Icon icon = item.getIcon();
		// 没有图标，使用默认
		if (icon == null) {
			icon = defaultIcon;
		}
		// 显示图标
		if (icon != null) {
			setIcon(icon);
		}
		// 文本
		String title = tag.getCaption();
		if (title != null) {
//			FontKit.setLabelText(this, title);
			setText(title);
		} else {
//			FontKit.setLabelText(this, "None Tub");
			setText("None Tub");
		}
		// 提示
		String tooltip = tag.getTooltip();
		if (tooltip != null && tooltip.trim().length() > 0) {
			FontKit.setToolTipText(this, tooltip);
		}

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

		// 边框!
		setBorder(new EmptyBorder(2, 2, 2, 2));
		setEnabled(list.isEnabled());
		setOpaque(true);
		
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		loadColors();
		super.updateUI();
	}

}