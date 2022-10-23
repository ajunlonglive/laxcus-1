/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command;

import java.security.*;

/**
 * 命令操作许可 <br>
 * 
 * 在运行过程中，对命令的方法调用时的检查<br><br>
 * 
 * 在各节点 *.policy 文件中的格式：<br>
 * permission com.laxcus.command.CommandPermission "using.[Command方法名]"; <br><br> 
 * 
 * 注意：CommandPermission只需要方法名称（目标名称），不包含操作行为列表，即"actions"项是忽略的。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2014
 * @since laxcus 1.0
 */
public class CommandPermission extends BasicPermission {

	private static final long serialVersionUID = 6775185385902511193L;

	/**
	 * 构造命令操作许可
	 * @param name 目标名称
	 */
	public CommandPermission(String name) {
		super(name);
	}

	/**
	 * 构造命令操作许可
	 * @param name 目标名称
	 * @param actions 操作行为
	 */
	public CommandPermission(String name, String actions) {
		super(name, actions);
	}

}