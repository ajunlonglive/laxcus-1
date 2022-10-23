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
 * 设置存储映像数据
 * 
 * @author scott.liang
 * @version 1.1 6/13/2015
 * @since laxcus 1.0
 */
public final class SetChunkReflexData extends SetReflexData {

	private static final long serialVersionUID = 7987992483218646087L;

	/**
	 * 根据传入的命令实例，生成它的数据副本
	 * @param that SetChunkReflexData实例
	 */
	private SetChunkReflexData(SetChunkReflexData that) {
		super(that);
	}
	
	/**
	 * 构造默认的设置存储映像数据实例
	 */
	public SetChunkReflexData() {
		super();
	}

	/**
	 * 构造设置存储映像数据实例，指定映全部参数
	 * @param space 数据表名
	 * @param stub 数据块编号
	 * @param data 映像数据
	 * @since 1.1
	 */
	public SetChunkReflexData(Space space, long stub, byte[] data) {
		this();
		setFlag(space, stub);
		setData(data);
	}

	/**
	 * 从可类化数据读取器中解析固态映像数据
	 * @param reader 可类化数据读取器
	 * @since l.1
	 */
	public SetChunkReflexData(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetChunkReflexData duplicate() {
		return new SetChunkReflexData(this);
	}

}