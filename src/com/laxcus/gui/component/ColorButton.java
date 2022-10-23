/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.component;

import java.awt.*;

import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 颜色按纽
 * 
 * @author scott.liang
 * @version 1.0 6/16/2021
 * @since laxcus 1.0
 */
public class ColorButton extends FlatButton {

	private static final long serialVersionUID = -3999726215838385971L;

	/** 颜色 **/
	private Color color;

	/**
	 * 构造默认的颜色按纽
	 */
	public ColorButton() {
		super();
	}
	
	/**
	 * 构造颜色按纽
	 * @param c 颜色值
	 */
	public ColorButton(Color c) {
		super();
		setColor(c);
	}

	/**
	 * 设置颜色，允许空指针
	 * @param c
	 */
	public void setColor(Color c) {
		color = c;
		// 重新绘制颜色
		repaint();
	}

	/**
	 * 返回颜色
	 * @return
	 */
	public Color getColor() {
		return color;
	}
	
//	/**
//	 * 判断是NIMBUS界面
//	 * @return
//	 */
//	protected boolean isNimbusUI() {
//		LookAndFeel laf = UIManager.getLookAndFeel();
//		if (laf == null) {
//			return false;
//		}
//		String name = laf.getID();
//		return (name != null && name.equalsIgnoreCase("Nimbus"));
//	}
	
	/**
	 * 返回边框的高亮颜色，区别选择的颜色
	 * @return
	 */
	private Color getLightColor() {
		if (isNimbusUI() || Skins.isGraySkin()) {
			return Color.BLACK;
		} else {
			Color c = getBackground();
			ESL esl = new RGB(c).toESL();
			return esl.toBrighter(50).toColor();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// 背景色
		Color back = g.getColor();

//		// 左侧坐标
//		int x1 = 8;
//		int y1 = 8;
		// 左侧坐标
		int x1 = 6;
		int y1 = 6;
		// 宽度
		int width = (getWidth() - x1 * 2);
		int height = (getHeight() - y1 * 2);
		// 右侧坐标
		int x2 = x1 + width - 1;
		int y2 = y1 + height - 1;
		
		Color light = getLightColor();

		// 背景色
		g.setColor(color == null ? Color.WHITE : color);
		g.fillRect(x1, y1, width, height);

		// 黑色边框线
		g.setColor(light);
		g.drawLine(x1, y1, x2, y1); // 上横线
		g.drawLine(x1, y2, x2, y2); // 下横线
		
		g.drawLine(x1, y1, x1, y2); // 左侧线
		g.drawLine(x2, y1, x2, y2); // 右侧线
		
		// 恢复原来的颜色
		g.setColor(back);
	}

}