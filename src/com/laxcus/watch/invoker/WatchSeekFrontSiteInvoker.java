/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.util.*;

import com.laxcus.command.site.front.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检索基于节点地址的用户登录信息调用器。<br><br><br>
 * 
 * 流程：<br>
 * 1. WATCH -> HOME -> CALL <br>
 * 2. WATCH -> BANK -> GATE <br>
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class WatchSeekFrontSiteInvoker extends WatchInvoker {

	/**
	 * 构造检索基于节点地址的用户登录信息调用器。
	 * @param cmd 检索基于节点地址的用户登录信息
	 */
	public WatchSeekFrontSiteInvoker(SeekFrontSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekFrontSite getCommand() {
		return (SeekFrontSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须是登录到HOME/BANK节点，否则拒绝执行
		boolean success = (isBankHub() || isHomeHub());
		if (!success) {
			faultX(FaultTip.HOME_BANK_RETRY);
			return false;
		}
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		FrontUserProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(FrontUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.fatal(e);
		}
		// 判断成功
		boolean success = (product != null);

		// 打印结果或者失败
		if (success) {
			print(product.list());
		} else {
			SeekFrontSite cmd = getCommand();
			faultX(FaultTip.FAILED_X, cmd);
		}

		return useful(success);
	}

//	/**
//	 * 打印空格
//	 */
//	private void printGap() {
//		ShowItem showItem = new ShowItem();
//		for (int i = 0; i < 3; i++) {
//			showItem.add(new ShowStringCell(i, ""));
//		}
//		// 增加一行记录
//		addShowItem(showItem);
//	}
	
	/**
	 * 打印结果
	 * @param array
	 */
	private void print(List<FrontDetail> array) {
		// 显示时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "SEEK-FRONT-USER/USERNAME",
				"SEEK-FRONT-USER/SERVER", "SEEK-FRONT-USER/CLIENT" });

		// 记录
		int index = 0;
		for (FrontDetail detail : array) {
			if (index > 0) printGap(3);

			// 大于0，统计值增1
			if (detail.size() > 0) index++;

			for (FrontItem e : detail.list()) {
				// 显示数组
				Object[] a = new Object[] { e.getUsername(),
						detail.getLocal(), e.getFront() };
				printRow(a);

				//				ShowItem item = new ShowItem();
				//				item.add(new ShowStringCell(0, e.getUsername()));
				//				item.add(new ShowStringCell(1, detail.getLocal()));
				//				item.add(new ShowStringCell(2, e.getFront()));
				//				addShowItem(item);
			}
		}

		// 输出全部记录
		flushTable();
	}

}
