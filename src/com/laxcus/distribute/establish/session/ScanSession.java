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
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;
import com.laxcus.site.Node;

/**
 * ESTABLISH.SCAN阶段会话。<br><br>
 * 
 * ESTABLISH.SCAN阶段会话在CALL.ISSUE阶段生成，通过网络传输至DATA节点执行。<br>
 * 
 * @author scott.liang
 * @version 1.1 6/23/2012
 * @since laxcus 1.0
 */
public final class ScanSession extends EstablishSession {

	private static final long serialVersionUID = 4270703921609427435L;

	/** 待扫描的数据表空间集合 **/
	private TreeSet<ScanMember> array = new TreeSet<ScanMember>();

	/**
	 * 根据传入的“SCAN”阶段会话实例，生成它的数据副本
	 * @param that ScanSession实例
	 */
	private ScanSession(ScanSession that) {
		super(that);
		// 全部保存
		array.addAll(that.array);
	}

	/**
	 * 构造默认和私有的“SCAN”阶段会话
	 */
	private ScanSession() {
		super(PhaseTag.SCAN);
	}

	/**
	 * 构造“ESTABLISH.SCAN”阶段会话，指定阶段命名
	 * @param phase SCAN阶段命名
	 */
	public ScanSession(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 构造“ESTABLISH.SCAN”阶段会话，指定阶段命名和DATA站点地址
	 * @param phase SCAN阶段会话
	 * @param endpoint 目标站点地址
	 */
	public ScanSession(Phase phase, Node endpoint) {
		this(phase);
		setRemote(endpoint);
	}

	/**
	 * 从可类化读取器中解析“ESTABLISH.SCAN”阶段会话参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanSession(ClassReader reader) {
		this();
		this.resolve(reader);
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
	 * 保存一个被扫描成员
	 * @param e ScanMember实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addMember(ScanMember e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 输出被扫描成员集合
	 * @return ScanMember列表
	 */
	public List<ScanMember> getMembers() {
		return new ArrayList<ScanMember>(array);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeSession#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 前缀
		super.buildSuffix(writer);
		// 被扫描成员集合
		writer.writeInt(array.size());
		for (ScanMember member : array) {
			writer.writeObject(member);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeSession#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 前缀
		super.resolveSuffix(reader);
		// 被扫描成员集合
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			ScanMember member = new ScanMember(reader);
			array.add(member);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.TaskObject#duplicate()
	 */
	@Override
	public ScanSession duplicate() {
		return new ScanSession(this);
	}

}