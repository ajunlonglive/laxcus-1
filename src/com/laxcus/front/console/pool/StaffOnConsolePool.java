/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.console.pool;

import com.laxcus.access.schema.*;
import com.laxcus.front.pool.*;
import com.laxcus.law.cross.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.util.naming.*;

/**
 * 控制台站点资源管理池 <br>
 * 
 * @author scott.liang
 * @version 1.2 9/12/2014
 * @since laxcus 1.0
 */
public class StaffOnConsolePool extends StaffOnFrontPool {

	/** 资源管理池的静态句柄，全局唯一 **/
	private static StaffOnConsolePool selfHandle = new StaffOnConsolePool();

	/**
	 * 构造资源管理池
	 */
	private StaffOnConsolePool() {
		super();
	}

	/**
	 * 返回资源管理池的静态句柄
	 * @return StaffOnConsolePool句柄
	 */
	public static StaffOnConsolePool getInstance() {
		return StaffOnConsolePool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#exhibit(com.laxcus.access.schema.Schema)
	 */
	@Override
	protected void exhibit(Schema schema) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.access.schema.Fame)
	 */
	@Override
	protected void erase(Fame fame) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#exhibit(com.laxcus.access.schema.Table)
	 */
	@Override
	protected void exhibit(Table table) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.access.schema.Space)
	 */
	@Override
	protected void erase(Space space) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#exhibit(com.laxcus.util.naming.Phase)
	 */
	@Override
	protected void exhibit(Phase phase) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.util.naming.Phase)
	 */
	@Override
	protected void erase(Phase phase) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.pool.StaffOnFrontPool#erase(com.laxcus.law.cross.PassiveItem)
	 */
	@Override
	protected void erase(PassiveItem item) {
		// TODO Auto-generated method stub
		
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
