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
 * 发布分布计算应用软件包 <br><br>
 * 
 * 流程：<br>
 * 1. FRONT生成命令，投递给GATE节点 <br>
 * 2. GATE节点通过HASH节点，找到ACCOUNT节点，投递给ACCOUNT节点 <br>
 * 3. ACCOUNT节点处理流程： <br>
 * 3.1 拆包，把引导包加入组件集 <br>
 * 3.2 通过TOP/HOME，找到DATA/WORK/CALL节点，通知它们下载新的组件包 <br>
 * 3.3 投递附件和动态链接库给DATA/WORK/CALL节点 <br>
 * 3.4 通知GATE节点处理结果 <br>
 * 4. GATE收到结果，返回给FRONT节点 <br>
 * 5. FRONT把结果打印在计算机界面上。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/17/2020
 * @since laxcus 1.0
 */
public class DeployConductPackage extends DeployCloudPackage {

	private static final long serialVersionUID = 3890929113772954817L;

	/**
	 * 构造默认的发布分布计算应用软件包
	 */
	public DeployConductPackage() {
		super();
	}

	/**
	 * 从可类化读取器中解析发布分布计算应用软件包
	 * @param reader 可类化读取器
	 */
	public DeployConductPackage(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 生成发布分布计算应用软件包副本
	 * @param that 发布分布计算应用软件包实例
	 */
	private DeployConductPackage(DeployConductPackage that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DeployConductPackage duplicate() {
		return new DeployConductPackage(this);
	}

}