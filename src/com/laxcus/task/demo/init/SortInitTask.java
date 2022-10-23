/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.init;

import com.laxcus.access.index.balance.*;
import com.laxcus.access.index.section.*;
import com.laxcus.access.index.zone.*;
import com.laxcus.access.type.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.distribute.parameter.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 初始化随机排序类。<br>
 * 这是一个DIFFUSE/CONVERGE分布计算的例子，用于展示接口的设计/开发流程<br><br>
 * 
 * 语法表达式: <br>
 * CONDUCT DEMO_SORT<br>
 * INIT  <br>
 * FROM  SITES:6; begin(int)=1; end(int)=999999999; total(int)=100; <br>
 * BALANCE  <br>
 * TO  SITES:3; orderby(string|char)='desc'; <br>
 * PUT WRITETO:"/disks/random.bin"; <br><br>
 * 
 * "DEMO_SORT"是任务名称，全部调用过程与这个关键字绑定，忽略大小写。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 8/11/2011
 * @since laxcus 1.0
 */
public class SortInitTask extends InitTask {

	/**
	 * 初始化随机数排序任务
	 */
	public SortInitTask() {
		super();
	}

	/**
	 * 建立FROM阶段对象和分配会话
	 * @param conduct
	 * @throws TaskException
	 */
	private void createFromTask(Conduct conduct) throws TaskException {
		FromObject object = conduct.getFromObject();
		FromInputter inputter = object.getInputter(0);
		Phase phase = inputter.getPhase();

		// 检查命名主机数量
		NodeSet set = getFromSeeker().findFromSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new InitTaskException("cannot be find site by '%s'", phase);
		}

		// 检查FROM阶段节点数目，以实际需求为准
		int sites = inputter.getSites();
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}

		Logger.debug(getIssuer(), this, "createFromTask", "from sites is %d", sites);

		int begin = findInteger(inputter, "begin");
		int end = findInteger(inputter, "end");
		int total = findInteger(inputter, "total");

		int workSites = conduct.getToObject().getInputter().getSites();
		if (workSites < 1) {
			throw new InitTaskException("illegal to sites: %d", workSites);
		}

		IntegerZone zone = new IntegerZone(begin, end, 1);
		IndexBalancer balancer = createBalancer(ColumnType.INTEGER);
		balancer.add(zone);
		ColumnSector sector = balancer.balance(workSites);

		// 每一个会话实例需要产生的数据量
		int capacity = total / sites;
		int left = total % sites ;

		int[] sizes = new int[sites];
		for (int index = 0; index < sizes.length; index++) {
			if (index < left) {
				sizes[index] = capacity + 1;
			} else {
				sizes[index] = capacity;
			}
		}

		// 生成FROM阶段分配器
		FromDispatcher dispatcher = new FromDispatcher(phase);
		dispatcher.setIndexSector(sector);

		for (int index = 0; index < sites; index++) {
			Node node = set.next();

			FromSession session = new FromSession(phase, node);
			session.setIndexSector(sector);
			session.addParameter(new TaskInteger("size", sizes[index])); //这段会话产生的数据量

			// 保存FROM会话实例
			dispatcher.addSession(session);
		}

		object.setDispatcher(dispatcher);
	}

	/**
	 * 建立TO阶段实例
	 * @param conduct
	 * @throws TaskException
	 */
	private void createToTask(Conduct conduct) throws TaskException {
		ToObject object = conduct.getToObject();
		Phase phase = object.getPhase();

		// 检查TO阶段主机地址是否存在
		checkToSites(phase);

		// 建立TO阶段分配器
		ToDispatcher dispatcher = new ToDispatcher(phase);
		object.setDispatcher(dispatcher);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.init.InitTask#init(com.laxcus.command.conduct.Conduct)
	 */
	@Override
	public Conduct init(Conduct conduct) throws TaskException {		
		// 建立FROM/TO阶段实例,分配参数
		createFromTask(conduct);
		createToTask(conduct);

		return conduct;
	}

}