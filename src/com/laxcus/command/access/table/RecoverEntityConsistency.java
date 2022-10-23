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
 * 恢复表数据一致性
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public final class RecoverEntityConsistency extends ProcessTable {

	private static final long serialVersionUID = 7862497276680493541L;

	/**
	 * 构造恢复表数据一致性
	 */
	public RecoverEntityConsistency() {
		super();
	}

	/**
	 * 根据传入恢复表数据一致性，生成它的数据副本
	 * @param that RecoverEntityConsistency实例
	 */
	private RecoverEntityConsistency(RecoverEntityConsistency that) {
		super(that);
	}

	/**
	 * 构造恢复表数据一致性，指定数据表名
	 * @param space 数据表名
	 */
	public RecoverEntityConsistency(Space space) {
		this();
		setSpace(space);
	}
	
	/**
	 * 从可类化读取器中解析恢复表数据一致性
	 * @param reader 可类化数据读取器
	 */
	public RecoverEntityConsistency(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public RecoverEntityConsistency duplicate() {
		return new RecoverEntityConsistency(this);
	}

}