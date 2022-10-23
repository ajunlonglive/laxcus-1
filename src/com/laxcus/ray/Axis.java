/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import java.awt.*;

import com.laxcus.util.*;

/**
 * 按纽坐标
 * 包括它的排列位置和屏幕位置
 * 
 * @author scott.liang
 * @version 1.0 5/18/2022
 * @since laxcus 1.0
 */
final class Axis implements Cloneable, Comparable<Axis> {

	/** 水平排列位置 **/
	int h;

	/** 垂直排列位置 **/
	int v;

	/** 屏幕坐标点 **/
	Point point;
	
	/**
	 * 生成按纽坐标
	 */
	private Axis() {
		super();
	}
	
	/**
	 * 构造按纽坐标
	 * @param h
	 * @param v
	 * @param p
	 */
	public Axis(int h, int v, Point p) {
		this();
		setH(h);
		setV(v);
		setPoint(p);
	}
	
	/**
	 * 构造按纽坐标
	 * @param h
	 * @param v
	 * @param x
	 * @param y
	 */
	public Axis(int h, int v, int x, int y) {
		this(h, v, new Point(x, y));
	}

	/**
	 * 构造按纽坐标副本
	 * @param that
	 */
	private Axis(Axis that) {
		this();
		h = that.h;
		v = that.v;
		point = new Point(that.point.x, that.point.y);
	}

	/**
	 * 设置水平排列位置，从0开始
	 * @param i
	 */
	public void setH(int i) {
		h = i;
	}

	/**
	 * 返回水平排列位置
	 * @return
	 */
	public int getH() {
		return h;
	}

	/**
	 * 设置垂直排列位，从0开始
	 * @param i
	 */
	public void setV(int i) {
		v = i;
	}

	/**
	 * 返回垂直排列位
	 * @return
	 */
	public int getV() {
		return v;
	}

	/**
	 * 设置屏幕坐标点
	 * @param p
	 */
	public void setPoint(Point p) {
		point = p;
	}

	/**
	 * 返回屏幕坐标点
	 * @return
	 */
	public Point getPoint() {
		return point;
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
	public Axis duplicate() {
		return new Axis(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d %d %d %d", h, v, point.x, point.y);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return h ^ v ^ point.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Axis that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(h, that.h);
		if (ret == 0) {
			ret = Laxkit.compareTo(v, that.v);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(point.x, that.point.x);
			if (ret == 0) {
				ret = Laxkit.compareTo(point.y, that.point.y);
			}
		}
		return ret;
	}

}