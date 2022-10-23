/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.sort;

import java.io.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.util.*;
import com.laxcus.log.client.*;
import com.laxcus.util.charset.*;

/**
 * 字符串排序比较器(根据字典序进行排序) <br>
 * 
 * @author scott.liang
 * @version 1.1 11/30/2012
 * @since laxcus 1.0
 */
public abstract class WordComparator implements ColumnComparator {
	
	/** 列编号 **/
	private short columnId;

	/** 字符大小写是否敏感(CASE or NOT CASE)。默认是敏感(TRUE) **/
	private boolean sentient;

	/** 对应的字符集 **/
	private Charset charset;

	/** 数据封装 **/
	private Packing packing; // = new Packing();

	/**
	 * 构造字符串排序比较器，同时指定字符集
	 * @param charset 字符集
	 */
	protected WordComparator(Charset charset) {
		super();
		setSentient(true);
		setCharset(charset);
	}
	
	/**
	 * 大小写敏感 (CASE or NOTCASE)
	 * 
	 * @param b 大小写敏感
	 */
	public void setSentient(boolean b) {
		sentient = b;
	}

	/**
	 * 判断是大小写敏感
	 * 
	 * @return 返回真或者假
	 */
	public boolean isSentient() {
		return sentient;
	}
	
	/**
	 * 设置数据封装
	 * @param e Packing实例
	 */
	public void setPacking(Packing e) {
		if (e != null) {
			packing = e.duplicate();
		}
	}
	
	/**
	 * 返回数据封装
	 * @return Packing实例
	 */
	public Packing getPacking() {
		return packing;
	}
	
	/**
	 * 设置列编号
	 * @param id 列编号
	 */
	public void setColumnId(short id) {
		columnId = id;
	}

	/**
	 * 返回列编号
	 * @see com.laxcus.command.access.sort.ColumnComparator#getColumnId()
	 */
	@Override
	public short getColumnId() {
		return columnId;
	}
	
	/**
	 * 设置字符集
	 * @param e Charset子类实例
	 */
	private void setCharset(Charset e) {
		charset = e;
	}

	/**
	 * 返回字符集
	 * @return Charset子类实例
	 */
	public Charset getCharset() {
		return charset;
	}

//	/**
//	 * 根据字典序，字符串排序比较
//	 * 
//	 * @param b1 字节数组1
//	 * @param b2 字节数组2
//	 * @return 返回字典序排序值
//	 */
//	protected int compare(byte[] b1, byte[] b2) {
//		// 如果数据被打包(压缩和加密，执行反操作)
//		if (packing != null && packing.isEnabled()) {
//			try {
//				b1 = VariableGenerator.depacking(packing, b1, 0, b1.length);
//				b2 = VariableGenerator.depacking(packing, b2, 0, b2.length);
//			} catch (IOException e) {
//				Logger.error(e);
//				return -1;
//			}
//		}
//
//		// 解码，转成字符串
//		String s1 = null;
//		String s2 = null;
//		try {
//			s1 = charset.decode(b1, 0, b1.length);
//			s2 = charset.decode(b2, 0, b2.length);
//		} catch (CharsetException e) {
//			Logger.error(e);
//			return -1;
//		}
//		
//		// 大小写敏感
//		if (sentient) {
//			return s1.compareTo(s2);
//		} else {
//			// 忽略大小写进行比较
//			return s1.compareToIgnoreCase(s2);
//		}
//	}

	/**
	 * 根据字典序列进行字符串排序
	 * 
	 * @param b1 字节数组1
	 * @param b2 字节数组2
	 * @param asc 升序排序或者否
	 * @return 返回字典序排序值
	 */
	protected int compare(byte[] b1, byte[] b2, boolean asc) {
		// 如果数据被打包(压缩和加密，执行反操作)
		if (packing != null && packing.isEnabled()) {
			try {
				b1 = VariableGenerator.depacking(packing, b1, 0, b1.length);
				b2 = VariableGenerator.depacking(packing, b2, 0, b2.length);
			} catch (IOException e) {
				Logger.error(e);
				return -1;
			}
		}

		// 解码，转成字符串
		String s1 = null;
		String s2 = null;
		try {
			s1 = charset.decode(b1, 0, b1.length);
			s2 = charset.decode(b2, 0, b2.length);
		} catch (CharsetException e) {
			Logger.error(e);
			return -1;
		}
		
//		System.out.printf("Comparator, %s -> %s\n", s1, s2);
		
		// 大小写敏感
		if (sentient) {
			if (asc) {
				return s1.compareTo(s2);
			} else {
				return s2.compareTo(s1);
			}
		} else {
			// 忽略大小写进行比较
			if (asc) {
				return s1.compareToIgnoreCase(s2);
			} else {
				return s2.compareToIgnoreCase(s1);
			}
		}
	}
	
}