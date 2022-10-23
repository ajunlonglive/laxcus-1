/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.data;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * DATA站点成员。<br><br>
 * 
 * @author scott.liang
 * @version 1.1 6/12/2015
 * @since laxcus 1.0
 */
public final class DataMember extends PhaseMember {

	private static final long serialVersionUID = -3300205030290578056L;

	/** 数据表名 -> 数据块区域 **/
	private Map<Space, StubReflex> reflexes = new TreeMap<Space, StubReflex>();

	/**
	 * 根据传入DATA站点成员参数，生成它的数据副本
	 * @param that DataMember实例
	 */
	private DataMember(DataMember that) {
		super(that);
		reflexes.putAll(that.reflexes);
	}

	/**
	 * 构造默认的DATA站点成员
	 */
	private DataMember() {
		super();
	}

	/**
	 * 构造DATA站点成员，指定数据持有人
	 * @param username 持有人
	 */
	public DataMember(Siger username) {
		this();
		setSiger(username);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DataMember(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存数据块空间域
	 * @param e StubReflex实例
	 * @return 返回真或者假
	 */
	public boolean addStubReflex(StubReflex e) {
		Laxkit.nullabled(e);

		reflexes.put(e.getSpace(), e);
		return true;
	}

	/**
	 * 返回数据表名集合
	 * @return Space列表
	 */
	public List<Space> getSpaces() {
		return new ArrayList<Space>(reflexes.keySet());
	}

	/**
	 * 返回数据块区域映像集合
	 * @return StubReflex列表
	 */
	public List<StubReflex> getStubReflexes() {
		return new ArrayList<StubReflex>(reflexes.values());
	}

	/**
	 * 根据表名查找数据块区域
	 * @param space 数据表名
	 * @return 返回StubReflex实例，没有返回空指针
	 */
	public StubReflex findStubReflex(Space space) {
		if (space != null) {
			return reflexes.get(space);
		}
		return null;
	}

	/**
	 * 根据表名，取它的磁盘数据总尺寸
	 * @param space 数据表名
	 * @return 全部数据块总长度
	 */
	public long findDiskCapacity(Space space) {
		StubReflex reflex = findStubReflex(space);
		if (reflex == null) {
			return 0L;
		}
		return reflex.getDiskCapacity();
	}

	/**
	 * 根据表名，取它的内存数据总尺寸（数据块索引元数据）
	 * @param space 数据表名
	 * @return 全部数据索引总长度
	 */
	public long findMemoryCapacity(Space space) {
		StubReflex reflex = findStubReflex(space);
		if (reflex == null) {
			return 0L;
		}
		return reflex.getMemoryCapacity();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.JobMember#reset()
	 */
	@Override
	public void reset() {
		// 释放当前参数
		reflexes.clear();
		// 释放上级参数
		super.reset();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.SiteMember#duplicate()
	 */
	@Override
	public DataMember duplicate() {
		return new DataMember(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.site.JobMember#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 数据块区域
		writer.writeInt(reflexes.size());
		for (StubReflex e : reflexes.values()) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.site.JobMember#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 数据块区域
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			StubReflex e = new StubReflex(reader);
			reflexes.put(e.getSpace(), e);
		}
	}

}