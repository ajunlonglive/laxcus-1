/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 设置站点日志命令调用器
 * 
 * @author scott.liang
 * @version 1.0 08/16/2017
 * @since laxcus 1.0
 */
public class RaySetLogLevelInvoker extends RayInvoker {

	/**
	 * 构造设置站点日志命令调用器，指定命令
	 * @param cmd - 设置站点日志命令
	 */
	public RaySetLogLevelInvoker(SetLogLevel cmd) {
		super(cmd);
		// 指定快速处理
		cmd.setQuick(true);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetLogLevel getCommand() {
		return (SetLogLevel) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 发送到注册站点，由注册站点再分发
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		SetLogLevelProduct product = null;
		// 判断成功
		if (isSuccessObjectable(index)) {
			try {
				product = getObject(SetLogLevelProduct.class, index);
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
	private void print(List<SetLogLevelItem> array) {
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "SET-LOGLEVEL/STATUS", "SET-LOGLEVEL/SITE" });

		for (SetLogLevelItem e : array) {
			ShowItem item = new ShowItem();
			// 图标
			item.add(createConfirmTableCell(0, e.isSuccessful()));
			item.add(new ShowStringCell(1, e.getSite()));
			// 保存单元
			addShowItem(item);
		}
		// 输出全部记录
		flushTable();
	}

}