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
 * 长整型平稳分割器。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/16/2009
 * @since laxcus 1.0
 */
public final class LongBalancer extends Bit64Balancer {

	/**
	 * 构造长整型平衡分割器
	 */
	public LongBalancer() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.balance.Bit64Balancer#getSector()
	 */
	@Override
	public Bit64Sector getSector() {
		return new LongSector();
	}

}