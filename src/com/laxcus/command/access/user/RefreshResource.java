/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.util.classable.*;

/**
 * 刷新网络资源 <br><br>
 * 
 * 网络资源以用户签名为基础进行刷新。
 * 
 * @author scott.liang
 * @version 1.0 7/11/2015
 * @since laxcus 1.0
 */
public abstract class RefreshResource extends MultiUser {

	private static final long serialVersionUID = 772691851252721589L;

	/**
	 * 根据传入的刷新网络资源命令，生成它的数据副本
	 * @param that 刷新网络资源命令
	 */
	protected RefreshResource(RefreshResource that) {
		super(that);
	}

	/**
	 * 构造刷新网络资源命令
	 */
	protected RefreshResource() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}

}