/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import com.laxcus.access.diagram.*;
import com.laxcus.command.access.user.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 操作权限命令。
 * 
 * @author scott.liang
 * @version 1.1 05/09/2015
 * @since laxcus 1.0
 */
public abstract class Certificate extends MultiUser {

	private static final long serialVersionUID = 4361369810980621142L;

	/** 权限表 **/
	private Permit permit;

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 权限表
		writer.writeDefault(permit);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 权限表
		permit = (Permit) reader.readDefault();
	}

	/**
	 * 构造操作权限命令
	 */
	protected Certificate() {
		super();
	}

	/**
	 * 根据传入的操作权限命令，生成它的数据副本
	 * @param that Certificate实例
	 */
	protected Certificate(Certificate that) {
		super(that);
		permit = that.permit;
	}

	/**
	 * 设置操作权限表
	 * @param e Permit实例
	 */
	public void setPermit(Permit e) {
		Laxkit.nullabled(e);

		permit = e;
	}

	/**
	 * 返回操作权限表
	 * @return Permit实例
	 */
	public Permit getPermit() {
		return permit;
	}

}