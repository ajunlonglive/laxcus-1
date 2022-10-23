/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.product;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 运行中的边缘容器单元 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public final class TubContainerItem implements Cloneable, Classable, Comparable<TubContainerItem> {

	/** 边缘容器名称 */
	private Naming naming;

	/** 绑定主机地址 **/
	private String className;

	/** 标题 **/
	private String caption;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(naming);
		writer.writeString(className);
		writer.writeString(caption);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		naming = new Naming(reader);
		className = reader.readString();
		caption = reader.readString();
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入对象生成它的数据副本
	 * @param that TubTubItem实例
	 */
	private TubContainerItem(TubContainerItem that) {
		this();
		naming = that.naming.duplicate();
		className = that.className;
		caption = that.caption;
	}

	/**
	 * 构造默认的运行中的边缘容器单元
	 */
	public TubContainerItem() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析运行中的边缘容器单元参数
	 * @param reader 可类化数据读取器
	 */
	public TubContainerItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置主机地址，允许空指针
	 * @param e
	 */
	public void setClassName(String e) {
		className = e;
	}

	/**
	 * 返回主机地址
	 * @return String实例或者空指针
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * 设置标题
	 * @param e 标题
	 */
	public void setCaption(String e) {
		caption = e;
	}

	/**
	 * 返回标题
	 * @return 标题
	 */
	public String getCaption() {
		return caption;
	}
	
	/**
	 * 设置边缘容器
	 * @param e 命名实例
	 */
	public void setNaming(Naming e) {
		Laxkit.nullabled(e);

		naming = e;
	}

	/**
	 * 返回边缘容器
	 * @return Naming实例
	 */
	public Naming getNaming() {
		return naming;
	}

	/**
	 * 返回运行中的边缘容器单元的数据副本
	 * @return TubContainerItem实例
	 */
	public TubContainerItem duplicate() {
		return new TubContainerItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TubContainerItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TubContainerItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return  naming.hashCode();
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
		return String.format("%s#%s#%s", naming, className, caption);
	}

	/*
	 * 比较两个运行中的边缘容器单元的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TubContainerItem that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		// 名称是唯一判断标准
		return Laxkit.compareTo(naming, that.naming);
	}


}