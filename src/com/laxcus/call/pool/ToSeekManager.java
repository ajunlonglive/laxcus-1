/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import com.laxcus.echo.invoke.*;
import com.laxcus.task.*;
import com.laxcus.task.conduct.seeker.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * CONDUCT.TO分布资源管理器。部署在CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 5/2/2018
 * @since laxcus 1.0
 */
public class ToSeekManager extends ConductSeekManager implements ToSeeker {

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
	private static ToSeekManager selfHandle = new ToSeekManager();

	/**
	 * 构造私有的CONDUCT.TO分布资源管理器
	 */
	private ToSeekManager() {
		super();
	}

	/**
	 * 返回CONDUCT.TO分布资源管理器实例
	 * @return CONDUCT.TO分布资源管理器句柄
	 */
	public static ToSeekManager getInstance() {
		// 安全检查
		TrackManager.check("ToSeekManager.getInstance");
		// 返回句柄
		return ToSeekManager.selfHandle;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.DistributeSeeker#isSystemLevel(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public boolean isSystemLevel(long invokerId, Phase phase) throws TaskException {
		// 用户签名有效
		available(invokerId);

		if (PhaseTag.isConduct(phase.getFamily())) {
			return initPool.isSystemLevel(phase.getSock());
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

		if (PhaseTag.isConduct(phase.getFamily())) {
			return initPool.isUserLevel(phase.getSock());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.seeker.ToSeeker#findToSites(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public NodeSet findToSites(long invokerId, Phase phase) throws TaskException {
		// 是系统级，返回全部WORK站点。每个WORK站点默认保存全部“系统级”的CONDUCT.TO阶段分布组件
		if (isSystemLevel(invokerId, phase)) {
			return metaPool.list();
		}

		// 可能是用户级组件，进行INIT阶段安全检查，判断签名一致
		Phase init = new Phase(phase.getIssuer(), PhaseTag.INIT, phase.getSock());
		boolean success = initPool.contains(init);
		if (!success) {
			throw new TaskSecurityException("refuse %s", phase);
		}
		return metaPool.findSites(phase);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.conduct.seeker.ToSeeker#getToSites(long)
	 */
	@Override
	public int getToSites(long invokerId) throws TaskException {
		// 找到用户签名
		Siger issuer = findIssuer(invokerId);
		if (issuer == null) {
			throw new TaskNotFoundException("cannot be find issuer! %d", invokerId);
		}
		// 判断允许
		boolean success = allow(issuer);
		if (!success) {
			throw new TaskSecurityException("security denied '%s'", issuer);
		}
		// 统计关联节点
		NodeSet set = metaPool.findSites(issuer);
		return (set == null ? 0 : set.size());
	}
}