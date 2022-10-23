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
 * 显示授权资源 <br>
 * 
 * 授权人操作，从FRONT站点发往GATE站点，显示他自己授权的共享资源。<br>
 * 
 * 授权资源由数据持有人（授权人）分享给被授权人，授权人也可以随时回收自己的授权资源。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class ShowOpenResource extends MultiUser {

	private static final long serialVersionUID = 6925283396870784492L;

	/**
	 * 构造默认显示授权资源
	 */
	public ShowOpenResource() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析显示授权资源
	 * @param reader 可类化数据读取器
	 */
	public ShowOpenResource(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成显示授权资源的数据副本
	 * @param that 显示授权资源实例
	 */
	private ShowOpenResource(ShowOpenResource that) {
		super(that);
	}

	/**
	 * 判断是全部被授权人
	 * @return 返回真或者假
	 */
	public boolean isAll() {
		return getUserSize() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ShowOpenResource duplicate() {
		return new ShowOpenResource(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.endow.ShareCross#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.endow.ShareCross#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}
	
}