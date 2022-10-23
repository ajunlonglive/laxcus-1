/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command;

import java.util.*;

import com.laxcus.law.rule.*;

/**
 * 事务命令。<br>
 * 
 * 提供一批事务操作的事务处理单元。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2014
 * @since laxcus 1.0
 */
public abstract class RuleCommand extends Command {

	private static final long serialVersionUID = -3583659490137426189L;

	/**
	 * 构造默认的事务命令
	 */
	protected RuleCommand() {
		super();
	}

	/**
	 * 生成事务命令的数据副本
	 * @param that 事务命令
	 */
	protected RuleCommand(RuleCommand that) {
		super(that);
	}

	/**
	 * 获得关联的事务规则。这个方法由子类去实现。
	 * 
	 * @return 事务规则列表
	 */
	public abstract List<RuleItem> getRules();
}
