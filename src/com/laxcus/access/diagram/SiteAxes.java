/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.diagram;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 节点坐标。<br><br>
 * 节点坐标由两个参数组成：当前节点的编号、一组节点的数目。<br>
 * 只有节点编号大于等于0，成员数目大于0时，节点坐标才有效。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 6/23/2018
 * @since laxcus 1.0
 */
public final class SiteAxes implements Classable, Cloneable, Serializable, Comparable<SiteAxes> {

	private static final long serialVersionUID = -6598736458140554699L;

	/** 节点编号**/
	private int no;

	/** 节点成员数目**/
	private int partners;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 节点编号
		writer.writeInt(no);
		// 节点成员数目
		writer.writeInt(partners);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 节点编号
		no = reader.readInt();
		// 节点成员数目
		partners = reader.readInt();
		// 返回解析的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入参数，生成它的数据副本
	 * @param that Axes实例
	 */
	private SiteAxes(SiteAxes that) {
		super();
		no = that.no;
		partners = that.partners;
	}

	/**
	 * 构造默认和私有的节点坐标
	 */
	public SiteAxes() {
		super();
		no = -1;
		partners = 0;
	}

	/**
	 * 构造节点坐标，指定节点编号和节点成员数目
	 * @param no 节点编号
	 * @param members 节点成员数目
	 */
	public SiteAxes(int no, int members) {
		this();
		setNo(no);
		setPartners(members);
	}
	
	/**
	 * 从可类化数据读取器中解析节点坐标
	 * @param reader 可类化数据读取器
	 */
	public SiteAxes(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置节点编号
	 * @param i 节点编号
	 */
	public void setNo(int i) {
		no = i;
	}

	/**
	 * 返回节点编号
	 * @return 节点编号
	 */
	public int getNo() {
		return no;
	}

	/**
	 * 设置节点成员数目
	 * @param i 节点成员数目
	 */
	public void setPartners(int i) {
		partners = i;
	}

	/**
	 * 返回节点成员数目
	 * @return 节点成员数目
	 */
	public int getPartners() {
		return partners;
	}

	/**
	 * 判断有效
	 * @return 返回真或者假
	 */
	public boolean isValid() {
		return no >= 0 && partners > 0;
	}

	/**
	 * 判断无效
	 * @return 返回真或者假
	 */
	public boolean isInvalid() {
		return !isValid();
	}
	
	/**
	 * 根据签名，定位它的下标位置
	 * @param siger 用户签名
	 * @return 返回下标位置，无效返回-1。
	 */
	public int locate(Siger siger) {
		if (isValid()) {
			return siger.mod(partners);
		}
		return -1;
	}
	
	/**
	 * 根据签名，判断它属于这个站点坐标范围
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean allow(Siger siger) {
		boolean success = isValid();
		if (success) {
			success = (no == siger.mod(partners));
		}
		return success;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return Axes实例
	 */
	public SiteAxes duplicate() {
		return new SiteAxes(this);
	}

	/**
	 * 检查两个节点坐标一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SiteAxes.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SiteAxes) that) == 0;
	}

	/**
	 * 返回节点坐标的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (partners == 0) {
			return no;
		} else {
			return no ^ partners;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回节点坐标的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d#%d", no, partners);
	}

	/**
	 * 比较两个节点坐标相同
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SiteAxes that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(no, that.no);
		if (ret == 0) {
			ret = Laxkit.compareTo(partners, that.partners);
		}
		return ret;
	}

}