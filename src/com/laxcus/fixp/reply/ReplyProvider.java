/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import java.util.*;

import com.laxcus.echo.*;
import com.laxcus.fixp.*;
import com.laxcus.log.client.*;
import com.laxcus.security.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 异步数据提供者。
 * 做为ReplySender/ReplyClient的基础类存在，提供它们需要的公共参数和方法。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2018
 * @since laxcus 1.0
 */
public class ReplyProvider extends ReplyWatcher {

	/** 序列号生成器 **/
	static SerialGenerator generator = new SerialGenerator();

	/** FIXP包分组发送前的单元 **/
	private int packetSize = ReplyTransfer.getDefaultPacketContentSize();

	/** FIXP子包单元尺寸 **/
	private int subPacketSize;

	/** 发送模式 **/
	protected int mode;

	/** 单次发送FIXP子包数目 **/
	protected int sendUnit;

	/** FIXP子包发送间隔 **/
	protected int sendInterval;

	/** 子包发送后等待返回结果的超时时间，默认是10秒 */
	protected int feedbackTimeout;

	/**
	 * 构造默认的异步数据提供者
	 */
	protected ReplyProvider() {
		super();
		// 包尺寸
		packetSize = ReplyTransfer.getDefaultPacketContentSize();
		// 子包尺寸
		subPacketSize = ReplyTransfer.getDefaultSubPacketContentSize();

		// 设置默认的传输模式
		setMode(ReplyTransfer.SERIAL_TRANSFER);
		// 单次发送数目统计
		setSendUnit(1);
		// 发送间隔时间，默认5毫秒
		setSendInterval(5);
		// 设置接收子包超时
		setFeedbackTimeout(ReplyWorker.getSubPacketTimeout());
	}

	/**
	 * 设置传输模式
	 * @param who
	 */
	public void setMode(int who) {
		if (ReplyTransfer.isTransferMode(who)) {
			mode = who;
		}
	}

	/**
	 * 设置发送单元数目
	 * @param len
	 */
	public boolean setSendUnit(int len) {
		if (len > 0 && len < 0xFFFF) {
			sendUnit = len;
			return true;
		}
		Logger.error(this, "setSendUnix", "illegal param:%d", len);
		return false;
	}

	/**
	 * 设置子包超时时间，单位：毫秒(子包超时时间小于包超时时间)
	 * @param ms 子超超时时间
	 */
	public void setFeedbackTimeout(int ms) {
		if (ms >= 1000) {
			feedbackTimeout = ms;
		}
	}

	/**
	 * 设置FIXP子包之间的发送间隔，以毫秒为单位。通过迟延发送，避免接收端压力过大，减少丢包概率
	 * @param ms 以毫秒为单位的时间
	 */
	public void setSendInterval(int ms) {
		if (ms >= 0) {
			sendInterval = ms;
		}
	}

	/**
	 * 返回 FIXP子包之间的发送间隔，以毫秒为单位。通过迟延发送，避免接收端压力过大，减少丢包概率
	 * @return 以毫秒为单位的时间
	 */
	public int getSendInterval() {
		return sendInterval;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.fixp.reply.ReplyWatcher#setToken(com.laxcus.echo.CastToken)
	 */
	@Override
	public void setToken(CastToken e) {
		super.setToken(e);
		SocketHost host = e.getListener();
		// 判断是公网，选择公网的传输流量
		boolean wide = ReplyUtil.isWideAddress(host.getAddress());
		if (wide) {
			packetSize = ReplyTransfer.getDefaultWidePacketContentSize();
			subPacketSize = ReplyTransfer.getDefaultWideSubPacketContentSize();
		}
	}

	/**
	 * 设置FIXP UDP数据包的传输单元长度。
	 * 在传输的时候，数据将拆分成多个小包传输。
	 * 
	 * @param n 数据包长度
	 */
	public void setPacketSize(int n) {
		if (n >= 1024) packetSize = n;
	}

	/**
	 * 返回一个大FIXP UDP数据包的传输单元。
	 * @return
	 */
	public int getPacketSize() {
		return packetSize;
	}

	/**
	 * 返回默认的子包尺寸
	 * @return 子包尺寸
	 */
	public int getSubPacketSize() {
		return subPacketSize;
	}

	/**
	 * 设置默认的子包尺寸
	 * @param len 指定长度
	 * @return 设置成功返回真，否则假
	 */
	public int setSubPacketSize(int len) {
		boolean success = (len > 0 && len < ReplyTransfer.MAX_SUBPACKET_CONTENT_SIZE);
		if (success) {
			subPacketSize = len;
		}
		return subPacketSize;
	}

	/**
	 * 将数据分成N个子包
	 * @param data 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @param unit 子包数据长度
	 * @return 返回子包集合
	 * @throws SecureException
	 */
	protected List<Packet> split(byte[] data, int off, int len, int unit) throws SecureException {		
		ArrayList<Packet> array = new ArrayList<Packet>();
		// 按照最大传输单元，切割成N个子包
		int end = off + len;
		for (int seek = off; seek < end;) {
			int size = Laxkit.limit(seek, end, unit);
			byte[] b = Arrays.copyOfRange(data, seek, seek + size);
			seek += size;
			// 生成快速投递数据包
			Packet packet = new Packet(Ask.NOTIFY, Ask.CAST);

			// 加密
			if (hasCipher()) {
				b = getCipher().encrypt(b);
			}

			// EACH签名
			long each = Laxkit.doEach(b);
			packet.addMessage(MessageKey.EACH_KEY, each);

			// 保存数据
			packet.setData(b);
			array.add(packet);
		}
		// 设置参数
		int count = array.size();
		for (int index = 0; index < count; index++) {
			Packet packet = array.get(index);
			packet.setRemote(getToken().getListener());
			packet.addMessage(MessageKey.CAST_CODE, getCastCode());
			packet.addMessage(MessageKey.SUBPACKET_COUNT, count);
			packet.addMessage(MessageKey.SUBPACKET_SERIAL, index); // 子包序列号，从0开始
			packet.addMessage(MessageKey.PACKET_IDENTIFY, packetId);
		}
		return array;
	}

	//	/**
	//	 * 将数据分成N个子包，单元使用默认长度
	//	 * @param data 字节数组
	//	 * @param off 下标
	//	 * @param len 有效长度
	//	 * @return 返回子包集合
	//	 * @throws SecureException
	//	 */
	//	protected List<Packet> split(byte[] data, int off, int len) throws SecureException {
	//		int unit = ReplyTransfer.getDefaultSubPacketSize();
	//		return split(data, off, len, unit);
	//	}

	/**
	 * 取出子包编号，子包编号从0开始。
	 * 
	 * @param packet 数据包
	 * @param packetCount 当前包统计数目s
	 * @return 数组包列表
	 */
	protected List<Integer> getSerials(Packet packet, int packetCount) {
		ArrayList<Integer> array = new ArrayList<Integer>();

		for (int index = 0; true; index++) {
			Integer serial = packet.findInteger(MessageKey.SUBPACKET_SERIAL, index);
			// 全部完成，退出
			if (serial == null) {
				break;
			}
			array.add(serial);
		}

		// 如果是空集合，是要求整个重传
		if (array.isEmpty()) {
			for (int index = 0; index < packetCount; index++) {
				array.add(index);
			}
		}

		// 返回数组
		return array;
	}

}