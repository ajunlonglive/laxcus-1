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
 * 删除分布数据构建应用软件包 <br><br>
 * 
 * 流程：<br>
 * 1. FRONT生成命令，投递给GATE节点 <br>
 * 2. GATE节点通过HASH节点，找到ACCOUNT节点，投递给ACCOUNT节点 <br>
 * 3. ACCOUNT节点处理流程： <br>
 * 3.1 打到软件包集合，清除同名包。 <br>
 * 3.2 通过TOP/HOME，找到DATA/WORK/CALL节点，通知它们下载新的组件包 <br>
 * 3.3 投递删除命令给DATA/WORK/BUILD/CALL节点 <br>
 * 3.4 通知GATE节点处理结果 <br>
 * 4. GATE收到结果，返回给FRONT节点 <br>
 * 5. FRONT把结果打印在计算机界面上。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/20/2020
 * @since laxcus 1.0
 */
public class DropEstablishPackage extends DropCloudPackage {

	private static final long serialVersionUID = -7268539786156964731L;

	/**
	 * 构造默认的删除分布数据构建应用软件包
	 */
	public DropEstablishPackage() {
		super();
	}

	/**
	 * 从可类化读取器中解析删除分布数据构建应用软件包
	 * @param reader 可类化读取器
	 */
	public DropEstablishPackage(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 生成删除分布数据构建应用软件包副本
	 * @param that 删除分布数据构建应用软件包实例
	 */
	private DropEstablishPackage(DropEstablishPackage that) {
		super(that);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropEstablishPackage duplicate() {
		return new DropEstablishPackage(this);
	}

}