/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.custom;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.ui.display.*;

/**
 * 用于FRONT显示界面的自定义调用器。
 * 
 * 所有用于FRONT节点显示界面的自定义调用器，从这里派生
 * 
 * @author scott.liang
 * @version 1.0 5/31/2021
 * @since laxcus 1.0
 */
public abstract class CustomDisplayInvoker extends EchoInvoker {

	/** 临时显示接口，在生成调用器时设置 **/
	private MeetDisplay display;

	/**
	 * 构造默认的FRONT调用器
	 */
	protected CustomDisplayInvoker() {
		super();
	}

	/**
	 * 构造交互操作异步调用器调用器，指定命令
	 * @param cmd 异步命令
	 */
	protected CustomDisplayInvoker(Command cmd) {
		super(cmd);
	}
	
	/**
	 * 设置静态的临时显示接口。在进程启动时设置
	 * @param e MeetDisplay实例
	 */
	public void setDisplay(MeetDisplay e) {
		display = e;
	}

	/**
	 * 返回静态的临时显示接口
	 * @return MeetDisplay实例
	 */
	public MeetDisplay getDisplay() {
		return display;
	}
	
}
