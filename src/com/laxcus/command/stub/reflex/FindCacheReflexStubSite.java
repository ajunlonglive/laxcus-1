/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;
import com.laxcus.util.classable.*;

/**
 * 查询缓存映像数据块站点。<br>
 * 
 * 这个命令由DATA发出，目标是CALL站点。CALL检索本地保存的缓存映像数据块注册记录，返回对应的DATA站点。
 * 如果没有缓存映像数据块站点，从当前的“DATA从站点”中选择一个。DATA主站点不保存缓存映像数据
 * 
 * @author scott.liang
 * @version 1.1 7/10/2015
 * @since laxcus 1.0
 */
public final class FindCacheReflexStubSite extends FindReflexStubSite {

	private static final long serialVersionUID = 8499818345868558243L;

	/**
	 * 构造默认和私有的查询缓存映像数据块站点
	 */
	private FindCacheReflexStubSite() {
		super();
	}

	/**
	 * 根据传入的查询缓存映像数据块站点实例，生成它的浅层数据副本
	 * @param that 查询缓存映像数据块站点命令
	 */
	private FindCacheReflexStubSite(FindCacheReflexStubSite that) {
		super(that);
	}

	/**
	 * 构造查询缓存映像数据块站点，指定数据块标识
	 * @param flag 数据块标识
	 */
	public FindCacheReflexStubSite(StubFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 构造查询缓存映像数据块站点，指定数据表名和数据块编号
	 * @param space 数据表名
	 * @param stub 数据块编号
	 */
	public FindCacheReflexStubSite(Space space, long stub) {
		this();
		setFlag(space, stub);
	}

	/**
	 * 从可类化数据读取器中解析查询缓存映像数据块站点命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindCacheReflexStubSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindCacheReflexStubSite duplicate() {
		return new FindCacheReflexStubSite(this);
	}

}