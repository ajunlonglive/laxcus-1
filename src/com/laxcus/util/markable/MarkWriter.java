/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.markable;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 标记化写入器 <br>
 * 将标记化对象转为字节数组保存
 * 
 * @author scott.liang
 * @version 1.0 8/22/2017
 * @since laxcus 1.0
 */
public final class MarkWriter {
	
	/** 默认10K **/
	private static final int defaultSize = 10240;

	/** 标记参数记录器 **/
	private MarkRecorder recorder;

	/** 字节缓存 **/
	private byte[] buff;

	/** 当前缓存下标位置，随缓存数据增加同步移动 **/
	private int seek;

	/**
	 * 设置标记化记录器
	 * @param e 标记化记录器
	 */
	private void setRecorder(MarkRecorder e) {
		Laxkit.nullabled(e);
		recorder = e;
	}
	
	/**
	 * 返回标记化记录器
	 * @return 标记化记录器
	 */
	public MarkRecorder getRecorder() {
		return recorder;
	}

	/**
	 * 建立标记化写入器，同时指定它的缓冲尺寸。
	 * @param size 字节缓冲长度
	 */
	private MarkWriter(int size) {
		super();
		if (size < 1) {
			throw new IllegalArgumentException("illegal size:" + size);
		}
		buff = new byte[size];
		seek = 0;
	}

	/**
	 * 构造标记化写入器，指定标记化记录器。缓冲尺寸是10K。
	 * @param recorder 标记化记录器
	 */
	public MarkWriter(MarkRecorder recorder) {
		this(defaultSize);
		setRecorder(recorder);
	}
	
	/**
	 * 构造标记化写入器，指定标记化记录器和内存尺寸
	 * @param recorder 标记化记录器
	 * @param size 内存长度
	 */
	public MarkWriter(MarkRecorder recorder, int size) {
		this(size);
		setRecorder(recorder);
	}
	
	/**
	 * 建立默认的标记化写入器。缓冲尺寸是10K字节
	 */
	public MarkWriter() {
		this(new MarkRecorder());
	}

	/**
	 * 重置字节数组缓存为空状态
	 */
	public void reset() {
		seek = 0;
	}

	/**
	 * 字节数组缓存的有效尺寸
	 * @return 有效尺寸
	 */
	public int size() {
		return seek;
	}

	/**
	 * 向缓存写入一段字节数组。
	 * 本处是基本的字节数组写入，所有写操作的调用它来完成。
	 * @param b 字节数组句柄
	 * @param off 下标开始位置
	 * @param len 字节数组长度
	 * @throws IndexOutOfBoundsException
	 */
	private void write(byte[] b, int off, int len) {
		if (off < 0 || len < 1 || off > b.length || (off + len > b.length)
				|| (off + len < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}

		int count = seek + len;
		if (count > buff.length) {
			buff = Arrays.copyOf(buff, Math.max(buff.length << 1, count));
		}
		System.arraycopy(b, off, buff, seek, len);
		seek = count;
	}

	/**
	 * 向缓冲区写入一个完整的字节数组
	 * @param b 字节数组
	 */
	private void write(byte[] b) {
		write(b, 0, b.length);
	}

	/**
	 * 写入一个整型
	 * @param value 整型
	 */
	public void writeInt(int value) {
		byte[] b = Laxkit.toBytes(value);
		write(b);
	}

	/**
	 * 建立标记化标识
	 * @param paramName 变量名称
	 * @param family 对象类型
	 * @param value 对象实例
	 * @return 返回标记化标识
	 */
	private MarkFlag createMarkTag(String paramName, byte family, Object value) {
		// 如果是标记化类型，需要指定类名称；非标记化类型，不需要指定类名称
		if (MarkType.isMarkable(family)) {
			return new MarkFlag(recorder, paramName, family, value.getClass());
		} else {
			return new MarkFlag(recorder, paramName, family);
		}
	}

	/**
	 * 建立内部标识
	 * @param family 对象类型
	 * @param value 对象实例
	 * @return 返回标记化标识
	 */
	private ClassFlag createClassTag(byte family, Object value) {
		// 如果是标记化类型，需要指定类名称；非标记化类型，不需要指定类名称
		if (MarkType.isMarkable(family)) {
			return new ClassFlag(recorder, family, value.getClass());
		} else {
			return new ClassFlag(recorder, family);
		}
	}

	/**
	 * 写入一个字节
	 * @param b 字节
	 * @return 返回写入的字节数组
	 */
	private byte[] writeByte(byte b) {
		ClassWriter writer = new ClassWriter();
		writer.write(b);
		return writer.effuse();
	}

	/**
	 * 写入一个布符值
	 * @param b 布尔值
	 * @return 返回写入的字节数组
	 */
	private byte[] writeBoolean(boolean b){
		ClassWriter writer = new ClassWriter();
		writer.writeBoolean(b);
		return writer.effuse();
	}

	/**
	 * 写入一个字符
	 * @param ch 字符值
	 * @return 返回写入的字节数组
	 */
	private byte[] writeChar(char ch){
		ClassWriter writer = new ClassWriter();
		writer.writeChar(ch);
		return writer.effuse();
	}

	/**
	 * 写入一个短整数
	 * @param value 短整数值
	 * @return 返回写入的字节数组
	 */
	private byte[] writeShort(short value){
		ClassWriter writer = new ClassWriter();
		writer.writeShort(value);
		return writer.effuse();
	}

	/**
	 * 写入一个整数
	 * @param value 整数值
	 * @return 返回写入的字节数组
	 */
	private byte[] writeInteger(int value){
		ClassWriter writer = new ClassWriter();
		writer.writeInt(value);
		return writer.effuse();
	}

	/**
	 * 写入一个长整数
	 * @param value 长整数值
	 * @return 返回写入的字节数组
	 */
	private byte[] writeLong(long value){
		ClassWriter writer = new ClassWriter();
		writer.writeLong(value);
		return writer.effuse();
	}

	/**
	 * 写入一个单浮点
	 * @param value 单浮点值
	 * @return 返回写入的字节数组
	 */
	private byte[] writeFloat(float value){
		ClassWriter writer = new ClassWriter();
		writer.writeFloat(value);
		return writer.effuse();
	}

	/**
	 * 写入一个双浮点
	 * @param value 双浮点值
	 * @return 返回写入的字节数组
	 */
	private byte[] writeDouble(double value){
		ClassWriter writer = new ClassWriter();
		writer.writeDouble(value);
		return writer.effuse();
	}

	/**
	 * 写入一个字符串
	 * @param str 字符串
	 * @return 返回写入的字节数组
	 */
	private byte[] writeString(String str){
		ClassWriter writer = new ClassWriter();
		writer.writeString(str);
		return writer.effuse();
	}

	/**
	 * 写入字节数组
	 * @param b 字节数组
	 * @return 返回写入的字节数组
	 */
	private byte[] writeByteArray(byte[] b){
		ClassWriter writer = new ClassWriter();
		writer.writeByteArray(b);
		return writer.effuse();
	}

	/**
	 * 写入布尔数数组
	 * @param b 布尔数数组
	 * @return 返回写入的短整数数组
	 */
	private byte[] writeBooleanArray(boolean[] b){
		ClassWriter writer = new ClassWriter();
		writer.writeBooleanArray(b);
		return writer.effuse();
	}

	/**
	 * 写入字符数组
	 * @param b 字符数组
	 * @return 返回写入的字符数组
	 */
	private byte[] writeCharArray(char[] b){
		ClassWriter writer = new ClassWriter();
		writer.writeCharArray(b);
		return writer.effuse();
	}

	/**
	 * 写入短整数数组
	 * @param b 短整数数组
	 * @return 返回写入的短整数数组
	 */
	private byte[] writeShortArray(short[] b){
		ClassWriter writer = new ClassWriter();
		writer.writeShortArray(b);
		return writer.effuse();
	}

	/**
	 * 写入整数数组
	 * @param b 整数数组
	 * @return 返回写入的整数数组
	 */
	private byte[] writeIntegerArray(int[] b){
		ClassWriter writer = new ClassWriter();
		writer.writeIntArray(b);
		return writer.effuse();
	}

	/**
	 * 写入长整数数组
	 * @param b 长整数数组
	 * @return 返回写入的长整数数组
	 */
	private byte[] writeLongArray(long[] b) {
		ClassWriter writer = new ClassWriter();
		writer.writeLongArray(b);
		return writer.effuse();
	}

	/**
	 * 写入单浮点数组
	 * @param b 单浮点数组
	 * @return 返回写入的字节数组
	 */
	private byte[] writeFloatArray(float[] b) {
		ClassWriter writer = new ClassWriter();
		writer.writeFloatArray(b);
		return writer.effuse();
	}

	/**
	 * 写入双浮点数组
	 * @param b 双浮点数组
	 * @return 返回写入的字节数组
	 */
	private byte[] writeDoubleArray(double[] b){
		ClassWriter writer = new ClassWriter();
		writer.writeDoubleArray(b);
		return writer.effuse();
	}

	/**
	 * 写入字符串数组
	 * @param b 字符串数组
	 * @return 返回写入的字节数组
	 */
	private byte[] writeStringArray(String[] b) {
		ClassWriter writer = new ClassWriter();
		writer.writeStringArray(b);
		return writer.effuse();
	}

	/**
	 * 生成标记化字节数组
	 * @param value 标记化字节数组
	 * @return 字节数组
	 */
	private byte[] buildMarkable(Object value) {
		MarkWriter writer = new MarkWriter(recorder);
		writer.writeObject(value);
		return writer.toBody();
	}

	/**
	 * 写入标记化对象
	 * @param value 变量对象
	 * @return 输出生成标记化对象的字节数组
	 */
	private byte[] writeMarkable(Object value) {
		// 标记化字节数组
		byte[] b = buildMarkable(value);

		// 可类化写入器
		ClassWriter writer = new ClassWriter();
		// 长度和数据内容
		writer.writeInt(b.length);
		writer.write(b);
		// 输出结果
		return writer.effuse();
	}

	/**
	 * 生成串行化字节数组
	 * @param value 对象值
	 * @return 字节数组
	 * @throws MarkableException
	 */
	private byte[] buildSerialable(Object value) throws MarkableException{
		ByteArrayOutputStream buff = new ByteArrayOutputStream(10240);
		try {
			ObjectOutputStream e = new ObjectOutputStream(buff);
			e.writeObject(value);
			e.flush(); e.close();
		} catch (IOException e) {
			throw new MarkableException(e);
		} catch (Throwable e) {
			throw new MarkableException(e);
		}
		// 输出串行化字节数组
		return buff.toByteArray();
	}

	/**
	 * 写入串行化对象
	 * @param value 串行化对象
	 * @return 输出生成串行化对象的字节数组
	 * @throws MarkableException - 标记化异常
	 */
	private byte[] writeSerialable(Object value) throws MarkableException {
		// 生成串行串字节数组
		byte[] b = buildSerialable(value);

		// 写入参数
		ClassWriter writer = new ClassWriter();
		// 数据长度和内容
		writer.writeInt(b.length);
		writer.write(b);
		// 输出结果
		return writer.effuse();
	}

	/**
	 * 把树集合转换为字节数组后输出
	 * @param object TreeSet实例
	 * @return 输出树集的字节数组
	 */
	private byte[] writeTreeSet(Object object) {
		TreeSet<?> base = (TreeSet<?>) object;
		ClassWriter writer = new ClassWriter();
		// 确定成员数目
		writer.writeInt(base.size());

		// 把对象逐一转换和写入
		for (Object value : base) {
			// 确定类标识
			ClassFlag tag = createClassTag(MarkType.translate(value), value);
			// 把类转换为字节数组
			byte[] b = build(tag.getType(), value);
			// 写入类标记和类数据
			writer.writeObject(tag);
			writer.write(b);
		}

		// 输出字节数组
		byte[] b = writer.effuse();
		// 全部数据重新写入
		writer = new ClassWriter();
		// 数据长度和内容
		writer.writeInt(b.length);
		writer.write(b);

		// 输出结果
		return writer.effuse();
	}

	/**
	 * 写入二叉树集
	 * @param object TreeSet实例
	 * @return 输出写入的字节数组
	 */
	private byte[] writeTreeMap(Object object) {
		TreeMap<?, ?> base = (TreeMap<?, ?>) object;
		ClassWriter writer = new ClassWriter();
		writer.writeInt(base.size());


		Set<?> keys = base.keySet();
		for (Object key : keys) {
			Object value = base.get(key);

			// 确定类标识
			ClassFlag kTag = createClassTag(MarkType.translate(key), key);
			ClassFlag vTag = createClassTag(MarkType.translate(value), value);
			// 键域
			byte[] kArray = build(kTag.getType(), key);
			// 值域
			byte[] vArray = build(vTag.getType(), value);

			// 写入数据
			writer.writeObject(kTag);
			writer.writeObject(vTag);
			writer.write(kArray);
			writer.write(vArray);
		}

		// 输出字节数组
		byte[] b = writer.effuse();
		// 全部数据重新写入
		writer = new ClassWriter();
		// 数据长度和内容
		writer.writeInt(b.length);
		writer.write(b);
		// 输出结果
		return writer.effuse();
	}

	/**
	 * 把对象转为字节数组和输出
	 * @param object 数组对象
	 * @return 转换后的字节数组
	 */
	private byte[] writeArrayList(Object object) {
		ArrayList<?> base = (ArrayList<?>) object;
		ClassWriter writer = new ClassWriter();
		// 确定成员数目
		writer.writeInt(base.size());

		// 把对象逐一转换和写入
		for (Object value : base) {
			// 确定类标识
			ClassFlag tag = createClassTag(MarkType.translate(value), value);
			// 把类转换为字节数组
			byte[] b = build(tag.getType(), value);
			// 写入数据
			writer.writeObject(tag);
			writer.write(b);
		}

		// 输出字节数组
		byte[] b = writer.effuse();
		// 全部数据重新写入
		writer = new ClassWriter();
		// 数据长度和内容
		writer.writeInt(b.length);
		writer.write(b);

		// 输出结果
		return writer.effuse();
	}

	/**
	 * 根据类型，转换成对应的字节数组
	 * @param family 对象类型
	 * @param value 对象值
	 * @return 输出字节数组
	 */
	private byte[] build(byte family, Object value) {
		// 原生数据类型，转为字节数组
		if (MarkType.isByte(family)) {
			return writeByte((java.lang.Byte) value);
		} else if (MarkType.isBoolean(family)) {
			return writeBoolean((java.lang.Boolean) value);
		} else if (MarkType.isChar(family)) {
			return writeChar((java.lang.Character) value);
		} else if (MarkType.isShort(family)) {
			return writeShort((java.lang.Short) value);
		} else if (MarkType.isInteger(family)) {
			return writeInteger((java.lang.Integer) value);
		} else if (MarkType.isLong(family)) {
			return writeLong((java.lang.Long) value);
		} else if (MarkType.isFloat(family)) {
			return writeFloat((java.lang.Float) value);
		} else if (MarkType.isDouble(family)) {
			return writeDouble((java.lang.Double) value);
		}
		// 原生类型数组
		else if (MarkType.isByteArray(family)) {
			return writeByteArray((byte[]) value);
		} else if (MarkType.isBooleanArray(family)) {
			return writeBooleanArray((boolean[]) value);
		} else if (MarkType.isCharArray(family)) {
			return writeCharArray((char[]) value);
		} else if (MarkType.isShortArray(family)) {
			return writeShortArray((short[]) value);
		} else if (MarkType.isIntegerArray(family)) {
			return writeIntegerArray((int[]) value);
		} else if (MarkType.isLongArray(family)) {
			return writeLongArray((long[]) value);
		} else if (MarkType.isFloat(family)) {
			return writeFloatArray((float[]) value);
		} else if (MarkType.isDoubleArray(family)) {
			return writeDoubleArray((double[]) value);
		}
		// 字符串和字节数组
		else if (MarkType.isString(family)) {
			return writeString((String) value);
		} else if (MarkType.isStringArray(family)) {
			return writeStringArray((String[]) value);
		}
		// 判断继承标记化，输出为字节数组
		else if (MarkType.isMarkable(family)) {
			return writeMarkable(value);
		}
		// 判断是树集
		else if (MarkType.isTreeSet(family)) {
			return writeTreeSet(value);
		}
		// 判断是二叉树映像
		else if (MarkType.isTreeMap(family)) {
			return writeTreeMap(value);
		}
		// 判断是数组列表
		else if(MarkType.isArrayList(family)) {
			return writeArrayList(value);
		}
		// 判断是串行化
		else if (MarkType.isSerialable(family)) {
			return writeSerialable(value);
		}

		// 以上不成立，弹出错误
		throw new MarkableException("cannot be markable: %d", family);
	}

	/**
	 * 把一个字段信息转为字节数组输出
	 * @param field 字节实例
	 * @param parent 根对象
	 * @return 返回字节数组，如果是非转换字段或者对象是空指针时，输出空指针
	 */
	private byte[] build(Field field, Object parent) {
		// 1. 判断对象是需要写入的，否则返回空指针
		if (!MarkModifier.allow(field)) {
			return null; // 不允许返回空指针
		}

		// 2. 取出对象变量
		Object value = null;
		try {
			field.setAccessible(true);  // 设置为可访问状态，取出参数
			value = field.get(parent);
			field.setAccessible(false); // 恢复为不可访问
		} catch (IllegalArgumentException e) {
			throw new MarkableException(e);
		} catch (IllegalAccessException e) {
			throw new MarkableException(e);
		} catch (SecurityException e) {
			throw new MarkableException(e);
		} 

		// 如果对象是空指针，忽略它
		if(value == null) {
			return null;
		}

		ClassWriter writer = new ClassWriter();
		// 转换类型
		byte family = MarkType.translate(field);
		// 生成标识符和标记化字节流
		MarkFlag tag = createMarkTag(field.getName(), family, value);
		byte[] stream = build(family, value);
		// 写入标记符。标记符和标记化字节流在一起
		writer.writeObject(tag);
		// 写入标记化字节流
		writer.write(stream);
		// 输出结果
		return writer.effuse();
	}

	/**
	 * 把对象中的参数写入可类化写入器
	 * @param clazz 当前类类型
	 * @param object 对象
	 * @param writer 可类化写入器
	 */
	private void writeObject(Class<?> clazz, Object object, ClassWriter writer) {
		// 如果是根对象，忽略它
		if (clazz == java.lang.Object.class) {
			return;
		}

		// 取上一级对象
		Class<?> superClazz = clazz.getSuperclass();
		// 递归写入上一级对象
		if(superClazz != null) {
			writeObject(superClazz, object, writer);
		}

		short count = 0;
		ClassWriter sub = new ClassWriter();
		// 取出本级单元
		Field[] fields = clazz.getDeclaredFields();
		// 将标记化对象中的参数取出，写入缓存
		for (Field field : fields) {
			byte[] b = build(field, object);
			// 忽略空指针
			if (b == null) {
				continue;
			}
			// 保存参数
			sub.write(b);
			count++;
		}

		// 变量数目和字节数组
		writer.writeShort(count);
		// 成员数目大于0才写入字节数组
		if (count > 0) {
			writer.write(sub.effuse());
		}
	}

	/**
	 * 将对象实例转为字节数组，写入缓存中
	 * 
	 * @param object 对象实现
	 * @return 返回生成的字节数组长度
	 * 
	 * @throws MarkableException - 如果不支持标记化或者串行化时，弹出异常
	 */
	public int writeObject(Object object) {
		ClassWriter writer = new ClassWriter(10240);

		Class<?> clazz = object.getClass();

		// 记录这个类的类名和变量名参数
		recorder.load(clazz);

		// 定位开始下标
		int pos = writer.size();
		// 写入对象
		writeObject(clazz, object, writer);

		// 输出本次的字节数组
		byte[] b = writer.effuse();

		// 写入长度前缀
		writeInt(b.length);
		// 写入字节数组
		write(b);
		// 返回写入的字节长度
		return size() - pos;
	}
	
	/**
	 * 输出头部段数据
	 * @return 当前头部的字节数组
	 */
	public byte[] toHead() {
		return recorder.build();
	}

	/**
	 * 输出内容段数据
	 * @return 当前字节数组的数据副本
	 */
	public byte[] toBody() {
		return Arrays.copyOf(buff, seek);
	}
	
	/**
	 * 输出总数据。由标记参数记录器的头部数据和内存数据组成。
	 * 这个方法通常只被最外层，需要写入磁盘或者传输给其他方法时才调用。
	 * 
	 * @return 全部数据的字节数组
	 */
	public byte[] toAll() {
		// 预定义空间尺寸
		ClassWriter writer = new ClassWriter(4096 + seek);
		// 头数据
		recorder.build(writer);
		// 内容部分
		if (seek > 0) {
			writer.write(buff, 0, seek);
		}
		// 输出结果
		return writer.effuse();
	}
}