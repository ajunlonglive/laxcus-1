/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.dict;

import java.util.*;

/**
 * ACCOUNT节点注册用户分析器
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
final class MemberChecker extends TimerTask {
	
	/** 资源管理池 **/
	private StaffOnAccountPool staffPool;

	/**
	 * 资源分析器
	 * @param e
	 */
	public MemberChecker(StaffOnAccountPool e) {
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