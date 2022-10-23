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
import com.laxcus.visit.*;

/**
 * 修改账号密码命令调用器。<br>
 * 修改注册用户的账号密码。密码修改由账号持有人、管理员、等同管理员权限的用户修改。
 * 
 * @author scott.liang
 * @version 1.0 7/28/2009
 * @since laxcus 1.0
 */
public class MeetAlterUserInvoker extends MeetInvoker {

	/**
	 * 构造修改账号密码命令调用器，指定命令
	 * @param cmd 修改账号密码命令
	 */
	public MeetAlterUserInvoker(AlterUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AlterUser getCommand() {
		return (AlterUser) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		AlterUserProduct product = null;
		int index = getEchoKeys().get(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(AlterUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断修改用户密码成功
		boolean success = (product != null && product.isSuccessful());
		
		// 显示结果
		print(success);

		return useful(success);
	}

	/**
	 * 打印结果
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		AlterUser cmd = getCommand();
		String username = cmd.getPlainText();
		if (username == null) {
			username = cmd.getUsername().toString();
		}

		// 显示运行时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "ALTER-USER/STATUS", "ALTER-USER/USERNAME" });

		ShowItem item = new ShowItem();
		item.add(createConfirmTableCell(0, success));
		item.add(new ShowStringCell(1, username));
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}
}
