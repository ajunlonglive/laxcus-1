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
public class SubSelectInitTask extends SQLInitTask {

	/** 工作节点数目 **/
	private int defaultWorkSites;

	/**
	 * 构造SubSelectInitTask实例
	 */
	public SubSelectInitTask() {
		super();
		defaultWorkSites = 0;
	}

	/**
	 * 建立标准FROM任务
	 * @param select
	 * @param conduct
	 * @return
	 * @throws TaskException
	 */
	private int createStandardFromTask(Select select, Conduct conduct) throws TaskException {
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
		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, defaultWorkSites);

		// 采用SELECT标准分片
		Phase phase = SelectTaskKit.FROM_STANDARD.duplicate();

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
		FromObject object = new FromObject(phase);
		object.setDispatcher(dispatcher);
		conduct.setFromObject(object);

		return 1;
	}

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
		createStandardFromTask(select, conduct);
		return 1;
	}
	
//	/**
//	 * 建立“”的EXISTS任务实例，它根据有没有返回结果，产生数据
//	 * @param phase
//	 * @param select
//	 * @param conduct
//	 * @throws TaskException
//	 */
//	private void createToEvaluateTaskByExists(Phase phase, Select select, Conduct conduct) throws TaskException {
//		checkToSites(phase);
//		
//		
//	}
//	
//	/**
//	 * 建立“产生数据”的EXISTS任务实例，它只返回一个标记，有或者没有数据
//	 * @param phase
//	 * @param select
//	 * @param conduct
//	 * @throws TaskException
//	 */
//	private void createToGenerateTaskByExists(Phase phase, Select preselect, Select select, Conduct conduct) throws TaskException {
//
//		checkToSites(phase);
//
//		Logger.debug(getIssuer(), this, "createToGenerateTaskByExists",
//				"check %s work sites: %d", phase, defaultWorkSites);
//
//		Space space = select.getSpace();
//		short columnId = select.getWhere().getColumnId();
//		Dock dock = new Dock(space, columnId);
//		
//		// 本阶段列空间和分片
//		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, defaultWorkSites);
//
//		// 输入接口
//		ToInputter inputter = new ToInputter(phase);
//		inputter.setSites(defaultWorkSites); // WORK节点
//		// 输出接口和分片
//		ToDispatcher dispatcher = new ToDispatcher(phase);
//		dispatcher.setIndexSector(sector);		
//		// 保存"NOT IN"的SELECT句柄
//		dispatcher.addCommand(SQLTaskKit.SELECT_OBJECT, select);
//
//		// 定义TO阶段的GENERATE模式对象
//		ToObject object = new ToObject(ToMode.GENERATE, phase);
//		object.setInputter(inputter);
//		object.setDispatcher(dispatcher);
//
//		// 将当前对象绑定在最后
//		conduct.attachToObject(object);
//	}
	
	private void createToTaskByExists(Select preselect, Select select, Conduct conduct) throws TaskException {
		
	}

	/**
	 * 建立"产生数据"的TO阶段对象
	 * @param select
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToGenerateTaskByIn(Phase toPhase, Select select, Conduct conduct) throws TaskException {
		// 指向DATA节点的FROM会话
		Phase fromPhase = SelectTaskKit.FROM_STANDARD;
		// 检查FROM主机，如果就弹出异常
		checkFromSites(fromPhase);

		Logger.debug(getIssuer(), this, "createToGenerateTaskByIn", "check %s work sites: %d", toPhase, defaultWorkSites);

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
		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, defaultWorkSites);

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
	 * 产生IN的TO计算模式
	 * @param select
	 * @param subSelect
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToEvaluateTaskByIn(Phase to, Select select, boolean subSelect, Conduct conduct) throws TaskException {
		createToEvaluateTask(to, select, subSelect, conduct);
	}
	
	/**
	 * 设置IN的实例
	 * @param select
	 * @param subSelect
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToTaskByIn(Select select, boolean subSelect, Conduct conduct) throws TaskException {
		// 1. 以GENERATE模式+NOT NULL语句，产生FromSession，去DATA节点提取全部数据,返回分片元数据
		createToGenerateTaskByIn(SubSelectTaskKit.TO_IN, select, conduct);
		// 2. 以EVALUATE模式,去DATA节点提取数据,然后和上一计算结果进行比较,取出分片范围内不相等的记录
		createToEvaluateTaskByIn(SubSelectTaskKit.TO_IN, select, subSelect, conduct);
	}

	/**
	 * 分配"NOT IN"任务。这里分为两段：1.生成FROM会话去DATA抓到全部数据，2.把前后两段数据进行检查，选出匹配的结果
	 * @param select
	 * @param subSelect
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToTaskByNotIn(Select select, boolean subSelect, Conduct conduct) throws TaskException {
		// 1. 以GENERATE模式+NOT NULL语句，产生FromSession，去DATA节点提取全部数据,返回分片元数据
		createToGenerateTaskByNotIn(SubSelectTaskKit.TO_NOTIN, select, conduct);
		// 2. 以EVALUATE模式,去DATA节点提取数据,然后和上一计算结果进行比较,取出分片范围内不相等的记录
		createToEvaluateTaskByNotIn(SubSelectTaskKit.TO_NOTIN, select, subSelect, conduct);
	}

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
		
		// 检查TO阶段对象存在
		checkToSites(phase);

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
			
			// 给下个阶段分区！
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
	
	/**
	 * 找到最大值
	 * @param phase
	 * @param select
	 * @param conduct
	 */
	private void createToEvaluateTaskByArrgerateGreaterAll(Phase phase, Select preselect, Conduct conduct) throws TaskException {
		Logger.debug(getIssuer(), this, "createToEvaluateTaskByArrgerateGreaterAll", "check '%s'", phase);

		ToInputter inputter = new ToInputter(phase);
		inputter.setSites(1); // 注意，只要一个TO节点，分析全部列参数

		// 记录分派过程中需要的参数, ToSession在后续的BALANCE阶段，根据返回结果数据进行分配
		ToDispatcher dispatcher = new ToDispatcher(phase);
		dispatcher.addCommand(SubSelectTaskKit.SELECT, preselect);
		
//		dispatcher.addCommand(SubSelectTaskKit.PRESELECT, array.get(seek - 1)); // 前一个SELECT		
		
		// 分派，聚合到一个
		short columnId = preselect.getColumnIds()[0];
		Dock dock = new Dock(preselect.getSpace(), columnId);

		// 列分区，只调用一个节点
		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, 1);
		dispatcher.setIndexSector(sector);

		// 建立TO对象
		ToObject object = new ToObject(ToMode.EVALUATE, phase);
		object.setInputter(inputter);
		object.setDispatcher(dispatcher);
		
		// 设置可类化对象
		object.addClassable("dock", dock);
		
		// 标记值，用在SubSelectBalanceTask
		object.setSign(">ALL:PREVIOUS");

		// 绑定这个对象
		conduct.attachToObject(object);
	}
	
	/**
	 * 生成TO计算模式阶段对象，但是它在Balance生成FromSession，固定指定分区！
	 * @param select
	 * @param subSelect
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToEvaluateTaskByGreaterAll(Select select, Conduct conduct) throws TaskException {
		// 采用SELECT标准分片，必须指定分区！
		Phase from = SelectTaskKit.FROM_STANDARD;
		
		// 检查FROM主机，如果就弹出异常
		checkFromSites(from);
		
		Logger.debug(getIssuer(), this, "createToEvaluateTaskByGreaterAll", "check %s", from);
		
		Space space = select.getSpace();
		
		// 输入接口
		ToInputter inputter = new ToInputter(from);
		inputter.setSites(defaultWorkSites); // SQLTaskKit.DEFAULT_WORKSITES); 
		// 输出接口和分片
		ToDispatcher dispatcher = new ToDispatcher(from);
		// 保存 ">ALL" 的SELECT句柄
		dispatcher.addCommand(SubSelectTaskKit.SELECT, select);
		
		// 有子级，进行分片
//		short columnId = select.getColumnIds()[0];
		short columnId = select.getWhere().getColumnId();
		Dock dock = new Dock(space, columnId);
		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, defaultWorkSites);
		dispatcher.setIndexSector(sector);
		
		// 定义TO阶段的“计算数据”模式，它在“Balance阶段”根据返回的最大列值，生成“FromSession”对象。
		ToObject object = new ToObject(ToMode.EVALUATE, from);
		object.setInputter(inputter);
		object.setDispatcher(dispatcher);
		
		// 命令对象
		object.addCommand(SubSelectTaskKit.SELECT, select);
		
		// 设置标记，用在Balance的判断
		object.setSign(">ALL"); //SubSelectComparator.GREATER_ALL); // ">ALL");
		
		// 将当前对象绑定在最后
		conduct.attachToObject(object);
	}
	
	/**
	 * 收集上个阶段产生在DATA.FROM/WORK.TO保存的数据，收集解析后返回！
	 * @param select SELECT命令
	 * @param conduct 分布命令
	 * @throws TaskException
	 */
	private void createToEvaluateTaskCollect(Select select, Conduct conduct) throws TaskException {
		// TO计算阶段，收集数据和返回结果
		Phase phase = SubSelectTaskKit.TO_EVALUATE_COLLECT;

		// 检查TO阶段对象存在！
		checkToSites(phase);

		Logger.debug(getIssuer(), this, "createToEvaluateTaskCollect", "check %s", phase);

		// 输入接口
		ToInputter inputter = new ToInputter(phase);
		inputter.setSites(defaultWorkSites);  // 默认当前所有WORK节点，在SubSelectBalanceTask中，用ToInputter.getSites() 取出。
		// 输出接口和分片
		ToDispatcher dispatcher = new ToDispatcher(phase);
		// 保存 ">ALL" 的SELECT句柄
		dispatcher.addCommand(SubSelectTaskKit.SELECT, select);

		//		// 有子级，进行分片
		//		Space space = select.getSpace();
		//		short columnId = select.getColumnIds()[0];
		//		Dock dock = new Dock(space, columnId);
		//		ColumnSector sector = getFromSeeker().createIndexSector(getInvokerId(), dock, defaultWorkSites);
		//		dispatcher.setIndexSector(sector);

		// 定义TO阶段的“计算数据”模式，它在“Balance阶段”根据返回的最大列值，生成“FromSession”对象。
		ToObject object = new ToObject(ToMode.EVALUATE, phase);
		object.setInputter(inputter);
		object.setDispatcher(dispatcher);
		// 命令对象
		object.addCommand(SubSelectTaskKit.SELECT, select);

		// 将当前对象绑定在最后
		conduct.attachToObject(object);
	}
	
	/**
	 * 产生">ALL"操作分布任务
	 * @param preselect
	 * @param select
	 * @param subSelect
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToTaskGreaterAll(Select preselect, Select select, boolean subSelect, Conduct conduct) throws TaskException {
		Logger.debug(getIssuer(), this, "createToTaskGreaterAll", "这是 >ALL");
		
		// 1. 产生一个聚合上次数据集，找到最大值的。这是TO.EVALUATE模式
		createToEvaluateTaskByArrgerateGreaterAll(SubSelectTaskKit.TO_ARRGERATE_GREATERALL, preselect, conduct);
		
		// 2. 生成FromSession对象，对DATA节点拿到数据，必须指定分区
		createToEvaluateTaskByGreaterAll(select.duplicate(), conduct);
		
		// 3. 如果没有子级选项，聚合分区数据，输出数据结果！
		if (!subSelect) {
			createToEvaluateTaskCollect(select.duplicate(), conduct);
		}
	}

	private void createToTaskLessAll(Select select, boolean subSelect, Conduct conduct) throws TaskException {
		//1. 对它的上级提出要求，要求返回一个最小值。最小值的数据写在元信息的备注里面

		//2. 初始化参数
		createToEvaluateTask(SubSelectTaskKit.TO_LESSALL, select, subSelect, conduct);
	}

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
			Select preselect = array.get(seek - 1); // 前一个SELECT
			createToTaskByExists(preselect, select, conduct);
			return 2;
		}
		// "NOT EXISTS"操作
		else if (CompareOperator.isNotExists(operator)) {

		}
		// "IN"操作 , "=ANY"操作等价
		else if (CompareOperator.isIn(operator) || CompareOperator.isEqualAny(operator)) {
			createToTaskByIn(select, sub, conduct);
			ToDispatcher dispatcher = conduct.getLastToObject().getDispatcher();
			dispatcher.addCommand(SubSelectTaskKit.SELECT, select);
			dispatcher.addCommand(SubSelectTaskKit.PRESELECT, array.get(seek - 1));

			//			super.addSerialable(dispatcher, SubSelectTaskKit.SELECT, select);
			//			super.addSerialable(dispatcher, SubSelectTaskKit.PRESELECT, array.get(seek - 1));
			//			dispatcher.addParameter(new TaskInstance(SubSelectTaskKit.SELECT, select));
			//			dispatcher.addParameter(new TaskInstance(SubSelectTaskKit.PRESELECT, array.get(seek - 1)));
		}
		// "NOT IN"操作，"<>ALL"操作等价。如果有NOT IN操作，先建立GENERATE任务，再建立EVALUATE任务。其它情况直接建立EVALUATE任务
		else if (CompareOperator.isNotIn(operator) || CompareOperator.isNotEqualAll(operator)) {  // CompareOperator.isNotEqualAny(operator)) { 
			createToTaskByNotIn(select, sub, conduct);
			ToDispatcher dispatcher = conduct.getLastToObject().getDispatcher();
			dispatcher.addCommand(SubSelectTaskKit.SELECT, select);
			dispatcher.addCommand(SubSelectTaskKit.PRESELECT, array.get(seek - 1)); // 前一个SELECT

			//			super.addSerialable(dispatcher, SubSelectTaskKit.SELECT, select);
			//			super.addSerialable(dispatcher, SubSelectTaskKit.PRESELECT, array.get(seek - 1));
			//			createNotInToGenerateTask(select, /*preselect, recursion,*/ conduct);
		}
		// "=ALL"操作. 1. 单行与“”相同，2. 多行是空记录，3. 没有结果，父集取得全部
		else if(CompareOperator.isEqualAll(operator)) {
			
		}
//		// "<>ALL"操作，相当于“NOT IN”操作
//		else if(CompareOperator.isNotEqualAll(operator)) {
//			createToTaskByNotIn(select, sub, conduct);
//			ToDispatcher dispatcher = conduct.getLastToObject().getDispatcher();
//			dispatcher.addCommand(SubSelectTaskKit.SELECT, select);
//			dispatcher.addCommand(SubSelectTaskKit.PRESELECT, array.get(seek - 1)); // 前一个SELECT			
//		}
		// ">ALL"
		else if (CompareOperator.isGreaterAll(operator)) {
			Select preselect = array.get(seek - 1); // 前一个SELECT
			createToTaskGreaterAll(preselect, select, sub, conduct);
			ToDispatcher dispatcher = conduct.getLastToObject().getDispatcher();
			dispatcher.addCommand(SubSelectTaskKit.SELECT, select);
			dispatcher.addCommand(SubSelectTaskKit.PRESELECT, array.get(seek - 1)); // 前一个SELECT		
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

		//		// "=ANY"操作，相当于"IN"操作
		//		else if(CompareOperator.isEqualAny(operator)) {
		//			createToTaskByIn(select, sub, conduct);
		//			ToDispatcher dispatcher = conduct.getLastToObject().getDispatcher();
		//			dispatcher.addCommand(SubSelectTaskKit.SELECT, select);
		//			dispatcher.addCommand(SubSelectTaskKit.PRESELECT, array.get(seek - 1));
		//		} 
		
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

	/**
	 * 初始化嵌套检索各级配置参数
	 * @see com.laxcus.task.conduct.init.InitTask#init(com.laxcus.command.conduct.Conduct)
	 */
	@Override
	public Conduct init(Conduct conduct) throws TaskException {
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
			Logger.debug(getIssuer(), this, "init", "SQL语句 { %s }", cmd.getPrimitive());
		}

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