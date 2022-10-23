/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.balance;

import java.io.*;

import com.laxcus.distribute.calculate.cyber.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 默认的BALANCE阶段资源分配实例
 * 
 * @author scott.liang
 * @version 1.0 9/23/2009
 * @since laxcus 1.0
 */
public class DefaultBalanceTask extends BalanceTask {

	/**
	 * 构造默认的资源分配实例
	 */
	public DefaultBalanceTask() {
		super();
	}
	
	/**
	 * 把元数据生成分布矩阵，解析参数，分配本次TO阶段会话到TO对象中
	 * 
	 * @param object 本次TO阶段对象
	 * @param areas 分布计算区数组
	 * @return 返回分配后的TO对象
	 * @throws TaskException
	 */
	public ToObject admix(ToObject object, FluxArea[] areas)
			throws TaskException {
		CyberMatrix matrix = new CyberMatrix(areas);
		// 分派TO阶段会话到TO对象中
		return this.dispatch(object, matrix);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, byte[], int, int)
	 */
	@Override
	public ToObject admix(ToObject object, byte[] b, int off, int len)
			throws TaskException {
		FluxArea[] areas = super.splitFluxArea(b, off, len);
		// 分派TO阶段会话到TO对象中
		return this.admix(object, areas);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.balance.BalanceTask#admix(com.laxcus.distribute.conduct.ToObject, java.io.File[])
	 */
	@Override
	public ToObject admix(ToObject object, File[] files)
			throws TaskException {
		CyberMatrix matrix = new CyberMatrix();
		for (File file : files) {
			if (!(file.exists() && file.isFile())) {
				continue;
			}
			byte[] b = this.readFile(file);
			FluxArea[] areas = super.splitFluxArea(b, 0, b.length);
			matrix.add(areas);
		}
		// 分割数据
		return dispatch(object, matrix);
	}
	
	/**
	 * 从元数据分布矩阵中解析参数，分派TO阶段会话到TO对象中
	 * @param object TO阶段对象
	 * @param matrix 分布数据矩阵
	 * @return 分配资源后的TO对象
	 * @throws TaskException
	 */
	private ToObject dispatch(ToObject object, CyberMatrix matrix) throws TaskException {
		ToInputter inputter = object.getInputter();
		Phase phase = inputter.getPhase();

		// 生成委派器
		ToDispatcher dispatcher = object.getDispatcher();
		if (dispatcher == null) {
			dispatcher = new ToDispatcher(phase);
			object.setDispatcher(dispatcher);
		}

		// 确定命名节点地址
		int sites = inputter.getSites();
		NodeSet set = getToSeeker().findToSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new BalanceTaskException("cannot find %s", phase);
		}
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}

		// 根据实际节点数进行分配
		CyberSphere[] spheres = matrix.balance(sites);

		// 分配ToSession
		for (int index = 0; index < sites; index++) {
			Node remote = set.next();
			ToSession session = new ToSession(phase, remote);
			session.setSphere(spheres[index]);
			dispatcher.addSession(session);
		}

		// 返回分配后的TO对象
		return object;
	}

}