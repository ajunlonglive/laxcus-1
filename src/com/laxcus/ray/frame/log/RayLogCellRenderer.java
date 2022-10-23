/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.frame.log;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.color.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 日志显示单元。
 * 
 * @author scott.liang
 * @version 1.0 09/06/2009
 * @since laxcus 1.0
 */
class RayLogCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 987871799148924041L;

	/** 显示图标 **/
	private Icon debugIcon, subDebugIcon;

	private Icon infoIcon, subInfoIcon;

	private Icon warningIcon, subWarningIcon;

	private Icon errorIcon, subErrorIcon;

	private Icon fatalIcon, subFatalIcon;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	
	/**
	 * 构造日志单元委托
	 */
	public RayLogCellRenderer() {
		super();
		// 初始化
		init();
	}
	
	/**
	 * 加载图标
	 */
	private void loadIcons() {
		debugIcon = UIManager.getIcon("LogFrame.DebugIcon");
		infoIcon = UIManager.getIcon("LogFrame.InfoIcon");
		warningIcon = UIManager.getIcon("LogFrame.WarningIcon");
		errorIcon = UIManager.getIcon("LogFrame.ErrorIcon");
		fatalIcon = UIManager.getIcon("LogFrame.FatalIcon");

		subDebugIcon = UIManager.getIcon("LogFrame.SubDebugIcon");
		subInfoIcon = UIManager.getIcon("LogFrame.SubInfoIcon");
		subWarningIcon = UIManager.getIcon("LogFrame.SubWarningIcon");
		subErrorIcon = UIManager.getIcon("LogFrame.SubErrorIcon");
		subFatalIcon = UIManager.getIcon("LogFrame.SubFatalIcon");
	}

	/**
	 * 加载颜色
	 */
	private void loadColor() {
		textSelectForeground = Skins.findListTextSelectForeground();
		textSelectBackground = Skins.findListTextSelectBackground();
		textForeground = Skins.findListForeground();
		textBackground = Skins.findListBackground();
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		loadIcons();
		loadColor();

		// 图标和文本的间隔尺寸，5个像素
		setIconTextGap(5);
		setOpaque(true);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		// 判断匹配
		boolean success = (value != null && value.getClass() == LogItem.class);
		if (!success) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());

		LogItem log = (LogItem) value;

		Icon icon = null;
		Color foreground = null;

		// 显示图标和选择颜色
		switch(log.getFamily()) {
		case LogItem.DEBUG:
			icon = debugIcon; break;
		case LogItem.INFO:
			icon = infoIcon; break;
		case LogItem.WARNING:
			icon = warningIcon; break;
		case LogItem.ERROR:
			icon = errorIcon;
			foreground = ColorTemplate.findColor("红绯", Color.MAGENTA);
			break;
		case LogItem.FATAL:
			icon = fatalIcon;
			foreground = ColorTemplate.findColor("红赤", Color.RED);
			break;
		case LogItem.SUBDEBUG:
			icon = subDebugIcon; break;
		case LogItem.SUBINFO:
			icon = subInfoIcon; break;
		case LogItem.SUBWARNING:
			icon = subWarningIcon; break;
		// 子错误/子故障
		case LogItem.SUBERROR:
			icon = subErrorIcon;
			foreground = ColorTemplate.findColor("红绯", Color.MAGENTA);
			break;
		case LogItem.SUBFATAL:
			icon = subFatalIcon;
			foreground = ColorTemplate.findColor("红赤", Color.RED);
			break;
		}
		// 取默认前景颜色
		if (foreground == null) {
			foreground = textForeground;
		}

		// 显示图标和文本
		if (icon != null) {
			setIcon(icon);
		} else {
			setIcon(null);
		}
		
		// 设置文本
		setText(log.getText());

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
		setBorder(new EmptyBorder(2, 2, 2, 2));
		return this;
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

}