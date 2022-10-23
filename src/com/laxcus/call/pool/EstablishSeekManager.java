/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import com.laxcus.task.establish.issue.*;
import com.laxcus.util.*;

/**
 * ESTABLISH分布资源管理器
 * 
 * @author scott.liang
 * @version 1.0 5/5/2018
 * @since laxcus 1.0
 */
public class EstablishSeekManager extends SeekManager {

	/** ESTABLISH.ISSUE组件管理池 **/
	protected IssueTaskPool issuePool;

	/**
	 * 构造默认的ESTABLISH分布资源管理器
	 */
	protected EstablishSeekManager() {
		super();
	}

	/**
	 * 设置ESTABLISH.ISSUE组件管理池
	 * @param e
	 */
	public void setIssuePool(IssueTaskPool e) {
		Laxkit.nullabled(e);
		issuePool = e;
	}

	
}
