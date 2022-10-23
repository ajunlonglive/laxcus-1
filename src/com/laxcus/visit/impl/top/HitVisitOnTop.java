/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.top;

import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.*;
import com.laxcus.visit.*;
import com.laxcus.visit.hit.*;

/**
 * HitVisit接口的TOP站点实现。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/13/2009
 * @since laxcus 1.0
 */
public class HitVisitOnTop implements HitVisit {

	/**
	 * 构造默认HitVisitOnTop实例。
	 */
	public HitVisitOnTop() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hit.HitVisit#isManager()
	 */
	@Override
	public boolean isManager() throws VisitException {
		return TopLauncher.getInstance().isManager();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.hit.HitVisit#isManagerDisabled(com.laxcus.site.Node)
	 */
	@Override
	public boolean isManagerDisabled(Node hub) throws VisitException {
		// 向指定的节点发送FIXP.TEST命令（TCP/UDP两个通信），判断指定的节点已经失效！
		boolean disabled = !TopLauncher.getInstance().ring(hub);

		Logger.debug(this, "isManagerDisabled", disabled, "check disabled %s", hub);

		return disabled;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.visit.hit.HitVisit#discuss(com.laxcus.site.Node)
	 */
	@Override
	public boolean discuss(Node remote) throws VisitException {
		TopLauncher launcher = TopLauncher.getInstance();
		// 当前地址
		Node local = launcher.getListener();
		
		// 判断是MONITOR状态，且节点类型一致！
		boolean success = launcher.isMonitor() && (remote.getFamily() == local.getFamily());
		// 匹配时...
		if (success) {
			// 传入的站点排序比当前站点小，条件成立
			success = (remote.compareTo(local) < 0);
		}
		
		Logger.debug(this, "discuss", success, "%s < %s", remote, local);

		return success;
	}

}