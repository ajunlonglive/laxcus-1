/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.front;

import com.laxcus.tub.command.*;
import com.laxcus.tub.product.*;
import com.laxcus.tub.visit.*;

/**
 * FRONT节点的边缘命令调用接口。
 * 
 * @author scott.liang
 * @version 1.0 10/11/2020
 * @since laxcus 1.0
 */
public class TubVisitOnFront implements TubVisit {

	/**
	 * 构造FRONT节点的边缘命令调用接口
	 */
	public TubVisitOnFront() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.tub.visit.TubVisit#submit(java.lang.String)
	 */
	@Override
	public TubProduct submit(TubCommand cmd) throws TubVisitException {
		return null;
	}

}