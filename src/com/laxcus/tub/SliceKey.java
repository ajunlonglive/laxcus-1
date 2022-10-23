/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub;

/**
 * FIXP协议消息键
 * 
 * @author scott.liang
 * @version 1.2 3/13/2013
 * @since laxcus 1.0
 */
public final class SliceKey {

	/* 公共键列表 */

	/** 对话！通信双方都会使用 **/
	public final static short SPEAK = 1000;

	/** 超时 ... **/
	public static final short TIMEOUT = 1001;
	
	/** 序列号 **/
	public final static short SERIAL_NUMBER = 1002;

	/** 串行类型 **/
	public final static short SERIAL_TYPE = 1010;

	/** 串行对象 **/
	public final static short SERIAL_OBJECTS = 1011;

	/** 内容长度 **/
	public final static short CONTENT_LENGTH = 1020;

	/** 内容类型 **/
	public final static short CONTENT_TYPE = 1021;

	/** 内容单元数目 **/
	public final static short CONTENT_ITEMS = 1022;

	/** 二进制数据类型，对应CONTENT_TYPE **/
	public static final short RAW_DATA = 1030;
	
	/** 单向通知，要求服务端不用返回应答数据包  **/
	public final static short DIRECT_NOTIFY = 1040;

	/** 安全操作类型，见SecureType中的定义 */
	public static final short SECURE_FAMILY = 1200;

	/** 删除密文，出现在HELO命令里。 **/
	public final static short DROP_SECURE = 1201;

	/** 主机地址 **/
	public final static short HOST = 2001;

	/** 节点地址 */
	public final static short NODE_ADDRESS = 2002;
	
	/** 快速投递标记 **/
	public static final short CAST_FLAG = 2003;
	public static final short CAST_CODE = 2004;
	public static final short EACH_KEY = 2005;
	
	/** KEEP UDP子包键列表 */

	/**
	 * 包编号，相对一次数据发送，多个FIXP数据包发送序列而言。是一个标准的包编号。
	 * 而对于子包，包编号是判断多个子包是属于某个FIXP数据包的唯一判断条件
	 **/
	public final static short PACKET_IDENTIFY = 3000;

	/** 子包统计数目 **/
	public final static short SUBPACKET_COUNT = 3001;

	/** 子包序号 **/
	public final static short SUBPACKET_SERIAL = 3002;

	/** 子包超时时间 **/
	public final static short SUBPACKET_TIMEOUT = 3003;
	
	/** 失效超时时间 **/
	public final static short SUBPACKET_DISABLE_TIMEOUT = 3004;
	
	/** 以下五个是ReplyClient - ReplyReceive 之间的流量控制协调参数 **/
	
	/** 子包传输模式 **/
	public final static short SUBPACKET_TRANSFER_MODE = 3005;

	/** 子包发送间隔 **/
	public final static short SUBPACKET_SEND_INTERVAL = 3006;

	/** 默认子包内容尺寸 **/
	public final static short SUBPACKET_CONTENT_SIZE = 3007;

	/** 子包单次发送成员数 **/
	public final static short SUBPACKET_UNIT = 3008;

	/** 发送后等待反馈包时的SOCKET接收超时 **/
	public final static short SUBPACKET_FEEDBACK_TIMEOUT = 3009;

}