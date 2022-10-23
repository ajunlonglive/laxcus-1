/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.color;

import java.awt.*;

import javax.swing.plaf.*;

/**
 * RGB颜色值
 * 
 * @author scott.liang
 * @version 1.0 3/25/2020
 * @since laxcus 1.0
 */
public class RGB implements Cloneable {

	/** 红色 **/
	public int red;

	/** 绿色 **/
	public int green;

	/** 兰色 **/
	public int blue;

	/**
	 * 构造RGB值
	 * @param that
	 */
	private RGB(RGB that) {
		super();
		red = that.red;
		green = that.green;
		blue = that.blue;
	}
	
	/**
	 * 构造默认的RGB颜色值
	 */
	public RGB() {
		super();
		red = green = blue = 0;
	}

	/**
	 * 构造RGB颜色值，指定参数
	 * @param red 红色
	 * @param green 绿色
	 * @param blue 兰色
	 */
	public RGB(int red, int green, int blue) {
		this();
		setRed(red);
		setGreen(green);
		setBlue(blue);
	}

	/**
	 * 构造RGB颜色值，指定颜色对象
	 * @param c 颜色
	 */
	public RGB(Color c) {
		this(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	/**
	 * 构造RGB颜色值，指定参数
	 * @param rgb RGB颜色值
	 */
	public RGB(int rgb) {
		this(new Color(rgb));
	}

	/**
	 * 返回红色
	 * @return
	 */
	public int getRed() {
		return red;
	}

	/**
	 * 设置红色，在0-255范围内
	 * @param v 颜色值
	 */
	public void setRed(int v) {
		if (v < 0) {
			red = 0;
		} else if (v > 255) {
			red = 255;
		} else {
			red = v;
		}
	}

	/**
	 * 返回绿色
	 * @return
	 */
	public int getGreen() {
		return green;
	}

	/**
	 * 设置绿色，在0-255范围内
	 * @param v 颜色值
	 */
	public void setGreen(int v) {
		if (v < 0) {
			green = 0;
		} else if (v > 255) {
			green = 255;
		} else {
			green = v;
		}
	}
	
	/**
	 * 返回兰色
	 * @return
	 */
	public int getBlue() {
		return blue;
	}

	/**
	 * 设置兰色，在0-255范围内
	 * @param v 颜色值
	 */
	public void setBlue(int v) {
		if (v < 0) {
			blue = 0;
		} else if (v > 255) {
			blue = 255;
		} else {
			blue = v;
		}
	}
	
	/**
	 * 转换成对应的ESL颜色对象
	 * @return 返回ESL对象
	 */
	public ESL toESL() {
		return ESLConverter.convert(this);
	}
	
	/**
	 * 返回RGB三原色值
	 * @return 返回真
	 */
	public int getRGB() {
		return toColor().getRGB();
	}
	
	/**
	 * 返回颜色对象
	 * @return
	 */
	public Color toColor() {
		return new Color(red, green, blue);
	}
	
	/**
	 * 返回HSL对象
	 * @return
	 */
	public HSL toHSL() {
		return HSLConverter.RGB2HSL(duplicate());
	}

	/**
	 * 返回颜色对象
	 * @return
	 */
	public ColorUIResource toColorUIResource() {
		return new ColorUIResource(red, green, blue);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RGB {" + red + "," + green + "," + blue + "}";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/**
	 * 生成副本
	 * @return
	 */
	public RGB duplicate() {
		return new RGB(this);
	}
	
}