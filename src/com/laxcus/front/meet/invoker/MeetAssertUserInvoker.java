/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 判断用户名称调用器。<br>
 * 由管理员、等同管理员权限的用户操作。
 * 
 * @author scott.liang
 * @version 1.0 2/2/2010
 * @since laxcus 1.0
 */
public class MeetAssertUserInvoker extends MeetInvoker {

	/**
	 * 构造判断用户名称调用器，指定命令
	 * @param cmd 判断用户名称
	 */
	public MeetAssertUserInvoker(AssertUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AssertUser getCommand() {
		return (AssertUser) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 判断是管理员或者等同管理员的用户
		boolean success = isAdministrator();
		if (!success) {
			success = getStaffPool().canDBA();
		}
		// 权限不足
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return useful(false);
		}
		return fireToHub();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		AssertUserProduct product = null;
		int index = getEchoKeys().get(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(AssertUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		// 无效！
		if (product == null) {
			faultX(FaultTip.IMPLEMENT_FAULT);
			return useful(false);
		}

		// 显示结果
		print(product.isSuccessful());

		return useful();
	}

	/**
	 * 打印结果
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		AssertUser cmd = getCommand();
		String username = cmd.getUsernameText();
		if (username == null) {
			username = cmd.getUsername().toString();
		}

		// 显示运行时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "ASSERT-USER/USERNAME",
				"ASSERT-USER/STATUS" });

		// 结果
		String res = (success ? getXMLContent("ASSERT-USER/STATUS/YES")
				: getXMLContent("ASSERT-USER/STATUS/NO"));

		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, username));
		item.add(new ShowStringCell(1, res));
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}
}
