/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

/**
 * 用户许可证
 * 保存配置参数
 * 
 * @author scott.liang
 * @version 1.0 7/10/2020
 * @since laxcus 1.0
 */
public class Licence {

	/** 判断节点输入签名和许可证签名，忽略 **/
	public static final int LICENCE_IGNORE = 1;

	/** 判断节点输入签名和许可证签名，拒绝 **/
	public static final int LICENCE_REFUSE = 2;

	/** 判断节点输入签名和许可证签名，通过! **/
	public static final int LICENCE_ALLOW = 3;
	
}