/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.naming;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 参数或者对象的命名。<br><br>
 * 
 * 命名是一行文本内容的字符串，是LAXCUS大数据系统中的一项基本定义。它忽略大小写，可以是任何语言的文字信息。<br>
 * 命名对象不可以派生，只能够包含在其它类中使用。<br><br>
 * 
 * 采用命名定义的包括:<br>
 * <1> 数据库的参数。<br>
 * <2> 热发布任务组件（中间件）的阶段任务对象。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 12/9/2008
 * @since laxcus 1.0
 */
public final class Naming implements Classable, Serializable, Cloneable, Markable, Comparable<Naming> {

	private static final long serialVersionUID = 8404497839637389102L;

	/** 字符串(忽略大小写) */
	private String value;

	/** 散列码 */
	private transient int hash;

	/**
	 * 建立一个空的命名对象
	 */
	private Naming() {
		super();
		hash = 0;
	}

	/**
	 * 根据传入的命名对象，生成它的数据副本
	 * @param that 命名对象
	 */
	private Naming(Naming that) {
		this();
		value = new String(that.value);
		hash = that.hash;
	}

	/**
	 * 构造一个命名对象并且赋值
	 * @param text 字符串
	 */
	public Naming(String text) {
		this();
		set(text);
	}

	/**
	 * 使用JAVA平台的默认字符集对传入的字节数组解码，构造一个命名
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 */
	public Naming(byte[] b, int off, int len) {
		this();
		set(b, off, len);
	}

	/**
	 * 使用JAVA平台字符集，对传入的字节数组进行编码，构造一个命名
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @param charset JAVA平台字符集
	 */
	public Naming(byte[] b, int off, int len, java.nio.charset.Charset charset) {
		this();
		set(b, off, len, charset);
	}

	/**
	 * 使用JAVA平台字符集，对传入的字节数组进行编码，构造一个命名
	 * @param b 字节数组
	 * @param charset JAVA平台字符集
	 */
	public Naming(byte[] b, java.nio.charset.Charset charset) {
		this(b, 0, b.length, charset);
	}

	/**
	 * 使用LAXCUS字符集，对传入的字体数组进行编码，构造一个命名
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @param charset LAXCUS字符集
	 */
	public Naming(byte[] b, int off, int len, com.laxcus.util.charset.Charset charset) {
		this();
		set(b, off, len, charset);
	}

	/**
	 * 使用LAXCUS字符集，对传入的字体数组进行编码，构造一个命名
	 * @param b 字节数组
	 * @param charset LAXCUS字符集
	 */
	public Naming(byte[] b, com.laxcus.util.charset.Charset charset) {
		this(b, 0, b.length, charset);
	}

	/**
	 * 使用平台的默认字符集对传入的字节数组解码，构造一个命名
	 * @param b 字节数组
	 */
	public Naming(byte[] b) {
		this();
		set(b, 0, (b == null ? 0 : b.length));
	}

	/**
	 * 使用传入的可类化读取器解析命名
	 * @param reader 可类化读取器
	 */
	public Naming(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出数据表参数
	 * @param reader 标记化读取器
	 */
	public Naming(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置文字内容，不允许是空指针或者空的字符串(忽略两侧的ASCII空格)
	 * @param text 字符串
	 * @throws NullPointerException
	 */
	public void set(String text) {
		if (text == null) {
			throw new NullPointerException("null pointer!");
		} 
		if (text.trim().isEmpty()) {
			throw new NullPointerException("empty string!");
		}
		value = new String(text.trim());
	}

	/**
	 * 使用平台的默认字符集对传入的字节数组解码，生成字符串
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @throws NullPointerException, IndexOutOfBoundsException
	 */
	public void set(byte[] b, int off, int len) {
		if (b == null || off < 0 || len < 1) {
			throw new NullPointerException("cannot be null!");
		} else if (off > b.length || off + len > b.length) {
			throw new IndexOutOfBoundsException();
		}
		value = new String(b, off, len);
	}

	/**
	 * 使用JAVA平台的规定字符集对传入的字节数组解码，生成字符串
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @param charset JAVA平台字符集
	 * @throws NullPointerException, IndexOutOfBoundsException
	 */
	public void set(byte[] b, int off, int len, java.nio.charset.Charset charset) {
		if (b == null || off < 0 || len < 1) {
			throw new NullPointerException("cannot be null!");
		} else if (off > b.length || off + len > b.length) {
			throw new IndexOutOfBoundsException();
		}
		value = new String(b, off, len, charset);
	}

	/**
	 * 使用LAXCUS字符集对传入的字节数组解码，生成字符串
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @param charset LAXCUS字符集
	 * @throws NullPointerException, IndexOutOfBoundsException
	 */
	public void set(byte[] b, int off, int len, com.laxcus.util.charset.Charset charset) {
		if (b == null || off < 0 || len < 1) {
			throw new NullPointerException("cannot be null!");
		} else if (off > b.length || off + len > b.length) {
			throw new IndexOutOfBoundsException();
		}
		value = charset.decode(b, off, len);
	}

	/**
	 * 返回文字内容
	 * @return 字符串
	 */
	public final String get() {
		return value;
	}
	
	/**
	 * 根据JAVA平台字符集，返回它编码后的字节数组
	 * @param charset 被支持的字符集名称
	 * @return 返回编码后的字节数组
	 * @throws UnsupportedEncodingException
	 */
	public byte[] getBytes(String charset) throws UnsupportedEncodingException {
		return value.getBytes(charset);
	}

	/**
	 * 根据JAVA平台字符集，返回它编码后的字节数组
	 * @param charset JAVA平台字符集
	 * @return 返回编码后的字节数组
	 */
	public byte[] getBytes(java.nio.charset.Charset charset) {
		return value.getBytes(charset);
	}

	/**
	 * 根据LAXCUS字符集，返回它编码后的字节数组
	 * @param charset LAXCUS字符集
	 * @return 返回编码后的字节数组
	 */
	public byte[] getBytes(com.laxcus.util.charset.Charset charset) {
		return charset.encode(value);
	}

	/**
	 * 返回JAVA平台字符集编码的原始字节数组
	 * @return 返回编码后的字节数组
	 */
	public byte[] getBytes() {
		return value.getBytes();
	}

	/**
	 * 返回文本长度
	 * @return 字符串长度
	 */
	public int length() {
		return value.length();
	}

	/**
	 * 返回当前命名的数据副本
	 * @return Naming实例
	 */
	public Naming duplicate() {
		return new Naming(this);
	}
	
	/**
	 * 比较与输入的对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Naming.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Naming) that) == 0;
	}

	/**
	 * 输出命名的散列码(转化为小写字符后再计算散列码)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = Arrays.hashCode(value.toLowerCase().getBytes());
		}
		return hash;
	}

	/**
	 * 返回命名的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value;
	}

	/**
	 * 克隆当前命名的数据副本（内部元素被复制，而不是句柄赋值）
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 按照字典序对字符串进行排列
	 * @param that 另一个命名对象
	 * @return 相同是0，小于是负正数，大于是正整数
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Naming that) {
		// 空对象排在前面，当前对象排在后面
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(value, that.value, false);
	}

	/**
	 * 比较字符串
	 * @param str 字符串
	 * @return 整数值
	 */
	public int compareTo(String str) {
		if (str == null) {
			return 1;
		}
		return compareTo(new Naming(str));
	}

	/**
	 * 将字符串写入可类化写入器
	 * @param writer 可类化写入器
	 * @return 返回写入的字节长度
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeString(value);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中取出字符串
	 * @param reader 可类化读取器
	 * @return 返回读取的字节长度
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		value = reader.readString();
		hash = 0; //重置
		return reader.getSeek() - seek;
	}
}