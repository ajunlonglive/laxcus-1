/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.command.access.user.*;
import com.laxcus.util.classable.*;

/**
 * 检查集群分布成员。<br>
 * WATCH节点调用
 * 
 * @author scott.liang
 * @version 1.0 2/8/2020
 * @since laxcus 1.0
 */
public class CheckDistributedMember extends MultiUser {

	private static final long serialVersionUID = 7629658262279920474L;

	/**
	 * 构造默认的检查集群分布成员
	 */
	public CheckDistributedMember() {
		super();
	}

	/**
	 * 构造默认的检查集群分布成员
	 */
	public CheckDistributedMember(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成检查集群分布成员副本
	 * @param that 检查集群分布成员
	 */
	private CheckDistributedMember(MultiUser that) {
		super(that);
	}

	/**
	 * 判断是显示全部
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return getUserSize() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public CheckDistributedMember duplicate() {
		return new CheckDistributedMember(this);
	}

}