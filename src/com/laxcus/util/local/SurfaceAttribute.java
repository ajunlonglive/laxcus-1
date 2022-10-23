/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.local;

/**
 * XML标签属性
 * 
 * @author scott.liang
 * @version 1.0 9/21/2018
 * @since laxcus 1.0
 */
public class SurfaceAttribute implements Comparable<SurfaceAttribute>, Cloneable {

	/** 属性名 **/
	private String name;
	
	/** 属性值 **/
	private String value;
	
	/**
	 * 构造默认的XML标签属性
	 */
	private SurfaceAttribute() {
		super();
	}
	
	/**
	 * 构造XML标签属性，指定参数
	 * @param name XML标签属性名称
	 * @param value XML标签属性值
	 */
	public SurfaceAttribute(String name, String value) {
		this();
		setName(name);
		setValue(value);
	}
	
	/**
	 * 设置XML标签属性名称
	 * @param s
	 */
	public void setName(String s) {
		name = s;
	}

	/**
	 * 返回XML标签属性名称
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置XML标签属性值
	 * @param s
	 */
	public void setValue(String s) {
		value = s;
	}

	/**
	 * 返回XML标签属性值
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s - %s", name, value);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SurfaceAttribute that) {
		if (that == null) {
			return 1;
		}

		return name.compareTo(that.name);
	}

}