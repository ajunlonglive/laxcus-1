/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.call.pool.*;
import com.laxcus.command.field.*;
import com.laxcus.site.Node;

/**
 * 撤销BUILD资源命令调用器。<br>
 * 这个命令由HOME发出，目标是CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 3/23/2013
 * @since laxcus 1.0
 */
public class CallDropBuildFieldInvoker extends CallInvoker {

	/**
	 * 构造撤销BUILD资源命令调用器，指定命令
	 * @param cmd 撤销BUILD资源命令
	 */
	public CallDropBuildFieldInvoker(DropBuildField cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropBuildField getCommand() {
		return (DropBuildField) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropBuildField cmd = getCommand();
		Node node = cmd.getNode();
		BuildOnCallPool.getInstance().drop(node);
		return useful();
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
