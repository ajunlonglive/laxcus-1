/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;

/**
 * 删除数据表调用器。<br>
 * 删除数据表，以及数据表下的全部表记录。
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class GateDropTableInvoker extends GateSeekAccountSiteInvoker {

	/**
	 * 构造删除数据表调用器，指定命令
	 * @param cmd 删除数据表命令
	 */
	public GateDropTableInvoker(DropTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropTable getCommand() {
		return (DropTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropTable cmd = getCommand();
		Space space = cmd.getSpace();
		// 判断拥有删除数据表权限
		boolean success = canDropTable(space);
		if (success) {
			success = transmit(); // 命令交给BANK站点去执行
		}
		if (!success) {
			refuse();
		}
		return success;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return reflect();
	}

}