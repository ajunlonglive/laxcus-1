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

import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 提示信息单元。
 * 
 * @author scott.liang
 * @version 1.0 8/23/2009
 * @since laxcus 1.0
 */
public class TerminalMessageCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -5328143322180357917L;

	/** 普通消息的图标和颜色 **/
	private Icon messageIcon;
	private Color messageColor;

	/** 警告消息的图标和颜色 **/
	private Icon warningIcon;
	private Color warningColor;
	
	/** 故障消息的图标和颜色 **/
	private Icon faultIcon;
	private Color faultColor;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;
	
	/**
	 * 构造消息显示单元
	 */
	public TerminalMessageCellRenderer() {
		super();
		init();
	}
	
	private void loadColor() {
		// 调用系统颜色参数
		messageColor = Skins.findMessagePanelMessageForeground();
		warningColor = Skins.findMessagePanelWarningForeground();
		faultColor = Skins.findMessagePanelFaultForeground();

		// 前景/背景颜色
		textSelectForeground = Skins.findListTextSelectForeground();
		textSelectBackground = Skins.findListTextSelectBackground();
		textForeground = Skins.findListForeground();
		textBackground = Skins.findListBackground();
	}

	private void loadImages() {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/window/prompt");
		// 选择图标
		faultIcon = loader.findImage("fault.png");
		warningIcon = loader.findImage("warning.png");
		messageIcon = loader.findImage("message.png");
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		setOpaque(true);
		// 图标和文本的间隔尺寸，5个像素
		setIconTextGap(5);
		
		loadImages();
		loadColor();
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		// 传入对象必须是NoteItem
		if (value == null || value.getClass() != NoteItem.class) {
			setIcon(null);
			setText("");
			return this;
		}

		// 设置字体
		setFont(list.getFont());

		NoteItem item = (NoteItem) value;

		Color color = null;
		Icon icon = null;
		
		// Color color = Color.black;

		// 选择图标和颜色
		if (item.isFault()) {
			icon = faultIcon;
			color = faultColor;
		} else if (item.isWarning()) {
			icon = warningIcon;
			color = warningColor;
		} else if (item.isMessage()) {
			icon = messageIcon;
			color = messageColor;
		}
		
		// 取默认颜色
		if (color == null) {
			color = textForeground;
		}

		// 文字和图标
//		FontKit.setLabelText(this, item.getText());
		setText(item.getText());
		setIcon(icon);

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
			setForeground(color);
		}
		
		// 边框, top, left, bottom, right
		setBorder(new EmptyBorder(8, 2, 8, 2));
		
		setEnabled(list.isEnabled());

		setOpaque(true); // 这行一定要有，在刷新时使用
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