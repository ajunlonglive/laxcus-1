/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.remote;

import java.io.*;

import com.laxcus.util.classable.*;

/**
 * 原生数据格式化器 <br>
 * 
 * 这个类对传入的各种数组、数据类型、对象，进行编码和解码。<br><br>
 * 
 * 此方案取代JAVA中的串行化，由格式化器生成的数据，具有尺寸小、结构灵活、自由定义等诸多特点。在大规模数据传输、数据交换、数据计算时，能够很大程度上减少数据冗余，提高数据处理速度。<br><br>
 * 
 * 这是Laxcus大数据系统中一个很重要的类！！！<br><br>
 * 
 * 
 * 对传入对象的判断和执行顺序（解析遵循同样的顺序）：<br>
 * 1. 空指针 <br>
 * 2. 数组类型:  xxx.getClass().isArray() <br>
 * 3. 原生数据类型：byte, bool, char, short, int, long, float, double, String <br>
 * 4. 可类化对象 <br>
 * 5. 串行化对象 <br>
 * 6. 不是以上任何对象，弹出错误 <br>
 * 
 * 
 * @author scott.liang
 * @version 1.0 11/06/2015
 * @since laxcus 1.0
 */
public class PatternFormatter implements Serializable {

	private static final long serialVersionUID = -2907001600567135087L;

	/** 空对象 **/
	public final static byte NULL = 1;

	/** 数组类型 **/
	public final static byte ARRAY_BYTE = 10;
	public final static byte ARRAY_BOOL = 11;
	public final static byte ARRAY_CHAR = 12;
	public final static byte ARRAY_SHORT = 13;
	public final static byte ARRAY_INT = 14;
	public final static byte ARRAY_LONG = 15;
	public final static byte ARRAY_FLOAT = 16;
	public final static byte ARRAY_DOUBLE = 17;
	public final static byte ARRAY_OBJECT = 18;

	/** 单个数据类型 **/
	public final static byte PRIMITIVE_BYTE = 20; 	//BYTE的JAVA类型
	public final static byte PRIMITIVE_BOOL = 21; 	//BYTE的JAVA类型
	public final static byte PRIMITIVE_CHAR = 22; 	//CHAR的JAVA类型
	public final static byte PRIMITIVE_SHORT = 23; 	//SHORT的JAVA类型
	public final static byte PRIMITIVE_INT = 24;
	public final static byte PRIMITIVE_LONG = 25;
	public final static byte PRIMITIVE_FLOAT = 26;
	public final static byte PRIMITIVE_DOUBLE = 27;
	public final static byte PRIMITIVE_STRING = 28;	// JAVA的字符串类型，属于特殊处理
	public final static byte PRIMITIVE_VOID = 29;		// VOID符号

	/** 可类化标识 **/
	public final static byte CLASSABLE = 30;
	/** 串行化标识 **/
	public final static byte SERIALIZABLE = 40;

	/** JVM对系统的的原生数据类型描述，共9种。 **/
	public final static byte TYPE_VOID = 101;
	public final static byte TYPE_BOOL = 102;
	public final static byte TYPE_BYTE = 103;
	public final static byte TYPE_CHAR = 104;
	public final static byte TYPE_SHORT = 105;
	public final static byte TYPE_INT = 106;
	public final static byte TYPE_LONG = 107;
	public final static byte TYPE_FLOAT = 108;
	public final static byte TYPE_DOUBLE = 109;
	
	/**
	 * 构造默认的原生数据格式化器
	 */
	protected PatternFormatter() {
		super();
	}

	/**
	 * 写入一组原生数据
	 * @param all 数据对象
	 * @return 输入数据对象的字节流。
	 */
	protected byte[] writeAllObjects(Object[] all) {
		ClassWriter writer = new ClassWriter(1024);
		// 实际数值
		int size = (all == null ? 0 : all.length);
		writer.writeInt(size);
		for (int index = 0; index < size; index++) {
			byte[] b = writeObject(all[index]);
			writer.write(b);
		}
		return writer.effuse();
	}

	/**
	 * 读出一组原生数据
	 * @param reader 可类化读取器
	 * @return 返回解析的数据对象
	 */
	protected Object[] readAllObjects(ClassReader reader) {
		// 参数
		int size = reader.readInt();
		if (size == 0) {
			return null;
		}
		Object[] all = new Object[size];
		for (int index = 0; index < size; index++) {
			all[index] = readObject(reader);
		}
		return all;
	}

	/**
	 * 从可类化数据读取器中读一个JAVA对象
	 * @param reader 可类化数据读取器
	 * @return Object子类对象实例
	 */
	protected Object readObject(ClassReader reader) {
		byte flag = reader.read();
		switch (flag) {
		/** 空指针 **/
		case PatternFormatter.NULL:
			return null;
		/** JAVA原生数据类型 **/
		case PatternFormatter.TYPE_VOID:
			return java.lang.Void.TYPE;
		case PatternFormatter.TYPE_BOOL:
			return java.lang.Boolean.TYPE;
		case PatternFormatter.TYPE_BYTE:
			return java.lang.Byte.TYPE;
		case PatternFormatter.TYPE_CHAR:
			return java.lang.Character.TYPE;
		case PatternFormatter.TYPE_SHORT:
			return java.lang.Short.TYPE;
		case PatternFormatter.TYPE_INT:
			return java.lang.Integer.TYPE;
		case PatternFormatter.TYPE_LONG:
			return java.lang.Long.TYPE;
		case PatternFormatter.TYPE_FLOAT:
			return java.lang.Float.TYPE;
		case PatternFormatter.TYPE_DOUBLE:
			return java.lang.Double.TYPE;
		/** 各种数组 **/
		case PatternFormatter.ARRAY_BYTE:
			return reader.readByteArray();
		case PatternFormatter.ARRAY_BOOL:
			return reader.readBooleanArray();
		case PatternFormatter.ARRAY_CHAR:
			return reader.readCharArray();
		case PatternFormatter.ARRAY_SHORT:
			return reader.readShortArray();
		case PatternFormatter.ARRAY_INT:
			return reader.readIntArray();
		case PatternFormatter.ARRAY_LONG:
			return reader.readLongArray();
		case PatternFormatter.ARRAY_FLOAT:
			return reader.readFloatArray();
		case PatternFormatter.ARRAY_DOUBLE:
			return reader.readDoubleArray();
		case PatternFormatter.ARRAY_OBJECT: {
			int size = reader.readInt();
			Object[] all = new Object[size];
			for (int index = 0; index < size; index++) {
				all[index] = readObject(reader);
			}
			return all;
		}
		/** 原生数据对象，包括字符串 **/
		case PatternFormatter.PRIMITIVE_BYTE:
			return new Byte(reader.read());
		case PatternFormatter.PRIMITIVE_BOOL:
			return new Boolean(reader.readBoolean());
		case PatternFormatter.PRIMITIVE_CHAR:
			return new java.lang.Character(reader.readChar());
		case PatternFormatter.PRIMITIVE_SHORT:
			return new java.lang.Short(reader.readShort());
		case PatternFormatter.PRIMITIVE_INT:
			return new java.lang.Integer(reader.readInt());
		case PatternFormatter.PRIMITIVE_LONG:
			return new java.lang.Long(reader.readLong());
		case PatternFormatter.PRIMITIVE_FLOAT:
			return new java.lang.Float(reader.readFloat());
		case PatternFormatter.PRIMITIVE_DOUBLE:
			return new java.lang.Double(reader.readDouble());
		case PatternFormatter.PRIMITIVE_STRING:
			return reader.readString();
		case PatternFormatter.PRIMITIVE_VOID:
			return java.lang.Void.class; // void
		/** 实现可类化接口的类 **/
		case PatternFormatter.CLASSABLE:
			return readClassableObject(reader);
		/** 实现串行化接口的类 **/
		case PatternFormatter.SERIALIZABLE: 
			return readSerializableObject(reader);
		}

		// 以前都不支持，是错误
		throw new UnsupportedOperationException("illegal flag " + flag);
	}

	/**
	 * 写入一个对象
	 * @param object Object子类对象实例
	 * @return 输出这个对象编码后的字节数组
	 */
	protected byte[] writeObject(Object object) {
		ClassWriter writer = new ClassWriter();
		// 如果是空值，写入空标记，继续下一个
		if (object == null) {
			writer.write(PatternFormatter.NULL);
		} else {
			// 判断是JAVA原生数据类型并且输出。（即 xxx.TYPE，由Class.getPrimitiveClass("int")产生 ）
			byte[] b = writePrimitiveType(object);
			// 判断是数组对象，并且输出
			if(b == null) {
				b = writeArrayObject(object);
			}
			// 判断是JAVA原生数据对象并且输出。（即 Byte.class, Short.class, String.class, 由new Byte, new Short, new String产生）
			if (b == null) {
				b = writePrimitiveObject(object);
			}
			// 判断是可类化对象，并且输出
			if (b == null) {
				b = writeClassableObject(object);
			}
			// 判断是JAVA串行化对象，并且输出
			if (b == null) {
				b = writeSerializableObject(object);
			}
			// 以上都不成立，是错误
			if (b == null) {
				throw new UnsupportedOperationException(object.getClass().getName());
			}
			writer.write(b);
		}
		return writer.effuse();
	}

	/**
	 * 写入一个数组对象
	 * @param that 数组对象实例
	 * @return 输出对象数组编码后的字节数组
	 */
	private byte[] writeArrayObject(Object that) {
		ClassWriter writer = new ClassWriter();
		Class<?> clazz = that.getClass();
		if (clazz == byte[].class) {
			byte[] b = (byte[]) that;
			writer.write(PatternFormatter.ARRAY_BYTE);
			writer.writeByteArray(b);
		} else if (clazz == boolean[].class) {
			boolean[] b = (boolean[]) that;
			writer.write(PatternFormatter.ARRAY_BOOL);
			writer.writeBooleanArray(b);
		} else if (clazz == char[].class) {
			char[] b = (char[]) that;
			writer.write(PatternFormatter.ARRAY_CHAR);
			writer.writeCharArray(b);
		} else if (clazz == short[].class) {
			short[] b = (short[]) that;
			writer.write(PatternFormatter.ARRAY_SHORT);
			writer.writeShortArray(b);
		} else if (clazz == int[].class) {
			int[] b = (int[]) that;
			writer.write(PatternFormatter.ARRAY_INT);
			writer.writeIntArray(b);
		} else if (clazz == long[].class) {
			long[] b = (long[]) that;
			writer.write(PatternFormatter.ARRAY_LONG);
			writer.writeLongArray(b);
		} else if (clazz == float[].class) {
			float[] b = (float[]) that;
			writer.write(PatternFormatter.ARRAY_FLOAT);
			writer.writeFloatArray(b);
		} else if (clazz == double[].class) {
			double[] b = (double[]) that;
			writer.write(PatternFormatter.ARRAY_DOUBLE);
			writer.writeDoubleArray(b);
		} else if (that.getClass().isArray()) {
			byte[] b = writeAllObjects((Object[]) that);
			writer.write(PatternFormatter.ARRAY_OBJECT);
			writer.write(b);
		} else {
			return null;
		}
		return writer.effuse();
	}

	/**
	 * 写JAVA基本数据类型 <BR>
	 * 基本数据类型是JVM产生，定义在java.lang包，以“xxx.TYPE”出现。如 Byte.TYPE , Long.TYPE 。
	 * 按照 Class.getPrimitiveClass("int"); 样式产生。
	 * 
	 * @param object 基本数据类型
	 * @return 编码后的字节数组
	 */
	private byte[] writePrimitiveType(Object object) {
		ClassWriter writer = new ClassWriter(8);
		if (object == java.lang.Void.TYPE) {
			writer.write(PatternFormatter.TYPE_VOID);
		} else if (object == java.lang.Boolean.TYPE) {
			writer.write(PatternFormatter.TYPE_BOOL);
		} else if (object == java.lang.Byte.TYPE) {
			writer.write(PatternFormatter.TYPE_BYTE);
		} else if (object == java.lang.Character.TYPE) {
			writer.write(PatternFormatter.TYPE_CHAR);
		} else if (object == java.lang.Short.TYPE) {
			writer.write(PatternFormatter.TYPE_SHORT);
		} else if (object == java.lang.Integer.TYPE) {
			writer.write(PatternFormatter.TYPE_INT);
		} else if (object == java.lang.Long.TYPE) {
			writer.write(PatternFormatter.TYPE_LONG);
		} else if (object == java.lang.Float.TYPE) {
			writer.write(PatternFormatter.TYPE_FLOAT);
		} else if (object == java.lang.Double.TYPE) {
			writer.write(PatternFormatter.TYPE_DOUBLE);
		} else {
			return null; // 以上不匹配，返回空
		}
		return writer.effuse();
	}

	/**
	 * 写JAVA基本数据对象 <br><br>
	 * 
	 * 基本数据对象是JAVA数据类型（共9种）的类封装化，即“new Long(120L) , new Character('a')”，它区别于JAVA基本数据类型，即“xxx.TYPE”。<br>
	 * 在目前JAVA定义的9种数据类型之外，本处增加“java.lang.String”为基本数据对象。<br>
	 * 
	 * @param object JAVA基本数据对象
	 * @return 字节数组
	 */
	private byte[] writePrimitiveObject(Object object) {
		ClassWriter writer = new ClassWriter();
		Class<?> clazz = object.getClass();
		if (clazz == Byte.class) {
			byte b = ((Byte) object).byteValue();
			writer.write(PatternFormatter.PRIMITIVE_BYTE);
			writer.write(b);
		} else if (clazz == Boolean.class) {
			boolean b = ((Boolean) object).booleanValue();
			writer.write(PatternFormatter.PRIMITIVE_BOOL);
			writer.writeBoolean(b);
		} else if (clazz == Character.class) {
			char ch = ((Character) object).charValue();
			writer.write(PatternFormatter.PRIMITIVE_CHAR);
			writer.writeChar(ch);
		} else if (clazz == Short.class) {
			short value = ((Short) object).shortValue();
			writer.write(PatternFormatter.PRIMITIVE_SHORT);
			writer.writeShort(value);
		} else if (clazz == Integer.class) {
			int value = ((Integer) object).intValue();
			writer.write(PatternFormatter.PRIMITIVE_INT);
			writer.writeInt(value);
		} else if (clazz == Long.class) {
			long value = ((Long) object).longValue();
			writer.write(PatternFormatter.PRIMITIVE_LONG);
			writer.writeLong(value);
		} else if (clazz == Float.class) {
			float value = ((Float) object).floatValue();
			writer.write(PatternFormatter.PRIMITIVE_FLOAT);
			writer.writeFloat(value);
		} else if (clazz == Double.class) {
			double value = ((Double) object).doubleValue();
			writer.write(PatternFormatter.PRIMITIVE_DOUBLE);
			writer.writeDouble(value);
		} else if (clazz == String.class) {
			String value = (String) object;
			writer.write(PatternFormatter.PRIMITIVE_STRING);
			writer.writeString(value);
		} else if (clazz == Void.class) {
			writer.write(PatternFormatter.PRIMITIVE_VOID);
		} else {
			return null;
		}
		return writer.effuse();
	}

	/**
	 * 从可类化解析器中解析一个实现可类化接口的对象
	 * @param reader
	 * @return 如果成功，返回可类化接口对象。
	 */
	private Classable readClassableObject(ClassReader reader) {
		int size = reader.readInt();
		byte[] b = reader.read(size);
		Classable result = null;
		try {
			ClassReader e = new ClassReader(b, 0, b.length);
			result = e.readDefault();
		} catch (ClassableException e) {
			throw new UnsupportedOperationException(e);
		}
		return result;
	}

	/**
	 * 写入一个实现了可类化接口的对象
	 * @param object
	 * @return 成功，返回写入的字节数组。不支持可类化接口，返回空指针。
	 * @throws 其它异常
	 */
	private byte[] writeClassableObject(Object object) {
		// 如果不支持可类化，返回空指针
		if (!isClassable(object)) {
			return null;
		}
		// 生成字节数组
		ClassWriter writer = new ClassWriter();
		writer.writeDefault((Classable) object);
		byte[] b = writer.effuse();
		// 重置缓存
		writer.reset();
		// 写入可类化标记
		writer.write(PatternFormatter.CLASSABLE);
		// 写入字节数组长度
		writer.writeInt(b.length);
		// 写入字节数组
		writer.write(b);
		return writer.effuse();
	}

	/**
	 * 从可类化存储器中读取一个实现了串行化接口的类
	 * @param reader 可类化读取器
	 * @return UnsupportedOperationException
	 */
	private Object readSerializableObject(ClassReader reader) {
		int size = reader.readInt();
		byte[] b = reader.read(size);
		Object that = null;
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(b, 0, b.length);
			ObjectInputStream oin = new ObjectInputStream(bin);
			that = oin.readObject();
			oin.close();
		} catch (ClassNotFoundException e) {
			throw new UnsupportedOperationException(e);
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}
		return that;
	}

	/**
	 * 将实现了类行化接口的对象写入，输出它的字节数组。
	 * @param object 对象实例
	 * @return 成功，输出串行化的字节数组。不支持，输出一个空指针。
	 * @throws UnsupportedOperationException
	 */
	private byte[] writeSerializableObject(Object object) {
		if(!isSerializable(object)) {
			return null;
		}
		byte[] b = null;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(bout);
			oout.writeObject(object);
			oout.close();
			b = bout.toByteArray();
		} catch (IOException e) {
			throw new UnsupportedOperationException(e);
		}

		ClassWriter writer = new ClassWriter();
		// 写入可类化标记
		writer.write(PatternFormatter.SERIALIZABLE);
		// 写入字节数组长度
		writer.writeInt(b.length);
		// 写入字节数组
		writer.write(b);
		return writer.effuse();
	}

	/**
	 * 判断传入类实现了可类化接口
	 * @param that 类对象
	 * @return 返回真或者假
	 */
	public boolean isClassable(Class<?> that) {
		// 取出全部接口
		Class<?>[] clazz = that.getInterfaces();
		// 判断有可类化接口
		for (int i = 0; i < clazz.length; i++) {
			if (clazz[i] == com.laxcus.util.classable.Classable.class) {
				return true;
			}
		}
		// 检查它的父类，最到最后
		Class<?> parent = that.getSuperclass();
		if (parent != null) {
			return isClassable(parent);
		}
		// 以上处理完成后...
		return false;
	}

	/**
	 * 判断类实现了可类化接口
	 * @param that 对象实例
	 * @return 返回真或者假
	 */
	public boolean isClassable(Object that) {
		return isClassable(that.getClass());
	}

	/**
	 * 判断传入类实现了串行化接口
	 * @param that 类对象
	 * @return 返回真或者假
	 */
	public boolean isSerializable(Class<?> that) {
		// 全部接口
		Class<?>[] clazz = that.getInterfaces();
		// 判断实现了串行化接口
		for (int i = 0; i < clazz.length; i++) {
			if (clazz[i] == java.io.Serializable.class) {
				return true;
			}
		}
		// 检查它的父类，最到最后
		Class<?> parent = that.getSuperclass();
		if (parent != null) {
			return isSerializable(parent);
		}
		// 以上处理完成后...
		return false;
	}

	/**
	 * 判断传入类实现了串行化接口
	 * @param that 对象实例
	 * @return 返回真或者假
	 */
	public boolean isSerializable(Object that) {
		return isSerializable(that.getClass());
	}
}