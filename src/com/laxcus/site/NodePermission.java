/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

import java.security.*;

/**
 * 节点操作许可 <br>
 * 
 * NodePermission是限制用户操作“Node”类，防止在未经授权的情况下，任意修改参数。
 * NodePermission被要求配置在各站点的“conf/*.policy”文件中。
 * 
 * @author scott.liang
 * @version 1.0 10/09/2013
 * @since laxcus 1.0
 */
public class NodePermission extends BasicPermission {

	private static final long serialVersionUID = 9164117338118743256L;

	/**
	 * 构造节点操作许可
	 * @param name 目标名称
	 */
	public NodePermission(String name) {
		super(name);
	}

	/**
	 * 构造节点操作许可
	 * @param name  目标名称
	 * @param actions 操作行为
	 */
	public NodePermission(String name, String actions) {
		super(name, actions);
	}

}