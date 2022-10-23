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
 * 查找数据块DATA从站点 <br>
 * 
 * @author scott.liang
 * @version 1.1 12/7/2015
 * @since laxcus 1.0
 */
public final class FindStubSlaveSite extends FindStubSite {

	private static final long serialVersionUID = -1568707891692386952L;

	/**
	 * 根据传入的查找数据块DATA从站点命令，生成它的数据副本
	 * @param that 查询子块站点地址命令
	 */
	private FindStubSlaveSite(FindStubSlaveSite that) {
		super(that);
	}

	/**
	 * 构造默认的查找数据块DATA从站点命令
	 */
	public FindStubSlaveSite() {
		super();
	}

	/**
	 * 构造查找数据块DATA从站点命令，指定数据块编号
	 * @param stub 数据块编号
	 */
	public FindStubSlaveSite(long stub) {
		this();
		add(stub);
	}

	/**
	 * 构造查找数据块DATA从站点命令，指定一批数据块编号
	 * @param stubs 数据块编号数组
	 */
	public FindStubSlaveSite(Collection<Long> stubs) {
		this();
		addAll(stubs);
	}

	/**
	 * 构造查找数据块DATA从站点，指定表名和数据块编号
	 * @param space 表名
	 * @param stub 数据块编号
	 */
	public FindStubSlaveSite(Space space, long stub) {
		this();
		setSpace(space);
		add(stub);
	}

	/**
	 * 构造查找数据块DATA从站点，指定表名和一批数据块编号
	 * @param space 表名
	 * @param stubs 数据块编号数组
	 */
	public FindStubSlaveSite(Space space, Collection<Long> stubs) {
		this();
		setSpace(space);
		addAll(stubs);
	}

	/**
	 * 从可类化数据读取器中解析查找数据块DATA从站点命令参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindStubSlaveSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindStubSlaveSite duplicate() {
		return new FindStubSlaveSite(this);
	}

}