/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;


import com.laxcus.access.schema.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.task.*;
import com.laxcus.task.contact.seeker.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * CONTACT.DISTANT分布资源管理器，部署在CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 5/9/2020
 * @since laxcus 1.0
 */
public class DistantSeekManager extends ContactSeekManager implements DistantSeeker {

	/** WORK站点元数据管理池 **/
	private WorkOnCallPool metaPool;

	/**
	 * 设置WORK站点元数据管理池
	 * @param e
	 */
	public void setMetaPool(WorkOnCallPool e) {
		Laxkit.nullabled(e);
		metaPool = e;
	}

	/** 定义句柄 **/
	private static DistantSeekManager selfHandle = new DistantSeekManager();

	/**
	 * 构造私有的CONTACT.DISTANT分布资源管理器
	 */
	private DistantSeekManager() {
		super();
	}

	/**
	 * 返回CONTACT.DISTANT分布资源管理器实例
	 * @return CONTACT.DISTANT分布资源管理器句柄
	 */
	public static DistantSeekManager getInstance() {
		// 安全检查
		TrackManager.check("DistantSeekManager.getInstance");
		// 返回句柄
		return DistantSeekManager.selfHandle;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeSeeker#isSystemLevel(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public boolean isSystemLevel(long invokerId, Phase phase) throws TaskException {
		// 用户签名有效
		available(invokerId);

		if (PhaseTag.isContact(phase.getFamily())) {
			return forkPool.isSystemLevel(phase.getSock());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeSeeker#isUserLevel(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public boolean isUserLevel(long invokerId, Phase phase) throws TaskException {
		// 用户签名有效
		available(invokerId);

		// 判断阶段类型
		if (PhaseTag.isContact(phase.getFamily())) {
			return forkPool.isUserLevel(phase.getSock());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.contact.seeker.DistantSeeker#findDistantSites(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public NodeSet findDistantSites(long invokerId, Phase phase) throws TaskException {
		// 是系统级，返回全部WORK站点。每个WORK站点默认保存全部“系统级”的CONTACT.DISTANT阶段分布组件
		if (isSystemLevel(invokerId, phase)) {
			return metaPool.list();
		}

		// 可能是用户级组件，进行FORK阶段安全检查，判断签名一致
		Phase init = new Phase(phase.getIssuer(), PhaseTag.FORK, phase.getSock());
		boolean success = forkPool.contains(init);
		if (!success) {
			throw new TaskSecurityException("refuse %s", phase);
		}
		return metaPool.findSites(phase);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.contact.seeker.DistantSeeker#findDistantTable(long, com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findDistantTable(long invokerId, Space space)
			throws TaskException {
		// 判断签名和表有效
		available(invokerId, space);

		// 在本地找
		Table table = staffPool.findLocalTable(space);
		// 去管理节点找
		if (table == null) {
			table = findHubTable(invokerId, space);
		}
		if (table == null) {
			throw new TaskException("cannot be find '%s'", space);
		}
		return table;
	}

}