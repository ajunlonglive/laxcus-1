/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.boot;

/**
 * 数据位置 <br>
 * 
 * 文档或者图标的在JAR或者磁盘的位置 <br>
 * 
 * @author scott.liang
 * @version 1.0 7/4/2021
 * @since laxcus 1.0
 */
public class BootLocation {
	
	/** JAR包中的位置 **/
	private String JURI;
	
	/** 磁盘位置 **/
	private String URI;

	/**
	 * 设置数据位置
	 */
	public BootLocation() {
		super();
	}
	
	/**
	 * 设置文档在JAR包中的位置
	 * @param s
	 */
	public void setJURI(String s) {
		JURI = s;
	}

	/**
	 * 返回文档在JAR包中的位置
	 * @return
	 */
	public String getJURI() {
		return JURI;
	}
	
	/**
	 * 设置磁盘位置
	 * @param s
	 */
	public void setURI(String s) {
		URI = s;
	}

	/**
	 * 返回磁盘位置
	 * @return
	 */
	public String getURI() {
		return URI;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (JURI != null) {
			return String.format("JURI: %s", JURI);
		} else if (URI != null) {
			return String.format("URI: %s", URI);
		} else {
			return "";
		}
	}

}