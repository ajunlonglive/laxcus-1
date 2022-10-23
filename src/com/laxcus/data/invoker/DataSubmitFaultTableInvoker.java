/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.command.access.table.*;
import com.laxcus.site.*;

/**
 * 提交故障表调用器
 * 
 * @author scott.liang
 * @version 1.0 6/26/2019
 * @since laxcus 1.0
 */
public class DataSubmitFaultTableInvoker extends DataInvoker {

	/**
	 * 构造提交故障表调用器，指定命令
	 * @param cmd 提交故障表
	 */
	public DataSubmitFaultTableInvoker(SubmitFaultTable cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SubmitFaultTable getCommand() {
		return (SubmitFaultTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SubmitFaultTable cmd = getCommand();
		// 本地地址
		cmd.setSite(getLocal());
		// HOME SITE
		Node hub = getHub();
		// POST HOME SITE
		boolean success = directTo(hub, cmd);
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
