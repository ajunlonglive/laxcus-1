/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.fork;

import com.laxcus.command.contact.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.distribute.contact.session.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 初始化随机排序类。<br>
 * 这是一个DIFFUSE/CONVERGE分布计算的例子，用于展示接口的设计/开发流程<br><br>
 * 
 * 语法表达式: <br>
 * CONDUCT DEMO_SORT<br>
 * INIT  <br>
 * DISTANT  SITES:6; begin(int)=1; end(int)=999999999; total(int)=100; <br>
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
public class PrintForkTask extends ForkTask {

	/**
	 * 初始化随机数排序任务
	 */
	public PrintForkTask() {
		super();
	}

	/**
	 * 建立DISTANT阶段对象和分配会话
	 * @param contact
	 * @throws TaskException
	 */
	private void createDistantTask(Contact contact) throws TaskException {
		DistantObject object = contact.getDistantObject();
		DistantInputter inputter = object.getInputter();
		Phase phase = inputter.getPhase();

		// 检查命名主机数量
		NodeSet set = getDistantSeeker().findDistantSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new ForkTaskException("cannot be find site by '%s'", phase);
		}

		// 检查DISTANT阶段节点数目，以实际需求为准
		int sites = inputter.getSites();
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}

//		Logger.debug(getIssuer(), this, "createDistantTask", "distant sites is %d", sites);

//		int begin = findInteger(inputter, "begin");
//		int end = findInteger(inputter, "end");
//		int total = findInteger(inputter, "total");

//		int workSites = contact.getToObject().getInputter().getSites();
//		if (workSites < 1) {
//			throw new ForkTaskException("illegal to sites: %d", workSites);
//		}

//		IntegerZone zone = new IntegerZone(begin, end, 1);
//		IndexBalancer balancer = createBalancer(ColumnType.INTEGER);
//		balancer.add(zone);
//		IndexSector sector = balancer.balance(workSites);

//		// 每一个会话实例需要产生的数据量
//		int capacity = total / sites;
//		int left = total % sites ;

//		int[] sizes = new int[sites];
//		for (int index = 0; index < sizes.length; index++) {
//			if (index < left) {
//				sizes[index] = capacity + 1;
//			} else {
//				sizes[index] = capacity;
//			}
//		}

		// 生成DISTANT阶段分配器
		DistantDispatcher dispatcher = new DistantDispatcher(phase);
//		dispatcher.setIndexSector(sector);

		for (int index = 0; index < sites; index++) {
			Node node = set.next();

			DistantSession session = new DistantSession(phase, node);
//			session.setIndexSector(sector);
//			session.addParameter(new TaskInteger("size", sizes[index])); //这段会话产生的数据量

			// 保存DISTANT会话实例
			dispatcher.addSession(session);
		}

		object.setDispatcher(dispatcher);
	}

//	/**
//	 * 建立TO阶段实例
//	 * @param contact
//	 * @throws TaskException
//	 */
//	private void createToTask(Contact contact) throws TaskException {
//		ToObject object = contact.getToObject();
//		Phase phase = object.getPhase();
//
//		// 检查TO阶段主机地址是否存在
//		checkToSites(phase);
//
//		// 建立TO阶段分配器
//		ToDispatcher dispatcher = new ToDispatcher(phase);
//		object.setDispatcher(dispatcher);
//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.fork.ForkTask#fork(com.laxcus.command.contact.Contact)
	 */
	@Override
	public Contact fork(Contact contact) throws TaskException {		
		// 建立DISTANT阶段实例,分配参数
		createDistantTask(contact);
//		createToTask(contact);

		return contact;
	}

}