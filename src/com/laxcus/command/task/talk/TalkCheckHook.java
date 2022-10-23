/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task.talk;

import com.laxcus.command.*;

/**
 * 分布任务组件状态查询命令钩子
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public class TalkCheckHook extends CommandHook {

	/**
	 * 构造默认的分布任务组件状态查询命令钩子
	 */
	public TalkCheckHook() {
		super();
	}

	/**
	 * 返回查询站点结果
	 * @return TalkCheckProduct实例
	 */
	public TalkCheckProduct getProduct() {
		return (TalkCheckProduct) super.getResult();
	}
}