/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.util;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 基于某个模值的SHA256签名域 <br> <br>
 * 
 * 使用这个类，可以将大量的用户账号分散保存到不同的SignArea。这样可以加速检索速度，在写入磁盘时，降低数据写入时间。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public final class SignField implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 4170804814833587335L;

	/** 模值 **/
	private int mod;

	/** SHA256签名集 */
	private TreeSet<Siger> array = new TreeSet<Siger>();

	/**
	 * 将模值签名域写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 模值
		writer.writeInt(mod);
		// 写入成员数目
		writer.writeInt(array.size());
		// 写入每一个成员
		for (Siger e : array) {
			writer.writeObject(e);
		}
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析模值签名域
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 模值
		mod = reader.readInt();
		// 读成员数目
		int size = reader.readInt();
		// 读每个成员和保存它
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			array.add(e);
		}
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的模值签名域实例，生成它的副本
	 * @param that SignField实例
	 */
	private SignField(SignField that) {
		this();
		mod = that.mod;
		array.addAll(that.array);
	}

	/**
	 * 构造一个默认的模值签名域
	 */
	public SignField() {
		super();
	}

	/**
	 * 构造模值签名域，指定模值
	 * @param mod 模值
	 */
	public SignField(int mod) {
		this();
		setMod(mod);
	}
	
	/**
	 * 从可类化数据读取器中解析模值签名域
	 * @param reader 可类化数据读取器
	 */
	public SignField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 把字节数组解析成解析模值签名域
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 */
	public SignField(byte[] b, int off, int len) {
		this(new ClassReader(b, off, len));
	}

	/**
	 * 把字节数组解析成解析模值签名域
	 * @param b 字节数组
	 */
	public SignField(byte[] b) {
		this(b, 0, b.length);
	}

	/**
	 * 设置模值
	 * @param i 模值
	 */
	public void setMod(int i) {
		if(i < 0) {
			throw new ArithmeticException("must be >= 0");
		}
		mod = i;
	}
	
	/**
	 * 返回模值
	 * @return 模值
	 */
	public int getMod() {
		return mod;
	}

	/**
	 * 保存一个用户签名
	 * @param e 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean add(Siger e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除一个用户签名
	 * @param e 用户签名
	 * @return 成功返回真，否则假
	 */
	public boolean remove(Siger e) {
		return array.remove(e);
	}

	/**
	 * 保存一批保存用户签名
	 * @param that SignField实例
	 * @return 返回新增成员数目
	 */
	public int addAll(SignField that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that.array);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批用户签名
	 * @param a 用户签名数组
	 * @return 已经保存的成员数目
	 */
	public int addAll(Siger[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批用户签名
	 * @param that 模值签名域
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<Siger> that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that);
		}
		return array.size() - size;
	}

	/**
	 * 判断用户签名存在
	 * @param siger Siger实例
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger) {
		if (siger != null) {
			return array.contains(siger);
		}
		return false;
	}

	/**
	 * 输出全部用户签名
	 * @return Siger列表
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}

	/**
	 * 输出全部用户签名
	 * @return Siger集合
	 */
	public Set<Siger> set() {
		return array;
	}
	
	/**
	 * 清除全部用户签名
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 返回用户签名的数目
	 * @return Siger数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 检测集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * 输出全部用户签名数组
	 * @return 用户签名数组
	 */
	public Siger[] toArray() {
		Siger[] a = new Siger[array.size()];
		return array.toArray(a);
	}
	
	/**
	 * 返回一个数据副本
	 * @return SignField副本
	 */
	public SignField duplicate() {
		return new SignField(this);
	}

	/**
	 * 克隆当前模值签名域的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 输出字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}
}