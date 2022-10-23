/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.system.select.util;

/**
 * SQL分布处理操作参数集合
 * 
 * @author scott.liang
 * @version 1.0 9/23/2011
 * @since laxcus 1.0
 */
public class SQLTaskKit {

	/** 默认WORK节点数目，实际数量由CALL检测后修改 **/
	public static int DEFAULT_WORKSITES = 1000;

	/** 自定义参数中的SELECT对象名 **/
	public final static String SELECT_OBJECT = "SELECT_OBJECT";

//	/** 自定义参数中的列空间对象名称 **/
//	public final static String DOCK_OBJECT = "SELECT_DOCK";
	
	/**
	 * 
	 */
	public SQLTaskKit() {
		super();
	}

}