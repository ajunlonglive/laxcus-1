/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import com.laxcus.fixp.*;
import com.laxcus.util.*;

/**
 * UDP数据流发送方案
 * 
 * @author scott.liang
 * @version 1.0 9/6/2020
 * @since laxcus 1.0
 */
public class FlowSketch implements Cloneable, Comparable<FlowSketch> {

	/** 默认值是100毫秒 **/
	private static final int MAX_SEND_INTERVAL = 100;

	/** 默认值是10毫秒 **/
	private static final int DEFAULT_SEND_INTERVAL = 10;

	/** 最大丢包统计 **/
	private static final int MAX_LOSE_PACKETS = 5;

	/** 单次发送的最大单元数目，默认是100 **/
	private static final int MAX_SEND_UNITS = 100;

	/** FIXP UDP包发送 **/
	private int mode;

	/** 子包一次发送数目 **/
	private int sendUnit;

	/** FIXP UDP子包内容长度 **/
	private int subPacketContentSize;

	/** 发送间隔 **/
	private int sendInterval;

	/** ReplyClient接收ReplyReceiver反馈结果超时时间。单位：毫秒。要大于接收超时时间。所有ReplyReceiver在收包后要向ReplyClient反馈结果。**/
	private int feedbackTimeout;

	/** 丢包统计，默认值是0 **/
	private int lessenCount;

	/**
	 * 构造默认的UDP数据流发送方案
	 */
	public FlowSketch() {
		super();
		lessenCount = 0;
	}

	/**
	 * 生成UDP数据流发送方案副本
	 * @param that UDP数据流发送方案
	 */
	private FlowSketch(FlowSketch that) {
		this();
		mode = that.mode;
		sendUnit = that.sendUnit;
		subPacketContentSize = that.subPacketContentSize;
		sendInterval = that.sendInterval;
		feedbackTimeout = that.feedbackTimeout;
		// 丢包统计
		lessenCount = that.lessenCount;
	}

	/**
	 * 在子包容量基础上，分配剩下的参数
	 * @param capacity 系统分配容量
	 * @param sameMembers 同地址成员数目
	 * @param size 单个子包尺寸
	 */
	private void createLeft(final int capacity, int sameMembers, int size) {
		// 计算当前容量下的发送单元数目
		sendUnit = capacity / size;

		// 修定参数
		if (sendUnit < 1) {
			sendUnit = 1;
		} else if (sendUnit >= FlowSketch.MAX_SEND_UNITS) {
			sendUnit = FlowSketch.MAX_SEND_UNITS;
		}

		// 选择串行/并行，如果系统分配空间大于它的使用空间，选择并行，否则串行
		int length = size * sendUnit;
		mode = (capacity > 0 && capacity >= length ? ReplyTransfer.PARALLEL_TRANSFER : ReplyTransfer.SERIAL_TRANSFER);

		// 如果同源地址达到超过2个，加上自己，那么将有3个，这里要采用串行发送数据包
		if (sameMembers >= 2) {
			mode = ReplyTransfer.SERIAL_TRANSFER;
		}

		// 发送间隔时间，以微秒计
		int timeslice = ReplyTransfer.getDefaultFlowTimeslice(); 
		// 发包延时，单包微秒接收时间 * 发送数目 / 1000 = 延时毫秒 
		if (timeslice > 0) {
			sendInterval = (timeslice * sendUnit) / 1000;
		} else {
			sendInterval = FlowSketch.DEFAULT_SEND_INTERVAL;
		}
		// 最少1毫秒，给ReplySucker读取SOCKET缓存留出时间
		if (sendInterval < 1) {
			sendInterval = 1;
		}

		// 在ReplyReceiver收包超时基础上，增加2秒，是ReplyClient接收ReplyReceiver反馈的超时时间。这是一个基础时间，只能增加不能减少。
		feedbackTimeout = ReplyHelper.getSubPacketTimeout() + 2000;
	}

	/**
	 * 建立公网流量方案
	 * @param capacity
	 * @param sameMembers
	 */
	private void createWide(final int capacity, int sameMembers) {
		// 固定使用FIXP最小UDP包尺寸，防止因为包尺寸超过IP包限制，分发过程出现丢包
		subPacketContentSize = ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE;
		// 一个FIXP子包尺寸
		int size = subPacketContentSize + ReplyTransfer.PACKET_HEADSIZE;

		// 分配剩下的参数
		createLeft(capacity, sameMembers, size);
	}

	/**
	 * 生成内网流量方案
	 * @param capacity
	 * @param sameMembers
	 */
	private void createLocal(final int capacity, int sameMembers) {
		// 容量小于最小子包尺寸，取最小子包容量
		if (capacity < ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE + ReplyTransfer.PACKET_HEADSIZE) {
			subPacketContentSize = ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE;
		} 
		// 容量小于默认子包内容尺寸，取这个容量
		else if (capacity < ReplyTransfer.getDefaultSubPacketContentSize()) {
			subPacketContentSize = capacity;
		}
		// 容量小于整个子包尺寸，去掉包头尺寸，取内容容量
		else if (capacity < ReplyTransfer.getDefaultSubPacketContentSize() + ReplyTransfer.PACKET_HEADSIZE) {
			subPacketContentSize = capacity - ReplyTransfer.PACKET_HEADSIZE;
		} 

		//		// 子包
		//		else if (capacity < ReplyTransfer.getDefaultSubPacketContentSize() + ReplyTransfer.PACKET_HEADSIZE) {
		//			subPacketContentSize = ReplyTransfer.getDefaultSubPacketContentSize() - ReplyTransfer.PACKET_HEADSIZE;
		//		}

		else {
			// 固定使用FIXP最小UDP包尺寸，防止因为包尺寸超过IP包限制，分发过程出现丢包
			subPacketContentSize = ReplyTransfer.getDefaultSubPacketContentSize();
		}

		// 一个FIXP子包尺寸
		int size = subPacketContentSize + ReplyTransfer.PACKET_HEADSIZE;

		// 分配剩下的参数
		createLeft(capacity, sameMembers, size);
	}

	/**
	 * 区分公网/内网，分别建立流量方案
	 * @param wide 公网
	 * @param capacity 被分配的流量
	 * @param sameMembers 同Address地址成员数目
	 */
	public void createDefault(boolean wide, int capacity, int sameMembers) {
		// 判断公网/内网，区别对待!
		if (wide) {
			createWide(capacity, sameMembers);
		} else {
			createLocal(capacity, sameMembers);
		}
	}

	/**
	 * 降低公网的流量/流速
	 */
	private void doWideLessen(int count) {
		if (count < 1) {
			count = 1;
		}

		boolean success = false;
		// 1. 增加发送间隔
		if (sendInterval < FlowSketch.MAX_SEND_INTERVAL) {
			int interval = sendInterval + count;
			interval = (interval > FlowSketch.MAX_SEND_INTERVAL ? FlowSketch.MAX_SEND_INTERVAL : interval);
			sendInterval = interval;
			success = true;
		}
		// 2. 减少发送单元，保证最少一个
		if (sendUnit > 1) { // 保证最少一个单元
			sendUnit -= 1;
			success = true;
		}

		// 以上没有处理，处理并行（并行与上面分开两个调用处理)
		if (!success || count >= FlowSketch.MAX_LOSE_PACKETS) {
			if (ReplyTransfer.isParallelTransfer(mode)) {
				mode = ReplyTransfer.SERIAL_TRANSFER;
			}
		}
	}

	/**
	 * 降低公网的流量/流速
	 */
	private void doLocalLessen(int count) {
		if (count < 1) {
			count = 1;
		}

		boolean success = false;
		// 1. 增加发送间隔
		if (sendInterval < FlowSketch.MAX_SEND_INTERVAL) {
			int interval = sendInterval + count;
			interval = (interval > FlowSketch.MAX_SEND_INTERVAL ? FlowSketch.MAX_SEND_INTERVAL : interval);
			sendInterval = interval;
			success = true;
		}
		// 2. 减少发送单元，保证最少一个
		if (sendUnit > 1) { // 保证最少一个单元
			sendUnit -= 1;
			success = true;
		}
		// 3. 减少子包发送尺寸，与上面分开两次调用正理
		if (!success) {
			if (subPacketContentSize > ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE) {
				int size = subPacketContentSize - 256;
				subPacketContentSize = (size >= ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE ? size : ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE);
				success = true;
			}
		}
		// 以上没有处理，处理并行（并行与上面分开两个调用处理)
		if (!success || count >= FlowSketch.MAX_LOSE_PACKETS) {
			if (ReplyTransfer.isParallelTransfer(mode)) {
				mode = ReplyTransfer.SERIAL_TRANSFER;
			}
		}
	}

	/**
	 * 降低数据流量参数
	 * @param wide 公网
	 * @param count 丢包统计
	 */
	public void lessen(boolean wide, int count) {
		if (wide) {
			doWideLessen(count);
		} else {
			doLocalLessen(count);
		}
		// 增加统计值
		lessenCount++;
	}

	/**
	 * 返回丢包统计值
	 * @return 整数
	 */
	public int getLessenCount() {
		return lessenCount;
	}

	/**
	 * 返回传输模式
	 * @return
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * 返回子包发送单位数
	 * @return 数字
	 */
	public int getSendUnit() {
		return sendUnit;
	}

	/**
	 * 返回一个子包的尺寸
	 * @return
	 */
	public int getSubPacketContentSize() {
		return subPacketContentSize;
	}

	/**
	 * 返回子包间的发送间隔
	 * @return 单位：毫秒
	 */
	public int getSendInterval() {
		return sendInterval;
	}

	/**
	 * 返回接收反馈的接时时间
	 * @return 单位：毫秒
	 */
	public int getFeedbackTimeout() {
		return feedbackTimeout;
	}

	/**
	 * 生成副本
	 * @return
	 */
	public FlowSketch duplicate() {
		return new FlowSketch(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		return compareTo((FlowSketch) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s (%d#%d#%d) %d",
				ReplyTransfer.translateTransferMode(mode), sendUnit,
				subPacketContentSize, sendInterval, feedbackTimeout);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FlowSketch that) {
		if (that == null) {
			return -1;
		}
		// 以下参数要逐一判断
		int ret = Laxkit.compareTo(mode, that.mode);
		if (ret == 0) {
			ret = Laxkit.compareTo(sendUnit, that.sendUnit);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(subPacketContentSize, that.subPacketContentSize);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(sendInterval, that.sendInterval);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(feedbackTimeout, that.feedbackTimeout);
		}
		return ret;
	}

	/**
	 * 向FIXP数据包注入参数。<br>
	 * 解析参数见：ReplyClient.doFlowControl 方法 <br><br>
	 * 
	 * @param packet FIXP数据包
	 */
	public void influx(Packet packet) {
		packet.addMessage(MessageKey.SUBPACKET_TRANSFER_MODE, getMode()); // 子包传输模式
		packet.addMessage(MessageKey.SUBPACKET_SEND_INTERVAL, getSendInterval()); // 子包发送间隔
		packet.addMessage(MessageKey.SUBPACKET_CONTENT_SIZE, getSubPacketContentSize()); // 单个子包内容尺寸
		packet.addMessage(MessageKey.SUBPACKET_UNIT, getSendUnit()); // 子包发送统计
		packet.addMessage(MessageKey.SUBPACKET_FEEDBACK_TIMEOUT, getFeedbackTimeout()); // 子包接收超时
	}

}

//	/**
//	 * 在子包容量基础上，分配剩下的参数
//	 * @param capacity 系统分配容量
//	 * @param members 同地址成员数目
//	 * @param size 单个子包尺寸
//	 */
//	private void createLeft(final int capacity, int members, int size) {
//		// 计算当前容量下的发送单元数目
//		sendUnit = capacity / size;
//
//		// 修定参数
//		if (sendUnit < 1) {
//			sendUnit = 1;
//		} else if (sendUnit >= FlowSketch.MAX_SEND_UNITS) {
//			sendUnit = FlowSketch.MAX_SEND_UNITS;
//		}
//
//		// 选择串行/并行，如果系统分配空间大于它的使用空间，选择并行，否则串行
//		int length = size * sendUnit;
//		mode = (capacity > 0 && capacity >= length ? ReplyTransfer.PARALLEL_TRANSFER : ReplyTransfer.SERIAL_TRANSFER);
//		
//		// 如果同源地址达到超过3个，采用串行处理
//		if (members >= 3) {
//			mode = ReplyTransfer.SERIAL_TRANSFER;
//		}
//
//		// 发送间隔时间，以微秒计
//		int timeslice = ReplyTransfer.getDefaultFlowTimeslice(); 
//		// 如果是串行，选择发送间隔
//		if (mode == ReplyTransfer.SERIAL_TRANSFER) {
//			if (timeslice > 0) {
//				int time = ((members + 1) * timeslice); // +1是当前自己的线程
//				sendInterval = time / 1000; // 转换成毫秒
//				if (time % 1000 != 0) sendInterval += 1;
//				if (sendInterval < 1) sendInterval = 1;
//				else if(sendInterval > FlowSketch.DEFAULT_SEND_INTERVAL) sendInterval = FlowSketch.DEFAULT_SEND_INTERVAL;
//			} else {
//				sendInterval = 1; // 间隔时间1毫秒，给ReplySucker读SOCKET留出时间
//			}
//			
//			// 如果空间是负数，增加延时
//			int left = capacity - size;
//			if (left < 0) {
//				sendInterval += ((0 - left) / 128); // 以128字节为一个单元，增加延时时间
//				if (sendInterval > FlowSketch.DEFAULT_SEND_INTERVAL) sendInterval = FlowSketch.DEFAULT_SEND_INTERVAL;
//			}
//		} else {
//			sendInterval = 1; // 并行发送间隔是1毫秒，给ReplySucker读操作留出时间!
//		}
//
//		// 在ReplyReceiver收包超时基础上，增加2秒，是ReplyClient接收ReplyReceiver反馈的超时时间。这是一个基础时间，只能增加不能减少。
//		feedbackTimeout = ReplyHelper.getSubPacketTimeout() + 2000;
//	}


//	/**
//	 * 生成基于公网环境的数据流方案
//	 */
//	private void createWide(final int capacity, int members) {
//		// 固定使用FIXP最小UDP包尺寸，防止因为包尺寸超过IP包限制，分发过程出现丢包
//		subPacketContentSize = ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE;
//		// 一个FIXP子包尺寸
//		int size = subPacketContentSize + ReplyTransfer.PACKET_HEADSIZE;
//
//		// 计算当前容量下的发送单元数目
//		sendUnit = capacity / size;
//
//		// 修定参数
//		if (sendUnit < 1) {
//			sendUnit = 1; mode = ReplyTransfer.SERIAL_TRANSFER; sendInterval = FlowSketch.DEFAULT_SEND_INTERVAL;
//		} else if (sendUnit > 10) {
//			sendUnit = 10; mode = ReplyTransfer.PARALLEL_TRANSFER; sendInterval = 0;
//		} else if (sendUnit < 5) {
//			mode = ReplyTransfer.SERIAL_TRANSFER; sendInterval = 3;
//		} else {
//			mode = ReplyTransfer.SERIAL_TRANSFER; sendInterval = 1;
//		}
//
//		// 在ReplyReceiver收包超时基础上，增加2秒，是ReplyClient接收ReplyReceiver反馈的超时时间。这是一个基础时间，只能增加不能减少。
//		feedbackTimeout = ReplyHelper.getSubPacketTimeout() + 2000;
//	}
//
//	/**
//	 * 生成基于内网环境的数据流方案
//	 */
//	private void createLocal(final int capacity, int members) {
//		// 固定使用FIXP最小UDP包尺寸，防止因为包尺寸超过IP包限制，分发过程出现丢包
//		subPacketContentSize = ReplyTransfer.getDefaultSubPacketContentSize();
//		// 一个FIXP子包尺寸
//		int size = subPacketContentSize + ReplyTransfer.PACKET_HEADSIZE;
//
//		// 计算当前容量下的发送单元数目
//		sendUnit = capacity / size;
//
//		//		System.out.printf("%d - %d # %d\n", subPacketContentSize, size, sendUnit );
//
//		// 修定参数
//		if (sendUnit < 1) {
//			sendUnit = 1; mode = ReplyTransfer.SERIAL_TRANSFER; sendInterval = FlowSketch.DEFAULT_SEND_INTERVAL;
//		} else if (sendUnit > 10) {
//			sendUnit = 10; mode = ReplyTransfer.PARALLEL_TRANSFER; sendInterval = 0;
//		} else if (sendUnit < 5) {
//			mode = ReplyTransfer.SERIAL_TRANSFER; sendInterval = 3;
//		} else {
//			mode = ReplyTransfer.SERIAL_TRANSFER; sendInterval = 1;
//		}
//
//		// 在ReplyReceiver收包超时基础上，增加2秒，是ReplyClient接收ReplyReceiver反馈的超时时间。这是一个基础时间，只能增加不能减少。
//		feedbackTimeout = ReplyHelper.getSubPacketTimeout() + 2000;
//	}


//	public static void main(String[] args) {
//		FlowSketch e = new FlowSketch();
//		
//		ReplyTransfer.setDefaultSubPacketContentSize( ReplyTransfer.MIN_SUBPACKET_CONTENT_SIZE+653);
//		e.createDefault(false, ReplyTransfer.getDefaultSubPacketContentSize() * 18);
//		
////		e.createDefault(false, 0);
//
//		System.out.println(e.toString() + "\r\n");
//		for (int i = 0; i < 20; i++) {
//			e.lessen(false);
//			System.out.printf("%d - %s\n" , i+1, e.toString());
//		}
//	}

//	public static void main(String[] args) {
//		FlowSketch e = new FlowSketch();
//		int capacity = 1024 * 1024 * 2;
//		for (int sameMembers = 0; sameMembers < 10; sameMembers++) {
//			e.createDefault(false, capacity, sameMembers);
//			System.out.println(e);
//		}
//
//		boolean wide = true;
//		int blocks = 64;
//		int buffer = 1024 * 1024 * 5;
//		int using = 1024 * 1024;
//
//		int len = FlowMonitor.getInstance().doChannelCapacity(wide, blocks, buffer, using);
//		System.out.printf("SOCKET信道缓存空间是：%d - %s\n", len, ConfigParser.splitCapacity(len));
//
//		// 尺寸
//		buffer = 10240;
//		using = 10240 / 2;
//		len = FlowMonitor.getInstance().doChannelCapacity(wide, blocks, buffer, using);
//		System.out.printf("SOCKET信道缓存空间是：%d - %s\n", len, ConfigParser.splitCapacity(len));
//	}
