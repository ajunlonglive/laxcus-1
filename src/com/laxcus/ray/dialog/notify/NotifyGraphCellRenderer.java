/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.notify;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.skin.*;

/**
 * 软件启动单元。
 * 
 * @author scott.liang
 * @version 1.0 8/8/2020
 * @since laxcus 1.0
 */
class NotifyGraphCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -5328143322180357917L;

//	/** 选中前景/背景颜色 **/
//	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/**
	 * 构造消息显示单元
	 */
	public NotifyGraphCellRenderer() {
		super();
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		loadColors();
		// 图标和文本的间隔尺寸，8个像素
		setIconTextGap(10);
		setOpaque(true); // 这行一定要有，在刷新时使用
	}

	/**
	 * 加载颜色
	 */
	private void loadColors() {
//		textSelectForeground = Skins.findListTextSelectForeground();
//		textSelectBackground = Skins.findListTextSelectBackground();
		textForeground = Skins.findListForeground();
		textBackground = Skins.findListBackground(); 
	}

	/**
	 * 格式化文本
	 * @param font
	 * @param text
	 */
	private String doText(Font font, String text) {
		text = text.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;");
		text = text.replaceAll("\\x20", "&nbsp;");
		text = text.replaceAll("\\s+", "<BR>");

		//		String body = String.format(
		//				"<html><font size=\"4\" face=\"%s\">%s</font></html>",
		//				font.getName(), text);

		String body = String.format("<html><body>%s</body></html>",text);

		return body;
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if (value == null) {
			setIcon(null);
			setText("");
			return this;
		}
		// 只支持这两个对象！
		boolean success = (value.getClass() == GraphIconCell.class || value.getClass() == GraphTextCell.class);
		if (!success) {
			setIcon(null);
			setText("");
			return this;
		}

		// 设置字体
		setFont(list.getFont());

//		// 设置背景和前景色
//		if (isSelected) {
//			setBackground(textSelectBackground);
//			setForeground(textSelectForeground);
//		} else {
//			/** 
//			 * 注意！这个一个JAVA的BUG！
//			 * 情况：textBackground和Color.WHITE相等情况下，用setBackground(textBackground)
//			 * 而不是setBackground(Color.WHITE)，背景变灰（不正常！！！），setBackground(Color.WHITE)则是显示正常。
//			 * 所以采用下面办法避免故障！！！
//			 **/
//			if (Color.WHITE.equals(textBackground)) {
//				setBackground(Color.WHITE);
//			} else {
//				setBackground(textBackground);
//			}
//			setForeground(textForeground);
//		}

		if (Color.WHITE.equals(textBackground)) {
			setBackground(Color.WHITE);
		} else {
			setBackground(textBackground);
		}
		setForeground(textForeground);
		
		// 选择显示！
		if (value.getClass() == GraphIconCell.class) {
			GraphIconCell e = (GraphIconCell) value;
			setIcon(e.getIcon());
			setText(null);
			setToolTipText(e.getTooltip());
		} else if (value.getClass() == GraphTextCell.class) {
			GraphTextCell e = (GraphTextCell) value;
			String text = e.getText();
			text = doText(list.getFont(), text);
			setIcon(null);
			setText(text);
			setToolTipText(e.getTooltip());
		}

		setEnabled(list.isEnabled());
		setBorder(new EmptyBorder(3, 2, 3, 2));

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

///**
// * 图型显示单元。
// * 
// * @author scott.liang
// * @version 1.0 12/07/2011
// * @since laxcus 1.0
// */
//public class NotifyGraphCellRenderer extends JLabel implements ListCellRenderer {
//
//	private static final long serialVersionUID = 2796091057997376266L;
//
//	/** 没选中前景/背景颜色 **/
//	private Color textForeground, textBackground;
//	
//	/**
//	 * 构造消息显示单元
//	 */
//	public NotifyGraphCellRenderer() {
//		super();
//		init();
//	}
//	
//	private void loadColors() {
//		textForeground = Skins.findListForeground();
//		textBackground = Skins.findListBackground();
//	}
//	
//	private void init() {
//		loadColors();
//	}
//	
//	/**
//	 * 只显示图标
//	 * @param cell
//	 */
//	private void doIcon(GraphIconCell cell) {
//		setIcon(cell.getIcon());
//		setText(null);
////		setToolTipText(cell.getTooltip());
//		
//		FontKit.setToolTipText(this, cell.getTooltip());
//	}
//
//	/**
//	 * 只显示文本
//	 * @param font
//	 * @param cell
//	 */
//	private void doText(Font font, GraphTextCell cell) {
//		String text = cell.getText();
//		text = text.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;");
//		text = text.replaceAll("\\x20", "&nbsp;");
//		text = text.replaceAll("\\s+", "<BR>");
//		
//		String body = String.format(
//				"<html><font size=\"4\" face=\"%s\">%s</font></html>",
//				font.getName(), text);
//		
//		setIcon(null);
//		
////		setText(body); 
////		setToolTipText(cell.getTooltip());
//		
//		FontKit.setLabelText(this, body);
//		FontKit.setToolTipText(this, cell.getTooltip());
//		
//		super.setBorder(new EmptyBorder(0, 0, 15, 0));
//	}
//
//	/* (non-Javadoc)
//	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
//	 */
//	@Override
//	public Component getListCellRendererComponent(JList list, Object value,
//			int index, boolean isSelected, boolean cellHasFocus) {
//
//		setFont(list.getFont());
//		
//		// 检查传入对象，分别处理
//		if (value.getClass() == GraphIconCell.class) {
//			doIcon((GraphIconCell) value);
//		} else if (value.getClass() == GraphTextCell.class) {
//			doText(list.getFont(), (GraphTextCell) value);
//		} else {
//			return this;
//		}
//
////		// 设置背景和前景色
////		setBackground(Color.white);
////		setForeground(new Color(0x3e, 0x62, 0xad));
//		
//		// 设置背景和前景色
//		setBackground(textBackground);
//		setForeground(textForeground);
//
//		setEnabled(list.isEnabled());
//
//		setOpaque(true); // 这行一定要有，在刷新时使用
//		return this;
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see javax.swing.JLabel#updateUI()
//	 */
//	@Override
//	public void updateUI() {
//		loadColors();
//		super.updateUI();
//	}
//
//}