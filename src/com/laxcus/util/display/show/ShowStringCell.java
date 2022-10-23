/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

import java.awt.*;

/**
 * 字符串单元
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowStringCell extends ShowItemCell {

	private static final long serialVersionUID = 5600710847953033883L;

	/** 字符串 **/
	private String value;

	/**
	 * 生成字符串单元的数据副本
	 * @param that TableStringCell实例
	 */
	private ShowStringCell(ShowStringCell that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的字符串单元
	 */
	private ShowStringCell() {
		super();
	}

	/**
	 * 构造字符串单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowStringCell(int index) {
		this();
		setIndex(index);
	}

	/**
	 * 构造字符串单元，指定索引下标和字符串
	 * @param index 索引下标
	 * @param value 字符串
	 */
	public ShowStringCell(int index, String value) {
		this(index);
		setValue(value);
	}

	/**
	 * 构造字符串单元，指定索引下标和字符串
	 * @param index 索引下标
	 * @param value 字符串
	 * @param tooltip 工具提示
	 */
	public ShowStringCell(int index, String value, String tooltip) {
		this(index, value);
		setTooltip(tooltip);
	}
	
	/**
	 * 构造字符串单元，指定索引下标和字符串
	 * @param index 索引下标
	 * @param value 字符串
	 * @param tooltip 工具提示
	 * @param foreground 前景色
	 */
	public ShowStringCell(int index, String value, String tooltip, Color foreground) {
		this(index, value, tooltip);
		setForeground(foreground);
	}
	
	/**
	 * 构造字符串单元，指定索引下标和字符串
	 * @param index 索引下标
	 * @param value 字符串
	 * @param foreground 前景色
	 */
	public ShowStringCell(int index, String value, Color foreground) {
		this(index, value);
		setForeground(foreground);
	}

	/**
	 * 构造字符串单元，指定下标和对象
	 * @param index 索引下标
	 * @param value 对象
	 */
	public ShowStringCell(int index, Object value) {
		this(index, (value == null ? "null" : value.toString()));
	}

	/**
	 * 构造字符串单元，指定索引下标、对象实例、前景颜色
	 * @param index 索引下标
	 * @param value 对象
	 * @param foreground 前景色
	 */
	public ShowStringCell(int index, Object value, Color foreground) {
		this(index, value);
		setForeground(foreground);
	}

	/**
	 * 构造字符串单元，指定索引下标、对象实例、前景颜色、背景颜色
	 * @param index 索引下标
	 * @param value 对象
	 * @param foreground 前景颜色
	 * @param background 背景颜色
	 */
	public ShowStringCell(int index, Object value, Color foreground, Color background) {
		this(index, value, foreground);
		setBackground(background);
	}

	/**
	 * 设置字符串
	 * @param e String实例
	 */
	public void setValue(String e) {
		value = e;
		setNullable(value == null);
	}

	/**
	 * 返回字符串
	 * @return String实例
	 */
	public String getValue() {
		return value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#compareTo(com.laxcus.util.display.show.ShowItemCell)
	 */
	@Override
	public int compareTo(ShowItemCell that) {
		int ret = super.compareTo(that);
		// 父类一致情况下，判断字符串一致
		if (ret == 0 && that.getClass() == ShowStringCell.class) {
			String next = ((ShowStringCell) that).value;
			ret = value.compareTo(next);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#duplicate()
	 */
	@Override
	public ShowStringCell duplicate() {
		return new ShowStringCell(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#visible()
	 */
	@Override
	public Object visible() {
		if (isNullable()) {
			return "null";
		}
		return value;
	}

}