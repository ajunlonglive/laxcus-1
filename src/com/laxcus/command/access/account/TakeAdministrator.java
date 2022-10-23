/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.account;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

/**
 * 获取系统管理员账号。<br>
 * 
 * 命令从GATE站点发出，目标是BANK站点。
 * 
 * @author scott.liang
 * @version 1.0 7/28/2018
 * @since laxcus 1.0
 */
public final class TakeAdministrator extends Command {

	private static final long serialVersionUID = 3808155278146081017L;

	/**
	 * 根据传入实例，生成它的浅层数据副本
	 * @param that TakeAdministrator实例
	 */
	private TakeAdministrator(TakeAdministrator that) {
		super(that);
	}

	/**
	 * 构造默认的获取系统管理员账号命令
	 */
	public TakeAdministrator() {
		super();
	}
	
	/**
	 * 从可类化数据读取器中解析获取系统管理员账号命令
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeAdministrator(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public TakeAdministrator duplicate() {
		return new TakeAdministrator(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		
	}

}