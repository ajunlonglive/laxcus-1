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
 * 派发备份资源数据 <br>
 * 
 * 当TOP/HOME管理站点更新本地资源数据后，产生这个命令，交给它们的命令管理池去处理。
 * 
 * @author scott.liang
 * @version 1.1 11/21/2015
 * @since laxcus 1.0
 */
public final class DispatchReserveResource extends ReserverResource {

	private static final long serialVersionUID = 2397725166962325806L;

	/**
	 * 构造默认和私有的派发备份资源数据命令
	 */
	private DispatchReserveResource() {
		super();
	}

	/**
	 * 生成派发备份资源数据的数据副本
	 * 
	 * @param that DispatchReserveResource实例
	 */
	private DispatchReserveResource(DispatchReserveResource that) {
		super(that);
	}

	/**
	 * 构造派发备份资源数据命令，指定文件路径
	 * 
	 * @param path 文件路径
	 */
	public DispatchReserveResource(String path) {
		this();
		setResource(path);
	}

	/**
	 * 从可类化读取器中解析派发备份资源数据
	 * 
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DispatchReserveResource(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DispatchReserveResource duplicate() {
		return new DispatchReserveResource(this);
	}

}
