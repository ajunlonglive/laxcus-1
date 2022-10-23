/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.init;

import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.*;
import com.laxcus.access.index.section.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.access.type.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
import com.laxcus.command.access.select.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.system.select.util.*;
import com.laxcus.util.naming.*;

/**
 * <code>SQL SELECT</code>连接检索初始化处理。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/2/2014
 * @since laxcus 1.0
 */
public class JoinSelectInitTask extends SQLInitTask {

	/**
	 * 
	 */
	public JoinSelectInitTask() {
		super();
	}

	/**
	 * 指定平衡分配接口
	 * @param select
	 * @return
	 */
	private BalanceObject initBalance() {
		return new BalanceObject(JoinTaskKit.JOIN_BALANCE);
	}

	private Where splitNotNull(ColumnAttribute attribute) throws InitTaskException {
		ColumnIndex index = null;
		switch (attribute.getType()) {
		case ColumnType.RAW:
			index = new LongIndex(0L, new com.laxcus.access.column.Raw());
			break;
		case ColumnType.CHAR:
			index = new LongIndex(0L, new com.laxcus.access.column.Char());
			break;
		case ColumnType.WCHAR:
			index = new LongIndex(0L, new com.laxcus.access.column.WChar());
			break;
		case ColumnType.HCHAR:
			index = new LongIndex(0L, new com.laxcus.access.column.HChar());
			break;
		case ColumnType.SHORT:
			index = new ShortIndex((short) 0, new com.laxcus.access.column.Short());
			break;
		case ColumnType.INTEGER:
			index = new IntegerIndex(0, new com.laxcus.access.column.Integer());
			break;			
		case ColumnType.LONG:
			index = new LongIndex(0L, new com.laxcus.access.column.Long());
			break;
		case ColumnType.FLOAT:
			index = new FloatIndex(0.0f, new com.laxcus.access.column.Float());
			break;
		case ColumnType.DOUBLE:
			index = new DoubleIndex(0.0f, new com.laxcus.access.column.Double());
			break;			
		case ColumnType.DATE:
			index = new IntegerIndex(0, new com.laxcus.access.column.Date());
			break;
		case ColumnType.TIME:
			index = new IntegerIndex(0, new com.laxcus.access.column.Time());
			break;			
		case ColumnType.TIMESTAMP:
			index = new LongIndex(0L, new com.laxcus.access.column.Timestamp());
			break;
		default:
			throw new InitTaskException("invalid column: %d", attribute.getType());
		}

		index.getColumn().setId(attribute.getColumnId());
		index.getColumn().setNull(false);

		return new Where(CompareOperator.NOT_NULL, index);
	}
	
	/**
	 * @param sector
	 * @param dock
	 * @param sheet
	 * @param dispatcher
	 * @throws TaskException
	 */
	private void doFromSession(ColumnSector sector, Dock dock,
			ListSheet sheet, FromDispatcher dispatcher) throws TaskException {
		// 找到表
		Space space = dock.getSpace();
		Table table = getFromSeeker().findFromTable(getInvokerId(), space);
		// 找到表中的参数
		ColumnAttribute attribute = table.find(dock.getColumnId());
		Where condi = this.splitNotNull(attribute);

		// 建立SELECT语句
		Select select = new Select(dock.getSpace());
		select.setListSheet(sheet);
		select.setAutoAdjust(false);
		select.setWhere(condi);

		List<StubSector> list = getFromSeeker().createStubSector(getInvokerId(), space); 
		for (StubSector stub : list) {
			Node node = stub.getRemote();

			CastSelect cmd = new CastSelect(select, stub.list());
			// // 克隆SELECT
			// Select clone = (Select) select.clone();
			// // 设置数据块编号
			// clone.setChunkIds(stub.list());

			// 建立会话
			FromSession session = new FromSession(dispatcher.getPhase(), node);
			session.setCommand(cmd);
			session.setIndexSector(sector);

			// 保存会话
			dispatcher.addSession(session);
		}
	}

//	/**
//	 * @param sector
//	 * @param dock
//	 * @param sheet
//	 * @param dispatcher
//	 * @throws InitTaskException
//	 */
//	private void doFromSession(IndexSector sector, Dock dock, ListSheet sheet,
//			FromDispatcher dispatcher) throws InitTaskException {
//
//		Space space = dock.getSpace();
//		// 找到Table、IndexImage、CodePointImage，写入配置
//		Table table = super.getFromFilter().findTable(space);
//		if (table == null) {
//			throw new InitTaskException("cannot find table by %s", space);
//		}
//
//		// 查找索引分区
//		IndexImage indexImage = super.getFromFilter().findIndexImage(space);
//		if (indexImage == null) {
//			throw new InitTaskException("cannot find index by %s", space);
//		}
//
//		ColumnAttribute attribute = table.find(dock.getColumnId());
//		Condition condi = this.splitNotNull(attribute);
//
//		Select select = new Select(dock.getSpace());
//		select.setListSheet(sheet);
//		select.setAutoAdjust(false);
//		select.setCondition(condi);
//
//		// 根据WHERE语句，检索匹配的chunkid
//		ChunkSet output = new ChunkSet();
//		int count = indexImage.find(condi, output);
//		if (count < 1) {
//			throw new InitTaskException("cannot find chunk by %s", space);
//		}
//
//		// 根据数据块标识号，查找匹配的主机地址
//		HashMap<SiteHost, ChunkSet> froms = new HashMap<SiteHost, ChunkSet>();
//		for(long chunkid : output.list()) {
//			NodeSet sites = super.getFromFilter().findFromSites(chunkid);
//			for (Node node : sites.show()) { 
//				ChunkSet set = froms.get(node);
//				if (set == null) {
//					set = new ChunkSet();
//					froms.put(node.getHost(), set);
//				}
//				set.add(chunkid);
//			}
//		}
//
//		for (SiteHost site : froms.keySet()) {
//			ChunkSet idset = froms.get(site);
//			// 克隆SELECT，查找数据块标识号
//			Select clone = (Select) select.clone();
//
//			long[] chunkids = idset.toArray();
//			clone.setChunkIds(chunkids);
//
//			FromSession session = new FromSession(dispatcher.getPhase(), site);
//			session.setSelect(clone);
//			session.setSector(sector);
//
//			// 保存它
//			dispatcher.addSession(session);
//		}
//	}

	/**
	 * 建立FROM对象
	 * @param join
	 * @return
	 * @throws TaskException
	 */
	private FromObject createFrom(Join join) throws TaskException {
		// 预定义一个极大的分片
		OnIndex index = join.getIndex();
		Dock left = index.getLeft();
		Dock right = index.getRight();

		Logger.debug("JoinSelectInitTask.createFrom, %s, %s", left, right);

		int sites = 10000; // 默认1000个节点
//		IndexSector sector = super.createSector(sites, left);
//		Docket docket = this.createDocket(left);
		
		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), left, sites);
		
//		IndexSector sector = super.getFromIndicator().createIndexSector(left, sites);

		Phase phase = JoinTaskKit.SELECT_FROM_INNERJOIN;
		// 建立FROM阶段分发器，保存多个FROM会话
		FromDispatcher dispatcher = new FromDispatcher(phase);

		// 取出左右的显示表
		ListSheet leftSheet = join.getListSheet().getListSheet(left.getSpace());
		ListSheet rightSheet = join.getListSheet().getListSheet(right.getSpace());

		Logger.debug("JoinSelectInitTask.createFrom,left sheet size is %d", leftSheet.size());
		//		for(ListElement element :leftSheet.list()) {
		//			System.out.printf("left columnid:%d\n", element.getColumnId());
		//		}

		Logger.debug("JoinSelectInitTask.createFrom,right sheet size is %d", rightSheet.size());
		//		for (ListElement element : rightSheet.list()) {
		//			System.out.printf("right columnid:%d\n", element.getColumnId());
		//		}

		// 建立左侧FROM会话
		doFromSession(sector, left, leftSheet, dispatcher);
		// 建立右侧FROM会话
		doFromSession(sector, right, rightSheet, dispatcher);

		Logger.debug("JoinSelectInitTask.createFrom, session size is %d", dispatcher.size());

		FromObject object = new FromObject(phase);
		object.setDispatcher(dispatcher);
		return object;
	}

	/**
	 * 建立TO阶段对象
	 * @param sector
	 * @return
	 * @throws TaskException
	 */
	private ToObject createTo(ColumnSector sector, Join join) throws TaskException {
		Phase phase = JoinTaskKit.SELECT_TO_INNERJOING;

		// 检查主机，如果无效弹出异常
		this.checkToSites(phase);

		// 输入
		ToInputter inputter = new ToInputter(phase);
		inputter.setSites(1000); // 默认1000个WORK节点

		// TO分配器
		ToDispatcher dispatcher = new ToDispatcher(phase);
		dispatcher.addCommand(JoinTaskKit.JOIN_OBJECT, join);

		ToObject object = new ToObject(ToMode.EVALUATE, phase);		
		object.setInputter(inputter);
		object.setDispatcher(dispatcher);

		// 生成迭代编号
		object.doIterateIndex();
		return object;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.init.InitTask#init(com.laxcus.command.conduct.Conduct)
	 */
	@Override
	public Conduct init(Conduct conduct) throws TaskException {
		InitObject init = conduct.getInitObject();
		// 取出JOIN实例，参数名称在com.laxcus.call.pool.DataPool.select中定义
		TaskParameter value = (TaskParameter) init.findParameter(JoinTaskKit.JOIN_OBJECT);
		if (value == null || !value.isCommand()) {
			throw new InitTaskException("null join");
		}
		Join join = (Join) ((TaskCommand) value).getValue();

		// 根据SELECT，生成FROM阶段分配器，FROM输入器忽略
		FromObject from = createFrom(join);
		conduct.setFromObject(from);

		// 取出其中一个分区
		ColumnSector sector = from.getDispatcher().getSession(0).getIndexSector();

		// 根据SELECT，生成TO对象链表
		ToObject to = createTo(sector, join);
		conduct.setToObject(to);

		// 平衡分布计算接口
		BalanceObject balance = initBalance();
		conduct.setBalanceObject(balance);

		return conduct;
	}

}