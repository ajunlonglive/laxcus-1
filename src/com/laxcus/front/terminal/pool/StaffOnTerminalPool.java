/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal.pool;

import com.laxcus.access.schema.*;
import com.laxcus.front.pool.*;
import com.laxcus.front.terminal.*;
import com.laxcus.front.terminal.component.*;
import com.laxcus.law.cross.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.util.naming.*;

/**
 * 终端站点资源管理池 <br>
 * 
 * @author scott.liang
 * @version 1.2 9/12/2014
 * @since laxcus 1.0
 */
public class StaffOnTerminalPool extends StaffOnFrontPool {

	/** 资源管理池的静态句柄，全局唯一 **/
	private static StaffOnTerminalPool selfHandle = new StaffOnTerminalPool();

	/**
	 * 构造资源管理池
	 */
	private StaffOnTerminalPool() {
		super();
	}

	/**
	 * 返回资源管理池的静态句柄
	 * @return
	 */
	public static StaffOnTerminalPool getInstance() {
		return StaffOnTerminalPool.selfHandle;
	}

	/**
	 * 返回云端数据浏览面板
	 * @return
	 */
	private TerminalRemoteDataListPanel getRemoteDataListPanel() {
		return TerminalLauncher.getInstance().getWindow().getRemoteDataListPanel();
	}
	
	/**
	 * 返回云端软件浏览面板
	 * @return
	 */
	private TerminalRemoteSoftwareListPanel getRemoteSoftwareListPanel() {
		return TerminalLauncher.getInstance().getWindow().getRemoteSoftwareListPanel();
	}

	/**
	 * 返回本地 浏览面板
	 * @return
	 */
	private TerminalTubSoftwareListPanel getLocalBroswerListPanel() {
		return TerminalLauncher.getInstance().getWindow().getLocalTubListPanel();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#reveal()
	 */
	@Override
	public void reveal() {
		TerminalLauncher.getInstance().getWindow().showCyberStatus();
	}

	/**
	 * 显示数据库
	 * @param schema
	 */
	@Override
	protected void exhibit(Schema schema) {
		TerminalRemoteDataListPanel panel = getRemoteDataListPanel();
		panel.addSchema(schema);
	}

	/**
	 * 删除数据库
	 * @param fame 数据库名称
	 */
	@Override
	protected void erase(Fame fame) {
		TerminalRemoteDataListPanel panel = getRemoteDataListPanel();
		panel.removeSchema(fame);
	}

	/**
	 * 显示专属数据表
	 * @param table
	 */
	@Override
	protected void exhibit(Table table) {
		TerminalRemoteDataListPanel panel = getRemoteDataListPanel();
		panel.addTable(table);
	}

	/**
	 * 删除专属数据表
	 * @param space
	 */
	@Override
	protected void erase(Space space) {
		TerminalRemoteDataListPanel panel = getRemoteDataListPanel();
		panel.removeTable(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#exhibit(com.laxcus.law.cross.CrossTable)
	 */
	@Override
	protected void exhibit(PassiveItem item) {
		TerminalRemoteDataListPanel panel = getRemoteDataListPanel();
		panel.addPassiveItem(item);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.law.cross.CrossTable)
	 */
	@Override
	protected void erase(PassiveItem item) {
		TerminalRemoteDataListPanel panel = getRemoteDataListPanel();
		panel.removePassiveItem(item);
	}


	/**
	 * 显示阶段命名
	 * @param phase
	 */
	@Override
	protected void exhibit(Phase phase) {
		// 阶段命名不在范围时...
		boolean success = (PhaseTag.isInit(phase) || PhaseTag.isIssue(phase) || PhaseTag.isFork(phase));
		if (success) {
			//			TerminalRemoteDataListPanel panel = getRemoteBroswerListPanel();
			//			panel.addPhase(phase);

			TerminalRemoteSoftwareListPanel panel = getRemoteSoftwareListPanel();
			panel.addPhase(phase);
		}
	}

	/**
	 * 删除阶段命名
	 * @param phase
	 */
	@Override
	protected void erase(Phase phase) {
		//		TerminalRemoteDataListPanel panel = getRemoteBroswerListPanel();
		//		panel.removePhase(phase);

		TerminalRemoteSoftwareListPanel panel = getRemoteSoftwareListPanel();
		panel.removePhase(phase);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#exhibit(com.laxcus.tub.TubTag)
	 */
	@Override
	protected void exhibit(TubTag tag) {
		TerminalTubSoftwareListPanel panel = getLocalBroswerListPanel();
		panel.addTubTag(tag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.tub.TubTag)
	 */
	@Override
	protected void erase(TubTag tag) {
		TerminalTubSoftwareListPanel panel = getLocalBroswerListPanel();
		panel.removeTubTag(tag);
	}

}