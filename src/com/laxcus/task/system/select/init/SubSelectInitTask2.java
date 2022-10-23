/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.init;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.*;
import com.laxcus.access.index.section.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.access.type.*;
import com.laxcus.access.util.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.system.select.util.*;
import com.laxcus.util.naming.*;

/**
 * <code>SQL SELECT</code>嵌套检索。<br><br>
 * 
 * 根据传入的SELECT实例，采用倒推的办法，取得最内层的SELECT实例，分配FROM阶段的检索目标和资源。<br>
 * TO阶段分配拆分后的SELECT实例，资源分配由WORK节点去调用ToTask实施。<br>
 * 
 * 剩余的检索目标和资源分配转换到WORK节点进行，即TO阶段。<br>
 * 
 * @author scott.liang
 * @version 1.2 3/23/2014
 * @since laxcus 1.0
 */
public class SubSelectInitTask2 extends SQLInitTask {
	
	/** 工作节点数目 **/
	private int defaultWorkSites;

	/**
	 * 构造SubSelectInitTask实例
	 */
	public SubSelectInitTask2() {
		super();
		defaultWorkSites = 0;
	}

	//	/**
	//	 * 找到链表的最后一个对象，给它设置分区
	//	 * @param dock
	//	 * @param sector
	//	 * @param conduct
	//	 * @throws InitTaskException
	//	 */
	//	private void setLastSector(Dock dock, IndexSector sector, Conduct conduct) throws InitTaskException {
	//		TaskOutputter dispatcher = null;
	//		// 先检查TO阶段对象，再检查FROM对象
	//		ToObject last = conduct.getLastTo();
	//		if (last != null) {
	//			dispatcher = last.getDispatcher();
	//		} else {
	//			dispatcher = conduct.getFrom().getDispatcher();
	//			for (FromSession session : ((FromDispatcher) dispatcher).list()) {
	//				session.setSQLSector(dock, sector);
	//			}
	//		}
	//		if (dispatcher == null) {
	//			throw new InitTaskException("cannot find last dispatcher");
	//		}
	//		dispatcher.setSQLSector(dock, sector);
	//	}

	//	/**
	//	 * 找到最后一个对象的分派器，然后更新里面的分区数据
	//	 * @param sector
	//	 * @param conduct
	//	 * @throws InitTaskException
	//	 */
	//	private void replaceLastSector(IndexSector sector, Conduct conduct) throws InitTaskException {
	//		IndexSector that = null;
	//		ToObject last = conduct.getTo().last();
	//		if (last != null) {
	//			ToDispatcher dispatcher = last.getDispatcher();
	//			if (dispatcher == null) {
	//				throw new InitTaskException("cannot find to dispatcher");
	//			}
	//			that = dispatcher.getSector();
	//		} else {
	//			FromDispatcher dispatcher = conduct.getFrom().getDispatcher();
	//			if (dispatcher == null) {
	//				throw new InitTaskException("cannot find from dispatcher");
	//			}
	//			that = dispatcher.getSector();
	//		}
	//
	//		if (that == null) {
	//			throw new InitTaskException("cannot find previous sector");
	//		}
	//		// that.replace(sector);
	//	}

	//	/**
	//	 * 根据比较符，建立对应的FROM阶段子命名。通过子命名找到对应的处理接口。根命名是一致的。
	//	 * @param compare - 比较符， SELECT * FROM schema.table WHERE column_name (=|<>|>|>=|<|<=|IN) (SELECT column_name FROM ...)
	//	 * @return
	//	 * @throws TaskException
	//	 */
	//	private Phase createFromPhase(byte compare) throws TaskException {
	//		switch (compare) {
	//		case Types.IN:
	//			return SubSelectTaskKit.SUBSELECT_FROM_IN;
	//		case Types.NOT_IN:
	//			return SubSelectTaskKit.SUBSELECT_FROM_NOTIN;
	//		case Types.EXISTS:
	//			break;
	//		case Types.NOT_EXISTS:
	//			break;
	//		case Types.EQUAL_ALL:
	//			break;
	//		case Types.EQUAL_ANY:
	//			break;
	//		case Types.NOTEQUAL_ALL:
	//			break;
	//		case Types.NOTEQUAL_ANY:
	//			break;
	//		case Types.GREATER_ALL:
	//			break;
	//		case Types.GREATER_ANY:
	//			break;
	//		case Types.LESS_ALL:
	//			break;
	//		case Types.LESS_ANY:
	//			break;
	//		case Types.GREATER_EQUAL_ALL:
	//			break;
	//		case Types.GREATER_EQUAL_ANY:
	//			break;
	//		case Types.LESS_EQUAL_ALL:
	//			break;
	//		case Types.LESS_EQUAL_ANY:
	//			break;
	//		}
	//		
	//		throw new TaskException("illegal compare operator:%d", Condition.translateCompare(compare));
	//	}

	//	private Phase createToGeneratePhase(byte compare) throws TaskException {
	//		switch (compare) {
	//		case Types.NOT_IN:
	//			return SubSelectTaskKit.SUBSELECT_TO_NOTIN;
	//		case Types.EXISTS:
	//			break;
	//		case Types.NOT_EXISTS:
	//			break;
	//		}
	//		return null;
	//	}

//	public Phase createToComputePhase(byte compare) throws TaskException {
//		switch (compare) {
//		case CompareOperator.IN:
//			return SubSelectTaskKit.TO_IN;
//		case CompareOperator.NOT_IN:
//			return SubSelectTaskKit.TO_NOTIN;
//		case CompareOperator.EXISTS:
//			break;
//		case CompareOperator.NOT_EXISTS:
//			break;
//		case CompareOperator.EQUAL_ALL:
//			break;
//		case CompareOperator.EQUAL_ANY:
//			break;
//		case CompareOperator.NOTEQUAL_ALL:
//			break;
//		case CompareOperator.NOTEQUAL_ANY:
//			break;
//		case CompareOperator.GREATER_ALL:
//			break;
//		case CompareOperator.GREATER_ANY:
//			break;
//		case CompareOperator.LESS_ALL:
//			break;
//		case CompareOperator.LESS_ANY:
//			break;
//		case CompareOperator.GREATER_EQUAL_ALL:
//			break;
//		case CompareOperator.GREATER_EQUAL_ANY:
//			break;
//		case CompareOperator.LESS_EQUAL_ALL:
//			break;
//		case CompareOperator.LESS_EQUAL_ANY:
//			break;
//		}
//
//		throw new TaskException("illegal compare operator: %d", compare); 
//		//		CompareOperator.translate(compare)); 
//		// Condition.translateCompare(compare));
//	}

	/**
	 * 初始化FROM阶段配置，并且保存到CONDUCT实例。返回使用的嵌套SELECT语句的数目(最大是2)。
	 * @param array
	 * @param seek
	 * @param conduct
	 * @return
	 * @throws TaskException
	 */
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

	//	/**
	//	 * @param select
	//	 * @param conduct
	//	 * @return
	//	 * @throws TaskException
	//	 */
	//	private int createFromTask(Select select, Conduct conduct) throws TaskException {	
	//		Space space = select.getSpace();
	//		// 根据表名找到表配置
	//		Table table = super.getFromFilter().findTable(space);
	//		if (table == null) {
	//			throw new InitTaskException("cannot find table by %s", space);
	//		}
	//
	////		// 根据表名查找索引分区
	////		IndexImage indexImage = super.getFromChooser().findIndexImage(space);
	////		if (indexImage == null) {
	////			throw new InitTaskException("cannot find index by %s", space);
	////		}
	////
	////		// 根据WHERE语句，检索匹配的数据块
	////		ChunkSet tokens = indexImage.find(select.getCondition());
	////		if (tokens.isEmpty()) {
	////			throw new InitTaskException("cannot find chunk");
	////		}
	//		
	//		// 根据SELECT语句找到对应的数据块集合
	//		ChunkSet tokens = super.getFromFilter().findChunkSet(select);
	//		if (tokens == null || tokens.isEmpty()) {
	//			throw new InitTaskException("cannot find chunk");
	//		}
	//
	//		// 根据数据块标识号，查找匹配的主机地址
	//		Map<Node, ChunkSet> froms = super.getFromFilter().findFromSiteMap(tokens);
	//		if (froms == null || froms.isEmpty()) {
	//			throw new InitTaskException("cannot find site by %s", space);
	//		}
	//		
	////		HashMap<SiteHost, ChunkSet> froms = new HashMap<SiteHost, ChunkSet>();
	////		for(long chunkid : tokens.list()) {
	////			SiteSet sites = super.getFromChooser().findFromSites(chunkid);
	////			for (SiteHost host : sites.list()) { 
	////				ChunkSet set = froms.get(host);
	////				if (set == null) {
	////					set = new ChunkSet();
	////					froms.put(host, set);
	////				}
	////				set.add(chunkid);
	////			}
	////		}
	//
	//		/* 根据WORK节点数量为后续的TO阶段分片
	//		 * 分片原则：
	//		 * 节点数量可多不可少。当实际WORK节点不足时，这些片段可以在BALANCE阶段合并；
	//		 * 反之WORK节点多而片段少时， 因为分片对应的数据已经固定存在，分布则不能分割。
	//		 * 默认设置1000个WORK节点，这个数量足够大。
	//		 */
	//		short columnId = select.getColumnIds()[0];
	//		Dock dock = new Dock(space, columnId);
	//		// 根据节点数量和列空间生成分区
	//		int sites = SQLTaskKit.DEFAULT_WORKSITES;
	//		IndexSector sector = super.createSector(sites, dock);
	//
	//		// 采用SELECT标准分片
	//		Phase phase = SelectTaskKit.FROM_STANDARD;
	//
	//		// 命名FROM输出，保存多个FROM阶段命名
	//		FromDispatcher dispatcher = new FromDispatcher(phase);
	//		dispatcher.setSQLSector(dock, sector);
	//
	//		for (Node host : froms.keySet()) {
	//			ChunkSet set = froms.get(host);
	//			// 克隆SELECT，设置它要查找的数据块
	//			Select clone = (Select) select.clone();
	//			long[] chunkids = set.toArray();
	//			clone.setChunkIds(chunkids);
	//
	//			// 每一个具体的FROM任务(定义：目标主机，SELECT，分片区)
	//			FromSession session = new FromSession(phase, host.getHost());
	//			session.setSelect(clone);
	//			session.setSQLSector(dock, sector);
	//
	//			// 保存它
	//			dispatcher.addSession(session);
	//		}
	//
	//		// 设置FROM任务
	//		FromObject from = new FromObject(phase);
	//		from.setDispatcher(dispatcher);		
	//		conduct.setFrom(from);
	//		
	//		return 1;
	//	}

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
		// 根据节点数量和列空间生成分区
//		int sites = SQLTaskKit.DEFAULT_WORKSITES;
//		int sites = 2;

		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, defaultWorkSites);

		// 采用SELECT标准分片
		Phase phase = SelectTaskKit.FROM_STANDARD;

		// 命名FROM输出，保存多个FROM阶段命名
		FromDispatcher dispatcher = new FromDispatcher(phase);
		dispatcher.setIndexSector(sector);

		for (StubSector stub : list) {
			Node node = stub.getRemote();

			// 含数据块编号的SELECT命令
			CastSelect cmd = new CastSelect(select, stub.list());

			// 每一个具体的FROM任务(定义：目标主机，SELECT，分片区)
			FromSession session = new FromSession(phase, node);
			session.setCommand(cmd);
			session.setIndexSector(sector);
			// 保存它
			dispatcher.addSession(session);
		}

		// 设置FROM任务
		FromObject from = new FromObject(phase);
		from.setDispatcher(dispatcher);
		conduct.setFromObject(from);

		return 1;
	}

	/**
	 * 设置IN的实例
	 * @param select
	 * @param subSelect
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToTaskByIn(Select select, boolean subSelect, Conduct conduct) throws TaskException {
		createToEvaluateTask(SubSelectTaskKit.TO_IN, select, subSelect, conduct);

		//		Phase phase = SubSelectTaskKit.TO_IN;
		//		// 检查WORK主机命名是否有效
		//		checkToSites(phase);
		//
		//		final int sites = SQLTaskKit.DEFAULT_WORKSITES; // 默认1000个WORK节点
		//		ToInputter inputter = new ToInputter(phase);
		//		inputter.setSites(sites);
		//
		//		// 记录分派过程中需要的参数, ToSession在后续的BALANCE阶段，根据返回结果数据进行分配
		//		ToDispatcher dispatcher = new ToDispatcher(phase);
		//		// 如果是子级，根据"选择列表"的列为后续阶段分片
		//		if (subSelect) {
		//			short columnId = select.getColumnIdentities()[0];
		//			Dock dock = new Dock(select.getSpace(), columnId);
		//			IndexSector sector = super.createSector(sites, dock);
		//			dispatcher.setSelectSector(dock, sector);
		//		}
		//
		//		// 定义TO对象
		//		ToObject object = new ToObject(ToObject.COMPUTE_TASK, phase);
		//		object.setInputter(inputter);
		//		object.setDispatcher(dispatcher);
		//
		//		// 绑定这个对象
		//		conduct.attachTo(object);
	}

	/**
	 * 分配"NOT IN"任务。这里分为两段：1.生成FROM会话去DATA抓到全部数据，2.把前后两段数据进行检查，选出匹配的结果
	 * @param select
	 * @param subSelect
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToTaskByNotIn(Select select, boolean subSelect, Conduct conduct) throws TaskException {
//		Logger.debug(getIssuer(), "SubSelectInitTask.createToTaskByNotIn, into...");

		// 1. 以GENERATE模式+NOT NULL语句，产生FromSession，去DATA节点提取全部数据,返回分片元数据
		createToGenerateTaskByNotIn(SubSelectTaskKit.TO_NOTIN, select, conduct);
		// 2. 以EVALUATE模式,去DATA节点提取数据,然后和上一计算结果进行比较,取出分片范围内不相等的记录
		createToEvaluateTaskByNotIn(SubSelectTaskKit.TO_NOTIN, select, subSelect, conduct);
	}

	//	/**
	//	 * 生成新的列索引
	//	 * @param space 表名
	//	 * @param columnId 属性编号
	//	 * @return 返回ColumnIndex
	//	 * @throws TaskException
	//	 */
	//	private ColumnIndex createColumnIndex(Space space, short columnId) throws TaskException {
	//		Table table = findTable(space);
	//		if (table == null) {
	//			throw new TaskNotFoundException("cannot be find table! %s", space);
	//		}
	//		ColumnAttribute attribute = table.find(columnId);
	//		if (attribute == null) {
	//			throw new TaskNotFoundException("cannot be find attribute! %d", columnId);
	//		}
	//		// 生成实例
	//		return IndexGenerator.createColumnIndex(attribute);
	//	}

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

	//	/**
	//	 * 建立"产生数据"的TO阶段对象
	//	 * @param select
	//	 * @param conduct
	//	 * @throws TaskException
	//	 */
	//	private void createToGenerateTaskByNotIn(Select select, Conduct conduct) throws TaskException {
	//		// 指向DATA节点的FROM会话
	//		Phase phase = SelectTaskKit.FROM_STANDARD;
	//		// 检查FROM主机，如果就弹出异常
	//		checkFromSites(phase);
	//		
	//		// 保留列编号，生成新的列索引替换旧的迭代值
	//		Where where = select.getWhere();
	//		short columnId = where.getColumnId();
	//		ColumnIndex columnIdex = createColumnIndex(select.getSpace(), columnId);
	//		Where clone = new Where(CompareOperator.ALL, columnIdex);
	//		select.setWhere(clone);
	//		select.setPrimitive("峨眉山月半轮秋，影入平羌江水流。无端隔水抛莲子，遥被人知半日羞！");
	//
	//		Logger.debug(getIssuer(), this, "createToGenerateTaskByNotIn", "支持JNI SQL语法：'%s'", clone.isSupportJNI());
	//		Logger.debug(getIssuer(), this, "createToGenerateTaskByNotIn", "%s", select.getPrimitive());
	//
	//		// 表名
	//		Space space = select.getSpace();
	//		Dock dock = new Dock(space, columnId);
	//
	//		// 查找对应的列属性
	//		ColumnAttribute attribute = super.findAttribute(dock);
	//		// 生成NOT NULL检索索引
	//		ColumnIndex index = null;
	//		try {
	//			index = IndexGenerator.createNullIndex(attribute, false);
	//		} catch (IOException e) {
	//			throw new InitTaskException(e);
	//		}
	//
	//		// 执行产生操作，以NOT NULL为检索条件，从DATA节点抓取数据后，在WORK节点进行分片
	//		Where condi = new Where(CompareOperator.NOT_NULL, index);
	//
	//		Select draw = new Select(space);
	//		draw.setWhere(condi);
	//		draw.setAutoAdjust(false);
	//		// 设置显示成员
	//		ListSheet sheet = select.getListSheet().duplicate();
	//		ColumnElement element = new ColumnElement(space, attribute.getTag());
	//		sheet.add(element);
	//		draw.setListSheet(sheet);
	//
	//		//		// 找到上一级的分区，与它的分片保持一致
	//		//		IndexSector sector = null;
	//		//		ToObject last = conduct.getTo().last();
	//		//		if (last != null) {
	//		//			ToDispatcher td = last.getDispatcher();
	//		//			sector = td.getSector().duplicate();
	//		//		} else {
	//		//			FromDispatcher fd = conduct.getFrom().getDispatcher();
	//		//			sector = fd.getSector().duplicate();
	//		//		}
	//		//		// 设置Packing参数
	//		//		if(attribute.isVariable()) {
	//		//			((VariableSector)sector).setPacking( ((VariableAttribute)attribute ).getPacking());
	//		//		}
	//		//		// 大小写敏感判断
	//		//		if (attribute.isWord()) {
	//		//			((WordSector) sector).setSentient(((WordAttribute) attribute).isSentient());
	//		//		}
	//
	//		// 本阶段列空间和分片
	//		//		IndexSector sector = super.createSector(SQLTaskKit.DEFAULT_WORKSITES, dock);
	//		//		Docket docket = createDocket(dock);
	//
	//		// 本阶段列空间和分片
	//		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, SQLTaskKit.DEFAULT_WORKSITES);
	//
	//		// 输入接口
	//		ToInputter inputter = new ToInputter(phase);
	//		inputter.setSites(SQLTaskKit.DEFAULT_WORKSITES); 
	//		// 输出接口和分片
	//		ToDispatcher dispatcher = new ToDispatcher(phase);
	//		dispatcher.setIndexSector(sector);		
	//		// 保存"NOT IN"的SELECT句柄
	//		dispatcher.addCommand(SQLTaskKit.SELECT_OBJECT, select);
	//
	//		//		setSelect(dispatcher, draw);
	//
	//		// 定义TO阶段的GENERATE模式对象
	//		ToObject object = new ToObject(ToMode.GENERATE, phase);
	//		object.setInputter(inputter);
	//		object.setDispatcher(dispatcher);
	//
	//		// 将当前对象绑定在最后
	//		conduct.attachToObject(object);
	//	}

	/**
	 * 建立"产生数据"的TO阶段对象
	 * @param select
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToGenerateTaskByNotIn(Phase toPhase, Select select, Conduct conduct) throws TaskException {
		// 指向DATA节点的FROM会话
		Phase fromPhase = SelectTaskKit.FROM_STANDARD;
		// 检查FROM主机，如果就弹出异常
		checkFromSites(fromPhase);
		
//		// 确认WORK节点
//		NodeSet set = getToSeeker().findToSites(getInvokerId(), toPhase);
//		int workSites = (set == null ? 0 : set.size());
//		if (workSites < 1) {
//			throw new InitTaskException("work sites missing! %s %d", toPhase, workSites);
//		}

		Logger.debug(getIssuer(), this, "createToGenerateTaskByNotIn", "check %s work sites: %d", toPhase, defaultWorkSites);
		
		// 表名
		short columnId = select.getWhere().getColumnId();
		Space space = select.getSpace();
		Dock dock = new Dock(space, columnId);

		// 查找对应的列属性
		ColumnAttribute attribute = super.findAttribute(dock);
		// 生成NOT NULL检索索引
		ColumnIndex index = null;
		try {
			index = IndexGenerator.createNullIndex(attribute, false);
		} catch (IOException e) {
			throw new InitTaskException(e);
		}

		// 执行产生操作，以NOT NULL为检索条件，从DATA节点抓取数据后，在WORK节点进行分片
		Where condi = new Where(CompareOperator.NOT_NULL, index);

		// 重新定义WHERE语句
		select.resetWhere(condi);

		// 本阶段列空间和分片
		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, defaultWorkSites); // SQLTaskKit.DEFAULT_WORKSITES);

		// 输入接口
		ToInputter inputter = new ToInputter(fromPhase);
		inputter.setSites(defaultWorkSites); // SQLTaskKit.DEFAULT_WORKSITES); 
		// 输出接口和分片
		ToDispatcher dispatcher = new ToDispatcher(fromPhase);
		dispatcher.setIndexSector(sector);		
		// 保存"NOT IN"的SELECT句柄
		dispatcher.addCommand(SQLTaskKit.SELECT_OBJECT, select);

		// 定义TO阶段的GENERATE模式对象
		ToObject object = new ToObject(ToMode.GENERATE, fromPhase);
		object.setInputter(inputter);
		object.setDispatcher(dispatcher);

		// 将当前对象绑定在最后
		conduct.attachToObject(object);
	}

	/**
	 * 产生NOT IN的TO计算模式
	 * @param select
	 * @param subSelect
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToEvaluateTaskByNotIn(Phase to, Select select, boolean subSelect, Conduct conduct) throws TaskException {
		createToEvaluateTask(to, select, subSelect, conduct);
	}

	/**
	 * 分配一个TO阶段的EVALUATE模式任务
	 * @param phase - 命名
	 * @param select - 检索语句
	 * @param subSelect - 是否子检索
	 * @param conduct - 参数存储
	 * @throws TaskException
	 */
	private void createToEvaluateTask(Phase phase, Select select, boolean subSelect, Conduct conduct) throws TaskException {
		Logger.debug(getIssuer(), this, "createToEvaluateTask", "check '%s'", phase);

		//		// 检查WORK主机命名是否有效
		//		checkToSites(phase);

//		// 获得WORK节点主机数目
//		NodeSet set = getToSeeker().findToSites(getInvokerId(), phase);
//		int sites = (set == null ? 0 : set.size());
//		if (sites < 1) {
//			throw new TaskNotFoundException("work sites missing! '%s'", phase);
//		}

		//		final int sites = SQLTaskKit.DEFAULT_WORKSITES; // 默认1000个WORK节点
		
		ToInputter inputter = new ToInputter(phase);
		inputter.setSites(defaultWorkSites);

		// 记录分派过程中需要的参数, ToSession在后续的BALANCE阶段，根据返回结果数据进行分配
		ToDispatcher dispatcher = new ToDispatcher(phase);
		// 如果是子级，根据"选择列表"的列为后续阶段分片
		if (subSelect) {
			short columnId = select.getColumnIds()[0];
			Dock dock = new Dock(select.getSpace(), columnId);
			//	IndexSector sector = super.createSector(sites, dock);

			//			Docket docket = createDocket(dock);
			ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, defaultWorkSites);
			dispatcher.setIndexSector(sector);
			//			dispatcher.setSQLSector(dock, sector);
		}

		// 建立TO对象
		ToObject object = new ToObject(ToMode.EVALUATE, phase);
		object.setInputter(inputter);
		object.setDispatcher(dispatcher);

		// 绑定这个对象
		conduct.attachToObject(object);
	}

	private void createToTaskGreaterAll(Select select, boolean subSelect, Conduct conduct) throws TaskException {
		//1. 对上次调用提出要求，要求返回一个最大值. 这个参数写在元信息的备注里面

		//2. 分配资源
//		createToEvaluateTask(SubSelectTaskKit.TO_GREATERALL, select, subSelect, conduct);
	}

	private void createToTaskLessAll(Select select, boolean subSelect, Conduct conduct) throws TaskException {
		//1. 对它的上级提出要求，要求返回一个最小值。最小值的数据写在元信息的备注里面

		//2. 初始化参数
		createToEvaluateTask(SubSelectTaskKit.TO_LESSALL, select, subSelect, conduct);
	}

	//	private void createFromTask(Select select, Select preselect, Conduct conduct) throws TaskException {
	//		Space space = select.getSpace();
	//		// 找到Table、IndexImage、CodePointImage，写入配置
	//		Table table = super.getFromChooser().findTable(space);
	//		if (table == null) {
	//			throw new InitTaskException("cannot find table by %s", space);
	//		}
	//
	//		// 查找索引分区
	//		IndexImage indexImage = super.getFromChooser().findIndexImage(space);
	//		if (indexImage == null) {
	//			throw new InitTaskException("cannot find index by %s", space);
	//		}
	//
	//		// 根据WHERE语句，检索匹配的数据块
	//		ChunkSet tokens = indexImage.find(select.getCondition());
	//		if (tokens.isEmpty()) {
	//			throw new InitTaskException("cannot find chunk");
	//		}
	//
	//		// 根据数据块标识号，查找匹配的主机地址
	//		HashMap<SiteHost, ChunkSet> froms = new HashMap<SiteHost, ChunkSet>();
	//		for(long chunkid : tokens.list()) {
	//			SiteSet sites = super.getFromChooser().findFromSites(chunkid);
	//			for (SiteHost host : sites.list()) { 
	//				ChunkSet set = froms.get(host);
	//				if (set == null) {
	//					set = new ChunkSet();
	//					froms.put(host, set);
	//				}
	//				set.add(chunkid);
	//			}
	//		}
	//
	//		/* 根据WORK节点数量为后续的TO阶段分片
	//		 * 分片原则：
	//		 * 节点数量可多不可少。当实际WORK节点不足时，这些片段可以在BALANCE阶段合并；
	//		 * 反之WORK节点多而片段少时， 因为分片对应的数据已经固定存在，分布则不能分割。
	//		 * 默认设置1000个WORK节点，这个数量足够大。
	//		 */
	//		short columnId = select.getColumnIdentities()[0];
	//		Dock dock = new Dock(space, columnId);
	//		// 根据节点数量和列空间生成分区
	//		int sites = SQLTaskKit.DEFAULT_WORKSITES;
	//		IndexSector sector = super.createSector(sites, dock); // space, columnId);
	//
	//		//		// 根据上级与当前SELECT显示列的比较关系，确定对应的阶段中的子命名，它们有对应的FROM编码
	//		//		byte operator = preselect.getCondition().getCompare();
	//		//		Phase phase = createFromPhase(operator);
	//
	//		// 采用SELECT标准分片
	//		Phase phase = SelectTaskKit.FROM_STANDARD;
	//
	//		// 命名FROM输出，保存多个FROM阶段命名
	//		FromDispatcher dispatcher = new FromDispatcher(phase);
	//		dispatcher.setSelectSector(dock, sector);
	//
	//		for (SiteHost host : froms.keySet()) {
	//			ChunkSet set = froms.get(host);
	//			// 克隆SELECT，设置它要查找的数据块
	//			Select clone = (Select) select.clone();
	//			long[] chunkids = set.toArray();
	//			clone.setChunkids(chunkids);
	//
	//			// 每一个具体的FROM任务(定义：目标主机，SELECT，分片区)
	//			FromSession session = new FromSession(phase, host);
	//			session.setSelect(clone);
	//			session.setSelectSector(dock, sector);
	//
	//			// 保存它
	//			dispatcher.addSession(session);
	//		}
	//
	//		// 设置FROM任务
	//		FromObject from = new FromObject(phase);
	//		from.setDispatcher(dispatcher);		
	//		conduct.setFrom(from);
	//	}

	//	/**
	//	 * @param select
	//	 * @param conduct
	//	 * @throws TaskException
	//	 */
	//	private void createSelectTask(Select select, Conduct conduct) throws TaskException {
	//		GroupAdjuster group = select.getGroup();
	//		OrderAdjuster order = select.getOrder();		 
	//		// 不处理
	//		if (group == null && order == null && !select.isDistinct()) {
	//			return;
	//		}
	//
	//		final int sites = SQLTaskKit.DEFAULT_WORKSITES;
	//		Space space = select.getSpace();
	//		
	//		// 按照优先顺序分配迭代对象，"GROUP BY"第一，"ORDER BY"第二，"DISTINCT"最后
	//		if (group != null) {
	//			Phase phase = SelectTaskKit.TO_GROUPBY;
	//			// 检查"GROUP BY"主机数目，如果不存在弹出异常
	//			super.checkToSites(phase);
	//			
	//			// 以GROUP BY为参照，为前面的阶段设置分区
	//			Dock dock = new Dock(space, group.getColumnIds()[0]);
	//			IndexSector sector = super.createSector(sites, dock);
	//			setLastSector(dock, sector, conduct);
	//			
	//			// 建立"GROUP BY"的TO阶段分配器
	//			ToInputter inputter = new ToInputter(phase);
	//			inputter.setSites(sites);
	//			
	//			ToDispatcher dispatcher = new ToDispatcher(phase);
	//			dispatcher.addValue(new TaskInstance(SQLTaskKit.SELECT_OBJECT, select));
	//
	//			ToObject object = new ToObject(ToObject.COMPUTE_TASK, phase);
	//			object.setInputter(inputter);
	//			object.setDispatcher(dispatcher);
	//			
	//			conduct.attachTo(object);
	//		}
	//
	//		if(select.isDistinct()) {
	//			Phase phase = SelectTaskKit.TO_DISTINCT;
	//			// 检查任务实例在节点上的分布
	//			super.checkToSites(phase);
	//						
	//			// 按照"GROUP BY"/"ORDER BY"/"显示列"的排列顺序，为DISTINCT前面的阶段设置分区
	//			short columnId = select.getColumnIds()[0];
	//			if (group != null) {
	//				columnId = group.getColumnIds()[0];
	//			} else if (order != null) {
	//				columnId = order.getColumnId();
	//			}
	//			Dock dock = new Dock(space, columnId);
	//			IndexSector sector = super.createSector(sites, dock);
	//			setLastSector(dock, sector, conduct);
	//
	//			ToInputter inputter = new ToInputter(phase);
	//			inputter.setSites(sites);
	//			
	//			ToDispatcher dispatcher = new ToDispatcher(phase);
	//			dispatcher.addValue(new TaskInstance(SQLTaskKit.SELECT_OBJECT, select));
	//
	//			ToObject object = new ToObject(ToObject.COMPUTE_TASK, phase);
	//			object.setInputter(inputter);
	//			object.setDispatcher(dispatcher);
	//
	//			conduct.attachTo(object);
	//		}
	//
	//		if (order != null) {
	//			Phase phase = SelectTaskKit.TO_ORDERBY;
	//			// 检查"ORDER BY"主机数目，如果没有弹出异常
	//			super.checkToSites(phase);
	//			
	//			// 以ORDER BY为参照，为它前面的阶段设置分区
	//			Dock dock = new Dock(space, order.getColumnId());
	//			IndexSector sector = super.createSector(sites, dock);
	//			setLastSector(dock, sector, conduct);
	//			
	//			// 建立"ORDER BY"的TO阶段分配器
	//			ToInputter inputter = new ToInputter(phase);
	//			inputter.setSites(sites);
	//			
	//			ToDispatcher dispatcher = new ToDispatcher(phase);
	//			dispatcher.addValue(new TaskInstance(SQLTaskKit.SELECT_OBJECT, select));
	//
	//			ToObject object = new ToObject(ToObject.COMPUTE_TASK, phase);
	//			object.setInputter(inputter);
	//			object.setDispatcher(dispatcher);
	//
	//			conduct.attachTo(object);
	//		}
	//		
	//	}



	//	/**
	//	 * 建立TO阶段的"计算"模式任务
	//	 * @param select
	//	 * @param preselect
	//	 * @param recursion
	//	 * @param conduct
	//	 * @throws TaskException
	//	 */
	//	private void createToComputeTask(Select select, Select preselect, int recursion, Conduct conduct) throws TaskException {
	//		// 根据上级与当前SELECT显示列的比较关系，确定对应的阶段中的子命名，它们有对应的FROM编码
	//		//	byte operator = preselect.getCondition().getCompare();
	//		byte operator = select.getCondition().getCompare();
	//		Phase phase = createToComputePhase(operator); // preselect.getCondition().getCompare());
	//
	//		// 检查WORK主机命名是否有效
	//		checkToSites(phase);
	//
	////		// 下一阶段的分区依据:列空间
	////		short columnId = 0;
	////		if (recursion > 1) { // 大于1是子级
	////			columnId = preselect.getColumnIdentities()[0];
	////		} else if (recursion == 1) { // 根状态，GROUP BY优化，其次ORDER BY
	////			if (preselect.getGroup() != null) {
	////				columnId = preselect.getGroup().getColumnIds()[0];
	////			} else if (preselect.getOrder() != null) {
	////				columnId = preselect.getOrder().getColumnId();
	////			} else if (preselect.isDistinct()) {
	////				columnId = preselect.getColumnIdentities()[0];
	////			}
	////		}
	////		if (columnId > 0) {
	////			Dock dock = new Dock(preselect.getSpace(), columnId);
	////			dispatcher.addParameter(new TaskInstance(SQLTaskKit.DOCK_OBJECT, dock));
	////		}
	//		
	//		short columnId = 0;
	//		// 准备下一阶段的列空间和分区
	//		if (recursion > 1) { // 大于1是子级，取它的显示列
	//			columnId = select.getColumnIds()[0];
	//		} else if (recursion == 1) { //根状态，以GROUP BY/ORDER BY/DISTINCT分片
	//			if (select.getGroup() != null) {
	//				columnId = select.getGroup().getColumnIds()[0];
	//			} else if (select.getOrder() != null) {
	//				columnId = select.getOrder().getColumnId();
	//			} else if (select.isDistinct()) {
	//				columnId = select.getColumnIds()[0];
	//			}
	//		}
	//		
	//		final int sites = SQLTaskKit.DEFAULT_WORKSITES; // 默认1000个WORK节点
	//		ToInputter inputter = new ToInputter(phase);
	//		inputter.setSites(sites);
	//
	//		// 记录分派过程中需要的参数, ToSession在后续的BALANCE阶段，根据返回结果数据进行分配
	//		ToDispatcher dispatcher = new ToDispatcher(phase);
	//		if (columnId > 0) {
	//			Dock dock = new Dock(select.getSpace(), columnId);
	//			IndexSector sector = super.createSector(sites, dock);
	//			dispatcher.setSQLSector(dock, sector);
	//		}
	//		// 本次SELECT句柄
	//		dispatcher.addValue(new TaskInstance(SQLTaskKit.SELECT_OBJECT, select));
	//		
	//		// 定义TO对象
	//		ToObject object = new ToObject(ToObject.COMPUTE_TASK, phase);
	//		object.setInputter(inputter);
	//		object.setDispatcher(dispatcher);
	//
	//		// 绑定这个对象
	//		conduct.attachTo(object);
	//	}

	/**
	 * 根据select初始化一个TO阶段参数，同时根据preselect分派它的后续TO阶段分片点。<br>
	 * 分配ToSession留在BALANCE阶段完成(运行时处理机制)，依据是这个阶段的分片点。<br>
	 * select和preselect必须是有效的。
	 * 
	 * @param select - 当前TO阶段的检索
	 * @param preselect - 对语句来说是上一级，到本次TO阶段来说是下一级检索
	 * @param recursion - 递归编号，1是根状态，大于1是子级
	 * @param conduct
	 * @throws TaskException
	 */
	private int createToTask(List<Select> array, int seek, Conduct conduct) throws TaskException {
		Select select = array.get(seek);
		int size = array.size();
		boolean sub = (seek + 1 < size);

//		if (sub) {
//			Select preselect = array.get(seek + 1);
//			byte operator = preselect.getWhere().getCompare();
//			// 如果是EXISTS/NOT EXISTS比较符，需要两组语句配置...
//			if (CompareOperator.isExists(operator)) {
//				// createToTaskByExists(select, preselect, conduct);
//				return 2;
//			} else if (CompareOperator.isNotExists(operator)) {
//				// createToTaskByNotExists(select, preselect, conduct);
//				return 2;
//			}
//		}

		byte operator = select.getWhere().getCompare();
		
		// "EXISTS"操作
		if (CompareOperator.isExists(operator)) {

		}
		// "NOT EXISTS"操作
		else if (CompareOperator.isNotExists(operator)) {

		}
		// "IN"操作
		else if (CompareOperator.isIn(operator)) { // || CompareOperator.isEqualAny(operator)) {
			createToTaskByIn(select, sub, conduct);
			ToDispatcher dispatcher = conduct.getLastToObject().getDispatcher();
			dispatcher.addCommand(SubSelectTaskKit.SELECT, select);
			dispatcher.addCommand(SubSelectTaskKit.PRESELECT, array.get(seek - 1));

			//			super.addSerialable(dispatcher, SubSelectTaskKit.SELECT, select);
			//			super.addSerialable(dispatcher, SubSelectTaskKit.PRESELECT, array.get(seek - 1));
			//			dispatcher.addParameter(new TaskInstance(SubSelectTaskKit.SELECT, select));
			//			dispatcher.addParameter(new TaskInstance(SubSelectTaskKit.PRESELECT, array.get(seek - 1)));
		}
		// "NOT IN"操作，如果有NOT IN操作，先建立GENERATE任务，再建立EVALUATE任务。其它情况直接建立EVALUATE任务
		else if (CompareOperator.isNotIn(operator)) { // || CompareOperator.isNotEqualAll(operator)) { 
			createToTaskByNotIn(select, sub, conduct);
			ToDispatcher dispatcher = conduct.getLastToObject().getDispatcher();
			dispatcher.addCommand(SubSelectTaskKit.SELECT, select);
			dispatcher.addCommand(SubSelectTaskKit.PRESELECT, array.get(seek - 1)); // 前一个SELECT

			//			super.addSerialable(dispatcher, SubSelectTaskKit.SELECT, select);
			//			super.addSerialable(dispatcher, SubSelectTaskKit.PRESELECT, array.get(seek - 1));
			//			createNotInToGenerateTask(select, /*preselect, recursion,*/ conduct);
		}
		// "=ALL"操作
		else if(CompareOperator.isEqualAll(operator)) {

		}
		// "<>ALL"操作，相当于“NOT IN”操作
		else if(CompareOperator.isNotEqualAll(operator)) {
			createToTaskByNotIn(select, sub, conduct);
			ToDispatcher dispatcher = conduct.getLastToObject().getDispatcher();
			dispatcher.addCommand(SubSelectTaskKit.SELECT, select);
			dispatcher.addCommand(SubSelectTaskKit.PRESELECT, array.get(seek - 1)); // 前一个SELECT			
		}
		// ">ALL"
		else if(CompareOperator.isGreaterAll(operator)) {
			createToTaskGreaterAll(select, sub, conduct);
		}
		// "<ALL"
		else if(CompareOperator.isLessAll(operator)) {
			createToTaskLessAll(select, sub, conduct);
		}
		// ">=ALL"
		else if(CompareOperator.isGreaterEuqlaAll(operator)) {
			
		}
		// "<=ALL"
		else if(CompareOperator.isLessEqualAll(operator)) {
			
		}
		
		// "=ANY"操作，相当于"IN"操作
		else if(CompareOperator.isEqualAny(operator)) {
			createToTaskByIn(select, sub, conduct);
			ToDispatcher dispatcher = conduct.getLastToObject().getDispatcher();
			dispatcher.addCommand(SubSelectTaskKit.SELECT, select);
			dispatcher.addCommand(SubSelectTaskKit.PRESELECT, array.get(seek - 1));
		} 
		// "<>ANY"操作
		else if (CompareOperator.isNotEqualAny(operator)) {

		} 
		// ">ANY"
		else if (CompareOperator.isGreaterAny(operator)) {

		} 
		// "<ANY"
		else if (CompareOperator.isLessAny(operator)) {

		} 
		// ">=ANY"
		else if (CompareOperator.isGreaterEuqlaAny(operator)) {

		}
		// "<=ANY"
		else if (CompareOperator.isLessEqualAny(operator)) {

		}
		
		// 其它，纯粹的比较符号：>,<,>=,<=,<>,= 新增, 2020-11-18
		else if (operator == CompareOperator.EQUAL) {

		} else if (operator == CompareOperator.NOT_EQUAL) {

		} else if (operator == CompareOperator.LESS) {

		} else if (operator == CompareOperator.LESS_EQUAL) {

		} else if (operator == CompareOperator.GREATER) {

		} else if (operator == CompareOperator.GREATER_EQUAL) {

		}

		//		createToComputeTask(select, preselect, recursion, conduct);

		// 返回1或者2
		return 1;
	}

	//	private void createToTask(Select select, Select preselect, int recursion, Conduct conduct) throws TaskException {
	//		byte operator = preselect.getCondition().getCompare();
	//
	//		// 如果有NOT IN操作，先建立GENERATE任务，再建立EVALUATE任务。其它情况直接建立EVALUATE任务
	//		if (operator == CompareType.NOT_IN) { // || operator == CompareType.NOT_EXISTS) {
	//			createNotInToGenerateTask(select, /*preselect, recursion,*/ conduct);
	//		}
	//		createToComputeTask(select, preselect, recursion, conduct);
	//	}


	/**
	 * 递归检查，采用倒推方式，直到找到最后的SELECT语句，分配给FROM阶段。<br>
	 * 然后逐一返回，分配给TO阶段。TO任务形成迭代链接模式。
	 * @param select - 当前SELECT语句
	 * @param preselect - 上一级SELECT语句
	 * @param recursion - 递归编号，有效值从1开始，表示"根"，以后逐次增加。0外表无递归。
	 * @param conduct - 分布计算语句
	 * @throws TaskException
	 */
	@SuppressWarnings("unused")
	private void distribute(Select select, Select preselect, int recursion, Conduct conduct) throws TaskException {
		if (select.hasNested()) {
			Where condi = select.getWhere();
			WhereIndex index = condi.getIndex();
			// 如果处理嵌套查询， 继续递归
			Select sub = ((NestedIndex) index).getSelect();
			distribute(sub, select, recursion + 1, conduct);

			//			// 使用传入的SELECT，设置TO阶段任务
			//			if (preselect != null) {
			//				System.out.printf("\nTO/SUBTO\n%s\n%s\n", select.getPrimitive(), preselect.getPrimitive());
			////				createToTask(select, preselect, recursion, conduct);
			//			} else {
			//				// 回退到顶层，检索是否有"GROUP BY|ORDER BY"，处理完就返回
			//				System.out.printf("\nPRIMITIVE\n%s\n", select.getPrimitive());
			////				createGroupOrderTask(select, conduct);
			//			}

			//			System.out.printf("\nTO_%d\n%s\n", recursion, select.getPrimitive());
			//	createToTask(select, preselect, recursion, conduct);
		} else {
			//	System.out.printf("FROM\n%s\n%s\n", select.getPrimitive(), preselect.getPrimitive());

			//			System.out.printf("\nFROM\n%s\n", select.getPrimitive());

			// 在FROM阶段，递归编号一定大于0，否则是错误
			if (recursion == 0) {
				throw new InitTaskException("illgal sub select:%s", select.getPrimitive());
			}
			// 传入的SELECT已经是最后一个，设置FROM阶段对象
			//	createFromTask(select, preselect, conduct);

			// FROM阶段是提取数据和分割数据，TO阶段的计算任务实现计算工作
			//	createToTask(select, preselect, recursion, conduct);
		}
	}

	//	private void distribute(Select select, Select preselect, int recursion, Conduct conduct) throws TaskException {
	//		if (select.hasSubSelect()) {
	//			Condition condi = select.getCondition();
	//			WhereIndex index = condi.getIndex();
	//			// 如果处理嵌套查询， 继续递归
	//			Select sub = ((SubSelectIndex) index).getSelect();
	//			distribute(sub, select, recursion + 1, conduct);
	//			// 使用传入的SELECT，设置TO阶段任务
	//			if (preselect != null) {
	//				System.out.printf("\nTO/SUBTO\n%s\n%s\n", select.getPrimitive(), preselect.getPrimitive());
	////				createToTask(select, preselect, recursion, conduct);
	//			} else {
	//				// 回退到顶层，检索是否有"GROUP BY|ORDER BY"，处理完就返回
	//				System.out.printf("\nPRIMITIVE\n%s\n", select.getPrimitive());
	////				createGroupOrderTask(select, conduct);
	//			}
	//		} else {
	//			System.out.printf("FROM\n%s\n%s\n", select.getPrimitive(), preselect.getPrimitive());
	//			
	//			if (recursion == 0) {
	//				throw new InitTaskException("illgal sub select:%s", select.getPrimitive());
	//			}
	//			// 传入的SELECT已经是最后一个，设置FROM阶段对象
	////			createFromTask(select, preselect, conduct);
	//			// FROM阶段是提取数据和分割数据，TO阶段的计算任务实现计算工作
	////			createToTask(select, preselect, recursion, conduct);
	//		}
	//	}

	/**
	 * 指定平衡分配接口
	 * @param select
	 * @return
	 */
	private BalanceObject initBalance() {
		return new BalanceObject(SubSelectTaskKit.BALANCE);
	}

	/**
	 * 用递归的方式，取出每一段SELECT，保存到数组里
	 * @param select
	 * @param array
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
		// SELECT迭代最后面的，保存在最前面
		array.add(select);
	}


	//	private void split(int seek, ArrayList<Select> array) {
	//		if (seek < 5781) { // 最大递归深度
	//			split(seek + 1, array);
	//		} else {
	//			System.out.printf("\n\nseek is %d\n", seek);
	//		}
	//	}

	/**
	 * 初始化嵌套检索各级配置参数
	 * @see com.laxcus.task.conduct.init.InitTask#init(com.laxcus.command.conduct.Conduct)
	 */
	@Override
	public Conduct init(Conduct conduct) throws TaskException {
		//		InitObject init = conduct.getInitObject();
		//		// 取出嵌套的SELECT实例，参数名称在com.laxcus.call.pool.DataPool.select中定义
		//		TaskParameter value = (TaskParameter) init.findValue("SELECT_OBJECT");
		//		if (value == null || !value.isCommand()) {
		//			throw new InitTaskException("null select");
		//		}
		//		Select select = (Select) ((TaskCommand) value).getValue();
		
		// 获得全部WORK节点
		defaultWorkSites = getToSeeker().getToSites(getInvokerId());
		if (defaultWorkSites < 1) {
			throw new TaskNotFoundException("not found work sites!");
		}

		// 取出SELECT命令
		InitObject init = conduct.getInitObject();
		Select select = (Select) init.findCommand("SELECT_OBJECT");

		// 递归分割SELECT，保存到数组
		ArrayList<Select> array = new ArrayList<Select>();
		recursion(select, array);

		// 打印结果
		for (int i = 0; i < array.size(); i++) {
			Select cmd = array.get(i);
			Logger.debug(getIssuer(), this, "init", "{ %s }", cmd.getPrimitive());
		}

		//	split(0, array);

		// 处理FROM阶段任务
		int seek = 0;
		int size = createFromTask(array, seek, conduct);
		seek += size;

		// 处理TO阶段任务
		while (seek < array.size()) {
			size = createToTask(array, seek, conduct);
			seek += size;
		}

		// 在最外层检查和处理GROUP BY/ORDER BY/DISTINCT
		if (select.isDistinct() || select.hasGroup() || select.hasOrder()) {
			SelectInitTask task = new SelectInitTask();
			task.setInvokerId(getInvokerId());
			task.setCommand(getCommand());
			task.setFromSeeker(getFromSeeker());
			task.setToSeeker(getToSeeker());
			task.createToTask(select, conduct);
		}

		// 平衡分布计算接口
		BalanceObject balance = initBalance();
		conduct.setBalanceObject(balance);

		// 返回分配完成后的conduct实例
		return conduct;
	}

}