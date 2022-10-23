/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.end;

import com.laxcus.access.schema.*;
import com.laxcus.task.*;

/**
 * ESTABLISH.END阶段资源代理
 * 
 * @author scott.liang
 * @version 1.0 5/23/2011
 * @since laxcus 1.0
 */
public interface EndTrustor extends TailTrustor {

	/**
	 * 根据数据表名，查找关联的数据表
	 * 
	 * @param space 数据表名
	 * @return Table实例
	 */
	Table findEndTable(Space space);

}