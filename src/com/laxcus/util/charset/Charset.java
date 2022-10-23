/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.charset;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * LAXCUS字符集。<br><br>
 * 
 * 为保证在不同操作系统平台和语言的相互兼容，LAXCUS定义了一套字符集，所有涉及文字处理的，要求采用LAXCUS字符集来完成。<br>
 * 字符集采用UNICODE编码，子类包括UTF8、UTF16、UTF32三种，分别对应CHAR、WCHAR、HCHAR三种SQL列类型。
 * 同时其它领域也要使用。
 * 
 * @author scott.liang
 * @version 1.2 11/28/2011
 * @since laxcus 1.0
 */
public abstract class Charset implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -6053859673999671470L;

	/**
	 * 根据传入的字符集，生成它的副本
	 * @param that
	 */
	protected Charset(Charset that) {
		super();
	}
	
	/**
	 * 建立一个LAXCUS字符集，由子类指定实际类型
	 */
	protected Charset() {
		super();
	}

	/**
	 * 将字符标记写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeString(describe());
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析标记，如果与当前类定义不匹配则弹出"ClassableException"错误
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		String name = reader.readString();
		// 不匹配弹出异常
		if (name.compareToIgnoreCase(describe()) != 0) {
			throw new ClassableException("illegal %s", name);
		}
		return reader.getSeek() - scale;
	}

	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 字符集的名字，包括:UTF8、UTF16、UTF32
	 * @return 字符集的字符串名称
	 */
	public abstract String describe();

	/**
	 * 由子类实现，生成一个实际类的副本
	 * @return Charset子类实例
	 */
	public abstract Charset duplicate();

	/**
	 * 根据子类的编码集名称，将字符串编码为字节数组输出
	 * @param text JAVA字符串
	 * @return 编码后的字节数组
	 */
	public byte[] encode(String text) {
		// 判断是空指针
		Laxkit.nullabled(text);
		// 编码
		try {
			return text.getBytes(describe());
		} catch (UnsupportedEncodingException e) {
			throw new CharsetException(e);
		}
	}

	/**
	 * 根据子类定义的编码集名称，将字节数组解码为字符串
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @return 解码后字符串
	 */
	public String decode(byte[] b, int off, int len) {
		// 判断是空指针
		if (b == null || len < 1) {
			throw new NullPointerException();
		}
		try {
			return new String(b, off, len, describe());
		} catch (UnsupportedEncodingException e) {
			throw new CharsetException(e);
		}
	}
	
	/**
	 * 根据子类定义的编码集名称，将字节数组解码为字符串
	 * @param b 字节数组
	 * @return 解码后字符串
	 */
	public String decode(byte[] b) {
		return this.decode(b, 0, b.length);
	}

	/**
	 * 找到指定下标处的代码位，没有返回-1。<br>
	 * 代码位是一个16位或者32位的整数。<br>
	 * 
	 * @param index 字符串下标
	 * @param text 字符串文本
	 * @return 整型值代码位
	 */
	public int codePointAt(int index, String text) {
		int seek = 0;
		char[] chars = text.toCharArray();
		for(int scan = 0; seek < chars.length; scan++) {
			int code = java.lang.Character.codePointAt(chars, seek);
			// 扫描到指定的下标位置,返回
			if(scan == index) return code;
			// 判断是BMP(基本多语言平面)还是辅助平面.BMP是1个字符,辅助平面2字符
			seek += java.lang.Character.charCount(code); 
		}
		return -1; // 没找到
	}

	/**
	 * 返回指定下标位置的代码位值 (输入数据是编码状态,在使用前需要解码生成字符串)
	 * 
	 * @param index 解码后的字符串下标
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @return 整型值代码位
	 */
	public int codePointAt(int index, byte[] b, int off, int len) {
		return codePointAt(index, decode(b, off, len));
	}

	/**
	 * 以代码位为单元，统计实际的字符数。
	 * 新版UTF16中，存在两个字符表示一个字。旧版UCS-2，一个字符一个字
	 * 
	 * @param text 字符串
	 * @return 全部代码位统计值
	 */
	public int codePointCount(String text) {
		char[] chars = text.toCharArray();
		int count = 0;
		for(int i = 0; i < chars.length; count++) {
			int code = java.lang.Character.codePointAt(chars, i);
			i += java.lang.Character.charCount(code);
		}
		return count;
	}

	/**
	 * 新版JAVA字符串采用UTF16编码，UTF16存在占用两个字符(一个字符双字节)的可能(区别与UCS-2)。
	 * 这个函数就是以代码位为标准(一个字符或者两个字符)，得到真实的字符串
	 * 
	 * @param start 开始位置
	 * @param size 指定长度
	 * @param text 字符串
	 * @return 截取后的字符串
	 */
	public String subCodePoints(int start, int size, String text) {
		char[] chars = text.toCharArray();
		int begin = -1, end = -1;
		for (int scan = 0, seek = 0; seek < chars.length; scan++) {
			if (scan == start) {
				begin = seek;
				if (size == -1) { // 如果未指定长度,移到最后位置
					end = chars.length;
					break;
				}
			} else if (scan > start && scan - start == size) {
				end = seek;
				break;
			}

			// 计算代码位是占一个字符还是两个字符
			int code = java.lang.Character.codePointAt(chars, seek);
			seek += java.lang.Character.charCount(code); // 移到下一个坐标点
		}

		// 错误
		if (start < 0) {
			throw new IndexOutOfBoundsException("start offset:" + start);
		} else if (begin == -1) {
			throw new IndexOutOfBoundsException("string length:" + chars.length + ", split offset:" + start);
		}

		// 如果最后结束未达到指定点，以实际位置为准
		if (end == -1) end = chars.length;
		
		// 返回截取结果
		return new String(chars, begin, end - begin);
	}
	
	/**
	 * 同上
	 * @param start 开始位置
	 * @param text 字符串
	 * @return 截取后的字符串
	 */
	public String subCodePoints(int start, String text) {
		return subCodePoints(start, -1, text);
	}

	/**
	 * 同上
	 * @param start 开始位置
	 * @param size 指定长度
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 截取后的字符串
	 */
	public String subCodePoints(int start, int size, byte[] b, int off, int len) {
		return subCodePoints(start, size, decode(b, off, len));
	}
	
	/**
	 * 同上
	 * @param start 开始位置
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 截取后的字符串
	 */
	public String subCodePoints(int start, byte[] b, int off, int len) {
		return subCodePoints(start, -1, decode(b, off, len));
	}
	
}