/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.method;

import com.laxcus.front.*;
import com.laxcus.tub.command.*;
import com.laxcus.tub.product.*;
import com.laxcus.util.*;

/**
 * 边缘容器命令运行器
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public abstract class TubCommandRunner {

	/** FRONT节点 **/
	protected static FrontLauncher launcher;

	/**
	 * 设置FRONT节点实例 
	 * @param e FRONT节点
	 */
	public static void setLauncher(FrontLauncher e) {
		TubCommandRunner.launcher = e;
	}

	/**
	 * 返回FRONT节点实例
	 * @return FRONT节点
	 */
	public static FrontLauncher getLauncher() {
		return TubCommandRunner.launcher;
	}

	/** 边缘容器命令 **/
	private TubCommand command;

	/**
	 * 构造边缘容器命令运行器
	 * @param cmd 命令
	 */
	protected TubCommandRunner(TubCommand cmd) {
		super();
		setCommand(cmd);
	}

	/**
	 * 设置边缘计算命令（原始命令）
	 * @param e 命令句柄
	 * @throws NullPointerException - 如果是空值
	 */
	public final void setCommand(TubCommand e) {
		Laxkit.nullabled(e);
		command = e;
	}

	/**
	 * 返回边缘计算命令（原始命令！） <br>
	 * 
	 * @return TubCommand实例
	 */
	public TubCommand getCommand() {
		return command;
	}

	/**
	 * 返回处理报告
	 * @return TubProduct 实例
	 */
	public abstract TubProduct launch();

}