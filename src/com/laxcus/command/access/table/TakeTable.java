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
 * 查表命令<br><br>
 * 
 * “TAKE TABLE”只查找一个表，“SHOW TABLE”查找多个表。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class TakeTable extends ProcessTable {

	private static final long serialVersionUID = -8086483939452830684L;
	
	/**
	 * 构造默认的查表命令
	 */
	private TakeTable() {
		super();
	}

	/**
	 * 生成查表命令的数据副本
	 * @param that TakeTable实例
	 */
	private TakeTable(TakeTable that) {
		super(that);
	}

	/**
	 * 构造查表命令，指定数据表名
	 * @param space 数据表名
	 */
	public TakeTable(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析查表命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeTable duplicate() {
		return new TakeTable(this);
	}

}