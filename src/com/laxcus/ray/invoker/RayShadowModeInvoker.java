/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.site.entrance.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 定位GATE站点模式调用器。
 * 这个命令将通过BANK站点，发送给全部ENTRANCE站点。
 * 
 * @author scott.liang
 * @version 1.0 7/19/2019
 * @since laxcus 1.0
 */
public class RayShadowModeInvoker extends RayInvoker {

	/**
	 * 构造定位GATE站点模式调用器
	 * @param cmd 定位GATE站点模式
	 */
	public RayShadowModeInvoker(ShadowMode cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShadowMode getCommand() {
		return (ShadowMode) super.getCommand();
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
		ShadowModeProduct product = null;
		int index = findEchoKey(0);
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(ShadowModeProduct.class, index);
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
	private void print(ShadowModeProduct product) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "SHADOW-MODE/SITE" });
		// 处理单元
		for (Node e : product.list()) {
			ShowItem item = new ShowItem();
			item.add(new ShowStringCell(0, e));
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

}