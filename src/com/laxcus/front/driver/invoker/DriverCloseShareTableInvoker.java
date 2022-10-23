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
 * 关闭数据表共享资源调用器
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class DriverCloseShareTableInvoker extends DriverShareCrossInvoker {

	/**
	 * 构造关闭数据表共享资源调用器
	 * @param mission 驱动任务
	 */
	public DriverCloseShareTableInvoker(DriverMission mission) {
		super(mission);
	}

}