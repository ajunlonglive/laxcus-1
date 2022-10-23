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
 * 分布任务组件交互操作命令钩子
 * 
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public class TalkAskHook extends CommandHook {

	/**
	 * 构造默认的分布任务组件交互操作命令钩子
	 */
	public TalkAskHook() {
		super();
	}

	/**
	 * 返回对话结果
	 * @return TalkAskProduct实例
	 */
	public TalkAskProduct getProduct() {
		return (TalkAskProduct) super.getResult();
	}
}