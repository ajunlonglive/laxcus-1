/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.rollback;

import com.laxcus.util.*;

/**
 * 数据块回滚文件单元
 * 
 * @author scott.liang
 * @version 1.1 7/23/2016
 * @since laxcus 1.0
 */
public abstract class RollbackStubItem extends RollbackItem {

	private static final long serialVersionUID = -5568255688561540184L;

	/** 数据块编号 **/
	private long stub;

	/** 索引号 **/
	private int index;

	/**
	 * 构造默认的数据块回滚文件单元
	 */
	protected RollbackStubItem() {
		super();
	}

	/**
	 * 生成数据块回滚文件单元的数据副本
	 * @param that
	 */
	protected RollbackStubItem(RollbackStubItem that) {
		super(that);
		stub = that.stub;
		index = that.index;
	}

	/**
	 * 构造数据块回滚文件单元，指定参数
	 * @param invokerId - 调用器编号
	 * @param stub - 数据块编号
	 * @param index - 索引号
	 */
	protected RollbackStubItem(long invokerId, long stub, int index) {
		this();
		setInvokerId(invokerId);
		setStub(stub);
		setIndex(index);
	}

	/**
	 * 设置数据块编号
	 * @param i
	 */
	public void setStub(long i) {
		stub = i;
	}

	/**
	 * 返回数据块编号
	 * @return
	 */
	public long getStub() {
		return stub;
	}

	/**
	 * 设置索引号
	 * @param i
	 */
	public void setIndex(int i) {
		index = i;
	}

	/**
	 * 返回索引号
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.data.rollback.RollbackItem#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (getInvokerId() ^ stub ^ index);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RollbackItem that) {
		// 比较上级
		int ret = super.compareTo(that);
		// 比较本地
		if (ret == 0 && this.getClass() == that.getClass()) {
			RollbackStubItem e = (RollbackStubItem) that;
			ret = Laxkit.compareTo(stub, e.stub);
			if (ret == 0) ret = Laxkit.compareTo(index, e.index);
		}
		return ret;
	}

}