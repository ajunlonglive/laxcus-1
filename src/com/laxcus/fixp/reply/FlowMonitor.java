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
import com.laxcus.log.client.*;
import com.laxcus.thread.*;
import com.laxcus.util.net.*;

/**
 * REPLY异步应答数据记录监视器
 * 通过对传入的记录进行实时智能分析，给出一个合适的流量控制参数。
 * 
 * @author scott.liang
 * @version 1.0 9/5/2020
 * @since laxcus 1.0
 */
public class FlowMonitor extends MutexThread {
	
	/** 空间尺寸 **/
	private final static int CHANNEL_2048K = 1024 * 1024 * 2;

	private final static int CHANNEL_1024K = 1024 * 1024;

	private final static int CHANNEL_512K = 512 * 1024;

	private final static int CHANNEL_256K = 256 * 1024;

	private final static int CHANNEL_128K = 128 * 1024;

	private final static int CHANNEL_10K = 10 * 1024;

	/** 参数实例 **/
	private static FlowMonitor selfHandle = new FlowMonitor();

	/** 接收缓存尺寸，包括一个ReplySucker和任意个MISucker的SOCKET缓存空间 **/
	private int maxBuffer;

	/** 统计ReplySucker和MISucker的SOCKET缓存区个数 **/
	private int maxSockets;

	/** 已经使用的SOCKET缓存尺寸 **/
	private int usingBuffer;

	/** 节点IP地址 -> 数据接收池 **/
	private TreeMap<Address, FlowBasket> baskets = new TreeMap<Address, FlowBasket>();

	/**
	 * 构造默认的异步应答数据记录监视器
	 */
	private FlowMonitor() {
		super();
		maxBuffer = 0;
		maxSockets = 0;
		// 30秒检查一次
		setSleepTime(30);
	}

	/**
	 * 返回异步应答数据记录监视器
	 * @return FlowMonitor实例
	 */
	public static FlowMonitor getInstance() {
		return FlowMonitor.selfHandle;
	}

	/**
	 * 设置ReplySucker的socket接收缓冲区长度，只取三分之一的空间，其他留给ReplySender使用。
	 * @param mi, Massive MI单元
	 * @param socketBuffer SOCKET接收缓冲区尺寸
	 */
	public synchronized boolean addSocketReceiveBufferSize(boolean mi, int socketBuffer) {
		if (socketBuffer <= 0) {
			return false;
		}

		// 如果是MI单元，空间分成2块，否则是ReplySucker，分成3块
		if (mi) {
			maxBuffer += socketBuffer / 2;
		} else {
			maxBuffer += socketBuffer / 3;
		}
		maxSockets++;
		return true;
	}

	/**
	 * 返回SOCKET接收缓冲区尺寸
	 * @return 整数
	 */
	public int getSocketReceiveBufferSize() {
		return maxBuffer;
	}

	/**
	 * 返回SOCKET缓存区个数
	 * @return 整数
	 */
	public int getSocketUnits() {
		return maxSockets;
	}

	/**
	 * 分配缓存空间
	 * @param wide 公网
	 * @param blocks SOCKET信道的FLOW BLOCK
	 * @param buffer SOCKET信道缓存空间
	 * @param using SOCKET信道已经使用的缓存空间
	 * @return 返回一个数据流可占用的SOCKET信道空间
	 */
	private int doChannelCapacity(boolean wide, int blocks, int buffer, int using) {
		int len = 0;
		// 单通道剩余空间尺寸
		final int left = buffer - using;
		// 判断公网/内网
		if (wide) {
			// 在公网，单通道剩余空间先尝试分成20块
			len = left / 20;
			if (len >= FlowMonitor.CHANNEL_10K) {
				len = FlowMonitor.CHANNEL_10K;
			}
			// 在10K以下，使用blocks单位空间
			else {
				len = buffer / blocks; // 按照flow block可用的空间尺寸
				len = (left >= len ? len : 0);
			}
		} else {
			// 在内网之间，单通道剩余空间分成10块
			len = left / 10;
			if (len >= FlowMonitor.CHANNEL_2048K) {
				len = FlowMonitor.CHANNEL_2048K;
			} else if (len >= FlowMonitor.CHANNEL_1024K) {
				len = FlowMonitor.CHANNEL_1024K;
			} else if (len >= FlowMonitor.CHANNEL_512K) {
				len = FlowMonitor.CHANNEL_512K;
			} else if (len >= FlowMonitor.CHANNEL_256K) {
				len = FlowMonitor.CHANNEL_256K;
			} else if (len >= FlowMonitor.CHANNEL_128K) {
				len = FlowMonitor.CHANNEL_128K;
			}
			// 其他情况，按照规定的尺寸分割
			else {
				len = buffer / blocks; // 按照flow block可用的空间尺寸
				len = (left >= len ? len : 0);
			}
		}
		return len;
	}

	/**
	 * 区分公网/内网，给一个数据流量分配SOCKET缓存空间
	 * @return 返回分配的缓存空间
	 */
	private int allocate(boolean wide) {
		// 一个通道的成员数目
		int blocks = ReplyTransfer.getDefaultFlowBlocks() / maxSockets;
		if (ReplyTransfer.getDefaultFlowBlocks() % maxSockets != 0) {
			blocks++;
		}
		// 一个通道的缓存空间尺寸
		int buffer = maxBuffer / maxSockets;
		// 一个通道的剩余空间尺寸
		int using = (usingBuffer < 1 ? 0 : usingBuffer / maxSockets);
		if (usingBuffer > 0 && usingBuffer % maxSockets != 0) {
			using++;
		}

		// 分割空间
		return doChannelCapacity(wide, blocks, buffer, using);
	}

	/**
	 * 增加缓存容量
	 * @param capacity
	 */
	private void add(int capacity) {
		if (capacity > 0) {
			usingBuffer += capacity;
		}
	}

	/**
	 * 减少缓存容量
	 * @param capacity
	 */
	private void subtract(int capacity) {
		if (capacity > 0) {
			usingBuffer -= capacity;
		}
	}


	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into ...");
		while (!isInterrupted()) {
			// 定时检查注入的参数
			check();
			// 延时
			sleep();
		}
		Logger.info(this, "process", "exit ...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 清除!
		baskets.clear();
	}

	/**
	 * 查找成员
	 * @param from
	 * @return
	 */
	private FlowElement findElement(Address from, CastCode code) {
		// 判断已经分配，返回即定UDP流量方案
		FlowBasket basket = baskets.get(from);
		if (basket != null) {
			FlowElement element = basket.find(from, code);
			if (element != null) {
				return element;
			}
		}
		return null;
	}

	/**
	 * 保存成员
	 * @param element
	 */
	private void addElement(FlowElement element) {
		Address from = element.getAddress();
		FlowBasket basket = baskets.get(from);
		if (basket == null) {
			basket = new FlowBasket(from);
			baskets.put(basket.getAddress(), basket);
		}
		basket.add(element);
	}

	/**
	 * 删除有效成员
	 * @param from 来源地址
	 * @param code 异步通信码
	 * @return 返回被删除成员
	 */
	private FlowElement removeElement(Address from, CastCode code) {
		FlowBasket basket = baskets.get(from);
		if (basket != null) {
			FlowElement element = basket.delete(from, code);
			// 空集合，删除！
			if (basket.isEmpty()) {
				baskets.remove(from);
			}
			// 有效，返回！
			if (element != null) {
				return element;
			}
		}
		return null;
	}

	/**
	 * 查找这个地址下面的连接
	 * @param address 地址
	 * @return 返回成员数
	 */
	private int findMembers(Address address) {
		FlowBasket basket = baskets.get(address);
		if (basket != null) {
			return basket.size();
		}
		return 0;
	}

	/**
	 * 建立一个新的UDP流量方案
	 * @param from 来源地址
	 * @return 返回分配的流量方案
	 */
	public FlowSketch create(Address from, CastCode code) {
		boolean wide = ReplyUtil.isWideAddress(from);

		// 锁定保存
		super.lockSingle();
		try {
			// 判断已经分配，返回即定UDP流量方案
			FlowElement element = findElement(from, code);
			if (element != null) {
				return element.getSketch().duplicate();
			}

			// 分配一段SOCKET接收缓存空间
			int capacity = allocate(wide);

			// 统计当前成员
			int sameMembers = findMembers(from);
			element = new FlowElement(from, code);
			// 建立UDP流量方案
			element.createSketch(capacity, sameMembers);
			// 保存成员
			addElement(element);
			// 增加缓存空间
			add(capacity);

			//			Logger.info(this, "create", "从%d单位容量里，分配来自\'%s\'的UDP流量方案：%s",
			//					element.getCapacity(), element.getRemote(), element.getSketch());

			// 返回分配的流量方案
			return element.getSketch().duplicate();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		return null;
	}

	/**
	 * 撤销退出UDP流量方案
	 * @param from 来源地址
	 * @return 成功返回真，否则假
	 */
	public boolean exit(Address from, CastCode code) {
		// 锁定保存
		super.lockSingle();
		try {
			// 判断已经分配，返回即定UDP流量方案
			FlowElement element = removeElement(from, code);
			if (element != null) {
				int capacity = element.getCapacity();
				// 回收SOCKET缓存空间
				subtract(capacity);

				//				Logger.info(this, "exit", "释放来自 %s 的UDP流量方案，缓存尺寸：%d", from, capacity);

				return true;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * 激活流量控制
	 * @param from 来源主机地址
	 * @param code 异步通信码
	 */
	public boolean active(Address from, CastCode code) {
		// 锁定保存
		super.lockSingle();
		try {
			// 分配有资源
			FlowElement element = findElement(from, code);
			if (element != null) {
				element.active();
				return true;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * 保存发生丢包的节点
	 * @param from 来源地址
	 * @param code 异步通信码
	 * @param count 丢包统计数
	 * @return 成功返回真，否则假
	 */
	public boolean lose(Address from, CastCode code, int count) {
		// 锁定
		super.lockSingle();
		try {
			FlowElement element = findElement(from, code);
			// 必须存在，然后才能记录丢失的数据流
			if (element != null) {
				element.lose(count);
				return true;
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * 查找对应的数据发送流方案
	 * @param address 目标地址
	 * @param code 异步通信码
	 * @return 返回对应地址的FlowSketch，没有返回默认值
	 */
	public FlowSketch findSketch(Address from, CastCode code) {
		// 锁定
		super.lockMulti();
		try {
			// 从有效资源池中取
			FlowElement element = findElement(from, code);
			if (element != null) {
				FlowSketch sketch = element.getSketch();
				if (sketch != null) {
					return sketch.duplicate(); // 返回一个副本
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		// 以上不成立，返回空指针
		return null;
	}

	/**
	 * 删除过期成员
	 * @param timeout
	 */
	private void deleteElement(long timeout) {
		// 保存超时成员地址
		ArrayList<FlowFlag> array = new ArrayList<FlowFlag>();

		Iterator<Map.Entry<Address, FlowBasket>> iterator = baskets.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Address, FlowBasket> entry = iterator.next();
			FlowBasket basket = entry.getValue();
			// 查找和保存超时主机地址
			array.addAll(basket.findTimeout(timeout));
		}
		// 删除过期
		for (FlowFlag flag : array) {
			FlowElement element = removeElement(flag.getAddress(), flag.getCode());
			if (element != null) {
				subtract(element.getCapacity());
			}
		}
	}

	/**
	 * 定时检查，删除过期FlowElement
	 */
	private void check() {
		// 以接收失效时间为准，加多10秒，没有激活就删除
		long timeout = ReplyHelper.getDisableTimeout() + 10000; 

		// 单向锁，停止其它
		super.lockSingle();
		try {
			deleteElement(timeout);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}


	//	/**
	//	 * 返回全部成员数
	//	 * @return
	//	 */
	//	private int getElements() {
	//		int count = 0;
	//		Iterator<Map.Entry<Address, FlowBasket>> iterator = baskets.entrySet().iterator();
	//		while (iterator.hasNext()) {
	//			Map.Entry<Address, FlowBasket> entry = iterator.next();
	//			count += entry.getValue().size();
	//		}
	//		return count;
	//	}

	//	/**
	//	 * 根据定义的“Flow Block”尺寸，给一个线程分配可用的容量
	//	 * @return 返回整数值
	//	 */
	//	private int allocate() {
	//		int capacity = maxBuffer / ReplyTransfer.getDefaultFlowBlocks();
	//		// 剩余容量大于指定的分配容量，则返回分配容量，否则是0
	//		int leftBuffer = maxBuffer - usingBuffer;
	//		capacity = (leftBuffer >= capacity ? capacity : 0);
	//		return capacity;
	//	}

	//	/**
	//	 * 设置ReplySucker的socket接收缓冲区长度，只取三分之一的空间，其他留给ReplySender使用。
	//	 * @param socketBuffer SOCKET接收缓冲区尺寸
	//	 */
	//	public synchronized boolean addSocketReceiveBufferSize(int socketBuffer) {
	//		if(socketBuffer <=0) {
	//			return false;
	//		}
	//		if (socketBuffer > 0) {
	//			maxBuffer += socketBuffer / 3;
	//		} else {
	//			maxBuffer += 10240; // 最少要求10K
	//		}
	//		maxSockets++;
	//	}	
}