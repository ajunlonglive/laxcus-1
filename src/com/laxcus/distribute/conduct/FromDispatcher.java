/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.conduct;

import java.util.*;

import com.laxcus.access.index.section.*;
import com.laxcus.distribute.conduct.session.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * DIFFUSE/CONVERGE分布计算的FROM阶段连接任务分派器。<br>
 * 保存面向多个DATA节点的FROM阶段会语。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public final class FromDispatcher extends ConductDispatcher {

	private static final long serialVersionUID = -8616074319231790941L;

	/** FROM阶段会话集合 **/
	private ArrayList<FromSession> array = new ArrayList<FromSession>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.ConductDispatcher#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 写入前缀信息
		super.buildSuffix(writer);
		// session数目
		writer.writeInt(array.size());
		for(FromSession session : array) {
			writer.writeObject(session);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.ConductDispatcher#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析前面的数据
		super.resolveSuffix(reader);
		// session解析
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			FromSession session = new FromSession(reader);
			array.add(session);
		}
	}

	/**
	 * 根据传入的参数，生成它的副本
	 * @param that FromDispatcher实例
	 */
	private FromDispatcher(FromDispatcher that) {
		super(that);
		for (FromSession session : that.array) {
			addSession(session.duplicate());
		}
	}

	/**
	 * 建立FROM阶段资源分派器
	 */
	public FromDispatcher() {
		super();
	}

	/**
	 * 建立FROM阶段资源分派器，并且指定阶段命名
	 * @param phase 阶段命名
	 */
	public FromDispatcher(Phase phase) {
		this();
		super.setPhase(phase);
	}
	
	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 */
	public FromDispatcher(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个FROM阶段会话对象
	 * @param session FromSession实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSession(FromSession session) {
		if (session.getPhase() == null) {
			session.setPhase(getPhase());
		}
		// 设置序列编号
		session.setNumber(array.size());
		// 保存它
		return array.add(session);
	}

	/**
	 * 返回指定下标位置的FROM阶段会话对象
	 * @param index 下标位置
	 * @return FromSession实例
	 */
	public FromSession getSession(int index) {
		if (index < 0 || index >= array.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return array.get(index);
	}

	/**
	 * FROM阶段会话集合数组收缩到实际尺寸
	 */
	public void trim() {
		array.trimToSize();
	}

	/**
	 * 输出全部FROM阶段会话
	 * @return 返回FromSession集合
	 */
	public List<FromSession> list() {
		return  new ArrayList<FromSession>(array);
	}

	/**
	 * 判断FROM阶段会话连接数目是否空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 统计FROM阶段会话连接数目
	 * @return FROM阶段会话连接数目
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
		// 上级设置
		super.setIssuer(username);
		// 设置会话签名
		for (FromSession session : array) {
			session.setIssuer(username);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.ConductDispatcher#setIndexSector(com.laxcus.access.index.section.IndexSector)
	 */
	public void setIndexSector(ColumnSector e) {
		// 设置上级索引分区
		super.setIndexSector(e);
		// 给会话设置索引分区
		for (FromSession session : array) {
			session.setIndexSector(e);
		}
	}

	/*
	 * 根据当前FROM阶段分派器，生成一个新的副本
	 * @see com.laxcus.distribute.DistributeObject#duplicate()
	 */
	@Override
	public FromDispatcher duplicate() {
		return new FromDispatcher(this);
	}
}