/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.task;

import com.laxcus.command.*;

/**
 * 发布分布任务组件应用附件命令钩子
 * @author scott.liang
 * @version 1.0 10/8/2019
 * @since laxcus 1.0
 */
public class PublishSingleTaskAssistComponentHook extends CommandHook {

	/**
	 * 构造默认的发布分布任务组件应用附件命令钩子
	 */
	public PublishSingleTaskAssistComponentHook() {
		super();
	}

	/**
	 * 返回发布结果
	 * @return PublishTaskAssistComponentProduct实例
	 */
	public PublishTaskAssistComponentProduct getProduct() {
		return (PublishTaskAssistComponentProduct) super.getResult();
	}
}