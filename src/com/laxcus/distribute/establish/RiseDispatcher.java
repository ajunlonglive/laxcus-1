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
 * RISE阶段任务分派器。<br>
 * 保存与DATA主节点的RISE阶段会语。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public final class RiseDispatcher extends EstablishDispatcher {

	private static final long serialVersionUID = 5299292143065746409L;

	/** RISE阶段会话集合 **/
	private ArrayList<RiseSession> array = new ArrayList<RiseSession>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.EstablishDispatcher#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 写入前缀信息
		super.buildSuffix(writer);
		// SESSION数目
		writer.writeInt(array.size());
		for(RiseSession session : array) {
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
		// SESSION解析
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RiseSession session = new RiseSession(reader);
			array.add(session);
		}
	}

	/**
	 * 根据传入的参数，生成它的副本
	 * @param that RiseDispatcher实例
	 */
	private RiseDispatcher(RiseDispatcher that) {
		super(that);
		for (RiseSession e : that.array) {
			addSession(e.duplicate());
		}
	}

	/**
	 * 建立RISE阶段资源分派器
	 */
	public RiseDispatcher() {
		super();
	}

	/**
	 * 建立RISE阶段资源分派器，并且指定阶段命名
	 * @param phase 阶段命名
	 */
	public RiseDispatcher(Phase phase) {
		this();
		setPhase(phase);
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader
	 */
	public RiseDispatcher(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个RISE阶段会话对象
	 * @param session RiseSession实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSession(RiseSession session) {
		if (session.getPhase() == null) {
			session.setPhase(getPhase());
		}
		// 设置序列编号
		session.setNumber(array.size());
		// 保存它
		return array.add(session);
	}
	
	/**
	 * 保存一批RISE阶段会话对象
	 * @param a RiseSession数组
	 * @return 返回新增会话数目
	 */
	public int addSessions(Collection<RiseSession> a) {
		int size = array.size();
		for (RiseSession e : a) {
			addSession(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回指定下标位置的RISE阶段会话对象
	 * @param index 指定下标位置
	 * @return RiseSession实例对象
	 */
	public RiseSession getSession(int index) {
		if (index < 0 || index >= array.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return array.get(index);
	}

	/**
	 * RISE阶段会话集合数组收缩到实际尺寸
	 */
	public void trim() {
		array.trimToSize();
	}

	/**
	 * 输出RISE阶段会话连接集合
	 * @return RiseSession列表
	 */
	public List<RiseSession> list() {
		return new ArrayList<RiseSession>(array);
	}

	/**
	 * 判断RISE阶段会话连接数目是否空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 统计RISE阶段会话连接数目
	 * @return RISE阶段会话连接数目
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
		for (RiseSession e : array) {
			e.setIssuer(username);
		}
	}

	/*
	 * 根据当前RISE阶段分派器，生成一个新的副本
	 * @see com.laxcus.distribute.DistributeObject#duplicate()
	 */
	@Override
	public RiseDispatcher duplicate() {
		return new RiseDispatcher(this);
	}
}