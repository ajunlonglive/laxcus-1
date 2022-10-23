/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import com.laxcus.command.*;

/**
 * 发布系统应用命令钩子
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class DeploySystemPackageHook extends CommandHook {

	/**
	 * 构造默认的发布系统应用命令钩子
	 */
	public DeploySystemPackageHook() {
		super();
	}

	/**
	 * 返回发布结果
	 * @return DeploySystemPackageProduct实例
	 */
	public DeploySystemPackageProduct getProduct() {
		return (DeploySystemPackageProduct) super.getResult();
	}
}