/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

import java.security.*;

/**
 * 分布资源管理器安全许可 <br>
 * 
 * 各种Task实例调用外部API时，系统对这个Task来源和操作许可进行安全检查。<br><br>
 * 
 * 
 * CALL/DATA/WORK/BUILD节点的 conf/*.policy 文件中的格式：<br>
 * permission com.laxcus.task.DistributeManagerPermission "using.方法名"; <br><br> 
 * 
 * 注意：DistributeManagerPermission只需要方法名称（目标名称），不包含操作行为列表，即"actions"项是忽略的。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2014
 * @since laxcus 1.0
 */
public class TrackManagerPermission extends BasicPermission {

	private static final long serialVersionUID = -77527466277291950L;

	/**
	 * 构造分布资源管理器安全许可
	 * @param name 目标名称
	 */
	public TrackManagerPermission(String name) {
		super(name);
	}

	/**
	 * 构造分布资源管理器安全许可
	 * @param name 目标名称
	 * @param actions 操作行为
	 */
	public TrackManagerPermission(String name, String actions) {
		super(name, actions);
	}

}