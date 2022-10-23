/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.relate;


import com.laxcus.command.*;

/**
 * 获取CALL站点成员命令钩子
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeCallItemHook extends CommandHook {

	/**
	 * 构造默认的获取CALL站点成员命令钩子
	 */
	public TakeCallItemHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return 获取CALL站点成员命令反馈结果
	 */
	public TakeCallItemProduct getProduct() {
		return (TakeCallItemProduct) super.getResult();
	}

}