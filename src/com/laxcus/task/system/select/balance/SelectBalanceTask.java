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
import com.laxcus.command.access.select.*;
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
 * 标准SELECT检索平衡接口，处理GROUP BY/ORDER BY/DISTINCT。<br>
 * 
 * @author scott.liang
 * @version 1.0 9/23/2012
 * @since laxcus 1.0
 */
public class SelectBalanceTask extends BalanceTask {

	/**
	 * 构造SELECT检索平衡实例
	 */
	public SelectBalanceTask() {
		super();
	}

	/**
	 * 判断命令是降序
	 * @param phase
	 * @return
	 */
	private boolean isDESC(Phase phase, Select select) {
		// 判断是TO阶段命令
		if (!PhaseTag.isTo(phase)) {
			return false;
		}
		// 有任务子命名
		Naming sub = phase.getSub();
		if (sub == null) {
			return false;
		}
		OrderByAdapter or = select.getOrder();
		if (or == null) {
			return false;
		}
		// 判断是ORDERBY采用降序排序
		return (SelectTaskKit.ORDERBY.equalsIgnoreCase(sub.toString()) && or.isDESC());
	}

	/**
	 * SELECT分布数据的平衡分配
	 * @param current 本次TO对象
	 * @param areas 数据分布区域
	 * @return 调整后的TO阶段对象
	 * @throws TaskException
	 */
	public ToObject dispatch(ToObject current, FluxArea[] areas) throws TaskException {
		addFluxDocks(areas);

		Phase phase = current.getPhase();
		ToDispatcher dispatcher = current.getDispatcher();
		ToInputter inputter = current.getInputter();

		//		// 取出SELECT检索语句，它在初始化时定义
		//		TaskParameter value = dispatcher.findValue(SQLTaskKit.SELECT_OBJECT);
		//		if (value == null || !value.isCommand()) {
		//			throw new BalanceTaskException("cannot find select");
		//		}
		//		Select select = (Select) ((TaskCommand) value).getValue();

		// 从TO分派器中取出SELECT命令，它在初始化时定义（在SelectInitTask中设置）
		Select select = (Select) dispatcher.findCommand(SQLTaskKit.SELECT_OBJECT);

		// 判断是降序排序
		boolean desc = isDESC(phase, select);

		// 当前阶段是"GROUP BY"，如果有"ORDER BY"时，为下一阶段预定义分区区域
		//		Dock dock = dispatcher.getIndexDock();
		//		IndexSector sector = dispatcher.getIndexSector();
		//		if (sector != null) sector.setDock(dock);

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

		// 区分升序和降度，建立与每个节点的会话
		if (desc) {
			for (int index = spheres.length - 1; index >= 0; index--) { // 降序，从最后开始
				Node node = set.next();
				ToSession session = new ToSession(phase, node);
				session.setSphere(spheres[index]);

				// Logger.debug(getIssuer(), this, "dispatch", "site: %s, 降序！",
				// node);
				// // 保存SELECT检索句柄
				// session.addCommand(SQLTaskKit.SELECT_OBJECT, select);

				// SELECT命令保存到会话
				session.setCommand(select);
				// 待处理TO阶段任务的后续分区，为再下一TO阶段的分片做准备
				if (sector != null) {
					session.setIndexSector(sector);
				}
				// 保存会话句柄
				dispatcher.addSession(session);
			}
		} else {
			for (int index = 0; index < spheres.length; index++) { // 升序，从0开始
				Node node = set.next();
				ToSession session = new ToSession(phase, node);
				session.setSphere(spheres[index]);

				// Logger.debug(getIssuer(), this, "dispatch", "site: %s, 升序！",
				// node);
				// // 保存SELECT检索句柄
				// session.addCommand(SQLTaskKit.SELECT_OBJECT, select);

				// SELECT命令保存到会话
				session.setCommand(select);
				// 待处理TO阶段任务的后续分区，为再下一TO阶段的分片做准备
				if (sector != null) {
					session.setIndexSector(sector);
				}
				// 保存会话句柄
				dispatcher.addSession(session);
			}
		}

		return current;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, byte[], int, int)
	 */
	@Override
	public ToObject admix(ToObject current, byte[] b, int off, int len) throws TaskException {
		FluxArea[] areas = splitFluxArea(b, off, len);
		return dispatch(current, areas);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, java.io.File[])
	 */
	@Override
	public ToObject admix(ToObject current, File[] files) throws TaskException {
		return defaultAdmix(current, files);
	}
}