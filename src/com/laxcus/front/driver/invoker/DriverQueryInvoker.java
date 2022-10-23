/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.front.driver.mission.*;
import java.util.*;

import com.laxcus.access.index.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.law.rule.*;

/**
 * SQL查询任务调用器 <br>
 * 
 * 它是SELECT、DELETE、UPDATE调用器的父类。
 * 
 * @author scott.liang
 * @version 1.0 01/08/2015
 * @since laxcus 1.0
 */
public abstract class DriverQueryInvoker extends DriverInvoker {

	/**
	 * 构造默认的SQL查询调用器，指定命令
	 * @param mission 驱动任务
	 */
	protected DriverQueryInvoker(DriverMission mission) {
		super(mission);
	}

	/**
	 * 收集表名
	 * @param where
	 * @return
	 */
	private List<Space> collect(Where where) {
		ArrayList<Space> array = new ArrayList<Space>();
		WhereIndex index = where.getIndex();
		if (index.getClass() == NestedIndex.class) {
			NestedIndex sub = (NestedIndex) index;
			array.add(sub.getSelect().getSpace());
		} else if (index.getClass() == OnIndex.class) {
			OnIndex on = (OnIndex) index;
			array.add(on.getLeft().getSpace());
			array.add(on.getRight().getSpace());
		}
		return array;
	}
	
	/**
	 * 生成数据表规则集合
	 * @param cmd 查询命令
	 * @param operator 操作符
	 * @return 数据表规则集合
	 */
	protected List<RuleItem> createTableRules(Query cmd, byte operator) {
		ArrayList<RuleItem> array = new ArrayList<RuleItem>();

		// 共享读
		TableRuleItem root = new TableRuleItem(operator, cmd.getSpace());
		array.add(root);

		// 收集WHERE语句嵌套的表名
		List<Space> spaces = collect(cmd.getWhere());
		// 共享读集合
		for (Space space : spaces) {
			TableRuleItem sub = new TableRuleItem(operator, space);
			array.add(sub);
		}
		return array;
	}
	
}