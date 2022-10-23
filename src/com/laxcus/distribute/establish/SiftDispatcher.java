/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish;

import java.util.*;

import com.laxcus.distribute.establish.session.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * SIFT阶段任务分派器。<br>
 * 保存与BUILD站点的SIFT阶段会话。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public final class SiftDispatcher extends EstablishDispatcher {

	private static final long serialVersionUID = -1691126590853311395L;

	/** "SIFT - SUBSIFT"的链表排列序号，下标从0开始(0是根序号，以后依次增1) **/
	private int iterateIndex;

	/** SIFT阶段会话集合 **/
	private ArrayList<SiftSession> array = new ArrayList<SiftSession>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.EstablishDispatcher#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 写入前缀信息
		super.buildSuffix(writer);
		// 2. 迭代编号
		writer.writeInt(iterateIndex);
		// SESSION数目
		writer.writeInt(array.size());
		for(SiftSession session : array) {
			writer.writeObject(session);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.EstablishDispatcher#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析前面的数据
		super.resolveSuffix(reader);
		// 2. 迭代编号
		iterateIndex = reader.readInt();
		// SESSION解析
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SiftSession session = new SiftSession(reader);
			array.add(session);
		}
	}

	/**
	 * 根据传入的参数，生成它的副本
	 * @param that SiftDispatcher实例
	 */
	private SiftDispatcher(SiftDispatcher that) {
		super(that);
		iterateIndex = that.iterateIndex;
		for (SiftSession session : that.array) {
			addSession((SiftSession) session.clone());
		}
	}

	/**
	 * 建立SIFT阶段资源分派器
	 */
	public SiftDispatcher() {
		super();
		iterateIndex = 0;
	}

	/**
	 * 建立SIFT阶段资源分派器，并且指定阶段命名
	 * @param phase 阶段命名
	 */
	public SiftDispatcher(Phase phase) {
		this();
		setPhase(phase);
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public SiftDispatcher(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 返回当前排列序号(从SIFT根对象开始设置，下标是0)
	 * @return 排列序号
	 */
	public int getIterateIndex() {
		return iterateIndex;
	}

	/**
	 * 设置"SIFT - SUBSIFT"链表成员的排列序号(从SIFT根对象开始设置，下标是0)
	 * @param index 排列序号
	 */
	protected void setIterateIndex(int index) {
		// 设置当前迭代编号
		iterateIndex = index;
		// 给SIFT会话分配编号
		for (SiftSession session : array) {
			session.setIterateIndex(index);
		}
	}
	
	/**
	 * 保存一个SIFT阶段会话对象
	 * @param session SiftSession实例
	 * @return 返回真或者假
	 */
	public boolean addSession(SiftSession session) {
		if (session.getPhase() == null) {
			session.setPhase(getPhase());
		}
		// 设置序列编号
		session.setNumber(array.size());
		session.setIterateIndex(iterateIndex);
		// 保存它
		return array.add(session);
	}

	/**
	 * 返回指定下标位置的SIFT阶段会话对象
	 * @param index 下标
	 * @return SiftSession实例
	 */
	public SiftSession getSession(int index) {
		if (index < 0 || index >= array.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return array.get(index);
	}

	/**
	 * SIFT阶段会话集合数组收缩到实际尺寸
	 */
	public void trim() {
		array.trimToSize();
	}

	/**
	 * 输出SIFT阶段会话连接集合
	 * @return SiftSession列表
	 */
	public List<SiftSession> list() {
		return new ArrayList<SiftSession>(array);
	}

	/**
	 * 判断SIFT阶段会话连接数目是否空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 统计SIFT阶段会话连接数目
	 * @return SIFT阶段会话连接数目
	 */
	public int size() {
		return array.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeObject#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger username) {
		super.setIssuer(username);
		for (SiftSession e : array) {
			e.setIssuer(username);
		}
	}

	/*
	 * 根据当前SIFT阶段分派器，生成一个新的副本
	 * @see com.laxcus.distribute.DistributeObject#duplicate()
	 */
	@Override
	public SiftDispatcher duplicate() {
		return new SiftDispatcher(this);
	}
}