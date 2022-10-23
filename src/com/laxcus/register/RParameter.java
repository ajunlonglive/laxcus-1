/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 环境变量
 * 
 * @author scott.liang
 * @version 1.0 7/15/2021
 * @since laxcus 1.0
 */
public abstract class RParameter extends RToken {

	/** 参数数据类型 */
	private byte type;
	
	/**
	 * 构造运行环境变量
	 */
	protected RParameter() {
		super(RTokenAttribute.PARAMETER);
		type = 0; // 不定义
	}

	/**
	 * 构造运行环境变量，指定属性
	 * @param attribute 属性
	 * @param type 类型 
	 */
	protected RParameter(byte type) {
		this();
		setType(type);
	}
	
	/**
	 * 生成运行环境变量副本
	 * @param that
	 */
	protected RParameter(RParameter that) {
		super(that);
		type = that.type;
	}
	
	/**
	 * 返回参数类型，见RTokenType定义
	 * @return 参数类型的字节描述
	 */
	public byte getType() {
		return type;
	}

	/**
	 * 设置参数类型，见RTokenType定义
	 * @param who 参数类型
	 */
	private void setType(byte who) {
		if (!RParameterType.isValueType(who)) {
			throw new IllegalValueException("illegal value: %d", who);
		}
		type = who;
	}

	/**
	 * 判断是布尔类型
	 * @return 返回真或者假
	 */
	public boolean isBoolean() {
		return RParameterType.isBoolean(type);
	}

	/**
	 * 判断是字节数组类型
	 * @return 返回真或者假
	 */
	public boolean isRaw() {
		return RParameterType.isRaw(type);
	}

	/**
	 * 判断是字符串类型
	 * @return 返回真或者假
	 */
	public boolean isString() {
		return RParameterType.isString(type);
	}

	/**
	 * 判断是短整型
	 * @return 返回真或者假
	 */
	public boolean isShort() {
		return RParameterType.isShort(type);
	}

	/**
	 * 判断是整型
	 * @return 返回真或者假
	 */
	public boolean isInteger() {
		return RParameterType.isInteger(type);
	}

	/**
	 * 判断是长整型
	 * @return 返回真或者假
	 */
	public boolean isLong() {
		return RParameterType.isLong(type);
	}

	/**
	 * 判断是单浮点值
	 * @return 返回真或者假
	 */
	public boolean isFloat() {
		return RParameterType.isFloat(type);
	}

	/**
	 * 判断是双浮点值
	 * @return 返回真或者假
	 */
	public boolean isDouble() {
		return RParameterType.isDouble(type);
	}

	/**
	 * 判断是日期类型
	 * @return 返回真或者假
	 */
	public boolean isDate() {
		return RParameterType.isDate(type);
	}

	/**
	 * 判断是时间类型
	 * @return 返回真或者假
	 */
	public boolean isTime() {
		return RParameterType.isTime(type);
	}

	/**
	 * 判断是时间戳类型
	 * @return 返回真或者假
	 */
	public boolean isTimestamp() {
		return RParameterType.isTimestamp(type);
	}

	/**
	 * 判断是串行化对象类型
	 * @return 返回真或者假
	 */
	public boolean isSerializable() {
		return RParameterType.isSerializable(type);
	}

	/**
	 * 判断是可类化接口类型
	 * @return 返回真或者假
	 */
	public boolean isClassable() {
		return RParameterType.isClassable(type);
	}
	
	/**
	 * 判断是命令类型
	 * @return 返回真或者假
	 */
	public boolean isCommand() {
		return RParameterType.isCommand(type);
	}

//	/*
//	 * (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object that) {
//		if (that == null || that.getClass() != getClass()) {
//			return false;
//		} else if (that == this) {
//			return true;
//		}
//		// 比较一致
//		return compareTo((RParameter) that) == 0;
//	}
	
//	public int compareTo(RParameter that) {
//		if (that == null) {
//			return 1;
//		}
////		if (!Laxkit.isClassFrom(that, RParameter.class)) {
////			return 1;
////		}
//		
//		RParameter param = (RParameter) that;
//		int ret = super.compareTo(param);
//		if (ret == 0) {
//			ret = Laxkit.compareTo(type, param.type);
//		}
//		return ret;
//	}
	
	/**
	 * 将子类的参数写入可类化写入器
	 * @param writer 可类化写入器
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.write(type);
	}

	/**
	 * 从可类化读取器中解析子类的参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		type = reader.read();
	}
}