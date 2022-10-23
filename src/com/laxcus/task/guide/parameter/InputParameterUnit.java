/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.guide.parameter;

import java.util.*;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;

/**
 * 启动参数单元 <br>
 * 
 * 对应一个阶段，有任意多个参数组成。
 * 
 * @author scott.liang
 * @version 1.0 7/25/2020
 * @since laxcus 1.0
 */
public final class InputParameterUnit implements Serializable, Cloneable, Classable, Markable, Comparable<InputParameterUnit> {

	private static final long serialVersionUID = -2782920028766869120L;

	/** 阶段类型，见PhaseTag中的定义 **/
	private int family;

	/** 全部参数 **/
	private ArrayList<InputParameter> array = new ArrayList<InputParameter>();

	/**
	 * 构造默认的启动参数单元
	 */
	public InputParameterUnit() {
		super();
		family = 0;
	}

	/**
	 * 构造默认的启动参数单元
	 * @param family 阶段类型
	 */
	public InputParameterUnit(int family) {
		this();
		setFamily(family);
	}

	/**
	 * 生成启动参数单元的数据副本
	 * @param that 传入类
	 */
	private InputParameterUnit(InputParameterUnit that){
		this();
		family = that.family;
		array.addAll(that.array);
	}

	/**
	 * 构造默认的启动参数单元
	 * @param reader 可类化读取器
	 */
	public InputParameterUnit(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置阶段类型，见PhaseTag中定义
	 * @param who 阶段类型
	 */
	public void setFamily(int who) {
		// 判断是指定的类型
		if (!PhaseTag.isPhase(who)) {
			throw new IllegalValueException("illegal type: %d", who);
		}
		family = who;
	}

	/**
	 * 返回阶段类型
	 * @return 整数
	 */
	public int getFamily() {
		return family;
	}

	/**
	 * 返回文本描述
	 * @return
	 */
	public String getFamilyText() {
		return PhaseTag.translate(family);
	}

	/**
	 * 保存一行参数
	 * @param e
	 * @return
	 */
	public boolean add(InputParameter e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 删除一行参数
	 * @param e
	 * @return
	 */
	public boolean remove(InputParameter e) {
		Laxkit.nullabled(e);
		return array.remove(e);
	}
	
	/**
	 * 返回基于某个名称的参数集合
	 * @param name 参数名称
	 * @return 返回InputParameter列表
	 */
	public List<InputParameter> get(Naming name) {
		ArrayList<InputParameter> a = new ArrayList<InputParameter>();
		for (InputParameter e : array) {
			boolean success = (Laxkit.compareTo(e.getName(), name) == 0);
			if (success) {
				a.add(e);
			}
		}
		return a;
	}

	/**
	 * 返回基于某个名称的参数集合
	 * @param name 参数名称
	 * @return 返回InputParameter列表
	 */
	public List<InputParameter> get(String name) {
		return get(new Naming(name));
	}

	/**
	 * 返回基于某个名称和下标的参数
	 * @param name 参数名称
	 * @param index 下标位置
	 * @return 返回InputParameter实例，或者空指针
	 */
	public InputParameter find(Naming name, int index) {
		List<InputParameter> a = get(name);
		// 在范围内
		if (a.size() > 0 && index < a.size()) {
			return a.get(index);
		}
		return null;
	}

	/**
	 * 返回基于某个名称和下标的参数
	 * @param name 参数名称
	 * @param index 下标位置
	 * @return 返回InputParameter实例，或者空指针
	 */
	public InputParameter find(String name, int index) {
		return find(new Naming(name), index);
	}

	/**
	 * 返回基于某个名称和0下标的参数
	 * @param name 参数名称
	 * @return 返回InputParameter实例，或者空指针
	 */
	public InputParameter find(Naming name) {
		return find(name, 0);
	}

	/**
	 * 返回基于某个名称和0下标的参数
	 * @param name 参数名称
	 * @return 返回InputParameter实例，或者空指针
	 */
	public InputParameter find(String name) {
		return find(name, 0);
	}

	/**
	 * 输出全部参数
	 * @return BootParameter集合
	 */
	public List<InputParameter> list() {
		return new ArrayList<InputParameter>(array);
	}

	/**
	 * 判断成员数目
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 生成副本
	 * @return
	 */
	public InputParameterUnit duplicate() {
		return new InputParameterUnit(this);
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
	 * 将数据类型，标题、数据值输出到可类化写入器
	 * @param writer 可类化写入器
	 * @return 返回写入的字节长度
	 */
	@Override
	public int build(ClassWriter writer) {
		writer.writeInt(family);
		int size = writer.size();
		writer.writeInt(array.size());
		for (InputParameter e : array) {
			writer.writeObject(e);
		}
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

		family = reader.readInt();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			// 读取类型
			byte type = reader.current();
			// 生成默认实例，读取参数
			InputParameter param = InputParameterCreator.createDefault(type);
			param.resolve(reader);
			array.add(param);
		}

		// 返回解析长度
		return reader.getSeek() - seek;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return PhaseTag.translate(family);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return family;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(InputParameterUnit that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(family, that.family);
	}

}
