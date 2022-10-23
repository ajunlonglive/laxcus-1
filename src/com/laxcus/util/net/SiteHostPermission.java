/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.net;

import java.security.*;

/**
 * 站点主机操作许可 <br>
 * 
 * SiteHostPermission将限制用户操作“SiteHost”类，防止在未经授权的情况下，任意修改参数。
 * SiteHostPermission被要求配置在各站点的“conf/*.policy”文件中。
 * 
 * @author scott.liang
 * @version 1.0 10/09/2013
 * @since laxcus 1.0
 */
public class SiteHostPermission extends BasicPermission {

	private static final long serialVersionUID = -1851646483099716697L;

	/**
	 * 构造站点主机操作许可
	 * @param name 目标名称
	 */
	public SiteHostPermission(String name) {
		super(name);
	}

	/**
	 * 构造站点主机操作许可
	 * @param name 目标名称
	 * @param actions 操作行为
	 */
	public SiteHostPermission(String name, String actions) {
		super(name, actions);
	}

}