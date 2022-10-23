/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool;

import java.security.*;

/**
 * 站点管理池许可 <br>
 * 
 * 站点在运行过程中，调用VirtualPool方法前日，进行安全检查<br><br>
 * 
 * 在各节点 *.policy 文件中的格式：<br>
 * permission com.laxcus.pool.VirtualPoolPermission "using.方法名"; <br><br> 
 * 
 * 注意：VirtualPoolPermission只需要方法名称（目标名称），不包含操作行为列表，即"actions"项是忽略的。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2014
 * @since laxcus 1.0
 */
public class VirtualPoolPermission extends BasicPermission {

	private static final long serialVersionUID = -4741881974047908478L;

	/**
	 * 构造站点管理池许可
	 * @param name 目标名称
	 */
	public VirtualPoolPermission(String name) {
		super(name);
	}

	/**
	 * 构造站点管理池许可
	 * @param name 目标名称
	 * @param actions 操作行为
	 */
	public VirtualPoolPermission(String name, String actions) {
		super(name, actions);
	}

}