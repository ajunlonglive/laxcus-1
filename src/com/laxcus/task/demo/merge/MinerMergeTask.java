/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.demo.merge;

import java.io.*;

import com.laxcus.distribute.calculate.cyber.*;
import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.distribute.contact.*;
import com.laxcus.distribute.contact.session.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.merge.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 对多个节点产生的随机数进行平均分配。依据是根据它们的参数片段进行平衡分布。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 12/26/2020
 * @since laxcus 1.0
 */
public class MinerMergeTask extends MergeTask {

	/**
	 * 构造随机排序平衡处理任务
	 */
	public MinerMergeTask() {
		super();
	}

	/**
	 * 数据分片
	 * @param object
	 * @param areas
	 * @return
	 * @throws TaskException
	 */
	public DistantObject dispatch(DistantObject object, FluxArea[] areas) throws TaskException {
		// 保存单元
		addFluxDocks(areas);
		
		Phase phase = object.getPhase();
		DistantInputter inputter = object.getInputter();
		int sites = inputter.getSites();

		Logger.info(getIssuer(), this, "dispatch", "phase:%s, site is %d", phase, sites);

		// 确定实际可用的WORK节点数目
		NodeSet set = getDistantSeeker().findDistantSites(getInvokerId(), phase);
		if (set == null || set.isEmpty()) {
			throw new MergeTaskException("cannot find site by %s", phase);
		}
		if(sites < 1 || sites > set.size()) {
			sites = set.size();
		}

		// 解析分布数据流和调整到需要的数目
		CyberMatrix matrix = new CyberMatrix();
		for (int i = 0; i < areas.length; i++) {
			matrix.add(areas[i]);
		}
		// 根据实际节点数，调整数据分片到最合适的数量
		CyberSphere[] spheres = matrix.balance(sites);

		// 建立TO阶段分派器
		DistantDispatcher dispatcher = new DistantDispatcher(phase);
		for (int index = 0; index < spheres.length; index++) {
			// 按照调用顺序取得一个主机地址
			Node node = set.next();
			// 建立SESSION，保存输入的配置和当前会话的分片信息
			DistantSession session = new DistantSession(phase, node);
			session.addParameters(object.getInputter().getParameters());
			session.setSphere(spheres[index]);
			// 保存SESSION
			dispatcher.addSession(session);
		}
		// 设置分派器
		object.setDispatcher(dispatcher);

		return object;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.merge.MergeTask#next(com.laxcus.distribute.contact.DistantObject, byte[], int, int)
	 */
	@Override
	public DistantObject next(DistantObject current, byte[] b, int off, int len) throws TaskException {
		FluxArea[] areas = splitFluxArea(b, off, len);
		return dispatch(current, areas);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.merge.MergeTask#next(com.laxcus.distribute.contact.DistantObject, java.io.File[])
	 */
	@Override
	public DistantObject next(DistantObject current, File[] files)
			throws TaskException {
		return defaultAdmix(current, files);
	}

}