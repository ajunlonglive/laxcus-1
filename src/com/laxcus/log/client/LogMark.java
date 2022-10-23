/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

/**
 * 日志标签，在“local.xml”文件。
 * 
 * 带“MK”前缀是标识符。
 * 
 * @author scott.liang
 * @version 1.0 12/12/2015
 * @since laxcus 1.0
 */
public final class LogMark {

	/** 日志标签 **/
	public static final String LOG = "log";

	/** 套接字超时 **/
	public static final String RECEIVE_TIMEOUT = "receive-timeout";
	
	/** 子包超时 **/
	public static final String SUBPACKET_TIMEOUT = "subpacket-timeout";

	/** 日志级别 **/
	public static final String LEVEL = "level";

	/** 控制台打印 **/
	public static final String CONSOLE_PRINT = "console-print";

	/** 本地保存日志目录 **/
	public static final String DIRECTORY = "directory";

	/** 最大日志文件尺寸 **/
	public static final String FILESIZE = "filesize";

	/** 日志传输模式**/
	public static final String SEND_MODE = "send-mode";

	/** 日志在内存的缓冲尺寸 **/
	public static final String BUFFER_SIZE = "buffer-size";

	/** 日志向服务器的发送间隔时间 **/
	public static final String SEND_INTERVAL = "send-interval";


	/** 服务器 **/
	public static final String MK_SERVER = "SERVER";

	/** 磁盘文件 **/
	public static final String MK_FILE = "FILE";

	/** 缓冲 **/
	public static final String MK_BUFFER = "BUFFER";

	/** 不定义 **/
	public static final String MK_NONE = "NONE";
}
