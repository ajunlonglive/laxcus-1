/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.site.gate.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检查GATE站点的注册用户和站点编号的一致性调用器。
 * 
 * @author scott.liang
 * @version 1.0 7/20/2019
 * @since laxcus 1.0
 */
public class RayCheckShadowConsistencyInvoker extends RayInvoker {

	/**
	 * 构造检查GATE站点的注册用户和站点编号的一致性调用器
	 * @param cmd 检查GATE站点的注册用户和站点编号的一致性
	 */
	public RayCheckShadowConsistencyInvoker(CheckShadowConsistency cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckShadowConsistency getCommand() {
		return (CheckShadowConsistency) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 这个命令只发送给BANK站点。如果不是BANK站点，拒绝发送
		if (!isBankHub()) {
			faultX(FaultTip.BANK_RETRY);
			return useful(false);
		}

		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CheckShadowConsistencyProduct product = null;
		int index = findEchoKey(0);
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(CheckShadowConsistencyProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 判断是成功
		boolean success = (product != null);
		if (success) {
			print(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		return useful(success);
	}
	
	/**
	 * 打印结果
	 * @param product
	 */
	private void print(CheckShadowConsistencyProduct product) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "SHADOW-CONSISTENCY/SITE",
				"SHADOW-CONSISTENCY/COUNT", "SHADOW-CONSISTENCY/MEMBERS", "SHADOW-CONSISTENCY/MATCHS",
		"SHADOW-CONSISTENCY/RATE" });
		// 处理单元
		for (GateUserConsistencyItem e : product.list()) {
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, e.getSite()));
			item.add(new ShowIntegerCell(1, e.getUsers()));
			item.add(new ShowIntegerCell(2, e.getMembers()));
			item.add(new ShowIntegerCell(3, e.getMatchs()));

			// 完整率
			String rate = ConfigParser.splitRate(e.getMatchs(), e.getUsers(), 3);
			item.add(new ShowStringCell(4, rate));

			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

}
