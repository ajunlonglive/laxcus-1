/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cloud.task;

/**
 * tasks.xml文件里的用户配置标签
 * 
 * @author scott.liang
 * @version 1.0 11/8/2017
 * @since laxcus 1.0
 */
public class TaskMark {

	/** 用户签名 **/
	public static final String SIGN = "sign";

	/** 阶段命名 **/
	public static final String PHASE = "phase";

//	/** 分布任务组件版本号 **/
//	public static final String VERSION = "version";

	/** 分布任务根标签 **/
	public static final String TASK = "task";

	/** 分布任务命名。必须保证系统内唯一 **/
	public static final String TASK_NAMING = "naming";

	/** 分布任务接口引导类 **/
	public static final String TASK_CLASS = "boot-class";

	/** 分布任务资源。可以是一段文本内容，或者指向DTC文件的路径 **/
	public static final String TASK_RESOURCE = "resource";

	/** 分布任务项目实例 **/
	public static final String TASK_PROJECT_CLASS = "project-class";

	//	<task>
	//	<naming> DEMO_SORT </naming> <!-- on call site -->
	//	<boot-class> com.laxcus.task.demo.sort.balance.SortBalanceTask </boot-class>
	//	<resource> <![CDATA[ select balance data ]]> </resource>
	//	<project-class> com.laxcus.task.DefaultProject </project-class>
	//</task>

}
