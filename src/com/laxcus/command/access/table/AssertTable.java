/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * 判断表存在命令。<br>
 * 命令格式： “assert table [schema name.table name]”
 * 
 * @author scott.liang
 * @version 1.1 8/1/2015
 * @since laxcus 1.0
 */
public class AssertTable extends ProcessTable {

	private static final long serialVersionUID = 6738091687292203435L;

	/**
	 * 构造默认和私有的判断表存在命令。
	 */
	private AssertTable() {
		super();
	}

	/**
	 * 根据传入的判断表存在命令，生成它的数据副本
	 * @param that 判断表存在命令对象实例
	 */
	private AssertTable(AssertTable that) {
		super(that);
	}

	/**
	 * 构造判断表存在命令，指定数据表名
	 * @param space 数据表名
	 */
	public AssertTable(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析判断表存在命令参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public AssertTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public AssertTable duplicate() {
		return new AssertTable(this);
	}

}
