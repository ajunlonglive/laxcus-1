/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.util;

import com.laxcus.util.naming.*;

/**
 * 连接操作参数
 * 
 * @author scott.liang
 *
 */
public class JoinTaskKit extends SQLTaskKit {

	//// JOIN 分布计算参数  ////
	
	public final static String JOINSELECT = "JOINSELECT"; // JOIN基础命名
	public final static String INNERJOIN = "INNERJOIN"; // 子命名，内联卡笛尔

	// JOIN阶段命名
	public final static Phase SELECT_FROM_INNERJOIN = new Phase(PhaseTag.FROM, Sock.doSystemSock( JOINSELECT), INNERJOIN);
	public final static Phase SELECT_TO_INNERJOING = new Phase(PhaseTag.TO, Sock.doSystemSock(JOINSELECT), INNERJOIN);
	public final static Phase JOIN_BALANCE = new Phase(PhaseTag.BALANCE, Sock.doSystemSock( JOINSELECT));

	// 变量
	public final static String JOIN_OBJECT = "JOIN_OBJECT";

	/**
	 * 
	 */
	public JoinTaskKit() {
		super();
	}

	
}
