/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.properties;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.util.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 桌面背景图像单元
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
class BackgroundImageCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -2627998675618919558L;

	/** 其他图标 **/
	private Icon noneIcon, jpegIcon, pngIcon, gifIcon; 

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/**
	 * 构造桌面背景图像单元
	 */
	public BackgroundImageCellRenderer(){
		super();
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		loadTextColor();
		loadIcons();
		setHorizontalAlignment(SwingConstants.LEFT);
		setVerticalAlignment(SwingConstants.CENTER);
		setIconTextGap(6);
		setOpaque(true);
	}

	/**
	 * 加载颜色
	 */
	private void loadTextColor() {
		textSelectForeground = Skins.findListTextSelectForeground();
		textSelectBackground = Skins.findListTextSelectBackground();
		textForeground = Skins.findListForeground();
		textBackground = Skins.findListBackground();
	}

	//	/**
	//	 * 加载图标
	//	 */
	//	private void loadIcons2() {
	//		ResourceLoader loader = new ResourceLoader("conf/ray/image/window/environment");
	//		noneIcon = loader.findImage("none.png");
	//		jpegIcon = loader.findImage("jpeg.png");
	//		pngIcon = loader.findImage("png.png");
	//		gifIcon = loader.findImage("gif.png");
	//		otherIcon = loader.findImage("other.png");
	//	}

	private void loadIcons() {
		gifIcon = UIManager.getIcon("PropertiesDialog.backgroundGifIcon");
		jpegIcon = UIManager.getIcon("PropertiesDialog.backgroundJpegIcon");
		pngIcon = UIManager.getIcon("PropertiesDialog.backgroundPngIcon");
		noneIcon = UIManager.getIcon("PropertiesDialog.backgroundNoneIcon");
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JLabel#updateUI()
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		loadTextColor();
		FontKit.setDefaultFont(this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		//		if (value == null) {
		//			setIcon(null);
		//			setText("");
		//			return this;
		//		}
		//		if (value.getClass() != BackgroundImageItem.class) {
		//			setIcon(null);
		//			setText("");
		//			return this;
		//		}

		boolean success = (value != null && value.getClass() == BackgroundImageItem.class);
		if (!success) {
			setIcon(null);
			setText("");
			return this;
		}

		setFont(list.getFont());

		BackgroundImageItem item = (BackgroundImageItem) value;

		//		System.out.printf("%s GIF:%s, PNG:%s, JPEG:%s, Other:%s, None:%s \n", 
		//				item.toString(), item.isGIF(), item.isPNG(), item.isJPEG(), item.isOther(), item.isNone());

		// 设置图标
		//		if (item.isNone()) {
		//			setIcon(noneIcon);
		//		} 

		// 设置图标
		Icon icon = item.getIcon();
		if (icon != null) {
			setIcon(icon);
		} else {
			if (item.isGIF()) {
				setIcon(gifIcon);
			} else if (item.isJPEG()) {
				setIcon(jpegIcon);
			} else if (item.isPNG()) {
				setIcon(pngIcon);
			} else {
				setIcon(noneIcon);
			}
		}

		// 设置标签字体
//		FontKit.setLabelText(this, item.getComment());

		String text = item.getComment();
		if (text == null) {
			text = "";
		}
		setText(text);
			
		text =	item.toString();
		// 图片长度
		long length = item.getLength();
		if (length > 0) {
			String ms = ConfigParser.splitCapacity(length);
			text = String.format("%s %s", text, ms);
		}
		// 图片尺寸
		int width = item.getWidth();
		int height = item.getHeight();
		if (width > 0 && height > 0) {
			text = String.format("%s %dx%d", text, width, height);
		}
		// 显示提示
		setToolTipText(text);
		
//		FontKit.setToolTipText(this, text);

		// 显示颜色
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

		// top, left, bottom, right
		setBorder(new EmptyBorder(2, 2, 2, 2));
		return this;
	}

}
