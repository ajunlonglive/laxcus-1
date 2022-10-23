/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.traffic;

import com.laxcus.command.*;

/**
 * 检测数据传输流量钩子
 * 
 * @author scott.liang
 * @version 1.0 8/21/2018
 * @since laxcus 1.0
 */
public class GustHook extends CommandHook {

	/**
	 * 构造默认的检测数据传输流量钩子
	 */
	public GustHook() {
		super();
	}

	/**
	 * 返回流量检测报告
	 * @return 流量检测报告
	 */
	public TrafficProduct getProduct() {
		return (TrafficProduct) super.getResult();
	}

}