/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.rollback;

import java.io.*;

import com.laxcus.util.*;

/**
 * 更新操作的回滚文件单元
 * 
 * @author scott.liang
 * @version 1.1 7/23/2016
 * @since laxcus 1.0
 */
public class RollbackUpdateItem implements Cloneable, Comparable<RollbackUpdateItem>, Serializable {

	private static final long serialVersionUID = 5352590291626440651L;

	/** 删除单元 **/
	private RollbackUpdateDeleteItem deleteItem;

	/** 插入单元 **/
	private RollbackUpdateInsertItem insertItem;

	/**
	 * 构造默认的更新操作的回滚文件单元
	 */
	private RollbackUpdateItem() {
		super();
	}

	/**
	 * 更新操作的回滚文件单元，指定参数
	 * @param delete 删除单元
	 * @param insert 插入单元
	 */
	public RollbackUpdateItem(RollbackUpdateDeleteItem delete, RollbackUpdateInsertItem insert) {
		this();
		setDeleteItem(delete);
		setInsertItem(insert);
	}

	/**
	 * 生成更新操作的回滚文件单元的数据副本
	 * @param that RollbackUpdateItem实例
	 */
	private RollbackUpdateItem(RollbackUpdateItem that) {
		this();
		deleteItem = that.deleteItem;
		insertItem = that.insertItem;
	}

	/**
	 * 设置插入单元
	 * @param e RollbackUpdateInsertItem实例
	 */
	public void setInsertItem(RollbackUpdateInsertItem e) {
		Laxkit.nullabled(e);

		insertItem = e;
	}

	/**
	 * 返回插入单元
	 * @return RollbackUpdateInsertItem实例
	 */
	public RollbackUpdateInsertItem getInsertItem() {
		return insertItem;
	}

	/**
	 * 设置删除单元
	 * @param e RollbackUpdateDeleteItem实例
	 */
	public void setDeleteItem(RollbackUpdateDeleteItem e) {
		Laxkit.nullabled(e);

		deleteItem = e;
	}

	/**
	 * 返回删除单元
	 * @return RollbackUpdateDeleteItem实例
	 */
	public RollbackUpdateDeleteItem getDeleteItem() {
		return deleteItem;
	}

	/**
	 * 生成数据副本
	 * @return RollbackUpdateItem实例
	 */
	public RollbackUpdateItem duplicate() {
		return new RollbackUpdateItem(this);
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (deleteItem.hashCode() ^ insertItem.hashCode());
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RollbackUpdateItem that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(insertItem, that.insertItem);
		if (ret == 0) {
			ret = Laxkit.compareTo(deleteItem, that.deleteItem);
		}
		return ret;
	}

}