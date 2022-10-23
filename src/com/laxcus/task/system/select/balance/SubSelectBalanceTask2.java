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

import com.laxcus.access.index.section.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.command.access.*;
import com.laxcus.command.access.cast.*;
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
 * 处理SELECT嵌套检索，以及平衡分布数据资源。
 * 
 * @author scott.liang
 * @version 1.1 5/17/2013
 * @since laxcus 1.0
 */
public class SubSelectBalanceTask2 extends BalanceTask {

	/**
	 * 构造默认的SubSelectBalanceTask实例
	 */
	public SubSelectBalanceTask2() {
		super();
	}
	
	/**
	 * 分配GENERATE类型的TO阶段会话，这些会话在WORK站点执行，实际请求指向DATA站点
	 * @param current
	 * @param areas
	 * @return - 分派资源后的TO阶段对象
	 * @throws TaskException
	 */
	private ToObject dispatchGenerate(ToObject current, FluxArea[] areas) throws TaskException {
		// TO阶段分派器
		ToDispatcher dispatcher = current.getDispatcher();
		Select select = (Select) dispatcher.findCommand(SQLTaskKit.SELECT_OBJECT);
		//		Select select = getSelect(dispatcher);

		// 产生数据块分区
		Space space = select.getSpace();
		List<StubSector> list = getFromSeeker().createStubSector(getInvokerId(), space);

		// 索引扇区
		ColumnSector sector = dispatcher.getIndexSector();

		// 本次阶段命名
		Phase phase = current.getPhase();
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

		return current;
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
	 * @param current - 当前TO阶段资源
	 * @param areas - FLUX数据区
	 * @return - 分配资源后的TO阶段对象
	 * @throws TaskException
	 */
	private ToObject dispatchCombin(ToObject current, FluxArea[] areas) throws TaskException {		
		// 找到上次FLUX AREA数据
		ToObject previous = current.previous();
		Phase prePhase = previous.getPhase();
		int preIndex = previous.getIterateIndex();
		FluxArea[] preAreas = findFluxArea(prePhase, preIndex);

		// 上次元数据(GENERATE)和本次元数据合并
		CyberMatrix matrix = new CyberMatrix();
		matrix.add(preAreas);
		matrix.add(areas);

		Phase phase = current.getPhase();
		ToInputter inputter = current.getInputter();
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
		ToDispatcher dispatcher = current.getDispatcher();
		// 分区
		ColumnSector sector = dispatcher.getIndexSector();

		// 分配会话
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

		return current;
	}

	/**
	 * 执行EVALUATE类型的TO阶段计算
	 * @param current - TO阶段对象
	 * @param areas - FLUX数据区
	 * @return - 分配资源后的TO阶段对象
	 * @throws TaskException
	 */
	private ToObject dispatchEvaluate(ToObject current, FluxArea[] areas) throws TaskException {
		Phase phase = current.getPhase();
		// 比较后续TO阶段的节点数目，选择一个合理的值
		ToInputter inputter = current.getInputter();
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

		Logger.debug(getIssuer(), this, "dispatchEvaluate", "site is %d, cyber size is %d",
				sites, spheres.length);

		// 根据分片的数量，建立多个TO阶段会话
		ToDispatcher dispatcher = current.getDispatcher();
		ColumnSector sector = dispatcher.getIndexSector() ;
		for (int index = 0; index < spheres.length; index++) {
			Node node = set.next();
			ToSession session = new ToSession(phase, node);
			// 保存抓取数据的元信息(节点地址和文件磁盘信息)
			session.setSphere(spheres[index]);
			// 下阶段的分区
			if (sector != null) {
				session.setIndexSector(sector);
			}
			
//			session.setCommand(e)
			
			// 复制全部参数（其中有以自定义参数身份保存的SELECT命令）
			session.addParameters(dispatcher.getParameters());
			// 保存TO会话
			dispatcher.addSession(session);
		}

		// 返回处理结果
		return current;
	}

	/**
	 * 根据传入参数分配资源
	 * @param current - 当前TO阶段对象
	 * @param areas - FLUX分区
	 * @return - 分配资源后的TO阶段对象
	 * @throws TaskException
	 */
	public ToObject dispatch(ToObject current, FluxArea[] areas)
			throws TaskException {
		// 保存元数据的分布站点地址（记录这些站点地址是在最后释放时去删除集群中的分布记录，防止产生垃圾数据）
		super.addFluxDocks(areas);

		// 1. 如果本次是GENERATE对象，先保存上次计算的元数据
		if (current.isGenerate()) {
			// 将GENERATE的分布记录保存下来
			super.addFluxAreas(current.getPhase(), current.getIterateIndex(), areas); 
			// 执行GENERATE操作
			return dispatchGenerate(current, areas);
		} else {
			// 判断在本次操作前，已经执行过TO阶段操作
			ToObject previous = current.previous();
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
				return dispatchCombin(current, areas);
			} else {
				return dispatchEvaluate(current, areas);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, byte[], int, int)
	 */
	@Override
	public ToObject admix(ToObject current, byte[] b, int off, int len) throws TaskException {
		FluxArea[] areas = splitFluxArea(b, off, len);
		return this.dispatch(current, areas);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, java.io.File[])
	 */
	@Override
	public ToObject admix(ToObject current, File[] files) throws TaskException {
		return super.defaultAdmix(current, files);
	}
}