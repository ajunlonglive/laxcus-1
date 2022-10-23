/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import com.laxcus.access.diagram.*;
import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 单账号权限处理命令。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public abstract class SingleCertificate extends Command {

	private static final long serialVersionUID = -4390650889486356732L;

	/** 账号签名 */
	private Siger siger;

	/** 权限表 **/
	private Permit permit;

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 签名
		writer.writeObject(siger);
		// 权限表
		writer.writeDefault(permit);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		siger = new Siger(reader);
		// 权限表
		permit = (Permit) reader.readDefault();
	}

	/**
	 * 构造操作权限命令
	 */
	protected SingleCertificate() {
		super();
	}

	/**
	 * 根据传入的操作权限命令，生成它的数据副本
	 * @param that SingleCertificate实例
	 */
	protected SingleCertificate(SingleCertificate that) {
		super(that);
		siger = that.siger;
		permit = that.permit;
	}

	/**
	 * 设置账号签名
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回账号签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
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