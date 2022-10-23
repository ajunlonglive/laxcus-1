/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.index;

import com.laxcus.access.type.*;

/**
 * 数据块索引生成器。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/23/2009
 * @since laxcus 1.0
 */
public class StubIndexCreator {

	/**
	 * 根据传入的数据类型，建立一个默认的数据块索引
	 * @param family  索引类型
	 * @return  返回数据块索引实例，不匹配是空指针 
	 */
	public static StubIndex create(byte family) {
		switch (family) {
		case IndexType.SHORT_INDEX:
			return new StubShortIndex();
		case IndexType.INTEGER_INDEX:
			return new StubIntegerIndex();
		case IndexType.LONG_INDEX:
			return new StubLongIndex();
		case IndexType.FLOAT_INDEX:
			return new StubFloatIndex();
		case IndexType.DOUBLE_INDEX:
			return new StubDoubleIndex();
		}
		return null;
	}

}