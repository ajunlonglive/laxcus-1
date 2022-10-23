/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.conduct;

import java.util.*;

import com.laxcus.distribute.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * DIFFUSE/CONVERGE分布计算模型的DIFFUSE(FROM)阶段对象。<br><br>
 * 
 * DIFFUSE作用于DATA节点，是数据产生阶段。<br>
 * 数据产生有两种:<br>
 * <1> 根据SQL SELECT语句，从数据存储层提取 (允许多个SELECT同时进行)。<br>
 * <2> 根据用户定义参数，从对应的命名接口中产生(用户自定义参数由用户自己解析)。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/15/2015
 * @since laxcus 1.0
 */
public final class FromObject extends SessionObject {

	private static final long serialVersionUID = 2048098335299408313L;

	/** FROM参数输入器(多个，平行分布处理) **/
	private List<FromInputter> inputters = new ArrayList<FromInputter>();

	/** FROM资源分派器(只有一个) */
	private FromDispatcher dispatcher;

	/**
	 * 将FROM阶段对象参数写入可类化存储器
	 * @see com.laxcus.distribute.SessionObject#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 生成前缀
		super.buildSuffix(writer);
		// FROM输入
		writer.writeInt(inputters.size());
		for (int i = 0; i < inputters.size(); i++) {
			writer.writeObject(inputters.get(i));
		}
		// FROM分派器
		writer.writeInstance(dispatcher);
	}

	/**
	 * 从可类化读取器中解析FROM阶段对象参数
	 * @see com.laxcus.distribute.SessionObject#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀
		super.resolveSuffix(reader);
		// FROM输入器
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			FromInputter in = new FromInputter(reader);
			inputters.add(in);
		}
		// FROM资源分派器
		dispatcher = reader.readInstance(FromDispatcher.class);
	}

	/**
	 * 根据传入的FromObject对象，生成它的副本
	 * @param that FromObject实例
	 */
	private FromObject(FromObject that) {
		super(that);
		inputters.addAll(that.inputters);
		dispatcher = that.dispatcher;
	}

	/**
	 * 构造一个默认的FROM阶段对象
	 */
	public FromObject() {
		super(PhaseTag.FROM);
	}

	/**
	 * 初始化对象并且设置FROM阶段命名
	 * 
	 * @param phase FROM阶段命名
	 */
	public FromObject(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 从可类化读取器中解析参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public FromObject(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个FROM阶段输入
	 * @param inputter FromInputter实例
	 * @return 返回真或者假
	 */
	public boolean addInputter(FromInputter inputter) {
		if (inputter != null) {
			return inputters.add(inputter);
		}
		return false;
	}

	/**
	 * 保存一批FROM阶段输入
	 * @param a FromInputter数组
	 * @return 返回新增加的成员数目
	 */
	public int addInputters(Collection<FromInputter> a) {
		int size = inputters.size();
		for (FromInputter e : a) {
			addInputter(e);
		}
		return inputters.size() - size;
	}

	/**
	 * 返回指定下标的FROM输入接口
	 * @param index 所在下标
	 * @return FromInputter实例
	 */
	public FromInputter getInputter(int index) {
		if (index < 0 || index >= inputters.size()) {
			return null;
		}
		return inputters.get(index);
	}

	/**
	 * 统计FROM输入数目
	 * @return FROM输入数目
	 */
	public int countInput() {
		return inputters.size();
	}

	/**
	 * 设置FROM分派器
	 * @param e FromDispatcher实例
	 */
	public void setDispatcher(FromDispatcher e) {
		dispatcher = e;
	}

	/**
	 * 返回FROM分派器
	 * @return FromDispatcher实例
	 */
	public FromDispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * 生成FROM阶段命名对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public FromObject duplicate() {
		return new FromObject(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeObject#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger username) {
		super.setIssuer(username);
		for (FromInputter in : inputters) {
			in.setIssuer(username);
		}
		if (dispatcher != null) {
			dispatcher.setIssuer(username);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeObject#getRules()
	 */
	@Override
	public List<RuleItem> getRules() {
		ArrayList<RuleItem> array = new ArrayList<RuleItem>();
		// 上级事务规则
		array.addAll(super.getRules());
		// 输入器的事务规则
		for (FromInputter e : inputters) {
			array.addAll(e.getRules());
		}
		// 分派器的事务规则
		if (dispatcher != null) {
			array.addAll(dispatcher.getRules());
		}
		// 输出集合
		return array;
	}

}