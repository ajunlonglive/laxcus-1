/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.data;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块区域映像。<br><br>
 * 
 * 描述一个DATA站点一个表下面，全部数据块元数据信息。<br>
 * 包括：<br>
 * 1. 数据表名 <br>
 * 2. 数据块统计数目 <br>
 * 3. 数据块文件总长度。<br>
 * 
 * @author scott.liang
 * @version 1.1 12/19/2015
 * @since laxcus 1.0
 */
public final class StubReflex implements Classable, Cloneable, Serializable, Comparable<StubReflex> {

	private static final long serialVersionUID = -2094967975875930263L;

	/** 数据表名 **/
	private Space space;

	/** 数据块数目 **/
	private int stubs;

	/** 磁盘数据总量（在磁盘上的数据块总尺寸） **/
	private long diskCapacity;
	
	/** 内存数据总量（在内存中的元数据总尺寸）**/
	private long memoryCapacity;

	/**
	 * 根据传入实例，生成它的数据副本
	 * @param that
	 */
	private StubReflex(StubReflex that) {
		super();
		space = that.space.duplicate();
		stubs = that.stubs;
		diskCapacity = that.diskCapacity;
		memoryCapacity = that.memoryCapacity;
	}

	/**
	 * 构造默认的数据块区域映像
	 */
	private StubReflex() {
		super();
		stubs = 0;
		diskCapacity = 0L;
		memoryCapacity = 0L;
	}

	/**
	 * 构造数据块区域映像，指定全部参数
	 * @param space 数据表名
	 * @param stubs 数据块统计值
	 * @param capacity 数据存储总量
	 */
	public StubReflex(Space space, int stubs, long capacity) {
		this();
		setSpace(space);
		setStubs(stubs);
		setDiskCapacity(capacity);
	}

	/**
	 * 从可类化数据读取器中解析数据块区域映像
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public StubReflex(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名，不允许空值
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);
		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置数据块数目
	 * @param num 数据块数目
	 */
	public void setStubs(int num) {
		if (num < 0) {
			throw new IllegalValueException("illegal stubs:%d", num);
		}
		stubs = num;
	}

	/**
	 * 返回数据块数目
	 * @return 数据块数目
	 */
	public int getStubs() {
		return stubs;
	}

	/**
	 * 设置磁盘数据总量
	 * @param len 磁盘数据总量
	 */
	public void setDiskCapacity(long len) {
		if (len < 0) {
			throw new IllegalValueException("illegal data size:%d", len);
		}
		diskCapacity = len;
	}

	/**
	 * 返回磁盘数据总量
	 * @return 磁盘数据总量
	 */
	public long getDiskCapacity() {
		return diskCapacity;
	}
	
	/**
	 * 设置内存数据总量
	 * @param len 内存数据总量
	 */
	public void setMemoryCapacity(long len) {
		if (len < 0) {
			throw new IllegalValueException("illegal data size:%d", len);
		}
		memoryCapacity = len;
	}

	/**
	 * 返回内存数据总量
	 * @return 内存数据总量
	 */
	public long getMemoryCapacity() {
		return memoryCapacity;
	}

	/**
	 * 生成数据块区域映像浅层数据副本
	 * @return 返回StubReflex实例
	 */
	public StubReflex duplicate() {
		return new StubReflex(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != StubReflex.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((StubReflex) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (space.hashCode() ^ stubs ^ diskCapacity ^ memoryCapacity);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s/%d/%d/%d", space, stubs, diskCapacity, memoryCapacity);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StubReflex that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(space, that.space);
		if (ret == 0) {
			ret = Laxkit.compareTo(stubs, that.stubs);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(diskCapacity, that.diskCapacity);
		}
		if( ret == 0) {
			ret = Laxkit.compareTo(memoryCapacity, that.memoryCapacity);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(space);
		writer.writeInt(stubs);
		writer.writeLong(diskCapacity);
		writer.writeLong(memoryCapacity);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		space = new Space(reader);
		stubs = reader.readInt();
		diskCapacity = reader.readLong();
		memoryCapacity = reader.readLong();
		return reader.getSeek() - seek;
	}

}