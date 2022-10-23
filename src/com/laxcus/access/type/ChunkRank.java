/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.type;

/**
 * 存储块级别 <br>
 * 
 * 分为存储在DATA主站点上的主数据块，和存储在DATA从站点的从数据块。
 * 
 * @author scott.liang
 * @version 1.0 01/12/2016
 * @since laxcus 1.0
 */
public final class ChunkRank {

	/** 主块 **/
	public final static byte PRIME_CHUNK = 1;

	/** 从块 **/
	public final static byte SLAVE_CHUNK = 2;

	/**
	 * 判断数据块是主块
	 * @param who  级别标识
	 * @return  返回真或者假
	 */
	public static boolean isPrimeChunk(byte who) {
		return who == ChunkRank.PRIME_CHUNK;
	}

	/**
	 * 判断数据块是从块
	 * @param who  级别标识
	 * @return  返回真或者假
	 */
	public static boolean isSlaveChunk(byte who) {
		return who == ChunkRank.SLAVE_CHUNK;
	}

	/**
	 * 判断是合法类型
	 * @param who  数据块级别
	 * @return  返回真或者假
	 */
	public static boolean isFamily(byte who) {
		switch (who) {
		case ChunkRank.PRIME_CHUNK:
		case ChunkRank.SLAVE_CHUNK:
			return true;
		default:
			return false;
		}
	}
}