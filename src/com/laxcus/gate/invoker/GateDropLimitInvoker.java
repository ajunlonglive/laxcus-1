/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.limit.*;

/**
 * 删除限制操作命令调用器 <br>
 * 命令发送到ACCOUNT站点，找到账号修改限制记录，数据结果原样反馈。
 * 
 * @author scott.liang
 * @version 3/23/2017
 * @since laxcus 1.0
 */
public class GateDropLimitInvoker extends GateSelfOperateInvoker {

	/**
	 * 构造删除限制操作命令调用器，指定命令
	 * @param cmd 建立限制操作命令
	 */
	public GateDropLimitInvoker(DropLimit cmd) {
		super(cmd);
		// 通过广播反射更新自己 
		setMulticast(true);
	}

}