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
 * 提交备份资源数据 <br>
 * 
 * “CommitReserveResource”命令由TOP/HOME管理站点发出，被投递到TOP/HOME镜像站点，TOP/HOME镜像站点产生“TakeReserveRsource”命令。
 * 
 * @author scott.liang
 * @version 1.1 11/21/2015
 * @since laxcus 1.0
 */
public final class CommitReserveResource extends ReserverResource {

	private static final long serialVersionUID = 2397725166962325806L;

	/**
	 * 构造默认和私有的提交备份资源数据命令
	 */
	private CommitReserveResource() {
		super();
	}

	/**
	 * 生成提交备份资源数据的数据副本
	 * 
	 * @param that CommitReserveResource实例
	 */
	private CommitReserveResource(CommitReserveResource that) {
		super(that);
	}

	/**
	 * 构造提交备份资源数据命令，指定文件路径
	 * 
	 * @param path 文件路径
	 */
	public CommitReserveResource(String path) {
		this();
		setResource(path);
	}

	/**
	 * 从可类化读取器中解析提交备份资源数据
	 * 
	 * @param reader - 可类化数据读取器
	 * @since 1.1
	 */
	public CommitReserveResource(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CommitReserveResource duplicate() {
		return new CommitReserveResource(this);
	}

}
