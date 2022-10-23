/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.contact.merge;

import java.io.*;

import com.laxcus.distribute.calculate.cyber.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.distribute.contact.session.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 默认的MERGE阶段资源分配实例
 * 
 * @author scott.liang
 * @version 1.0 9/23/2009
 * @since laxcus 1.0
 */
public class DefaultMergeTask extends MergeTask {

	/**
	 * 构造默认的资源分配实例
	 */
	public DefaultMergeTask() {
		super();
	}
	
	/**
	 * 把元数据生成分布矩阵，解析参数，分配本次DISTANT阶段会话到DISTANT对象中
	 * 
	 * @param object 本次DISTANT阶段对象
	 * @param areas 分布计算区数组
	 * @return 返回分配后的DISTANT对象
	 * @throws TaskException
	 */
	public DistantObject next(DistantObject object, FluxArea[] areas)
			throws TaskException {
		CyberMatrix matrix = new CyberMatrix(areas);
		// 分派DISTANT阶段会话到DISTANT对象中
		return dispatch(object, matrix);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.merge.MergeTask#next(com.laxcus.distribute.contact.DistantObject, byte[], int, int)
	 */
	@Override
	public DistantObject next(DistantObject object, byte[] b, int off, int len)
			throws TaskException {
		FluxArea[] areas = super.splitFluxArea(b, off, len);
		// 分派DISTANT阶段会话到DISTANT对象中
		return next(object, areas);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.merge.MergeTask#next(com.laxcus.distribute.contact.DistantObject, java.io.File[])
	 */
	@Override
	public DistantObject next(DistantObject object, File[] files)
			throws TaskException {
		CyberMatrix matrix = new CyberMatrix();
		for (File file : files) {
			if (!(file.exists() && file.isFile())) {
				continue;
			}
			byte[] b = readFile(file);
			FluxArea[] areas = super.splitFluxArea(b, 0, b.length);
			matrix.add(areas);
		}
		// 分割数据
		return dispatch(object, matrix);
	}
	
	/**
	 * 从元数据分布矩阵中解析参数，分派DISTANT阶段会话到DISTANT对象中
	 * @param object DISTANT阶段对象
	 * @param matrix 分布数据矩阵
	 * @return 分配资源后的DISTANT对象
	 * @throws TaskException
	 */
	private DistantObject dispatch(DistantObject object, CyberMatrix matrix) throws TaskException {
		DistantInputter inputter = object.getInputter();
		Phase phase = inputter.getPhase();

		// 生成委派器
		DistantDispatcher dispatcher = object.getDispatcher();
		if (dispatcher == null) {
			dispatcher = new DistantDispatcher(phase);
			object.setDispatcher(dispatcher);
		}

		// 确定命名节点地址
		int sites = inputter.getSites();
		NodeSet set = getDistantSeeker().findDistantSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new MergeTaskException("cannot find %s", phase);
		}
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}

		// 根据实际节点数进行分配
		CyberSphere[] spheres = matrix.balance(sites);

		// 分配DistantSession
		for (int index = 0; index < sites; index++) {
			Node remote = set.next();
			DistantSession session = new DistantSession(phase, remote);
			session.setSphere(spheres[index]);
			dispatcher.addSession(session);
		}

		// 返回分配后的DISTANT对象
		return object;
	}

}