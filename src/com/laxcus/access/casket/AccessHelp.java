/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.casket;

/**
 * 数据存取辅助码
 * 
 * @author scott.liang
 * @version 1.0 11/02/2012
 * @since laxcus 1.0
 */
public final class AccessHelp {
	
	/** 辅助码对应标识 **/
	public final static int INSERT_FULL = 1;

	/**
	 * 判断INSERT填充“满”状态。
	 * @param who 标识符
	 * @return 成立返回“真”，否则“假”。
	 */
	public static boolean isInsertFull(int who) {
		return who == AccessHelp.INSERT_FULL;
	}

}