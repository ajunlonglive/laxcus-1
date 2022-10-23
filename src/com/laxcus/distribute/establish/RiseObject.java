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
 * 数据构建的“RISE”阶段对象。<br><br>
 * 
 * RISE分布任务组件部署在DATA主站点。<br>
 * 
 * 工作内容：<br>
 * 1. 从BUILD节点下载数据块。<br>
 * 2. 如果数据块编号重叠，删除旧数据块，更新为新的数据块。<br>
 * 3. 数据块编号不重叠，写入新的数据块。<br>
 * 4. 将本地的数据块分发到关联的DATA从站点。<br>
 * 
 * @author scott.liang
 * @version 1.1 12/11/2015
 * @since laxcus 1.0
 */
public class RiseObject extends SessionObject {

	private static final long serialVersionUID = -1600963987755401477L;

	/** RISE阶段参数输入器 **/
	private ArrayList<RiseInputter> inputters = new ArrayList<RiseInputter>();

	/** RISE阶段数据分派器 */
	private RiseDispatcher dispatcher;

	/**
	 * 增加一个RISE阶段输入器
	 * @param e RISE阶段输入器
	 * @return 返回真或者假
	 */
	public boolean addInputter(RiseInputter e) {
		Laxkit.nullabled(e);

		return inputters.add(e);
	}

	/**
	 * 增加一组RISE阶段输入器
	 * @param a RiseInputter数组
	 * @return 返回新增成员数目
	 */
	public int addInputters(Collection<RiseInputter> a) {
		int size = inputters.size();
		for (RiseInputter e : a) {
			addInputter(e);
		}
		return inputters.size() - size;
	}

	/**
	 * 返回指定下标的RISE阶段输入器接口
	 * @param index 下标
	 * @return RiseInputter实例
	 */
	public RiseInputter getInputter(int index) {
		if (index < 0 || index >= inputters.size()) {
			return null;
		}
		return inputters.get(index);
	}

	/**
	 * 输出全部RISE阶段输入器
	 * @return RiseInputter列表
	 */
	public List<RiseInputter> getInputters() {
		return new ArrayList<RiseInputter>(inputters);
	}

	/**
	 * 设置RISE阶段数据分派器
	 * @param e RiseDispatcher实例
	 */
	public void setDispatcher(RiseDispatcher e) {
		dispatcher = e;
	}

	/**
	 * 返回RISE阶段数据分派器
	 * @return RiseDispatcher实例
	 */
	public RiseDispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * 根据传入的RISE阶段对象，生成它的数据副本
	 * @param that RISE阶段实例
	 */
	private RiseObject(RiseObject that) {
		super(that);
		inputters.addAll(that.inputters);
		dispatcher = that.dispatcher;
	}

	/**
	 * 构造默认的RISE阶段对象
	 */
	public RiseObject() {
		super(PhaseTag.RISE);
	}

	/**
	 * 构造RISE阶段对象，指定它的阶段命名。
	 * @param phase 阶段命名
	 */
	public RiseObject(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 使用可类化读取器解析RISE对象参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public RiseObject(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置阶段对象持有人
	 * @see com.laxcus.distribute.DistributedObject#setIssuer(com.laxcus.util.Siger)
	 */
	@Override
	public void setIssuer(Siger username) {
		super.setIssuer(username);
		if (dispatcher != null) {
			dispatcher.setIssuer(username);
		}
		for(RiseInputter e : inputters) {
			e.setIssuer(username);
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
		// 输入器的事务规则
		for (RiseInputter e : inputters) {
			array.addAll(e.getRules());
		}
		// 分派器的事务规则
		if (dispatcher != null) {
			array.addAll(dispatcher.getRules());
		}
		// 输出集合
		return array;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.distribute.DistributeObject#duplicate()
	 */
	@Override
	public RiseObject duplicate() {
		return new RiseObject(this);
	}

	/**
	 * 将当前对象信息写入可类化存储器
	 * @see com.laxcus.distribute.SessionObject#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 前缀
		super.buildSuffix(writer);
		// RISE参数输入器
		writer.writeInt(inputters.size());
		for (int i = 0; i < inputters.size(); i++) {
			writer.writeObject(inputters.get(i));
		}
		// RISE分派器
		writer.writeInstance(dispatcher);
	}

	/**
	 * 从可类化读取器中解析当前对象信息
	 * @see com.laxcus.distribute.SessionObject#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 前缀
		super.resolveSuffix(reader);
		// RISE参数输入器
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RiseInputter e = new RiseInputter(reader);
			inputters.add(e);
		}
		// RISE资源分派器
		dispatcher = reader.readInstance(RiseDispatcher.class);
	}

}