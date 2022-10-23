/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.awt.*;

import com.laxcus.command.mix.*;
import com.laxcus.ray.*;
import com.laxcus.util.display.show.*;

/**
 * 设置命令处理模式调用器
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class RayCommandModeInvoker extends RayInvoker {

	/**
	 * 构造设置命令处理模式调用器，指定命令
	 * @param cmd 设置命令处理模式命令
	 */
	public RayCommandModeInvoker(CommandMode cmd) {
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

		RayLauncher launcher = getLauncher();
		launcher.setMemory(cmd.isMemoryMode());
		
		// 打印结果
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
	 * @param memory
	 */
	private void print(boolean memory) {
		createShowTitle(new String[]{"COMMAND-MODE/MODE"});
		
		Color foreground = findXMLForeground("COMMAND-MODE/MODE");
		
		ShowItem item = new ShowItem();
		String text = (memory ? getXMLContent("COMMAND-MODE/MODE/MEMORY") : getXMLContent("COMMAND-MODE/MODE/DISK"));
		item.add(new ShowStringCell(0, text, foreground));
		addShowItem(item);
		
//		// 在状态栏显示
//		setStatusText(text);
		
		// 输出全部记录
		flushTable();
	}
}
