/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.index;

import java.io.*;
import java.util.*;

import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块元数据 <br>
 * 元数据信息包括：<br>
 * <1> 数据块编号(64位长整型，由TOP节点分配，全局唯一)<br>
 * <2> 数据块长度(通常是64M，级限环境可达2G，但是不允许超过)。<br>
 * <3> 数据块级别(主块或者从块，PrimeChunk or SlaveChunk)<br>
 * <4> 数据块状态(缓存块/存储块，已经封闭或者未封闭) <br>
 * <5> 当前块的索引集合
 * 
 * @author scott.liang
 * @version 1.2 8/12/2015
 * @since laxcus 1.0
 */
public final class StubItem implements Classable, Serializable, Cloneable, Comparable<StubItem> {

	private static final long serialVersionUID = -4310874406125613575L;

	/** 数据块编号 */
	private long stub;

	/** 数据块长度 **/
	private long length;

	/** 数据块级别(主块或者从块) */
	private byte rank;

	/** 数据块状态(未封闭或者封闭) */
	private byte status;

	/** 列编号 -> 数据块索引 */
	private Map<java.lang.Short, StubIndex> mapIndex = new TreeMap<java.lang.Short, StubIndex>();

	/**
	 * 将数据块元数据写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 数据块编号
		writer.writeLong(stub);
		// 数据块长度
		writer.writeLong(length);
		// 级别(主/从)
		writer.write(rank);
		// 状态
		writer.write(status);
		// 索引记录
		writer.writeInt(mapIndex.size());
		// 写入每一个索引
		for(StubIndex e : mapIndex.values()) {
			writer.writeObject(e);
		}
		// 返回写入的字节数
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器解析数据块元数据
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 数据块编号
		stub = reader.readLong();
		// 数据块长度
		length = reader.readLong();
		// 级别
		rank = reader.read();
		// 状态
		status = reader.read();
		// 索引数目
		int size = reader.readInt();
		// 解析索引
		for (int i = 0; i < size; i++) {
			byte family = reader.current();
			StubIndex index = StubIndexCreator.create(family);
			// 解析索引
			index.resolve(reader);
			// 保存
			mapIndex.put(index.getColumnId(), index);
		}
		// 返回解析的尺寸
		return reader.getSeek() - scale;
	}

	/**
	 * 根据传入的数据块元数据，生成一个它的副本
	 * @param that 数据块元数据实例
	 */
	private StubItem(StubItem that) {
		this();
		stub = that.stub;
		length = that.length;
		rank = that.rank;
		status = that.status;
		mapIndex.putAll(that.mapIndex);
	}

	/**
	 * 建立一个默认的数据块元数据
	 */
	protected StubItem() {
		super();
		stub = 0L; // 初始定义为无效状态:0
		length = 0L;
		rank = 0;
		status = 0;
	}

	/**
	 * 构造数据块元数据，并且指定它的数据块编号
	 * @param stub  数据块编号
	 */
	public StubItem(long stub) {
		this();
		setStub(stub);
	}

	/**
	 * 构造数据块元数据，指定数据块编号和长度
	 * @param stub  数据块编号
	 * @param length  数据块长度
	 */
	public StubItem(long stub, long length) {
		this(stub);
		setLength(length);
	}

	/**
	 * 构造数据块元数据，并且指定它的数据块编号、数据块长度、数据块级别、数据块状态
	 * @param stub  数据块编号
	 * @param length  文件尺寸
	 * @param rank  级别
	 * @param status  状态
	 */
	public StubItem(long stub, long length, byte rank, byte status) {
		this();
		setStub(stub);
		setLength(length);
		setRank(rank);
		setStatus(status);
	}

	/**
	 * 从可类化数据读取器中解析数据
	 * @param reader  可类化读取器
	 * @since 1.2
	 */
	public StubItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据块编号
	 * @param id 数据块编号
	 */
	public void setStub(long id) {
		stub = id;
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public long getStub() {
		return stub;
	}

	/**
	 * 设置数据块长度
	 * @param len 长整型
	 */
	public void setLength(long len) {
		length = len;
	}

	/**
	 * 返回数据块长度
	 * @return 长整型
	 */
	public long getLength() {
		return length;
	}

	/**
	 * 设置数据块级别(主块/从块)
	 * @param who 数据块级别
	 */
	public void setRank(byte who) {
		if (!ChunkRank.isFamily(who)) {
			throw new IllegalValueException("illegal chunk rank:%d", who);
		}
		rank = who;
	}

	/**
	 * 返回数据块级别
	 * @return 数据块级别的字节描述
	 */
	public byte getRank() {
		return rank;
	}

	/**
	 * 判断是主块
	 * @return 返回真或者假
	 */
	public boolean isPrime() {
		return ChunkRank.isPrimeChunk(rank);
	}

	/**
	 * 判断是从块
	 * @return 返回真或者假
	 */
	public boolean isSlave() {
		return ChunkRank.isSlaveChunk(rank);
	}

	/**
	 * 设置数据块当前状态(完成/未完成)
	 * @param who 数据块状态
	 */
	public void setStatus(byte who) {
		if(!MassStatus.isFamily(who)) {
			throw new IllegalValueException("illegal status:%d", who);
		}
		status = who;
	}

	/**
	 * 返回数据块当前状态
	 * @return 数据块状态的字节描述
	 */
	public byte getStatus() {
		return status;
	}

	/**
	 * 判断是存储块状态
	 * @return  返回真或者假
	 */
	public boolean isChunk() {
		return MassStatus.isChunk(status);
	}

	/**
	 * 判断是缓存块状态
	 * @return  返回真或者假
	 */
	public boolean isCache() {
		return MassStatus.isCache(status);
	}

	/**
	 * 保存一个数据块索引
	 * @param index  索引
	 * @return  成功返回真，否则假
	 */
	public boolean add(StubIndex index) {
		short columnId = index.getColumnId();
		// 一个数据块的某个列只能有一个索引。如果列编号已经存在，不允许更新
		if (mapIndex.containsKey(columnId)) {
			return false;
		}
		return mapIndex.put(columnId, index) == null;
	}

	/**
	 * 返回当前数据块索引
	 * @return java.util.List<StubIndex>
	 */
	public List<StubIndex> list() {
		return new ArrayList<StubIndex>(mapIndex.values());
	}

	/**
	 * 返回当前索引列集合
	 * @return java.util.Set<short>
	 */
	public Set<Short> keys() {
		return new TreeSet<Short>(mapIndex.keySet());
	}

	/**
	 * 根据列编号，返回一个数据块索引
	 * @param columnId
	 * @return  StubIndex子类实例
	 */
	public StubIndex find(short columnId) {
		return mapIndex.get(columnId);
	}

	/**
	 * 清除全部数据块索引
	 */
	public void clear() {
		mapIndex.clear();
	}

	/**
	 * 判断数据块索引是否空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return mapIndex.isEmpty();
	}

	/**
	 * 统计当前数据块索引尺寸
	 * @return 数据块索引的整型值
	 */
	public int size() {
		return mapIndex.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%x_%d_%d_%d", stub, length, rank, status);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != StubItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((StubItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (stub >>> 32 ^ stub);
	}

	/**
	 * 根据当前数据块元数据，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new StubItem(this);
	}

	/**
	 * 根据数据块编号，比较它们的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StubItem that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(stub, that.stub);
	}

}