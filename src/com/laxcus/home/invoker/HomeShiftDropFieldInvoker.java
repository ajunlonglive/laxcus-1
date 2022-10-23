/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.field.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * “DROP FIELD”本地转发命令调用器。<br>
 * 调用器发送“DROP FIELD”命令给一批CALL站点，不等待CALL站点的应答，发送完毕即退出。
 * 
 * @author scott.liang
 * @version 1.0 3/23/2013
 * @since laxcus 1.0
 */
public class HomeShiftDropFieldInvoker extends HomeInvoker {

	/**
	 * 构造“DROP FIELD”本地转发命令调用器，指定命令
	 * @param cmd “SHIFT DROP FIELD”命令
	 */
	public HomeShiftDropFieldInvoker(ShiftDropField cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftDropField getCommand() {
		return (ShiftDropField) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftDropField shift = getCommand();
		List<Node> nodes = shift.getSites();
		DropField cmd = shift.getCommand();

		// 向目标地址发送一批命令
		int count = directTo(nodes, cmd);
		boolean success = (count == nodes.size());

		Logger.debug(this, "launch", success, "send count %d", count);

		// 无论成功/失败都退出
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