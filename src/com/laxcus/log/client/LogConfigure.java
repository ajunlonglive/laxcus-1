/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com  All rights reserved. 
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.client;

import java.io.*;

import org.w3c.dom.*;

import com.laxcus.util.*;
import com.laxcus.xml.*;

/**
 * 日志配置资料
 * 
 * @author scott.liang
 * @version 1.0 3/27/2009
 * @since laxcus 1.0
 */
public final class LogConfigure {

	/** 
	 * 日志数据传输模式：<br>
	 * 1. 不发送 <br>
	 * 2. 发送到缓存（先保存到内存中，超出容量后释放）<br>
	 * 3. 发送到磁盘文件 <br>
	 * 4. 发送到日志服务器 <br>
	 **/
	public final static int NOT_SEND = 0;
	public final static int SENDTO_BUFFER = 1;
	public final static int SENDTO_DISK = 2;
	public final static int SENDTO_SERVER = 3;

	/** SOCKET接收超时，单位：毫秒 **/
	private int receiveTimeout;
	
	/** FIXP 子包超时，单位：毫秒 **/
	private int subpacketTimeout;
	
	/** 在控制台打印 **/
	private boolean print;

	/** 日志数据传输数据 **/
	private int transferMode;

	/** 本地日志缓存区 **/
	private int buffsize;

	/** 日志发送间隔（输出到磁盘或者发送到日志服务器的间隔时间）。单位：秒 */
	private int sendInterval;

	/** 本地日志写入目录 **/
	private File localPath;

	/** 本地保存时，日志文件最大尺寸 */
	private int filesize;

	/** 日志级别，低于这个级别的日志不发送 **/
	private int level;

	/**
	 * 初始化日志配置
	 */
	public LogConfigure() {
		super();
		// 不在终端上打印日志
		setConsolePrint(false);
		// 默认是发送缓存模式
		setTransferMode(LogConfigure.SENDTO_BUFFER);
		// 默认是调试状态
		setLevel(LogLevel.DEBUG);
		// 日志发送间隔时间
		setSendInterval(5);
		
		// 默认时间
		setSubPacketTimeout(20000);
		setReceiveTimeout(60000);
	}

	/**
	 * 设置FIXP客户端套接字子包超时时间，即"KEEP UDP TIMEOUT"的时间
	 * 
	 * @param ms 毫秒
	 */
	public void setSubPacketTimeout(int ms) {
		subpacketTimeout = ms;
	}
	
	/**
	 * 返回FIXP客户端套接字子包超时时间
	 * @return 毫秒
	 */
	public int getSubPacketTimeout() {
		return subpacketTimeout;
	}

	/**
	 * FIXP客户端套接字接收数据包超时
	 * 
	 * @param ms 毫秒
	 */
	public void setReceiveTimeout(int ms) {
		receiveTimeout = ms;
	}

	/**
	 * 返回FIXP客户端套接字接收数据包超时
	 * @return
	 */
	public int getReceiveTimeout() {
		return receiveTimeout;
	}
	
	/**
	 * 设置显示终端打印
	 * @param b
	 */
	public void setConsolePrint(boolean b) {
		print = b;
	}

	/**
	 * 是否在本地终端打印日志
	 * @return
	 */
	public boolean isConsolePrint() {
		return print;
	}

	/**
	 * 设置日志传输模式
	 * @param who 传输模式
	 */
	public void setTransferMode(int who) {
		switch (who) {
		case LogConfigure.NOT_SEND:
		case LogConfigure.SENDTO_BUFFER:
		case LogConfigure.SENDTO_DISK:
		case LogConfigure.SENDTO_SERVER:
			transferMode = who;
			break;
		default:
			throw new IllegalValueException("illegal transfer mode %d", who);
		}
	}

	/**
	 * 返回日志发送模式(none, buffer,file, server)
	 * @return
	 */
	public int getTransferMode() {
		return transferMode;
	}

	/**
	 * 不发送日志
	 * @return
	 */
	public boolean isNotSend() {
		return transferMode == LogConfigure.NOT_SEND;
	}

	/**
	 * 缓存发送模式
	 * @return
	 */
	public boolean isSendToBuffer() {
		return transferMode == LogConfigure.SENDTO_BUFFER;
	}

	/**
	 * 发送到磁盘文件
	 * @return
	 */
	public boolean isSendToDisk() {
		return transferMode == LogConfigure.SENDTO_DISK;
	}

	/**
	 * 发送到日志服务器
	 * @return
	 */
	public boolean isSendToServer() {
		return transferMode == LogConfigure.SENDTO_SERVER;
	}

	/**
	 * 本地日志写入目录
	 * @return
	 */
	public File getDirectory() {
		return localPath;
	}

	/**
	 * 本地日志写入目录
	 */
	public void setDirectory(File file) {
		localPath = file;
	}
	
	/**
	 * 本地日志最大保存尺寸
	 * @return
	 */
	public int getFileSize() {
		return filesize;
	}

	/**
	 * 设置日志文件尺寸
	 * @param len
	 */
	public void setFileSize(int len) {
		filesize = len;
	}

	/**
	 * 本地日志缓存区尺寸
	 * @return
	 */
	public int getBufferSize() {
		return buffsize;
	}
	
	/**
	 * 设置本地日志缓存区尺寸
	 * @param len
	 */
	public void setBufferSize(int len) {
		buffsize = len;
	}

	/**
	 * 日志发送间隔时间。单位：秒。
	 * @param second 间隔时间
	 */
	public void setSendInterval(int second) {
		if (second < 5) {
			sendInterval = 5;
		} else {
			sendInterval = second;
		}
	}

	/**
	 * 日志发送间隔时间（日志在本地内存的滞留时间）。单位：秒 
	 * @return
	 */
	public int getSendInterval() {
		return sendInterval;
	}

	/**
	 * 返回发送的日志等级
	 * @return 日志等级
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * 设置发送的日志等级
	 * @param who 日志等级
	 */
	public void setLevel(int who) {
		if (!LogLevel.isLevel(who)) {
			throw new IllegalValueException("invalid log level:%d", who);
		}
		level = who;
	}

	/**
	 * 加载并且解析日志配置文件
	 * @return boolean
	 */
	public boolean loadXML(String filename) {
		File file = new File(filename);
		if (!file.exists()) {
			return false;
		}
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b, 0, b.length);
			in.close();
			return loadXML(b);
		} catch (IOException exp) {
			exp.printStackTrace();
		}
		return false;
	}

	/**
	 * 解析传输模式
	 * @param input 输入句
	 * @return 返回对应的传输模式，不匹配返回-1。
	 */
	private int translateSendMode(String input) {
		if (input.matches("^\\s*(?i)(SERVER)\\s*$")) {
			return LogConfigure.SENDTO_SERVER;
		} else if (input.matches("^\\s*(?i)(FILE|DISK)\\s*$")) {
			return LogConfigure.SENDTO_DISK;
		} else if (input.matches("^\\s*(?i)(BUFFER)\\s*$")) {
			return LogConfigure.SENDTO_BUFFER;
		} else if (input.matches("^\\s*(?i)(NONE)\\s*$")) {
			return LogConfigure.NOT_SEND;
		}
		return -1;
	}

	/**
	 * 解析日志配置文件
	 * @param data XML数据
	 * @return 成功返回真，否则假
	 */
	public boolean loadXML(byte[] data) {
		Document document = XMLocal.loadXMLSource(data);
		if (document == null) {
			return false;
		}

		NodeList list = document.getElementsByTagName(LogMark.LOG); // "log");
		if (list.getLength() != 1) {
			Logger.error(this, "loadXML", "cannot be find 'log' tag");
			return false;
		}
		Element element = (Element) list.item(0);

		// 客户端接收超时/子包超时
		String value = XMLocal.getValue(element, LogMark.RECEIVE_TIMEOUT);
		receiveTimeout = (int) ConfigParser.splitTime(value, receiveTimeout);
		// 子包超时
		value = XMLocal.getValue(element, LogMark.RECEIVE_TIMEOUT);
		subpacketTimeout = (int) ConfigParser.splitTime(value, subpacketTimeout);
		
		// 日志等级
		value = XMLocal.getValue(element, LogMark.LEVEL); // "level");
		level = LogLevel.translate(value);
		// 判断日志级别有效
		if (!LogLevel.isLevel(level)) {
			throw new IllegalValueException("illegal log level: %s", value);
		}

		// 终端打印
		value = XMLocal.getValue(element, LogMark.CONSOLE_PRINT); // "console-print");
		print = ConfigParser.splitBoolean(value, false);

		// 本地日志文件目录
		value = XMLocal.getValue(element, LogMark.DIRECTORY); // "directory");
		value = ConfigParser.splitPath(value); // 过滤和转换关键字
		setDirectory(new File(value));

		// 日志文件尺寸，默认10M
		value = XMLocal.getValue(element, LogMark.FILESIZE); // "filesize");
		setFileSize((int) ConfigParser.splitLongCapacity(value, 0xA00000)); //10240 * 1024);

		// 传输模式
		value = XMLocal.getValue(element, LogMark.SEND_MODE); // "send-mode");
		transferMode = translateSendMode(value);
		if(transferMode == -1) {
			throw new IllegalValueException("invalid send mode:%s" , value);
		}

		// 日志缓存尺寸，默认10K
		value = XMLocal.getValue(element, LogMark.BUFFER_SIZE); // "buffer-size");
		setBufferSize((int) ConfigParser.splitLongCapacity(value, 10240));

		// 日志发送间隔时间（如果 send-mode = server 时，这个参数生效）
		value = XMLocal.getValue(element, LogMark.SEND_INTERVAL); // "send-interval");
		sendInterval = (int) (ConfigParser.splitTime(value, 10000) / 1000);

		return true;
	}

}