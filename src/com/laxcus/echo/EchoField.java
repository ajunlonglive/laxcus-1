/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 回显数据域。<br><br>
 * 
 * 由于RPC异步应答数据的长度大小不一，数据传输采用分组转发的模式。应答数据由任意多个回显数据域组成。<br>
 * 
 * 回显数据域由EchoClient根据传入的处理结果数据生成和发出，
 * 传递到EchoAgent中，再由EchoAgent转发给EchoReceiver，
 * EchoReceiver完成最后的处理。<br>
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class EchoField implements Classable, Cloneable, Serializable, Comparable<EchoField> {

	private static final long serialVersionUID = 5628017814678346145L;

	/** 本段数据在数据流中的编号 **/
	private int number;

	/** 本段数据在数据流的下标位置 **/
	private long seek;

	/** 异步应答数据。**/
	private byte[] data;
	
	/**
	 * 将回显数据域参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 * @since 1.1
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeInt(number);
		writer.writeLong(seek);
		writer.writeByteArray(data);
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析回显数据域参数
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 * @since 1.1
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		number = reader.readInt();
		seek = reader.readLong();
		data = reader.readByteArray();
		return reader.getSeek() - scale;
	}
	
	/**
	 * 构造默认和私有的回显数据域
	 */
	private EchoField() {
		super();
	}

	/**
	 * 根据传入的回显数据域，构造它的数据副本
	 * @param that EchoField实例
	 */
	private EchoField(EchoField that) {
		this();
		number = that.number;
		seek = that.seek;
		setData(that.data);
	}
	
	/**
	 * 构造回显数据域，指定全部参数
	 * @param number  在数据流中的编号
	 * @param seek 数据在数据流的下标位置
	 * @param b 字节数组
	 * @param off 字节数组指定下标
	 * @param len 字节数组指定有效长度
	 */
	public EchoField(int number, long seek, byte[] b, int off, int len) {
		this();
		setNumber(number);
		setSeek(seek);
		setData(b, off, len);
	}
	
	/**
	 * 构造回显数据域，指定全部参数
	 * @param number  在数据流中的编号
	 * @param seek 数据在数据流的下标位置
	 * @param b 字节数组
	 */
	public EchoField(int number, long seek, byte[] b) {
		this();
		setNumber(number);
		setSeek(seek);
		setData(b);
	}

	/**
	 * 从可类化读取器中解析回显数据域
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public EchoField(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置本块数据在在数据流中的编号
	 * @param i 在数据流中的编号
	 */
	public void setNumber(int i) {
		number = i;
	}
	
	/**
	 * 返回本块数据在在数据流中的编号
	 * @return 在数据流中的编号
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * 设置本块数据在数据流的下标位置
	 * @param i 数据在数据流的下标位置
	 */
	public void setSeek(long i ) {
		seek = i;
	}
	
	/**
	 * 返回本块数据在数据流的下标位置
	 * @return 数据在数据流的下标位置
	 */
	public long getSeek() {
		return seek;
	}
	
	/**
	 * 设置传输数据。不允许空值或者0长度。
	 * @param b 字节数组
	 * @param off 开始下标
	 * @param len 有效长度
	 * @throws NullPointerException, IndexOutOfBoundsException
	 */
	public void setData(byte[] b, int off, int len) {
		if (b == null || b.length == 0) {
			throw new NullPointerException();
		} else if (off < 0 || len < 1 || off > b.length
				|| (off + len > b.length) || (off + len < 0)) {
			throw new IndexOutOfBoundsException();
		}
		// 复制数据
		data = Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 设置异步应答数据。不允许空值
	 * @param b 字节数组
	 */
	public void setData(byte[] b) {
		if (b == null || b.length == 0) {
			throw new NullPointerException();
		}
		setData(b, 0, b.length);
	}

	/**
	 * 返回数据
	 * @return 字节数组
	 */
	public byte[] getData() {
		return data;
	}
	
	/**
	 * 返回数据长度
	 * @return 数据长度
	 */
	public int getDataSize() {
		return data.length;
	}
	
	/**
	 * 生成当前对象实例的数据副本
	 * @return EchoField实例
	 */
	public EchoField duplicate() {
		return new EchoField(this);
	}
	
	/**
	 * 比较两个回显数据域一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != EchoField.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((EchoField) that) == 0;
	}

	/**
	 * 回显数据域散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return number;
	}

	/**
	 * 返回回显数据域的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d %d", number, seek);
	}

	/**
	 * 根据当前的回显数据域，克隆它的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个回显数据域的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(EchoField that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(number, that.number);
		if (ret == 0) {
			ret = Laxkit.compareTo(seek, that.seek);
		}
		return ret;
	}

}