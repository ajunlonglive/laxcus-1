/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.util;

import java.io.*;

import com.laxcus.util.*;

/**
 * 数据单元测点
 * 
 * @author scott.liang
 * @version 1.05 4/23/2014
 * @since laxcus 1.0
 */
public class Vident implements  Comparable<Vident>, Serializable, Cloneable {

	private static final long serialVersionUID = -8338614973498219118L;

	/** 用户签名 **/
	private Siger siger;

	/** 统计已经分配到CALL/WORK/BUILD节点的数目 **/
	private int sites;

	/** 元数据尺寸 **/
	private long memory;

	/**
	 * 
	 */
	public Vident() {
		super();
		sites = 0;
		memory = 0L;
	}

	public Vident(Siger siger) {
		this();
		setSiger(siger);
	}

	/**
	 * 设置用户签名 
	 * @param e
	 */
	public void setSiger(Siger e) {
		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 增加站点分配次数
	 * @param i
	 */
	public void addSites(int i) {
		if (i < 0) {
			throw new IllegalValueException("must be >= 0, this is %d", i);
		}
		sites += i;
	}

	/**
	 * 返回站点分配次数
	 * @return
	 */
	public int getSites() {
		return sites;
	}

	/**
	 * 增加数据内存容量
	 * @param size
	 */
	public void addMemory(long size) {
		if (size < 0L) {
			throw new IllegalValueException("must be >= 0, this is %d", size);
		}
		memory += size;
	}

	/**
	 * 返回数据内存容量
	 * @return
	 */
	public long getMemory() {
		return memory;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Vident that) {
		if (that == null) {
			return 1;
		}

		// 分配顺序：分配次数小的排在前面，空间大的排在前面
		int ret = Laxkit.compareTo(sites, that.sites);
		if (ret == 0) {
			ret = Laxkit.compareTo(that.memory, memory);
		}
		return ret;
	}

	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	//	 */
	//	@Override
	//	public int build(ClassWriter writer) {
	//		// TODO Auto-generated method stub
	//		return 0;
	//	}
	//
	//	/* (non-Javadoc)
	//	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	//	 */
	//	@Override
	//	public int resolve(ClassReader reader) {
	//		// TODO Auto-generated method stub
	//		return 0;
	//	}

}
