/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.user.*;
import com.laxcus.site.*;
import com.laxcus.task.conduct.to.*;
import com.laxcus.task.contact.distant.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;
import com.laxcus.work.pool.*;

/**
 * 检索用户注册元数据调用器
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public class WorkSeekRegisterMetadataInvoker extends WorkInvoker {

	/**
	 * 检索检索用户注册元数据调用器
	 * @param cmd 检索用户在线注册的元数据
	 */
	public WorkSeekRegisterMetadataInvoker(SeekRegisterMetadata cmd) {
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

			// 用户资源
			Refer refer = StaffOnWorkPool.getInstance().findRefer(siger);
			if (refer != null) {
				for (Space space : refer.getTables()) {
					// 本地的表名
					item.addLocalTable(space);
				}
			}

			// 找阶段命名
			List<Phase> phases = findLocalPhases(siger);
			for(Phase e : phases) {
				item.addLocalPhase(e);
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
	 * @param siger 用户签名
	 * @return 阶段命名列表
	 */
	private List<Phase> findLocalPhases(Siger siger) {
		// 保存阶段命名
		ArrayList<Phase> phases = new ArrayList<Phase>();
		phases.addAll(ToTaskPool.getInstance().findPhases(siger));
		phases.addAll(DistantTaskPool.getInstance().findPhases(siger));
		return phases;
	}

	//	/**
	//	 * 查远程注册组件
	//	 * @param siger
	//	 * @return
	//	 */
	//	private List<RemotePhaseItem> findRemotePhases(Siger siger) {
	//		ArrayList<RemotePhaseItem> array = new ArrayList<RemotePhaseItem>();
	//		array.addAll(findRemotePhases(WorkOnWorkPool.getInstance(), siger));
	//		array.addAll(findRemotePhases(BuildOnWorkPool.getInstance(), siger));
	//		array.addAll(findRemotePhases(WorkOnWorkPool.getInstance(), siger));
	//		return array;
	//	}
	//
	//	/**
	//	 * 查匹配
	//	 * @param pool
	//	 * @param siger
	//	 * @return
	//	 */
	//	private List<RemotePhaseItem> findRemotePhases(SlaveOnWorkPool pool, Siger siger) {
	//
	//		ArrayList<RemotePhaseItem> array = new ArrayList<RemotePhaseItem>();
	//
	//		List<Phase> phases = pool.findPhases(siger);
	//		for (Phase e : phases) {
	//			NodeSet set = WorkOnWorkPool.getInstance().findSites(e);
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