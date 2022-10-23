/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

/**
 * 回显标签，在“local.xml”文件的“echo”标签段中。
 * 
 * @author scott.liang
 * @version 1.0 11/30/2016
 * @since laxcus 1.0
 */
public final class EchoMark {

	/** "echo"标签 */
	public static final String MARK_ECHO = "echo";

	/** 命令管理池 **/
	public static final String MARK_COMMAND_POOL = "command-pool";

	/** 调用器管理池 **/
	public static final String MARK_INVOKER_POOL = "invoker-pool";

	/** 调用器 **/
	public static final String MARK_INVOKER = "invoker";

	/** 命令管理池延时间隔 **/
	public static final String COMMAND_POOL_SILENT_TIME = "silent-time";

	/** 调用器管理池延时间隔 **/
	public static final String INVOKER_POOL_SILENT_TIME = "silent-time";

	/** 命令管理池线程堆栈尺寸 **/
	public static final String COMMAND_POOL_STACK_SIZE = "stack-size";

	/** 调用器管理池线程堆栈尺寸 **/
	public static final String INVOKER_POOL_STACK_SIZE = "stack-size";

	/** 回显目录 */
	public static final String ECHO_DIRECTORY = "echo-directory";

	/** 回显命令池（EchoCommandPool）最大命令数目，-1无限制 */
	public static final String MAX_COMMANDS = "max-commands";

	/** 回显调用器管理池（InvokerPool）的最大调用器数目，-1无限制 */
	public static final String MAX_INVOKERS = "max-invokers";
	
	/** 最大调用器数目标签的属性 **/
	public static final String MAX_INVOKERS_ATTR_CONFINE_TIME = "confine-time";

	/** CPU最大使用比例 */
	public static final String CPU_RATE = "cpu-rate";

	/** 虚拟机内存最大使用比例 */
	public static final String VM_MEMORY_RATE = "vm-memory-rate";
	
	/** 调用器超时失效检测间隔 **/
	public static final String DISABLE_CHECK_INTERVAL = "disable-check-interval";
	
	/** 命令在命令管理池，异步调用器在调用器管理池的超时时间，超过规定时间就删除。设置这个时间，避免无限等待。 */
	public static final String COMMAND_TIMEOUT = "command-timeout";
	
	/** 异步调用器在调用器管理池的超时时间，超过规定时间就删除。设置这个时间，避免无限等待。 */
	public static final String INVOKER_TIMEOUT = "timeout";
	
	/** 调用器线程堆栈尺寸 **/
	public static final String INVOKER_STACK_SIZE = "stack-size";

	/** 调用器发生交叠现象的再次重起时间。交叠现象即调用器的launch/end未完成，下一阶段的end已经要求启动。 */
	public static final String INVOKER_CROSS_INTERVAL = "cross-interval";

	/** 调用器的默认通信模式，TCP/UDP任意一种 */
	public static final String INVOKER_TRANSFER_MODE = "transfer-mode";

	/** 调用器FIXP UDP乱序传输发生网络故障后，调用器最大重试次数 **/
	public static final String INVOKER_TRANSFER_MAX_RETRY = "transfer-max-retry";

	/** 发生网络故障后，套接字重试间隔时间 **/
	public static final String INVOKER_TRANSFER_RETRY_INTERVAL = "transfer-retry-interval";
	
	/** 用户日志目录 */
	public static final String USERLOG_DIRECTORY = "userlog-directory";

	/** 用户日志文件长度 **/
	public static final String USERLOG_BLOCK_SIZE = "userlog-block-size";
	
	////////// 以下是了运行设备的最低限制，低于规定值将报警 ///////
	
	/** 设备最低限值 **/
	public static final String LEAST = "least";

	/** 计时器定时调用间隔 **/
	public static final String LEAST_INTERVAL = "interval";

	/** 最低环境内存，低于这个阀值将报警 ，用百分比或者容量值表示，如 20%, 512M，也可以忽略不定义 **/
	public static final String LEAST_MEMORY = "memory";

	/** 
	 * 最低磁盘空间限制，低于阀值将报警，允许设置统一的容量，如：20%， 1024M。
	 * 也可以针对某个目录定义，如：
	 * 1. 磁盘目录路径 20%
	 * 2. 磁盘目录路径 2096MB
	 */
	public static final String LEAST_DISK = "disk";

}