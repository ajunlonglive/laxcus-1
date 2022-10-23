/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.put;

import com.laxcus.access.schema.*;
import com.laxcus.task.*;

/**
 * CONDUCT.PUT代理接口。<br>
 * 
 * @author scott.liang
 * @version 1.1 6/19/2011
 * @since laxcus 1.0
 */
public interface PutTrustor extends TailTrustor {

	/**
	 * 根据数据表名，查找关联的数据表
	 * 
	 * @param space 数据表名
	 * @return Table实例
	 */
	Table findPutTable(Space space);

}