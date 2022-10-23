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

import com.laxcus.util.color.*;
import com.laxcus.util.display.*;
import com.laxcus.util.loader.*;
import com.laxcus.util.skin.*;

/**
 * 日志显示单元。
 * 
 * @author scott.liang
 * @version 1.0 09/06/2009
 * @since laxcus 1.0
 */
public class TerminalLogCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 987871799148924041L;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;
	
	/** 显示图标 **/
	private Icon debugIcon, subDebugIcon;

	private Icon infoIcon, subInfoIcon;

	private Icon warningIcon, subWarningIcon;

	private Icon errorIcon, subErrorIcon;

	private Icon fatalIcon, subFatalIcon;

	/**
	 * 构造日志单元委托
	 */
	public TerminalLogCellRenderer() {
		super();
		init();
	}
	
	private void loadImages() {
		ResourceLoader loader = new ResourceLoader("conf/front/terminal/image/window/log");
		debugIcon = loader.findImage("debug.png");
		infoIcon = loader.findImage("info.png");
		warningIcon = loader.findImage("warning.png");
		errorIcon = loader.findImage("error.png");
		fatalIcon = loader.findImage("fatal.png");

		subDebugIcon = loader.findImage("subdebug.png");
		subInfoIcon = loader.findImage("subinfo.png");
		subWarningIcon = loader.findImage("subwarning.png");
		subErrorIcon = loader.findImage("suberror.png");
		subFatalIcon = loader.findImage("subfatal.png");
	}

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
	
//	/**
//	 * 格式化文本
//	 * @param font
//	 * @param text
//	 * @return
//	 */
//	private String format(Font font, String text) {
//		if(text == null) {
//			return "null log";
//		}
//		text = text.replaceAll("([\\t]{1})", "&nbsp;&nbsp;&nbsp;");
//		text = text.replaceAll("([\\x20]{1})", "&nbsp;");
//		text = String.format("<HTML><font face=\"%s\">%s</font></HTML>",
//				font.getName(), text);
//		return text;
//	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if (value == null || value.getClass() != LogItem.class) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());

		LogItem log = (LogItem) value;

		Icon icon = null;
		Color color = null;

		// 显示图标和选择颜色
		switch(log.getFamily()) {
		case LogItem.DEBUG:
			icon = debugIcon; break;
		case LogItem.INFO:
			icon = infoIcon; break;
		case LogItem.WARNING:
			icon = warningIcon;
			break;
		case LogItem.ERROR:
			icon = errorIcon;
			color = ColorTemplate.findColor("红绯", Color.MAGENTA);
			break;
		case LogItem.FATAL:
			icon = fatalIcon;
			color = ColorTemplate.findColor("红赤", Color.RED);
			break;
		case LogItem.SUBDEBUG:
			icon = subDebugIcon; break;
		case LogItem.SUBINFO:
			icon = subInfoIcon; break;
		case LogItem.SUBWARNING:
			icon = subWarningIcon;
			break;
		case LogItem.SUBERROR:
			icon = subErrorIcon;
			color = ColorTemplate.findColor("红绯", Color.MAGENTA);
			break;
		case LogItem.SUBFATAL:
			icon = subFatalIcon;
			color = ColorTemplate.findColor("红赤", Color.RED);
			break;
		}

		// 取默认前景颜色
		if (color == null) {
			color = textForeground;
		}

		// 文本
		String text = log.getText();
		// 显示图标和文本
		setIcon(icon);
//		FontKit.setLabelText(this, text);
		setText(text);

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