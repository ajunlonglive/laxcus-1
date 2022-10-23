/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.util.display.show.*;
import com.laxcus.watch.*;

/**
 * 图形界面日志可显示数目调用器
 * 
 * @author scott.liang
 * @version 1.0 11/7/2019
 * @since laxcus 1.0
 */
public class WatchMaxLogElementsInvoker extends WatchInvoker {

	/**
	 * 构造图形界面日志可显示数目调用器，指定命令
	 * @param cmd 可显示数目
	 */
	public WatchMaxLogElementsInvoker(MaxLogElements cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#isDistributed()
	 */
	@Override
	public boolean isDistributed() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MaxLogElements getCommand() {
		return (MaxLogElements) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MaxLogElements cmd = getCommand();

		WatchLauncher launcher = getLauncher();
		int size = launcher.setMaxLogs(cmd.getElements());
		print(size);

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 显示数字
	 * @param elements
	 */
	private void print(int elements) {
		// 标题
		createShowTitle(new String[] { "MAX-LOG-ELEMENTS/ELEMENTS" });
		ShowItem item = new ShowItem();
		item.add(new ShowIntegerCell(0, elements));
		addShowItem(item);
		// 输出全部记录
		flushTable();
	}

}