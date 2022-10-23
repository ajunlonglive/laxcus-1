/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

import java.security.*;

/**
 * 站点启动器许可 <br>
 * 
 * 站点在运行过程中，调用SiteLauncher方法前日，进行安全检查<br><br>
 * 
 * 在各节点 *.policy 文件中的格式：<br>
 * permission com.laxcus.launch.SiteLauncherPermission "using.方法名"; <br><br> 
 * 
 * 注意：SiteLauncherPermission只需要方法名称（目标名称），不包含操作行为列表，即"actions"项是忽略的。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2014
 * @since laxcus 1.0
 */
public class SiteLauncherPermission extends BasicPermission {

	private static final long serialVersionUID = 8048426494159384708L;

	/**
	 * 构造站点启动器许可
	 * @param name 目标名称
	 */
	public SiteLauncherPermission(String name) {
		super(name);
	}

	/**
	 * 构造站点启动器许可
	 * @param name 目标名称
	 * @param actions 操作行为
	 */
	public SiteLauncherPermission(String name, String actions) {
		super(name, actions);
	}

}