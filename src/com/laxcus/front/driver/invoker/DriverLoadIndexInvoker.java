/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.front.driver.mission.*;

/**
 * 加载索引命令调用器
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus 1.0
 */
public class DriverLoadIndexInvoker extends DriverFastMassInvoker {

	/**
	 * 构造加载索引命令调用器，指定驱动任务
	 * @param mission 驱动任务
	 */
	public DriverLoadIndexInvoker(DriverMission mission) {
		super(mission);
	}

}