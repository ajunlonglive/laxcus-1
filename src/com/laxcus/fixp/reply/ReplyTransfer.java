/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import java.io.*;
import java.util.regex.*;

import com.laxcus.log.client.*;

/**
 * 异步传输参数。<br>
 * 数据在发送端以乱序发出，在接收端根据数据编号进行重组。
 * 
 * @author scott.liang
 * @version 1.0 08/12/2018
 * @since laxcus 1.0
 */
public class ReplyTransfer {

	/** 锁串行发送（加锁方式，单次发送指定数目子包） **/
	public final static int SERIAL_TRANSFER = 1;

	/** 无锁并行传输（完全开放，任意发送）**/
	public final static int PARALLEL_TRANSFER = 2;

	/** UDP数据包最大传输尺寸 **/
	public final static int MTU = 65507;

	/** 截止目前，命令：“Ask.NOTIFY, Ask.CAST”用来投递异步数据，它的包头实测尺寸是68个字节，考虑冗余将此扩大到90个字节。**/
	public final static int PACKET_HEADSIZE = 90;

	/** FIXP子包内容域最小尺寸，不能再小了！默认以INTERNET的IP MTU的576为基础参数，考虑FIXP子包包头的约90字节，数据域设定为：576-90=486 **/
	public final static int MIN_SUBPACKET_CONTENT_SIZE = 576 - ReplyTransfer.PACKET_HEADSIZE; // 486

	/** FIXP子包域最大尺寸。以UDP最大单元尺寸减去90个字节。**/
	public final static int MAX_SUBPACKET_CONTENT_SIZE = ReplyTransfer.MTU - ReplyTransfer.PACKET_HEADSIZE;

	/** FIXP子包域中间尺寸 **/
	public final static int MIDDLE_SUBPACKET_CONTENT_SIZE = ReplyTransfer.MAX_SUBPACKET_CONTENT_SIZE / 2; 

	/** 默认是串行传输模式 **/
	private static volatile int defaultTransferMode = ReplyTransfer.SERIAL_TRANSFER;

	/** 乱序传输FIXP批量包数据域尺寸，默认180K。 **/
	private static volatile int defaultPacketContentSize = 180 * 1024;

	/** 乱序传输的FIXP子包数据域尺寸。取最大值，UDP最大包长度减少FIXP包头100字节。 **/
	private static volatile int defaultSubPacketContentSize = ReplyTransfer.MAX_SUBPACKET_CONTENT_SIZE;

	/** 在公网的乱序传输FIXP批量包数据域尺寸，默认10K。 **/
	private static volatile int defaultWidePacketContentSize = 10240;

	/** 在公网的乱序传输的FIXP子包数据域尺寸。是子包最小值。 **/
	private static volatile int defaultWideSubPacketContentSize = ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE;

	/** 流量块，假设有512个进程共同使用 **/
	private static volatile int defaultFlowBlocks = 512;

	/** UDP SOCKET(ReplySucker)读出一个UDP包并且分配给其它线程(ReplyHelper)处理的时间，以微秒计，默认是1微秒，即0.001毫秒 **/
	private static volatile int defaultFlowTimeslice = 1000;

	/**
	 * 判断是合法传输模式
	 * @param who 类型
	 * @return 返回真或者假
	 */
	public static boolean isTransferMode(int who) {
		switch (who) {
		case ReplyTransfer.SERIAL_TRANSFER:
		case ReplyTransfer.PARALLEL_TRANSFER:
			return true;
		}
		return false;
	}

	/**
	 * 翻译传输类型
	 * @param who
	 * @return
	 */
	public static String translateTransferMode(int who) {
		switch (who) {
		case ReplyTransfer.SERIAL_TRANSFER:
			return "SERIAL";
		case ReplyTransfer.PARALLEL_TRANSFER:
			return "PARALLEL";
		}
		return "NONE";
	}

	/**
	 * 设置传输模式
	 * @param who 传输模式
	 * @return 返回更新后的传输模式
	 */
	public static int setDefaultTransferMode(int who) {
		if (ReplyTransfer.isTransferMode(who)) {
			ReplyTransfer.defaultTransferMode = who;
		}
		return ReplyTransfer.defaultTransferMode;
	}

	/**
	 * 返回传输模式
	 * @return 数字
	 */
	public static int getDefaultTransferMode() {
		return ReplyTransfer.defaultTransferMode;
	}


	/**
	 * 判断是并行传输模式
	 * @return 返回真或者假
	 */
	public static boolean isParallelTransfer(int who) {
		return who == ReplyTransfer.PARALLEL_TRANSFER;
	}

	/**
	 * 判断是串行传输模式
	 * @return 返回真或者假
	 */
	public static boolean isSerialTransfer(int who) {
		return who == ReplyTransfer.SERIAL_TRANSFER;
	}

	/**
	 * 判断是串行传输模式
	 * @return 返回真或者假
	 */
	public static boolean isSerialTransfer() {
		return ReplyTransfer.isSerialTransfer(ReplyTransfer.defaultTransferMode);
	}

	/**
	 * 判断是并行传输模式
	 * @return 返回真或者假
	 */
	public static boolean isParallelTransfer() {
		return ReplyTransfer.isParallelTransfer(ReplyTransfer.defaultTransferMode);
	}

	/**
	 * 设置乱序传输FIXP批量包数据域尺寸。
	 * @param len FIXP UDP数据包尺寸
	 * @return 返回FIXP UDP批量包数据域尺寸。
	 */
	public static int setDefaultPacketContentSize(int len) {
		if (len >= ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE) {
			ReplyTransfer.defaultPacketContentSize = len;
		}
		return ReplyTransfer.defaultPacketContentSize;
	}

	/**
	 * 返回FIXP UDP批量包数据域尺寸
	 * 
	 * @return FIXP UDP批量包数据域尺寸
	 */
	public static int getDefaultPacketContentSize() {
		return ReplyTransfer.defaultPacketContentSize;
	}

	/**
	 * 设置公网的乱序传输FIXP批量包数据域尺寸。
	 * @param len FIXP UDP数据包尺寸
	 * @return 返回FIXP UDP批量包数据域尺寸。
	 */
	public static int setDefaultWidePacketContentSize(int len) {
		if (len >= ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE) {
			ReplyTransfer.defaultWidePacketContentSize = len;
		}
		return ReplyTransfer.defaultWidePacketContentSize;
	}

	/**
	 * 返回公网的FIXP UDP批量包数据域尺寸
	 * 
	 * @return FIXP UDP批量包数据域尺寸
	 */
	public static int getDefaultWidePacketContentSize() {
		return ReplyTransfer.defaultWidePacketContentSize;
	}

	/**
	 * 判断是有效的子包尺寸
	 * @param len
	 * @return
	 */
	public static boolean isSubPacketContentSize(int len) {
		return (len > 0 && len <= ReplyTransfer.MAX_SUBPACKET_CONTENT_SIZE);
	}

	/**
	 * 设置乱序传输子包数据域尺寸。用户设置这个参数时，应尽量保证全网一致。<br>
	 * 子包数据域尺寸选择在476 到 65507 - 100字节之间，65507是UDP包最大长度，100字节是FIXP包头余量（除重传的FIXP数据包，一般FIXP包头不会超过100字节，而重传FIXP数据包没有数据域）。
	 * 
	 * @param len 数据包最大长度
	 * @return 返回设置的FIXP UDP子包数据域长度
	 */
	public static int setDefaultSubPacketContentSize(int len) {
		// 在规定范围内!
		if (ReplyTransfer.isSubPacketContentSize(len)) {
			ReplyTransfer.defaultSubPacketContentSize = len;
		}
		return ReplyTransfer.defaultSubPacketContentSize;
	}

	/**
	 * 返回FIXP UDP子包数据域尺寸。系统默认或者用户定义。
	 * 
	 * @return FIXP UDP子包长度
	 */
	public static int getDefaultSubPacketContentSize() {
		return ReplyTransfer.defaultSubPacketContentSize;
	}

	/**
	 * 设置公网的乱序传输子包数据域尺寸。用户设置这个参数时，应尽量保证全网一致。<br>
	 * 子包数据域尺寸选择在476 到 65507 - 100字节之间，65507是UDP包最大长度，100字节是FIXP包头余量（除重传的FIXP数据包，一般FIXP包头不会超过100字节，而重传FIXP数据包没有数据域）。
	 * 
	 * @param len 数据包最大长度
	 * @return 返回设置的FIXP UDP子包数据域长度
	 */
	public static int setDefaultWideSubPacketContentSize(int len) {
		if (ReplyTransfer.isSubPacketContentSize(len)) {
			ReplyTransfer.defaultWideSubPacketContentSize = len;
		}
		return ReplyTransfer.defaultWideSubPacketContentSize;
	}

	/**
	 * 返回公网的FIXP UDP子包数据域尺寸。取UDP允许的最大值。
	 * 
	 * @return FIXP UDP子包长度
	 */
	public static int getDefaultWideSubPacketContentSize() {
		return ReplyTransfer.defaultWideSubPacketContentSize;
	}

	/**
	 * 设置默认的流量块。<br>
	 * 每个流量块对应一个线程，通过流量块，计算每个线程可能占用的SOCKET接收缓存空间。进一步计算线程能够使用的流量参数。
	 * 
	 * @param blocks 流量块
	 * @return 返回新设置值
	 */
	public static void setDefaultFlowBlocks(int blocks) {
		if (blocks > 0) {
			ReplyTransfer.defaultFlowBlocks = blocks;
		}
	}

	/**
	 * 返回默认的流量块
	 * @return 返回对应值
	 */
	public static int getDefaultFlowBlocks() {
		return ReplyTransfer.defaultFlowBlocks;
	}
	
	/**
	 * ReplySucker读出一个UDP包消耗时间，以“毫微秒”时间计，一秒等于100万毫微秒，一毫秒等于1000微秒。
	 * @param mms 微秒计
	 */
	public static void setDefaultFlowTimeslice(int mms) {
		if (mms >= 0) {
			ReplyTransfer.defaultFlowTimeslice = mms;
		}
	}
	
	/**
	 * 返回ReplySucker的UDP SOCKET读一个UDP的包时间，包括从TCP/IP堆栈中读取和分发出去的时间（注意考虑系统运行负载，会有不同的时间值，通常以最大值为准）
	 * @return 微秒计
	 */
	public static int getDefaultFlowTimeslice() {
		return ReplyTransfer.defaultFlowTimeslice;
	}

	/**
	 * 读LINUX SOCKET缓存尺寸
	 * @param key 关键字
	 * @return 返回值
	 */
	private static int readLinuxSocketBuffer(String key) {
		String regex = String.format("^\\s*(?i)(?:%s)\\s*(?:=)\\s*([0-9]+?)\\s*$", key);

		File file = new File("/etc/sysctl.conf");
		boolean success = (file.exists() && file.isFile());
		if (!success) {
			return -1;
		}

		Pattern pattern = Pattern.compile(regex);
		try {
			FileReader disk = new FileReader(file);
			BufferedReader reader = new BufferedReader(disk);
			do {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				Matcher matcher = pattern.matcher(line);
				success = matcher.matches();
				if (success) {
					reader.close();
					disk.close();
					return Integer.parseInt(matcher.group(1));
				}
			} while (true);
			reader.close();
			disk.close();
		} catch (IOException e) {
			Logger.error(e);
		}
		return -1;
	}

	/**
	 * 分配LINUX的公网空间尺寸
	 * @param size 空间尺寸
	 */
	private static void doLinuxWidePacketSize(int size) {
		// 每个FIXP包的空间尺寸
		int headSize = ReplyTransfer.defaultFlowBlocks * ReplyTransfer.PACKET_HEADSIZE; // 包头尺寸
		int capacity = (size - headSize) / ReplyTransfer.defaultFlowBlocks; // 每个线程可使用的SOCKET接收缓存空间
		// 如果FIXP包空间尺寸小于FIXP子包最小尺寸，以最小包为准
		if (capacity < ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE) {
			capacity = ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE;
		}
		// FIXP包尺寸
		ReplyTransfer.setDefaultWidePacketContentSize(capacity);

		// FIXP子包尺寸，默认分成5个
		int subPacketSize = capacity / 5;
		if (subPacketSize < ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE) {
			subPacketSize = ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE;
		}
		ReplyTransfer.setDefaultWideSubPacketContentSize(subPacketSize);
	}

	/**
	 * 分配LINUX内网空间尺寸
	 * @param size
	 */
	private static void doLinuxLocalPacketSize(int size) {
		// 每个FIXP包的空间尺寸
		int headSize = ReplyTransfer.defaultFlowBlocks * ReplyTransfer.PACKET_HEADSIZE;
		int capacity = (size - headSize) / ReplyTransfer.defaultFlowBlocks;
		// 如果FIXP包空间尺寸小于FIXP子包最小尺寸，以最小包为准
		if (capacity < ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE) {
			capacity = ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE;
		}
		// FIXP包尺寸
		ReplyTransfer.setDefaultPacketContentSize(capacity);

		// FIXP子包尺寸，默认分成10个
		int subPacketSize = capacity / 10;
		if (subPacketSize < ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE) {
			subPacketSize = ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE;
		}
		ReplyTransfer.setDefaultSubPacketContentSize(subPacketSize);
	}

	/**
	 * 根据当前计算机SOCKET缓存空间调整FIXP包尺寸
	 */
	public static void doLinuxPacketSize() {
		// 取出SOCKET接收缓存尺寸
		int size = ReplyTransfer.readLinuxSocketBuffer("net.core.rmem_max");

		// 依据SOCKET接收缓存尺寸，分配FIXP包和FIXP子包的尺寸
		if (size > 0) {
			ReplyTransfer.doLinuxLocalPacketSize(size);
			ReplyTransfer.doLinuxWidePacketSize(size);
		}
	}


}