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
 * 数据构建的“SIFT”阶段对象。<br>
 * 
 * @author scott.liang
 * @version 1.3 4/2/2015
 * @since laxcus 1.0
 */
public final class SiftObject extends SessionObject {

	private static final long serialVersionUID = 8440955088906908485L;

	/** SIFT阶段数据输入接口，来源于用户 */
	private SiftInputter inputter;

	/** SIFT阶段数据分派器。根据SIFT输入的参数和当时的资源分析判断后产生。 **/
	private SiftDispatcher dispatcher;

	/** 上一级SIFT操作对象，与子级SIFT对象形成双向链 **/
	private SiftObject leader;

	/** 下一级SIFT操作对象，与上级SIFT对象互相形成映射 **/
	private SiftObject slave;

	/** 跨过后面的迭代对象数，这个参数在运行过程中，由BalanceTask的子类设置。默认是0 **/
	private int skipObjects;

	/**
	 * 设置操作人
	 * @param username 操作人签名
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
	 * @return 返回写入字节长度
	 */
	private int buildSimple(ClassWriter writer) {
		int size = writer.size();
		// 1. 前缀
		super.buildSuffix(writer);
		// 2. SIFT阶段输入器
		writer.writeInstance(inputter);
		// 3. SIFT阶段资源分派器
		writer.writeInstance(dispatcher);
		// 返回写入长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器解析基础参数
	 * @param reader 可类化读取器
	 * @return 返回解析字节长度
	 */
	private int resolveSimple(ClassReader reader) {
		int scale = reader.getSeek();
		// 1.解析前缀
		super.resolveSuffix(reader);
		// 2. SIFT资源输入器
		inputter = reader.readInstance(SiftInputter.class);
		// 3. SIFT资源分派器
		dispatcher = reader.readInstance(SiftDispatcher.class);		
		// 返回解析字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 将上级对象向后向前逐一编组，输出到对象数组中
	 * @return SiftObject列表
	 */
	private List<SiftObject> doHolders() {
		ArrayList<SiftObject> array = new ArrayList<SiftObject>();
		SiftObject that = leader;
		while (that != null) {
			array.add(that);
			that = that.previous(); // 前一个对象
		}
		return array;
	}

	/**
	 * 将子级对象从前向后逐一编组，输出到对象数组中
	 * @return SiftObject列表
	 */
	private List<SiftObject> doSlaves() {
		ArrayList<SiftObject> array = new ArrayList<SiftObject>();
		SiftObject that = slave;
		while (that != null) {
			array.add(that);
			that = that.next(); // 后一个对象
		}
		return array;
	}

	/**
	 * 设置当前对象的最上级对象，逐一递增到最前面
	 * @param that SiftObject实例
	 */
	private void setLeader(SiftObject that) {
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
	 * @param that SiftObject实例
	 */
	private void setSlave(SiftObject that) {
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
		this.buildSimple(writer);
		// 2. 上级对象写入可类化写入器(只写对象基础参数)
		List<SiftObject> thats = this.doHolders();
		writer.writeInt(thats.size());
		for (int i = 0; i < thats.size(); i++) {
			thats.get(i).buildSimple(writer);
		}
		// 3. 子级对象写入可类化写入器(只写对象基础参数)
		thats = this.doSlaves();
		writer.writeInt(thats.size());
		for (int i = 0; i < thats.size(); i++) {
			thats.get(i).buildSimple(writer);
		}
		// 跨过迭代对象
		writer.writeInt(this.skipObjects);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.SessionObject#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 1. 解析当前对象的基础参数
		this.resolveSimple(reader);
		// 2. 解析上级对象的基础参数，并且放到当前对象的最前面
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SiftObject that = new SiftObject();
			that.resolveSimple(reader);
			this.setLeader(that);
		}
		// 3. 解析子级对象的基础对象，并且放到当前对象的最后
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SiftObject that = new SiftObject();
			that.resolveSimple(reader);
			this.setSlave(that);
		}
		// 跨过迭代对象
		this.skipObjects = reader.readInt();
	}

	/**
	 * 根据传入的SIFT阶段命名对象，生成一个它的副本
	 * @param that SiftObject实例
	 */
	private SiftObject(SiftObject that) {
		super(that);
		inputter = that.inputter;
		dispatcher = that.dispatcher;
		leader = that.leader;
		slave = that.slave;
		this.skipObjects = that.skipObjects;
	}

	/**
	 * 构造一个默认和私有的SIFT阶段对象
	 */
	private SiftObject() {
		super(PhaseTag.SIFT);
	}

	/**
	 * 构造SIFT阶段命名对象，同指定的工作模式和阶段命名
	 * @param phase 阶段命名
	 */
	public SiftObject(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 从可类化读取器中解析SIFT阶段对象的所有参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SiftObject(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置跨越迭代对象数，必须大于等于0。
	 * 如果此参数大于0，“CallEstablishInvoker”的判断将以此为依据，不再自增内置的“iterateToIndex”变量
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
	 * 设置SIFT阶段数据输入接口 
	 * @param e SiftInputter接口
	 */
	public void setInputter(SiftInputter e) {
		inputter = e;
	}

	/**
	 * 返回SIFT阶段数据输入接口
	 * @return SiftInputter接口
	 */
	public SiftInputter getInputter() {
		return inputter;
	}

	/**
	 * 设置SIFT阶段数据分派接口
	 * @param e SiftDispatcher实例
	 */
	public void setDispatcher(SiftDispatcher e) {
		dispatcher = e;
	}

	/**
	 * 返回SIFT阶段数据分派接口
	 * @return SiftDispatcher实例
	 */
	public SiftDispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * 在当前对象的最后捆绑一个SIFT对象，形成双向链表结构
	 * @param object SiftObject实例
	 */
	public void attach(SiftObject object) {
		if (slave != null) {
			slave.attach(object);
		} else {
			slave = object;
			slave.leader = this;
		}
	}

	/**
	 * 判断是否有上级SIFT对象，如果没有，这是根对象
	 * @return 返回真或者假
	 */
	public boolean hasPrevious() {
		return leader != null;
	}

	/**
	 * 向前递推到第N个SIFT对象，返回这个对象的句柄，超出返回空指针
	 * @param iterateIndex 当前迭代编号
	 * @param endIndex 迭代次数
	 * @return 返回SiftObject实例
	 */
	private SiftObject previous(int iterateIndex, final int endIndex) {
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
	 * @return SiftObject实例
	 */
	public SiftObject previous(int endIndex) {
		return previous(0, endIndex);
	}

	/**
	 * 返回当前对象的前面一个对象
	 * @return SiftObject实例，或者空指针
	 */
	public SiftObject previous() {
		return previous(1);
	}

	/**
	 * 判断是否有子级SIFT对象
	 * @return 返回真或者假
	 */
	public boolean hasNext() {
		return slave != null;
	}

	/**
	 * 向后递推到第N个SIFT对象，返回这个对象的句柄
	 * @param iterateIndex 当前迭代编号
	 * @param endIndex 迭代次数
	 * @return SiftObject实例，或者空指针
	 */
	private SiftObject next(int iterateIndex, final int endIndex) {
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
	 * @return SiftObject实例，或者空指针
	 */
	public SiftObject next(int endIndex) {
		return next(0, endIndex);
	}

	/**
	 * 返回当前对象的下一个对象(SUBSIFT)，如果没有是一个空指针。
	 * @return SiftObject实例，或者空指针
	 */
	public SiftObject next() {
		return next(1);
	}
	
	/**
	 * 跨过N个阶段对象后，判断仍然有阶段对象
	 * @param endIndex 迭代次数
	 * @return 返回真或者假
	 */
	public boolean hasNext(int endIndex) {
		SiftObject e = next(endIndex);
		return e != null;
	}

	/**
	 * 返回链表结构的第一个SIFT阶段对象，如果没有，返回自己
	 * @return SiftObject实例
	 */
	public SiftObject first() {
		if (leader == null) {
			return this;
		} else {
			return leader.first();
		}
	}

	/**
	 * 返回链表结构里的最后一个SIFT阶段对象，如果没有，返回自己。
	 * @return SiftObject实例
	 */
	public SiftObject last() {
		// 如果没有子级，返回自己。否则递归返回最后一个子级(SUBSIFT)
		if (slave == null) {
			return this;
		} else {
			return slave.last();
		}
	}

	/**
	 * 设置"SIFT - SUBSIFT"链表成员的迭代编号。<br>
	 * 这个操作必须从<b>SIFT根对象</b>开始设置，编号是0，依次以加1方式递增。
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
	 * 从根对象开始，依次递增，给每级SIFT对象分配迭代编号。根对象迭代号是0，逐级加1。
	 * 这个方法由CALL节点负责，在"INIT"阶段任务分配完成后调用。
	 */
	public void doIterateIndex() {
		createIterateIndex(0);
	}

	/**
	 * 生成SIFT阶段命名对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public SiftObject duplicate() {
		return new SiftObject(this);
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
		SiftObject sub = slave;
		while (sub != null) {
			array.addAll(sub.getRules());
			sub = sub.next();
		}
		// 输出集合
		return array;
	}
}
