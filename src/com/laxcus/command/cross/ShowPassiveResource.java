/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import com.laxcus.command.access.user.*;
import com.laxcus.util.classable.*;

/**
 * 显示被授权的数据资源 <br><br>
 * 
 * 被授权人操作，显示授权人分享给自己的数据资源。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/30/2017
 * @since laxcus 1.0
 */
public class ShowPassiveResource extends MultiUser {

	private static final long serialVersionUID = 9071080830954995017L;

	/**
	 * 构造默认的显示被授权的数据资源
	 */
	public ShowPassiveResource() {
		super();
	}

	/**
	 * 生成显示被授权的数据资源的数据副本
	 * @param that 显示被授权的数据资源实例
	 */
	private ShowPassiveResource(ShowPassiveResource that) {
		super(that);
	}
	
	/**
	 * 从可类化数据读取器中解析显示被授权的数据资源
	 * @param reader 可类化数据读取器
	 */
	public ShowPassiveResource (ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 判断是全部授权人
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return getUserSize() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShowPassiveResource duplicate() {
		return new ShowPassiveResource(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}

}