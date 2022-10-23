/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool;

import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 站点管理池。<br>
 * 
 * 站点管理池标记一个注册站点的属性，以示与其它管理池的区别。
 * 
 * @author scott.liang
 * @version 1.0 2/5/2009
 * @since laxcus 1.0
 */
public abstract class SitePool extends VirtualPool {

	/** 站点类型，见SiteTag定义 **/
	private byte family;

	/**
	 * 构造站点管理池，指定站点类型
	 * @param who 站点类型
	 */
	protected SitePool(byte who) {
		super();
		setFamily(who);
	}

	/**
	 * 设置站点类型
	 * @param who 站点类型
	 */
	private void setFamily(byte who) {
		// 检查站点类型
		if (!SiteTag.isSite(who)) {
			throw new IllegalValueException("illegal site: %d", who);
		}
		family = who;
	}

	/**
	 * 返回管理池的站点类型
	 * @return 站点类型
	 */
	public byte getFamily() {
		return family;
	}

}