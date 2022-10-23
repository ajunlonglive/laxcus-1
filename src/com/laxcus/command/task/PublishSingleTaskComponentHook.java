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
 * 发布分布任务组件命令钩子
 * @author scott.liang
 * @version 1.0 10/10/2019
 * @since laxcus 1.0
 */
public class PublishSingleTaskComponentHook extends CommandHook {

	/**
	 * 构造默认的发布分布任务组件命令钩子
	 */
	public PublishSingleTaskComponentHook() {
		super();
	}

	/**
	 * 返回发布结果
	 * @return PublishTaskComponentProduct实例
	 */
	public PublishTaskComponentProduct getProduct() {
		return (PublishTaskComponentProduct) super.getResult();
	}
}