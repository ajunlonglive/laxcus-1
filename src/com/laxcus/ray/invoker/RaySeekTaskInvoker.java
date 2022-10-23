/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.task.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 检索分布任务组件的调用器。
 * 
 * 命令处理流程：
 * 1. WATCH -> TOP -> HOME -> DATA/WORK/CALL/BUILD -> HOME ->TOP -> WATCH
 * 2. WATCH -> HOME -> DATA/WORK/CALL/BUILD -> HOME -> WATCH
 * 
 * @author scott.liang
 * @version 1.0 5/26/2017
 * @since laxcus 1.0
 */
public class RaySeekTaskInvoker extends RayInvoker {

	/**
	 * 构造检索分布任务组件的调用器，指定命令
	 * @param cmd 检索分布任务组件命令
	 */
	public RaySeekTaskInvoker(SeekTask cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekTask getCommand() {
		return (SeekTask) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 发送到HOME/TOP站点的任意一个
		boolean success = (isTopHub() || isHomeHub());
		if (!success) {
			faultX(FaultTip.TOP_HOME_RETRY);
			return false;
		}
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SeekTask cmd = getCommand();
		SeekTaskProduct product = null;

		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SeekTaskProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			fault(e);
		}

		boolean success = (product != null);
		// 出错
		if (!success) {
			faultX(FaultTip.FAILED_X, cmd.getPrimitive());
			return useful(false);
		}

		// 打印结果
		print(product.list());

		return useful();
	}

	/**
	 * 打印结果
	 * @param array
	 */
	private void print(List<SeekTaskItem> array) {
		// 显示运行时间
		printRuntime();
		// 设置标题
		createShowTitle(new String[] { "SEEK-TASK/SITE", "SEEK-TASK/PHASE" });

		for (SeekTaskItem e : array) {
			printRow(new Object[] { e.getSite(), e.getPhase() });
		}
		// 输出全部记录
		flushTable();
	}

}