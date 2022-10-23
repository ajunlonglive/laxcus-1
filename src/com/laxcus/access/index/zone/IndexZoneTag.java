/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.zone;

/**
 * 列索引值区域类型 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/12/2011
 * @since laxcus 1.0
 */
public final class IndexZoneTag {

	/** 列索引值区域类型 **/
	public static final byte SHORT_ZONE = 1;
	public static final byte INTEGER_ZONE = 2;
	public static final byte LONG_ZONE = 3;
	public static final byte FLOAT_ZONE = 4;
	public static final byte DOUBLE_ZONE = 5;
	
	/**
	 * 判断是列索引值区域类型
	 * @param who 标识类型
	 * @return 返回“真”或者“假”
	 */
	public static boolean isIndexZone(byte who) {
		switch (who) {
		case IndexZoneTag.SHORT_ZONE:
		case IndexZoneTag.INTEGER_ZONE:
		case IndexZoneTag.LONG_ZONE:
		case IndexZoneTag.FLOAT_ZONE:
		case IndexZoneTag.DOUBLE_ZONE:
			return true;
		default:
			return false;
		}
	}
	
}
