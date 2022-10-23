/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.contact;

import java.util.*;

import com.laxcus.distribute.*;
import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * CONTACT快速计算模型的DISTANT(CONVERGE)阶段对象。<br><br>
 * 
 * 该阶段对象产生在CALL节点，执行在WORK节点，允许有任意多次的计算过程，以"迭代"的形式出现。<br>
 * 数据的最初来源自DATA节点，开始的WORK节点的数据必须从DATA节点上提取。<br>
 * <br>
 * 根据DISTANT阶段的迭代关系，返回结果有两种:<br>
 * <1> 如果在一个阶段后面有子级(slave)，表示需要继续迭代，返回FluxArea，即分布数据映射图。
 * 需要由CALL节点进行MERGE阶段处理，然后分发给子级处理。<br>
 * <2> 如果没有子级(slave)，它返回的是计算结果数据。<br>
 * <br>
 * 
 * @author scott.liang
 * @version 1.1 7/15/2015
 * @since laxcus 1.0
 */
public final class DistantObject extends SessionObject {

	private static final long serialVersionUID = 492080166768570661L;

	/** 工作模式，由外部方法在初始化时定义 **/
	private int mode;

	/** DISTANT阶段数据输入接口，来源于FRONT站点的用户输入后的解析 */
	private DistantInputter inputter;

	/** 
	 * DISTANT阶段数据分派器，在FORK阶段完成它的初始化，在MERGE阶段分配DISTANT阶段会话和保存。DISTANT会话实现网络分布计算。
	 * 运行过程中生成，包括FORK/MERGE阶段，同级的DistantInputter提供帮助。
	 **/
	private DistantDispatcher dispatcher;

	/** 上一级DISTANT操作对象，与子级DISTANT对象形成双向链 **/
	private DistantObject leader;

	/** 下一级DISTANT操作对象，与上级DISTANT对象互相形成映射 **/
	private DistantObject slave;
		
	/** 跨过后面的迭代对象数，这个参数在运行过程中，由MergeTask的子类设置。默认是0 **/
	private int skipObjects;

	/**
	 * 设置操作人
	 * @param username 操作人签名，是SHA256码
	 */
	public void setIssuer(Siger username) {
		super.setIssuer(username);
		// 输入器签名
		if (inputter != null) {
			inputter.setIssuer(username);
		}
		// 分派器签名
		if (dispatcher != null) {
			dispatcher.setIssuer(username);
		}
		// 迭代子级
		if (slave != null) {
			slave.setIssuer(username);
		}
	}

	/**
	 * 将基础参数写入可类化写入器
	 * @param writer 可类化写入器
	 * @return 写入的字节长度
	 */
	private int buildSimple(ClassWriter writer) {
		int size = writer.size();
		// 1. 前缀
		super.buildSuffix(writer);
		// 2. DISTANT阶段对象的工作模式，分为是产生数据和计算数据两种
		writer.writeInt(mode);
		// 3. DISTANT阶段输入器
		writer.writeInstance(inputter);
		// 4. DISTANT阶段资源分派器
		writer.writeInstance(dispatcher);
		
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器解析基础参数
	 * @param reader 可类化读取器
	 * @return 解析的字节长度
	 */
	private int resolveSimple(ClassReader reader) {
		int seek = reader.getSeek();
		// 1.解析前缀
		super.resolveSuffix(reader);
		// 2. DISTANT阶段对象的任务工作模式
		mode = reader.readInt();
		// 3. DISTANT资源输入器
		inputter = reader.readInstance(DistantInputter.class);
		// 4. DISTANT资源分派器
		dispatcher = reader.readInstance(DistantDispatcher.class);
		
		// 返回解析字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 将上级对象向后向前逐一编组，输出到对象数组中
	 * @return DistantObject列表
	 */
	private List<DistantObject> doHolders() {
		ArrayList<DistantObject> array = new ArrayList<DistantObject>();
		DistantObject that = leader;
		while (that != null) {
			array.add(that);
			that = that.previous(); // 前一个对象
		}
		return array;
	}

	/**
	 * 将子级对象从前向后逐一编组，输出到对象数组中
	 * @return DistantObject列表
	 */
	private List<DistantObject> doSlaves() {
		ArrayList<DistantObject> array = new ArrayList<DistantObject>();
		DistantObject that = slave;
		while (that != null) {
			array.add(that);
			that = that.next(); // 后一个对象
		}
		return array;
	}

	/**
	 * 设置当前对象的最上级对象，逐一递增到最前面
	 * @param that DistantObject实例
	 */
	private void setLeader(DistantObject that) {
		if(leader == null) {
			// 传入对象的子级是当前对象，并且将自己升为当前对象的上级
			that.slave = this; 
			leader = that;
		} else {
			leader.setLeader(that);
		}
	}

	/**
	 * 设置当前对象的子级对象，逐一递减到最后
	 * @param that DistantObject实例
	 */
	private void setSlave(DistantObject that) {
		if(slave == null) {
			// 传入对象的上级是当前对象，并且将自己成为当前对象的下级
			that.leader = this; 
			slave = that;
		} else {
			slave.setSlave(that);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.SessionObject#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 1. 写入当前对象的基础参数
		buildSimple(writer);
		// 2. 上级对象写入可类化写入器(只写对象基础参数)
		List<DistantObject> thats = doHolders();
		writer.writeInt(thats.size());
		for (int i = 0; i < thats.size(); i++) {
			thats.get(i).buildSimple(writer);
		}
		// 3. 子级对象写入可类化写入器(只写对象基础参数)
		thats = doSlaves();
		writer.writeInt(thats.size());
		for (int i = 0; i < thats.size(); i++) {
			thats.get(i).buildSimple(writer);
		}
		// 跨越对象
		writer.writeInt(this.skipObjects);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.SessionObject#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 1. 解析当前对象的基础参数
		resolveSimple(reader);
		// 2. 解析上级对象的基础参数，并且放到当前对象的最前面
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			DistantObject that = new DistantObject();
			that.resolveSimple(reader);
			setLeader(that);
		}
		// 3. 解析子级对象的基础对象，并且放到当前对象的最后
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			DistantObject that = new DistantObject();
			that.resolveSimple(reader);
//			System.out.printf("slave object is %s\n", that.getPhase());
			setSlave(that);
		}
		// 跨越对象
		this.skipObjects = reader.readInt();
	}

	/**
	 * 根据传入的DISTANT阶段命名对象，生成一个它的副本
	 * @param that DistantObject实例
	 */
	private DistantObject(DistantObject that) {
		super(that);
		mode = that.mode;
		inputter = that.inputter;
		dispatcher = that.dispatcher;
		leader = that.leader;
		slave = that.slave;
		this.skipObjects = that.skipObjects;
	}

	/**
	 * 构造一个默认和私有的DISTANT阶段对象
	 */
	private DistantObject() {
		super(PhaseTag.DISTANT);
	}

	/**
	 * 构造DISTANT阶段对象，同时指定的工作模式
	 * @param mode DISTANT阶段工作模式（GENERATE/EVALUATE）
	 */
	public DistantObject(int mode) {
		this();
		setMode(mode);
	}

	/**
	 * 构造DISTANT阶段命名对象，同指定的工作模式和阶段命名
	 * @param mode DISTANT阶段工作模式
	 * @param phase 阶段命名
	 */
	public DistantObject(int mode, Phase phase) {
		this(mode);
		setPhase(phase);
	}

	/**
	 * 从可类化读取器中解析DISTANT阶段对象的所有参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DistantObject(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置跨越迭代对象数，必须大于等于0。
	 * 如果此参数大于0，“CallContactInvoker”的判断将以此为依据，不再自增内置的“iterateToIndex”变量
	 * @param who 数字
	 */
	public boolean setSkipObjects(int who) {
		if (who >= 0) {
			skipObjects = who;
			return true;
		}
		return false;
	}

	/**
	 * 返回跨越迭代对象数
	 * @return 数字
	 */
	public int getSkipObjects() {
		return skipObjects;
	}

	/**
	 * 设置DISTANT阶段对象的工作模式（产生数据/计算数据的任意一种）
	 * @param who 工作模式
	 */
	private void setMode(int who) {
		if(!DistantMode.isMode(who)) {
			throw new IllegalValueException("illegal mode:%d", who);
		}
		mode = who;
	}

	/**
	 * 返回DISTANT阶段对象的工作模式（产生数据/计算数据中的任意一种）
	 * @return 工作模式
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * 判断当前对象是不是"产生数据"的任务对象
	 * @return 返回真或者假
	 */
	public boolean isGenerate() {
		return DistantMode.isGenerate(mode);
	}

	/**
	 * 判断当前对象是不是"计算数据"的任务对象
	 * @return 返回真或者假
	 */
	public boolean isEvaluate() {
		return DistantMode.isEvaluate(mode);
	}

	/**
	 * 设置DISTANT阶段数据输入接口 
	 * @param e DistantInputter实例
	 */
	public void setInputter(DistantInputter e) {
		inputter = e;
	}

	/**
	 * 返回输入接口
	 * @return DistantInputter实例
	 */
	public DistantInputter getInputter() {
		return inputter;
	}

	/**
	 * 设置DISTANT阶段分派器
	 * @param e DistantDispatcher实例
	 */
	public void setDispatcher(DistantDispatcher e) {
		dispatcher = e;
	}

	/**
	 * 返回DISTANT阶段分派器
	 * @return DistantDispatcher实例
	 */
	public DistantDispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * 在当前对象的最后捆绑一个DISTANT对象，形成双向链表结构
	 * @param that DistantObject实例
	 */
	public void attach(DistantObject that) {
		if (slave != null) {
			slave.attach(that);
		} else {
			slave = that;
			slave.leader = this;
		}
	}

	/**
	 * 判断是否有上级DISTANT对象，如果没有，这是根对象
	 * @return 返回真或者假
	 */
	public boolean hasPrevious() {
		return leader != null;
	}

	/**
	 * 向前递推到第N个DISTANT对象，返回这个对象的句柄，超出返回空指针
	 * @param iterateIndex 当前迭代编号
	 * @param endIndex 迭代次数
	 * @return DistantObject实例
	 */
	private DistantObject previous(int iterateIndex, final int endIndex) {
		if (iterateIndex == endIndex) {
			return this;
		} else if (leader != null) {
			return leader.previous(iterateIndex + 1, endIndex);
		} else {
			return null;
		}
	}

	/**
	 * 返回从当前对象开始，向前递推到第N个对象
	 * @param endIndex 迭代次数
	 * @return DistantObject实例
	 */
	public DistantObject previous(int endIndex) {
		return previous(0, endIndex);
	}

	/**
	 * 返回当前对象的前面一个对象
	 * @return DistantObject实例
	 */
	public DistantObject previous() {
		return previous(1);
	}

	/**
	 * 判断是否有子级DISTANT对象
	 * @return 返回真或者假
	 */
	public boolean hasNext() {
		return slave != null;
	}

	/**
	 * 向后递推到第N个DISTANT对象，返回这个对象的句柄
	 * @param iterateIndex 当前迭代编号
	 * @param endIndex 迭代次数
	 * @return DistantObject实例
	 */
	private DistantObject next(int iterateIndex, final int endIndex) {
		if (iterateIndex == endIndex) {
			return this;
		} else if (slave != null) {
			return slave.next(iterateIndex + 1, endIndex);
		} else {
			return null;
		}
	}

	/**
	 * 以当前对象下标为0，返回从它开始的第N级链接对象。
	 * @param endIndex 迭代次数
	 * @return DistantObject实例
	 */
	public DistantObject next(int endIndex) {
		return next(0, endIndex);
	}

	/**
	 * 返回当前对象的下一个对象(SUBDISTANT)，如果没有是一个空指针。
	 * @return DistantObject实例
	 */
	public DistantObject next() {
		return next(1);
	}

	/**
	 * 跨过N个阶段对象后，判断仍然有阶段对象
	 * @param endIndex 迭代次数
	 * @return 返回真或者假
	 */
	public boolean hasNext(int endIndex) {
		DistantObject e = next(endIndex);
		return e != null;
	}

	/**
	 * 返回链表结构的第一个DISTANT阶段对象，如果没有，返回自己
	 * @return DistantObject实例
	 */
	public DistantObject first() {
		if (leader == null) {
			return this;
		} else {
			return leader.first();
		}
	}

	/**
	 * 返回链表结构里的最后一个DISTANT阶段对象，如果没有，返回自己。
	 * @return DistantObject实例
	 */
	public DistantObject last() {
		// 如果没有子级，返回自己。否则递归返回最后一个子级(SUBDISTANT)
		if (slave == null) {
			return this;
		} else {
			return slave.last();
		}
	}

	/**
	 * 设置"DISTANT - SUBDISTANT"链表成员的迭代编号。<br>
	 * 这个操作必须从<b>DISTANT根对象</b>开始设置，编号是0，依次以加1方式递增。
	 * 
	 * @param index 给当前对象分配的迭代编号
	 */
	private void createIterateIndex(int index) {
		// 设置当前对象的迭代编号
		super.setIterateIndex(index);
		// 给分派器设置迭代编号
		if (dispatcher != null) {
			dispatcher.setIterateIndex(index);
		}
		// 如果有子级，迭代编号加1
		if (slave != null) {
			slave.createIterateIndex(index + 1);
		}
	}

	/**
	 * 从根对象开始，依次递增，给每级DISTANT对象分配迭代编号。根对象迭代号是0，逐级加1。
	 * 这个方法由CALL节点负责，在"FORK"阶段任务分配完成后调用。
	 */
	public void doIterateIndex() {
		createIterateIndex(0);
	}

	/**
	 * 生成DISTANT阶段命名对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public DistantObject duplicate() {
		return new DistantObject(this);
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
		// 输入器事务规则
		if (inputter != null) {
			array.addAll(inputter.getRules());
		}
		// 分派器事务规则
		if (dispatcher != null) {
			array.addAll(dispatcher.getRules());
		}
		// 子级中的事务
		DistantObject sub = slave;
		while (sub != null) {
			array.addAll(sub.getRules());
			sub = sub.next();
		}
		// 输出集合
		return array;
	}

}