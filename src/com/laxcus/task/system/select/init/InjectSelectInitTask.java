/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.init;

import java.util.*;

import com.laxcus.access.index.*;
import com.laxcus.access.index.section.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.system.select.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 查询插入的INIT任务
 * 
 * @author scott.liang
 * @version 1.0 11/30/2020
 * @since laxcus 1.0
 */
public class InjectSelectInitTask extends InitTask {

	/**
	 * 构造默认的查询插入任务
	 */
	public InjectSelectInitTask() {
		super();
	}

	/**
	 * 指定平衡分配接口
	 * @param select
	 * @return
	 */
	private BalanceObject initBalance() {
		return new BalanceObject(InjectSelectTaskKit.BALANCE);
	}
	
	/**
	 * 用递归的方式，取出每一段SELECT，保存到数组里
	 * @param select SELECT语句命令
	 * @param array 数据
	 */
	private void recursion(Select select, ArrayList<Select> array) {
		if (select.hasNested()) {
			Where where = select.getWhere();
			WhereIndex index = where.getIndex();
			// 有嵌套查询， 继续递归
			Select sub = ((NestedIndex) index).getSelect();
			recursion(sub, array);
		}
		// 保存到数组
		array.add(select);
	}
	
	/**
	 * @param select
	 * @param conduct
	 * @return
	 * @throws TaskException
	 */
	private int createFromTask(Select select, Conduct conduct) throws TaskException {
		Space space = select.getSpace();
		// 建立数据块分区
		List<StubSector> list = getFromSeeker().createStubSector(getInvokerId(), space);

		/*
		 * 根据WORK节点数量为后续的TO阶段分片
		 * 分片原则： 节点数量可多不可少。当实际WORK节点不足时，这些片段可以在BALANCE阶段合并；
		 * 反之WORK节点多而片段少时，因为分片对应的数据已经固定存在，分布则不能分割。默认设置1000个WORK节点，这个数量足够大。
		 */
		short columnId = select.getColumnIds()[0];

		Dock dock = new Dock(space, columnId);
		
//		// 根据节点数量和列空间生成分区
//		int sites = SQLTaskKit.DEFAULT_WORKSITES;
		
		// 找到WORK.TO阶段地址数目
		Phase toPhase = InjectSelectTaskKit.TO; // new Phase(PhaseTag.TO, new Sock("", ""));
		NodeSet set = getToSeeker().findToSites(getInvokerId(), toPhase);
		int sites = (set != null ? set.size() : 0);
		if (sites < 1) {
			throw new TaskNotFoundException("work site missing! %s", toPhase);
		}

		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, sites);

		// 采用SELECT标准分片
		Phase fromPhase = SelectTaskKit.FROM_STANDARD;

		// 命名FROM输出，保存多个FROM阶段命名
		FromDispatcher dispatcher = new FromDispatcher(fromPhase);
		dispatcher.setIndexSector(sector);

		for (StubSector stub : list) {
			Node node = stub.getRemote();

			// 含数据块编号的SELECT命令
			CastSelect cmd = new CastSelect(select, stub.list());

			// 每一个具体的FROM任务(定义：目标主机，SELECT，分片区)
			FromSession session = new FromSession(fromPhase, node);
			session.setCommand(cmd);
			session.setIndexSector(sector);
			// 保存它
			dispatcher.addSession(session);
		}

		// 设置FROM任务
		FromObject from = new FromObject(fromPhase);
		from.setDispatcher(dispatcher);
		conduct.setFromObject(from);

		return 1;
	}
	
	private int createFromTask(List<Select> array, int seek, Conduct conduct) throws TaskException {
		Select select = array.get(seek);
		if (seek + 1 < array.size()) {
			Select preselect = array.get(seek + 1);
			byte operator = preselect.getWhere().getCompare();

			// 如果是EXISTS/NOT EXISTS比较符，需要两组语句配置...
			if (CompareOperator.isExists(operator)) {
				// createExistsFromTask(seek,preselect, conduct);
				return 2;
			} else if (CompareOperator.isNotExists(operator)) {
				return 2;
			}
		}

		// 以上成立时，在此处理
		createFromTask(select, conduct);
		return 1;
	}
	
	/**
	 * 生成TO阶段对象
	 * @param select
	 * @param conduct
	 * @throws TaskException
	 */
	protected void createToTask(InjectSelect inject, Conduct conduct) throws TaskException {
//		Select select = inject.getSelect();
//		Space space = select.getSpace();
		
		Phase phase = InjectSelectTaskKit.TO;
		checkToSites(phase);
		
		// 设置当前阶段的输入/分派器，保存到conduct配置中
		ToInputter inputter = new ToInputter(phase);
		ToDispatcher dispatcher = new ToDispatcher(phase);
		dispatcher.addCommand(InjectSelectTaskKit.INJECT_SELECT_OBJECT, inject); //做为自定义参数保存。这个参数将跟随会话传递到不同目标站点

		// 执行分派!
		ToObject object = new ToObject(ToMode.EVALUATE, phase);
		object.setInputter(inputter);
		object.setDispatcher(dispatcher);

		conduct.attachToObject(object);
	}
	
	/**
	 * 数据输出接口由终端处理
	 * @param select
	 * @return
	 */
	private PutObject createPut(Select select) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.init.InitTask#init(com.laxcus.command.conduct.Conduct)
	 */
	@Override
	public Conduct init(Conduct conduct) throws TaskException {
		// 取出SELECT命令
		InitObject init = conduct.getInitObject();
		InjectSelect inject = (InjectSelect) init.findCommand("INJECT_SELECT_OBJECT"); // "INJECT_SELECT_OBJECT"在MeetInjectSelectInvoker中定义。

		Select select = inject.getSelect();
		// 递归分割SELECT，保存到数组
		ArrayList<Select> array = new ArrayList<Select>();
		recursion(select, array);
		
		// 处理FROM阶段任务
		int seek = 0;
		int size = createFromTask(array, seek, conduct);
		seek += size;
		
		// 最后一次TO阶段操作，实现数据插入

		// 根据SELECT，生成TO对象链表
		createToTask(inject, conduct);
		
		// 平衡分布计算接口
		BalanceObject balance = initBalance();
		conduct.setBalanceObject(balance);
		
		// 生成PUT显示
		PutObject put = createPut(select);
		conduct.setPutObject(put);

		// 返回结果
		return conduct;
	}

}
