/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.reload.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 扫描堆栈命令命令调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 7/26/2018
 * @since laxcus 1.0
 */
public class RayScanCommandStackInvoker extends RayInvoker {

	/**
	 * 构造扫描堆栈命令命令调用器，指定命令
	 * @param cmd 扫描堆栈命令命令
	 */
	public RayScanCommandStackInvoker(ScanCommandStack cmd) {
		super(cmd);
		cmd.setFast(true); // 默认是极速处理：跳过管理池的检查！
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanCommandStack getCommand() {
		return (ScanCommandStack) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ScanCommandStackProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ScanCommandStackProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		boolean success = (product != null);
		// 打印结果
		if (success) {
			print(product.list());
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}
		return useful(success);
	}

	/**
	 * 打印结果
	 * @param array
	 */
	private void print(List<ScanCommandStackItem> array) {
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "SCAN-STACK-COMMAND/SITE", "SCAN-STACK-COMMAND/STATUS" });

		for (int index = 0; index < array.size(); index++) {
			ScanCommandStackItem e = array.get(index);
			// 显示结果
			ShowItem showItem = new ShowItem();
			showItem.add(new ShowStringCell(0, e.getSite()));
			showItem.add(createConfirmTableCell(1, e.isSuccessful()));
			
			addShowItem(showItem);
		}
		// 输出全部记录
		flushTable();
	}

}