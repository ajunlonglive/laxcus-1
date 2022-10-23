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
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 流量成员池
 * 
 * @author scott.liang
 * @version 1.0 9/18/2020
 * @since laxcus 1.0
 */
final class FlowBasket {
	
	/** 节点地址 **/
	private Address address;

	/** 主机套接字 -> 流量控制成员 **/
	private TreeMap<FlowFlag, FlowElement> elements = new TreeMap<FlowFlag, FlowElement>();

	/**
	 * 构造流量成员池，指定主机地址
	 * @param address 主机地址
	 */
	public FlowBasket(Address address) {
		super();
		setAddress(address);
	}

	/**
	 * 设置节点地址，不允许空指针
	 * @param e 节点地址
	 */
	public void setAddress(Address e) {
		Laxkit.nullabled(e);
		address = e.duplicate();
	}

	/**
	 * 返回节点地址
	 * @return Address实例
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * 保存成员
	 * @param e
	 */
	public void add(FlowElement e) {
		elements.put(e.getFlag(), e);
	}
	
	/**
	 * 统计发送单元
	 * @return
	 */
	public int getSendUnits() {
		int sendUnits = 0;
		Iterator<Map.Entry<FlowFlag, FlowElement>> iterator = elements.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<FlowFlag, FlowElement> entry = iterator.next();
			FlowElement element = entry.getValue();
			sendUnits += element.getSketch().getSendUnit();
		}
		return sendUnits;
	}

	/**
	 * 查找超时的主机
	 * @param timeout
	 * @return 超时主机
	 */
	public List<FlowFlag> findTimeout(long timeout) {
		ArrayList<FlowFlag> array = new ArrayList<FlowFlag>();
		
		Iterator<Map.Entry<FlowFlag, FlowElement>> iterator = elements.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<FlowFlag, FlowElement> entry = iterator.next();
			FlowElement element = entry.getValue();
			// 判断超时，保存地址
			if (element.isTimeout(timeout)) {
				array.add(element.getFlag());
			}
		}
		return array;
	}
	
	/**
	 * 弹出第一个成员
	 * @return 返回FlowElement实例，或者空指针
	 */
	public FlowElement popup() {
		// 取出第一个成员
		FlowFlag from = elements.firstKey();
		if (from == null) {
			return null;
		}
		// 从队列删除
		return elements.remove(from);
	}
	
	/**
	 * 查找成员
	 * @param flag 异步通信码
	 * @return 返回FlowElement实例，没有是空指针
	 */
	public FlowElement find(FlowFlag flag) {
		return elements.get(flag);
	}

	/**
	 * 查找成员
	 * @param address
	 * @param code
	 * @return
	 */
	public FlowElement find(Address address, CastCode code) {
		return find(new FlowFlag(address, code));
	}

	/**
	 * 删除成员
	 * @param flag 异步通信码
	 * @return 返回FlowElement实例，或者空指针
	 */
	public FlowElement delete(FlowFlag flag) {
		return elements.remove(flag);
	}
	
	/**
	 * 删除成员
	 * @param address
	 * @param code
	 * @return
	 */
	public FlowElement delete(Address address, CastCode code) {
		return delete(new FlowFlag(address, code));
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	/**
	 * 成员数
	 * @return
	 */
	public int size() {
		return elements.size();
	}

}