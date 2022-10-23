/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.parameter;

import java.io.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 串行化实例对象参数 <br>
 * 
 * @author scott.liang
 * @version 1.1 5/13/2015
 * @since laxcus 1.0
 */
public final class TaskSerializable extends TaskParameter {

	private static final long serialVersionUID = 7629613805429826504L;

	/** 串行化实例对象 **/
	private Serializable value;

	/**
	 * 根据传入的对象实例参数，生成它的副本
	 * @param that TaskSerializable实例
	 */
	private TaskSerializable(TaskSerializable that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的串行化对象参数
	 */
	public TaskSerializable() {
		super(TaskParameterType.SERIALABLE);
	}

	/**
	 * 建立一个默认的串行化对象参数，同时指定名称和对象值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskSerializable(String title, Serializable value) {
		this();
		setName(title);
		setValue(value);
	}

	/**
	 * 建立一个默认的串行化对象参数，同时指定名称和对象值
	 * @param title 名称
	 * @param value 参数值
	 */
	public TaskSerializable(Naming title, Serializable value) {
		this();
		setName(title);
		setValue(value);
	}

	/**
	 * 从可类化读取器中解析串行参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public TaskSerializable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置串行化对象
	 * @param e 串行化对象
	 */
	public void setValue(Serializable e) {
		Laxkit.nullabled(e);

		value = e;
	}

	/**
	 * 返回串行化对象
	 * @return 串行化对象
	 */
	public Serializable getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.value.TaskParameter#duplicate()
	 */
	@Override
	public TaskSerializable duplicate() {
		return new TaskSerializable(this);
	}

	/**
	 * 将串行化对象写入可类化写入器
	 * @see com.laxcus.distribute.parameter.TaskParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		try {
			ByteArrayOutputStream buff = new ByteArrayOutputStream(10240);
			ObjectOutputStream s = new ObjectOutputStream(buff);
			s.writeObject(value);
			s.flush(); s.close();

			// 写入可类化写入器
			byte[] b = buff.toByteArray();
			writer.writeInt(b.length);
			writer.write(b);
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.error(e);
		}
	}

	/**
	 * 从可类化读取器中解析串行化对象
	 * @see com.laxcus.distribute.parameter.TaskParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		byte[] b = reader.read(size);

		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(b);
			ObjectInputStream in = new ObjectInputStream(bin);
			value = (Serializable) in.readObject();
			in.close();
		} catch (IOException e) {
			Logger.error(e);
		} catch (ClassNotFoundException e) {
			Logger.error(e);
		}
	}

}