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
 * 设置最大CALL节点网关调用器
 * 
 * @author scott.liang
 * @version 1.0 03/25/2018
 * @since laxcus 1.0
 */
public class DriverSetMaxGatewaysInvoker extends DriverSetMultiUserParameterInvoker {

	/**
	 * 构造设置最大CALL节点网关调用器，指定驱动任务
	 * @param mission 驱动任务
	 */
	public DriverSetMaxGatewaysInvoker(DriverMission mission) {
		super(mission);
	}

}