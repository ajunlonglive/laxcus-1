/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.dispatcher;

import com.laxcus.echo.invoke.*;
import com.laxcus.front.desktop.*;
import com.laxcus.front.desktop.pool.*;
import com.laxcus.platform.listener.*;
import com.laxcus.ui.display.*;

/**
 * 转发命令处理器
 * 
 * @author scott.liang
 * @version 1.0 8/29/2020
 * @since laxcus 1.0
 */
public abstract class ShiftCommandProcesser {
	
	/** 自动处理，如果是，不允许CommandAuditor去核准执行 **/
	protected boolean auto;

	/** 确认器 **/
	protected CommandAuditor auditor;

	/** 显示界面 **/
	protected MeetDisplay display;

	/**
	 * 构造转发命令处理器
	 * @param auto 自动处理
	 * @param auditor 命令核准器
	 * @param display 交互显示接口
	 */
	protected ShiftCommandProcesser(boolean auto, CommandAuditor auditor, MeetDisplay display) {
		super();
		setAuto(auto);
		setCommandAuditor(auditor);
		setMeetDisplay(display);
	}
	
	/**
	 * 转发命令处理器
	 * @param auditor
	 * @param display
	 */
	protected ShiftCommandProcesser(CommandAuditor auditor, MeetDisplay display) {
		this(false, auditor, display);
	}
	
	/**
	 * 设置自动处理
	 * @param b
	 */
	public void setAuto(boolean b) {
		auto = b;
	}

	/**
	 * 判断自动处理
	 * @return
	 */
	public boolean isAuto() {
		return auto;
	}

	/**
	 * 设置命令核准接口
	 * @param e
	 */
	public void setCommandAuditor(CommandAuditor e) {
		auditor = e;
	}

	/**
	 * 返回命令核准接口
	 * @return
	 */
	protected CommandAuditor getCommandAuditor() {
		return auditor;
	}
	
	/**
	 * 设置显示接口
	 * @param e
	 */
	public void setMeetDisplay(MeetDisplay e) {
		display = e;
	}
	
	/**
	 * 返回显示接口
	 * @return
	 */
	protected MeetDisplay getMeetDisplay() {
		return display;
	}

	/**
	 * 异步调用器转发给管理池处理
	 * 
	 * @param invoker 异步调用器
	 * @return 成功返回真，否则假
	 */
	protected boolean launch(EchoInvoker invoker) {
		DesktopInvokerPool pool = DesktopLauncher.getInstance().getInvokerPool();
		return pool.launch(invoker, display);
	}

	/**
	 * 处理命令
	 */
	public abstract void process();

}


///**
// * 输入一行
// * @param input
// * @return
// */
//protected String readLine(String input) {
//	return auditor.readLine(input);
//}