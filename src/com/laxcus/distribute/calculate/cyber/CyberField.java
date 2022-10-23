/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.cyber;

import java.io.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 网络数据域 <br>
 * 
 * 描述一个站点（DATA/WORK）下的一块数据域。由站点地址和实体数据映像域组成。
 * 
 * @author scott.liang
 * @version 1.1 03/18/2015
 * @since laxcus 1.0
 */
public final class CyberField implements Serializable, Cloneable, Classable, Comparable<CyberField> {

	private static final long serialVersionUID = 4128762270437030123L;

	/** 分布数据映像标识 **/
	private CyberFlag flag;

	/** 数据计算域 **/
	private FluxField field;

	/**
	 * 构造默认和私有的网络数据域
	 */
	private CyberField() {
		super();
	}

	/**
	 * 根据传入的网络数据域，生成它的数据副本
	 * @param that CyberField实例
	 */
	private CyberField(CyberField that) {
		super();
		flag = that.flag.duplicate();
		field = that.field.duplicate();
	}

	/**
	 * 构造网络数据域，指定全部参数
	 * @param flag 分布数据映像标识
	 * @param field 分布计算域
	 */
	public CyberField(CyberFlag flag, FluxField field) {
		this();
		setFlag(flag);
		setField(field);
	}

	/**
	 * 从可类化读取器中解析网络数据域
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CyberField(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置分布数据映像标识
	 * @param e CyberFlag实例
	 */
	public void setFlag(CyberFlag e) {
		Laxkit.nullabled(e);

		flag = e;
	}

	/**
	 * 返回分布数据映像标识
	 * @return CyberFlag实例
	 */
	public CyberFlag getFlag() {
		return flag;
	}

	/**
	 * 返回数据源节点地址
	 * @return Node实例
	 */
	public Node getNode() {
		return flag.getNode();
	}

	/**
	 * 返回任务编号
	 * @return 任务编号的长整型值
	 */
	public long getTaskId() {
		return flag.getTaskId();
	}

	/**
	 * 设置数据计算域
	 * @param e FluxField实例
	 */
	public void setField(FluxField e) {
		Laxkit.nullabled(e);

		field = e;
	}

	/**
	 * 返回数据计算域
	 * @return FluxField实例
	 */
	public FluxField getField() {
		return field;
	}
	
	/**
	 * 返回模值
	 * @return 模值
	 */
	public long getMod() {
		return field.getMod();
	}

	/**
	 * 返回实际数据长度
	 * @return 数据长度的整型值
	 */
	public long length() {
		return field.length();
	}

	/**
	 * 返回当前对象的数据副本
	 * @return CyberField实例
	 */
	public CyberField duplicate() {
		return new CyberField(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", flag, field);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CyberField.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((CyberField) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return flag.hashCode() ^ field.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CyberField that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(flag, that.flag);
		if (ret == 0) {
			ret = Laxkit.compareTo(field, that.field);
		}
		return ret;
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
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(flag);
		writer.writeObject(field);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		flag = new CyberFlag(reader);
		field = new FluxField(reader);
		return reader.getSeek() - seek;
	}
}