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
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * “显示数据块尺寸”命令的异步调用器。
 * 
 * @author scott.liang
 * @version 1.1 9/2/2012
 * @since laxcus 1.0
 */
public class DesktopShowEntitySizeInvoker extends DesktopInvoker {

	/**
	 * 构造“显示数据块尺寸”命令的异步调用器，指定命令
	 * @param cmd 显示数据块尺寸命令
	 */
	public DesktopShowEntitySizeInvoker(ShowEntitySize cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowEntitySize getCommand() {
		return (ShowEntitySize) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		ShowEntitySizeProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ShowEntitySizeProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.fatal(e);
		}
		
		// 判断是成功
		boolean success = (product != null);
		if (!success) {
			printFault();
			return false;
		}

		ShowEntitySize cmd = getCommand();
		Space space = cmd.getSpace();

		int size = product.getLength();
		if (size == -1) {
			fault(String.format("cannot find %s size", space));
			return false;
		}

		String text = String.format("%d M", size / 1024 / 1024);

		// 标题
		ShowTitle title = new ShowTitle();
		title.add(new ShowTitleCell(0, space.toString()));
		setShowTitle(title);
		// 列单元
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, text));
		addShowItem(item);

		// 输出全部记录
		flushTable();
		
		return useful();
	}

}