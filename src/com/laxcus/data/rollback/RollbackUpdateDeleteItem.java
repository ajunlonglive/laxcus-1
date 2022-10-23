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
 * 更新操作的删除状态回滚文件单元
 * 
 * @author scott.liang
 * @version 1.1 7/23/2016
 * @since laxcus 1.0
 */
public class RollbackUpdateDeleteItem extends RollbackStubItem {

	private static final long serialVersionUID = 7199920417178952489L;

	/** 文件正则表达式 **/
	private final static String REGEX = "^([0-9A-F]+)_([0-9A-F]+)_([0-9A-F]+)" + RollbackArchive.UPDEL_SUFFIX + "$";

	/**
	 * 构造默认的更新操作的删除状态回滚文件单元
	 */
	private RollbackUpdateDeleteItem() {
		super();
	}

	/**
	 * 生成更新操作的删除状态回滚文件单元的数据副本
	 * @param that
	 */
	private RollbackUpdateDeleteItem(RollbackUpdateDeleteItem that) {
		super(that);
	}

	/**
	 * 构造更新操作的删除状态回滚文件单元，指定参数
	 * @param invokerId
	 * @param stub
	 * @param index
	 */
	public RollbackUpdateDeleteItem(long invokerId, long stub, int index) {
		super(invokerId, stub, index);
	}

	/**
	 * 解析更新操作的删除状态回滚文件单元的字符串文件
	 * @param input
	 */
	public RollbackUpdateDeleteItem(String input) {
		this();
		split(input);
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
		// 调用器编号
		setInvokerId(Long.parseLong(matcher.group(1), 16));
		// 数据块编号
		setStub(Long.parseLong(matcher.group(2), 16));
		// 索引号
		setIndex(Integer.parseInt(matcher.group(3), 16));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%X_%X_%X%s", getInvokerId(), getStub(), getIndex(),
				RollbackArchive.UPDEL_SUFFIX);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.data.rollback.RollbackItem#duplicate()
	 */
	@Override
	public RollbackItem duplicate() {
		return new RollbackUpdateDeleteItem(this);
	}

}