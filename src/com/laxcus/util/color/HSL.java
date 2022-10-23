/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.color;

/**
 * HSL颜色
 * 
 * 分为：色调（色相）、饱和度、亮度
 * 
 * Hue、Saturation、Lightness/Brightness
 * 
 * @author scott.liang
 * @version 1.0 6/20/2021
 * @since laxcus 1.0
 */
public final class HSL {

	/** 色调. HUE **/
	private float h = 0;

	/** 饱和度. SATURATION */
	private float s = 0;

	/** 亮度. LIGHTNESS */
	private float l = 0;

	/**
	 * 构造默认的HSL颜色
	 */
	public HSL() {
		super();
	}

	/**
	 * 设置HSL颜色
	 * @param that HSL颜色
	 */
	public HSL(HSL that) {
		this();
		h = that.h;
		s = that.s;
		l = that.l;
	}
	
	/**
	 * 构造HSL颜色，指定参数
	 * @param h
	 * @param s
	 * @param l
	 */
	public HSL(float h, float s, float l) {
		this();
		setH(h);
		setS(s);
		setL(l);
	}

	/**
	 * 返回色调
	 * @return
	 */
	public float getH() {
		return h;
	}

	/**
	 * 设置色调
	 * @param who
	 */
	public void setH(float who) {
		if (who < 0) {
			h = 0;
		} else if (who > 360) {
			h = 360;
		} else {
			h = who;
		}
	}

	/**
	 * 返回饱和度
	 * @return
	 */
	public float getS() {
		return s;
	}

	/**
	 * 设置饱和度
	 * @param who
	 */
	public void setS(float who) {
		if (who < 0) {
			s = 0;
		} else if (who > 255) {
			s = 255;
		} else {
			s = who;
		}
	}

	/**
	 * 返回亮度
	 * @return
	 */
	public float getL() {
		return l;
	}

	/**
	 * 设置亮度
	 * @param who
	 */
	public void setL(float who) {
		if (who < 0) {
			l = 0;
		} else if (who > 255) {
			l = 255;
		} else {
			l = who;
		}
	}
	
	/**
	 * 返回RGB值
	 * @return
	 */
	public RGB toRGB() {
		return HSLConverter.HSL2RGB(duplicate());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HSL {" + h + ", " + s + ", " + l + "}";
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
	public HSL duplicate() {
		return new HSL(this);
	}

}
