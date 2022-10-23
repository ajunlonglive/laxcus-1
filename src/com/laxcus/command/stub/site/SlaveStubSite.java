/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.site;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块编号的从站点。<br>
 * 
 * 由CALL站点返回的集合。
 * 
 * @author scott.liang
 * @version 1.1 5/17/2015
 * @since laxcus 1.0
 */
public final class SlaveStubSite implements Serializable, Cloneable, Classable, Comparable<SlaveStubSite> {

	private static final long serialVersionUID = 8106900154255074188L;

	/** 命令来源地址 **/
	private Node source;

	/** 数据表名 **/
	private Space space;

	/** 数据块编号 **/
	private Set<Long> stubs = new TreeSet<Long>();
	
	/**
	 * 根据传入的数据块编号从站点实例，生成它的浅层数据副本
	 * @param that SlaveStubSite实例
	 */
	private SlaveStubSite(SlaveStubSite that) {
		this();
		source = that.source;
		space = that.space;
		stubs.addAll(that.stubs);
	}

	/**
	 * 设置数据块编号从站点
	 */
	public SlaveStubSite() {
		super();
	}

	/**
	 * 设置数据块编号从站点
	 * @param source
	 * @param space
	 */
	public SlaveStubSite(Node source, Space space) {
		this();
		setSource(source);
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析数据块编号从站点
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SlaveStubSite(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置数据块来源地址
	 * @param e
	 */
	public void setSource(Node e) {
		Laxkit.nullabled(e);

		source = e;
	}

	/**
	 * 返回数据块来源地址
	 * @return
	 */
	public Node getSource() {
		return source;
	}

	/**
	 * 设置数据表名
	 * @param e
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置数据块编号
	 * @param e
	 */
	public void addStub(long e) {
		stubs.add(e);
	}

	/**
	 * 返回数据块编号
	 * @return
	 */
	public List<Long> getStubs() {
		return new ArrayList<Long>( stubs);
	}
	
	/**
	 * 生成TransmitItem的浅层数据副本
	 * @return
	 */
	public SlaveStubSite duplicate() {
		return new SlaveStubSite(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s/%s", source, space);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SlaveStubSite.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SlaveStubSite) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return source.hashCode() ^ space.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SlaveStubSite that) {
		int ret = Laxkit.compareTo(source, that.source);
		if (ret == 0) {
			ret = Laxkit.compareTo(space, that.space);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(source);
		writer.writeObject(space);
		writer.writeInt(stubs.size());
		for (long stub : stubs) {
			writer.writeLong(stub);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		source = new Node(reader);
		space = new Space(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			stubs.add(stub);
		}
		return reader.getSeek() - seek;
	}

}