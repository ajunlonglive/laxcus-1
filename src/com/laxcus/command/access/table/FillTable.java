/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 获取当前账号下的全部表命令。<br>
 * 命令格式："fill all table "。
 * 
 * @author scott.liang
 * @version 1.1 6/2/2015
 * @since laxcus 1.0
 */
public class FillTable extends Command {

	private static final long serialVersionUID = -7057577872829568392L;

	/**
	 * 生成获取全部表命令的数据副本
	 * @param that FillTable实例
	 */
	private FillTable(FillTable that) {
		super(that);
	}

	/**
	 * 构造默认的获取全部表命令
	 */
	public FillTable() {
		super();
	}

	/**
	 * 从可类化读取器中解析获取全部表命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public FillTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FillTable duplicate() {
		return new FillTable(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// TODO Auto-generated method stub
	}

}
