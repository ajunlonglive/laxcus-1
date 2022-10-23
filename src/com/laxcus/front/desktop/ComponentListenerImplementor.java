/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.util.*;

import com.laxcus.front.desktop.pool.*;
import com.laxcus.platform.listener.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.set.*;

/**
 *
 * @author scott.liang
 * @version 1.0 2022-3-26
 * @since laxcus 1.0
 */
public class ComponentListenerImplementor implements WareListener {

	/**
	 * 
	 */
	public ComponentListenerImplementor() {
		super();
	}
	
	/**
	 * 资源管理池
	 * @return
	 */
	protected StaffOnDesktopPool getStaffPool() {
		return DesktopLauncher.getInstance().getStaffPool();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.platform.ResourceAssistor#findPhases(int)
	 */
	@Override
	public Phase[] findPhases(int family) {
		StaffOnDesktopPool pool = getStaffPool();
		List<Phase> list = pool.findPhases(family);
		int size = (list != null ? list.size() : 0);
		if (size > 0) {
			Phase[] a = new Phase[size];
			return list.toArray(a);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.ResourceAssistor#findTaskSites(com.laxcus.util.naming.Phase)
	 */
	@Override
	public NodeSet findTaskSites(Phase phase) {
		StaffOnDesktopPool pool = getStaffPool();
		return pool.findTaskSites(phase);
	}

}
