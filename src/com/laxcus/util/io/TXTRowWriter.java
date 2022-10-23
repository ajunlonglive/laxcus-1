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
 * TXT行数据写入器。<br>
 * 把一组记录以TXT的格式，制表符为分隔符，写入磁盘。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/3/2019
 * @since laxcus 1.0
 */
public class TXTRowWriter extends PlainRowWriter {

	/**
	 * 构造默认的TXT行数据写入器
	 */
	public TXTRowWriter() {
		super();
	}

	/**
	 * 构造TXT行数据写入器，指定文件名
	 * 
	 * @param file 文件名
	 */
	public TXTRowWriter(File file) {
		super(file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.laxcus.util.io.StyleWriter#writeTitle(com.laxcus.access.schema.Sheet)
	 */
	@Override
	public void writeTitle(Sheet sheet) throws IOException {
		super.writeTitle(sheet, '\t');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.laxcus.util.io.StyleWriter#writeContent(com.laxcus.access.schema.
	 * Sheet, com.laxcus.access.row.Row[])
	 */
	@Override
	public void writeContent(Sheet sheet, Row[] rows) throws IOException {
		super.writeContent(sheet, '\t', rows);
	}

	// /**
	// * 数据写入TXT格式的磁盘文件
	// * @param text
	// * @throws IOException
	// */
	// private void write(String text) throws IOException {
	// boolean has = (file.exists() && file.isAbsolute() && file.length() > 0);
	// FileOutputStream writer = new FileOutputStream(file, true);
	// // 如果文件已经存在，在最后写入回车换行符
	// if (has) {
	// writer.write("\r\n".getBytes());
	// }
	//
	// // 以UTF8格式写入磁盘文件
	// byte[] b = new UTF8().encode(text);
	// writer.write(b);
	// // 关闭磁盘文件
	// writer.close();
	// }

	// /**
	// * 按照格式写入
	// * @param text
	// * @throws IOException
	// */
	// private void write(String text) throws IOException {
	// // 判断父目录存在
	// File parent = file.getParentFile();
	// if (!parent.exists()) {
	// boolean success = parent.mkdirs();
	// if (!success) {
	// throw new IOException("cannot be mkdir " + parent.toString());
	// }
	// }
	//
	// // 判断文件存在且字节长度大于0
	// boolean has = (file.exists() && file.isFile() && file.length() > 0);
	//
	// // 采用指定的编码格式，输出成字节数组
	// byte[] b = null;
	// try {
	// b = text.getBytes(getCharset());
	// } catch (Throwable e) {
	// throw new IOException(e.getCause());
	// }
	//
	// FileOutputStream writer = new FileOutputStream(file, true);
	// // 如果文件已经存在，在最后写入回车换行符
	// if (has) {
	// writer.write("\r\n".getBytes());
	// }
	//
	// writer.write(b);
	// // 关闭磁盘文件
	// writer.close();
	// }

	// /**
	// * 按照格式写入
	// * @param text
	// * @throws IOException
	// */
	// private void write(String text) throws IOException {
	// // 判断父目录存在
	// File parent = file.getParentFile();
	// if (!parent.exists()) {
	// boolean success = parent.mkdirs();
	// if (!success) {
	// throw new IOException("cannot be mkdir " + parent.toString());
	// }
	// }
	//
	// // 判断文件存在且字节长度大于0
	// boolean has = (file.exists() && file.isFile() && file.length() > 0);
	//
	// // 采用指定的编码格式，输出成字节数组
	// byte[] CTRL = null;
	// byte[] b = null;
	// try {
	// if (has) {
	// CTRL = "\r\n".getBytes(getCharset());
	// }
	// b = text.getBytes(getCharset());
	// } catch (Throwable e) {
	// throw new IOException(e.getCause());
	// }
	//
	// FileOutputStream writer = new FileOutputStream(file, true);
	// // 如果文件已经存在，在最后写入回车换行符
	// if (CTRL != null) {
	// writer.write(CTRL);
	// }
	//
	// writer.write(b);
	// // 关闭磁盘文件
	// writer.close();
	// }

	// /*
	// * (non-Javadoc)
	// * @see
	// com.laxcus.util.io.StyleWriter#writeTitle(com.laxcus.access.schema.Sheet)
	// */
	// @Override
	// public void writeTitle(Sheet sheet) throws IOException {
	// StringBuilder buff = new StringBuilder();
	// int size = sheet.size();
	// for (int index = 0; index < size; index++) {
	// ColumnAttribute attribute = sheet.get(index);
	// if (buff.length() > 0) {
	// buff.append('\t'); // 加制表符，这是分隔符
	// }
	// buff.append(attribute.getNameText());
	// }
	// write(buff.toString());
	// }
	//
	// /*
	// * (non-Javadoc)
	// * @see
	// com.laxcus.util.io.StyleWriter#writeContent(com.laxcus.access.schema.Sheet,
	// com.laxcus.access.row.Row[])
	// */
	// @Override
	// public void writeContent(Sheet sheet, Row[] rows) throws IOException {
	// StringBuilder buf = new StringBuilder();
	// for (Row row : rows) {
	// String content = export(sheet, row);
	// if (buf.length() > 0) {
	// buf.append("\r\n");
	// }
	// buf.append(content);
	// }
	// write(buf.toString());
	// }

	// /**
	// * 根据列集合的排列表和行记录，返回一组列信息记录
	// *
	// * @param sheet 顺序表实例
	// * @param row 行记录
	// * @return 返回ShowItem实例
	// */
	// private String export(Sheet sheet, Row row) {
	// int size = sheet.size();
	// if (size != row.size()) {
	// throw new ColumnException("not match size!");
	// }
	//
	// StringBuilder buf = new StringBuilder();
	//
	// for (int index = 0; index < size; index++) {
	// ColumnAttribute attribute = sheet.get(index);
	// // 根据列标识号查找对应的列
	// Column column = row.find(attribute.getColumnId());
	//
	// if (attribute.getType() != column.getType()) {
	// throw new ColumnException("illegal attribute %d as %d",
	// attribute.getType(), column.getType());
	// }
	//
	// // 以制表符为间隔符
	// if (index > 0) {
	// buf.append('\t');
	// }
	//
	// String s = null;
	//
	// // 如果是可变长类型
	// if (attribute.isRaw()) {
	// s = ((Raw) column).toString(((VariableAttribute)
	// attribute).getPacking());
	// } else if (attribute.isMedia()) {
	// s = ((Media) column).toString(((VariableAttribute)
	// attribute).getPacking());
	// } else if (attribute.isWord()) {
	// s = ((Word) column).toString(((WordAttribute) attribute).getPacking(),
	// -1);
	// if (s != null) s = format(s); // 字符串转义处理
	// } else {
	// s = column.toString();
	// }
	//
	// // 有效，加1
	// if(s != null){
	// buf.append(s);
	// }
	// }
	//
	// return buf.toString();
	// }

	// /**
	// * 输出以制表符为分割符的字节串
	// * @param text
	// * @return
	// */
	// private String format(String text) {
	// boolean comma = false;
	// boolean crlf = false;
	// boolean first = false;
	// boolean last = false;
	// boolean middle = false;
	//
	// StringBuffer buf = new StringBuffer();
	// int len = text.length();
	// for (int i = 0; i < len; i++) {
	// char w = text.charAt(i);
	// if (w == '\"') {
	// if (i == 0) {
	// first = true;
	// } else if(i + 1 == len) {
	// last = true;
	// } else {
	// middle = true;
	// buf.append("\"\""); // 中间有引号
	// }
	// continue;
	// } else if (w == ',') {
	// comma = true;
	// } else if (w == '\r' || w == '\n') {
	// crlf = true;
	// }
	// buf.append(w);
	// }
	//
	// if (first && last) {
	// buf.insert(0, "\"\"\""); // 开始和结束有引号
	// buf.append("\"\"\"");
	// } else if (first && !last) {
	// buf.insert(0, "\"\"\""); // 开始有引号，结束一个
	// buf.append('\"');
	// } else if (!first && last) {
	// buf.insert(0, "\""); // 开始一个引号，结束3个
	// buf.append("\"\"\"");
	// } else if (comma || crlf || middle) {
	// // 前后加引号
	// buf.insert(0, '\"');
	// buf.append('\"');
	// }
	//
	// return buf.toString();
	// }

}