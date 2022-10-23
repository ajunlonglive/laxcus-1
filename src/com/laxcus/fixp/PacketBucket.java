/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * fixp keep packet
 * 
 * @author scott.liang 
 * 
 * @version 1.0 3/13/2009
 * 
 * @see com.laxcus.fixp
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * FIXP UDP子包集。用于KEEP UDP环境。<br>
 * 
 * FIXP子包集是一个FIXP数据包的切割后的结果，包含任意数量的FIXP包。<br>
 * 在这里，每个子包的数据尺寸长度要小于IP包长度。
 * FIXP子包集，是针对大规模、快速、无连接网络环境下的数据发送而设计。
 * 
 * @author scott.liang
 * @version 1.0 9/17/2009
 * @since laxcus 1.0
 */
public final class PacketBucket {

	//	/** 子包最大发送字节数(小于IP包的字节数)，超过将进行拆分 **/
	//	public final static int subPacketSize = 480;

	/** 在重发子包里，子包序号数目，50个。将占用400个字节。 **/
	public final static int RETRY_SERIALS = 50;

	/** 对方通信地址 **/
	private SocketHost remote;

	/** FIXP协议标头 **/
	private Mark mark;

	/** KEEP UDP通信包标识号，全过程中唯一 **/
	private int packetId;

	/** 子包顺序(从0开始) -> 数据包 */
	private TreeMap<Integer, Packet> packets = new TreeMap<Integer, Packet>();

	/** 子包统计数 **/
	private int count;

	/** 子包超时时间 **/
	private long subPacketTimeout;

	/** 失效超时时间 **/
	private long disableTimeout;

	/** 初始化开始启用时间 **/
	private long launchTime;

	/** 保存子包的时间 **/
	private long subPacketTime;

	/** 重传时间 **/
	private long retryTime;

	/**
	 * 构造FIXP子包集实例
	 */
	public PacketBucket() {
		super();
		subPacketTimeout = disableTimeout = 0;
		count = 0;
		launchTime = subPacketTime = System.currentTimeMillis();
	}

	/**
	 * 构造FIXP子包集实例并且保存一个子包
	 * @param sub 子包实例
	 */
	public PacketBucket(Packet sub) {
		this();
		add(sub);
	}

	/**
	 * 返回开始时间。单位：毫秒。
	 * @return 返回开始时间
	 */
	public long getLaunchTime() {
		return launchTime;
	}

	/**
	 * 更新接收子包时间
	 */
	public void refreshSubPacketTime() {
		subPacketTime = System.currentTimeMillis();
	}

	/**
	 * 更新重传时间
	 */
	public void refreshRetryTime() {
		retryTime = System.currentTimeMillis();
	}

	/**
	 * 设置子包集编号
	 * @param who 子包集编号
	 */
	public void setPacketId(int who) {
		packetId = who;
	}

	/**
	 * 返回子包集编号
	 * @return 子包集编号
	 */
	public int getPacketId() {
		return packetId;
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return packets.isEmpty();
	}

	/**
	 * 返回当前子包数目
	 * @return 子包数目的整型值
	 */
	public int size() {
		return packets.size();
	}

	/**
	 * 根据序列号返回对应的包。序列号从0开始。
	 * @param serial 顺序号
	 * @return 返回Packet实例，没有是空指针。
	 */
	public Packet findPacket(int serial) {
		if (serial >= 0 && serial < packets.size()) {
			return packets.get(serial);
		}
		return null;
	}

	/**
	 * 输出全部数据子包
	 * @return 数据子包集
	 */
	public List<Packet> list() {
		return new ArrayList<Packet>(packets.values());
	}

	/**
	 * 判断发生子包断裂。<br>
	 * 发生判断的情况是：最后一个子包编号不等于当前子包数目。
	 * 
	 * @return 返回真或者假
	 */
	public boolean isBreak() {
		int size = packets.size();
		if (size == 0) {
			return false;
		}

		int lastId = packets.lastKey();
		return lastId + 1 != size;
	}

//	/**
//	 * 保存一个子包
//	 * @param sub Packet实例
//	 */
//	public void add(Packet sub) {
//		if(sub == null) {
//			throw new NullPointerException("sub packet is null");
//		}
//		// 取出子包序号（从0开始）
//		Integer serial = sub.findInteger(MessageKey.SUBPACKET_SERIAL);
//		if (serial == null) {
//			throw new IllegalValueException("cannot be find subpacket serial! %s # %s", sub.getMark(), sub.getRemote());
//		}
//		packets.put(serial.intValue(), sub);
//
//		// 子包数目
//		if (count == 0) {
//			count = sub.findInteger(MessageKey.SUBPACKET_COUNT);
//		} else if (count != sub.findInteger(MessageKey.SUBPACKET_COUNT)) {
//			throw new IllegalValueException("illegal sub packet! %d - %d! %s # %s",
//					count, sub.findInteger(MessageKey.SUBPACKET_COUNT), sub.getMark(), sub.getRemote());
//		}
//
//		// 子包超时
//		if (subPacketTimeout == 0) {
//			subPacketTimeout = sub.findInteger(MessageKey.SUBPACKET_TIMEOUT);
//		} else if (subPacketTimeout != sub.findInteger(MessageKey.SUBPACKET_TIMEOUT)) {
//			throw new IllegalValueException("not match subpacket timeout! %s # %s", sub.getMark(), sub.getRemote());
//		}
//
//		// 失效超时
//		if (disableTimeout == 0) {
//			disableTimeout = sub.findInteger(MessageKey.SUBPACKET_DISABLE_TIMEOUT);
//		} else if (disableTimeout != sub.findInteger(MessageKey.SUBPACKET_DISABLE_TIMEOUT)) {
//			throw new IllegalValueException("not match disable timeout! %s # %s", sub.getMark(), sub.getRemote());
//		}
//
//		// 来源地址
//		if (remote == null) {
//			remote = sub.getRemote();
//		} else if (Laxkit.compareTo(remote, sub.getRemote()) != 0) {
//			throw new IllegalValueException("illegal socket address: %s - %s! %s", remote, sub.getRemote(), sub.getMark());
//		}
//
//		if (mark == null) {
//			mark = sub.getMark();
//		} else if (!mark.equals(sub.getMark())) {
//			throw new IllegalArgumentException("illegal fixp command");
//		}
//
//		// 子包编号
//		if (packetId == 0) {
//			packetId = sub.findInteger(MessageKey.PACKET_IDENTIFY);
//		} else if (packetId != sub.findInteger(MessageKey.PACKET_IDENTIFY)) {
//			throw new IllegalArgumentException("illegal packet identity");
//		}
//
//		// 更新保存子包的时间
//		refreshSubPacketTime();
//	}

//	/**
//	 * 保存一个子包
//	 * @param sub Packet实例
//	 */
//	public boolean add(Packet sub) {
//		if(sub == null) {
//			throw new NullPointerException("sub packet is null");
//		}
//		// 取出子包序号（从0开始）
//		Integer serial = sub.findInteger(MessageKey.SUBPACKET_SERIAL);
//		if (serial == null) {
//			Logger.error(this, "add", "cannot be find subpacket serial! %s # %s", sub.getMark(), sub.getRemote());
//			return false;
//		}
//
//		// 子包数目
//		if (count == 0) {
//			count = sub.findInteger(MessageKey.SUBPACKET_COUNT);
//		} else if (count != sub.findInteger(MessageKey.SUBPACKET_COUNT)) {
////			throw new IllegalValueException("illegal sub packet! %d - %d! %s # %s",
////					count, sub.findInteger(MessageKey.SUBPACKET_COUNT), sub.getMark(), sub.getRemote());
//			
//			Logger.error(this, "add", "illegal sub packet! %d - %d! %s # %s",
//					count, sub.findInteger(MessageKey.SUBPACKET_COUNT), sub.getMark(), sub.getRemote());
//			return false;
//		}
//
//		// 子包超时
//		if (subPacketTimeout == 0) {
//			subPacketTimeout = sub.findInteger(MessageKey.SUBPACKET_TIMEOUT);
//		} else if (subPacketTimeout != sub.findInteger(MessageKey.SUBPACKET_TIMEOUT)) {
////			throw new IllegalValueException("not match subpacket timeout! %s # %s", sub.getMark(), sub.getRemote());
//			Logger.error(this, "add", "not match subpacket timeout! %s # %s", sub.getMark(), sub.getRemote());
//			return false;
//		}
//
//		// 失效超时
//		if (disableTimeout == 0) {
//			disableTimeout = sub.findInteger(MessageKey.SUBPACKET_DISABLE_TIMEOUT);
//		} else if (disableTimeout != sub.findInteger(MessageKey.SUBPACKET_DISABLE_TIMEOUT)) {
////			throw new IllegalValueException("not match disable timeout! %s # %s", sub.getMark(), sub.getRemote());
//			Logger.error(this, "add", "not match disable timeout! %s # %s", sub.getMark(), sub.getRemote());
//			return false;
//		}
//
//		// 来源地址
//		if (remote == null) {
//			remote = sub.getRemote();
//		} else if (Laxkit.compareTo(remote, sub.getRemote()) != 0) {
////			throw new IllegalValueException("illegal socket address: %s - %s! %s", remote, sub.getRemote(), sub.getMark());
//			Logger.error(this, "add", "illegal socket address: %s - %s! %s", remote, sub.getRemote(), sub.getMark());
//			return false;
//		}
//
//		if (mark == null) {
//			mark = sub.getMark();
//		} else if (!mark.equals(sub.getMark())) {
//			throw new IllegalArgumentException("illegal fixp command");
//		}
//
//		// 子包编号
//		if (packetId == 0) {
//			packetId = sub.findInteger(MessageKey.PACKET_IDENTIFY);
//		} else if (packetId != sub.findInteger(MessageKey.PACKET_IDENTIFY)) {
//			throw new IllegalArgumentException("illegal packet identity");
//		}
//
//		packets.put(serial.intValue(), sub);
//		// 更新保存子包的时间
//		refreshSubPacketTime();
//		return true;
//	}
	
	/**
	 * 保存一个子包
	 * @param sub Packet实例
	 */
	public boolean add(Packet sub) {
		if (sub == null) {
			throw new NullPointerException("sub packet is null");
		}

		// 判断命令和来源地址一致
		if (mark != null && !mark.equals(sub.getMark())) {
			Logger.error(this, "add", "illegal fixp command! %s != %s", mark, sub.getMark());
			return false;
		}
		if (remote != null && Laxkit.compareTo(remote, sub.getRemote()) != 0) {
			Logger.error(this, "add", "illegal socket address: %s - %s! %s", remote, sub.getRemote(), sub.getMark());
			return false;
		}

		// 取出子包序号（从0开始）
		Integer serial = sub.findInteger(MessageKey.SUBPACKET_SERIAL);
		if (serial == null) {
			Logger.error(this, "add", "cannot be find subpacket serial! %s # %s", sub.getMark(), sub.getRemote());
			return false;
		}
		// 子包数目
		Integer iCount = sub.findInteger(MessageKey.SUBPACKET_COUNT);
		if (iCount == null) {
			Logger.error(this, "add", "cannot be find subpacket count! %s # %s", sub.getMark(), sub.getRemote());
			return false;
		}
		if (count > 0 && count != iCount.intValue()) {
			Logger.error(this, "add", "illegal sub packet! %d - %d! %s # %s", count, iCount.intValue(), sub.getMark(), sub.getRemote());
			return false;
		}

		// 子包超时
		Integer iTimeout = sub.findInteger(MessageKey.SUBPACKET_TIMEOUT);
		if (iTimeout == null) {
			Logger.error(this, "add", "cannot be find subpacket timeout! %s # %s", sub.getMark(), sub.getRemote());
			return false;
		}
		if (subPacketTimeout > 0 && subPacketTimeout != iTimeout.intValue()) {
			Logger.error(this, "add", "not match subpacket timeout! %s # %s", sub.getMark(), sub.getRemote());
			return false;
		}

		// 失效超时
		Integer iDisableTimeout = sub.findInteger(MessageKey.SUBPACKET_DISABLE_TIMEOUT);
		if (iDisableTimeout == null) {
			Logger.error(this, "add", "cannot be find disable timeout! %s # %s", sub.getMark(), sub.getRemote());
			return false;
		}
		if (disableTimeout > 0 && disableTimeout != iDisableTimeout.intValue()) {
			Logger.error(this, "add", "not match disable timeout! %s # %s", sub.getMark(), sub.getRemote());
			return false;
		}

		// 子包编号
		Integer identity = sub.findInteger(MessageKey.PACKET_IDENTIFY);
		if (identity == null) {
			Logger.error(this, "add", "cannot be find packet identity! %s # %s", sub.getMark(), sub.getRemote());
			return false;
		}
		if (packetId > 0 && packetId != identity.intValue()) {
			Logger.error(this, "add", "not match packet identity! %s # %s", sub.getMark(), sub.getRemote());
			return false;
		}

		// 如果是第一次，设置参数
		if (mark == null) {
			remote = sub.getRemote();
			mark = sub.getMark();
			// 参数
			packetId = identity.intValue(); // sub.findInteger(MessageKey.PACKET_IDENTIFY);
			count = iCount.intValue(); // sub.findInteger(MessageKey.SUBPACKET_COUNT);
			disableTimeout = iDisableTimeout.intValue(); //  sub.findInteger(MessageKey.SUBPACKET_DISABLE_TIMEOUT);
			subPacketTimeout = iTimeout.intValue(); // sub.findInteger(MessageKey.SUBPACKET_TIMEOUT);
		}

		// 根据序列号保存
		packets.put(serial.intValue(), sub);
		// 更新保存子包的时间
		refreshSubPacketTime();
		return true;
	}

	/**
	 * 返回对方主机地址
	 * @return SocketHost实例
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/**
	 * 判断达到外部要求的超时时间
	 * @param ms 毫秒极超时时间
	 * @return 返回真或者假
	 */
	public boolean isRetryTimeout(long ms) {
		return System.currentTimeMillis() - retryTime >= ms;
	}

	/**
	 * 判断达到子包超时时间
	 * @return 返回真或者假
	 */
	public boolean isSubPacketTimeout() {
		return subPacketTimeout > 0 && System.currentTimeMillis() - subPacketTime >= subPacketTimeout;
	}

	/**
	 * 判断达到失效超时时间
	 * @return 返回真或者假
	 */
	public boolean isDisableTimeout() {
		return disableTimeout >= 0 && System.currentTimeMillis() - subPacketTime >= disableTimeout;
	}

	/**
	 * 判断已经全部填满
	 * @return 返回真或者假
	 */
	public boolean isFull() {
		return count > 0 && packets.size() == count;
	}

	/**
	 * 从已经接收的子包中，分析出没有收到的子包编号。
	 * 子包编号从0开始
	 * @return 子包集合
	 */
	public List<Integer> getMissSerials() {
		ArrayList<Integer> array = new ArrayList<Integer>();
		for (int index = 0; index < count; index++) {
			// 编号不存在，保存它
			if (!packets.containsKey(index)) {
				array.add(index);
			}
		}
		return array;
	}

	/**
	 * 将子包集合并成一个大的FIXP数据包
	 * @return FIXP数据包实例
	 */
	public Packet compose() {
		// 按照序列号，取出全部数据并且保存
		ClassWriter buff = new ClassWriter();
		for (Packet sub : packets.values()) {
			byte[] b = sub.getData();
			if (!Laxkit.isEmpty(b)) {
				buff.write(b, 0, b.length);
			}
		}

		// 需要被屏蔽的消息类型
		short[] keys = { MessageKey.PACKET_IDENTIFY,
				MessageKey.SUBPACKET_COUNT, MessageKey.SUBPACKET_SERIAL,
				MessageKey.SUBPACKET_TIMEOUT, MessageKey.SUBPACKET_DISABLE_TIMEOUT };

		// 合并后的大的FIXP包
		Packet mass = new Packet(remote, mark);
		// 取出序列号为“0”的子包中的全部消息，保存它们
		Packet sub = findPacket(0);
		mass.addMessages(sub.getMessages());
		// 删除子包消息键
		for (short key : keys) {
			mass.removeMessage(key);
		}

		// 把数据保存到大FIXP包的数据域
		byte[] b = buff.effuse();
		if (!Laxkit.isEmpty(b)) {
			mass.setData(b, 0, b.length);
		}
		// 返回结果
		return mass;
	}
}