/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 被检索站点标记
 * 
 * @author scott.liang
 * @version 1.1 3/23/2015
 * @since laxcus 1.0
 */
public final class SeekSiteTag implements Classable, Serializable, Cloneable, Comparable<SeekSiteTag> {

	private static final long serialVersionUID = 283433151433051944L;

	/** 被查询的站点类型 **/
	private byte family;
	
	/**
	 * 站点级别。这个只对DATA站点有效，对其它站点是冗余数据。
	 * 如果指定级别，查找对应等级的站点，否则是查找全部关联站点。
	 **/
	private byte rank;
	
	/**
	 * 根据传入实例，生成被检索站点标记的数据副本
	 * @param that
	 */
	private SeekSiteTag(SeekSiteTag that) {
		super();
		set(that);
	}

	/**
	 * 构造默认的被检索站点标记
	 */
	public SeekSiteTag() {
		super();
		family = 0; // 默认无定义
		rank = 0; // 默认无定义
	}

	/**
	 * 构造被检索站点标记，指定站点类型
	 * @param family 站点类型
	 */
	public SeekSiteTag(byte family) {
		this();
		setFamily(family);
	}

	/**
	 * 构造被检索站点标记，指定站点类型和等级
	 * @param family 站点类型
	 * @param rank 站点级别
	 */
	public SeekSiteTag(byte family, byte rank) {
		this(family);
		setRank(rank);
	}

	/**
	 * 从可类化数据读取器中解析被检索站点标记
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SeekSiteTag(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置副本
	 * @param that SeekSiteTag实例
	 */
	public void set(SeekSiteTag that) {
		family = that.family;
		rank = that.rank;
	}

	/**
	 * 设置站点类型
	 * @param who 站点类型
	 */
	public void setFamily(byte who) {
		if (!SiteTag.isSite(who)) {
			throw new IllegalValueException("illegal site:%d", who);
		}
		family = who;
	}

	/**
	 * 返回站点类型
	 * @return 站点类型
	 */
	public byte getFamily() {
		return family;
	}
	
	/**
	 * 设置站点级别
	 * @param who 站点级别
	 */
	public void setRank(byte who) {
		if (!RankTag.isRank(who)) {
			throw new IllegalValueException("illegal rank:%d", who);
		}
		rank = who;
	}

	/**
	 * 返回站点级别
	 * @return 站点级别
	 */
	public byte getRank() {
		return rank;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return SeekSiteTag实例
	 */
	public SeekSiteTag duplicate() {
		return new SeekSiteTag(this);
	}

	/**
	 * 比较两个命令源标记是否一致。
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SeekSiteTag.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SeekSiteTag) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (family ^ rank);
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
		return String.format("%s/%s", SiteTag.translate(family),
				RankTag.translate(rank));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SeekSiteTag that) {
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(family, that.family);
		if (ret == 0) {
			ret = Laxkit.compareTo(rank, that.rank);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.write(family);
		writer.write(rank);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		family = reader.read();
		rank = reader.read();
		return reader.getSeek() - seek;
	}

}