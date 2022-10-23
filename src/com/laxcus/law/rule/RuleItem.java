/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.rule;

import java.io.*;

import com.laxcus.law.forbid.*;
import com.laxcus.law.limit.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 事务规则单元。<br><br>
 * 
 * 通过事务规则单元，用户可以在数据处理期间，对操作的数据，实现共享或者排它性的使用，以此保证被处理数据的完整性和一致性。<br>
 * 事务规则单元判断建立在相同账号并行发出的命令上，不同账号之间的命令不存在事务处理问题。<br>
 * 事务规则的申请和释放由FRONT节点发出，投递给AID节点，AID节点负责判断、锁定、解除，CALL/DATA/WORK/BUILD节点执行事务命令，这样就形成一个完整的链条，
 * <br><br>
 * 
 * 事务处理级别分为3种，上级覆盖下级的全部资源，从高到低依次是：<br>
 * 1. 用户事务。<br>
 * 2. 数据库事务。<br>
 * 3. 数据表事务。<br><br>
 * 
 * 事务操作符，分为三种，用来判断事务冲突，AID站点据此决定是共享的并行执行，还是排它的串行执行：<br>
 * 1. 共享读：与其它共享读、共享写在AID站点并行执行，与独享写串行执行。<br>
 * 2. 共享写：与共享读类似，区别是AID站点在并行执行后，抵达DATA站点改为串行执行。共享写针对单项的写入操作，如SQL INSERT。<br>
 * 3. 独享写：与任何一个共享读、共享写、独享写都是互斥的，即一个独享写被AID受理后，在它执行过程，任何其它事务都不能够执行，直到它完成才能解除。<br><br>
 * 
 * 事务冲突的判断，即排它性判断，上下级事务之间互相影响。判断事务冲突的方法是：“Rule.conflict”，返回真时存在冲突，否则没有冲突。<br>
 * 事务冲突判断：<br>
 * 1. 用户事务拥有对“数据库事务、数据表事务”的完全排它能力。<br>
 * 2. 同名的数据库事务，同名的数据表事务之间拥有完全排它能力。<br>
 * 3. “同数据库名”之间的数据库事务、数据表事务拥有完全排它能力。<br><br>
 * 
 * 事务判断的前提是用户签名一致，这个比较前提由事务关联的方法和类来保证。<br>
 * 
 * @author scott.liang
 * @version 1.1 4/1/2015
 * @since laxcus 1.0
 */
public abstract class RuleItem implements Classable, Serializable, Cloneable, Comparable<RuleItem> {
	
	private static final long serialVersionUID = -1510752954588530715L;
	
	/** 事务规则单元标识 **/
	private RuleFlag flag;

	/**
	 * 将站点参数写入可类化数据存储器
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 事务规则单元标识
		writer.writeObject(flag);
		// 写入子类数据
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化数据读取器中解析站点参数
	 * @since 1.1
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 事务规则单元标识
		flag = new RuleFlag(reader);
		// 读子类数据
		resolveSuffix(reader);
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造默认的事务规则单元
	 */
	protected RuleItem() {
		super();
	}

	/**
	 * 构造事务规则单元，指定级别和操作符
	 * @param rank 事务级别
	 * @param operator 操作符
	 */
	protected RuleItem(byte rank, byte operator) {
		this();
		flag = new RuleFlag(rank, operator);
	}

	/**
	 * 根据传入的事务规则单元，生成它的数据副本
	 * @param that RuleItem实例
	 */
	protected RuleItem(RuleItem that) {
		this();
		flag = that.flag;
	}

	/**
	 * 返回事务规则单元标识
	 * @return RuleFlag实例
	 */
	public RuleFlag getFlag() {
		return flag;
	}

	/**
	 * 返回操作符
	 * @return 操作符
	 */
	public byte getOperator() {
		return flag.getOperator();
	}

	/**
	 * 返回操作符的文本描述
	 * @return 操作符的文本描述
	 */
	public String getOperatorText() {
		return flag.getOperatorText();
	}

	/**
	 * 返回事务级别
	 * @return 事务级别
	 */
	public byte getRank() {
		return flag.getRank();
	}

	/**
	 * 返回事务级别的文本描述
	 * @return 事务级别的文本描述
	 */
	public String getRankText() {
		return flag.getRankText();
	}
	
	/**
	 * 判断是用户级别
	 * @return 返回真或者假
	 */
	public boolean isUserRank() {
		return flag.isUserRank();
	}

	/**
	 * 判断是数据库级别
	 * @return 返回真或者假
	 */
	public boolean isSchemaRank() {
		return flag.isSchemaRank();
	}

	/**
	 * 判断是表级别
	 * @return 返回真或者假
	 */
	public boolean isTableRank() {
		return flag.isTableRank();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != this.getClass()) {
			return false;
		} else if (this == that) {
			return true;
		}
		// 比较两个事务规则参数完成一致
		return compareTo((RuleItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return flag.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return flag.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RuleItem that) {
		if (that == null) {
			return 1;
		}
		// 标识一致
		return Laxkit.compareTo(flag, that.flag);
	}

	/**
	 * 生成与事务规则关联的故障锁定单元
	 * @return FaultItem实例
	 */
	public abstract FaultItem createFaultItem();

	/**
	 * 生成与事务规则关联的拒绝操作单元
	 * @return ForbidItem实例
	 */
	public abstract ForbidItem createForbidItem();

	/**
	 * 当前事务规则与传入的事务规则进行比较，判断它们存在冲突。所谓冲突表示两项操作不能共享数据资源，需要串行执行。
	 * @param that 被比较的事务规则
	 * @return 存在冲突返回“真”，否则“假”。
	 */
	public abstract boolean conflict(RuleItem that);

	/**
	 * 将当前事务规则单元生成一个新的数据副本
	 * @return RuleItem实例
	 */
	public abstract RuleItem duplicate();

	/**
	 * 将子类数据写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中读取子类数据
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);	

}