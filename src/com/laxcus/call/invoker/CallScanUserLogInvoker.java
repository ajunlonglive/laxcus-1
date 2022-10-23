/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.range.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;

/**
 * 检索用户日志调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 4/2/2018
 * @since laxcus 1.0
 */
public class CallScanUserLogInvoker extends CallInvoker {

	/**
	 * 构造检索用户日志调用器，指定命令
	 * @param cmd 检索用户日志命令
	 */
	public CallScanUserLogInvoker(ScanUserLog cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanUserLog getCommand() {
		return (ScanUserLog) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ScanUserLog cmd = getCommand();

		ScanUserLogProduct product = new ScanUserLogProduct();

		// 本地地址
		Node local = getLocal();

		// 检查用户日志
		for (Siger siger : cmd.getUsers()) {
			LongRange range = cmd.getRange();
			List<EchoLog> logs = UserLogPool.getInstance().find(siger, range);

			ScanUserLogItem item = new ScanUserLogItem(siger, local);
			item.addAll(logs);
			product.add(item);
		}

		// 反馈结果
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "item size%d", product.size());

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
}