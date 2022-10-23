/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.forbid;

import com.laxcus.law.*;
import com.laxcus.util.classable.*;

/**
 * 禁止操作单元生成器
 * 
 * @author scott.liang
 * @version 1.0 3/31/2017
 * @since laxcus 1.0
 */
public final class ForbidItemCreator {

	/**
	 * 从可类化数据读取器中解析一个禁止操作单元
	 * @param reader 可类化数据读取器
	 * @return ForbidItem实例
	 */
	public static ForbidItem resolve(ClassReader reader) {
		byte rank = reader.current();
		switch (rank) {
		case LawRank.ROW:
			return new RowForbidItem(reader);
		case LawRank.TABLE:
			return new TableForbidItem(reader);
		case LawRank.SCHEMA:
			return new SchemaForbidItem(reader);
		case LawRank.USER:
			return new UserForbidItem(reader);
		}
		return null;
	}
}