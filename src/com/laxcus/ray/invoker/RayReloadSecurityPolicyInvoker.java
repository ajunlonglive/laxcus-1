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
 * 重新设置节点的安全策略命令调用器。
 * 命令先投递到管理节点，再经过管理节点转发到下属节点
 * 
 * @author scott.liang
 * @version 1.0 6/17/2018
 * @since laxcus 1.0
 */
public class RayReloadSecurityPolicyInvoker extends RayInvoker {

	/**
	 * 构造重新设置节点的安全策略命令调用器，指定命令
	 * @param cmd 重装加载安全策略命令
	 */
	public RayReloadSecurityPolicyInvoker(ReloadSecurityPolicy cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReloadSecurityPolicy getCommand() {
		return (ReloadSecurityPolicy) super.getCommand();
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
		ReloadSecurityPolicyProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ReloadSecurityPolicyProduct.class, index);
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
	private void print(List<ReloadSecurityPolicyItem> array) {
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "RELOAD-SECURITY-POLICY/SITE", "RELOAD-SECURITY-POLICY/STATUS" });

		for (int index = 0; index < array.size(); index++) {
			ReloadSecurityPolicyItem e = array.get(index);
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