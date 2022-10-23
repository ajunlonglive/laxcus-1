/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.init;

import java.util.*;

import com.laxcus.access.index.section.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.command.access.select.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.system.select.util.*;
import com.laxcus.util.naming.*;

/**
 * <code>SQL SELECT</code>标准检索，支持<code>"GROUP BY"</code>、<code>"ORDER BY"</code>、<code>DISTINCT</code> <BR><BR>
 * 
 * 按照SELECT的规范，三个子语句的处理顺序是：<br>
 * 1.<code>GROUP BY</code> <br>
 * 2.<code>DISTINCT</code> <br>
 * 3.<code>ORDER BY</code> <br>
 * 
 * @author scott.liang
 * @version 1.1 10/07/2013
 * @since laxcus 1.0
 */
public class SelectInitTask extends SQLInitTask {

	/** 工作节点数目 **/
	private int defaultWorkSites;

	/**
	 * 建立SelectInitTask实例
	 */
	public SelectInitTask() {
		super();
		defaultWorkSites = 0;
	}

	/**
	 * 选择子命名，建立TO阶段命名
	 * @param sub 子命名
	 * @return 返回Phase实例
	 */
	private Phase createToPhase(String sub) {
		Sock root = Sock.doSystemSock(SelectTaskKit.SELECT);
		return new Phase(getIssuer(), PhaseTag.TO, root, sub);
	}

	/**
	 * 建立FROM阶段命名
	 * @return 返回Phase实例
	 */
	private Phase createFromStandard() {
		Sock root = Sock.doSystemSock(SelectTaskKit.SELECT);
		return new Phase(getIssuer(), PhaseTag.FROM, 
				root, SelectTaskKit.STANDARD);
	}

	/**
	 * 根据SELECT检索参数生成FROM阶段对象。<br>
	 * <b>注意：在FROM阶段，不用设置索引分区（IndexSector），由它后面的TO阶段给FROM阶段设置。</b><br><br>
	 * 
	 * @param select
	 * @param conduct
	 * @throws TaskException
	 */
	protected void createFromTask(Select select, Conduct conduct) throws TaskException {
		Space space = select.getSpace();

		Logger.debug(getIssuer(), this, "createFromTask", "goto createStubSector by '%s'", space);

		// 建立数据块索引区
		List<StubSector> list = getFromSeeker().createStubSector(getInvokerId(), space);

		Logger.debug(getIssuer(), this, "createFromTask", "StubSector size is:%d", list.size());

		Phase phase = createFromStandard();

		// SelectTaskKit.FROM_STANDARD;
		// 建立FROM阶段分发器，保存多个FROM会话(分区由后面的TO任务设置)
		FromDispatcher dispatcher = new FromDispatcher(phase);

		for (StubSector sector : list) {
			Node node = sector.getRemote();
			// 产生命令
			CastSelect cmd = new CastSelect(select, sector.list());
			// 建立FROM会话
			FromSession session = new FromSession(phase, node);
			session.setCommand(cmd);
			// 保存会话
			dispatcher.addSession(session);
		}

		Logger.debug(getIssuer(), this, "createFromTask", "from session size:%d", dispatcher.size());

		FromObject object = new FromObject(phase);
		object.setDispatcher(dispatcher);

		conduct.setFromObject(object);
	}

	/**
	 * 设置阶段任务
	 * @param phase
	 * @param dock
	 * @param select
	 * @param conduct
	 * @throws TaskException
	 */
	protected void setToTask(Phase phase, Dock dock, Select select, Conduct conduct) throws TaskException {
		// 检查阶段命名，如果不存在弹出异常
		super.checkToSites(phase);

		// 生成分区
		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, defaultWorkSites); // SQLTaskKit.DEFAULT_WORKSITES);

		Logger.debug(getIssuer(), this, "setToTask", "sector size is %d", sector.size());

		// 根据当前对象需求生成的索引分区，设置给它的前面对象。
		// 采用倒推方法设置，即如果是TO对象，将设置给FROM对象，如果是SUBTO对象，设置给前面一个TO对象或者SUBTO对象
		setPreviousSector(sector, conduct);

		// 设置当前阶段的输入/分派器，保存到conduct配置中
		ToInputter inputter = new ToInputter(phase);
		ToDispatcher dispatcher = new ToDispatcher(phase);
		dispatcher.addCommand(SQLTaskKit.SELECT_OBJECT, select); //做为自定义参数保存。这个参数将跟随会话传递到不同目标站点

		ToObject object = new ToObject(ToMode.EVALUATE, phase);
		object.setInputter(inputter);
		object.setDispatcher(dispatcher);

		conduct.attachToObject(object);
	}

	/**
	 * 生成TO对象，并且给前面的阶段设置分区。<br>
	 * 分片是按照节点数对上阶段的数据进行切割。分片原则:可多不可少，这样实际命名主机不足时有缩小余地，反之分片少则不能放大
	 * 
	 * @param select
	 * @param conduct
	 * @throws TaskException
	 */
	protected void createToTask(Select select, Conduct conduct) throws TaskException {
		GroupByAdapter group = select.getGroup();
		OrderByAdapter order = select.getOrder();
		Space space = select.getSpace();

		// 分配TO迭代链，顺序是"GROUP BY"优先，DISTINCT次之，"ORDER BY"最后	 
		if (group != null) {
			// 以GROUP BY为参照，为前面的阶段设置分区
			Dock dock = new Dock(space, group.getColumnIds()[0]);
			Phase phase = createToPhase(SelectTaskKit.GROUPBY);
			setToTask(phase, dock, select, conduct);
		}

		// 消除重复的行
		if(select.isDistinct()) {
			// 按照"GROUP BY"/"ORDER BY"/"显示列"的排列顺序，为DISTINCT前面的阶段设置分区
			short columnId = select.getColumnIds()[0];
			if (group != null) {
				columnId = group.getColumnIds()[0];
			} else if (order != null) {
				columnId = order.getColumnId();
			}
			Dock dock = new Dock(space, columnId);

			Phase phase = createToPhase(SelectTaskKit.DISTINCT);
			setToTask(phase, dock, select, conduct);
		}

		// ORDER BY排序
		if (order != null) {
			Dock dock = new Dock(space, order.getColumnId());
			Phase phase = createToPhase(SelectTaskKit.ORDERBY);
			setToTask(phase, dock, select, conduct);
		}
	}

	/**
	 * 指定平衡分配接口
	 * @param select
	 * @return
	 */
	private BalanceObject createBalance(Select select) {
		return new BalanceObject(SelectTaskKit.BALANCE); 
	}

	/**
	 * 数据输出接口由终端处理
	 * @param select
	 * @return
	 */
	private PutObject createPut(Select select) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.init.InitTask#init(com.laxcus.command.conduct.Conduct)
	 */
	@Override
	public Conduct init(Conduct conduct) throws TaskException {
		// 获得全部WORK节点
		defaultWorkSites = getToSeeker().getToSites(getInvokerId());
		if (defaultWorkSites < 1) {
			throw new TaskNotFoundException("not found work sites!");
		}
		
		// 从INIT对象中取出自定义的SELECT命令（见FRONT.xxxSelectInvoker中的定义）
		InitObject init = conduct.getInitObject();
		Select select = (Select) init.findCommand("SELECT_OBJECT");

		// 不要主动处理SQL函数，由GROUP BY/ORDER BY去完成
		select.setAutoAdjust(false);

		// 根据SELECT，生成FROM阶段分配器，FROM输入器忽略
		createFromTask(select, conduct);

		// 根据SELECT，生成TO对象链表
		createToTask(select, conduct);

		// 平衡分配
		BalanceObject balance = createBalance(select);
		conduct.setBalanceObject(balance);

		// 生成PUT显示
		PutObject put = createPut(select);
		conduct.setPutObject(put);

		return conduct;
	}

}