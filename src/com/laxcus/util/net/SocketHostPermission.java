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
 * 套接字地址许可 <br>
 * 
 * SocketHostPermission是限制用户操作“SocketHost”类。防止在未经授权的情况下，任意修改参数。
 * SocketHostPermission被要求配置在各站点的“conf/*.policy”文件中。
 * 
 * @author scott.liang
 * @version 1.0 10/09/2013
 * @since laxcus 1.0
 */
public class SocketHostPermission extends BasicPermission {

	private static final long serialVersionUID = 4818572067070295727L;

	/**
	 * 构造套接字地址许可
	 * @param name 目标名称
	 */
	public SocketHostPermission(String name) {
		super(name);
	}

	/**
	 * 构造套接字地址许可
	 * @param name 目标名称
	 * @param actions 操作行为
	 */
	public SocketHostPermission(String name, String actions) {
		super(name, actions);
	}

}