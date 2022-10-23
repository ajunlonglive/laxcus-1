/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.splash;

import java.awt.*;

import javax.swing.*;

/**
 * 启动封面面板
 * 
 * @author scott.liang
 * @version 1.0 6/13/2022
 * @since laxcus 1.0
 */
class SplashPanel extends JPanel {

	private static final long serialVersionUID = -528658760781910238L;

	/** 图片 **/
	private Image image;

	/**
	 * 构造启动封面面板
	 */
	public SplashPanel() {
		super();
	}

	/**
	 * 构造启动封面面板，指定面板图片
	 * @param image
	 */
	public SplashPanel(Image image) {
		this();
		setImage(image);
	}

	/**
	 * 设置背景图片
	 * @param img
	 */
	public void setImage(Image img) {
		if (img != null) {
			image = img;
			int width = image.getWidth(null);
			int height = image.getHeight(null);

			Dimension d = new Dimension(width, height);
			setPreferredSize(d);
			setSize(d);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JInternalFrame#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// 有图片
		if (image != null) {
			//			Color old = g.getColor();

			int width = getWidth();
			int height = getHeight();
			// 图片尺寸
			int imgWidth = image.getWidth(null);
			int imgHeight = image.getHeight(null);

			//			g.setColor(Color.BLUE);
			//			g.fillRect(0, 0, width, height);

			// 以全屏方式绘制图像
			//			g.drawImage(image, 0, 0, width - 1, height - 1, 0, 0, imgWidth - 1, imgHeight - 1, null);

			//			g.drawImage(image, 0, 0, width - 1, height - 1, 0, 0, imgWidth, imgHeight, null);

			//			g.drawImage(image, 0, 0, width, height, 0, 0, imgWidth - 1, imgHeight - 1, null);

			g.drawImage(image, 0, 0, width, height, 0, 0, imgWidth, imgHeight, null);

			//			g.drawImage(image, 0, 0, imgWidth, imgHeight, null);

			//			// 重置
			//			g.setColor(old);
		}
	}


}