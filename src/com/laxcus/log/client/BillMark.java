/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

/**
 * 操作标签，在“local.xml”文件。
 * 
 * 带“MK”前缀是标识符。
 * 
 * @author scott.liang
 * @version 1.0 10/13/2020
 * @since laxcus 1.0
 */
public final class BillMark {

	/** 操作标签 **/
	public static final String BILL = "bill";
	
	/** 套接字超时 **/
	public static final String RECEIVE_TIMEOUT = "receive-timeout";
	
	/** 子包超时 **/
	public static final String SUBPACKET_TIMEOUT = "subpacket-timeout";

	/** 操作级别，多个字符串，用逗号隔开，如：command, message, warning **/
	public static final String TYPE = "type";

	/** 控制台打印 **/
	public static final String CONSOLE_PRINT = "console-print";

	/** 本地保存操作目录 **/
	public static final String DIRECTORY = "directory";

	/** 最大操作文件尺寸 **/
	public static final String FILESIZE = "filesize";

	/** 操作传输模式**/
	public static final String SEND_MODE = "send-mode";

	/** 操作在内存的缓冲尺寸 **/
	public static final String BUFFER_SIZE = "buffer-size";

	/** 操作向服务器的发送间隔时间 **/
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
