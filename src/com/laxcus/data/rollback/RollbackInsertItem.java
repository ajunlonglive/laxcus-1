/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.rollback;

import java.util.regex.*;

import com.laxcus.util.*;

/**
 * 插入操作的回滚文件单元
 * 
 * @author scott.liang
 * @version 1.1 7/23/2016
 * @since laxcus 1.0
 */
public class RollbackInsertItem extends RollbackItem {

	private static final long serialVersionUID = 642041594227491657L;

	/** 文件正则表达式 **/
	private final static String REGEX = "^([0-9A-F]+)_([0-9A-F]+)" + RollbackArchive.INSERT_SUFFIX + "$";

	/** 索引号 **/
	private int index;

	/**
	 * 构造默认的插入操作的回滚文件单元
	 */
	private RollbackInsertItem() {
		super();
	}

	/**
	 * 生成插入操作的回滚文件单元的数据副本
	 * @param that
	 */
	private RollbackInsertItem(RollbackInsertItem that) {
		this();
		index = that.index;
	}

	/**
	 * 构造插入操作的回滚文件单元，指定参数
	 * @param invokerId
	 * @param index
	 */
	public RollbackInsertItem(long invokerId, int index) {
		this();
		setInvokerId(invokerId);
		setIndex(index);
	}

	/**
	 * 解析插入操作的回滚文件单元的字符串文件
	 * @param input
	 */
	public RollbackInsertItem(String input) {
		this();
		split(input);
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

	/**
	 * 判断是回滚文件
	 * @param input - 输入名称
	 * @return - 返回真或者假
	 */
	public static boolean validate(String input) {
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}

	/**
	 * 解析文件名
	 * @param input
	 * @throws IllegalValueException
	 */
	private void split(String input) {
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new IllegalValueException("illegal name:%s", input);
		}
		setInvokerId(Long.parseLong(matcher.group(1), 16));
		setIndex(Integer.parseInt(matcher.group(2), 16));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%X_%X%s", getInvokerId(), index,
				RollbackArchive.INSERT_SUFFIX);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.data.rollback.RollbackItem#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (getInvokerId() ^ index);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RollbackItem that) {
		int ret = super.compareTo(that);
		if (ret == 0 && getClass() == RollbackInsertItem.class) {
			RollbackInsertItem e = (RollbackInsertItem) that;
			ret = Laxkit.compareTo(index, e.index);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.data.rollback.RollbackItem#duplicate()
	 */
	@Override
	public RollbackItem duplicate() {
		return new RollbackInsertItem(this);
	}

}