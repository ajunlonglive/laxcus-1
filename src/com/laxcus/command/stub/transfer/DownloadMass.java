/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.transfer;

import com.laxcus.access.stub.*;
import com.laxcus.util.classable.*;

/**
 * 下载数据块命令 <br>
 * 
 * 这是一个数据块下载命令，在PRIME-DATA/SLAVE-DATA/BUILD站点之间进行，通过网络传输一个数据块。对于请求端是一个下载操作，对于服务端来说是一个上传操作。
 * 
 * @author scott.liang
 * @version 1.1 5/17/2015
 * @since laxcus 1.0
 */
public class DownloadMass extends TransferMass {

	private static final long serialVersionUID = -7616952372920783055L;

	/**
	 * 构造默认和私有的下载数据块命令
	 */
	private DownloadMass() {
		super();
	}

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that DownloadMass实例
	 */
	private DownloadMass(DownloadMass that) {
		super(that);
	}

	/**
	 * 构造下载数据块命令，指定数据块标识
	 * @param flag 数据块标识
	 */
	public DownloadMass(StubFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 从可类化数据读取器中解析下载数据块命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DownloadMass(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public DownloadMass duplicate() {
		return new DownloadMass(this);
	}

}