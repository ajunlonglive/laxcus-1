/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;

/**
 * 字符列属性基础类，子类包括：CHAR、WCHAR、HCHAR
 * 
 * @author scott.liang
 * @version 1.0 4/25/2009
 * @since laxcus 1.0
 */
public abstract class WordAttribute extends VariableAttribute {

	private static final long serialVersionUID = 3005622806403730412L;

	/** 大小写是否敏感(忽略大小写:CASE or NOT CASE)，默认是TRUE(敏感) **/
	private boolean sentient;

	/** 模糊检索(LIKE or NOT LIKE)。默认是FALSE **/
	private boolean like;

	/** LIKE关键字集合 **/
	protected ArrayList<RWord> likeArray = new ArrayList<RWord>();

	/**
	 * 生成一个字符列属性，并且设置它的数据类型
	 * @param family
	 */
	protected WordAttribute(byte family) {
		super(family);
		sentient = true;
		like = false;
	}

	/**
	 * 根据传入参数建立字符列属性的副本
	 * @param that
	 */
	protected WordAttribute(WordAttribute that) {
		super(that);
		sentient = that.sentient;
		like = that.like;
		likeArray.addAll(that.likeArray);
	}

	/**
	 * 设置大小写敏感。IS TRUE，大小写敏感，否则为NO
	 * @param b
	 */
	public void setSentient(boolean b) {
		sentient = b;
	}

	/**
	 * 判断是否支持大小写敏感
	 * @return
	 */
	public boolean isSentient() {
		return sentient;
	}

	/**
	 * 设置模糊检索
	 * @param b
	 */
	public void setLike(boolean b) {
		like = b;
	}

	/**
	 * 是否支持模糊检索
	 * @return
	 */
	public boolean isLike() {
		return like;
	}

	/**
	 * 保存一列模糊检索关键字
	 * @param word
	 * @return
	 */
	public boolean addRWord(RWord word) {
		return likeArray.add((RWord) word.clone());
	}

	/**
	 * 保存一批模糊检索关键字
	 * @param set
	 * @return
	 */
	public int addRWords(Collection<RWord> set) {
		int size = likeArray.size();
		for (RWord word : set) {
			addRWord(word);
		}
		return likeArray.size() - size;
	}

	/**
	 * 将字符列属性参数输出到可类化存储器。兼容C接口。
	 * @see com.laxcus.access.column.attribute.VariableAttribute#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 大小写敏感
		writer.writeBoolean(sentient);
		// "SQL LIKE"支持
		writer.writeBoolean(like);
		// "LIKE"关键字集合
		writer.writeInt(likeArray.size());
		for (int i = 0; i < likeArray.size(); i++) {
			RWord word = likeArray.get(i);
			word.build(writer);
		}
	}

	/**
	 * 从可类化读取器中解析字符列属性。兼容C接口
	 * @see com.laxcus.access.column.attribute.VariableAttribute#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);

		// 大小写敏感
		sentient = reader.readBoolean();
		// 支持"SQL LIKE"
		like = reader.readBoolean();

		// "LIKE"关键字
		short likeId = (short) (getColumnId() | 0x8000);
		// 成员长度
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			byte family = reader.current(); // 当前字节，但是不移动指针

			RWord column = (RWord) ColumnCreator.create(family);
			column.setId(likeId);
			column.resolve(reader);
			// 保存LIKE关键字
			likeArray.add(column);
		}
	}
	
	/**
	 * 根据子类类型定义，返回对应的LAXCUS字符集
	 * @return LAXCUS字符集实例
	 */
	public abstract Charset getCharset();

}