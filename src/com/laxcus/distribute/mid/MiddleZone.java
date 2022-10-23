/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.mid;

import java.util.*;
import java.io.*;

import com.laxcus.distribute.parameter.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 中间数据的元数据段 <br><br>
 * 
 * 元数据段在分布处理过程中产生，描述分布处理过程的工作状态，被传输到各个站点。
 * 在“MiddleZone”中，提供自定义参数存取操作。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public abstract class MiddleZone implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -7877596371589610727L;

	/** 自定义参数集合 **/
	private ArrayList<TaskParameter> array = new ArrayList<TaskParameter>();

	/**
	 * 构造一个默认的元数据段
	 */
	protected MiddleZone() {
		super();
	}

	/**
	 * 根据传入的元数据段参数，生成它的数据副本
	 * @param that 元数据段
	 */
	protected MiddleZone(MiddleZone that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 保存自定义参数
	 * @param e 自定义参数
	 * @return 成功返回真，否则假
	 */
	public boolean addValue(TaskParameter e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}
	
	/**
	 * 保存一组自定义参数
	 * @param a 自定义值数组
	 * @return 返回新增加的数值
	 */
	public int addValues(Collection<TaskParameter> a) {
		int size = array.size();
		for (TaskParameter e : a) {
			addValue(e);
		}
		return array.size() - size;
	}

	/**
	 * 返回自定义参数列表
	 * @return TaskParameter列表
	 */
	public List<TaskParameter> getParameters() {
		return new ArrayList<TaskParameter>(array);
	}

	/**
	 * 返回指定下标的自定义参数
	 * @param index 基于0下标
	 * @return 自定义参数实例
	 */
	public TaskParameter getParameter(int index) {
		if (index < 0 || index >= array.size()) {
			throw new IndexOutOfBoundsException();
		}
		return array.get(index);
	}
	
	/**
	 * 指定标题和这个标题的下标，删除一个自定义参数
	 * @param title 标题
	 * @param index 基于这个标题的下标
	 * @return 自定义参数实例
	 */
	public TaskParameter removeParameter(Naming title, int index) {
		// 用户的错误
		if (index < 0) {
			throw new ArrayIndexOutOfBoundsException();
		}

		int seek = 0;
		for (int i = 0; i < array.size(); i++) {
			TaskParameter value = array.get(i);
			if (value.getName().compareTo(title) == 0) {
				if (seek == index) {
					array.remove(i);
					return value;
				}
				seek++;
			}
		}
		return null;
	}

	/**
	 * 删除基于标题0下标的自定义参数
	 * @param title 标题
	 * @return 自定义参数实例
	 */
	public TaskParameter removeParameter(Naming title) {
		return removeParameter(title, 0);
	}

	/**
	 * 删除基于标题0下标的自定义参数
	 * @param title 标题
	 * @return 自定义参数实例
	 */
	public TaskParameter removeParameter(String title) {
		return removeParameter(new Naming(title));
	}
	
	/**
	 * 查找在指定标题和下标位置的自定义参数
	 * @param title 标题
	 * @param index 基于这个标题的下标
	 * @return 自定义参数实例
	 */
	public TaskParameter findParameter(Naming title, int index) {
		// 用户的错误
		if (index < 0) {
			throw new ArrayIndexOutOfBoundsException();
		}

		int seek = 0;
		for (TaskParameter value : this.array) {
			if (value.getName().compareTo(title) == 0) {
				if (seek == index) {
					return value;
				}
				seek++;
			}
		}
		return null;
	}

	/**
	 * 查找在指定命名和下标位置的自定义参数
	 * @param title 标题
	 * @param index 基于这个标题的下标
	 * @return 自定义参数实例
	 */
	public TaskParameter findParameter(String title, int index) {
		return this.findParameter(new Naming(title), index);
	}

	/**
	 * 查找基于0下标标题的自定义参数
	 * @param title 标题
	 * @return 自定义参数实例
	 */
	public TaskParameter findParameter(Naming title) {
		return this.findParameter(title, 0);
	}

	/**
	 * 查找基于0下标标题的自定义参数
	 * @param title 标题
	 * @return 自定义参数实例
	 */
	public TaskParameter findParameter(String title) {
		return this.findParameter(new Naming(title));
	}

	/**
	 * 统计自定义参数值数目
	 * @return 自定义参数值数目
	 */
	public int getParameterCount() {
		return array.size();
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
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 保存自定义参数
		writer.writeInt(array.size());
		for (TaskParameter value : array) {
			writer.writeObject(value);
		}
		// 写入子类参数
		buildSuffix(writer);
		// 写入字节长度
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 解析自定义参数
		int size = reader.readInt();
		array.ensureCapacity(size);
		for (int i = 0; i < size; i++) {
			TaskParameter value = TaskParameterCreator.split(reader);
			array.add(value);
		}
		// 读出子类参数
		resolveSuffix(reader);
		// 读取字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 生成数据流，返回数据流字节数组
	 * @return
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 解析数据流
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 返回解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}
	
	/**
	 * 子类实例生成自己的数据副本
	 * @return MiddleZone子类实例
	 */
	public abstract MiddleZone duplicate() ;

	/**
	 * 向可类化存储器写入子类数据信息
	 * @param writer 可类化存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化读取器中读出子类数据信息
	 * @param reader 可类化读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader); 
}