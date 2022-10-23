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
 * 标记化读取器 <br>
 * 从字节数组中读取参数，保存到实例里
 * 
 * @author scott.liang
 * @version 1.0 10/12/2017
 * @since laxcus 1.0
 */
public final class MarkReader {

	/** 标记参数记录器 **/
	private MarkRecorder recorder;

	/** 字节缓冲数组 **/
	private byte[] buff = null;

	/** 数据的开始位置、当前下标位置、结束位置 **/
	private int begin = 0;

	private int seek = 0;

	private int end = 0;

	/**
	 * 设置标记参数记录器，不允许空指针
	 * @param e 标记参数记录器
	 */
	private void setRecorder(MarkRecorder e) {
		Laxkit.nullabled(e);
		recorder = e;
	}
	
	/**
	 * 返回标记参数记录器
	 * @return 标记参数记录器
	 */
	public MarkRecorder getRecorder() {
		return recorder;
	}

	/**
	 * 构造标记化数据读取器，指定它绑定的字节数组
	 * @param recorder 标记化记录器
	 * @param b 标记化内容段字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 */
	public MarkReader(MarkRecorder recorder, byte[] b, int off, int len) {
		super();
		setRecorder(recorder);
		attach(b, off, len);
	}

	/**
	 * 构造一个标记化数据读取器，指定它绑字的字节数组
	 * @param recorder 标记化记录器
	 * @param b 标记化内容段字节数组
	 */
	public MarkReader(MarkRecorder recorder, byte[] b) {
		this(recorder, b, 0, b.length);
	}

	/**
	 * 构造标记化数据读取器，指定标记化记录器和可类化读取器
	 * @param recorder 标记化记录器
	 * @param reader 可类化读取器，保存标记化内容段数据
	 */
	public MarkReader(MarkRecorder recorder, ClassReader reader) {
		super();
		// 设置头部的标记化记录器
		setRecorder(recorder);
		// 绑定内容段
		attach(reader);
	}

	/**
	 * 构造标记化数据读取器，从磁盘文件中解析全部数据。<br>
	 * 这个构造函数是MarkWriter.flush的反向操作。
	 * @param file 磁盘文件中完整记录
	 * @throws IOException - 磁盘IO异常
	 */
	public MarkReader(File file) throws IOException {
		super();
		ClassReader reader = new ClassReader(file);
		// 从文件中读出头部数据
		recorder = new MarkRecorder(reader);
		// 绑定内容段
		attach(reader);
	}

	/**
	 * 构造标记化数据读取器，从字节数组中读取全部参数，包括做为头部数据的标记化参数记录器和内容段数据。
	 * 这个构造函数是MarkWriter.flush的反向操作。
	 * @param b 完整的字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 */
	public MarkReader(byte[] b, int off, int len) {
		super();
		ClassReader reader = new ClassReader(b, off, len);
		// 从文件中读出头部数据
		recorder = new MarkRecorder(reader);
		// 绑定内容段
		attach(reader);
	}

	/**
	 * 构造标记化数据读取器，从字节数组中读取全部参数，包括做为头部数据的标记化参数记录器和内容段数据。
	 * 这个构造函数是MarkWriter.flush的反向操作。
	 * @param b 完整的字节数组
	 */
	public MarkReader(byte[] b) {
		this(b, 0, b.length);
	}

	/**
	 * 关联一个字节数组对象，同时与之前的数组对象解除关联，所有参数将重新设置。
	 * @param b 新的字节数组对象
	 * @param off 开始下标，最小下标是0。
	 * @param len 有效字节长度，必须大于0。
	 */
	private void attach(byte[] b, int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 1 || off > b.length
				|| (off + len > b.length) || (off + len < 0)) {
			throw new IndexOutOfBoundsException();
		}
		buff = b;
		begin = seek = off;
		end = off + len;
	}

	/**
	 * 关联一个字节数组对象
	 * @param b 字节数组
	 */
	private void attach(byte[] b) {
		attach(b, 0, b.length);
	}

	/**
	 * 从可类化读取中剩余参数，关联一个字节数组对象
	 * @param reader 可类化数据读取器
	 */
	private void attach(ClassReader reader) {
		// 取出剩余参数
		int left = reader.getLeft();
		// 在长度有效的情况，再读剩余字节
		if (left > 0) {
			// 读出全部剩余字节数组
			byte[] b = reader.read(left);
			// 绑定这个数组
			attach(b);
		}
	}

	/**
	 * 字节数组的开始位置，不允许是负数。在关联时设置，之后不会改变。
	 * @return 开始位置
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * 字节数组的结束位置，不允许是负数。在关联时设置，之后不会改变。
	 * @return 结束位置
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * 返回当前字节数组的下标位置。这个位置随着数据读取将向后移动。
	 * @return 当前下标
	 */
	public int getSeek() {
		return seek;
	}

	/**
	 * 返回已经读取的长度
	 * @return 读取长度
	 */
	public int getUsed() {
		return seek - begin;
	}

	/**
	 * 返回剩余长度
	 * @return 返回整数值
	 */
	public int getLeft() {
		return end - seek;
	}

	/**
	 * 判断有剩余数据
	 * @return 返回真或者假
	 */
	public boolean hasLeft() {
		return getLeft() > 0;
	}

	/**
	 * 判断还有剩余对象
	 * @return 返回真或者假
	 */
	public boolean hasNext() {
		return getLeft() > 0;
	}

	/**
	 * 返回字节数据的有效长度
	 * @return 有效长度
	 */
	public int getLength() {
		return end - begin;
	}

	/**
	 * 根据传入的数据长度，判断读操作长度溢出
	 * @param len 指定的数据长度
	 * @return 读操作溢出返回真，否则假
	 * @throws ArithmeticException，如果传入长度是负数时
	 */
	public boolean isReadout(int len) {
		if (len < 0) {
			String e = String.format("%d < 0, must be >= 0", len);
			throw new ArithmeticException(e);
		}
		return seek + len > end;
	}

	/**
	 * 检查读长度溢出。如果发生溢出，将弹出数据长度异常
	 * @param len 指定的数据长度
	 * @throws ArrayIndexOutOfBoundsException, ArithmeticException
	 */
	public void checkReadout(int len) {
		if (isReadout(len)) {
			String e = String.format("%d + %d > %d", seek, len, end);
			throw new ArrayIndexOutOfBoundsException(e);
		}
	}

	/**
	 * 读取指定长度的字节数组，并且字节数组指针移动要求的长度。这是最基本的读取接口。
	 * @param len 必须大于0，而且在有效的长度范围内
	 * @throws IndexOutOfBoundsException
	 * @return 读取的字节数组
	 */
	private byte[] read(int len) {
		if (len < 1 || seek + len > end) {
			String s = String.format("%d < 1 || %d + %d > %d", len, seek, len, end);
			throw new IndexOutOfBoundsException(s);
		}
		byte[] b = Arrays.copyOfRange(buff, seek, seek + len);
		seek += len;
		return b;
	}

	/**
	 * 读取一个整型，并且字节数组指针移动4个字节
	 * @return 整型值
	 */
	public int readInt() {
		// 读4个字节
		byte[] b = read(4);
		return Laxkit.toInteger(b);
	}

	/**
	 * 从标记化读取器中取出实现标记化接口的类
	 * @param clazz 标记化接口类
	 * @param reader 标记化读取器
	 * @return 生成标记化接口的实现类
	 */
	private Object readMarkable(java.lang.Class<?> clazz, MarkReader reader) {
		// 枚举所有公共构造方法
		Constructor<?>[] constructors = clazz.getConstructors();
		// 选择一个首先出现和匹配的公共构造方法
		try {
			for (int i = 0; i < constructors.length; i++) {
				// 提取构造方法中的参数
				Class<?>[] types = constructors[i].getParameterTypes();

				if (types.length == 0) {
					// 调用空构造方法
					Object object = constructors[i].newInstance((Object[]) null);
					// 检查是否继承Markable接口
					if (!Laxkit.isInterfaceFrom(object, Markable.class)) {
						throw new MarkableException("illegal markable");
					}
					// 调用标记化读取器的“readObject”方法，把参数写入实现标记化接口的对象
					reader.readObject(object);
					return object;
				} else if (types.length == 1 && types[0] == MarkReader.class) {
					// 实现标记化接口的类构造方法，在方法里调用标记化读取器的“readObject”方法，来解析自己的参数
					Object object = constructors[i].newInstance(new Object[] { reader });
					// 检查是否支持Markable接口
					if (!Laxkit.isInterfaceFrom(object, Markable.class)) {
						throw new MarkableException("illegal markable");
					}
					return object;
				}
			}
		} catch (IllegalArgumentException e) {
			throw new MarkableException(e);
		} catch (InstantiationException e) {
			throw new MarkableException(e);
		} catch (IllegalAccessException e) {
			throw new MarkableException(e);
		} catch (InvocationTargetException e) {
			throw new MarkableException(e);
		}
		// wrong 
		throw new MarkableException("illegal %s", clazz.getName());
	}

	/**
	 * 根据类名生成类声明
	 * @param clazzName 类名称
	 * @return 类声明
	 */
	private java.lang.Class<?> createClass(String clazzName) {
		// 转化为类
		java.lang.Class<?> clazz = null;
		try {
			clazz = java.lang.Class.forName(clazzName);
		} catch (ClassNotFoundException e) {
			throw new MarkableException(e);
		}
		return clazz;
	}

	/**
	 * 根据类定义生成标记化对象
	 * @param clazz 类定义
	 * @param reader 可类化读取器
	 * @return 返回标记化对象
	 */
	private Object readMarkable(Class<?> clazz, ClassReader reader) {
		// 数据长度和内容
		int len = reader.readInt();
		byte[] b = reader.read(len);

		// 读标记化对象
		MarkReader e = new MarkReader(recorder, b);
		return readMarkable(clazz, e);
	}

	/**
	 * 从可类化读取器中读标记化对象
	 * 
	 * @param className 类名称
	 * @param reader 可类化读取器
	 * @return 返回解析的标记化对象
	 */
	private Object readMarkable(String className, ClassReader reader) {
		// 生成类实例
		java.lang.Class<?> clazz = createClass(className);
		return readMarkable(clazz, reader);
	}

	/**
	 * 生成串行化对象
	 * @param b
	 * @return
	 */
	private Object readSerialable(byte[] b) {
		Object value = null;
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(b);
			ObjectInputStream in = new ObjectInputStream(bin);
			value = in.readObject();
			in.close(); bin.close();
		} catch (IOException e) {
			throw new MarkableException(e);
		} catch (ClassNotFoundException e) {
			throw new MarkableException(e);
		}
		// 返回结果
		return value;
	}

	/**
	 * 从可类化读取器中读串行化对象
	 * @param reader 可类化读取器
	 * @return 返回解析的串行化对象
	 */
	private Object readSerialable(ClassReader reader) {
		// 数据长度和内容
		int len = reader.readInt();
		byte[] b = reader.read(len);
		return readSerialable(b);
	}

	/**
	 * 从可类化读取器取出树集对象
	 * @param reader 可类化读取器
	 * @return 树集对象
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object readTreeSet(byte[] b) {
		TreeSet base = new TreeSet();
		// 确定成员数目
		ClassReader reader = new ClassReader(b);
		int size = reader.readInt();
		// 逐一读取成员对象
		for (int i = 0; i < size; i++) {
			// 取类符号
			ClassFlag tag = new ClassFlag(recorder, reader);
			// 从可类化读取器中读取成员对象
			Object value = readValue(tag, reader);
			// 保存参数
			base.add(value);
		}

		return base;
	}

	/**
	 * 从可类化读取器取出树集对象
	 * @param reader 可类化读取器
	 * @return 树集对象
	 */
	private Object readTreeSet(ClassReader reader) {
		// 确定TREE SET的数据块长度
		int len = reader.readInt();
		// 读数据块字节数组
		byte[] b = reader.read(len);
		// 从字节数组读取树集
		return readTreeSet(b);
	}

	/**
	 * 从可类化读取器中取二叉树对象
	 * @param b 字节数组
	 * @return 二叉树对象
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object readTreeMap(byte[] b) {
		TreeMap base = new TreeMap();
		// 成员数目
		ClassReader reader = new ClassReader(b);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			// 取出类符号
			ClassFlag kTag = new ClassFlag(recorder, reader);
			ClassFlag vTag = new ClassFlag(recorder, reader);
			// 读键和值
			Object key = readValue(kTag, reader);
			Object value = readValue(vTag, reader);
			// 保存参数
			base.put(key, value);
		}

		return base;
	}

	/**
	 * 从可类化读取器中取二叉树对象
	 * @param reader 可类化读取器
	 * @return 二叉树对象
	 */
	private Object readTreeMap(ClassReader reader) {
		// 确定TREE MAP的数据块长度
		int len = reader.readInt();
		// 读数据块字节数组
		byte[] b = reader.read(len);
		// 读取二叉树
		return readTreeMap(b);
	}

	/**
	 * 从可类化读取器中取数组列表对象
	 * @param reader 可类化读取器
	 * @return 数组列表对象
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object readArrayList(byte[] b) {
		ArrayList base = new ArrayList();
		// 确定成员数目
		ClassReader reader = new ClassReader(b);
		int size = reader.readInt();
		// 逐一读取成员对象
		for (int i = 0; i < size; i++) {
			// 取类符号
			ClassFlag tag = new ClassFlag(recorder, reader);
			// 从可类化读取器中读取成员对象
			Object value = readValue(tag, reader);
			// 保存参数
			base.add(value);
		}
		// 输出数组列表
		return base;
	}

	/**
	 * 从可类化读取器中取数组列表对象
	 * @param reader 可类化读取器
	 * @return 数组列表对象
	 */
	private Object readArrayList(ClassReader reader) {
		// 确定ARRAY LIST的数据块长度
		int len = reader.readInt();
		// 读数据块字节数组
		byte[] b = reader.read(len);
		// 读取数组列表
		return readArrayList(b);
	}

	/**
	 * 根据数据类型定义，从可类化读取器中读对象
	 * @param type 对象类型，见MarkType的定义
	 * @param className 类定义
	 * @param reader 可类化读取器
	 * @return 返回读取的对象
	 */
	private Object readValue(byte type, String className, ClassReader reader) {
		// 单位个值
		if (MarkType.isByte(type)) {
			return reader.read();
		} else if (MarkType.isBoolean(type)) {
			return reader.readBoolean();
		} else if (MarkType.isChar(type)) {
			return reader.readChar();
		} else if (MarkType.isShort(type)) {
			return reader.readShort();
		} else if (MarkType.isInteger(type)) {
			return reader.readInt();
		} else if (MarkType.isLong(type)) {
			return reader.readLong();
		} else if (MarkType.isFloat(type)) {
			return reader.readFloat();
		} else if (MarkType.isDouble(type)) {
			return reader.readDouble();
		}
		// 数组值
		else if (MarkType.isByteArray(type)) {
			return reader.readByteArray();
		} else if (MarkType.isBooleanArray(type)) {
			return reader.readBooleanArray();
		} else if (MarkType.isCharArray(type)) {
			return reader.readCharArray();
		} else if (MarkType.isShortArray(type)) {
			return reader.readShortArray();
		} else if (MarkType.isIntegerArray(type)) {
			return reader.readIntArray();
		} else if (MarkType.isLongArray(type)) {
			return reader.readLongArray();
		} else if (MarkType.isFloatArray(type)) {
			return reader.readFloatArray();
		} else if (MarkType.isDoubleArray(type)) {
			return reader.readDoubleArray();
		}
		// 字符串
		else if (MarkType.isString(type)) {
			return reader.readString();
		} else if (MarkType.isStringArray(type)) {
			return reader.readStringArray();
		}
		// 标记化
		else if (MarkType.isMarkable(type)) {
			// 如果没定义对象时，肯定标记化对象名称变化。这是改名发生的错误，标记化不允许这样错误。
			if (className == null) {
				throw new MarkableException("class name is null! cannot be markable!");
			}
			return readMarkable(className, reader);
		}
		// 树集
		else if (MarkType.isTreeSet(type)) {
			return readTreeSet(reader);
		}
		// 二叉树映像
		else if (MarkType.isTreeMap(type)) {
			return readTreeMap(reader);
		}
		// 数组列表
		else if (MarkType.isArrayList(type)) {
			return readArrayList(reader);
		}
		// 串行化
		else if (MarkType.isSerialable(type)) {
			return readSerialable(reader);
		}

		// 以上不成立，是错误
		throw new MarkableException("cannot be markable:%d", type);
	}

	/**
	 * 从可类化读取中取出一个变量对象
	 * 
	 * @param tag 内部标识实例
	 * @param reader 可类化读取器
	 * @return 返回读取出的变量对象
	 */
	private Object readValue(ClassFlag tag, ClassReader reader) {
		return readValue(tag.getType(), tag.getClassName(), reader);
	}

	/**
	 * 从可类化读取中取出一个变量对象
	 * 
	 * @param tag 标记化标识实例
	 * @param reader 可类化读取器
	 * @return 返回读取出的变量对象
	 */
	private Object readValue(MarkFlag tag, ClassReader reader) {
		return readValue(tag.getType(), tag.getClassName(), reader);
	}

	/**
	 * 判断标记化实例和变量字段的数据类型匹配
	 * @param tag 标记化标识实例
	 * @param field 变量字段
	 * @return 返回真或者假
	 */
	private boolean match(MarkFlag tag, Field field) {
		return tag.getType() == MarkType.translate(field);
	}

	/**
	 * 根据变量名，查找匹配的Field
	 * @param tag MarkTag实例
	 * @param fields Field数组
	 * @return 返回匹配的Field，没有返回空指针
	 */
	private Field find(MarkFlag tag, Field[] fields) {
		for (Field field : fields) {
			String name = field.getName();
			// 比较变量名一致（大小写敏感），不一致就忽略它
			if (Laxkit.compareTo(name, tag.getParamName()) != 0) {
				continue;
			}
			// 在变量名一致情况下，判断参数类型一致
			if (match(tag, field)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * 递归读取参数，保存到传入对象中
	 * @param clazz 当前类类型
	 * @param object 指定对象
	 * @param reader 可类化读取器
	 */
	private void readObject(Class<?> clazz, Object object, ClassReader reader) {
		// 忽略根对象
		if (clazz == java.lang.Object.class) {
			return;
		}

		// 取上一级对象
		Class<?> superClazz = clazz.getSuperclass();
		// 递归将参数读取，保存到上一级对象
		if (superClazz != null) {
			readObject(superClazz, object, reader);
		}

		// 当前全部Field
		Field[] fields = clazz.getDeclaredFields();

		// 读成员数目
		short count = reader.readShort();
		// 逐一读取成员
		for (short i = 0; i < count; i++) {
			// 读取标记
			MarkFlag tag = new MarkFlag(recorder, reader);
			// 从可类化读取器取出变量对象
			Object value = readValue(tag, reader);

//			System.out.printf("[%s - %s - %s]\n", clazz.getName(), tag, value);

			// 根据变量名称查找匹配的Field
			Field field = find(tag, fields);
			
//			// 没有找到，是错误还是忽略？
//			if(field == null) {
//				throw new MarkableException("cannot be find Field by %s | %s",
//						tag, clazz.getName());
////				continue;
//			}
			
			// 没有找到，忽略它！
			if (field == null) {
				continue;
			}

			// 判断是可访问状态
			boolean accessible = field.isAccessible();

			try {
				// 设置为可访问
				if (!accessible) {
					field.setAccessible(true);
				}
				// 保存参数到类实例中
				field.set(object, value);
			} catch (SecurityException e) {
				throw new MarkableException(tag + " | " + clazz.getName() + " | " + e);
			} catch (IllegalArgumentException e) {
				throw new MarkableException(tag + " | " + clazz.getName() + " | " + e);
			} catch (IllegalAccessException e) {
				throw new MarkableException(tag + " | " + clazz.getName() + " | " + e);
			} finally {
				// 恢复为不可访问
				if (!accessible) {
					field.setAccessible(accessible);
				}
			}
		}
	}

	/**
	 * 解析一段字节数组中的参数，把变量写入被传入对象实例中
	 * @param b 字节数组
	 * @param object 对象实例
	 */
	private void readObject(byte[] b, Object object) {
		ClassReader reader = new ClassReader(b);
		Class<?> clazz = object.getClass();
		readObject(clazz, object, reader);
	}

	/**
	 * 解析标记化参数，保存到传入的对象实例
	 * @param object 对象实例
	 * @return 返回读取的字节长度
	 */
	public int readObject(Object object) {
		// 定位断点
		int pos = getSeek();

		// 确定本段长度
		int len = readInt();
		if (isReadout(len)) {
			throw new MarkableException("markable sizeout!");
		}
		// 超过0长度，读取字节数组
		if(len > 0) {
			// 读指定长度的字节数组
			byte[] b = read(len);
			readObject(b, object);
		}

//		System.out.printf("readObject, size:%d, Class:%s\n", getSeek() - pos,
//				object.getClass().getSimpleName());
		
		// 返回解析的字节数组长度
		return getSeek() - pos;
	}
	
	/**
	 * 取下一个对象
	 * @param object 对象实例
	 * @return 返回读取的字节长度
	 */
	public int next(Object object) {
		return this.readObject(object);
	}

}