/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub;

import java.io.*;
import java.util.*;

import com.laxcus.tub.turn.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * FIXP协议通信实体。<br>
 * 通信实体是数据流和数据包的父类，按照FIXP协议，提供基本的参数和规则定义。<br>
 * 
 * <pre>
 * 包括有四个部分：
 * 1. FIXP协议标头
 * 2. FIXP消息集合
 * 3. 数据域(字节数组)
 * 4. 对等联接的主机地址(socket)
 * </pre>
 * 
 * @author scott.liang
 * @version 1.1 11/10/2009
 * @since laxcus 1.0
 */
public class TubEntity {

	/** 当前套接字连接类型(TCP or UDP) */
	private byte socketFamily;

	/** 连接方主机地址 **/
	private SocketHost remote;

	/** FIXP协议标头 **/
	protected Mind mind;

	/** FIXP消息集合 **/
	private ArrayList<Slice> slices = new ArrayList<Slice>(5);

	/** FIXP数据域参数 **/
	protected byte[] data;

	/**
	 * 释放对象参数
	 */
	public void destroy() {
		remote = null;
		mind = null;
		// 清空
		slices.clear();
		// 释放
		data = null;
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
	 * 构造一个FIXP协议通信实体，指定网络通信类型。
	 * @param who 套接字通信类型(TCP/UDP)
	 */
	protected TubEntity(byte who) {
		super();
		// 套接字类型必须正确，否则是错误
		if (!SocketTag.isFamily(who)) {
			throw new IllegalValueException("illegal socket family:%d", who);
		}
		socketFamily = who;
	}

	/**
	 * 根据传入的FIXP协议通信实体，生成它的数据副本
	 * @param that TubEntity实例
	 */
	protected TubEntity(TubEntity that) {
		super();
		socketFamily = that.socketFamily;
		remote = that.remote;
		mind = that.mind;
		slices.addAll(that.slices);
		data = that.data;
	}

	/**
	 * 返回实体类型
	 * @return 套接字类型
	 */
	public byte getFamily() {
		return socketFamily;
	}

	/**
	 * 判断是数据包
	 * @return 返回真或者假
	 */
	public boolean isPacket() {
		return SocketTag.isPacket(socketFamily);
	}

	/**
	 * 判断是数据流
	 * @return 返回真或者假
	 */
	public boolean isStream() {
		return SocketTag.isStream(socketFamily);
	}

	/**
	 * 设置连接的主机地址
	 * @param e 目标主机地址
	 */
	public void setRemote(SocketHost e) {
		// 套接字类型不匹配，是错误
		if (e != null && e.getFamily() != socketFamily) {
			throw new IllegalValueException("illegal socket family:%d", e.getFamily());
		}
		// 设置
		remote = e;
	}

	/**
	 * 返回连接的主机地址
	 * @return SocketHost实例
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/**
	 * 设置FIXP协议标头
	 * @param e FIXP协议标头实例
	 */
	public void setMind(Mind e) {
		Laxkit.nullabled(e);

		mind = e;
	}

	/**
	 * 返回FIXP协议标头
	 * @return FIXP协议标头实例
	 */
	public Mind getMind() {
		return mind;
	}

	/**
	 * 重置消息成员数目
	 */
	private void ratch() {
		if (mind != null) {
			mind.setMessages((short) slices.size());
		}
	}

	/**
	 * 设置数据域
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度，如果是负数是空数据
	 */
	public void setData(byte[] b, int off, int len) {
		if (len > 0) {
			data = Arrays.copyOfRange(b, off, off + len);
		} else {
			data = null;
		}
		Slice e = new Slice(SliceKey.CONTENT_LENGTH, (data == null ? 0 : data.length));
		replaceMessage(e);
	}

	/**
	 * 设置数据域
	 * @param b 字节数组
	 */
	public void setData(byte[] b) {
		setData(b, 0, (b == null ? -1 : b.length));
	}

	/**
	 * 设置数据域
	 * 
	 * @param objects 数据域
	 */
	public void setData(Object[] objects) throws IOException {
		byte[] b = TubConstructor.build(objects);
		if (b == null) {
			throw new IllegalArgumentException("invalid objects");
		}
		setData(b, 0, b.length);
		// 序列化对象宿主(目前只限JAVA语言)
		addMessage(new Slice(SliceKey.SERIAL_TYPE, "java"));
		// 数据域的对象成员尺寸
		addMessage(new Slice(SliceKey.SERIAL_OBJECTS, objects.length));
	}

	/**
	 * 返回数据域
	 * 
	 * @return 字节数组
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * 查找匹配消息键的全部消息
	 * 
	 * @param key 消息键编号
	 * @return 消息列表，或者空指针
	 */
	public List<Slice> findMessages(short key) {
		ArrayList<Slice> a = new ArrayList<Slice>();
		for (int i = 0; i < slices.size(); i++) {
			Slice e = slices.get(i);
			if (e.getKey() == key) {
				a.add(e);
			}
		}
		return a;
	}

	/**
	 * 查找一个消息，从同值消息序列的0下标开始查找。消息键见SliceKey定义
	 * 
	 * @param key 消息键编号
	 * @param index 同类消息的顺序下标
	 * @return Slice实例
	 */
	public Slice findMessage(short key, int index) {
		List<Slice> a = findMessages(key);
		// 在范围内
		if (a.size() > 0 && index < a.size()) {
			return a.get(index);
		}
		return null;
	}

	/**
	 * 根据键值查找对应的消息
	 * @param key 消息键编号
	 * @return Slice实例
	 */
	public Slice findMessage(short key) {
		return findMessage(key, 0);
	}

	/**
	 * 查找0下标位置的字节数组，没找到返回NULL
	 * @param key 消息键编号
	 * @return 字节数组
	 */
	public byte[] findRaw(short key) {
		return findRaw(key, 0);
	}

	/**
	 * 查找指定下标位置的字节数组，没找到返回NULL
	 * @param key 消息键编号
	 * @param index 同类消息的顺序下标
	 * @return 字节数组
	 */
	public byte[] findRaw(short key, int index) {
		Slice e = findMessage(key, index);
		if (e == null || !e.isRaw()) {
			return null;
		}
		return e.getRaw();
	}

	/**
	 * 查找0下标位置的布尔值，没找到返回NULL
	 * @param key 消息键编号
	 * @return 布尔对象
	 */
	public Boolean findBoolean(short key) {
		return findBoolean(key, 0);
	}

	/**
	 * 查找指定下标的布尔值，没找到返回NULL
	 * @param key 消息键编号
	 * @param index 同类消息的顺序下标
	 * @return 布尔对象
	 */
	public Boolean findBoolean(short key, int index) {
		Slice e = findMessage(key, index);
		if (e == null || !e.isBoolean()) {
			return null;
		}
		return e.getBoolean();
	}

	/**
	 * 查找0下标位置的字符串，没找到返回NULL
	 * @param key 消息键编号
	 * @return 字符串
	 */
	public String findString(short key) {
		return findString(key, 0);
	}

	/**
	 * 查找指定下标位置的字符串，没找到返回NULL
	 * @param key 消息键编号
	 * @param index 同类消息的顺序下标
	 * @return 字符串
	 */
	public String findString(short key, int index) {
		Slice e = findMessage(key, index);
		if (e == null || !e.isString()) {
			return null;
		}
		return e.getString();
	}

	/**
	 * 查找全部匹配的字符串值
	 * @param key 消息键编号
	 * @return 返回String对象列表，没有是空集合
	 */
	public List<String> findStrings(short key) {
		List<Slice> a = findMessages(key);
		// 判断是整数类型
		ArrayList<String> array = new ArrayList<String>();
		for (int i = 0; i < a.size(); i++) {
			Slice e = a.get(i);
			if (e.isString()) {
				array.add(e.getString());
			}
		}
		return array;
	}

	/**
	 * 查找0下标位置的短整型值，没找到返回NULL
	 * @param key 消息键编号
	 * @return Short对象
	 */
	public Short findShort(short key) {
		return findShort(key, 0);
	}

	/**
	 * 查找指定下标的短整型值，没找到返回NULL
	 * @param key 消息键编号
	 * @param index 同类消息的顺序下标
	 * @return Short对象
	 */
	public Short findShort(short key, int index) {
		Slice e = findMessage(key, index);
		if (e == null || !e.isShort()) {
			return null;
		}
		return e.getShort();
	}

	/**
	 * 查找全部匹配的短整形值
	 * @param key 消息键编号
	 * @return 返回Short对象列表，没有是空集合
	 */
	public List<Short> findShorts(short key) {
		List<Slice> a = findMessages(key);
		// 判断是整数类型
		ArrayList<Short> array = new ArrayList<Short>();
		for (int i = 0; i < a.size(); i++) {
			Slice e = a.get(i);
			if (e.isShort()) {
				array.add(e.getShort());
			}
		}
		return array;
	}

	/**
	 * 查找0下标位置的整型值，没找到返回NULL
	 * @param key 消息键编号
	 * @return Integer对象
	 */
	public Integer findInteger(short key) {
		return findInteger(key, 0);
	}

	/**
	 * 查找指定下标的整型值，没找到返回NULL
	 * @param key 消息键编号
	 * @param index 同类消息的顺序下标
	 * @return Integer对象
	 */
	public Integer findInteger(short key, int index) {
		Slice e = findMessage(key, index);
		if (e == null || !e.isInteger()) {
			return null;
		}
		return e.getInteger();
	}

	/**
	 * 查找全部匹配的整形值
	 * @param key 消息键编号
	 * @return 返回Integer对象列表，没有是空集合
	 */
	public List<Integer> findIntegers(short key) {
		List<Slice> a = findMessages(key);
		// 判断是整数类型
		ArrayList<Integer> array = new ArrayList<Integer>();
		for (int i = 0; i < a.size(); i++) {
			Slice e = a.get(i);
			if (e.isInteger()) {
				array.add(e.getInteger());
			}
		}
		return array;
	}

	/**
	 * 查找0下标位置的长整型值
	 * @param key 消息键编号
	 * @return Long对象
	 */
	public Long findLong(short key) {
		return findLong(key, 0);
	}

	/**
	 * 查找一个指定下标的长整型值，不成功返回NULL
	 * @param key 消息键编号
	 * @param index 同类消息的顺序下标
	 * @return Long对象
	 */
	public Long findLong(short key, int index) {
		Slice e = findMessage(key, index);
		if (e == null || !e.isLong()) {
			return null;
		}
		return e.getLong();
	}

	/**
	 * 查找全部匹配的长整数值
	 * @param key 消息键编号
	 * @return 返回Long对象列表，没有是空集合
	 */
	public List<Long> findLongs(short key) {
		List<Slice> a = findMessages(key);
		// 判断是整数类型
		ArrayList<Long> array = new ArrayList<Long>();
		for (int i = 0; i < a.size(); i++) {
			Slice e = a.get(i);
			if (e.isLong()) {
				array.add(e.getLong());
			}
		}
		return array;
	}

	/**
	 * 查找0下标位置的单浮点值
	 * @param key 消息键编号
	 * @return Float对象
	 */
	public Float findFloat(short key) {
		return findFloat(key, 0);
	}

	/**
	 * 查找一个指定下标的单浮点值，不成功返回NULL
	 * @param key 消息键编号
	 * @param index 同类消息的顺序下标
	 * @return Float对象
	 */
	public Float findFloat(short key, int index) {
		Slice e = findMessage(key, index);
		if (e == null || !e.isFloat()) {
			return null;
		}
		return e.getFloat();
	}

	/**
	 * 查找一个双浮点值
	 * @param key 消息键编号
	 * @return Double对象
	 */
	public Double findDouble(short key) {
		return findDouble(key, 0);
	}

	/**
	 * 查找一个指定下标的双浮点值，不成功返回NULL
	 * @param key 消息键编号
	 * @param index 同类消息的顺序下标
	 * @return Double对象
	 */
	public Double findDouble(short key, int index) {
		Slice e = findMessage(key, 0);
		if (e == null || !e.isDouble()) {
			return null;
		}
		return e.getDouble();
	}

	/**
	 * 增加一项消息。不允许空指针
	 * @param e 消息实例
	 */
	public void addMessage(Slice e) {
		Laxkit.nullabled(e);
		slices.add(e);
		// 重置消息成员数目
		ratch();
	}

	/**
	 * 增加一组消息
	 * @param a 消息集合
	 * @return 返回新增消息数目
	 */
	public int addMessages(Collection<Slice> a) {
		int size = slices.size();
		if (a != null && a.size() > 0) {
			for (Slice e : a) {
				addMessage(e);
			}
		}
		return slices.size() - size;
	}

	/**
	 * 增加一项二进制消息
	 * @param key 消息键编号
	 * @param value 字节数组
	 */
	public void addMessage(short key, byte[] value) {
		addMessage(new Slice(key, value));
	}

	/**
	 * 增加一项可类化类。它将转为字节流保存
	 * @param key 消息键编号
	 * @param e 可类化对象实例
	 */
	public void addMessage(short key, Classable e) {
		ClassWriter writer = new ClassWriter();
		e.build(writer);
		byte[] b = writer.effuse();
		addMessage(key, b);
	}

	/**
	 * 增加一项字符串消息
	 * @param key 消息键编号
	 * @param value 字符串
	 */
	public void addMessage(short key, String value) {
		addMessage(new Slice(key, value));
	}

	/**
	 * 增加一项布尔值消息
	 * @param key 消息键编号
	 * @param value 布尔值
	 */
	public void addMessage(short key, boolean value) {
		addMessage(new Slice(key, value));
	}

	/**
	 * 增加一项短整型消息
	 * @param key 消息键编号
	 * @param value 短整型值
	 */
	public void addMessage(short key, short value) {
		addMessage(new Slice(key, value));
	}

	/**
	 * 增加一项整型消息
	 * @param key 消息键编号
	 * @param value 整型值
	 */
	public void addMessage(short key, int value) {
		addMessage(new Slice(key, value));
	}

	/**
	 * 增加一项长整型消息
	 * @param key 消息键编号
	 * @param value 长整型值
	 */
	public void addMessage(short key, long value) {
		addMessage(new Slice(key, value));
	}

	/**
	 * 增加一项单浮点消息
	 * @param key 消息键编号
	 * @param value 浮点值
	 */
	public void addMessage(short key, float value) {
		addMessage(new Slice(key, value));
	}

	/**
	 * 增加一项双浮点消息
	 * @param key 消息键编号
	 * @param value 双浮点值
	 */
	public void addMessage(short key, double value) {
		addMessage(new Slice(key, value));
	}

	/**
	 * 替换一项消息。先根据消息键删除旧数据，再保存新消息。
	 * @param e 消息实例
	 */
	public void replaceMessage(Slice e) {
		removeMessage(e.getKey());
		slices.add(e);
		// 重置消息成员数目
		ratch();
	}

	/**
	 * 替换一个二进制消息
	 * @param key 消息键编号
	 * @param value 字节数组
	 */
	public void replaceMessage(short key, byte[] value) {
		removeMessage(key);
		addMessage(new Slice(key, value));
	}

	/**
	 * 替换一个字符串消息
	 * @param key 消息键编号
	 * @param value 字符串
	 */
	public void replaceMessage(short key, String value) {
		removeMessage(key);
		addMessage(new Slice(key, value));
	}

	/**
	 * 替换一个布尔值消息
	 * @param key 消息键编号
	 * @param value 布尔值
	 */
	public void replaceMessage(short key, boolean value) {
		removeMessage(key);	
		addMessage(new Slice(key, value));
	}

	/**
	 * 替换一个短整型消息
	 * @param key 消息键编号
	 * @param value 短整型值
	 */
	public void replaceMessage(short key, short value) {
		removeMessage(key);	
		addMessage(new Slice(key, value));
	}

	/**
	 * 替换一个整型消息
	 * @param key 消息键编号
	 * @param value 整型值
	 */
	public void replaceMessage(short key, int value) {
		removeMessage(key);	
		addMessage(new Slice(key, value));
	}

	/**
	 * 替换一个长整型消息
	 * @param key 消息键编号
	 * @param value 长整型值
	 */
	public void replaceMessage(short key, long value) {
		removeMessage(key);	
		addMessage(new Slice(key, value));
	}

	/**
	 * 替换一个单浮点消息
	 * @param key 消息键编号
	 * @param value 单浮点值
	 */
	public void replaceMessage(short key, float value) {
		removeMessage(key);	
		addMessage(new Slice(key, value));
	}

	/**
	 * 替换一个双浮点消息
	 * @param key 消息键编号
	 * @param value 双浮点值
	 */
	public void replaceMessage(short key, double value) {
		removeMessage(key);	
		addMessage(new Slice(key, value));
	}

//	/**
//	 * 根据消息键值删除全部消息
//	 * 
//	 * @param key 消息键编号
//	 * @return 返回删除的消息键数目
//	 */
//	public int removeMessage(short key) {
//		int count = 0;
//		for (int index = 0; index < messages.size(); index++) {
//			Message e = messages.get(index);
//			if (e.getKey() == key) {
//				messages.remove(index);
//				index--;
//				count++;
//			}
//		}
//		// 重置消息成员数目
//		ratch();
//		// 被删除消息数目
//		return count;
//	}

	/**
	 * 根据消息键值删除全部消息
	 * 
	 * @param key 消息键编号
	 * @return 返回删除的消息键数目
	 */
	public int removeMessage(short key) {
		// 1. 判断匹配键，保存消息
		ArrayList<Slice> subs = new ArrayList<Slice>();
		for (int index = 0; index < slices.size(); index++) {
			Slice e = slices.get(index);
			if (e.getKey() == key) {
				subs.add(e);
			}
		}
		// 2. 清除消息
		for (Slice e : subs) {
			slices.remove(e);
		}

		// 重置消息成员数目
		ratch();
		// 被删除消息数目
		return subs.size();
	}
	
	/**
	 * 输出全部消息
	 * 
	 * @return 消息列表
	 */
	public List<Slice> getSlices() {
		return new ArrayList<Slice>(slices);
	}

	/**
	 * 收缩内存到有效范围
	 */
	public void trimMessages() {
		slices.trimToSize();
	}

	/**
	 * 生成FIXP协议的头部，包括标题和消息
	 * 
	 * @param datalen 预定义数据域长度
	 * @return 返回字节数组
	 */
	public byte[] buildHead(int datalen) throws TubProtocolException {
		replaceMessage(new Slice(SliceKey.CONTENT_LENGTH, datalen));
		// 设置消息成员数
		mind.setMessages((short) slices.size());
		// 输出标题到内存
		ClassWriter buff = new ClassWriter(512);
		buff.write(mind.build());
		// 输出消息，输出到内存
		for (Slice e : slices) {
			buff.write(e.build());
		}
		return buff.effuse();
	}

	/**
	 * 生成数据流，包括指定的数据域长度
	 * @param contentSize 预定发送的数据流长度
	 * @return 返回字节数组
	 */
	public byte[] build(int contentSize) throws TubProtocolException {
		if (contentSize < 0) {
			throw new TubProtocolException("content size error %d", contentSize);
		}

		// 设置最新的数据域尺寸(这个尺寸由外部传入，可以是一个提前预定量)
		replaceMessage(SliceKey.CONTENT_LENGTH, contentSize);
		// 设置消息成员数
		mind.setMessages((short) slices.size());
		// 确实实际数据尺寸
		int realen = (data == null ? 0 : data.length);

		// 缓存
		ClassWriter buff = new ClassWriter(128 + realen);

		// 生成标题字节流
		buff.write(mind.build());

		// 消息转为字节数组，输入到缓存
		int count = slices.size();
		for (int i = 0; i < count; i++) {
			Slice e = slices.get(i);
			buff.write(e.build());
		}

		// 写数据域
		if (realen > 0) {
			buff.write(data, 0, realen);
		}
		// 输出结果
		return buff.effuse();
	}

	/**
	 * 生成数据流
	 * @return 字节数组
	 */
	public byte[] build() throws TubProtocolException {
		return build(data == null ? 0 : data.length);
	}

	/**
	 * 返回数据域长度尺寸，未定义返回-1
	 * @return 整型值
	 */
	public int getContentLength() {
		Slice e = findMessage(SliceKey.CONTENT_LENGTH);
		return (e == null ? -1 : e.getInteger());
	}

	/**
	 * 设置数据域成员数
	 * @param items 成员数目
	 */
	public void setContentItems(long items) {
		replaceMessage(new Slice(SliceKey.CONTENT_ITEMS, items));
	}

	/**
	 * 返回内容单元成员数，未定义返回-1
	 * @return 成员数目
	 */
	public long getContentItems() {
		Slice e = findMessage(SliceKey.CONTENT_ITEMS);
		return (e == null ? -1 : e.getLong());
	}
}