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

/**
 * 启动参数集合
 * 
 * @author scott.liang
 * @version 1.0 7/27/2020
 * @since laxcus 1.0
 */
public final class InputParameterList implements Serializable, Cloneable, Classable, Markable {

	private static final long serialVersionUID = 6053697774073574422L;

	/** 全部参数 **/
	private TreeSet<InputParameterUnit> array = new TreeSet<InputParameterUnit>();

	/**
	 * 构造默认的启动参数集合
	 */
	public InputParameterList() {
		super();
	}

	/**
	 * 生成启动参数集合的数据副本
	 * @param that 传入类
	 */
	public InputParameterList(InputParameterList that){
		this();
		array.addAll(that.array);
	}

	/**
	 * 保存启动参数单元
	 * @param e
	 * @return
	 */
	public boolean add(InputParameterUnit e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}

	/**
	 * 删除启动参数单元
	 * @param e
	 * @return
	 */
	public boolean remove(InputParameterUnit e) {
		Laxkit.nullabled(e);
		return array.remove(e);
	}

	/**
	 * 输出全部参数
	 * @return BootParameterTable集合
	 */
	public List<InputParameterUnit> list() {
		return new ArrayList<InputParameterUnit>(array);
	}

	/**
	 * 根据阶段类型，查找匹配的启动参数单元
	 * @param family 阶段类型
	 * @return 返回InputParameterUnit实例
	 */
	public InputParameterUnit find(int family) {
		for (InputParameterUnit e : array) {
			if (e.getFamily() == family) {
				return e;
			}
		}
		return null;
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
	public InputParameterList duplicate() {
		return new InputParameterList(this);
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
		int size = writer.size();
		
		writer.writeInt(array.size());
		for (InputParameterUnit e : array) {
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

		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			// 生成默认实例，读取参数
			InputParameterUnit table = new InputParameterUnit(reader);
			array.add(table);
		}

		// 返回解析长度
		return reader.getSeek() - seek;
	}

}