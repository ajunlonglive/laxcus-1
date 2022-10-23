/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.permit;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.util.classable.*;

/**
 * 输入全部授权配置命令。只限自己账号下的授权表。
 * 命令格式：“fill all permit”
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class FillPermit extends Command {

	private static final long serialVersionUID = -2351772509155973990L;

	/**
	 * 构造默认和私有的输入全部授权配置命令
	 */
	public FillPermit() {
		super();
	}

	/**
	 * 根据传入的输入全部授权配置命令，生成它的数据副本
	 * @param that FillPermit实例
	 */
	private FillPermit(FillPermit that) {
		super(that);
	}

	/**
	 * 构造"fill permit"命令，指定它的回显地址
	 * @param cabin 回显地址
	 */
	public FillPermit(Cabin cabin) {
		this();
		setSource(cabin);
	}

	/**
	 * 从可类化读取器中解析输入全部授权配置命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public FillPermit(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * 根据当前输入全部授权配置命令，生成它的数据副本
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FillPermit duplicate() {
		return new FillPermit(this);
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