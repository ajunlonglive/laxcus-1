/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.slide;

import java.io.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.type.*;
import com.laxcus.access.util.*;
import com.laxcus.util.*;
import com.laxcus.util.charset.*;

/**
 * 字符列对象定位器。<br>
 * 
 * 根据字符列中的参数（默认是首选索引，其次是实体值），产生一个64位的长整型。这个长整型将代表它在集合中所处位置。
 * 
 * 用户定义的字符列个性化实现必须基于这个类。
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public abstract class WordSlider extends VariableSlider {

	/** 判断大小写敏感，默认是TRUE(敏感) **/
	private boolean sentient;

	/**
	 * 构造字符列对象定位器，指定列数据类型
	 * @param family 列数据类型
	 */
	protected WordSlider(byte family) {
		super(family);
		// 默认敏感
		setSentient(true);

		if (!ColumnType.isWord(family)) {
			throw new IllegalValueException("illegal word %d", family);
		}
	}

	/**
	 * 设置大小写敏感。IS TRUE，大小写敏感，否则为NO
	 * @param b 大小写敏感
	 */
	public void setSentient(boolean b) {
		sentient = b;
	}

	/**
	 * 判断大小写敏感
	 * @return 返回真或者假
	 */
	public boolean isSentient() {
		return sentient;
	}

	/**
	 * 从字符串中取出首字符的代码位。这个代码位将用于判断字符串的分区下标。<br><br>
	 * 
	 * 执行顺序是：<br>
	 * <1> 取出二进制字节数组格式的字符串，可能是已经压缩或者加密的。<br>
	 * <2> 解包(解密、解压) <br>
	 * <3> 根据字符集编码(UTF8、UTF16、UTF32)解码，生成String <br>
	 * <4> 如果是忽略大小写的，改成小写字符 <br>
	 * <5> 从String中取出首字符代码位(兼容UTF-16，UTC-2，包括基本多语言平面BMP和辅助平面)<br><br>
	 * 
	 * @param charset 字符集
	 * @param word 可变长列
	 * @return 返回长整型值
	 * @throws SliderException
	 */
	protected java.lang.Long doDefaultSeek(Charset charset, Word word) throws SliderException {
		// 不允许空指针
		Laxkit.nullabled(word);
		// 空值
		if (word.isNull() || word.isEmpty()) {
			return new java.lang.Long(0L);
		}

		// 返回首选项。有索引就返回索引值，否则返回数据值
		byte[] value = word.getPreferred();
		Packing packing = getPacking();
		// 解码
		if (packing != null && packing.isEnabled()) {
			try {
				value = VariableGenerator.depacking(packing, value, 0, value.length);
			} catch (IOException e) {
				throw new SliderException(e);
			}
		}

		String text = charset.decode(value, 0, value.length);
		// 如果大小写不敏感，转成小写字符
		if (!isSentient()) {
			text = text.toLowerCase();
		}

		// 取首字符代码位
		int codePoint = text.codePointAt(0); // charset.codePointAt(0, text);
		// 返回代码位
		return new java.lang.Long(codePoint);
	}

}