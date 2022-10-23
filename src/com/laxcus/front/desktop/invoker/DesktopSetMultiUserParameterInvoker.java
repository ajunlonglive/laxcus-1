/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 设置用户参数调用器
 * 
 * @author scott.liang
 * @version 1.0 05/30/2021
 * @since laxcus 1.0
 */
public abstract class DesktopSetMultiUserParameterInvoker extends DesktopInvoker {

	/**
	 * 构造设置用户参数调用器，指定命令
	 * @param cmd 设置用户参数命令
	 */
	protected DesktopSetMultiUserParameterInvoker(SetMultiUserParameter cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetMultiUserParameter getCommand() {
		return (SetMultiUserParameter) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须是管理员，或者等同管理员身份的用户
		boolean success = isAdministrator();
		if (!success) {
			success = getStaffPool().canDBA();
		}
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}
		// 发送命令到GATE站点
		return fireToHub() ;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		SetMultiUserParameterProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SetMultiUserParameterProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			print(product);
		} else {
			printFault();
		}

		// 退出
		return useful(success);
	}

	/**
	 * 打印结果
	 * @param items
	 */
	protected void print(SetMultiUserParameterProduct product) {
		printRuntime();
		
		// 建立标题
		createShowTitle(new String[] { "SET-PARAM/RESULT", "SET-PARAM/USERNAME" });

		// 命令
		SetMultiUserParameter cmd = getCommand();

		for (Siger siger : cmd.getUsers()) {
			String text = cmd.findText(siger);
			
			// 查找处理单元
			RefreshItem e = product.find(siger);
			boolean success = (e != null && e.isSuccessful());
			
			// 显示单元
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, success));
			// 明文
			item.add(new ShowStringCell(1, text));
			// 输出单元
			addShowItem(item);
		}
		
		// 输出全部记录
		flushTable();
	}

}