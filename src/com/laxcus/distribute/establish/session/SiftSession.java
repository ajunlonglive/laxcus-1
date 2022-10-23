/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.session;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.distribute.establish.mid.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;
import com.laxcus.site.Node;

/**
 * ESTABLISH.SIFT阶段会话。<br><br>
 * 
 * ESTABLISH.SIFT阶段会话在CALL.ASSIGN阶段生成，通过网络传输到BUILD站点执行。<br>
 * 
 * @author scott.liang
 * @version 1.1 6/23/2012
 * @since laxcus 1.0
 */
public final class SiftSession extends EstablishSession {

	private static final long serialVersionUID = -6735822996498301966L;

	/** SIFT会话对象的迭代编号，默认是-1，无迭代。如果有迭代，编号从0开始，依次加1。*/
	private int iterateIndex;
	
	/** 数据块分布区域，数据源在DATA节点**/
	private Map<EstablishFlag, SiftHead> fields = new TreeMap<EstablishFlag, SiftHead>();

	/**
	 * 构造默认和私有的“SIFT”阶段会话
	 */
	private SiftSession() {
		super(PhaseTag.SIFT);
	}

	/**
	 * 根据传入的SIFT阶段会话实例，生成它的浅层数据副本
	 * @param that SiftSession实例
	 */
	private SiftSession(SiftSession that) {
		super(that);
		// 设置会话迭代序号
		iterateIndex = that.iterateIndex;

		fields.putAll(that.fields);
	}

	/**
	 * 构造“ESTABLISH.SIFT”阶段会话，指定阶段命名
	 * @param phase SIFT阶段命名
	 */
	public SiftSession(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 构造ESTABLISH.SIFT阶段会话，设置阶段命名和BUILD站点地址
	 * @param phase SIFT阶段命名
	 * @param endpoint BUILD站点地址
	 */
	public SiftSession(Phase phase, Node endpoint) {
		this(phase);
		setRemote(endpoint);
	}

	/**
	 * 从可类化读取器中解析“SIFT”阶段会话参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SiftSession(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeSession#setRemote(com.laxcus.site.Node)
	 */
	@Override
	public void setRemote(Node e) {
		// 必须是BUILD站点地址
		if (!e.isBuild()) {
			throw new IllegalValueException("illegal site:%s", e);
		}
		super.setRemote(e);
	}
	
	/**
	 * 输出全部数据表名
	 * @return Space集合
	 */
	public Set<Space> getSpaces() {
		TreeSet<Space> a = new TreeSet<Space>();
		for (SiftHead e : fields.values()) {
			a.add(e.getFlag().getSpace());
		}
		return a;
	}

	/**
	 * 设置当前会话的迭代编号
	 * @param i 迭代编号
	 */
	public void setIterateIndex(int i) {
		iterateIndex = i;
	}

	/**
	 * 返回当前会话的迭代编号
	 * @return 迭代编号
	 */
	public int getIterateIndex() {
		return iterateIndex;
	}

	/**
	 * 设置SIFT阶段的下载数据区。由ASSIGN阶段分配，在SIFT阶段下载时使用。
	 * @param e SiftHead实例
	 */
	public boolean add(SiftHead e) {
		Laxkit.nullabled(e);

		EstablishFlag flag = e.getFlag();
		return this.fields.put(flag, e) == null;
	}

	/**
	 * 输出SIFT阶段的下载数据区键值
	 * @return EstablishFlag集合
	 */
	public Set<EstablishFlag> getKeys() {
		return new TreeSet<EstablishFlag>(fields.keySet());
	}

	/**
	 * 查找SIFT阶段下载数据区
	 * @param key EstablishFlag实例
	 * @return 返回SiftHead实例
	 */
	public SiftHead find(EstablishFlag key) {
		return fields.get(key);
	}

	/**
	 * 输出全部SIFT阶段下载数据区
	 * @return SiftHead列表
	 */
	public List<SiftHead> list() {
		return new ArrayList<SiftHead>(fields.values());
	}

	/**
	 * 统计成员数目
	 * @return 成员数目
	 */
	public int size() {
		return fields.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.TaskObject#duplicate()
	 */
	@Override
	public SiftSession duplicate() {
		return new SiftSession(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.StepSession#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 前缀
		super.buildSuffix(writer);
		// 迭代编号
		writer.writeInt(iterateIndex);
		// 数据块分布区
		writer.writeInt(fields.size());
		for (SiftHead e : fields.values()) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.StepSession#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 前缀
		super.resolveSuffix(reader);
		// 迭代编号
		iterateIndex = reader.readInt();
		// 数据分布区
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SiftHead head = new SiftHead(reader);
			fields.put(head.getFlag(), head);
		}
	}

}