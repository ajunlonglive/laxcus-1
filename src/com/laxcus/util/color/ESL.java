/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.color;

import java.awt.Color;

/**
 * ESL值
 * 
 * ESL是色彩（色调）、饱和度、亮度的组合
 * 
 * @author scott.liang
 * @version 1.0 3/27/2020
 * @since laxcus 1.0
 */
public class ESL implements Cloneable {

	/** 色彩 **/
	private double h;

	/** 饱和度 */
	private double s;

	/** 深度（亮度） */
	private double l;


	/**
	 * 构造ESL值
	 * @param that
	 */
	private ESL(ESL that) {
		super();
		h = that.h;
		s = that.s;
		l = that.l;
	}

	/**
	 * 构造默认的ESL
	 */
	public ESL() {
		super();
		h = s = l = 0;
	}

	/**
	 * 构造ESL，指定参数
	 * @param h 色彩
	 * @param s 饱和度
	 * @param l 深度
	 */
	public ESL(double h, double s, double l) {
		this();
		setH(h);
		setS(s);
		setL(l);
	}

	/**
	 * 构造ESL，指定颜色
	 * @param c
	 */
	public ESL(RGB rgb) {
		this();
		ESL esl = rgb.toESL();
		setH(esl.getH());
		setS(esl.getS());
		setL(esl.getL());
	}

	/**
	 * 构造ESL，指定颜色
	 * @param c
	 */
	public ESL(Color c) {
		this(new RGB(c));
	}

	/**
	 * 返回色彩
	 * @return
	 */
	public double getH() {
		return h;
	}

	/**
	 * 设置H值，最大239
	 * @param v
	 */
	public void setH(double v) {
		if (v < 0) {
			h = 0;
		} else if (v > 239) {
			h = 239;
		} else {
			h = v;
		}
	}

	/**
	 * 返回饱和度
	 * @return
	 */
	public double getS() {
		return s;
	}

	/**
	 * 设置饱和度，最大240
	 * @param v
	 */
	public void setS(double v) {
		if (v < 0) {
			s = 0;
		} else if (v > 240) {
			s = 240;
		} else {
			s = v;
		}
	}

	/**
	 * 返回亮度
	 * @return
	 */
	public double getL() {
		return l;
	}

	/**
	 * 设置亮度，最大240
	 * @param v
	 */
	public void setL(double v) {
		if (v < 0) {
			l = 0;
		} else if (v > 240) {
			l = 240;
		} else {
			l = v;
		}
	}

	/**
	 * 转换成对应的RGB颜色对象
	 * @return RGB对象实例
	 */
	public RGB toRGB() {
		return ESLConverter.convert(this);
	}

	/**
	 * 返回颜色对象
	 * @return
	 */
	public Color toColor() {
		return toRGB().toColor();
	}

	/**
	 * 调亮颜色
	 * @param flag 标记值，增加的幅度
	 */
	public void brighter(double flag) {
		l += flag;
		if (l > 240) {
			l = 240;
		}
	}

	/**
	 * 调整成亮色对象
	 * @param flag
	 * @return
	 */
	public ESL toBrighter(double flag) {
		ESL esl = duplicate();
		esl.brighter(flag);
		return esl;
	}

	/**
	 * 调暗颜色
	 * @param flag 标记值，降低的幅度
	 */
	public void darker(double flag) {
		l -= flag;
		if (l < 0) {
			l = 0;
		}
	}

	/**
	 * 调整成暗色对象
	 * @param flag
	 * @return
	 */
	public ESL toDraker(double flag) {
		ESL esl = duplicate();
		esl.darker(flag);
		return esl;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ESL {" + h + ", " + s + ", " + l + "}";
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
	public ESL duplicate() {
		return new ESL(this);
	}

}