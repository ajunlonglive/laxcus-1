/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote.client;

import java.util.*;

import com.laxcus.util.classable.*;

/**
 * 任务应答标记。<br>
 * CALL节点返回的数据结果最前面的标记信息。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/1/2009
 * @since laxcus 1.0
 */
public final class ReturnTag implements Classable {

	/** 行记录总数或者其它单元总数信息 */
	private long items;

	/** 信息的开始或者结束时间 **/
	private long beginTime, endTime;

	/** 数据域的分组数目(每个CALL节点返回的数据为一个分组) */
	private int groups;

	/** 每段的数据长度。每段数据实际对应一台CALL主机的处理结果。在此之下还有更细的针对各DATA主机的记录。 */
	private List<Integer> array = new ArrayList<Integer>();

	/**
	 * 构造默认的返回标记
	 */
	public ReturnTag() {
		super();
		reset();
	}

	/**
	 * 从可类化读取器中解析参数 
	 * @param reader 可类化数据读取器
	 */
	public ReturnTag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 重置参数
	 */
	public void reset() {
		items = 0L;
		beginTime = endTime = 0L;
		groups = 0;
		array.clear();
	}

	/**
	 * 增加单元数
	 * @param i 成员数
	 */
	public void addItem(long i) {
		items += i;
	}

	/**
	 * 返回单元总数
	 * @return 成员数目
	 */
	public long getItems() {
		return items;
	}

	/**
	 * 返回区域成员数
	 * @return 区域成员数
	 */
	public int getGroups(){
		return groups;
	}

	/**
	 * 增加一段区域字节长度
	 * @param len 字节长度
	 * @return 成功返回真，否则假
	 */
	public boolean addGroupSize(int len) {
		return array.add(len);
	}

	/**
	 * 返回区域字节集合
	 * @return int对象数组
	 */
	public List<Integer> getGroupSizes() {
		return array;
	}

	/**
	 * 返回数据区域字节总长度
	 * @return 区域字节总长度
	 */
	public long getSize() {
		long size = 0L;
		for(int len : array) {
			size += len;
		}
		return size;
	}

	/**
	 * 设置计算开始时间
	 * 
	 * @param i 计算开始时间
	 */
	public void setBeginTime(long i) {
		beginTime = i;
	}

	/**
	 * 返回计算开始时间
	 * @return 计算开始时间（长整型）
	 */
	public long getBeginTime() {
		return beginTime;
	}

	/**
	 * 设置计算结束时间
	 * @param i 计算结束时间
	 */
	public void setEndTime(long i) {
		endTime = i;
	}

	/**
	 * 返回计算结束时间
	 * @return 计算结束时间（长整型）
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * 本次计算耗时(单位：毫秒)
	 * 
	 * @return 计算消耗时间（长整型）
	 */
	public long usedTime() {
		return endTime - beginTime;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 参数
		writer.writeLong(items);
		writer.writeLong(beginTime);
		writer.writeLong(endTime);
		// 区域数
		writer.writeInt(groups = array.size());
		// 每段区域的长度
		for (int i = 0; i < array.size(); i++) {
			writer.writeInt(array.get(i).intValue());
		}
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 参数
		items = reader.readLong();
		beginTime = reader.readLong();
		endTime = reader.readLong();

		// 区域统计
		groups = reader.readInt();
		if (scale + groups * 4 > reader.getEnd()) {
			throw new IndexOutOfBoundsException("tag sizeout!");
		}
		// 每段区域字节长度
		for (int i = 0; i < groups; i++) {
			int size = reader.readInt();
			array.add(size);
		}
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 生成应答标记的数据流
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter buff = new ClassWriter(256);
		build(buff);
		return buff.effuse();
	}

	/**
	 * 从数据流中解析应答标记
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @return 解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

}