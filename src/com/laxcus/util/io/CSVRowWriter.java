/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.io;

import java.io.*;

import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;

/**
 * CSV数据写入器。<br>
 * 把一组行数据以CSV格式写入磁盘文件，逗号是列分隔符，回车换行是一行的结束符。
 * 
 * @author scott.liang
 * @version 1.0 5/3/2019
 * @since laxcus 1.0
 */
public class CSVRowWriter extends PlainRowWriter {

	/**
	 * 构造CSV数据写入器
	 */
	public CSVRowWriter() {
		super();
	}

	/**
	 * 构造CSV数据写入器，指定写入的磁盘文件，默认是UTF-8编码
	 * @param file 磁盘文件名
	 */
	public CSVRowWriter(File file) {
		super(file);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.io.StyleWriter#writeTitle(com.laxcus.access.schema.Sheet)
	 */
	@Override
	public void writeTitle(Sheet sheet) throws IOException {
		super.writeTitle(sheet, ',');
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.io.StyleWriter#writeContent(com.laxcus.access.schema.Sheet, com.laxcus.access.row.Row[])
	 */
	@Override
	public void writeContent(Sheet sheet, Row[] rows) throws IOException {
		super.writeContent(sheet, ',', rows);
	}
	
//	/**
//	 * 按照格式写入
//	 * @param text
//	 * @throws IOException
//	 */
//	private void write(String text) throws IOException {
//		// 判断父目录存在
//		File parent = file.getParentFile();
//		if (!parent.exists()) {
//			boolean success = parent.mkdirs();
//			if (!success) {
//				throw new IOException("cannot be mkdir " + parent.toString());
//			}
//		}
//
//		// 判断文件存在且字节长度大于0
//		boolean has = (file.exists() && file.isFile() && file.length() > 0);
//
//		// 采用指定的编码格式，输出成字节数组
//		byte[] CTRL = null;
//		byte[] b = null;
//		try {
//			if(has) {
//				CTRL = "\r\n".getBytes(getCharset());
//			}
//			b = text.getBytes(getCharset());
//		} catch (Throwable e) {
//			throw new IOException(e.getCause());
//		}
//
//		FileOutputStream writer = new FileOutputStream(file, true);
//		// 如果文件已经存在，在最后写入回车换行符
//		if (CTRL != null) {
//			writer.write(CTRL);
//		}
//
//		writer.write(b);
//		// 关闭磁盘文件
//		writer.close();
//	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.util.io.StyleWriter#writeTitle(com.laxcus.access.schema.Sheet)
//	 */
//	@Override
//	public void writeTitle(Sheet sheet) throws IOException {
//		StringBuilder buff = new StringBuilder();
//		int size = sheet.size();
//		for (int index = 0; index < size; index++) {
//			ColumnAttribute attribute = sheet.get(index);
//			if (buff.length() > 0) {
//				buff.append(','); // 加逗号，是分隔符
//			}
//			buff.append(attribute.getNameText());
//		}
//		// 写入磁盘
//		write(buff.toString());
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.util.io.StyleWriter#writeContent(com.laxcus.access.schema.Sheet, com.laxcus.access.row.Row[])
//	 */
//	@Override
//	public void writeContent(Sheet sheet, Row[] rows) throws IOException {
//		StringBuilder buf = new StringBuilder();
//		for (Row row : rows) {
//			String content = export(sheet, row);
//			if (buf.length() > 0) {
//				buf.append("\r\n");
//			}
//			buf.append(content);
//		}
//		write(buf.toString());
//	}
//
//
//	/**
//	 * 根据列集合的排列表和行记录，返回一组列信息记录
//	 * 
//	 * @param sheet 顺序表实例
//	 * @param row 行记录
//	 * @return 返回ShowItem实例
//	 */
//	private String export(Sheet sheet, Row row) {
//		int size = sheet.size();
//		if (size != row.size()) {
//			throw new ColumnException("not match size!");
//		}
//
//		StringBuilder buf = new StringBuilder();
//
//		for (int index = 0; index < size; index++) {
//			ColumnAttribute attribute = sheet.get(index);
//			// 根据列标识号查找对应的列
//			Column column = row.find(attribute.getColumnId());
//
//			if (attribute.getType() != column.getType()) {
//				throw new ColumnException("illegal attribute %d as %d", attribute.getType(), column.getType());
//			}
//
//			if (index > 0) {
//				buf.append(',');
//			}
//			
//			String s = null;
//			// 如果是可变长类型
//			if (attribute.isRaw()) {
//				s = ((Raw) column).toString(((VariableAttribute) attribute).getPacking());
//			} else if (attribute.isMedia()) {
//				s = ((Media) column).toString(((VariableAttribute) attribute).getPacking());
//			} else if (attribute.isWord()) {
//				s = ((Word) column).toString(((WordAttribute) attribute).getPacking(), -1);
//				if (s != null) s = format(s); // 字符串转义处理
//			} else {
//				s = column.toString();
//			}
//
//			if (s != null) {
//				buf.append(s);
//			}
//		}
//
//		return buf.toString();
//	}
//	
//	/**
//	 * 输出为CSV格式的字符串 <br><br>
//	 * 
//	 * 判断条件：<br>
//	 * 1. 有逗号和回车换行符，两侧加引号。<br>
//	 * 2. 中间有引号，这个引号被引号，两侧加引号。<br>
//	 * 3. 两侧的开始结尾有引号，这个引号被双引号包括，即总共3个引号，共6个引号。<br>
//	 * 4. 两侧的其中一侧有引号，这个引号被双引号包括，即总共3个引号，另一侧加1个引号。<br>
//	 * 
//	 * @param text 文本
//	 * @return 返回格式化后的字符串
//	 */
//	private String format(String text) {
//		boolean comma = false;
//		boolean crlf = false;
//		boolean first = false;
//		boolean last = false;
//		boolean middle = false;
//
//		int len = text.length();
//		StringBuffer buf = new StringBuffer(len + 10);
//
//		for (int i = 0; i < len; i++) {
//			char w = text.charAt(i);
//			if (w == '\"') {
//				if (i == 0) { 
//					first = true;
//				} else if(i + 1 == len) { 
//					last = true;
//				} else {
//					middle = true;
//					buf.append("\"\""); // 中间有引号
//				}
//				continue;
//			} else if (w == ',') {
//				comma = true;
//			} else if (w == '\r' || w == '\n') {
//				crlf = true;
//			}
//			buf.append(w);
//		}
//		
//		if (first && last) {
//			buf.insert(0, "\"\"\""); // 开始和结束有引号
//			buf.append("\"\"\"");
//		} else if (first && !last) {
//			buf.insert(0, "\"\"\""); // 开始有引号，结束一个
//			buf.append('\"');
//		} else if (!first && last) {
//			buf.insert(0, "\""); // 开始一个引号，结束3个
//			buf.append("\"\"\"");
//		} else if (comma || crlf || middle) {
//			// 前后加引号
//			buf.insert(0, '\"');
//			buf.append('\"');
//		}
//
//		return buf.toString();
//	}

}