/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * “更新数据块尺寸”命令的异步调用器。
 * 
 * @author scott.liang
 * @version 1.1 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopSetEntitySizeInvoker extends DesktopInvoker {

	/**
	 * 构造数据块尺寸命令的异步调用器，指定数据块尺寸命令。
	 * @param cmd 设置数据块尺寸
	 */
	public DesktopSetEntitySizeInvoker(SetEntitySize cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetEntitySize getCommand() {
		return (SetEntitySize) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return super.fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = getEchoKeys().get(0);
		SetEntitySizeProduct product = null;
		try {
			if (isSuccessCompleted()) {
				product = getObject(SetEntitySizeProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());
		
		// 显示处理结果
		print(success);

		return useful(success);
	}

	/**
	 * 打印结果
	 * @param success 成功或者否
	 */
	private void print(boolean success) {
		SetEntitySize cmd = getCommand();
		Space space = cmd.getSpace();
		int size = cmd.getSize();
		
		// 显示运行时间
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "SET-ENTITY-SIZE/STATUS",
				"SET-ENTITY-SIZE/TABLE", "SET-ENTITY-SIZE/SIZE" });
		
		ShowItem item = new ShowItem();
		item.add(createConfirmTableCell(0, success));
		item.add(new ShowStringCell(1, space));
		item.add(new ShowStringCell(2, ConfigParser.splitCapacity(size)));
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
	}
}