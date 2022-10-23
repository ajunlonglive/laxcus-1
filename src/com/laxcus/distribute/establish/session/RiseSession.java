/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.session;

import java.util.*;

import com.laxcus.distribute.establish.mid.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * ESTABLISH.RISE阶段会话。<br><br>
 * 
 * ESTABLISH.RISE阶段会话在CALL.ASSIGN阶段生成，通过网络传输至DATA节点（主/从节点）执行。它将执行从指定的BUILD站点下载数据块，和覆盖本地旧的数据块的工作。<br>
 * 
 * @author scott.liang
 * @version 1.1 6/23/2012
 * @since laxcus 1.0
 */
public final class RiseSession extends EstablishSession {

	private static final long serialVersionUID = -872873364035850259L;

	/** 数据块分布区域，数据源在BUILD节点**/
	private TreeMap<EstablishFlag, RiseNewHead> updates = new TreeMap<EstablishFlag, RiseNewHead>();

	/** 在目标DATA站点上，除了要求更新的数据块，还有被删除的数据块。通常这些数据块的内容，已经包含在更新的数据块中 **/
	private TreeMap<EstablishFlag, RiseOldHead> deletes = new TreeMap<EstablishFlag, RiseOldHead>();

	/**
	 * 根据传入的ESTABLISH.RISE阶段会话实例，生成它的浅层数据副本
	 * @param that Rise阶段会话
	 */
	private RiseSession(RiseSession that) {
		super(that);
		updates.putAll(that.updates);
		deletes.putAll(that.deletes);
	}

	/**
	 * 构造默认和私有的ESTABLISH.RISE阶段会话
	 */
	private RiseSession() {
		super(PhaseTag.RISE);
	}

	/**
	 * 构造ESTABLISH.RISE阶段会话，指定阶段命名
	 * @param phase 阶段命名（必须是RISE阶段类型)
	 * @throws IllegalPhaseException 
	 */
	public RiseSession(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 构造“ESTABLISH.RISE”阶段会话，指定阶段命名和DATA站点地址
	 * @param phase RISE阶段会话
	 * @param endpoint 目标站点地址
	 */
	public RiseSession(Phase phase, Node endpoint) {
		this(phase);
		setRemote(endpoint);
	}

	/**
	 * 从可类化数据读取器中解析ESTABLISH.RISE阶段会话
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RiseSession(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeSession#setRemote(com.laxcus.site.Node)
	 */
	@Override
	public void setRemote(Node e) {
		// 必须是DATA站点
		if(!e.isData()) {
			throw new IllegalValueException("illegal site:%s", e);
		}
		super.setRemote(e);
	}

	/**
	 * 设置RISE阶段的下载数据区。由ASSIGN阶段分配，在RISE阶段下载时使用。
	 * @param head RiseNewHead实例
	 */
	public boolean addUpdateHead(RiseNewHead head) {
		Laxkit.nullabled(head);
		
		return updates.put(head.getFlag(), head) == null;
	}

	/**
	 * 返回全部RISE更新标识
	 * @return EstablishFlag列表
	 */
	public List<EstablishFlag> getUpdateFlags() {
		return new ArrayList<EstablishFlag>(updates.keySet());
	}

	/**
	 * 查找关联的RISE阶段更新域
	 * @param flag EstablishFlag实例
	 * @return RiseNewHead实例
	 */
	public RiseNewHead findUpdateHead(EstablishFlag flag) {
		return updates.get(flag);
	}

	/**
	 * 返回全部RISE阶段下载数据区
	 * @return RiseNewHead列表
	 */
	public List<RiseNewHead> getUpdateHeads() {
		return new ArrayList<RiseNewHead>(updates.values());
	}
	
	/**
	 * 设置RISE阶段的被删除数据域。由ASSIGN阶段分配，在RISE阶段下载时使用。
	 * @param head RiseOldHead实例
	 */
	public boolean addDeleteHead(RiseOldHead head) {
		Laxkit.nullabled(head);
		
		return this.deletes.put(head.getFlag(), head) == null;
	}

	/**
	 * 返回全部RISE删除标识
	 * @return EstablishFlag列表
	 */
	public List<EstablishFlag> getDeleteFlags() {
		return new ArrayList<EstablishFlag>(deletes.keySet());
	}

	/**
	 * 查询RISE阶段删除域
	 * @param flag EstablishFlag实例
	 * @return RiseOldHead实例
	 */
	public RiseOldHead findDeleteHead(EstablishFlag flag) {
		return deletes.get(flag);
	}

	/**
	 * 返回全部RISE阶段被删除数据域
	 * @return RiseOldHead列表
	 */
	public List<RiseOldHead> getDeleteHeads() {
		return new ArrayList<RiseOldHead>(deletes.values());
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeObject#duplicate()
	 */
	@Override
	public RiseSession duplicate() {
		return new RiseSession(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.StepSession#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 前缀
		super.buildSuffix(writer);
		// 被更新的数据块
		writer.writeInt(updates.size());
		for (RiseNewHead head : updates.values()) {
			writer.writeObject(head);
		}
		// 要求删除的数据块
		writer.writeInt(deletes.size());
		for (RiseOldHead head : deletes.values()) {
			writer.writeObject(head);
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
		// 被更新的数据块
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RiseNewHead head = new RiseNewHead(reader);
			updates.put(head.getFlag(), head);
		}
		// 被删除的数据块
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RiseOldHead head = new RiseOldHead(reader);
			deletes.put(head.getFlag(), head);
		}
	}

}