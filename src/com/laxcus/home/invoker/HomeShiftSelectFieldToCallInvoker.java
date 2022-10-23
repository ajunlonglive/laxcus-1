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
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 转发投递命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 4/19/2013
 * @since laxcus 1.0
 */
public class HomeShiftSelectFieldToCallInvoker extends HomeInvoker {

	/**
	 * 构造转发投递命令调用器，指定转发命令
	 * @param cmd 转发命令
	 */
	public HomeShiftSelectFieldToCallInvoker(ShiftSelectFieldToCall cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftSelectFieldToCall getCommand() {
		return (ShiftSelectFieldToCall) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftSelectFieldToCall shift = getCommand();
		// 命令来源站点（DATA/WORK/BUILD任意一种），通知这些站点，向CALL站点发送命令
		Node endpoint = shift.getSite();

		ArrayList<CommandItem> array = new ArrayList<CommandItem>();
		for (SelectFieldToCall cmd : shift.list()) {
			CommandItem item = new CommandItem(endpoint, cmd);
			array.add(item);
		}
		// 以容错模式直接投送，不用反馈结果
		int count = directTo(array, false);

		// 判断成功
		boolean success = (count == array.size());
		Logger.debug(this, "launch", success, "all:%d, successful:%d",
				array.size(), count);

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