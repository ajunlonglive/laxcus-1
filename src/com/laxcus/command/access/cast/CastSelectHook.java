/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.cast;

import com.laxcus.command.*;

/**
 * CastSelect命令钩子
 * 
 * @author scott.liang
 * @version 1.0 3/23/2013
 * @since laxcus 1.0
 */
public class CastSelectHook extends CommandHook {

	/**
	 * 建立CastSelect命令钩子
	 */
	public CastSelectHook() {
		super();
	}

	/**
	 * 返回检索数据
	 * @return 字节数组
	 */
	public byte[] getData() {
		return (byte[]) super.getResult();
	}

}