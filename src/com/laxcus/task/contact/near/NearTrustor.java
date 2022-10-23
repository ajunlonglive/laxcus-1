/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.near;

import com.laxcus.access.schema.*;
import com.laxcus.task.*;

/**
 * CONTACT.NEAR代理接口。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/3/2020
 * @since laxcus 1.0
 */
public interface NearTrustor extends TailTrustor {

	/**
	 * 根据数据表名，查找关联的数据表
	 * 
	 * @param space 数据表名
	 * @return Table实例
	 */
	Table findNearTable(Space space);

}