/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.rebuild.*;

/**
 * 建立数据优化时间命令调用器。<br>
 * 
 * 命令从FRONT站点提交给ACCOUNT站点，GATE站点在此起转发作用。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2013
 * @since laxcus 1.0
 */
public class GateCreateRegulateTimeInvoker extends GateSelfOperateInvoker {

	/**
	 * 建立建立数据优化时间命令调用器。
	 * @param cmd 建立数据优化时间命令
	 */
	public GateCreateRegulateTimeInvoker(CreateRegulateTime cmd) {
		super(cmd);
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		return super.transmit();
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