/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute;

import java.io.*;
import java.util.*;

import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 分布式对象。<br><br>
 * 
 * CONDUCT/ESTABLISH命令在执行过程中，会被分解成多个“阶段”来处理。DistributeObject就是这个阶段处理设计理念的体现。<br>
 * 
 * DistributeObject是CONDUCT/ESTABLISH阶段对象的基础类，定义了所有分布式对象共同的参数：“阶段命名”。在它之下定义了自由定义参数的AccessObject，以及之后的SessionObject。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/3/2015
 * @since laxcus 1.0
 */
public abstract class DistributedObject implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -8013117762467963481L;

	/** 阶段命名 **/
	private Phase phase;

	/** 事务规则集合 **/
	private TreeSet<RuleItem> rules = new TreeSet<RuleItem>();

	/**
	 * 将分布式对象参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter w) {
		final int size = w.size();

		ClassWriter writer = new ClassWriter();
		// 阶段命名
		writer.writeObject(phase);
		// 事务规则集合
		writer.writeInt(rules.size());
		for (RuleItem e : rules) {
			writer.writeObject(e);
		}
		// 写入子类参数
		buildSuffix(writer);

		// 写入缓存
		byte[] b = writer.effuse();
		w.writeInt(b.length);
		w.write(b);

		// 返回写入的数据长度
		return w.size() - size;
	}

	/**
	 * 从可类化读取器中解析分布式对象参数。
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader r) {
		final int seek = r.getSeek();

		// 从可类化读取器中读取DistributeObject字节流
		int len = r.readInt();
		byte[] b = r.read(len);

		ClassReader reader = new ClassReader(b);
		// 阶段命名
		phase = new Phase(reader);
		// 解析事务规则
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RuleItem e = RuleItemCreator.resolve(reader);
			if (e == null) {
				throw new IllegalValueException("illegal rule");
			}
			rules.add(e);
		}
		// 解析子类对象
		resolveSuffix(reader);

		// 返回读取的数据长度
		return r.getSeek() - seek;
	}

	/**
	 * 构造一个默认的分布式对象
	 */
	protected DistributedObject() {
		super();
	}

	/**
	 * 根据传入的分布式对象实例，生成它的浅层数据副本。
	 * @param that DistributeObject实例
	 */
	protected DistributedObject(DistributedObject that) {
		this();
		phase = that.phase.duplicate();
		rules.addAll(that.rules);
	}

	/**
	 * 构造分布式对象，并且指定它的阶段命名。
	 * @param phase 阶段命名
	 */
	protected DistributedObject(Phase phase) {
		this();
		setPhase(phase);
	}
	
	/**
	 * 设置阶段命名 <br>
	 * 传入的参数如果是空值，弹出空指针异常。如果与原有阶段命名不匹配，弹出阶段命名异常。
	 * 
	 * @param e Phase实例
	 * @throws NullPointerException, IllegalPhaseException
	 */
	public void setPhase(Phase e) {
		Laxkit.nullabled(e);

		// 类型必须匹配
		if (phase != null && phase.getFamily() != e.getFamily()) {
			throw new IllegalPhaseException("cannot be match %s | %s", phase, e);
		}
		phase = e.duplicate();
	}

	/**
	 * 返回阶段命名
	 * @return Phase实例
	 */
	public Phase getPhase() {
		return phase;
	}

	/**
	 * 设置操作人签名
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		if (phase != null) {
			phase.setIssuer(e);
		}
	}

	/**
	 * 返回操作人签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		if (phase != null) {
			return phase.getIssuer();
		}
		return null;
	}

	/**
	 * 调用子类实例，克隆一个它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 保存一个事务规则
	 * @param e RuleItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean addRule(RuleItem e) {
		Laxkit.nullabled(e);

		return rules.add(e);
	}

	/**
	 * 保存一批事务规则
	 * @param all 事务规则数组
	 * @return 返回新增的事务规则成员数目
	 */
	public int addRules(Collection<RuleItem> all) {
		int size = rules.size();
		for (RuleItem e : all) {
			addRule(e);
		}
		return rules.size() - size;
	}

	/**
	 * 当前对象输出它的全部事务规则。
	 * 
	 * @return 事务规则单元列表
	 */
	public List<RuleItem> getRules() {
		return new ArrayList<RuleItem>(rules);
	}

	/**
	 * 子类生成自己实例的数据副本
	 * @return 返回DistributeObject子类实例
	 */
	public abstract DistributedObject duplicate();

	/**
	 * 将子类对象参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析子类对象参数信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);
}