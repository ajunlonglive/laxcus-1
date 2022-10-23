/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import com.laxcus.task.conduct.init.*;
import com.laxcus.util.*;

/**
 * CONDUCT分布资源管理器
 * 
 * @author scott.liang
 * @version 1.0 5/5/2018
 * @since laxcus 1.0
 */
public class ConductSeekManager extends SeekManager {

	/** CONDUCT.INIT组件管理池 **/
	protected InitTaskPool initPool;

	/**
	 * 构造默认的CONDUCT分布资源管理器
	 */
	protected ConductSeekManager() {
		super();
	}

	/**
	 * 设置CONDUCT.INIT组件管理池
	 * @param e
	 */
	public void setInitPool(InitTaskPool e) {
		Laxkit.nullabled(e);
		initPool = e;
	}

	
}
