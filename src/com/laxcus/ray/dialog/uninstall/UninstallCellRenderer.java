/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dialog.uninstall;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.laxcus.application.manage.*;
import com.laxcus.util.display.*;
import com.laxcus.util.skin.*;

/**
 * 桌面背景图像单元
 * 
 * @author scott.liang
 * @version 1.0 6/17/2021
 * @since laxcus 1.0
 */
class UninstallCellRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = -2627998675618919558L;

//	/** 其他图标 **/
//	private Icon noneIcon, jpegIcon, pngIcon, gifIcon; // , otherIcon;

	/** 选中前景/背景颜色 **/
	private Color textSelectForeground, textSelectBackground;

	/** 没选中前景/背景颜色 **/
	private Color textForeground, textBackground;

	/**
	 * 构造桌面背景图像单元
	 */
	public UninstallCellRenderer(){
		super();
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		loadTextColor();
		// loadIcons();
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
	
//	private void loadIcons() {
//		this.gifIcon = UIManager.getIcon("PropertiesDialog.backgroundGifIcon");
//		this.jpegIcon = UIManager.getIcon("PropertiesDialog.backgroundJpegIcon");
//		this.pngIcon = UIManager.getIcon("PropertiesDialog.backgroundPngIcon");
//		this.noneIcon = UIManager.getIcon("PropertiesDialog.backgroundNoneIcon");
//	}
	

	/*
	 * (non-Javadoc)
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
		if (value.getClass() != WRoot.class) {
			setIcon(null);
			setText("");
			return this;
		}
		
		setFont(list.getFont());

		WRoot item = (WRoot) value;

//		System.out.printf("%s GIF:%s, PNG:%s, JPEG:%s, Other:%s, None:%s \n", 
//				item.toString(), item.isGIF(), item.isPNG(), item.isJPEG(), item.isOther(), item.isNone());

		// 设置图标
//		if (item.isNone()) {
//			setIcon(noneIcon);
//		} 

//		if (item.isGIF()) {
//			setIcon(gifIcon);
//		} else if (item.isJPEG()) {
//			setIcon(jpegIcon);
//		} else if (item.isPNG()) {
//			setIcon(pngIcon);
//		} else {
//			setIcon(noneIcon);
//		}
		
		WElement element = item.getElement();

		// 设置标签字体
		FontKit.setLabelText(this, element.getTitle()); // item.getComment());
		FontKit.setToolTipText(this, element.getToolTip()); // item.toString());
		
		// 显示图标
		ImageIcon icon = element.getIcon();
		if (icon != null) {
			if (icon.getIconHeight() == 32 && icon.getIconWidth() == 32) {
				setIcon(icon);
			} else {
				ImageIcon image = ImageUtil.scale(icon.getImage(), 32, 32, true);
				setIcon(image);
			}
		}

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
		setBorder(new EmptyBorder(4, 4, 4, 4));
		setEnabled(list.isEnabled());
		
		return this;
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

}