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
 * 查询插入配置
 * 
 * @author scott.liang
 * @version 1.0 11/30/2020
 * @since laxcus 1.0
 */
public class InjectSelectTaskKit {

	/** 自定义参数中的SELECT对象名 **/
	public final static String INJECT_SELECT_OBJECT = "INJECT_SELECT_OBJECT";

	/** 查询插入根命名(对应tasks.xml中的定义) **/
	private final static String INJECT_SELECT = "INJECT_SELECT";
	
	/** TO阶段插入 **/
	private final static String INJECT = "INJECT";
	
	/** BALANCE阶段命名 **/
	public final static Phase BALANCE = new Phase(PhaseTag.BALANCE, Sock.doSystemSock(INJECT_SELECT));

	/** TO阶段命名 **/
	public final static Phase TO = new Phase(PhaseTag.TO, Sock.doSystemSock(INJECT_SELECT), INJECT);
	
}
