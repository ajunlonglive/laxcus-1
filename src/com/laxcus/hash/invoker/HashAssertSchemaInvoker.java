/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.invoker;

import java.util.*;

import com.laxcus.command.access.schema.*;
import com.laxcus.echo.*;
import com.laxcus.hash.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 判断数据库存在调用器。<br>
 * GATE/BANK发出，HASH接收。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class HashAssertSchemaInvoker extends HashInvoker {

	/**
	 * 构造判断数据库存在，指定命令
	 * @param cmd 判断数据库存在命令
	 */
	public HashAssertSchemaInvoker(AssertSchema cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AssertSchema getCommand() {
		return (AssertSchema) super.getCommand();
	}
	
	/**
	 * 拒绝操作
	 */
	private void refuse() {
		replyFault(Major.FAULTED, Minor.REFUSE); // 发送一个拒绝通知
	}
	
	/**
	 * 返回结果
	 * @param successful 成功标识
	 * @return 发送成功返回真，否则假
	 */
	private boolean reply(boolean successful) {
		AssertSchema cmd = getCommand();
		AssertSchemaProduct product = new AssertSchemaProduct(cmd.getFame(),
				successful);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		List<Node> accounts = StaffOnHashPool.getInstance().getAccountSites();

		AssertSchema cmd = getCommand();

		// 向全部ACCOUNT站点查询
		boolean success = launchTo(accounts, cmd);
		if (!success) {
			refuse();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		List<Integer> keys = getEchoKeys();
		int count = 0;
		int items = 0;
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					AssertSchemaProduct product = getObject(AssertSchemaProduct.class, index);
					if (product.isSuccessful()) items++;
					count++;
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 以上发生错误，返回一个故障通知
		if (count < keys.size()) {
			refuse();
			return false;
		}

		// 反馈结果
		boolean success = reply(items > 0);
		// 退出
		return useful(success);
	}

}
