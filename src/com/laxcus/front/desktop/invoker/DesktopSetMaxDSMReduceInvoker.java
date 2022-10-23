/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 设置DSM表压缩倍数异步调用器。<br><br>
 * 
 * <B> 这个命令供管理员或者有管理员权限的注册用户使用。它将修改ACCOUNT节点上的表配置参数，同时通知全部的DATA站点。</B>
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopSetMaxDSMReduceInvoker extends DesktopInvoker {

	/**
	 * 构造设置DSM表压缩倍数异步调用器，指定命令
	 * @param cmd 设置DSM表压缩倍数
	 */
	public DesktopSetMaxDSMReduceInvoker(SetMaxDSMReduce cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetMaxDSMReduce getCommand() {
		return (SetMaxDSMReduce) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须是管理员，或者等同管理员身份的注册用户才能使用
		boolean success = isAdministrator();
		if (!success) {
			success = canDBA();
		}
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return useful();
		}
		// 发向GATE站点
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		SetMaxDSMReduceProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SetMaxDSMReduceProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			// 打印结果
			print(product);
		} else {
			printFault(); // 打印故障
		}
		
		Logger.debug(this, "ending", success, "\'%s\'", getCommand());

		return useful(success);
	}

	/**
	 * 打印结果
	 * @param product 反馈报告
	 */
	private void print(SetMaxDSMReduceProduct product) {
		// 显示运行时间
		printRuntime();

		// 显示执行失败！
		if (!product.isSuccessful()) {
			faultX(FaultTip.IMPLEMENT_FAULT);
			return;
		}
		
		// 全部结果
		List<TissItem> array = product.list();

		// 设置标题
		createShowTitle(new String[] { "SET-MAX-DSMREDUCE/STATUS",
				"SET-MAX-DSMREDUCE/SITE", "SET-MAX-DSMREDUCE/CODE" });
		// 显示结果
		for(TissItem e : array) {
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
