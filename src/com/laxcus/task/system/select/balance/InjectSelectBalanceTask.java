/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.balance;

import java.io.*;

import com.laxcus.access.index.section.*;
import com.laxcus.command.access.*;
import com.laxcus.distribute.calculate.cyber.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.system.select.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 查询插入的BALANCE任务
 * 
 * @author scott.liang
 * @version 1.0 12/5/2020
 * @since laxcus 1.0
 */
public class InjectSelectBalanceTask extends BalanceTask {

	/**
	 * 构造默认的查询插入的BALANCE任务
	 */
	public InjectSelectBalanceTask() {
		super();
	}
	
	/**
	 * 返回结果的平衡分配
	 * @param current
	 * @param areas
	 * @return
	 * @throws TaskException
	 */
	public ToObject dispatch(ToObject current, FluxArea[] areas) throws TaskException {
		addFluxDocks(areas);
		
		Phase phase = current.getPhase();
		ToDispatcher dispatcher = current.getDispatcher();
		ToInputter inputter = current.getInputter();
		
		// 从TO分派器中取出INJECT SELECT命令，它在初始化时定义（在InjectSelectInitTask中设置）
		InjectSelect inject = (InjectSelect) dispatcher.findCommand(InjectSelectTaskKit.INJECT_SELECT_OBJECT);
		
		// 当前阶段是"GROUP BY"，如果有"ORDER BY"时，为下一阶段预定义分区区域
		ColumnSector sector = dispatcher.getIndexSector();

		// 比较后续TO阶段的节点数目，选择一个合理的值
		int sites = inputter.getSites(); 
		NodeSet set = getToSeeker().findToSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new BalanceTaskException("cannot find sites by '%s'", phase);
		}
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}
		
		Logger.info(getIssuer(), this, "dispatch", "phase:%s, site is %d", phase, sites);

		// 解析元数据，按照有效节点数进行重新排列
		CyberMatrix matrix = new CyberMatrix(areas);
		CyberSphere[] spheres = matrix.balance(sites);

		Logger.debug(getIssuer(), this, "dispatch", "area size:%d, sphere size: %d", areas.length, spheres.length);

		for (int index = 0; index < spheres.length; index++) { // 升序，从0开始
			Node node = set.next();
			ToSession session = new ToSession(phase, node);
			session.setSphere(spheres[index]);

			// Logger.debug(getIssuer(), this, "dispatch", "site: %s, 升序！",
			// node);
			// // 保存SELECT检索句柄
			// session.addCommand(SQLTaskKit.SELECT_OBJECT, select);

			// SELECT命令保存到会话
			session.setCommand(inject);
			// 待处理TO阶段任务的后续分区，为再下一TO阶段的分片做准备
			if (sector != null) {
				session.setIndexSector(sector);
			}
			// 保存会话句柄
			dispatcher.addSession(session);
		}
		
		return current;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, byte[], int, int)
	 */
	@Override
	public ToObject admix(ToObject current, byte[] b, int off, int len)
			throws TaskException {
		FluxArea[] areas = splitFluxArea(b, off, len);
		return dispatch(current, areas);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, java.io.File[])
	 */
	@Override
	public ToObject admix(ToObject current, File[] files) throws TaskException {
		return defaultAdmix(current, files);
	}

}
