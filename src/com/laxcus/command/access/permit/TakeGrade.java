/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 获取注册账号的操作权级命令。<br><br>
 * 
 * 身份是管理员或者普通注册用户的任意一种。
 * 
 * @author scott.liang
 * @version 1.1 4/9/2015
 * @since laxcus 1.0
 */
public class TakeGrade extends Command {

	private static final long serialVersionUID = -3283957634811307004L;

	/**
	 * 构造默认获取注册账号的操作权级命令
	 */
	public TakeGrade() {
		super();
	}

	/**
	 * 根据传入的获取注册账号的操作权级命令，生成它的数据副本
	 * @param that TakeGrade实例
	 */
	private TakeGrade(TakeGrade that) {
		super(that);
	}

	/**
	 * 从可类化读取器中解析获取注册账号的操作权级命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public TakeGrade(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeGrade duplicate() {
		return new TakeGrade(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {

	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {

	}


}