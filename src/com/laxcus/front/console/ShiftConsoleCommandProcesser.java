/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.console;

import java.io.*;

import com.laxcus.echo.invoke.*;
import com.laxcus.front.meet.*;

/**
 * 转发命令处理器
 * 
 * @author scott.liang
 * @version 1.0 8/29/2020
 * @since laxcus 1.0
 */
public abstract class ShiftConsoleCommandProcesser {

	/** JAVA控制台 */
	private Console console;
	
	/** 确认器 **/
	private MeetCommandAuditor auditor;

	/**
	 * 构造默认的转发命令处理器
	 * 
	 * @param console 控制台
	 */
	protected ShiftConsoleCommandProcesser(Console console, MeetCommandAuditor auditor) {
		super();
		setConsole(console);
		setCommandAuditor(auditor);
	}

	/**
	 * 设置控制台实例
	 * 
	 * @param e 实例
	 */
	public void setConsole(Console e) {
		console = e;
	}

	/**
	 * 返回控制台实例
	 * @return Console
	 */
	protected Console getConsole() {
		return console;
	}

	/**
	 * 设置命令核准接口
	 * @param e
	 */
	public void setCommandAuditor(MeetCommandAuditor e) {
		auditor = e;
	}

	/**
	 * 返回命令核准接口
	 * @return
	 */
	protected MeetCommandAuditor getCommandAuditor() {
		return auditor;
	}

	/**
	 * 异步调用器转发给管理池处理
	 * 
	 * @param invoker
	 */
	protected void launch(EchoInvoker invoker) {
		InvokerPool pool = ConsoleLauncher.getInstance().getInvokerPool();
		pool.launch(invoker);
	}
	
	/**
	 * 输入一行
	 * @param input
	 * @return
	 */
	protected String readLine(String input) {
		return console.readLine("%s", input);
	}

	/**
	 * 处理命令
	 */
	public abstract void process();

}