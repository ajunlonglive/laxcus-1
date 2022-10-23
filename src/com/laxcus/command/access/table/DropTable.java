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
 * 删除数据表命令。<br>
 * 命令格式：DROP TABLE 数据库.表 <br>
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class DropTable extends ProcessTable {

	private static final long serialVersionUID = 2859749796033965718L;

	/**
	 * 构造默认和私有删除数据表命令
	 */
	private DropTable() {
		super();
	}

	/**
	 * 从传入的删除数据表命令，生成它的数据副本
	 * @param that DropTable实例
	 */
	private DropTable(DropTable that) {
		super(that);
	}

	/**
	 * 构造删除数据表命令，指定数据表名
	 * @param space 数据表名
	 */
	public DropTable(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析删除数据表命令
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public DropTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DropTable duplicate() {
		return new DropTable(this);
	}

}
