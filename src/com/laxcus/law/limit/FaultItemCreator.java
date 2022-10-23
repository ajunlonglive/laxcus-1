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
 * 故障锁定单元生成器
 * 
 * @author scott.liang
 * @version 1.0 3/22/2017
 * @since laxcus 1.0
 */
public final class FaultItemCreator {

	/**
	 * 从可类化数据读取器中解析一个故障锁定单元
	 * @param reader 可类化数据读取器
	 * @return FaultItem实例
	 */
	public static FaultItem resolve(ClassReader reader) {
		byte rank = reader.current();
		switch (rank) {
		case LawRank.ROW:
			return new RowFaultItem(reader);
		case LawRank.TABLE:
			return new TableFaultItem(reader);
		case LawRank.SCHEMA:
			return new SchemaFaultItem(reader);
		case LawRank.USER:
			return new UserFaultItem(reader);
		}
		return null;
	}
}