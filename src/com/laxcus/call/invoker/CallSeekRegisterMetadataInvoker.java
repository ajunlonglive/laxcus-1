/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.call.pool.*;
import com.laxcus.command.access.user.*;
import com.laxcus.site.*;
import com.laxcus.task.conduct.balance.*;
import com.laxcus.task.conduct.init.*;
import com.laxcus.task.contact.fork.*;
import com.laxcus.task.contact.merge.*;
import com.laxcus.task.establish.assign.*;
import com.laxcus.task.establish.issue.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 * 检索CALL节点注册用户元数据调用器
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public class CallSeekRegisterMetadataInvoker extends CallInvoker {

	/**
	 * 检索用户注册的元数据调用器
	 * @param cmd 检索用户在线注册的元数据
	 */
	public CallSeekRegisterMetadataInvoker(SeekRegisterMetadata cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekRegisterMetadata getCommand() {
		return (SeekRegisterMetadata) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {

		SeekRegisterMetadataProduct product = new SeekRegisterMetadataProduct();

		SeekRegisterMetadata cmd = getCommand();
		for(Siger siger : cmd.getUsers()) {
			Seat seat = new Seat(siger, getLocal());
			SeekRegisterMetadataItem item = new SeekRegisterMetadataItem(seat);

			// 找表
			Refer refer = StaffOnCallPool.getInstance().findRefer(siger);
			if (refer != null) {
				for (Space space : refer.getTables()) {
					// 本的表名
					item.addLocalTable(space);
					// DATA节点注册的表名
					NodeSet set = DataOnCallPool.getInstance().findTableSites(space);
					if (set != null) {
						for (Node node : set.show()) {
							RemoteTableItem sub = new RemoteTableItem(space, node);
							item.addRemoteTable(sub);
						}
					}
				}
			}
			// 找阶段命名
			List<Phase> phases = findLocalPhases(siger);
			for(Phase e : phases) {
				item.addLocalPhase(e);
			}
			
//			// 码位计算器
//			List<ScalerPart> parts = ScalerPool.getInstance(). findScalerParts(siger);
//			for(ScalerPart e : parts) {
//				item.addLocalScaler(e.getName());
//			}
			
			// 找注册的表名
			List<RemotePhaseItem> remotes = findRemotePhases( siger) ;
			for(RemotePhaseItem e : remotes) {
				item.addRemotePhase(e);
			}

			// 保存
			product.add(item);
		}

		// 发送给TOP/HOME
		boolean success = replyProduct(product);
		return useful(success);
	}

	/**
	 * 找本地的分布任务组件
	 * @param siger
	 * @return
	 */
	private List<Phase> findLocalPhases(Siger siger) {
		// 保存阶段命名
		ArrayList<Phase> phases = new ArrayList<Phase>();
		phases.addAll(IssueTaskPool.getInstance().findPhases(siger));
		phases.addAll(AssignTaskPool.getInstance().findPhases(siger));
		phases.addAll(InitTaskPool.getInstance().findPhases(siger));
		phases.addAll(BalanceTaskPool.getInstance().findPhases(siger));
		phases.addAll(ForkTaskPool.getInstance().findPhases(siger));
		phases.addAll(MergeTaskPool.getInstance().findPhases(siger));
		return phases;
	}

	/**
	 * 查远程注册组件
	 * @param issuer 用户签名
	 * @return 结果集合
	 */
	private List<RemotePhaseItem> findRemotePhases(Siger issuer) {
		ArrayList<RemotePhaseItem> array = new ArrayList<RemotePhaseItem>();
		array.addAll(findRemotePhases(DataOnCallPool.getInstance(), issuer));
		array.addAll(findRemotePhases(WorkOnCallPool.getInstance(), issuer));
		array.addAll(findRemotePhases(BuildOnCallPool.getInstance(), issuer));
		return array;
	}

	/**
	 * 查匹配
	 * @param pool
	 * @param siger
	 * @return
	 */
	private List<RemotePhaseItem> findRemotePhases(SlaveOnCallPool pool, Siger siger) {
		ArrayList<RemotePhaseItem> array = new ArrayList<RemotePhaseItem>();

		List<Phase> phases = pool.findPhases(siger);
		for (Phase e : phases) {
			NodeSet set = pool.findSites(e);
			if (set != null) {
				for (Node node : set.show()) {
					RemotePhaseItem item = new RemotePhaseItem(e, node);
					array.add(item);
				}
			}
		}

		return array;
	}

	//	private List<RemotePhaseItem> findDataPhases(Siger issuer) {
	//		ArrayList<RemotePhaseItem> array = new ArrayList<RemotePhaseItem>();
	//		
	//		List<Phase> phases = DataOnCallPool.getInstance().findPhases(issuer);
	//		for (Phase e : phases) {
	//			NodeSet set = DataOnCallPool.getInstance().findSites(e);
	//			if (set != null) {
	//				for (Node node : set.show()) {
	//					RemotePhaseItem item = new RemotePhaseItem(e, node);
	//					array.add(item);
	//				}
	//			}
	//		}
	//		
	//		return array;
	//	}
	//
	//	private List<RemotePhaseItem> findWorkPhases(Siger issuer) {
	//		ArrayList<RemotePhaseItem> array = new ArrayList<RemotePhaseItem>();
	//		
	//		List<Phase> phases = WorkOnCallPool.getInstance().findPhases(issuer);
	//		for (Phase e : phases) {
	//			NodeSet set = WorkOnCallPool.getInstance().findSites(e);
	//			if (set != null) {
	//				for (Node node : set.show()) {
	//					RemotePhaseItem item = new RemotePhaseItem(e, node);
	//					array.add(item);
	//				}
	//			}
	//		}
	//		
	//		return array;
	//	}
	//
	//	private List<RemotePhaseItem> findBuildPhases(Siger issuer) {
	//		ArrayList<RemotePhaseItem> array = new ArrayList<RemotePhaseItem>();
	//
	//		List<Phase> phases = BuildOnCallPool.getInstance().findPhases(issuer);
	//		for (Phase e : phases) {
	//			NodeSet set = BuildOnCallPool.getInstance().findSites(e);
	//			if (set != null) {
	//				for (Node node : set.show()) {
	//					RemotePhaseItem item = new RemotePhaseItem(e, node);
	//					array.add(item);
	//				}
	//			}
	//		}
	//
	//		return array;
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
