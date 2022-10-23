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
 * 操作记录配置资料
 * 
 * @author scott.liang
 * @version 1.0 10/13/2022
 * @since laxcus 1.0
 */
public final class BillConfigure {

	/** 
	 * 操作记录数据传输模式：<br>
	 * 1. 不发送 <br>
	 * 2. 发送到缓存（先保存到内存中，超出容量后释放）<br>
	 * 3. 发送到磁盘文件 <br>
	 * 4. 发送到操作记录服务器 <br>
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

	/** 操作记录数据传输数据 **/
	private int transferMode;

	/** 本地操作记录缓存区 **/
	private int buffsize;

	/** 操作记录发送间隔（输出到磁盘或者发送到操作记录服务器的间隔时间）。单位：秒 */
	private int sendInterval;

	/** 本地操作记录写入目录 **/
	private File localPath;

	/** 本地保存时，操作记录文件最大尺寸 */
	private int filesize;

	/**
	 * 初始化操作记录配置
	 */
	public BillConfigure() {
		super();
		// 不在终端上打印操作记录
		setConsolePrint(false);
		// 默认是发送缓存模式
		setTransferMode(BillConfigure.SENDTO_BUFFER);
		// 操作记录发送间隔时间
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
	 * 是否在本地终端打印操作记录
	 * @return
	 */
	public boolean isConsolePrint() {
		return print;
	}

	/**
	 * 设置操作记录传输模式
	 * @param who 传输模式
	 */
	public void setTransferMode(int who) {
		switch (who) {
		case BillConfigure.NOT_SEND:
		case BillConfigure.SENDTO_BUFFER:
		case BillConfigure.SENDTO_DISK:
		case BillConfigure.SENDTO_SERVER:
			transferMode = who;
			break;
		default:
			throw new IllegalValueException("illegal transfer mode %d", who);
		}
	}

	/**
	 * 返回操作记录发送模式(none, buffer,file, server)
	 * @return
	 */
	public int getTransferMode() {
		return transferMode;
	}

	/**
	 * 不发送操作记录
	 * @return
	 */
	public boolean isNotSend() {
		return transferMode == BillConfigure.NOT_SEND;
	}

	/**
	 * 缓存发送模式
	 * @return
	 */
	public boolean isSendToBuffer() {
		return transferMode == BillConfigure.SENDTO_BUFFER;
	}

	/**
	 * 发送到磁盘文件
	 * @return
	 */
	public boolean isSendToDisk() {
		return transferMode == BillConfigure.SENDTO_DISK;
	}

	/**
	 * 发送到操作记录服务器
	 * @return
	 */
	public boolean isSendToServer() {
		return transferMode == BillConfigure.SENDTO_SERVER;
	}

	/**
	 * 本地操作记录写入目录
	 * @return
	 */
	public File getDirectory() {
		return localPath;
	}

	/**
	 * 本地操作记录写入目录
	 */
	public void setDirectory(File file) {
		localPath = file;
	}

	/**
	 * 本地操作记录最大保存尺寸
	 * @return
	 */
	public int getFileSize() {
		return filesize;
	}

	/**
	 * 设置操作记录文件尺寸
	 * @param len
	 */
	public void setFileSize(int len) {
		filesize = len;
	}

	/**
	 * 本地操作记录缓存区尺寸
	 * @return
	 */
	public int getBufferSize() {
		return buffsize;
	}

	/**
	 * 设置本地操作记录缓存区尺寸
	 * @param len
	 */
	public void setBufferSize(int len) {
		buffsize = len;
	}

	/**
	 * 操作记录发送间隔时间。单位：秒。
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
	 * 操作记录发送间隔时间（操作记录在本地内存的滞留时间）。单位：秒 
	 * @return
	 */
	public int getSendInterval() {
		return sendInterval;
	}

	/**
	 * 加载并且解析操作记录配置文件
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
			return BillConfigure.SENDTO_SERVER;
		} else if (input.matches("^\\s*(?i)(FILE|DISK)\\s*$")) {
			return BillConfigure.SENDTO_DISK;
		} else if (input.matches("^\\s*(?i)(BUFFER)\\s*$")) {
			return BillConfigure.SENDTO_BUFFER;
		} else if (input.matches("^\\s*(?i)(NONE)\\s*$")) {
			return BillConfigure.NOT_SEND;
		}
		return -1;
	}

	/**
	 * 解析操作记录配置文件
	 * @param data XML数据
	 * @return 成功返回真，否则假
	 */
	public boolean loadXML(byte[] data) {
		Document document = XMLocal.loadXMLSource(data);
		if (document == null) {
			return false;
		}

		NodeList list = document.getElementsByTagName(BillMark.BILL);
		if (list.getLength() != 1) {
			Logger.error(this, "loadXML", "cannot be find 'bill' tag");
			return false;
		}
		Element element = (Element) list.item(0);

		// 客户端接收超时/子包超时
		String value = XMLocal.getValue(element, BillMark.RECEIVE_TIMEOUT);
		receiveTimeout = (int) ConfigParser.splitTime(value, receiveTimeout);
		// 子包超时
		value = XMLocal.getValue(element, BillMark.RECEIVE_TIMEOUT);
		subpacketTimeout = (int) ConfigParser.splitTime(value, subpacketTimeout);

		// 类型定义
		value = XMLocal.getValue(element, BillMark.TYPE);
		if (value != null && value.trim().length() > 0) {
			int type = BillType.translateAll(value);
			Biller.setDefaultType(type);
		}

		// 终端打印
		value = XMLocal.getValue(element, BillMark.CONSOLE_PRINT); 
		print = ConfigParser.splitBoolean(value, false);

		// 本地操作记录文件目录
		value = XMLocal.getValue(element, BillMark.DIRECTORY); 
		value = ConfigParser.splitPath(value); // 过滤和转换关键字
		setDirectory(new File(value));

		// 操作记录文件尺寸，默认10M
		value = XMLocal.getValue(element, BillMark.FILESIZE); 
		setFileSize((int) ConfigParser.splitLongCapacity(value, 0xA00000)); 

		// 传输模式
		value = XMLocal.getValue(element, BillMark.SEND_MODE); 
		transferMode = translateSendMode(value);
		if(transferMode == -1) {
			throw new IllegalValueException("invalid send mode:%s" , value);
		}

		// 操作记录缓存尺寸，默认10K
		value = XMLocal.getValue(element, BillMark.BUFFER_SIZE); 
		setBufferSize((int) ConfigParser.splitLongCapacity(value, 10240));

		// 操作记录发送间隔时间（如果 send-mode = server 时，这个参数生效）
		value = XMLocal.getValue(element, BillMark.SEND_INTERVAL); 
		sendInterval = (int) (ConfigParser.splitTime(value, 10000) / 1000);

		return true;
	}

}