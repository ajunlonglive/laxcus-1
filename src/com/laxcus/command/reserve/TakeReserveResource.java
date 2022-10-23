/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.reserve;

import com.laxcus.util.classable.*;

/**
 * 获取备份资源数据 <br>
 * 
 * “TakeReserveResource”由TOP/HOME监视站点的“CommitReserveResource”发出，目标是TOP/HOME管理站点，来获取指定的资源数据文件。
 * 
 * @author scott.liang
 * @version 1.1 11/21/2015
 * @since laxcus 1.0
 */
public final class TakeReserveResource extends ReserverResource {

	private static final long serialVersionUID = -2171021796619169914L;

	/**
	 * 构造默认和私有的获取备份资源数据命令
	 */
	private TakeReserveResource() {
		super();
	}

	/**
	 * 构造获取备份资源数据命令，指定文件路径
	 * 
	 * @param path 文件路径
	 */
	public TakeReserveResource(String path) {
		this();
		setResource(path);
	}

	/**
	 * 生成获取备份资源数据的数据副本
	 * 
	 * @param that TakeReserveResource实例
	 */
	private TakeReserveResource(TakeReserveResource that) {
		super(that);
	}

	/**
	 * 从可类化读取器中解析获取备份资源数据
	 * 
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeReserveResource(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeReserveResource duplicate() {
		return new TakeReserveResource(this);
	}

}