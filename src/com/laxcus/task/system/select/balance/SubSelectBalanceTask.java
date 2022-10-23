/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.balance;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.*;
import com.laxcus.access.index.section.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.access.type.*;
import com.laxcus.access.util.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.command.access.select.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.calculate.cyber.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.system.select.util.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 处理SELECT嵌套检索，以及平衡分布数据资源。
 * 
 * @author scott.liang
 * @version 1.1 5/17/2013
 * @since laxcus 1.0
 */
public class SubSelectBalanceTask extends BalanceTask {

	/**
	 * 构造默认的SubSelectBalanceTask实例
	 */
	public SubSelectBalanceTask() {
		super();
	}

	/**
	 * 生成默认字节数组
	 * @return 字节数组
	 */
	private byte[] createDefaultValue() {
		Conduct conduct = (Conduct) getCommand();
		InitObject init = conduct.getInitObject();
		Select select = (Select) init.findCommand("SELECT_OBJECT");
		// 生成一个空字节数组
		MassFlag flag = new MassFlag(select.getSpace());
		return flag.build();
	}

	/**
	 * 判断命令是降序
	 * @param phase
	 * @return
	 */
	private boolean isDESC(Phase phase, Select select) {
		// 判断是TO阶段命令
		if (!PhaseTag.isTo(phase) || select == null) {
			return false;
		}
		// 有任务子命名
		Naming sub = phase.getSub();
		if (sub == null) {
			return false;
		}
		OrderByAdapter order = select.getOrder();
		if (order == null) {
			return false;
		}
		// 判断是ORDERBY采用降序排序
		return (SelectTaskKit.ORDERBY.equalsIgnoreCase(sub.toString()) && order.isDESC());
	}

	//	private void checkWhere(Where where) {
	//		if (where == null) {
	//			return;
	//		}
	//		
	//		Logger.debug(getIssuer(), this, "checkWhere", "%d - %s\n", where.getColumnId(), 
	//				where.getIndex().getClass().getName());
	//		for (Where sub : where.getPartners()) {
	//			checkWhere(sub);
	//		}
	//		checkWhere(where.next());
	//	}

	/**
	 * 分配GENERATE类型的TO阶段会话，这些会话在WORK站点执行，实际请求指向DATA站点
	 * @param object
	 * @param areas
	 * @return - 分派资源后的TO阶段对象
	 * @throws TaskException
	 */
	private ToObject dispatchGenerate(ToObject object, FluxArea[] areas) throws TaskException {
		// TO阶段分派器
		ToDispatcher dispatcher = object.getDispatcher();
		Select select = (Select) dispatcher.findCommand(SQLTaskKit.SELECT_OBJECT);
		//		Select select = getSelect(dispatcher);

		//		// DEBUG START!
		//		Logger.debug(getIssuer(), this, "dispatchGenerate", "%s", select.getPrimitive());
		//		checkWhere(select.getWhere());
		//		// DEBUG END!

		// 产生数据块分区
		Space space = select.getSpace();
		List<StubSector> list = getFromSeeker().createStubSector(getInvokerId(), space);

		// 索引扇区
		ColumnSector sector = dispatcher.getIndexSector();

		// 本次阶段命名
		Phase phase = object.getPhase();
		for (StubSector stub : list) {
			Node node = stub.getRemote();
			// 建立SELECT命令，指定数据块
			CastSelect cmd = new CastSelect(select, stub.list());
			// 建立FROM会话
			FromSession session = new FromSession(phase, node);
			session.setCommand(cmd);
			session.setIndexSector(sector);
			// 保存FROM会话
			dispatcher.addSession(session);
		}

		return object;
	}

	/**
	 * 上次MAKEUP的数据，和MAKEUP之前的COMPUTE数据，分配给下次COMPUTE数据。
	 * 取出上次的GENERATE计算记录，与本次的EVALATE计算记录合并，计算出下次的分配资源
	 * 
	 * @param object
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws TaskException
	 */

	/**
	 * 取出上次的GENERATE记录，与当前EVALATE记录合并，计算出下次的分配资源
	 * @param object 当前TO阶段资源
	 * @param areas FLUX数据区
	 * @return 分配资源后的TO阶段对象
	 * @throws TaskException
	 */
	private ToObject dispatchCombin(ToObject object, FluxArea[] areas) throws TaskException {		
		// 找到上次FLUX AREA数据
		ToObject previous = object.previous();
		Phase prePhase = previous.getPhase();
		int preIndex = previous.getIterateIndex();
		FluxArea[] preAreas = findFluxArea(prePhase, preIndex);

		// 上次元数据(GENERATE)和本次元数据合并
		CyberMatrix matrix = new CyberMatrix();
		matrix.add(preAreas);
		matrix.add(areas);

		Phase phase = object.getPhase();
		ToInputter inputter = object.getInputter();
		// 比较后续TO阶段的节点数目，选择一个合理的值
		int sites = inputter.getSites();
		NodeSet set = getToSeeker().findToSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new BalanceTaskException("cannot find sites by '%s'", phase);
		}
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}

		// 根据实际节点数目重新分配
		CyberSphere[] spheres = matrix.balance(sites);

		// TO分派器
		ToDispatcher dispatcher = object.getDispatcher();

		// 保存参数
		if (spheres.length == 0) {
			byte[] b = createDefaultValue();
			dispatcher.setDefaultReturnValue(b);
			return object;
		}

		// 从TO分派器中取出SELECT命令，它在初始化时定义（在SubSelectInitTask中设置）
		boolean desc = false;
		boolean exists = dispatcher.hasCommand(SubSelectTaskKit.SELECT);
		if (exists) {
			Select select = (Select) dispatcher.findCommand(SubSelectTaskKit.SELECT);
			desc = isDESC(phase, select);
		}

		Logger.debug(getIssuer(), this, "dispatchCombin", "select has:%s, phase %s, order by %s, sites size %d, cyber size is %d",
				(exists ? "Exists" : "Not Exists"), phase, (desc ? "DESC" : "ASC"), sites, spheres.length);

		// 分区
		ColumnSector sector = dispatcher.getIndexSector();

		// 按照降序/升序分配会话
		if (desc) {
			for (int index = spheres.length - 1; index >= 0; index--) {
				Node node = set.next();
				ToSession session = new ToSession(phase, node);
				// 保存抓取数据的元信息(节点地址和文件磁盘信息)
				session.setSphere(spheres[index]);
				// 设置分片
				if (sector != null) {
					session.setIndexSector(sector);
				}

				// 每个会话保存全部参数
				session.addParameters(dispatcher.getParameters());
				// 保存TO会话
				dispatcher.addSession(session);
			}
		} else {
			for (int index = 0; index < spheres.length; index++) {
				Node node = set.next();
				ToSession session = new ToSession(phase, node);
				// 保存抓取数据的元信息(节点地址和文件磁盘信息)
				session.setSphere(spheres[index]);
				// 设置分片
				if (sector != null) {
					session.setIndexSector(sector);
				}
				// 每个会话保存全部参数
				session.addParameters(dispatcher.getParameters());
				// 保存TO会话
				dispatcher.addSession(session);
			}
		}

		return object;
	}

	/**
	 * 找到列
	 * @param area
	 * @return
	 */
	private Column findColumn(FluxArea area) {
		Logger.debug(getIssuer(), this, "findColumn", "count FluxField %d", area.size());

		FluxField field = area.list().get(0);

		// 根据名称，找到列
		TaskParameter param = field.findParameter("column");
		if (param == null) {
			return null;
		}
		if (param.getClass() != TaskClassable.class) {
			return null;
		}

		// 取得列
		Object value = ((TaskClassable) param).getValue();
		if (!Laxkit.isClassFrom(value, Column.class)) {
			return null;
		}
		return ((Column) value);
	}

	/**
	 * 生成新的索引
	 * @param dock
	 * @param areas
	 * @return
	 * @throws TaskException
	 */
	private ColumnIndex createColumnIndex(Dock dock, FluxArea[] areas) throws TaskException {
		// 找到列
		Column column = findColumn(areas[0]);
		if (column == null) {
			throw new TaskNotFoundException("cannot be find column!");
		}

		// 查找对应的列属性
		ColumnAttribute attribute = findAttribute(dock);
		// 生成NOT NULL检索索引
		ColumnIndex index = null;
		try {
			index = IndexGenerator.createNullIndex(attribute, false);
		} catch (IOException e) {
			throw new BalanceTaskException(e);
		}

		// 列副本
		Column next = column.duplicate();
		next.setId(attribute.getColumnId());
		next.setNull(false);
		index.setColumn(next);

		// 返回新的索引
		return index;
	}
	
	/**
	 * ">ALL"之前的处理
	 * @param object
	 * @param areas
	 * @return
	 * @throws TaskException
	 */
	private ToObject dispatchPreviousGreaterAll(ToObject object, FluxArea[] areas) throws TaskException {
		Phase phase = object.getPhase();
		// 比较后续TO阶段的节点数目，选择一个合理的值
		ToInputter inputter = object.getInputter();
		int sites = inputter.getSites();
		NodeSet set = getToSeeker().findToSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new BalanceTaskException("cannot find sites by '%s'", phase);
		}
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}

		// 解析元数据，按照有效节点数进行重新排列
		CyberMatrix matrix = new CyberMatrix(areas);
		// 按照节点数重新排列和输出
		CyberSphere[] spheres = matrix.balance(sites);

		// 如果有分区，采用标准分布计算处理
		if (spheres.length > 0) {
			return dispatchStandardEvaluate(object, areas);
		}
		
		// 如果没有分区，调用下一个链对象的参数
		ToDispatcher dispatcher = object.getDispatcher();
		ToObject sub = object.next();
		// 分区对象
		ColumnSector sector = sub.getDispatcher().getIndexSector();
		
		Select select = (Select) sub.findCommand(SubSelectTaskKit.SELECT);
		Space space = select.getSpace();
		short columnId = select.getWhere().getColumnId();
		Dock dock = new Dock(space, columnId);
		
		// 查找对应的列属性
		ColumnAttribute attribute = findAttribute(dock);
		// 生成NOT NULL检索索引
		ColumnIndex index = null;
		try {
			index = IndexGenerator.createNullIndex(attribute, false);
		} catch (IOException e) {
			throw new BalanceTaskException(e);
		}
		
		// 生成“NOT NULL”检查条件，重新检索
		Where where = new Where(CompareOperator.NOT_NULL, index);
		// 重新定义WHERE语句
		select.resetWhere(where);
		
		phase = sub.getPhase();
		// 生成数据块索引分区
		List<StubSector> list = getFromSeeker().createStubSector(getInvokerId(), space);

		// 生成FromSession，直接查询数据库
		for (StubSector stub : list) {
			Node node = stub.getRemote();
			// 含数据块编号的SELECT命令
			CastSelect cmd = new CastSelect(select, stub.list());

			// 每一个具体的FROM任务(定义：目标主机，SELECT，分片区)
			FromSession session = new FromSession(phase, node);
			session.setCommand(cmd);

			// 设置分区，如果有，是在SubSelectInitTask中设置
			session.setIndexSector(sector);

			// 保存它
			dispatcher.addSession(session);
		}
		
		// 跳过之后的“dispatchGreaterAll”阶段，直接进入SubSelectInitTask.createToEvaluateTaskCollect，调用标准方法收集数据
		// 本处1，表示跳过“dispatchGreaterAll”这个阶段
		object.setSkipObjects(1);

		return object;
	}

	/**
	 * 取出列值，生成FromSession，进行大小等于的比较
	 * @param object
	 * @param areas
	 * @return
	 * @throws TaskException
	 */
	private ToObject dispatchGreaterAll(ToObject object, FluxArea[] areas) throws TaskException {
		Select select = (Select) object.findCommand(SubSelectTaskKit.SELECT);

		Space space = select.getSpace();
		short columnId = select.getWhere().getColumnId();
		Dock dock = new Dock(space, columnId);

		// 建立索引
		ColumnIndex index = createColumnIndex(dock, areas);

		// 生成“>”检查条件，重新检索
		Where where = new Where(CompareOperator.GREATER, index);
		// 重新定义WHERE语句
		select.resetWhere(where);

		Phase phase = object.getPhase();

		Logger.debug(getIssuer(), this, "dispatchGreaterAll", "[%s], 显示统计：%d, 最大列是：%s，执行：%s",
				select.getPrimitive(), select.getListSheet().size(), index.getColumn().toString(), phase);

		// 生成数据块索引分区
		List<StubSector> list = getFromSeeker().createStubSector(getInvokerId(), space);

		// 根据分片的数量，建立多个TO阶段会话
		ToDispatcher dispatcher = object.getDispatcher();
		// 保存
		for (StubSector stub : list) {
			Node node = stub.getRemote();
			// 含数据块编号的SELECT命令
			CastSelect cmd = new CastSelect(select, stub.list());

			// 每一个具体的FROM任务(定义：目标主机，SELECT，分片区)
			FromSession session = new FromSession(phase, node);
			session.setCommand(cmd);

			// 设置分区，如果有，是在SubSelectInitTask中设置
			session.setIndexSector(dispatcher.getIndexSector());

			// 保存它
			dispatcher.addSession(session);
		}

		return object;
	}
	
	/**
	 * 标准计算模式
	 * @param object
	 * @param areas
	 * @return
	 * @throws TaskException
	 */
	private ToObject dispatchStandardEvaluate(ToObject object, FluxArea[] areas) throws TaskException {
		Phase phase = object.getPhase();
		// 比较后续TO阶段的节点数目，选择一个合理的值
		ToInputter inputter = object.getInputter();
		int sites = inputter.getSites();
		NodeSet set = getToSeeker().findToSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new BalanceTaskException("cannot find sites by '%s'", phase);
		}
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}

		// 解析元数据，按照有效节点数进行重新排列
		CyberMatrix matrix = new CyberMatrix(areas);
		// 按照节点数重新排列和输出
		CyberSphere[] spheres = matrix.balance(sites);

		// 根据分片的数量，建立多个TO阶段会话
		ToDispatcher dispatcher = object.getDispatcher();

		// 如果是空值，返回默认值
		if (spheres.length == 0) {
			Logger.error(getIssuer(), this, "dispatchStandardEvaluate", "NOT FOUND AREA!");
			byte[] b = createDefaultValue();
			dispatcher.setDefaultReturnValue(b);
			return object;
		}

		// 从TO分派器中取出SELECT命令，它在初始化时定义（在SubSelectInitTask中设置）
		boolean desc = false;
		boolean exists = dispatcher.hasCommand(SubSelectTaskKit.SELECT);
		if (exists) {
			Select select = (Select) dispatcher.findCommand(SubSelectTaskKit.SELECT);
			desc = isDESC(phase, select);
		} else {
			exists = dispatcher.hasCommand(SQLTaskKit.SELECT_OBJECT);
			if (exists) {
				Select select = (Select) dispatcher.findCommand(SQLTaskKit.SELECT_OBJECT);
				desc = isDESC(phase, select);
			}
		}

		Logger.debug(getIssuer(), this, "dispatchStandardEvaluate", "select has:%s, phase %s, order by %s, sites size %d, cyber size is %d",
				(exists ? "Exists" : "Not Exists"), phase, (desc ? "DESC" : "ASC"), sites, spheres.length);

		ColumnSector sector = dispatcher.getIndexSector() ;

		// 按照降序/升序组合会话
		if (desc) {
			for (int index = spheres.length - 1; index >= 0; index--) {
				Node node = set.next();
				ToSession session = new ToSession(phase, node);
				// 保存抓取数据的元信息(节点地址和文件磁盘信息)
				session.setSphere(spheres[index]);
				// 下阶段的分区
				if (sector != null) {
					session.setIndexSector(sector);
				}
				// 复制全部参数（其中有以自定义参数身份保存的SELECT命令）
				session.addParameters(dispatcher.getParameters());
				// 保存TO会话
				dispatcher.addSession(session);
			}
		} else {
			for (int index = 0; index < spheres.length; index++) {
				Node node = set.next();
				ToSession session = new ToSession(phase, node);
				// 保存抓取数据的元信息(节点地址和文件磁盘信息)
				session.setSphere(spheres[index]);
				// 下阶段的分区
				if (sector != null) {
					session.setIndexSector(sector);
				}
				// 复制全部参数（其中有以自定义参数身份保存的SELECT命令）
				session.addParameters(dispatcher.getParameters());
				// 保存TO会话
				dispatcher.addSession(session);
			}
		}

		// 返回处理结果
		return object;
	}

	/**
	 * 执行EVALUATE类型的TO阶段计算
	 * @param object - TO阶段对象
	 * @param areas - FLUX数据区
	 * @return - 分配资源后的TO阶段对象
	 * @throws TaskException
	 */
	private ToObject dispatchEvaluate(ToObject object, FluxArea[] areas) throws TaskException {
		// 如果有“>ALL:PREVIOUS”关键字，专门处理
		if(object.hasSign(">ALL:PREVIOUS")) {
			return dispatchPreviousGreaterAll(object, areas);
		}
		// 如果是“>ALL”关键字，专门处理
		else if (object.hasSign(">ALL")) { // SubSelectComparator.GREATER_ALL)) {
			return dispatchGreaterAll(object, areas);
		}

		// 返回结果
		return dispatchStandardEvaluate(object, areas);
	}

	/**
	 * 根据传入参数分配资源
	 * @param object 当前TO阶段对象
	 * @param areas FLUX分区
	 * @return 分配资源后的TO阶段对象
	 * @throws TaskException
	 */
	public ToObject dispatch(ToObject object, FluxArea[] areas) throws TaskException {
		// 保存元数据的分布站点地址（记录这些站点地址是在最后释放时去删除集群中的分布记录，防止产生垃圾数据）
		super.addFluxDocks(areas);

		// 1. 如果本次是GENERATE对象，先保存上次计算的元数据
		if (object.isGenerate()) {
			// 将GENERATE的分布记录保存下来
			super.addFluxAreas(object.getPhase(), object.getIterateIndex(), areas); 
			// 执行GENERATE操作
			return dispatchGenerate(object, areas);
		} else {
			// 判断在本次操作前，已经执行过TO阶段操作
			ToObject previous = object.previous();
			boolean have = (previous != null);
			if (have) {
				Phase phase = previous.getPhase();
				int iterateIndex = previous.getIterateIndex();
				// 判断前一个有GENERATE记录
				have = hasFluxArea(phase, iterateIndex);
			}
			// 如果有GENERATE记录，合并它；否则直接产生EVALUATE会话
			if (have) {
				// 合并两次操作的数据
				return dispatchCombin(object, areas);
			} else {
				return dispatchEvaluate(object, areas);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, byte[], int, int)
	 */
	@Override
	public ToObject admix(ToObject object, byte[] b, int off, int len) throws TaskException {
		Logger.debug(getIssuer(), this, "admix", "元数据下标: %d, 长度: %d", off, len);

		FluxArea[] areas = splitFluxArea(b, off, len);
		return dispatch(object, areas);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, java.io.File[])
	 */
	@Override
	public ToObject admix(ToObject object, File[] files) throws TaskException {
		return super.defaultAdmix(object, files);
	}
}