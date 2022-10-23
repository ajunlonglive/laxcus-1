/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud;

import com.laxcus.util.classable.*;

/**
 * 发布分布迭代系统应用软件包 <br><br>
 *
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class DeployContactSystemPackage extends DeploySystemPackage {

	private static final long serialVersionUID = -1716198153823625380L;

	/**
	 * 构造默认的发布分布迭代系统应用软件包
	 */
	public DeployContactSystemPackage() {
		super();
	}

	/**
	 * 从可类化读取器中解析发布分布迭代系统应用软件包
	 * @param reader 可类化读取器
	 */
	public DeployContactSystemPackage(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 生成发布分布迭代系统应用软件包副本
	 * @param that 发布分布迭代系统应用软件包实例
	 */
	private DeployContactSystemPackage(DeployContactSystemPackage that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DeployContactSystemPackage duplicate() {
		return new DeployContactSystemPackage(this);
	}

}