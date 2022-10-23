/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.component;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.*;

import com.laxcus.util.*;
import com.laxcus.util.color.*;
import com.laxcus.util.skin.*;

/**
 * 密码文本域
 * 
 * @author scott.liang
 * @version 1.0 10/20/2021
 * @since laxcus 1.0
 */
public class FlatPasswordField extends JPasswordField {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public FlatPasswordField() {
		super();
		init();
	}

	/**
	 * @param arg0
	 */
	public FlatPasswordField(String arg0) {
		super(arg0);
		init();
	}

	/**
	 * @param arg0
	 */
	public FlatPasswordField(int arg0) {
		super(arg0);
		init();
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public FlatPasswordField(String arg0, int arg1) {
		super(arg0, arg1);
		init();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public FlatPasswordField(Document arg0, String arg1, int arg2) {
		super(arg0, arg1, arg2);
		init();
	}

//	/** 暗色调整值 **/
//	private final static int DRAK_VALUE = 80;
//
//	/** 亮色调整值 **/
//	private final static int LIGHT_VALUE = 50;

	/** 厚度，最小是1 **/
	private int thickness;
	
	/** 如果是圆角，这是孤度数，默认是6 **/
	private int arc;

	/** 圆角 **/
	private boolean roundedCorners;
	
	/** 
	 * 说明
	 * ARC:6, THICKNESS: 3 具有明显的孤形，适用在窗口上
	 * ARC:4, THICKNESS: 2/1, 有孤形，但是不明显，适合用在窗口组件，如按纽上
	 * ARC:6, THICKNESS: 1, 孤形明显，适合用在窗口组件，如按纽上
	 * 
	 */
	
	/**
	 * 初始化参数
	 */
	private void init() {
		// thickness = 8;
		// roundedCorners = false;

		setRoundedCorners(true);
		setArc(4);
		setThickness(1);
	}

	/**
	 * 返回孤度
	 * @return
	 */
	public int getArc() {
		return arc;
	}
	
	/**
	 * 返回厚度
	 * @return
	 */
	public int getThickness() {
		return thickness;
	}

	/**
	 * 判断是圆角
	 * @return
	 */
	public boolean isRoundedCorners() {
		return roundedCorners;
	}

	/**
	 * 设置孤度，最小是1
	 * @param i
	 */
	public void setArc(int i) {
		if (i > 0) {
			arc = i;
		}
	}
	
	public void setThickness(int i) {
		if (i > 0) {
			thickness = i;
		}
	}

	/**
	 * 设置为圆角
	 * @param b
	 */
	public void setRoundedCorners(boolean b) {
		roundedCorners = b;
	}

	/**
	 * 判断是NIMBUS界面
	 * 
	 * @return 返回真或者假
	 */
	public boolean isNimbusUI() {
		return GUIKit.isNimbusUI();
	}

	/**
	 * 判断是METAL界面
	 * 
	 * @return
	 */
	public boolean isMetalUI() {
		return GUIKit.isMetalUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.AbstractPasswordField#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
		// 有效状态
		if (isMetalUI()) {
			int width = getWidth();
			int height = getHeight();
			
			if (isEnabled()) {
				paintMetalDarkBorder(this, g, 0, 0, width, height);
			} else {
				paintMetalLightBorder(this, g, 0, 0, width, height);
			}
		} else {
			super.paintBorder(g);
		}
	}
	
//	/**
//	 * 绘制边框
//	 * 
//	 * @param color
//	 * @param g
//	 * @param x
//	 * @param y
//	 * @param width
//	 * @param height
//	 */
//	private void paintBorder(Color color, Graphics g, int x, int y, int width, int height) {
//		Color oldColor = g.getColor();
//
//		g.setColor(color);
//		
//		// 圆角
//		if (roundedCorners) {
//			for (int i = 0; i < thickness; i++) {
//				g.drawRoundRect(x + i, y + i, width - (i * 2) - 1, height - (i * 2) - 1, arc, arc);
//			}
//		} else {
//			g.drawRect(x, y, width - 1, height - 1);
//		}
//		
//		g.setColor(oldColor);
//	}
	
	/**
	 * 绘制边框
	 * 
	 * @param color
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void paintBorder(Color color, Graphics g, int x, int y, int width, int height) {
		Color oldColor = g.getColor();

		g.setColor(color);
		g.drawRect(x, y, width - 1, height - 1);

		g.setColor(oldColor);
	}
	
	/**
	 * 绘制METAL界面的阳刻浮雕效果
	 * 
	 * @param c
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void paintMetalLightBorder(Component c, Graphics g, int x, int y,
			int width, int height) {

		Color color = UIManager.getColor("PasswordField.background");
		if (color == null) {
			color = c.getBackground();
		}
		ESL esl = new RGB(color).toESL();
		// 灰色的时候，颜色变暗，否则是变亮
		if (Skins.isGraySkin()) {
			color = esl.toDraker(FlatTextField.DRAK_VALUE).toColor();
		} else {
			color = esl.toBrighter(FlatTextField.LIGHT_VALUE).toColor();
		}
		paintBorder(color, g, x, y, width, height);
		
//		Color light = esl.toBrighter(FlatPasswordField.LIGHT_VALUE).toColor();
//		paintBorder(light, g, x, y, width, height);
	}

	/**
	 * 绘制METAL界面的阴刻浮雕效果
	 * 
	 * @param c
	 * @param g
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void paintMetalDarkBorder(Component c, Graphics g, int x, int y,
			int width, int height) {
		// 取组件的背景色
		Color color = UIManager.getColor("PasswordField.background");
		if (color == null) {
			color = c.getBackground();
		}
		ESL esl = new RGB(color).toESL();
		// 灰色的时候，颜色变暗，否则是变亮
		if (Skins.isGraySkin()) {
			color = esl.toDraker(FlatTextField.DRAK_VALUE).toColor();
		} else {
			color = esl.toBrighter(FlatTextField.LIGHT_VALUE).toColor();
		}
		paintBorder(color, g, x, y, width, height);
		
//		Color dark = esl.toDraker(FlatPasswordField.DRAK_VALUE).toColor();
//		paintBorder(dark, g, x, y, width, height);
	}

}
