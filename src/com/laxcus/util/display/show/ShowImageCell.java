/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

import javax.swing.*;

/**
 * 图像单元，包括文本。
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowImageCell extends ShowItemCell {
	
	private static final long serialVersionUID = 8586194064562202843L;

	/** 字符串 **/
	private String text;

	/** 图像 **/
	private Icon icon;
	
	/**
	 * 生成IMAGE类型单元的数据副本
	 * @param that TableImageCell实例
	 */
	private ShowImageCell(ShowImageCell that) {
		super(that);
		icon = that.icon;
		text = that.text;
	}

	/**
	 * 构造默认的DATE类型单元
	 */
	private ShowImageCell() {
		super();
		icon = null;
	}

	/**
	 * 构造DATE类型单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowImageCell(int index) {
		this();
		setIndex(index);
	}
	
	/**
	 * 构造DATE类型单元，指定下标和图像对象
	 * @param index 索引下标
	 * @param icon 图像对象
	 */
	public ShowImageCell(int index, Icon icon) {
		this(index);
		setIcon(icon);
	}
	
	/**
	 * 构造DATE类型单元，指定下标和图像对象
	 * @param index 索引下标
	 * @param icon 图像对象
	 */
	public ShowImageCell(int index, Icon icon, String text) {
		this(index, icon);
		setText(text);
	}
	
	/**
	 * 设置字符串
	 * @param e String实例
	 */
	public void setText(String e) {
		text = e;
	}

	/**
	 * 返回字符串
	 * @return String实例
	 */
	public String getText() {
		return text;
	}


	/**
	 * 设置图像对象
	 * @param e Icon实例
	 */
	public void setIcon(Icon e) {
		icon = e;
		setNullable(icon == null);
	}
	
	/**
	 * 返回图像对象
	 * @return Icon实例
	 */
	public Icon getIcon() {
		return icon;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#duplicate()
	 */
	@Override
	public ShowItemCell duplicate() {
		return new ShowImageCell(this);
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
		return icon;
	}

}