/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote;

import com.laxcus.log.client.*;
import com.laxcus.util.classable.*;

/**
 * 原生数据生成器 <br><br>
 * 
 * 由客户端生成，包含向服务器请求的接口名称、方法名称、方法参数类型、方法中的参数值。以字节数组方式输出，通过网络传递给服务端，被服务端接收后解析，做为数据计算的依据，执行RPC操作。
 * 
 * @author scott.liang
 * @version 1.0 11/07/2015
 * @since laxcus 1.0
 */
public final class PatternConstructor extends PatternFormatter {

	private static final long serialVersionUID = 257288783305590324L;

	/** 调用的接口名 **/
	private String interfaceName;

	/** 接口中的方法名 **/
	private String methodName;

	/** 方法中的参数类数组 **/
	private Class<?>[] parameterTypes;

	/** 方法中的实际参数值 **/
	private Object[] params;

	/**
	 * 将请求参数写入可类化存储器
	 * @param writer 可类化存储器
	 * @return 返回写入的字节长度
	 */
	public int build(ClassWriter writer) {
		final int scale = writer.size();
		// 调用的接口名称
		writer.writeString(interfaceName);
		// 接口中的方法名称
		writer.writeString(methodName);
		// 参数类型
		int size = (parameterTypes == null ? 0 : parameterTypes.length);
		// 参数类成员数目
		writer.writeInt(size);
		// 逐一拆解和保存
		for (int index = 0; index < size; index++) {
			// 判断是原生类型
			boolean primitive = parameterTypes[index].isPrimitive();
			// 原生class类名，或者其它的全路径class类名
			String clazz = parameterTypes[index].getName();
			// 写入参数
			writer.writeBoolean(primitive);
			writer.writeString(clazz);
		}
		// 参数
		byte[] b = writeAllObjects(params);
		writer.write(b);
		// 返回解析的字节长度
		return writer.size() - scale;
	}

	/**
	 * 判断类型匹配
	 * @param clazz
	 * @param name
	 * @return
	 */
	private boolean match(Class<?> clazz, String name) {
		return clazz.getName().equals(name);
	}

	/**
	 * 根据字符串，返回CLASS
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<?> readPrimitiveClass(String name) throws ClassNotFoundException {
		if (match(Boolean.TYPE, name)) {
			return Boolean.TYPE;
		} else if (match(Character.TYPE, name)) {
			return Character.TYPE;
		} else if (match(Byte.TYPE, name)) {
			return Byte.TYPE;
		} else if (match(Short.TYPE, name)) {
			return Short.TYPE;
		} else if (match(Integer.TYPE, name)) {
			return Integer.TYPE;
		} else if (match(Long.TYPE, name)) {
			return Long.TYPE;
		} else if (match(Float.TYPE, name)) {
			return Float.TYPE;
		} else if (match(Double.TYPE, name)) {
			return Double.TYPE;
		} else if (match(Void.TYPE, name)) {
			return Void.TYPE;
		}
		// 没有找到，弹出异常
		throw new ClassNotFoundException(name);
	}

	/**
	 * 从可类化读取器中解析RPC请求参数
	 * @param reader 可类化读取器
	 * @return 返回解析的字节长度
	 */
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 接口名称
		interfaceName = reader.readString();
		// 方法名称
		methodName = reader.readString();
		// 参数类型(允许0数组)
		int size = reader.readInt();
		parameterTypes = new Class<?>[size];
		for (int index = 0; index < size; index++) {
			// 读出参数
			boolean primitive = reader.readBoolean();
			String clazz = reader.readString();
			try {
				if(primitive) {
					parameterTypes[index] = readPrimitiveClass(clazz);
				} else {
					parameterTypes[index] = Class.forName(clazz);
				}
			} catch (ClassNotFoundException e) {
				Logger.error(e);
			}
		}
		// 读全部参数
		params = readAllObjects(reader);
		// 返回解析的长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造默认的原生数据生成器
	 */
	public PatternConstructor() {
		super();
	}

	/**
	 * 构造原生数据生成器，指定参数
	 * @param params 实际参数值数组
	 */
	public PatternConstructor(Object[] params) {
		this();
		this.params = params;
	}

	/**
	 * 构造原生数据生成器，指定参数
	 * @param param 实际参数值
	 */
	public PatternConstructor(Object param) {
		this(new Object[] { param });
	}

	/**
	 * 构造原生数据生成器，指定全部参数
	 * @param interName 调用的接口名
	 * @param methodName 接口中的方法名
	 * @param types 方法中的参数类型数组
	 * @param params 方法中的参数值
	 */
	public PatternConstructor(String interName, String methodName, Class<?>[] types, Object[] params) {
		this();
		this.interfaceName = interName;
		this.methodName = methodName;
		this.parameterTypes = types;
		this.params = params;
	}

	/**
	 * 从可类化读取器中解析原生数据生成器参数
	 * @param reader 可类化读取器
	 */
	public PatternConstructor(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从字节数组中解析原生数据生成器参数
	 * @param b  字节数组
	 * @param off 数据开始下标
	 * @param len 有效数据长度
	 */
	public PatternConstructor(byte[] b, int off, int len) {
		this(new ClassReader(b, off, len));
	}

	/**
	 * 返回接口类名称
	 * @return 调用的接口类名称
	 */
	public String getInterfaceName() {
		return this.interfaceName;
	}

	/**
	 * 返回接口中的方法名
	 * @return 接口中的方法名
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/**
	 * 返回参数类型
	 * @return 接口中的参数类型数组
	 */
	public Class<?>[] getParameterTypes() {
		return this.parameterTypes;
	}

	/**
	 * 返回参数集合
	 * @return 接口的参数数组
	 */
	public Object[] getParameters() {
		return this.params;
	}

	/**
	 * 将原生数据生成器生成字节数组和输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 将一个对象生成字节数组和输出
	 * @param param 参数对象
	 * @return 生成后的字节数组
	 */
	public static byte[] build(Object param) {
		PatternConstructor creator = new PatternConstructor(param);
		return creator.build();
	}

	/**
	 * 将一组对象生成字节数组和输出
	 * @param params 参数对象数组
	 * @return 生成后的字节数组
	 */
	public static byte[] build(Object[] params) {
		PatternConstructor apply = new PatternConstructor(params);
		return apply.build();
	}

	/**
	 * 从字节数组中解析原生数据生成器
	 * @param b  字节数组
	 * @param off 数据开始下标
	 * @param len 有效数据长度
	 * @return 返回PatternCreator实例
	 */
	public static PatternConstructor resolve(byte[] b, int off, int len) {
		return new PatternConstructor(b, off, len);
	}

	/**
	 * 从字节数组中解析原生数据生成器
	 * @param b  字节数组
	 * @return 返回PatternCreator实例
	 */
	public static PatternConstructor resolve(byte[] b) {
		return new PatternConstructor(b, 0, b.length);
	}

}