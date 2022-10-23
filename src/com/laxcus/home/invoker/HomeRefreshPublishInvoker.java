/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.account.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;
import com.laxcus.site.*;

/**
 * 刷新发布的任务组件调用器
 * 
 * @author scott.liang
 * @version 1.0 3/13/2018
 * @since laxcus 1.0
 */
public class HomeRefreshPublishInvoker extends HomeInvoker {

	/**
	 * 构造刷新发布的任务组件调用器，指定命令
	 * @param cmd 刷新发布的任务组件
	 */
	public HomeRefreshPublishInvoker(RefreshPublish cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshPublish getCommand() {
		return (RefreshPublish) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果是监视器节点，忽略这个命令
		if (isMonitor()) {
			return useful(false);
		}
		
		RefreshPublish cmd = getCommand();
		Siger siger = cmd.getSiger();

//		// 子集站点
//		Node[] slaves = null;
//
//		// 查找关联站点
//		if (cmd.isTask()) {
//			slaves = findTaskSites(siger, cmd.getTaskFamily());
//		} else if (cmd.isScaler()) {
//			slaves = findScalerSites(siger);
//		} 
		
		Node[] slaves = findTaskSites(siger, cmd.getTaskFamily());

		// 以投递方式，通知HOME集群中的工作站点
		boolean success = (slaves != null && slaves.length > 0);
		if (success) {
			directTo(slaves, cmd);
		}

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 查找关联的工作站点
	 * @param siger 用户签名
	 * @param taskFamily 分布任务组件类型
	 * @return 站点地址数组
	 */
	private Node[] findTaskSites(Siger siger, int taskFamily) {
		NodeSet set = null;
		if (PhaseTag.onCallSite(taskFamily)) {
			if (siger != null) {
				set = CallOnHomePool.getInstance().findSites(siger);
			} else {
				set = new NodeSet(CallOnHomePool.getInstance().detail());
			}
		} else if (PhaseTag.onDataSite(taskFamily))  {
			if (siger != null) {
				set = DataOnHomePool.getInstance().findSites(siger);
			} else {
				set = new NodeSet(DataOnHomePool.getInstance().detail());
			}
		} else if (PhaseTag.onBuildSite(taskFamily)) {
			if (siger != null) {
				set = BuildOnHomePool.getInstance().findSites(siger);
			} else {
				set = new NodeSet(BuildOnHomePool.getInstance().detail());
			}
		} else if (PhaseTag.onWorkSite(taskFamily)) {
			if (siger != null) {
				set = WorkOnHomePool.getInstance().findSites(siger);
			} else {
				set = new NodeSet(WorkOnHomePool.getInstance().detail());
			}
		}

		Logger.debug(this, "findTaskSites", "check %s#%s count %d",
				siger, PhaseTag.translate(taskFamily), (set == null ? -1 : set.size()));

		return (set == null ? null : set.toArray());
	}
	
//	/**
//	 * 查找关联的工作站点
//	 * @param siger 用户签名
//	 * @param taskFamily 分布任务组件类型
//	 * @return 站点地址数组
//	 */
//	private Node[] findTaskSites(Siger siger, int taskFamily) {
//		NodeSet set = null;
//		if (PhaseTag.isInit(taskFamily) || PhaseTag.isBalance(taskFamily)
//				|| PhaseTag.isIssue(taskFamily) || PhaseTag.isAssign(taskFamily)
//				|| PhaseTag.isFork(taskFamily) || PhaseTag.isMerge(taskFamily)) {
//			if (siger != null) {
//				set = CallOnHomePool.getInstance().findSites(siger);
//			} else {
//				set = new NodeSet(CallOnHomePool.getInstance().detail());
//			}
//		} else if (PhaseTag.isFrom(taskFamily) || PhaseTag.isScan(taskFamily)
//				|| PhaseTag.isRise(taskFamily)) {
//			if (siger != null) {
//				set = DataOnHomePool.getInstance().findSites(siger);
//			} else {
//				set = new NodeSet(DataOnHomePool.getInstance().detail());
//			}
//		} else if (PhaseTag.isSift(taskFamily)) {
//			if (siger != null) {
//				set = BuildOnHomePool.getInstance().findSites(siger);
//			} else {
//				set = new NodeSet(BuildOnHomePool.getInstance().detail());
//			}
//		} else if (PhaseTag.isTo(taskFamily) || PhaseTag.isDistant(taskFamily)) {
//			if (siger != null) {
//				set = WorkOnHomePool.getInstance().findSites(siger);
//			} else {
//				set = new NodeSet(WorkOnHomePool.getInstance().detail());
//			}
//		}
//
//		Logger.debug(this, "findTaskSites", "check %s#%s count %d",
//				siger, PhaseTag.translate(taskFamily), (set == null ? -1 : set.size()));
//
//		return (set == null ? null : set.toArray());
//	}

//	/**
//	 * 查找码位计算器站点
//	 * @param siger 用户签名
//	 * @return 站点地址数组
//	 */
//	private Node[] findScalerSites(Siger siger) {
//		ArrayList<Node> array = new ArrayList<Node>();
//		NodeSet set = CallOnHomePool.getInstance().findSites(siger);
//		if (set != null) {
//			array.addAll(set.show());
//		}
//		set = DataOnHomePool.getInstance().findSites(siger);
//		if (set != null) {
//			array.addAll(set.show());
//		}
//		set = WorkOnHomePool.getInstance().findSites(siger);
//		if (set != null) {
//			array.addAll(set.show());
//		}
//		set = BuildOnHomePool.getInstance().findSites(siger);
//		if (set != null) {
//			array.addAll(set.show());
//		}
//
//		Node[] a = new Node[array.size()];
//		return array.toArray(a);
//	}

}