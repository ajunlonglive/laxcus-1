/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.reflex;

import com.laxcus.command.stub.transfer.*;
import com.laxcus.util.classable.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.stub.*;

/**
 * 删除缓存映像块命令  <br>
 * 
 * 这个命令由DATA主节点发出，目标是关联的DATA从节点。从节点收到此命令后，删除本地的缓存映像块
 * 
 * @author scott.liang
 * @version 1.1 6/13/2015
 * @since laxcus 1.0
 */
public final class DeleteCacheReflex extends TransferMass {

	private static final long serialVersionUID = 6588166454042868776L;

	/**
	 * 构造默认的删除缓存映像块
	 */
	private DeleteCacheReflex() {
		super();
	}

	/**
	 * 根据传入的删除缓存映像块命令，生成它的数据副本
	 * @param that DeleteCacheReflex实例
	 */
	private DeleteCacheReflex(DeleteCacheReflex that) {
		super(that);
	}

	/**
	 * 构造删除缓存映像块，指定数据块标识
	 * @param flag 数据块标识
	 */
	public DeleteCacheReflex(StubFlag flag) {
		this();
		setFlag(flag);
	}
	
	/**
	 * 构造删除缓存映像块，指定数据表名和数据块编号
	 * @param space 数据表名
	 * @param stub 缓存块编号
	 */
	public DeleteCacheReflex(Space space, long stub) {
		this();
		setFlag(space, stub);
	}

	/**
	 * 从可类化数据读取器中解析删除缓存映像块命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DeleteCacheReflex(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DeleteCacheReflex duplicate() {
		return new DeleteCacheReflex(this);
	}

}