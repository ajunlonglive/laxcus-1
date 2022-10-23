/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import java.util.*;

import com.laxcus.command.cross.*;
import com.laxcus.law.cross.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 共享资源调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopShareCrossInvoker extends DesktopInvoker {

	/**
	 * 构造共享资源调用器，指定命令
	 * @param cmd 共享资源命令
	 */
	protected DesktopShareCrossInvoker(ShareCross cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShareCross getCommand() {
		return (ShareCross) super.getCommand();
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
		ShareCrossProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ShareCrossProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功或者失败
		boolean success = (product != null);
		if (success) {
			print(product.list());
		} else {
			printFault();
		}
		// 结束
		return useful(success);
	}

	/**
	 * 在窗口上显示处理单元
	 * @param array
	 */
	private void print(List<ShareCrossItem> array) {
		// 显示处理时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "CROSS-ITEM/USERNAME",
				"CROSS-ITEM/OPERATOR", "CROSS-ITEM/TABLE" });

		ShareCross cmd = getCommand();

		for (ShareCrossItem e : array) {
			ShowItem item = new ShowItem();

			// 根据用户签名，查找对应的用户明文
			String username = cmd.findText(e.getSiger());
			CrossFlag flag = e.getFlag();

			item.add(new ShowStringCell(0, username));

			// 翻译操作符
			String tokens = CrossOperator.translate(flag.getOperator());
			item.add(new ShowStringCell(1, tokens));
			// 表名
			item.add(new ShowStringCell(2, flag.getSpace()));

			// 增加一行记录
			addShowItem(item);
		}

		// 输出全部记录
		flushTable();
	}

}