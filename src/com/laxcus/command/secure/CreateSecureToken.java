/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.secure;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 建立密钥令牌，输出到指定的节点
 * 
 * @author scott.liang
 * @version 1.0 2/13/2021
 * @since laxcus 1.0
 */
public class CreateSecureToken extends ExecuteSecureToken {

	private static final long serialVersionUID = -6191368128363534144L;

	/** 密钥令牌 **/
	private SecureTokenSlice slice;
	
	/**
	 * 构造默认的建立密钥令牌命令
	 */
	public CreateSecureToken() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析建立密钥令牌命令
	 * @param reader 可类化数据读取器
	 */
	public CreateSecureToken(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成建立密钥令牌命令的数据副本
	 * @param that CreateSecureToken实例
	 */
	private CreateSecureToken(CreateSecureToken that) {
		super(that);
		slice = that.slice;
	}

	/**
	 * 设置密钥令牌，不允许空指针
	 * @param e SecureTokenSlice实例
	 */
	public void setSlice(SecureTokenSlice e) {
		Laxkit.nullabled(e);
		slice = e;
	}

	/**
	 * 返回密钥令牌
	 * @return SecureTokenSlice实例
	 */
	public SecureTokenSlice getSlice() {
		return slice;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CreateSecureToken duplicate() {
		return new CreateSecureToken(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(slice);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		slice = new SecureTokenSlice(reader);
	}

}