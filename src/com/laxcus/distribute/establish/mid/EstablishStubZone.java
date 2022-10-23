/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.mid;

import java.util.*;

import com.laxcus.access.stub.index.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块映像段 <br><br>
 * 
 * 建立在EstablishZone基础上，保存数据块元数据。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public abstract class EstablishStubZone extends EstablishZone {

	private static final long serialVersionUID = 3154654760107228193L;

	/** 数据块单元集合 **/
	private TreeMap<Long, StubItem> items = new TreeMap<Long, StubItem>();

	/**
	 * 构造一个默认和私有的数据块映像段
	 */
	protected EstablishStubZone() {
		super();
	}

	/**
	 * 根据传入的数据块映像段参数，生成它的数据副本
	 * @param that 数据块映像段实例
	 */
	protected EstablishStubZone(EstablishStubZone that) {
		super(that);
		items.putAll(that.items);
	}

	/**
	 * 保存一个数据块单元
	 * @param e StubItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addStubItem(StubItem e) {
		Laxkit.nullabled(e);

		return items.put(e.getStub(), e) == null;
	}

	/**
	 * 保存一批数据块单元
	 * @param a 数据块单元集合
	 * @return 返回新增加的数据块统计数
	 */
	public int addStubItems(Collection<StubItem> a) {
		int size = items.size();
		for (StubItem e : a) {
			this.addStubItem(e);
		}
		return items.size() - size;
	}

	/**
	 * 返回数据块单元数组
	 * @return StubItem集合
	 */
	public List<StubItem> getStubItems() {
		return new ArrayList<StubItem>(items.values());
	}

	/**
	 * 返回数据块编号
	 * @return Long集合
	 */
	public List<Long> getStubs() {
		return new ArrayList<Long>(items.keySet());
	}

	/**
	 * 查询数据块元数据
	 * @param stub 数据块编号
	 * @return StubItem实例
	 */
	public StubItem findStubItem(Long stub) {
		return items.get(stub);
	}

	/**
	 * 统计数据块占用的磁盘空间
	 * @return 返回数据块占用的磁盘空间尺寸
	 */
	public long getStubItemCapacity() {
		long size = 0L;
		for (StubItem item : items.values()) {
			size += item.getLength();
		}
		return size;
	}

	/**
	 * 统计数据块成员数目
	 * @return 返回数据块成员数目
	 */
	public int getStubCount() {
		return items.size();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.establish.mid.EstablishField#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 数据块单元
		writer.writeInt(items.size());
		for (StubItem item : items.values()) {
			writer.writeObject(item);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.establish.mid.EstablishField#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 数据块单元
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			StubItem item = new StubItem(reader);
			addStubItem(item);
		}
	}

}