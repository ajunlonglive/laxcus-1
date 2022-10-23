/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import com.laxcus.front.desktop.pool.*;
import com.laxcus.front.pool.*;
import com.laxcus.platform.listener.*;
import com.laxcus.site.*;

/**
 * FRONT节点资源适配器
 * 
 * @author scott.liang
 * @version 1.0 3/26/2022
 * @since laxcus 1.0
 */
public class FrontListenerImplementor implements FrontListener {

	/**
	 * 
	 */
	public FrontListenerImplementor() {
		super();
	}

	/**
	 * 资源管理池
	 * @return
	 */
	protected StaffOnDesktopPool getStaffPool() {
		return DesktopLauncher.getInstance().getStaffPool();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.platform.listener.FrontListener#getCloudSites()
	 */
	@Override
	public Node[] getCloudSites() {
		java.util.List<Node> list = getStaffPool().getCloudSites();
		if (list == null || list.size() == 0) {
			return new Node[0];
		}

		// 输出数组
		Node[] hubs = new Node[list.size()];
		return list.toArray(hubs);
	}


	/* (non-Javadoc)
	 * @see com.laxcus.platform.ResourcePicker#getCallSites()
	 */
	@Override
	public Node[] getCallSites() {
		java.util.List<Node> list = CallOnFrontPool.getInstance().getHubs();
		if (list == null || list.size() == 0) {
			return new Node[0];
		}

		// 输出数组
		Node[] hubs = new Node[list.size()];
		return list.toArray(hubs);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.ResourcePicker#getEntranceSite()
	 */
	@Override
	public Node getEntranceSite() {
		return DesktopLauncher.getInstance().getRootHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.platform.ResourcePicker#getGateSite()
	 */
	@Override
	public Node getGateSite() {
		return DesktopLauncher.getInstance().getHub();
	}



}
