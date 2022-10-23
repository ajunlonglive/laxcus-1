/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.*;
import com.laxcus.command.mix.*;
import com.laxcus.util.display.show.*;
import com.laxcus.watch.*;

/**
 * 设置命令优先级调用器
 * 
 * @author scott.liang
 * @version 1.0 1/20/2020
 * @since laxcus 1.0
 */
public class WatchCommandRankInvoker extends WatchInvoker {

	/**
	 * 构造设置命令优先级调用器，指定命令
	 * @param cmd 命令优先级命令
	 */
	public WatchCommandRankInvoker(CommandRank cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CommandRank getCommand() {
		return (CommandRank) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CommandRank cmd = getCommand();

		WatchLauncher launcher = getLauncher();
		launcher.setCommandPriority(cmd.getRank());
		print(cmd.getRank());

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
	 * 打印优先级
	 * @param priority
	 */
	private void print(byte priority) {
		createShowTitle(new String[] { "COMMAND-PRIORITY/TYPE" });

		// 检查，选择文本描述
		String text = "Unknown";
		switch (priority) {
		case CommandPriority.FAST:
			text = getXMLContent("COMMAND-PRIORITY/TYPE/FAST");
			break;
		case CommandPriority.MAX:
			text = getXMLContent("COMMAND-PRIORITY/TYPE/MAX");
			break;
		case CommandPriority.NORMAL:
			text = getXMLContent("COMMAND-PRIORITY/TYPE/NORMAL");
			break;
		case CommandPriority.MIN:
			text = getXMLContent("COMMAND-PRIORITY/TYPE/MIN");
			break;
		case CommandPriority.NONE:
			text = getXMLContent("COMMAND-PRIORITY/TYPE/NONE");
			break;
		}

		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, text));
		addShowItem(item);

		// 输出全部记录
		flushTable();
	}

}