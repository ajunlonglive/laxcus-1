/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.pool;

import com.laxcus.access.schema.*;
import com.laxcus.front.pool.*;
import com.laxcus.law.cross.*;
import com.laxcus.platform.*;
import com.laxcus.platform.listener.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.util.naming.*;

/**
 * 桌面资源管理池 <br>
 * 
 * @author scott.liang
 * @version 1.0 5/19/2021
 * @since laxcus 1.0
 */
public class StaffOnDesktopPool extends StaffOnFrontPool {

	/** 资源管理池的静态句柄，全局唯一 **/
	private static StaffOnDesktopPool selfHandle = new StaffOnDesktopPool();

	/**
	 * 构造资源管理池
	 */
	private StaffOnDesktopPool() {
		super();
	}

	/**
	 * 返回资源管理池的静态句柄
	 * @return StaffOnDesktopPool句柄
	 */
	public static StaffOnDesktopPool getInstance() {
		return StaffOnDesktopPool.selfHandle;
	}
	
	/**
	 * 提取组件客户端
	 * @return DistributedComponentClient数组
	 */
	private WareClient[] getWareClients() {
		return PlatformKit.findListeners(WareClient.class);
	}
	
	/**
	 * 取数据库客户端
	 * @return DatabaseClient 数组
	 */
	public DatabaseClient[] getDatabaseClients(){
		return PlatformKit.findListeners(DatabaseClient.class);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#exhibit(com.laxcus.access.schema.Schema)
	 */
	@Override
	protected void exhibit(Schema schema) {
		DatabaseClient[] as = getDatabaseClients();
		int size = (as != null ? as.length : 0);
		for (int i = 0; i < size; i++) {
			DatabaseClient client = as[i];
			client.exhibit(schema);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.access.schema.Fame)
	 */
	@Override
	protected void erase(Fame fame) {
		DatabaseClient[] as = getDatabaseClients();
		int size = (as != null ? as.length : 0);
		for (int i = 0; i < size; i++) {
			DatabaseClient client = as[i];
			client.erase(fame);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#exhibit(com.laxcus.access.schema.Table)
	 */
	@Override
	protected void exhibit(Table table) {
		DatabaseClient[] as = getDatabaseClients();
		int size = (as != null ? as.length : 0);
		for(int i =0; i < size; i++) {
			DatabaseClient client = as[i];
			client.exhibit(table);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.access.schema.Space)
	 */
	@Override
	protected void erase(Space space) {
		DatabaseClient[] as = getDatabaseClients();
		int size = (as != null ? as.length : 0);
		for(int i =0; i < size; i++) {
			DatabaseClient client = as[i];
			client.erase(space);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#exhibit(com.laxcus.util.naming.Phase)
	 */
	@Override
	protected void exhibit(Phase phase) {
		// 阶段命名不在范围时...
		boolean success = (PhaseTag.isInit(phase) || PhaseTag.isIssue(phase) || PhaseTag.isFork(phase));
		if (success) {
			// 显示新的组件
			WareClient[] as = getWareClients();
			int size = (as != null ? as.length : 0);
			for (int i = 0; i < size; i++) {
				WareClient client = as[i];
				client.exhibit(phase);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.util.naming.Phase)
	 */
	@Override
	protected void erase(Phase phase) {
		WareClient[] as = getWareClients();
		int size = (as != null ? as.length : 0);
		for (int i = 0; i < size; i++) {
			WareClient client = as[i];
			client.erase(phase);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#reveal()
	 */
	@Override
	public void reveal() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#exhibit(com.laxcus.law.cross.PassiveItem)
	 */
	@Override
	protected void exhibit(PassiveItem item) {
		DatabaseClient[] as = getDatabaseClients();
		int size = (as != null ? as.length : 0);
		for(int i =0; i < size; i++) {
			DatabaseClient client = as[i];
			client.exhibit(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.law.cross.PassiveItem)
	 */
	@Override
	protected void erase(PassiveItem item) {
		DatabaseClient[] as = getDatabaseClients();
		int size = (as != null ? as.length : 0);
		for(int i =0; i < size; i++) {
			DatabaseClient client = as[i];
			client.erase(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#exhibit(com.laxcus.tub.TubTag)
	 */
	@Override
	protected void exhibit(TubTag tag) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.tub.TubTag)
	 */
	@Override
	protected void erase(TubTag tag) {
		// TODO Auto-generated method stub
		
	}

}