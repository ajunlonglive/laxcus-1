/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

import java.util.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;

/**
 * 前端工具组件
 * 
 * @author scott.liang
 * @version 1.2 4/23/2013
 * @since laxcus 1.0
 */
public final class TerminalKit {

	public final static long K = 1024;
	
	public final static long M = K * 1024L;
	
	public final static long G = M * 1024L;

	public final static long T = G * 1024L;
	
	public final static long P = T * 1024L;

	public static String format_size(String tag, long size) {
		String s = "";
		if (size >= TerminalKit.G) {
			s = String.format("%s=%gG", tag, (double) size / (double) TerminalKit.G);
		} else if (size >= TerminalKit.M) {
			s = String.format("%s=%gM", tag, (double) size / (double) TerminalKit.M);
		} else if (size >= TerminalKit.K) {
			s = String.format("%s=%gK", tag, (double) size / (double) TerminalKit.K);
		} else {
			s = String.format("%s=%d", tag, size);
		}
		return s;
	}
	
	public static String format_size(long chunksize) {
		String s = "";
		if (chunksize >= G) s = String.format("%gG", (double) chunksize / (double) G);
		else if (chunksize >= M) s = String.format("%gM", (double) chunksize / (double) M);
		else if (chunksize >= K) s = String.format("%gK", (double) chunksize / (double) K);
		else s = String.format("%d", chunksize);
		return s;
	}

	/**
	 * 根据列集合的排列表和行记录，返回一组列信息记录
	 * 
	 * @param sheet
	 * @param row
	 * @return
	 */
	public static String[] showRow(Sheet sheet, Row row) {
		int size = sheet.size();
		if (size != row.size()) {
			throw new ColumnException("not match size!");
		}
		List<String> array = new ArrayList<String>(size);

		for (int index = 0; index < size; index++) {
			ColumnAttribute attribute = sheet.get(index);
			// 根据列标识号查找对应的列
			Column column = row.find(attribute.getColumnId());
			
			if (attribute.getType() != column.getType()) {
				throw new ColumnException("illegal attribute %d as %d", 
						attribute.getType(), column.getType());
			}

			// 如果是可变长类型
			if (attribute.isRaw()) {
				String s = ((Raw) column).toString(((VariableAttribute) attribute).getPacking());
				array.add(s);
			} else if (attribute.isWord()) {
				String s = ((Word) column).toString(((WordAttribute) attribute).getPacking(), -1);
				array.add(s);
			} else {
				array.add(column.toString());
			}
		}
		
//		// debug code, start
//		StringBuilder bf = new StringBuilder();
//		for (String s : array) {
//			if (bf.length() > 0) bf.append("|");
//			bf.append(s);
//		}
//		Logger.debug("TerminalUtil.showRow, [%s]", bf.toString());
//		// debug code, end

		String[] s = new String[array.size()];
		return array.toArray(s);
	}
	
	public static void main(String[] args) {
		System.out.printf("G size:%d\n", java.lang.Long.MAX_VALUE / G);
		System.out.printf("T SIZE IS:%d\n", java.lang.Long.MAX_VALUE / T);
		System.out.printf("P SIZE IS:%d\n", java.lang.Long.MAX_VALUE / P);
	}
}