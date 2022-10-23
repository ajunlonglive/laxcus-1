/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.ray.*;
import com.laxcus.util.display.show.*;

/**
 * 设置命令超时调用器
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public class RayCommandTimeoutInvoker extends RayInvoker {

	/**
	 * 构造设置命令超时调用器，指定命令
	 * @param cmd 命令超时命令
	 */
	public RayCommandTimeoutInvoker(CommandTimeout cmd) {
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
	public CommandTimeout getCommand() {
		return (CommandTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CommandTimeout cmd = getCommand();

		RayLauncher launcher = getLauncher();
		launcher.setCommandTimeout(cmd.getInterval());
		
		print(cmd.getInterval());

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
	 * 打印时间
	 * @param ms
	 */
	private void print(long ms) {
		createShowTitle(new String[] { "COMMAND-TIMEOUT/TIME" });
		
		String text = doStyleTime(ms);

		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, text));
		addShowItem(item);
		
		// 输出全部记录
		flushTable();
		
//		this.printThreads();
	}
	
//	private void printThreads() {
//		Map<Thread, StackTraceElement[]>	maps = Thread.getAllStackTraces();
//		Iterator<Map.Entry<Thread, StackTraceElement[]>> iterator =	maps.entrySet().iterator();
//		int index = 1;
//		while(iterator.hasNext()) {
//			Map.Entry<Thread, StackTraceElement[]> entry = iterator.next();
//			Thread thread = entry.getKey();
//			System.out.printf("thread %d is %s\n",index, thread.getName());
//			index++;
//		}
//	}

}