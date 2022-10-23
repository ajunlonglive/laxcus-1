/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import java.util.*;

/**
 * CALL节点注册用户分析器
 * 
 * @author scott.liang
 * @version 1.0 10/25/2019
 * @since laxcus 1.0
 */
final class MemberChecker extends TimerTask {
	
	/** 资源管理池 **/
	private StaffOnCallPool staffPool;

	/**
	 * 资源分析器
	 * @param e
	 */
	public MemberChecker(StaffOnCallPool e) {
		staffPool = e;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		staffPool.checkMembers();
	}

}