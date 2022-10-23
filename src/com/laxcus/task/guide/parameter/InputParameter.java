/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.parameter;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;

/**
 * 启动输入参数。<br><br>
 * 
 * 启动输入参数的格式由系统规定，内容由用户定义，用在各种分布处理中。启动输入参数格式是：<br>
 * 
 * 启动输入参数名称(数据类型)=数字型数值|'字符串数值'|'日期/时间'|布尔值(false|true) <br>
 * 
 * 多个启动输入参数之间用逗号(,)分隔。<br><br>
 * 数据类型关键字: [bool|char|string|raw|short|int|long|float|double|date|time|timestamp] <br><br>
 * 
 * <br>
 * 
 * @author scott.liang
 * @version 1.0 7/27/2020
 * @since laxcus 1.0
 */
public abstract class InputParameter implements Serializable, Cloneable, Classable, Markable, Comparable<InputParameter> {

	private static final long serialVersionUID = -4396660962581017735L;

	/** 参数数据类型 */
	private byte family;

	/** 必选项或者否 **/
	private boolean select;
	
	/** 参数有效 */
	private boolean enabled;

	/** 参数名称，忽略大小写 */
	private Naming name;

	/** 工具提示文本 **/
	private String tooltip;

	/**
	 * 构造默认的启动输入参数，指定数据类型
	 * 
	 * @param family 数据类型
	 */
	protected InputParameter(byte family) {
		super();
		setFamily(family);
		setSelect(false);
		setEnabled(false);
	}

	/**
	 * 根据传入的启动输入参数，生成它的副本
	 * @param that BootParameter实例
	 */
	protected InputParameter(InputParameter that) {
		super();
		family = that.family;
		select = that.select;
		enabled = that.enabled;
		name = that.name;
		tooltip = that.tooltip;
	}

	/**
	 * 必须选择或者否
	 * @param b 真或者假
	 */
	public void setSelect(boolean b) {
		select = b;
	}

	/**
	 * 选择或者否
	 * @return 真或者假
	 */
	public boolean isSelect() {
		return select;
	}

	/**
	 * 有效或者否
	 * @param b 真或者假
	 */
	public void setEnabled(boolean b) {
		enabled = b;
	}

	/**
	 * 判断有效或者否
	 * @return 真或者假
	 */
	public boolean isEnabled() {
		return enabled;
	}

	
	/**
	 * 设置参数名称
	 * @param e 字符串
	 */
	public void setName(String e) {
		if (e == null || e.isEmpty()) {
			throw new NullPointerException();
		}
		name = new Naming(e);
	}

	/**
	 * 设置参数名称
	 * @param e 命名
	 */
	public void setName(Naming e) {
		Laxkit.nullabled(e);
		name = e;
	}

	/**
	 * 名称
	 * @return
	 */
	public Naming getName() {
		return name;
	}

	/**
	 * 返回参数名称
	 * @return 命名实例
	 */
	public String getNameText() {
		return name.toString();
	}

	/**
	 * 设置工具提示
	 * @param e 字符串或者空
	 */
	public void setTooltip(String e) {
		tooltip = e;
	}

	/**
	 * 返回工具提示
	 * @return 字符串或者空
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * 返回参数类型，见BootParameterType定义
	 * @return 参数类型的字节描述
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 设置参数类型，见BootParameterType定义
	 * @param who 参数类型
	 */
	private void setFamily(byte who) {
		if(!InputParameterType.isValueType(who)) {
			throw new IllegalValueException("illegal value: %d", who);
		}
		family = who;
	}

	/**
	 * 判断是布尔类型
	 * @return 返回真或者假
	 */
	public boolean isBoolean() {
		return InputParameterType.isBoolean(family);
	}

	/**
	 * 判断是字符串类型
	 * @return 返回真或者假
	 */
	public boolean isString() {
		return InputParameterType.isString(family);
	}

	/**
	 * 判断是短整型
	 * @return 返回真或者假
	 */
	public boolean isShort() {
		return InputParameterType.isShort(family);
	}

	/**
	 * 判断是整型
	 * @return 返回真或者假
	 */
	public boolean isInteger() {
		return InputParameterType.isInteger(family);
	}

	/**
	 * 判断是长整型
	 * @return 返回真或者假
	 */
	public boolean isLong() {
		return InputParameterType.isLong(family);
	}

	/**
	 * 判断是单浮点值
	 * @return 返回真或者假
	 */
	public boolean isFloat() {
		return InputParameterType.isFloat(family);
	}

	/**
	 * 判断是双浮点值
	 * @return 返回真或者假
	 */
	public boolean isDouble() {
		return InputParameterType.isDouble(family);
	}

	/**
	 * 判断是日期类型
	 * @return 返回真或者假
	 */
	public boolean isDate() {
		return InputParameterType.isDate(family);
	}

	/**
	 * 判断是时间类型
	 * @return 返回真或者假
	 */
	public boolean isTime() {
		return InputParameterType.isTime(family);
	}

	/**
	 * 判断是时间戳类型
	 * @return 返回真或者假
	 */
	public boolean isTimestamp() {
		return InputParameterType.isTimestamp(family);
	}

	/**
	 * 将数据类型，标题、数据值输出到可类化写入器
	 * @param writer 可类化写入器
	 * @return 返回写入的字节长度
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 数据类型
		writer.write(family);
		writer.writeBoolean(select);
		writer.writeBoolean(enabled);
		// 标题名称
		writer.writeObject(name);
		writer.writeString(tooltip);
		// 保存子类参数信息
		buildSuffix(writer);
		// 返回长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析数据类型、参数标题、数据值
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 数据类型
		setFamily(reader.read());
		select = reader.readBoolean();
		enabled = reader.readBoolean();
		// 标题名称
		name = new Naming(reader);
		tooltip = reader.readString();
		// 解析参数
		resolveSuffix(reader);
		// 返回解析长度
		return reader.getSeek() - seek;
	}

	/**
	 * 调用子类实现接口，生成一个它的副本
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
		if (that == null || !Laxkit.isClassFrom(that, InputParameter.class)) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较一致
		return compareTo((InputParameter) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return family ^ name.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(InputParameter that) {
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(family, that.family);
		if (ret == 0) {
			ret = Laxkit.compareTo(name, that.name);
		}
		return ret;
	}

	/**
	 * 子类生成自己实例的数据副本
	 * @return BootParameter子类实例
	 */
	public abstract InputParameter duplicate();

	/**
	 * 将子类的参数写入可类化写入器
	 * @param writer 可类化写入器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化读取器中解析子类的参数
	 * @param reader 可类化读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);
}