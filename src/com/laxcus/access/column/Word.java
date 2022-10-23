/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.type.*;
import com.laxcus.access.util.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 可变长字符列。
 * 
 * @author scott.liang
 * @version 1.0 3/9/2009
 * @since laxcus 1.0
 */
public abstract class Word extends Variable {

	private static final long serialVersionUID = 960971738891948714L;

	/** 模糊检索关键字 **/
	private List<RWord> array = new ArrayList<RWord>();

	/**
	 * 构造可变长字符列，并且指定它的列类型
	 * @param family 列类型
	 */
	protected Word(byte family) {
		super(family);
	}

	/**
	 * 根据传入列类型实例生成可变长字符列的副本
	 * @param that 列类型实例
	 */
	protected Word(Word that) {
		super(that);
		for(RWord word : that.array) {
			this.addRWord(word);
		}
	}

	/**
	 * 保存一个LIKE检索列
	 * @param word
	 */
	public void addRWord(RWord word) {
		array.add((RWord) word.duplicate());
	}

	/**
	 * 保存一组模糊关键字列
	 * @param words
	 */
	public void addRWords(Collection<RWord> words) {
		for (RWord word : words) {
			addRWord(word);
		}
	}

	/**
	 * 根据实际字符集对数据值进行解码，返回数据值的字符串描述
	 * @param packing 数据包装(压缩和加密)
	 * @param limit 返回字节长度限制，-1代表无限制
	 * @return 字符串
	 */
	public String toString(Packing packing, int limit) {
		if (isNull()) {
			return null;
		}

		// 返回初始数据流
		byte[] b = super.getValue(packing);
		if (b == null || b.length == 0) {
			return "";
		}
		// 解码
		Charset charset = getCharset();
		String s = charset.decode(b, 0, b.length);
		// 如果限制长度，返回指定长度
		if (limit > 0 && limit < s.length()) {
			return s.substring(0, limit) + "...";
		}
		return s;
	}

	/**
	 * 返回数据值的字符串描述
	 * @param packing 数据包装(压缩和加密)
	 * @return String
	 */
	public String toString(Packing packing) {
		return toString(packing, -1);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(null, -1);
	}

	/**
	 * 比较两个列是否一致
	 * @param word 字符列
	 * @return 返回真或者假
	 */
	protected boolean equals(Word word) {
		if (this.isNull() && word.isNull()) return true;
		else if(isNull() || word.isNull()) return false;

		// 如果双方都有索引，以索引做比较
		if (index != null && index.length > 0) {
			if (index.length != word.index.length) return false;
			for (int i = 0; i < index.length; i++) {
				if (index[i] != word.index[i]) return false;
			}
		} else {
			// 否则，以值做比较
			if (value.length != word.value.length) return false;
			for (int i = 0; i < value.length; i++) {
				if (value[i] != word.value[i]) return false;
			}
		}
		return true;
	}

	/*
	 * 比较两个字符列是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || !(that instanceof Word)) {
			return false;
		} else if (that == this) {
			return true;
		}
		return super.equals((Word) that);
	}

	/**
	 * 在提供<b>包装属性、大小写敏感、是否选择索引</b>的前提下，对两个字符列进行字典序列比较。
	 * 如果索引有效优先比较索引，否则比较数值。
	 * @param that 被比较列
	 * @param packing 封包属性参数
	 * @param sentient IS TRUE，大小写敏感
	 * @param ignoreIndex IS TRUE，忽略索引
	 * @return 返回排序值
	 */
	public int compare(Word that, Packing packing, boolean sentient, boolean ignoreIndex) {
		if(that == null) {
			return 1;
		} else if (this.getType() != that.getType()) {
			return super.compareTo(that); // 如果类型不一致，按照列编号排列
		} else if (this.isNull() && that.isNull()) {
			return 0;
		} else if (this.isNull()) {
			return -1;
		} else if (that.isNull()) {
			return 1;
		}

		byte[] b1 = null;
		byte[] b2 = null;
		// 如果索引存在，并且不忽略索引时
		if (!ignoreIndex && index != null && that.index != null) {
			b1 = index;
			b2 = that.index;
		} else {
			if (value == null || value.length == 0) return -1;
			if (that.value == null || that.value.length == 0) return 1;
			b1 = value;
			b2 = that.value;
		}

		// 如果有压缩，先解压再解码，否则直接解码
		if (packing != null && packing.isEnabled()) {
			try {
				b1 = VariableGenerator.depacking(packing, b1, 0, b1.length);
				b2 = VariableGenerator.depacking(packing, b2, 0, b2.length);
			} catch (IOException e) {
				Logger.error(e);
				return -1;
			}
		}
		// 解码
		Charset charset = getCharset();
		String s1 = charset.decode(b1, 0, b1.length);
		String s2 = charset.decode(b2, 0, b2.length);
		// 区分大小写敏感的比较
		if (sentient) {
			return s1.compareTo(s2);
		} else {
			return s1.compareToIgnoreCase(s2);
		}
	}

	/**
	 * 在提供<b>包装属性、大小写敏感、是否选择索引</b>的前提下，对两个字符列进行字典序列比较。
	 * 如果索引有效优先比较索引，否则比较数值。
	 * @param that 被比较列
	 * @param packing 封包属性参数
	 * @param sentient IS TRUE，大小写敏感
	 * @param ignoreIndex IS TRUE，忽略索引
	 * @return 返回排序值
	 */
	public int compare(Word that, Packing packing, boolean sentient, boolean asc, boolean ignoreIndex) {
		if(that == null) {
			return 1;
		} else if (this.getType() != that.getType()) {
			return super.compareTo(that); // 如果类型不一致，按照列编号排列
		} else if (this.isNull() && that.isNull()) {
			return 0;
		} else if (this.isNull()) {
			return -1;
		} else if (that.isNull()) {
			return 1;
		}

		byte[] b1 = null;
		byte[] b2 = null;
		// 如果索引存在，并且不忽略索引时
		if (!ignoreIndex && index != null && that.index != null) {
			b1 = index;
			b2 = that.index;
		} else {
			if (value == null || value.length == 0) return -1;
			if (that.value == null || that.value.length == 0) return 1;
			b1 = value;
			b2 = that.value;
		}

		// 如果有压缩，先解压再解码，否则直接解码
		if (packing != null && packing.isEnabled()) {
			try {
				b1 = VariableGenerator.depacking(packing, b1, 0, b1.length);
				b2 = VariableGenerator.depacking(packing, b2, 0, b2.length);
			} catch (IOException e) {
				Logger.error(e);
				return -1;
			}
		}
		// 解码
		Charset charset = getCharset();
		String s1 = charset.decode(b1, 0, b1.length);
		String s2 = charset.decode(b2, 0, b2.length);
		// 区分大小写敏感的比较
		if (sentient) {
			if (asc) {
				return s1.compareTo(s2);
			} else {
				return s2.compareTo(s1);
			}
		} else {
			if (asc) {
				return s1.compareToIgnoreCase(s2);
			} else {
				return s2.compareToIgnoreCase(s1);
			}
		}
	}
	
	/**
	 * 模糊检索比较，当前字符是否包含另一个字符
	 * @param that 被比较LIKE列
	 * @param packing 封包
	 * @param sentient IS TRUE，大小写敏感
	 * @param ignoreIndex 忽略索引
	 * @return 返回真或者假
	 */
	public boolean likeIn(RWord that, Packing packing, boolean sentient, boolean ignoreIndex) {
		if (this.isNull() && that.isNull()) {
			return true;
		} else if (this.isNull() || that.isNull()) {
			return false;
		} else if (!((isChar() && that.isRChar())
				|| (isWChar() && that.isRWChar()) 
				|| (isHChar() && that.isRHChar()))) {
			return false;
		}

		byte[] b1 = null;
		byte[] b2 = that.getIndex();
		// 如果索引存在，并且不忽略索引时
		if (!ignoreIndex && index != null ) {
			b1 = index;
		} else {
			if (value == null || value.length == 0) return false;
			b1 = value;
		}

		// 如果有压缩，先解压再解码，否则直接解码
		if (packing != null && packing.isEnabled()) {
			try {
				b1 = VariableGenerator.depacking(packing, b1, 0, b1.length);
				b2 = VariableGenerator.depacking(packing, b2, 0, b2.length);
			} catch (IOException e) {
				Logger.error(e);
				return false;
			}
		}
		// 解码
		Charset charset = getCharset();
		String s1 = charset.decode(b1, 0, b1.length);
		String s2 = charset.decode(b2, 0, b2.length);

		// 如果是大小写不敏感时，统一转成小写字符
		if(!sentient){
			s1 = s1.toLowerCase();
			s2 = s2.toLowerCase();
		}

		// 左侧空格字符
		int left = s1.indexOf(s2) ;
		if(left < 0) return false; // 没有找到
		// 右侧空格字符
		int right = s1.length() - left - s2.length();

		// 左右匹配检测。如果是-1表示无限制；否则为有限制字长，必须匹配
		boolean leftMatch = (that.getLeft() == -1 || left == that.getLeft());
		boolean rightMatch = (that.getRight() == -1 || right == that.getRight());
		return leftMatch && rightMatch;
	}

	/**
	 * 计算列数据将产生的字节长度
	 * @see com.laxcus.access.column.Variable#capacity()
	 */
	@Override
	public int capacity() {
		if (isNull()) return 1;

		int size = 5;
		size += (value == null ? 0 : value.length);
		size += (index == null ? 0 : index.length);
		for (RWord that : array) {
			size += that.capacity();
		}
		return size;
	}
	
	/**
	 * 计算字符串的散列码
	 * @param b 传入值
	 * @param packing 封装
	 * @param sentient 大小写敏感
	 * @return 返回SHA256编码
	 */
	private SHA256Hash hash(byte[] b, Packing packing, boolean sentient) {
		// 如果数据被打包(压缩和加密，执行反操作)
		if (packing != null && packing.isEnabled()) {
			try {
				b = VariableGenerator.depacking(packing, b, 0, b.length);
			} catch (IOException e) {
				Logger.error(e);
				return null;
			}
		}

		// 不敏感，先把字节转成字符串，字符串转成小写，再把字符串转换字节
		if (!sentient) {
			String s = getCharset().decode(b);
			s = s.toLowerCase();
			b = getCharset().encode(s);
		}
		// 最后计算SHA256散列码
		return Laxkit.doSHA256Hash(b);
	}
	

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Variable#hash(com.laxcus.access.column.attribute.ColumnAttribute)
	 */
	@Override
	public SHA256Hash hash(ColumnAttribute attribute) {
		if (isNull()) {
			return new SHA256Hash((byte)0);
		}
		
		// 不匹配，弹出错误
		if (attribute.getType() != getType()) {
			throw new ColumnAttributeException("%s != %s",
					ColumnType.translate(attribute.getType()), ColumnType.translate(getType()));
		}
		// 不匹配，弹出错误
		if (!Laxkit.isClassFrom(attribute, WordAttribute.class)) {
			throw new ColumnAttributeException("%s != %s",
					attribute.getClass().getName(), WordAttribute.class.getName());
		}
		
		// 计算散列码
		WordAttribute attr = (WordAttribute)attribute;
		if (value != null) {
			return hash(value, attr.getPacking(), attr.isSentient());
		}
		if (index != null) {
			return hash(value, attr.getPacking(), attr.isSentient());
		}
		return new SHA256Hash((byte)0);
	}
	
	/**
	 * 生成数据、索引、LIKE关键字
	 * @param writer 可类化写入器
	 */
	private void buildWord(ClassWriter writer) {
		ClassWriter buff = new ClassWriter();
		// 1. 写入值域(4字节长度标识 + 值内容)
		buff.writeInt(value.length);
		if (value.length > 0) {
			buff.write(value);
		}

		// 2. 检查索引域和LIKE关键字。任何一个存在即处理
		int indexLen = (index == null ? 0 : index.length);
		if (indexLen > 0 || array.size() > 0) {
			// 索引域
			buff.writeInt(indexLen);
			if (indexLen > 0) {
				buff.write(index);
			}
			// LIKE关键字域成员集合
			buff.writeInt(array.size());
			for (RWord that : array) {
				that.build(buff);
			}
		}
		// 输出全部数据
		byte[] b = buff.effuse();

		// 写入总长度(含其自身4字节): maxsize = 4 + b.length
		writer.writeInt(4 + b.length);
		// 写入数据
		writer.write(b);
	}
	
	/**
	 * 解析数据、索引、LIKE关键字
	 * @param reader 可类化读取器
	 */
	private void resolveWord(ClassReader reader) {
		// 后续数据的总长度
		int maxsize = reader.readInt();

		// 值域(值长度和值)
		int size = reader.readInt();
		// 读值长度
		if (size > 0) {
			setValue(reader.read(size));
		} else {
			setValue(new byte[0]);
		}

		// 如果条件成立，表示没有索引和LIKE关键字，可以退出
		if (maxsize == 8 + size) {
			return; // 8: maxsize(4) + value size(4)
		}

		// 索引域(索引长度和索引值)
		size = reader.readInt();
		// 读索引内容
		if (size > 0) {
			this.setIndex(reader.read(size));
		}

		// LIKE关键字成员数目(LIKE关键字在索引基础上分割形成)
		size = reader.readInt(); // LIKE关键字统计值
		for (int i = 0; i < size; i++) {
			// 判断列类型
			byte flag = reader.current();
			byte family = ColumnType.resolveType(flag);
			RWord that = null;
			switch (family) {
			case ColumnType.RCHAR:
				that = new RChar(); break;
			case ColumnType.RWCHAR:
				that = new RWChar(); break;
			case ColumnType.RHCHAR:
				that = new RHChar(); break;
			}
			if (that == null) {
				throw new ColumnException("resolve error! invalid column type: %d", family);
			}
			// 解析关键字参数
			that.resolve(reader);
			// 保存LIKE关键字
			array.add(that);
		}
	}

	/**
	 * 将字符列的参数写入可类化存储器。兼容C接口
	 * @see com.laxcus.access.column.Variable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		//1. 写入标记号(空值+列类型，占1个字节)
		byte tag = buildTag();
		writer.write(tag);
		// 如果不是空值，写入数据、索引、LIKE字。如果是空值时不处理
		if (!isNull()) {
			this.buildWord(writer);
		}
		// 返回写入数据的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析字符列参数。兼容C接口。
	 * @see com.laxcus.access.column.Variable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek(); // 刻度
		// 解析标记值
		resolveTag(reader.read());
		// 如果不是空值，解析数据、索引、LIKE字。如果是空值是，退出
		if (!isNull()) {
			this.resolveWord(reader);
		}
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 由子类决定，返回它的字符集编码
	 * @return Charset子类实例
	 */
	public abstract Charset getCharset();

}