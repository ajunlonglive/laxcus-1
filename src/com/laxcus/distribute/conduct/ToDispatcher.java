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
 * TO阶段任务分派器，保存多个面向不同WORK节点的TO阶段会话实例。 <br>
 * 同时配合对"CONVERGE"阶段的定义，TO分派器是是一个链表(或者称级连)的关系，从首链开始，依次连接。
 * 
 * @author scott.liang
 * @version 1.1 7/26/2015
 * @since laxcus 1.0
 */
public final class ToDispatcher extends ConductDispatcher {

	private static final long serialVersionUID = 3618073978502160459L;

	/** "TO - SUBTO"的链表排列序号，下标从0开始(0是根序号，以后依次增1) **/
	private int iterateIndex;

	/** TO阶段的分布计算会话集合，可以是FROM/TO两种会话实例中的任何一种，但是不能混合并存 **/
	private ArrayList<ConductSession> array = new ArrayList<ConductSession>();

	/**
	 * 将TO阶段任务分派器参数写入可类化存储器
	 * @see com.laxcus.distribute.conduct.ConductDispatcher#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 1. 前缀
		super.buildSuffix(writer);
		// 2. 迭代编号
		writer.writeInt(iterateIndex);
		// 3. 会话(以带类名方式写入)
		writer.writeInt(array.size());
		for (int i = 0; i < array.size(); i++) {
			writer.writeDefault(array.get(i));
		}
	}

	/*
	 * 从可类化读取器中解析TO阶段任务分派器
	 * @see com.laxcus.distribute.conduct.ConductDispatcher#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 1. 前缀
		super.resolveSuffix(reader);
		// 2. 迭代编号
		iterateIndex = reader.readInt();
		// 3. 会话 (带类名解析)
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			ConductSession session = (ConductSession) reader.readDefault();
			array.add(session);
		}
	}

	/**
	 * 根据传入的TO阶段分派器，生成一个它的副本
	 * @param that ToDispatcher实例
	 */
	private ToDispatcher(ToDispatcher that) {
		super(that);
		setIterateIndex(that.iterateIndex);		
		for (ConductSession task : that.array) {
			array.add((ConductSession) task.clone());
		}
		if (array.size() > 0) {
			trim();
		}
	}

	/**
	 * 构造一个默认的TO阶段任务分派器
	 */
	public ToDispatcher() {
		super();
		iterateIndex = 0;
	}

	/**
	 * 构造TO阶段任务分派器，并且指定它的阶段命名
	 * @param phase 阶段命名
	 */
	public ToDispatcher(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 从可类化读取器中解析TO阶段任务分派器
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public ToDispatcher(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 判断当前存储的是否全部是FROM会话
	 * @return 返回真或者假
	 */
	public boolean isFullFromSession() {
		int count = 0;
		for (ConductSession that : array) {
			if (that.isFrom()) count++;
		}
		return count > 0 && count == array.size();
	}

	/**
	 * 判断当前是否全部是TO会话
	 * @return 返回真或者假
	 */
	public boolean isFullToSession() {
		int count = 0;
		for (ConductSession that : array) {
			if (that.isTo()) count++;
		}
		return count > 0 && count == array.size();
	}

	/**
	 * 保存一个任务会话，前提是集合中的会话必须全部一致，否则弹出异常
	 * @param session 任务会话实例
	 * @throws ClassCastException
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSession(ConductSession session) {
		for(ConductSession that : array) {
			if(that.getFamily() != session.getFamily()) {
				throw new ClassCastException();
			}
		}
		// 定义命名
		if (session.getPhase() == null) {
			session.setPhase(getPhase());
		}
		// 设置迭代编号
		session.setIterateIndex(iterateIndex);
		// 设置序列编号
		session.setNumber(array.size());
		// 保存
		return array.add(session);
	}

	/**
	 * 返回指定下标位置的ToSession
	 * @param index 下标
	 * @return ToSession实例
	 */
	public ToSession getToSession(int index) {
		if (!isFullToSession()) {
			throw new ClassCastException("not a to-session");
		}
		if (index < 0 || index >= array.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return (ToSession) array.get(index);
	}

	/**
	 * 返回指定下标位置的FromSession
	 * @param index 下标
	 * @return FromSession实例
	 */
	public FromSession getFromSession(int index) {
		if (!isFullFromSession()) {
			throw new ClassCastException("not a from-session");
		}
		if (index < 0 || index >= array.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return (FromSession) array.get(index);
	}

	/**
	 * 输出全部FROM会话集合
	 * @return FromSession列表
	 */
	public List<FromSession> getFromSessions() {
		if (!isFullFromSession()) {
			throw new ClassCastException("not from-sessions");
		}
		ArrayList<FromSession> a = new ArrayList<FromSession>(array.size());
		for (int index = 0; index < array.size(); index++) {
			a.add((FromSession) array.get(index));
		}
		return a;
	}

	/**
	 * 统计会话成员数目
	 * @return 会话成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 收缩到有效空间
	 */
	public void trim() {
		array.trimToSize();
	}

	/**
	 * 返回当前迭代编号
	 * @return 迭代编号
	 */
	public int getIterateIndex() {
		return iterateIndex;
	}

	/**
	 * 设置"TO - SUBTO"链表成员的排列序号(从TO根对象开始设置，下标是0)
	 * @param index 排列序号
	 */
	protected void setIterateIndex(int index) {
		// 设置当前迭代编号
		iterateIndex = index;
		// 给TaskSession分配编号
		for (ConductSession session : array) {
			session.setIterateIndex(index);
		}
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
		for(ConductSession e : array) {
			e.setIssuer(username);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.ConductDispatcher#setIndexSector(com.laxcus.access.index.section.IndexSector)
	 */
	@Override
	public void setIndexSector(ColumnSector e) {
		// 设置上级索引分区
		super.setIndexSector(e);
		// 设置会话索引分区
		for (ConductSession session : array) {
			session.setIndexSector(e);
		}
	}
	
	/**
	 * 生成TO阶段任务分派器对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public ToDispatcher duplicate() {
		return new ToDispatcher(this);
	}
}