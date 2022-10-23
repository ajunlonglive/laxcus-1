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
 * 查询存储映像数据块站点。<br>
 * 
 * 这个命令由DATA发出，目标是CALL站点。CALL检索本地保存的存储映像数据块注册记录，返回对应的DATA站点。
 * 
 * @author scott.liang
 * @version 1.1 7/10/2015
 * @since laxcus 1.0
 */
public final class FindChunkReflexStubSite extends FindReflexStubSite {

	private static final long serialVersionUID = 1895538497276710127L;

	/**
	 * 构造默认和私有的查询存储映像数据块站点
	 */
	private FindChunkReflexStubSite() {
		super();
	}

	/**
	 * 根据传入的查询存储映像数据块站点实例，生成它的浅层数据副本
	 * @param that 查询存储映像数据块站点命令
	 */
	private FindChunkReflexStubSite(FindChunkReflexStubSite that) {
		super(that);
	}

	/**
	 * 构造查询存储映像数据块站点，指定数据块标识
	 * @param flag 数据块标识
	 */
	public FindChunkReflexStubSite(StubFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 构造查询存储映像数据块站点，指定数据表名和数据块编号
	 * @param space 数据表名
	 * @param stub 数据块编号
	 */
	public FindChunkReflexStubSite(Space space, long stub) {
		this();
		setFlag(space, stub);
	}

	/**
	 * 从可类化数据读取器中解析查询存储映像数据块站点命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FindChunkReflexStubSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public FindChunkReflexStubSite duplicate() {
		return new FindChunkReflexStubSite(this);
	}

}