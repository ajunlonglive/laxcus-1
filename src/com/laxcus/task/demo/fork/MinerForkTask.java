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
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 初始化查找挖矿
 * 
 * @author scott.liang
 * @version 1.0 12/27/2020
 * @since laxcus 1.0
 */
public class MinerForkTask extends ForkTask {

	/**
	 * 构造查找挖矿实例
	 */
	public MinerForkTask() {
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
			throw new TaskNotFoundException("cannot be find distant site by '%s'", phase);
		}
		// 检查DISTANT阶段节点数目，以实际需求为准
		int sites = inputter.getSites();
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}

		boolean gpu = findBoolean(inputter, "GPU");
		int zeros = findInteger(inputter, "zeros"); // 前缀0值统计
		String prefix = findString(inputter, "prefix"); // 前缀字符
		long begin = findLong(inputter, "begin"); // 查找搜索范围
		long end = findLong(inputter, "end");
		// 判断检索范围有效
		if (begin > end) { 
			throw new InitTaskException("%d > %d", begin, end);
		}
		long size = (end - begin + 1);

		long unit = size / sites; // 单元空间
		
//		Logger.debug(getIssuer(), this, "createDistantTask", "%s - [%d - %d]", prefix, begin, end);

		// 生成DISTANT阶段分配器
		DistantDispatcher dispatcher = new DistantDispatcher(phase);

		long seek = begin;
		for (int index = 0; index < sites; index++) {
			Node node = set.next();

			DistantSession session = new DistantSession(phase, node);

			// 设置索引下标，也是它的模值
			session.addInteger("INDEX",  index + 1);
			// 设置前缀字符
			session.addString("PREFIX", prefix);
			// 设置SHA256散列码前面0的数目
			session.addInteger("ZEROS", zeros);
			// 选择GPU处理或者否
			session.addBoolean("GPU", gpu);
			// 设置每个会话的处理范围
			session.addLong("BEGIN", seek);
			if (index < sites - 1) {
				session.addLong("END", seek + unit);
				seek += unit;
			} else {
				session.addLong("END", end);
			}
			
			// 保存DISTANT会话实例
			dispatcher.addSession(session);
		}

		object.setDispatcher(dispatcher);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.task.contact.fork.ForkTask#fork(com.laxcus.command.contact.Contact)
	 */
	@Override
	public Contact fork(Contact contact) throws TaskException {
		// 建立DISTANT阶段实例,分配参数
		createDistantTask(contact);
		// 返回结果
		return contact;
	}

}