/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

import java.io.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 站点成员 <br>
 * 
 * 站点成员是运行站点内一个子集，以用户签名为唯一标识。在这个签名下面，包含站点的私有属性参数。
 * 
 * @author scott.liang
 * @version 1.0 7/17/2012
 * @since laxcus 1.0
 */
public abstract class SiteMember implements Classable, Serializable, Cloneable, Comparable<SiteMember> {

	private static final long serialVersionUID = 5145914295245241929L;

	/** 用户签名 **/
	private Siger siger;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 数据持有人
		writer.writeObject(siger);
		// 写入子类
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 数据持有人
		siger = new Siger(reader);
		// 写入子类
		resolveSuffix(reader);
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造默认的站点成员
	 */
	protected SiteMember() {
		super();
	}
	
	/**
	 * 根据传入站点成员参数，生成它的数据副本
	 * @param that SiteMember实例
	 */
	protected SiteMember(SiteMember that) {
		this();
		if (that.siger != null) {
			siger = that.siger.duplicate();
		}
	}

	/**
	 * 设置站点成员签名，不允许空值.
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回站点成员签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		// 类属性必须一致
		if (that == null || that.getClass() != this.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SiteMember) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode();
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
	public int compareTo(SiteMember that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(siger, that.siger);
	}

	/**
	 * 站点成员子类生成一个浅层数据副本
	 * @return SiteMember实例
	 */
	public abstract SiteMember duplicate();

	/**
	 * 站点成员私有参数写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析属于站点成员的私有参数
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);	

}