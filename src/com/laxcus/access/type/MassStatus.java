/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.type;

/**
 * 数据块状态，兼容JNI C接口。 <br><br>
 * 
 * 数据块状态分为3种：<br>
 * 1. 缓存块（CACHE，未封闭）<br>
 * 2. 存储块（CHUNK，封闭）<br>
 * 3. 缓存映像块（CACHE REFLEX，缓存块在DATA从节点的快照）<br><br>
 * 
 * @author scott.liang
 * @version 1.0 01/12/2016
 * @since laxcus 1.0
 */
public class MassStatus {

	/** 缓存块 **/
	public final static byte CACHE = 1;

	/** 存储块 **/
	public final static byte CHUNK = 2;

	/** 缓存映像块 **/
	public final static byte CACHEREFLEX = 3;

	/**
	 * 判断是缓存块状态（DATA主节点的数据块未封闭状态）
	 * @param who 数据块状态标识
	 * @return  返回真或者假
	 */
	public static boolean isCache(byte who) {
		return who == MassStatus.CACHE;
	}

	/**
	 * 判断是存储块状态（DATA主/从节点的数据块封闭状态）
	 * @param who 数据块状态标识
	 * @return  返回真或者假
	 */
	public static boolean isChunk(byte who) {
		return who == MassStatus.CHUNK;
	}

	/**
	 * 判断是缓存块映像状态（保存在DATA从节点的缓存块）
	 * @param who 数据块状态标识
	 * @return  返回真或者假
	 */
	public static boolean isCacheReflex(byte who) {
		return who == MassStatus.CACHEREFLEX;
	}

	/**
	 * 判断是数据块状态
	 * @param who 数据块状态
	 * @return 返回真或者假
	 */
	public static boolean isFamily(byte who) {
		switch (who) {
		case MassStatus.CACHE:
		case MassStatus.CHUNK:
		case MassStatus.CACHEREFLEX:
			return true;
		default:
			return false;
		}
	}
}
