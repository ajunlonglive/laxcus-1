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
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 数据构建的“SCAN”阶段对象<br><br>
 * 
 * SCAN阶段作用到DATA主站点，提取数据表的索引映像。
 * 
 * @author scott.liang
 * @version 1.1 12/11/2015
 * @since laxcus 1.0
 */
public final class ScanObject extends SessionObject {

	private static final long serialVersionUID = 3953312413299569635L;

	/** SCAN阶段参数输入器 **/
	private ArrayList<ScanInputter> inputters = new ArrayList<ScanInputter>();

	/** SCAN阶段任务分派器 */
	private ScanDispatcher dispatcher;

	/**
	 * 根据传入的“SCAN”阶段对象实例，生成它的数据副本
	 * @param that ScanObject实例
	 */
	private ScanObject(ScanObject that) {
		super(that);
		inputters.addAll(that.inputters);
		dispatcher = that.dispatcher;
	}

	/**
	 * 构造一个默认的“SCAN”阶段对象
	 */
	public ScanObject() {
		super(PhaseTag.SCAN);
	}

	/**
	 * 构造“SCAN”阶段对象，并且设置阶段命名
	 * @param phase SCAN阶段命名
	 */
	public ScanObject(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 使用可类化读取器解析“SCAN”对象参数
	 * @param reader 可类化读取器
	 * @since 1.3
	 */
	public ScanObject(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 增加一个SCAN阶段输入器
	 * @param e SCAN阶段输入器
	 * @return 返回真或者假
	 */
	public boolean addInputter(ScanInputter e) {
		Laxkit.nullabled(e);

		return inputters.add(e);
	}

	/**
	 * 增加一组SCAN阶段输入器
	 * @param a SCAN阶段输入器数组
	 * @return 返回新增成员数目
	 */
	public int addInputters(Collection<ScanInputter> a) {
		int size = inputters.size();
		for (ScanInputter e : a) {
			addInputter(e);
		}
		return inputters.size() - size;
	}

	/**
	 * 返回指定下标的SCAN阶段输入器接口
	 * @param index 下标
	 * @return ScanInputter实例
	 */
	public ScanInputter getInputter(int index) {
		if (index < 0 || index >= inputters.size()) {
			return null;
		}
		return inputters.get(index);
	}

	/**
	 * 输出全部SCAN阶段输入器
	 * @return ScanInputter列表
	 */
	public List<ScanInputter> getInputters() {
		return new ArrayList<ScanInputter>(inputters);
	}

	/**
	 * 设置SCAN阶段任务分派器
	 * @param e ScanDispatcher实例
	 */
	public void setDispatcher(ScanDispatcher e) {
		dispatcher = e;
	}

	/**
	 * 返回SCAN阶段任务分派器
	 * @return ScanDispatcher实例
	 */
	public ScanDispatcher getDispatcher() {
		return dispatcher;
	}


	/**
	 * 设置阶段对象持有人
	 * @see com.laxcus.distribute.DistributedObject#setIssuer(com.laxcus.util.Siger)
	 */
	public void setIssuer(Siger username) {
		super.setIssuer(username);
		if (dispatcher != null) {
			dispatcher.setIssuer(username);
		}
		for(ScanInputter e : inputters) {
			e.setIssuer(username);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeObject#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		ArrayList<RuleItem> list = new ArrayList<RuleItem>();
		// 上级事务规则
		list.addAll(super.getRules());
		// 输入器的事务规则
		for (ScanInputter e : inputters) {
			list.addAll(e.getRules());
		}
		// 分派器的事务规则
		if (dispatcher != null) {
			list.addAll(dispatcher.getRules());
		}
		// 输出集合
		return list;
	}

	/**
	 * 将当前“SCAN”阶段对象参数写入可类化存储器
	 * @see com.laxcus.distribute.SessionObject#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 前缀信息
		super.buildSuffix(writer);
		// SCAN参数输入器
		writer.writeInt(inputters.size());
		for (int i = 0; i < inputters.size(); i++) {
			writer.writeObject(inputters.get(i));
		}
		// SCAN分派器
		writer.writeInstance(dispatcher);
	}

	/**
	 * 从可类化读取器中解析“SCAN”阶段对象参数。
	 * @see com.laxcus.distribute.SessionObject#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 前缀信息
		super.resolveSuffix(reader);
		// SCAN参数输入器
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			ScanInputter e = new ScanInputter(reader);
			inputters.add(e);
		}
		// SCAN资源分派器
		dispatcher = reader.readInstance(ScanDispatcher.class);
	}

	/**
	 * 根据当前实例，生成“SCAN”阶段对象的数据副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public DistributedObject duplicate() {
		return new ScanObject(this);
	}

}