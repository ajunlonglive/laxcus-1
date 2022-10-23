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
 * 发布分布数据构建应用软件包
 * 
 * @author scott.liang
 * @version 1.0 3/14/2020
 * @since laxcus 1.0
 */
public class DeployEstablishPackage extends DeployCloudPackage {

	private static final long serialVersionUID = 3890929113772954817L;

	/**
	 * 构造默认的发布分布数据构建应用软件包
	 */
	public DeployEstablishPackage() {
		super();
	}

	/**
	 * 从可类化读取器中解析发布分布数据构建应用软件包
	 * @param reader 可类化读取器
	 */
	public DeployEstablishPackage(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 生成发布分布数据构建应用软件包副本
	 * @param that 发布分布数据构建应用软件包实例
	 */
	private DeployEstablishPackage(DeployEstablishPackage that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DeployEstablishPackage duplicate() {
		return new DeployEstablishPackage(this);
	}

}