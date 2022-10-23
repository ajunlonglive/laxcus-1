/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

import java.awt.*;
import java.io.*;

import javax.swing.plaf.*;

import com.laxcus.util.*;

/**
 * 表数据单元 <br>
 * 
 * 在概念上对应数据库的“列（Column）”
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public abstract class ShowItemCell implements Serializable, Cloneable, Comparable<ShowItemCell> {

	private static final long serialVersionUID = 5725112713056367262L;

	/** 索引号，与标题的索引号对应 **/
	private int index;

	/** 空值，默认是“真” **/
	private boolean nullable;
	
	/** 前景颜色 **/
	private Color foreground;
	
	/** 背景颜色 **/
	private Color background;
	
	/** 可编辑，默认是假 **/
	private boolean editable;
	
	/** 提示文本 **/
	private String tooltip;
	
	/** 符号对象 **/
	private Object symbol;

	/**
	 * 构造默认的表数据单元
	 */
	protected ShowItemCell() {
		super();
		setNullable(true);
		setEditable(false);
	}

	/**
	 * 生成数据副本实例
	 * @param that ShowItemCell实例
	 */
	protected ShowItemCell(ShowItemCell that) {
		this();
		index = that.index;
		nullable = that.nullable;
		foreground = that.foreground;
		background = that.background;
		tooltip = that.tooltip;
		symbol = that.symbol;
	}

	/**
	 * 设置下标
	 * @param i 下标编号
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * 返回下标
	 * @return 下标编号
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * 设置空值
	 * @param b 空值
	 */
	public void setNullable(boolean b) {
		nullable = b;
	}

	/**
	 * 判断是空值
	 * @return 返回真或者假
	 */
	public boolean isNullable() {
		return nullable;
	}

	/**
	 * 设置单元的前景色。
	 * 特别注意！！！不能够是ColorUIResource实例，否则JVM拒绝正确显示！
	 * @param e 颜色实例
	 */
	public void setForeground(Color e) {
		if (e != null && Laxkit.isClassFrom(e, ColorUIResource.class)) {
			foreground = new Color(e.getRGB());
		} else {
			foreground = e;
		}
	}

	/**
	 * 返回单元的前景色
	 * @return 颜色实例
	 */
	public Color getForeground() {
		return foreground;
	}

	/**
	 * 设置单元的背景颜色
	 * 特别注意！！！不能够是ColorUIResource实例，否则JVM拒绝正确显示！
	 * @param e 颜色实例
	 */
	public void setBackground(Color e) {
		if (e != null && Laxkit.isClassFrom(e, ColorUIResource.class)) {
			background = new Color(e.getRGB());
		} else {
			background = e;
		}
	}

	/**
	 * 返回单元的背景颜色
	 * @return 颜色实例
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * 设置编辑模式
	 * @param b 真或者假
	 */
	public void setEditable(boolean b) {
		editable = b;
	}

	/**
	 * 判断可以编辑
	 * @return 返回真或者假
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * 设置提示文本
	 * @param e 提示文本
	 */
	public void setTooltip(String e) {
		tooltip = e;
	}

	/**
	 * 返回提示文本
	 * @return 提示文本
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * 设置一个符号对象。符号对象做为一个隐性参数存在，不显示！
	 * @param e 符号对象
	 */
	public void setSymbol(Object e) {
		symbol = e;
	}

	/**
	 * 返回符号对象
	 * @return
	 */
	public Object getSymbol() {
		return symbol;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ShowItemCell) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return index;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getName();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ShowItemCell that) {
		if(that == null) {
			return 1;
		}
		return Laxkit.compareTo(index, that.index);
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
	 * 返回子类实例的浅层数据副本
	 * @return ShowItemCell子类实例
	 */
	public abstract ShowItemCell duplicate();
	
	/**
	 * 将成员参数格式化为一个可视对象输出
	 * @return Object子类实例
	 */
	public abstract Object visible();

}