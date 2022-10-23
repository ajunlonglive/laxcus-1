/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.archive;

/**
 * 向导组件、任务组件标记参数
 * 
 * @author scott.liang
 * @version 1.0 6/24/2020
 * @since laxcus 1.0
 */
public class TF {

//	/** 私有组件包文件 **/
//	public final static String SELFLY_FILE = "selfly.dtc";

	/** 分布组件集的配置路径(在dtg包目录中，大小写敏感，全字符匹配) **/
	public final static String GROUP_INF = "GROUP-INF/group.xml";

	/** 分布组件集文件后缀 **/
	public final static String DTG_SUFFIX = ".dtg";

	/** 分布组件集文件正则表达式，以DTG后缀结束，忽略大小写 **/
	public final static String DTG_REGEX = "^\\s*([\\w\\W]+)(?i)(\\.DTG)\\s*$";

	/** 分布组件的配置路径(在dtc包目录中，大小写敏感，全字符匹配) **/
	public final static String TASK_INF = "TASK-INF/tasks.xml";

	/** 分布组件的文件后缀 **/
	public final static String DTC_SUFFIX = ".dtc";

	/** 分布组件的文件正则表达式，以DTC后缀结束，忽略大小写 **/
	public final static String DTC_REGEX = "^\\s*([\\w\\W]+)(?i)(\\.DTC)\\s*$";
	
	/** 向导组件的配置路径(在gtc包目录中，大小写敏感，全字符匹配) **/
	public final static String GUIDE_INF = "GUIDE-INF/guides.xml";
	
	/** 向导组件的文件后缀 **/
	public final static String GTC_SUFFIX = ".gtc";

	/** 向导组件的文件正则表达式，以GTC后缀结束，忽略大小写 **/
	public final static String GTC_REGEX = "^\\s*([\\w\\W]+)(?i)(\\.GTC)\\s*$";
	
}
