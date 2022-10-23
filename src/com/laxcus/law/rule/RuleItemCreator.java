/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.rule;

import com.laxcus.law.*;
import com.laxcus.util.classable.*;

/**
 * 事务规则单元生成器
 * 
 * @author scott.liang
 * @version 1.11 4/09/2018
 * @since laxcus 1.0
 */
public final class RuleItemCreator {

	/**
	 * 从可类化数据读取器中解析一个事务规则单元
	 * @param reader 可类化数据读取器
	 * @return Rule实例
	 */
	public static RuleItem resolve(ClassReader reader) {
		byte rank = reader.current();
		switch (rank) {
		case LawRank.ROW:
			return new RowRuleItem(reader);
		case LawRank.TABLE:
			return new TableRuleItem(reader);
		case LawRank.SCHEMA:
			return new SchemaRuleItem(reader);
		case LawRank.USER:
			return new UserRuleItem(reader);
		}
		return null;
	}
}