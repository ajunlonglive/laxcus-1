/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.front.*;
import com.laxcus.util.display.show.*;

/**
 * 设置命令处理模式调用器
 * 
 * @author scott.liang
 * @version 1.0 05/30/2021
 * @since laxcus 1.0
 */
public class DesktopCommandModeInvoker extends DesktopInvoker {

	/**
	 * 构造设置命令处理模式调用器，指定命令
	 * @param cmd 设置命令处理模式命令
	 */
	public DesktopCommandModeInvoker(CommandMode cmd) {
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
	public CommandMode getCommand() {
		return (CommandMode) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CommandMode cmd = getCommand();

		FrontLauncher launcher = getLauncher();
		launcher.setMemory(cmd.isMemoryMode());
		print(cmd.isMemoryMode());

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 打印结果
	 * @param memory 内存模式
	 */
	private void print(boolean memory) {
		String text = (memory ? getXMLContent("COMMAND-MODE/MODE/MEMORY") : getXMLContent("COMMAND-MODE/MODE/DISK"));

		// 提示，并且显示在底栏
		createShowTitle(new String[] { "COMMAND-MODE/MODE" });
		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, text));
		addShowItem(item);
		// 输出全部记录
		flushTable();

		//		// 显示在底栏
		//		setStatusText(text);

		//		// 显示在底栏
		//		if (isTerminal()) {
		//			createShowTitle(new String[] { "COMMAND-MODE/MODE" });
		//			ShowItem item = new ShowItem();
		//			item.add(new ShowStringCell(0, text));
		//			addShowItem(item);
		//			// 输出全部记录
		//			flushTable();
		//			// 显示在底栏
		//			setStatusText(text);
		//		} else if(isConsole()) {
		//			setStatusText(text);
		//		}
	}

}