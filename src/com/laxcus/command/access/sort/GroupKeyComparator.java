/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.sort;

import java.io.*;
import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.util.*;
import com.laxcus.log.client.*;
import com.laxcus.util.charset.*;

/**
 * SQL "GROUP BY" 的键值集合比较器
 * 
 * @author scott.liang
 * @version 1.0 11/19/2011
 * @since laxcus 1.0
 */
final class GroupKeyComparator implements Comparator<GroupKey> {

	/** 数据表实例 **/
	private Table table;
	
	/**
	 * 构造GroupKeyComparator实例，指定数据表
	 * @param table 数据表
	 */
	public GroupKeyComparator(Table table) {
		super();
		this.setTable(table);
	}

	/**
	 * 设置数据表
	 * @param e Table实例
	 */
	public void setTable(Table e) {
		this.table = e;
	}
	
	/**
	 * 返回数据表
	 * @return Table实例
	 */
	public Table getTable() {
		return this.table;
	}
	
	/**
	 * 比较字符列是否一致
	 * 
	 * @param word1 列值1
	 * @param word2 列值2
	 * @param attribute 列属性
	 * @return 返回排序值
	 */
	private int compareWord(Column word1, Column word2, ColumnAttribute attribute) {
		byte[] b1 = ((Word) word1).getValue();
		byte[] b2 = ((Word) word2).getValue();
		
		WordAttribute second = (WordAttribute) attribute;
		
		// 解包(解压和解密操作)
		Packing packing = second.getPacking();
		if(packing.isEnabled()) {
			try {
				b1 = VariableGenerator.depacking(packing, b1, 0, b1.length);
				b2 = VariableGenerator.depacking(packing, b2, 0, b2.length);
			} catch (IOException e) {
				Logger.error(e);
				return -1;
			}
		}
		// 根据类型进行解码
		Charset charset = null;
		if(second.isChar()) charset = new UTF8();
		else if(second.isWChar()) charset = new UTF16();
		else if(second.isHChar()) charset = new UTF32();
		String s1 = charset.decode(b1, 0, b1.length);
		String s2 = charset.decode(b2, 0, b2.length);
		// 如果大小写敏感时...
		if (second.isSentient()) {
			return s1.compareTo(s2) ;
		}
		// 忽略大小写敏感的比较
		return s1.compareToIgnoreCase(s2);
	}

	/* 比较两组GroupKey
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(GroupKey key1, GroupKey key2) {
		Column[] s1 = key1.getKeys();
		Column[] s2 = key2.getKeys();

		if (s1.length < s2.length) return -1;
		else if (s1.length > s2.length) return 1;

		for (int i = 0; i < s1.length; i++) {
			for (int j = 0; j < s2.length; j++) {
				if (s1[i].getId() != s2[j].getId()) continue;

				// 检查属性
				int ret = 0;
				ColumnAttribute attribute = table.find(s1[i].getId());
				if (attribute.isNumber() || attribute.isRaw()) {
					ret = s1[i].compare(s2[j]); // 继续排列位置
				} else if (attribute.isWord()) {
					ret = this.compareWord(s1[i], s2[j], attribute);
				}
				// 一致，继续比较。否则返回
				if (ret == 0) break;
				else return ret;
			}
		}

		return 0;
	}

}