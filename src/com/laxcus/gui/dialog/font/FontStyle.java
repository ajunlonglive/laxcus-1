/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gui.dialog.font;

import com.laxcus.util.*;

/**
 * 字体样式
 * 
 * @author scott.liang
 * @version 1.0 4/24/2022
 * @since laxcus 1.0
 */
class FontStyle implements Comparable<FontStyle> {
	
	/** 字体样式 **/
	int style;
	
	/** 字符样式名称 **/
	String name;

	/**
	 * 构造字体样式
	 * @param style 字体样式
	 * @param name 名称
	 */
	public FontStyle(int style, String name) {
		super();
		setStyle(style);
		setName(name);
	}
	
	/**
	 * 设置字体样式
	 * @param i
	 */
	public void setStyle(int i) {
		style = i;
	}
	
	/**
	 * 返回字体样式
	 * @return
	 */
	public int getStyle(){
		return style;
	}
	
	/**
	 * 设置名称
	 * @param s
	 */
	public void setName(String s) {
		name = s;
	}
	
	/**
	 * 返回名称
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object e) {
		if (e == null || e.getClass() != getClass()) {
			return false;
		} else if (e == this) {
			return true;
		}

		return compareTo((FontStyle) e) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return style;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FontStyle that) {
		if(that == null) {
			return 1;
		}
		return Laxkit.compareTo(style, that.style);
	}

}