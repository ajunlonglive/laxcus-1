/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import java.util.*;

import com.laxcus.fixp.*;
import com.laxcus.util.classable.*;

/**
 * FIXP UDP乱序子包集。<br>
 * 
 * CastReceiver使用，接收来自ReplyClient/ReplySender的FIXP UDP子包集合。
 * 按照子包序号进行编组，在数据接收完成后，做数据输出。
 * 
 * FIXP UDP子包应小于IP包长度，这样就避免中继设备的拆分和重组。
 * 是针对大规模、无连接、快速传输而设计。典型案例是LAXCUS数据块。
 * 
 * @author scott.liang
 * @version 1.0 01/17/2018
 * @since laxcus 1.0
 */
public class PacketBasket {
	
	/** 数据块统计数目 **/
	private int count;

	/** 子包序号（从0开始）- 子包实例 **/
	private TreeMap<Integer, Packet> packets = new TreeMap<Integer, Packet>();
	
	/**
	 * 销毁
	 */
	protected void destroy() {
		packets.clear();
		count = -1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {
		destroy();
	}

	/**
	 * 构造默认的FIXP UDP乱序子包集
	 */
	public PacketBasket() {
		super();
		count = -1; // 初始化为负数
	}
	
	/**
	 * 清除记录。两个操作：
	 * 1. 数据块统计数目设置为-1
	 * 2. 内存的子包清除
	 */
	public void clear() {
		count = -1;
		packets.clear();
	}

	/**
	 * 判断已经完成
	 * @return 返回真或者假
	 */
	public boolean isFull() {
		return count > 0 && count == packets.size();
	}
	
	/**
	 * 本次要传输的子包总数
	 * @return 子包总数
	 */
	public int count() {
		return count;
	}
	
	/**
	 * 当前保存的子包数目
	 * @return 返回数值
	 */
	public int size(){
		return packets.size();
	}
	
	/**
	 * 输出子包编号，升序排序
	 * @return
	 */
	public Set<Integer> keys() {
		return new TreeSet<Integer>(packets.keySet());
	}
	
	/**
	 * 根据编号找到一个子包
	 * @param packetId 数据子包编号
	 * @return 关联的数据子包
	 */
	public Packet find(int packetId) {
		return packets.get(packetId);
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

//	/**
//	 * 保存一个FIXP UDP乱序子包
//	 * @param sub FIXP UDP子包
//	 * @return 保存成功返回真，否则假
//	 */
//	public boolean add(Packet sub) {
//		// 取出子包统计数目
//		if (count == -1) {
//			count = sub.findInteger(MessageKey.SUBPACKET_COUNT);
//		}
//		// 取出子包序号
//		int index = sub.findInteger(MessageKey.SUBPACKET_SERIAL);
//		// 判断不存在，保存它
//		boolean success = (!packets.containsKey(index));
//		if (success) {
//			packets.put(index, sub);
//		}
//		return success;
//	}

	/**
	 * 保存一个FIXP UDP乱序子包
	 * @param sub FIXP UDP子包
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(Packet sub) {
		Integer id = sub.findInteger(MessageKey.SUBPACKET_COUNT);
		if (id == null) {
			return false;
		}

		// 取出子包统计数目
		if (count == -1) {
			count = id.intValue();
		} 
		// 不一致，忽略它！
		else if (count != id.intValue()) {
			return false;
		}
		// 取出子包序号
		id = sub.findInteger(MessageKey.SUBPACKET_SERIAL);
		if (id == null) {
			return false;
		}
		int index = id.intValue();
		// 只有不存在，才保存它
		boolean success = (!packets.containsKey(index));
		if (success) {
			packets.put(index, sub);
		}
		return success;
	}

	/**
	 * 将子包数据逐一读取全部输出
	 * @return 字节数组
	 */
	public byte[] flush() {
		int length = 0;
		// 统计长度
		Iterator<Map.Entry<Integer, Packet>> iterator = packets.entrySet().iterator();
		while (iterator.hasNext()) {
			Packet packet = iterator.next().getValue();
			length += packet.getContentLength();
		}

		// 把数据放入缓存
		ClassWriter buf = new ClassWriter(length);
		iterator = packets.entrySet().iterator();
		while (iterator.hasNext()) {
			Packet packet = iterator.next().getValue();
			byte[] b = packet.getData();
			buf.write(b);
		}

		//		System.out.printf("RECEIVER data size:%d\n", buf.size());

		return buf.effuse();
	}

	/**
	 * 收集没有找到的数据块编号
	 * @return 返回没有收到的子包编号集合
	 */
	public List<Integer> getMissSerials() {
		ArrayList<Integer> array = new ArrayList<Integer>();
		// 从已经收到的包中找到不存在的编号，写入集合包中。
		for (int index = 0; index < count; index++) {
			// 子包编号不存在，编号写入
			if (!packets.containsKey(index)) {
				array.add(index);
			}
		}
		return array;
	}
	
	/**
	 * 判断子包发生断裂。<br>
	 * 发生判断的情况是：最后一个子包编号不等于当前子包数目。
	 * 
	 * @return 返回真或者假。
	 */
	public boolean isBreak() {
		int size = packets.size();
		// 空值，无断裂！
		if (size == 0) {
			return false;
		}

		// 最后一个键编号+1，不等于当前尺寸时，即存在断裂！
		int lastId = packets.lastKey();
		return lastId + 1 != size;
	}

}