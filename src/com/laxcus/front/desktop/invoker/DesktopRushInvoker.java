/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.command.rebuild.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 数据块强制转换命令异步调用器。<br><br>
 * 
 * <B>注意：RUSH命令只允许系统管理员操作，用于测试环境，生产环境禁止使用！</B>
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopRushInvoker extends DesktopInvoker {

	/**
	 * 构造数据块强制转换命令异步调用器，指定命令实例
	 * @param cmd 数据块强制转换命令
	 */
	public DesktopRushInvoker(Rush cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Rush getCommand() {
		return (Rush) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须是管理员，或者等同管理员身份的用户
		boolean success = isAdministrator();
		if (success) {
			success = getStaffPool().canDBA();
		}
		// 不支持，报警退出！
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return useful(false);
		}
		// 提交命令
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		RushProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(RushProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			print(product.list());
		} else {
			printFault();
		}

		return useful(success);
	}

	/**
	 * 打印单元
	 * @param array RushItem数组
	 */
	private void print(List<TissItem> array) {
		// 建立标题
		createShowTitle(new String[] { "RUSH/STATUS", "RUSH/SITE", "RUSH/CODE" });

		// 打印
		for (TissItem e : array) {
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			// 站点地址
			item.add(new ShowStringCell(1, e.getSite()));
			// 返回码
			item.add(new ShowIntegerCell(2, e.getState()));
			// 显示
			addShowItem(item);
		}
		
		// 输出全部记录
		flushTable();
	}
}