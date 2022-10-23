/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.util.*;

import com.laxcus.access.index.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * SELECT命令转义器
 * 
 * @author scott.liang
 * @version 1.0 2/8/2018
 * @since laxcus 1.0
 */
public class SelectShifter extends DriverShifter {

	/**
	 * 构造默认的SELECT命令转义器
	 */
	public SelectShifter() {
		super();
	}
	
	/**
	 * 启动SQL.SELECT异步检索。<br><br>
	 * 
	 * SELECT检索有三种可能：<br>
	 * 1. 是嵌套检索（SUB SELECT），转给CONDUCT命令执行。<br>
	 * 2. 带“ORDER BY/GROUP BY/DISTINCT”关键字，转给CONDUCT命令执行。<br>
	 * 3. 是“SELECT * FROM schema.table WHERE ... AND|OR ...” 语句，直接执行。<br>
	 * 
	 * @see com.laxcus.front.driver.invoker.DriverShifter#createInvoker(com.laxcus.front.driver.mission.DriverMission)
	 * @return 返回异步调用器实例
	 */
	@Override
	public DriverInvoker createInvoker(DriverMission mission) {
		// 必须是指定命令
		if (!Laxkit.isClassFrom(mission.getCommand(), Select.class)) {
			mission.setException("cannot be cast!");
			return null;
		}

		Select select = (Select) mission.getCommand();

		if (select.hasNested()) {
			return createSubSelect(mission, select);
		} else if (select.isDistinct() || select.getGroup() != null
				|| select.getOrder() != null) {
			return createStandardSelect(mission, select);
		} else {
			return createDirectSelect(mission);
		}
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
	
	/**
	 * 根据INIT阶段命名，生成CONDUCT异步调用器，转发执行。
	 * @param root 根命名
	 * @return 成功受理返回“真”，否则“假”。
	 */
	private DriverInvoker translate(DriverMission mission, Select select, Sock root) {
		Phase phase = new Phase(getUsername(), PhaseTag.INIT, root);
		InitObject initObject = new InitObject(phase);
		initObject.addCommand("SELECT_OBJECT", select);

		// 设置共享读操作，生成全部表规则
		List<RuleItem> rules = createTableRules(select, RuleOperator.SHARE_READ);
		// 保存表规则
		initObject.addRules(rules);

		// 构造分布计算实例
		Conduct conduct = new Conduct(root);
		// 设置初始化命名对象，数据资源的处理，如参数分配、数据分片等，到CALL.INIT上执行
		conduct.setInitObject(initObject);
		
		// 保存原语
		conduct.setPrimitive(select.getPrimitive());
		// 更新命令
		mission.setCommand(conduct);
		// 返回驱动调用器
		return new DriverConductInvoker(mission);
	}

	private DriverInvoker createSubSelect(DriverMission mission, Select select) {
		// SUBSELECT是系统嵌套检索的根命名
		String rootText = "SUBSELECT";
		Sock root = Sock.doSystemSock(rootText);
		return translate(mission, select, root);
	}
	
	/**
	 * 建立一个标准SQL.SELECT检索。这个接口定义“CONDUCT.INIT”阶段，分配SELECT句，其它操作到CALL.INIT中去执行。
	 * @return 接受返回“真”，否则“假”。
	 */
	private DriverInvoker createStandardSelect(DriverMission mission, Select select) {
		// "SELECT"命名在tasks.xml和SelectTaskKit中定义。
		String rootText = "SELECT";
		Sock root = Sock.doSystemSock(rootText);
		return translate(mission, select, root);
	}
	
	/**
	 * 建立一个简单的SELECT检索，交给调用器处理
	 * @return 成功返回真，否则假
	 */
	private DriverInvoker createDirectSelect(DriverMission mission) {
		return new DriverDirectSelectInvoker(mission);
	}

}