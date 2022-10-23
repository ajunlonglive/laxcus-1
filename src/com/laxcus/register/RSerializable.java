/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.register;

import java.io.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 串行化实例对象参数 <br>
 * 
 * @author scott.liang
 * @version 1.1 7/15/2021
 * @since laxcus 1.0
 */
public final class RSerializable extends RParameter {

	/** 串行化实例对象 **/
	private Serializable value;

	/**
	 * 根据传入的对象实例参数，生成它的副本
	 * @param that RSerializable实例
	 */
	private RSerializable(RSerializable that) {
		super(that);
		value = that.value;
	}

	/**
	 * 建立一个默认的串行化对象参数
	 */
	public RSerializable() {
		super(RParameterType.SERIALABLE);
	}

	/**
	 * 建立一个默认的串行化对象参数，同时指定名称和对象值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RSerializable(String name, Serializable value) {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * 建立一个默认的串行化对象参数，同时指定名称和对象值
	 * @param name 名称
	 * @param value 参数值
	 */
	public RSerializable(Naming name, Serializable value) {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * 从可类化读取器中解析串行参数
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public RSerializable(ClassReader reader) {
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
	 * @see com.laxcus.distribute.value.RParameter#duplicate()
	 */
	@Override
	public RSerializable duplicate() {
		return new RSerializable(this);
	}

	/**
	 * 将串行化对象写入可类化写入器
	 * @see com.laxcus.distribute.parameter.RParameter#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		try {
			ByteArrayOutputStream buff = new ByteArrayOutputStream(10240);
			ObjectOutputStream s = new ObjectOutputStream(buff);
			s.writeObject(value);
			s.flush(); s.close();

			// 写入可类化写入器
			byte[] b = buff.toByteArray();
			super.buildSuffix(writer); writer.writeInt(b.length);
			super.buildSuffix(writer); writer.write(b);
		} catch (IOException e) {
			Logger.error(e);
		} catch (Throwable e) {
			Logger.error(e);
		}
	}

	/**
	 * 从可类化读取器中解析串行化对象
	 * @see com.laxcus.distribute.parameter.RParameter#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		
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