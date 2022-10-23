/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.skin;

import java.awt.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.plaf.metal.*;

import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 箭头图标生成器
 * 
 * @author scott.liang
 * @version 1.0 7/11/2022
 * @since laxcus 1.0
 */
public class ArrowIcon {

	/**
	 * 生成一个前景颜色值
	 * @return Color实例
	 */
	public static Color createForeground() {
		// 默认是暗灰色
		Color color = null;
		// 高亮色
		if (Skins.isGraySkin()) {
			color = Color.GRAY;
		} else if (Skins.isDarkSkin()) {
			color = Color.LIGHT_GRAY;
		} else {
			color = MetalLookAndFeel.getWindowTitleBackground();
			ESL esl = new ESL(color);
			esl.brighter(50); // 调亮
			color = esl.toColor();
		}

		//			color = UIManager.getColor("Panel.background");
		//			if (color != null) {
		//				ESL esl = new ESL(color);
		//				esl.brighter(50);
		//				color = esl.toColor();
		//			} else {
		//				color = new Color(223, 223, 223);
		//			}

		if (color == null) {
			color = new Color(223, 223, 223);
		}

		return color;
	}

	/**
	 * 生成向上的箭头图标
	 * @param w 宽度
	 * @param h 高度
	 * @return 图像对象
	 */
	public static ImageIcon createUp(int w, int h) {
		// 生成一个透明的新图像
		BufferedImage buff = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = buff.createGraphics();
		buff = g2d.getDeviceConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
		Graphics g = buff.getGraphics();

		int midWidth = w / 2;
		if (w % 2 == 0) midWidth -= 1;
		int midHeight = h / 2;
		if (h % 2 == 0) midHeight -= 1;

		// 设置颜色
		g.setColor(createForeground());
		
		int startX = midWidth; // 从中间开始
		int startY = midHeight - midWidth / 2;
		if (startY < 0) startY = 0;

		int extend = 0;
		for (int y = startY; y < h; y++) {
			if (extend > midWidth) {
				break;
			}
			g.drawLine(startX - extend, y, startX + extend, y);
			extend++;
		}
		
		g.dispose();

		// 返回图标
		return new ImageIcon(buff);
	}

	/**
	 * 生成向下的箭头
	 * @param w 宽度
	 * @param h 高度
	 * @return 返回图像对象实例
	 */
	public static ImageIcon createDown(int w, int h) {
		// 生成一个透明的新图像
		BufferedImage buff = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = buff.createGraphics();
		buff = g2d.getDeviceConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
		Graphics g = buff.getGraphics();

		int midWidth = w / 2;
		if (w % 2 == 0) midWidth -= 1;
		int midHeight = h / 2;
		if (h % 2 == 0) midHeight -= 1;

		// 设置颜色
		g.setColor(createForeground());
		
		int startX = midWidth; // 从中间开始
		int startY = midHeight - midWidth / 2;
		if (startY < 0) startY = 0;
		
		int extend = midWidth;
		for (int y = startY; y < h; y++) {
			if (extend < 0) {
				break;
			}
			g.drawLine(startX - extend, y, startX + extend, y);
			extend--;
		}
		// 销毁
		g.dispose();

		// 返回图标
		return new ImageIcon(buff);
	}


}