/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.markable;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 标记化参数记录器。<br>
 * 
 * 保存“类编号->类名称”和“变量编号->变量名称”的映像关系。
 * 
 * @author scott.liang
 * @version 1.0 11/7/2017
 * @since laxcus 1.0
 */
public class MarkRecorder implements Classable, Serializable {

	private static final long serialVersionUID = 6197681973175707388L;

	/** 无效值 **/
	public static final short INVALID = 0;
	
	/** 最大限制值 **/
	private final static short MAX_INDEX = java.lang.Short.MAX_VALUE;

	/** 参数序列索引号，从1开始增加。可以确定参数数量不会超过 0x7FFF **/
	private volatile short paramIndex = 1;

	/** 类序列号，从1开始。可以确定实现Markable接口类不会超过 0x7FFF **/
	private volatile short classIndex = 1;

	/** 参数编号 -> 参数名称 **/
	private TreeMap<java.lang.Short, String> activeParams = new TreeMap<java.lang.Short, String>();

	/** 参数名称 -> 参数编号 **/
	private TreeMap<String, java.lang.Short> passiveParams = new TreeMap<String, java.lang.Short>();

	/** 类编号 -> 类名称 **/
	private TreeMap<java.lang.Short, String> activeClazzs = new TreeMap<java.lang.Short, String>();

	/** 类名称 -> 类编号 **/
	private TreeMap<String, java.lang.Short> passiveClazzs = new TreeMap<String, java.lang.Short>();

	/**
	 * 构造默认的标记化参数记录器
	 */
	public MarkRecorder() {
		super();
		paramIndex = 1;
		classIndex = 1;
	}

	/**
	 * 从可类化读取器中解析标记化参数记录器参数
	 * @param reader 可类化读取器
	 */
	public MarkRecorder(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int seek = writer.size();
		// 类编号和类名称
		writer.writeInt(activeClazzs.size());
		Iterator<Map.Entry<java.lang.Short, String>> iterator = activeClazzs.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<java.lang.Short, String> entry = iterator.next();
			short no = entry.getKey().shortValue();
			String name = entry.getValue();
			// 把类编号和类名称写入内存
			writer.writeShort(no);
			writer.writeString(name);
		}
		// 参数编号和参数名称
		writer.writeInt(activeParams.size());
		iterator = activeParams.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<java.lang.Short, String> entry = iterator.next();
			short no = entry.getKey().shortValue();
			String name = entry.getValue();
			// 把参数编号和参数名称写入内存
			writer.writeShort(no);
			writer.writeString(name);
		}
		// 返回写入的字节长度
		return writer.size() - seek;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 类名称数目，形成“类编号->类名称”的映射
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			short no = reader.readShort();
			String name = reader.readString();
			// 保存类编号和类名称
			loadClass(no, name);
			// 类编号定位到最大
			if (classIndex < no) {
				classIndex = no;
			}
		}
		// 参数名称数目
		size = reader.readInt();
		for (int i = 0; i < size; i++) {
			// 读出参数编号和参数名称
			short no = reader.readShort();
			String name = reader.readString();
			// 保存参数编号和参数名称
			loadParameter(no, name);
			// 参数编号定位到最大
			if (paramIndex < no) {
				paramIndex = no;
			}
		}
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}
	
	/**
	 * 输出标记化原始记录
	 * @return 返回字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return activeParams.isEmpty() && passiveParams.isEmpty()
				&& activeClazzs.isEmpty() && passiveClazzs.isEmpty();
	}

	/**
	 * 根据类编号查找类名称
	 * @param classId 类编号
	 * @return 类名称
	 */
	public String findClassName(short classId) {
		return activeClazzs.get(classId);
	}

	/**
	 * 根据类名称查找类编号
	 * @param name 类名称
	 * @return 类编号
	 */
	public java.lang.Short findClass(String name) {
		return passiveClazzs.get(name);
	}

	/**
	 * 根据类定义查找类编号
	 * @param clazz 类定义
	 * @return 类编号
	 */
	public java.lang.Short findClass(Class<?> clazz) {
		String name = clazz.getName();
		return findClass(name);
	}

	/**
	 * 根据类名称，判断类编号存在
	 * @param name 类名称
	 * @return 返回真或者假
	 */
	public boolean hasClass(String name) {
		return findClass(name) != null;
	}

	/**
	 * 根据类定义，判断类编号存在
	 * @param clazz 类定义
	 * @return 返回真或者假
	 */
	public boolean hasClass(Class<?> clazz) {
		return findClass(clazz) != null;
	}

	/**
	 * 根据参数编号查找参数名
	 * @param paramId 参数编号
	 * @return 参数名
	 */
	public String findParamName(short paramId) {
		return activeParams.get(paramId);
	}

	/**
	 * 根据参数名称查找参数编号
	 * @param name 参数名称
	 * @return 参数编号
	 */
	public java.lang.Short findParam(String name) {
		return passiveParams.get(name);
	}

	/**
	 * 根据参数名称，判断有参数编号
	 * @param name 参数名称
	 * @return 返回真或者假
	 */
	public boolean hasParam(String name) {
		return findParam(name) != null;
	}

	/**
	 * 保存类编号和类名称
	 * @param no 类编号
	 * @param name 类名称
	 */
	private void loadClass(short no, String name) {
		activeClazzs.put(no, name);
		passiveClazzs.put(name, no);
	}

	/**
	 * 保存一个类名称。<br>
	 * 形成“类编号 -> 类名称”和“类名称->类编号”的相互映射。
	 * 
	 * @param clazz 类定义
	 */
	private void loadClass(Class<?> clazz) {
		// 如果类定义存在，忽略它
		if (hasClass(clazz)) {
			return;
		}
		// 增1
		if (classIndex < MarkRecorder.MAX_INDEX) {
			short no = classIndex++;
			loadClass(no, clazz.getName());
		}
	}

	/**
	 * 保存参数编号和参数名称
	 * @param no 参数编号
	 * @param name 参数名称
	 */
	private void loadParameter(short no, String name) {
		activeParams.put(no, name);
		passiveParams.put(name, no);
	}

	/**
	 * 保存一个参数名称。<br>
	 * 形成 “参数编号->参数名称”和“参数名称->参数编号”的相互映射。
	 * 
	 * @param name 参数名称
	 */
	private void loadParameter(String name) {
		// 参数名称存在，忽略退出
		if (hasParam(name)) {
			return;
		}
		// 编号增1
		if (paramIndex < MarkRecorder.MAX_INDEX) {
			short no = paramIndex++;
			loadParameter(no, name);
		}
	}

	/**
	 * 从类定义中取出参数名称，加载到内存中
	 * @param clazz 类定义
	 */
	private void loadParameter(Class<?> clazz) {
		// 如果是根对象，忽略它
		if (clazz == java.lang.Object.class) {
			return;
		}

		// 取上一级对象
		Class<?> superClazz = clazz.getSuperclass();
		// 递归写入上一级对象
		if (superClazz != null) {
			loadParameter(superClazz);
		}

		// 取出本级单元
		Field[] fields = clazz.getDeclaredFields();
		// 将标记化对象中的参数取出，写入缓存
		for (Field field : fields) {
			// 如果是不支持的修饰符，忽略它
			if (!MarkModifier.allow(field)) {
				continue;
			}

			// 保存参数
			loadParameter(field.getName());
			// 保存这个类定义和类中参数
			load(field.getDeclaringClass());
		}
	}

	/**
	 * 判断传入的类已经按照规定实现了Markable接口 <br><br>
	 * 
	 * 规则定义：<br>
	 * 1. 类声明实现“Markable”接口。<br>
	 * 2. 有公共空构造方法，或者公共构造方法是 xxx(MarkReader reader)。<br>
	 * 
	 * @param clazz 类定义
	 * @return 继承返回真，否则假
	 */
	private boolean isMarkable(java.lang.Class<?> clazz) {
		// 如果没有继承Markable接口，忽略它
		if (!Laxkit.isInterfaceFrom(clazz, Markable.class)) {
			return false;
		}

		// 枚举所有公共构造方法，检查有公有构造函数，或者构造函数指定了MarkReader参数
		Constructor<?>[] heads = clazz.getConstructors();
		int count = 0; // 统计符合要求的次数
		for (int i = 0; i < heads.length; i++) {
			// 提取构造方法中的参数
			Class<?>[] types = heads[i].getParameterTypes();
			// 有空的公有构造函数，或者构造方法传入“MarkReader”类
			if (types.length == 0) {
				count++;
			} else if (types.length == 1 && types[0] == MarkReader.class) {
				count++;
			}
		}
		// 返回真或者假
		return (count > 0);
	}

	/**
	 * 加载类定义。<br>
	 * 加载的前提条件是继承Markable接口，并且内存没有保存它。
	 * 
	 * @param clazz 类定义
	 * @return 加载返回真，否则假
	 */
	public boolean load(Class<?> clazz) {
		// 如果继承自Markable接口，且没有保存这个类和参数时，保存类和类参数
		boolean success = (isMarkable(clazz) && !hasClass(clazz));
		// 加载它
		if (success) {
			// 加载类名称
			loadClass(clazz);
			// 加载变量名
			loadParameter(clazz);
		}

		return success;
	}

}