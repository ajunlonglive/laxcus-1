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
import javax.swing.border.*;

import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 消息显示单元。
 * 
 * @author scott.liang
 * @version 1.0 8/23/2009
 * @since laxcus 1.0
 */
public class WatchMixedMessageCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -3347964917457252988L;

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
	public WatchMixedMessageCellRenderer() {
		super();
		init();
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		// 图标和文本的间隔尺寸，5个像素
		setIconTextGap(5);
		
		// 保存图标
		ResourceLoader loader = new ResourceLoader("conf/watch/image/prompt/");
		
		messageIcon = loader.findImage("message.png");
		warningIcon = loader.findImage("warning.png");
		faultIcon = loader.findImage("fault.png");
		
		// 加载颜色
		loadColor();
	}
	
	/**
	 * 加载颜色参数
	 */
	private void loadColor() {
		textSelectForeground = Skins.findListTextSelectForeground();
		textSelectBackground = Skins.findListTextSelectBackground();
		textForeground = Skins.findListForeground();
		textBackground = Skins.findListBackground();

		// 如果没有定义，调用系统颜色参数
		messageColor = Skins.findMessagePanelMessageForeground();
		warningColor = Skins.findMessagePanelWarningForeground();
		faultColor = Skins.findMessagePanelFaultForeground();
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

		setFont(list.getFont());

		NoteItem item = (NoteItem) value;

		Color foreground = null;
		Icon icon = null;
		if (item.isMessage()) {
			icon = messageIcon;
			foreground = messageColor;
		} else if (item.isWarning()) {
			icon = warningIcon;
			foreground = warningColor;
		} else if (item.isFault()) {
			icon = faultIcon;
			foreground = faultColor;
		}
		
		// 取默认颜色
		if (foreground == null) {
			foreground = textForeground;
		}

		// 设置文字
//		FontKit.setLabelText(this, item.getText());
		setText(item.getText());
		setIcon(icon);

		// 前景/背景颜色
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