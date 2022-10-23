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
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 删除账号命令异步调用器。<br>
 * 删除账号的同时，它名下的全部数据库、数据表一并删除。
 * 
 * @author scott.liang
 * @version 1.0 8/23/2012
 * @since laxcus 1.0
 */
public class MeetDropUserInvoker extends MeetInvoker {

	/**
	 * 构造删除账号命令异步调用器，指定删除命令
	 * @param cmd 删除用户账号
	 */
	public MeetDropUserInvoker(DropUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropUser getCommand() {
		return (DropUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropUser cmd = getCommand();
		Siger siger = cmd.getUsername();
		// 不能删除自己
		if (getUsername().compareTo(siger) == 0) {
			faultX(FaultTip.COMMAND_REFUSED);
			return false;
		}

		// 判断具备删除用户权限。 如果是管理员，拥有全部操作权限
		boolean success = isAdministrator();
		// 一般用户，必须拥有删除权限
		if (!success) {
			success = getStaffPool().canDropUser();
		}
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}

		// 发送到BANK站点
		return fireToHub();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		DropUserProduct product = null;

		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());

		// 打印结果
		print(success);

		return useful(success);
	}

	/**
	 * 打印结果
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		DropUser cmd = getCommand();
		String username = cmd.getPlainText();
		if (username == null) {
			username = cmd.getUsername().toString();
		}

		// 显示运行时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "DROP-USER/STATUS",
		"DROP-USER/USERNAME" });

		ShowItem item = new ShowItem();
		item.add(createConfirmTableCell(0, success));
		item.add(new ShowStringCell(1, username));
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}
}