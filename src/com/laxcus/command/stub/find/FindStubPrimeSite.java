/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.find;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * 从CALL站点地址池中，随机选择一个关联数据块编号的DATA站点地址。<br>
 * 
 * 此命令由WORK站点发出，目标是CALL站点。根据数据块编号，查找数据块所属的DATA站点地址。
 * 
 * @author scott.liang
 * @version 1.1 07/12/2015
 * @since laxcus 1.0
 */
public final class FindStubPrimeSite extends FindStubSite {

	private static final long serialVersionUID = 5575026004657491377L;

	/**
	 * 根据传入实例，生成FindStubPrimeSite命令副本
	 * @param that FindStubPrimeSite实例
	 */
	private FindStubPrimeSite(FindStubPrimeSite that) {
		super(that);
	}

	/**
	 * 构造默认的FindStubPrimeSite命令
	 */
	public FindStubPrimeSite() {
		super();
	}

	/**
	 * 构造FindStubPrimeSite，保存一个数据块编号
	 * @param stub 数据块编号
	 */
	public FindStubPrimeSite(long stub) {
		super();
		add(stub);
	}

	/**
	 * 构造FindStubPrimeSite，保存一批数据块编号
	 * @param stubs 数据块编号列表
	 */
	public FindStubPrimeSite(List<Long> stubs) {
		this();
		addAll(stubs);
	}
	
	/**
	 * 构造FindStubPrimeSite，保存表名和数据块编号
	 * @param space 表名
	 * @param stubs 数据块编号
	 */
	public FindStubPrimeSite(Space space, List<Long> stubs) {
		this();
		setSpace(space);
		addAll(stubs);
	}

	/**
	 * 从可类化数据读取器中解析FindStubPrimeSite参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindStubPrimeSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindStubPrimeSite duplicate() {
		return new FindStubPrimeSite(this);
	}

}