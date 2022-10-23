/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.front.desktop.pool.*;
import com.laxcus.platform.listener.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 用户资源适配器
 * 
 * @author scott.liang
 * @version 1.0 3/6/2022
 * @since laxcus 1.0
 */
public class DesktopDatabaseAdapter implements DatabaseListener {

	/**
	 * 构造用户资源适配器
	 */
	public DesktopDatabaseAdapter() {
		super();
	}

	/**
	 * 资源管理池
	 * @return
	 */
	protected StaffOnDesktopPool getStaffPool() {
		return DesktopLauncher.getInstance().getStaffPool();
	}

	/**
	 * 取出数据库名称
	 * @return Fame数组
	 */
	@Override
	public Fame[] getFames() {
		StaffOnDesktopPool pool = getStaffPool();
		List<Fame> list = pool.getFames();
		int size = (list != null ? list.size() : 0);
		if (size > 0) {
			Fame[] a = new Fame[list.size()];
			return list.toArray(a);
		} else {
			return new Fame[0];
		}
	}

	/**
	 * 取关联的表名称
	 * @param fame
	 * @return
	 */
	@Override
	public Space[] getSpaces(Fame fame) {
		StaffOnDesktopPool pool = getStaffPool();
		List<Space> list = pool.getSpaces();
		int size = (list != null ? list.size() : 0);
		if (size > 0) {
			ArrayList<Space> array = new ArrayList<Space>();
			for (Space space : list) {
				if (Laxkit.compareTo(space.getSchema(), fame) == 0) {
					array.add(space);
				}
			}
			Space[] a = new Space[array.size()];
			return array.toArray(a);
		} else {
			return new Space[0];
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.DatabaseListener#getSpaces()
	 */
	@Override
	public Space[] getSpaces() {
		StaffOnDesktopPool pool = getStaffPool();
		List<Space> list = pool.getSpaces();
		int size = (list != null ? list.size() : 0);
		if (size > 0) {
			Space[] array = new Space[size];
			return list.toArray(array);
		} else {
			return new Space[0];
		}
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.platform.ResourceAssistor#findTableSites(com.laxcus.access.schema.Space)
	 */
	@Override
	public NodeSet findTableSites(Space space) {
		StaffOnDesktopPool pool = getStaffPool();
		return pool.findTableSites(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.ResourceAssistor#findTable(com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findTable(Space space) {
		StaffOnDesktopPool pool = getStaffPool();
		return pool.findTable(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.ResourceAssistor#findPassiveTable(com.laxcus.access.schema.Space)
	 */
	@Override
	public Table findPassiveTable(Space space) {
		StaffOnDesktopPool pool = getStaffPool();
		return pool.findPassiveTable(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.DatabaseListener#isPassiveTable(com.laxcus.access.schema.Space)
	 */
	@Override
	public boolean isPassiveTable(Space space) {
		StaffOnDesktopPool pool = getStaffPool();
		return pool.isPassiveTable(space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.DatabaseListener#canTable(com.laxcus.access.schema.Space, int)
	 */
	@Override
	public boolean canTable(Space space, short operator) {
		StaffOnDesktopPool pool = getStaffPool();
		return pool.canTable(space, operator);
	}

}

//	/* (non-Javadoc)
//	 * @see com.laxcus.platform.ResourcePicker#getCallSites()
//	 */
//	@Override
//	public Node[] getCallSites() {
//		NodeSet set = getStaffPool().getCallSites();
//		if (set != null) {
//			return set.toArray();
//		}
//		return null;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.platform.ResourcePicker#getEntranceSite()
//	 */
//	@Override
//	public Node getEntranceSite() {
//		return DesktopLauncher.getInstance().getRootHub();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.platform.ResourcePicker#getGateSite()
//	 */
//	@Override
//	public Node getGateSite() {
//		return DesktopLauncher.getInstance().getHub();
//	}

///*
// * (non-Javadoc)
// * @see com.laxcus.platform.ResourceAssistor#findPhases(int)
// */
//@Override
//public Phase[] findPhases(int family) {
//	StaffOnDesktopPool pool = getStaffPool();
//	List<Phase> list = pool.findPhases(family);
//	int size = (list != null ? list.size() : 0);
//	if (size > 0) {
//		Phase[] a = new Phase[size];
//		return list.toArray(a);
//	}
//	return null;
//}
//
///* (non-Javadoc)
// * @see com.laxcus.platform.ResourceAssistor#findTaskSites(com.laxcus.util.naming.Phase)
// */
//@Override
//public NodeSet findTaskSites(Phase phase) {
//	StaffOnDesktopPool pool = getStaffPool();
//	return pool.findTaskSites(phase);
//}


//	/* (non-Javadoc)
//	 * @see com.laxcus.platform.ResourceAssistor#register(com.laxcus.platform.display.DistributedComponentDisplay)
//	 */
//	@Override
//	public boolean register(DistributedComponentTrigger e) {
//		StaffOnDesktopPool pool = getStaffPool();
//		return pool.addDistributedComponentTrigger(e);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.platform.ResourceAssistor#unregister(com.laxcus.platform.display.DistributedComponentDisplay)
//	 */
//	@Override
//	public boolean unregister(DistributedComponentTrigger e) {
//		StaffOnDesktopPool pool = getStaffPool();
//		return pool.removeDistributedComponentTrigger(e);
//	}

