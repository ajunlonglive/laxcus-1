/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.limit;

import com.laxcus.law.*;
import com.laxcus.util.classable.*;

/**
 * 限制操作规则单元生成器
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public final class LimitItemCreator {

	/**
	 * 从可类化数据读取器中解析一个限制操作规则单元
	 * @param reader 可类化数据读取器
	 * @return LimitItem实例
	 */
	public static LimitItem resolve(ClassReader reader) {
		byte rank = reader.current();
		switch (rank) {
		case LawRank.ROW:
			return new RowLimitItem(reader);
		case LawRank.TABLE:
			return new TableLimitItem(reader);
		case LawRank.SCHEMA:
			return new SchemaLimitItem(reader);
		case LawRank.USER:
			return new UserLimitItem(reader);
		}
		return null;
	}
}