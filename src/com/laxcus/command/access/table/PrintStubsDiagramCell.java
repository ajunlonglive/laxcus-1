/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块元信息。<br><br>
 * 
 * 包括数据块编号、数据块长度。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class PrintStubsDiagramCell implements Classable, Serializable, Cloneable, java.lang.Comparable<PrintStubsDiagramCell> {

	private static final long serialVersionUID = -4141107093522500150L;

	/** 数据块编号 **/
	private long stub;

	/** 数据块长度 **/
	private long length;

	/**
	 * 将数据块元信息写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 节点
		writer.writeLong(stub);
		// 写入成员数目
		writer.writeLong(length);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析数据块元信息
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		stub = reader.readLong();
		length = reader.readLong();
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的数据块元信息实例，生成它的副本
	 * @param that StubResult实例
	 */
	private PrintStubsDiagramCell(PrintStubsDiagramCell that) {
		this();
		stub = that.stub;
		length = that.length;
	}

	/**
	 * 构造一个空的数据块元信息
	 */
	public PrintStubsDiagramCell() {
		super();
	}

	/**
	 * 构造数据块元信息
	 * @param stub
	 */
	public PrintStubsDiagramCell(long stub) {
		this();
		setStub(stub);
	}

	/**
	 * 构造数据块元信息
	 * @param stub
	 */
	public PrintStubsDiagramCell(long stub, long length) {
		this(stub);
		setLength(length);
	}
	
	/**
	 * 从可类化数据读取器中解析数据块元信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public PrintStubsDiagramCell(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置节点地址
	 * @param e
	 */
	public void setStub(long e) {
		stub = e;
	}
	
	/**
	 * 返回节点地址
	 * @return
	 */
	public long getStub() {
		return stub;
	}

	/**
	 * 设置长度
	 * @param len
	 */
	public void setLength(long len) {
		length = len;
	}

	/**
	 * 返回长度
	 * @return
	 */
	public long getLength() {
		return length;
	}

	/**
	 * 克隆当前数据块元信息的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new PrintStubsDiagramCell(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PrintStubsDiagramCell that) {
		return Laxkit.compareTo(stub, that.stub);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (stub >> 32 ^ stub);
	}
}