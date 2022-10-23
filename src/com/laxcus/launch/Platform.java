/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

/**
 * 操作系统类型
 * 
 * @author scott.liang
 * @version 1.0 7/21/2012
 * @since laxcus 1.0
 */
public enum Platform {

	NONE(0), LINUX(1), WINDOWS(2);

	/**
	 * 系统类型
	 */
	private int type = 0;

	/**
	 * 定义系统平台
	 * @param i 系统平台类型
	 */
	private Platform(int i) {
		type = i;
	}

	/**
	 * 返回操作系统名称
	 * @return “LINUX”、“WINDOWS”、“NONE”三者之一。
	 */
	public String type() {
		switch(type){
		case 1:
			return "Linux";
		case 2:
			return "Windows";
		default:
			return "NONE";
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return type();
	}

}
