/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

/**
 * 前端任务返回结果类型
 * 
 * @author scott.liang
 * @version 1.0 2/4/2018
 * @since laxcus 1.0
 */
public final class MissionResultTag {

	/** 缓存类型 **/
	public final static byte BUFFER = 1;

	/** 磁盘文件类型 **/
	public final static byte FILE = 2;

	/** 报告类型  **/
	public final static byte PRODUCT = 3;
	
	/** 除此之外的对象 **/
	public final static byte OBJECT = 4;

	/**
	 * 将前端任务类型数字描述翻译为字符串描述 
	 * @param who 前端任务类型
	 * @return 站点字符串描述
	 */
	public static String translate(byte who) {
		switch (who) {
		case MissionResultTag.BUFFER:
			return "Buffer";
		case MissionResultTag.FILE:
			return "File";
		case MissionResultTag.PRODUCT:
			return "Product";
		case MissionResultTag.OBJECT:
			return "Object";
		}
		return "NONE";
	}

	/**
	 * 判断是合法的前端任务类型
	 * @param who 前端任务类型
	 * @return 返回真或者假
	 */
	public static boolean isFamily(byte who) {
		switch(who) {
		case MissionResultTag.BUFFER:
		case MissionResultTag.FILE:
		case MissionResultTag.PRODUCT:
		case MissionResultTag.OBJECT:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 判断是缓存类型
	 * @param who 前端任务类型
	 * @return 返回真或者假
	 */
	public static boolean isBuffer(byte who) {
		return who == MissionResultTag.BUFFER;
	}

	/**
	 * 判断是磁盘文件类型
	 * @param who 前端任务类型
	 * @return 返回真或者假
	 */
	public static boolean isFile(byte who) {
		return who == MissionResultTag.FILE;
	}

	/**
	 * 判断是报告类型
	 * @param who 前端任务类型
	 * @return 返回真或者假
	 */
	public static boolean isProduct(byte who) {
		return who == MissionResultTag.PRODUCT;
	}

	/**
	 * 判断是对象类型
	 * @param who 前端任务类型
	 * @return 返回真或者假
	 */
	public static boolean isObject(byte who) {
		return who == MissionResultTag.OBJECT;
	}

}