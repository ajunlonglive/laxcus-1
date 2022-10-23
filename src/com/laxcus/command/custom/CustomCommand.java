/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.custom;

import com.laxcus.command.*;

/**
 * 自定义命令 <br><BR>
 * 
 * CustomCommand是系统提供基础接口，用户根据自己业务需求，由用户实现的第三方扩展命令。
 * 在运行过程中，它被部署在扩展目录下，遵循Invoke/Produce机制，和系统命令一起使用。<BR>
 * 
 * 所有由用户实现，需要在系统中运行的命令，都属于自定义命令。<br>
 * 自定义命令属于LAXCUS增值业务。<br>
 * 
 * 每个CustomCommand都有一个关联的CustomInvoker子类来实现。
 * 
 * @author scott.liang
 * @version 1.0 6/12/2017
 * @since laxcus 1.0
 */
public abstract class CustomCommand extends Command {

	private static final long serialVersionUID = 5091114341837370385L;

	/**
	 * 构造默认的自定义命令
	 */
	protected CustomCommand() {
		super();
	}

	/**
	 * 生成自定义命令的副本
	 * 
	 * @param that 用户自定义命令
	 */
	protected CustomCommand(CustomCommand that) {
		super(that);
	}

}