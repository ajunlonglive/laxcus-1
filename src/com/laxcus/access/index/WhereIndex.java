/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * index column root class
 * 
 * @author scott.liang 
 * 
 * @version 1.0 6/12/2009
 * 
 * @see com.laxcus.access.index
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index;

import java.io.*;

import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * <code>SQL WHERE<code>被检索参数。<br>
 * 被检索参数放在WHERE语句后面，如 :WHERE column name <code>(>|<|>=|<=|<>|LIKE)</code> value <br>
 * 
 * @author scott.liang
 * @version 1.3 2/23/2016
 * @since laxcus 1.0
 */
public abstract class WhereIndex implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -1989460480566258915L;

	/** 检索索引类型，参考IndexType类 中定义 **/
	private byte family;

	/**
	 * 构造一个检索参数，并且指定它的检索类型
	 * @param family 检索类型
	 */
	protected WhereIndex(byte family) {
		super();
		setFamily(family);
	}

	/**
	 * 根据传入参数，建立一个被检索值的副本
	 * @param that WhereIndex实例
	 */
	protected WhereIndex(WhereIndex that) {
		super();
		family = that.family;
	}

	/**
	 * 设置检索索引类型
	 * @param who 检索索引值
	 */
	public void setFamily(byte who) {
		if (!IndexType.isFamily(who)) {
			throw new IllegalValueException("illegal index! %d", who);
		}
		family = who;
	}

	/**
	 * 返回检索索引类型定义
	 * @return 检索索引值
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 判断是否JOIN检索索引
	 * @return 返回真或者假
	 */
	public boolean isOn() {
		return IndexType.isOnIndex(family);
	}

	/**
	 * 判断是嵌套检索
	 * @return 返回真或者假
	 */
	public boolean isNestedIndex() {
		return IndexType.isNestedIndex(family);
	}

	/**
	 * 短整型类型
	 * @return 返回真或者假
	 */
	public boolean isShortIndex() {
		return IndexType.isShortIndex(family);
	}

	/**
	 * 整型类型
	 * @return 返回真或者假
	 */
	public boolean isIntegerIndex() {
		return IndexType.isIntegerIndex(family);
	}

	/**
	 * 长整型类型
	 * @return 返回真或者假
	 */
	public boolean isLongIndex() {
		return IndexType.isLongIndex(family);
	}

	/**
	 * 单浮点类型
	 * @return 返回真或者假
	 */
	public boolean isFloatIndex() {
		return IndexType.isFloatIndex(family);
	}

	/**
	 * 双浮点类型
	 * @return 返回真或者假
	 */
	public boolean isDoubleIndex() {
		return IndexType.isDoubleIndex(family);
	}

	/**
	 * 生成索引的数据流并且输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter buff = new ClassWriter(256);
		build(buff);
		return buff.effuse();
	}

	/**
	 * 调用子类实例，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 设置用户签名。本处是空方法，具体由子类重新定义
	 * @param e 
	 */
	public void setIssuer(Siger e) {
		
	}
	
	/**
	 * 返回用户签名
	 * @return 本处是空方法，具体由子类重新定义 
	 */
	public Siger getIssuer() {
		return null;
	}
	
	/**
	 * 根据子类实例，生成一个它的副本
	 * @return WhereIndex子类实例
	 */
	public abstract WhereIndex duplicate();

	/**
	 * 返回列编号
	 * @return 列编号值
	 */
	public abstract short getColumnId();

	/**
	 * 设置列编号
	 * @param id 列编号值
	 */
	public abstract void setColumnId(short id);

}