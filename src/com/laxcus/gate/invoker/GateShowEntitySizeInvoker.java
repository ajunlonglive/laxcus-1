/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.fast.*;

/**
 * 显示数据块尺寸命令调用器
 * 
 * @author scott.liang
 * @version 1.0 7/8/2018
 * @since laxcus 1.0
 */
public class GateShowEntitySizeInvoker extends GateSelfOperateInvoker {

	/**
	 * 建立显示数据块尺寸命令调用器
	 * @param cmd 显示数据块尺寸命令
	 */
	public GateShowEntitySizeInvoker(ShowEntitySize cmd) {
		super(cmd);
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		return transmit();
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	//	 */
	//	@Override
	//	public boolean ending() {
	//		return reflect();
	//	}

}