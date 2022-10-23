/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.site.front.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检索基于用户签名的用户登录信息调用器 <br><br><br>
 * 
 * 流程：<br>
 * 1. WATCH -> HOME -> CALL <br>
 * 2. WATCH -> BANK -> GATE <br>
 * 
 * @author scott.liang
 * @version 1.0 12/16/2017
 * @since laxcus 1.0
 */
public class RaySeekFrontUserInvoker extends RayInvoker {

	/**
	 * 构造检索基于用户签名的用户登录信息，指定命令
	 * @param cmd 检索基于用户签名的用户登录信息
	 */
	public RaySeekFrontUserInvoker(SeekFrontUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekFrontUser getCommand() {
		return (SeekFrontUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 要求是登录到BANK/HOME站点，否则不处理
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
		FrontUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(FrontUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null);
		if (success) {
			print(product.list());
		}

		return useful(success);
	}
	 
	/**
	 * 打印结果
	 * @param details
	 */
	private void print(List<FrontDetail> details) {
		// 显示时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "SEEK-FRONT-USER/USERNAME",
				"SEEK-FRONT-USER/SERVER", "SEEK-FRONT-USER/CLIENT" });

		SeekFrontUser cmd = getCommand();

		// 全部用户
		if (cmd.isAllUser()) {
			int index = 0;
			for (FrontDetail detail : details) {
				if (index > 0) printGap(3);
				if (detail.size() > 0)	index++;

				// 显示一个节点上的记录
				for (FrontItem e : detail.list()) {
					printRow(new Object[] { e.getUsername(), detail.getLocal(), e.getFront() });
				}
			}
		} else {
			List<Siger> sigers = cmd.getUsers();
			// 根据原来定义的用户签名， 逐一显示
			for (int index = 0; index < sigers.size(); index++) {
				// 显示空格
				if (index > 0) printGap(3);

				// 基于用户签名，显示这个签名相关的记录
				Siger siger = sigers.get(index);
				int count = 0;
				String username = cmd.findPlainText(siger);
				for (FrontDetail detail : details) {
					for (FrontItem e : detail.list()) {
						// 不匹配，忽略它！
						if (Laxkit.compareTo(e.getUsername(), siger) != 0) {
							continue;
						}
						// 显示结果
						printRow(new Object[] { username, detail.getLocal(), e.getFront() });

						count++;
					}
				}
				// 没有找到
				if (count == 0) {
					printRow(new Object[] { username, "", "" });
				}
			}
		}

		// 输出全部记录
		flushTable();
	}

}