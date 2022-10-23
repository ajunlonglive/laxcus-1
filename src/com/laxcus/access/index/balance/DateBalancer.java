/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.balance;

import com.laxcus.access.index.section.*;

/**
 * 日期平衡分割器。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/15/2009
 * @since laxcus 1.0
 */
public final class DateBalancer extends Bit32Balancer {

	/**
	 * 构造日期平衡分割器
	 */
	public DateBalancer() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.balance.Bit32Balancer#getSector()
	 */
	@Override
	public Bit32Sector getSector() {
		return new DateSector();
	}

}