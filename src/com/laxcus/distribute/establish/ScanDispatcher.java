/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish;

import java.util.*;

import com.laxcus.distribute.*;
import com.laxcus.distribute.establish.session.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * SCAN阶段任务分派器。<br>
 * 保存与多个DATA主节点的SCAN阶段会语。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public final class ScanDispatcher extends EstablishDispatcher {

	private static final long serialVersionUID = -2963831270309750953L;

	/** SCAN阶段会话集合 **/
	private ArrayList<ScanSession> array = new ArrayList<ScanSession>();

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
		for(ScanSession session : array) {
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
			ScanSession session = new ScanSession(reader);
			array.add(session);
		}
	}

	/**
	 * 根据传入的参数，生成它的副本
	 * @param that ScanDispatcher实例
	 */
	private ScanDispatcher(ScanDispatcher that) {
		super(that);
		for (ScanSession e : that.array) {
			this.addSession( e.duplicate() );
		}
	}

	/**
	 * 建立SCAN阶段资源分派器
	 */
	public ScanDispatcher() {
		super();
	}

	/**
	 * 建立SCAN阶段资源分派器，并且指定阶段命名
	 * @param phase 阶段命名
	 */
	public ScanDispatcher(Phase phase) {
		this();
		setPhase(phase);
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public ScanDispatcher(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个SCAN阶段会话对象
	 * @param session SCAN阶段会话对象
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSession(ScanSession session) {
		if (session.getPhase() == null) {
			session.setPhase(this.getPhase());
		}
		// 设置序列编号
		session.setNumber(array.size());
		// 保存它
		return array.add(session);
	}

	/**
	 * 返回指定下标位置的SCAN阶段会话对象
	 * @param index 下标
	 * @return SCAN阶段会话对象
	 */
	public ScanSession getSession(int index) {
		if (index < 0 || index >= array.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return array.get(index);
	}

	/**
	 * SCAN阶段会话集合数组收缩到实际尺寸
	 */
	public void trim() {
		this.array.trimToSize();
	}

	/**
	 * 输出SCAN阶段会话连接集合
	 * @return SCAN阶段会话对象集合
	 */
	public List<ScanSession> list() {
		return new ArrayList<ScanSession>(array);
	}

	/**
	 * 判断SCAN阶段会话连接数目是否空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 统计SCAN阶段会话连接数目
	 * @return SCAN阶段会话连接数目
	 */
	public int size() {
		return this.array.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeObject#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger username) {
		super.setIssuer(username);
		for (ScanSession e : array) {
			e.setIssuer(username);
		}
	}

	/*
	 * 根据当前SCAN阶段分派器，生成一个新的副本
	 * @see com.laxcus.distribute.DistributeObject#duplicate()
	 */
	@Override
	public DistributedObject duplicate() {
		return new ScanDispatcher(this);
	}
}