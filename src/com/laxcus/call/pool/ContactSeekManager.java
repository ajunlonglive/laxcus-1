/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import com.laxcus.task.contact.fork.*;
import com.laxcus.util.*;

/**
 * CONTACT分布资源管理器
 * 
 * @author scott.liang
 * @version 1.0 5/5/2018
 * @since laxcus 1.0
 */
public class ContactSeekManager extends SeekManager {

	/** CONDUCT.FORK组件管理池 **/
	protected ForkTaskPool forkPool;
	
	/**
	 * 构造默认的CONTACT分布资源管理器
	 */
	protected ContactSeekManager() {
		super();
	}

	/**
	 * 设置CONDUCT.FORK组件管理池
	 * @param e
	 */
	public void setForkPool(ForkTaskPool e) {
		Laxkit.nullabled(e);
		forkPool = e;
	}	
	
}
