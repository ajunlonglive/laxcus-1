/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access;

import java.io.*;

import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 列、函数进行关联/比较处理的基础类<br><br>
 * 
 * 语法描述: <br>
 * column_name1 > 122 and column_name2 = 'abc'<br>
 * function(column_name) = 'little' <br>
 * 
 * @author scott.liang
 * @version 1.1 7/12/2012
 * @since laxcus 1.0
 */
public abstract class Gradation implements Serializable, Cloneable, Classable {

	private static final long serialVersionUID = 8952328161841615762L;

	/** 本单元与下一单元的连接关系，见Types中定义 **/
	protected byte outerRelation;

	/** 单元内，同级关联条件之间的关系 **/
	protected byte relation;

	/** 比较关系(等于,不等于,大于,大于等于,小于,小于等于,等于空,不等于空...)，见Types中定义 **/
	protected byte compare;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 关系/比较参数
		writer.write(outerRelation);
		writer.write(relation);
		writer.write(compare);
		// 子类参数写入可类化存储器
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 关系/比较参数
		outerRelation = reader.read();
		relation = reader.read();
		compare = reader.read();
		// 读取子类参数
		resolveSuffix(reader);
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认的关联/比较单元
	 */
	protected Gradation() {
		super();
		outerRelation = LogicOperator.NONE;
		relation = LogicOperator.NONE;
		compare = 0;
	}

	/**
	 * 根据传入参数构造它的副本
	 * @param that Gradation实例
	 */
	protected Gradation(Gradation that) {
		this();
		outerRelation = that.outerRelation;
		relation = that.relation;
		compare = that.compare;
	}

	/**
	 * 设置比较关系符
	 * @param who 比较关系符
	 */
	public void setCompare(byte who) {
		if (!CompareOperator.isFamily(who)) {
			throw new IllegalValueException("illegal compare operator:%d", who);
		}
		compare = who;
	}

	/**
	 * 返回比较关系符
	 * @return 比较关系符
	 */
	public byte getCompare() {
		return compare;
	}

	/**
	 * 设置外部逻辑关系
	 * @param who 比较关系符
	 */
	public void setOuterRelation(byte who) {
		if(!LogicOperator.isFamily(who)) {
			throw new IllegalValueException("illegal outside relation: %d", who);
		}
		outerRelation = who;
	}

	/**
	 * 返回外部逻辑关系
	 * @return 比较关系符
	 */
	public byte getOuterRelation() {
		return this.outerRelation;
	}

	/**
	 * 设置同级逻辑连接关系
	 * @param who 比较关系符
	 */
	public void setRelation(byte who) {
		if (!LogicOperator.isFamily(who)) {
			throw new IllegalValueException("illegal relation:%d", who);
		}
		relation = who;
	}

	/**
	 * 返回同级逻辑连接关系
	 * @return 比较关系符
	 */
	public byte getRelation() {
		return relation;
	}

	/**
	 * 判断是"与"逻辑关系
	 * @return 返回真或者假
	 */
	public boolean isAND() {
		return LogicOperator.isAND(relation);
	}

	/**
	 * 判断是"或"逻辑关系
	 * @return 返回真或者假
	 */
	public boolean isOR() {
		return LogicOperator.isOR(relation);
	}

	/**
	 * 判断无逻辑联系
	 * @return 返回真或者假
	 */
	public boolean isNotRelate() {
		return LogicOperator.isNone(relation);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * Gradation子类根据自己当前参数，生成一个它的副本
	 * @return 返回Gradation子类实例
	 */
	public abstract Gradation duplicate();

	/**
	 * 将Gradation子类将私有参数写入可类化数据存储器
	 * @param writer  可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 将Gradation子类从可类化数据读取器中读出
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);
}