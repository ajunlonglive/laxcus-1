/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * 设置缓存映像数据
 * 
 * @author scott.liang
 * @version 1.1 6/13/2015
 * @since laxcus 1.0
 */
public final class SetCacheReflexData extends SetReflexData {

	private static final long serialVersionUID = 8286937671650875793L;

	/**
	 * 根据传入的命令实例，生成它的数据副本
	 * @param that SetCacheReflexData实例
	 */
	private SetCacheReflexData(SetCacheReflexData that) {
		super(that);
	}
	
	/**
	 * 构造默认的设置缓存映像数据实例
	 */
	public SetCacheReflexData() {
		super();
	}

	/**
	 * 构造设置缓存映像数据实例，指定全部参数
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @param data 映像数据
	 * @since 1.1
	 */
	public SetCacheReflexData(Space space, long stub, byte[] data) {
		this();
		setFlag(space, stub);
		setData(data);
	}

	/**
	 * 从可类化数据读取器中解析缓存映像数据
	 * @param reader 可类化数据读取器
	 * @since l.1
	 */
	public SetCacheReflexData(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetCacheReflexData duplicate() {
		return new SetCacheReflexData(this);
	}

}
