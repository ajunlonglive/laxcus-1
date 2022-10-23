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
import com.laxcus.task.establish.seeker.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * ESTABLISH.SIFT分布资源管理器。部署在CALL站点。
 * 
 * @author scott.liang
 * @version 1.0 5/2/2018
 * @since laxcus 1.0
 */
public class SiftSeekManager extends EstablishSeekManager implements SiftSeeker {

	/** BUILD站点元数据管理池 **/
	private BuildOnCallPool metaPool;

	/**
	 * 设置BUILD站点元数据管理池
	 * @param e
	 */
	public void setMetaPool(BuildOnCallPool e) {
		Laxkit.nullabled(e);
		metaPool = e;
	}

	/** 定义句柄 **/
	private static SiftSeekManager selfHandle = new SiftSeekManager();

	/**
	 * 构造私有的ESTABLISH.SIFT分布资源管理器
	 */
	private SiftSeekManager() {
		super();
	}

	/**
	 * 返回ESTABLISH.SIFT分布资源管理器实例
	 * @return ESTABLISH.SIFT分布资源管理器句柄
	 */
	public static SiftSeekManager getInstance() {
		// 安全检查
		TrackManager.check("SiftSeekManager.getInstance");
		// 返回句柄
		return SiftSeekManager.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.DistributeSeeker#isSystemLevel(com.laxcus.util.naming.Phase)
	 */
	@Override
	public boolean isSystemLevel(long invokerId, Phase phase) throws TaskException {
		available(invokerId);
		
		if (PhaseTag.isEstablish(phase.getFamily())) {
			return issuePool.isSystemLevel(phase.getSock());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.DistributeSeeker#isUserLevel(com.laxcus.util.naming.Phase)
	 */
	@Override
	public boolean isUserLevel(long invokerId, Phase phase) throws TaskException {
		available(invokerId);
		
		if (PhaseTag.isEstablish(phase.getFamily())) {
			return issuePool.isUserLevel(phase.getSock());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.task.establish.seeker.SiftSeeker#findSiftSites(long, com.laxcus.util.naming.Phase)
	 */
	@Override
	public NodeSet findSiftSites(long invokerId, Phase sift) throws TaskException {
		// 是系统级命名，返回全部BUILD地址。每个BUILD站点固定保存全部“系统级”SIFT阶段分布组件。
		if(isSystemLevel(invokerId,sift)) {
			return metaPool.list(); 
		}
		
		// 可能是用户级组件，进行ISSUE阶段安全检查，判断签名一致
		Phase issue = new Phase(sift.getIssuer(), PhaseTag.ISSUE, sift.getSock());
		boolean success = issuePool.contains(issue);
		if (!success) {
			throw new TaskSecurityException("refuse %s", sift);
		}
		
		return metaPool.findSites(sift);
	}

}
