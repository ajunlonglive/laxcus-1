/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.account;

import com.laxcus.access.diagram.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 获取系统管理员账号结果
 * 
 * @author scott.liang
 * @version 1.0 7/28/2018
 * @since laxcus 1.0
 */
public class TakeAdministratorProduct extends EchoProduct {

	private static final long serialVersionUID = 2792428170979963971L;

	/** 系统管理员实例 **/
	private Administrator administrator;

	/**
	 * 根据传入的命令实例，生成它的浅层副本
	 * @param that 获取系统管理员账号结果实例
	 */
	private TakeAdministratorProduct(TakeAdministratorProduct that) {
		super(that);
		administrator = that.administrator;
	}
	
	/**
	 * 构造默认的系统管理员账号结果
	 */
	public TakeAdministratorProduct() {
		super();
	}

	/**
	 * 构造默认的系统管理员账号结果，指定系统管理员
	 * @param administrator 系统管理员实例，或者空指针
	 */
	public TakeAdministratorProduct(Administrator administrator) {
		super();
		setAdministrator(administrator);
	}
	
	/**
	 * 从可类化读取器中解析系统管理员参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeAdministratorProduct(ClassReader reader) {
		super();
		resolve(reader);
	}
		
	/**
	 * 设置系统管理员，允许空指针
	 * @param e 系统管理员实例
	 */
	public void setAdministrator(Administrator e) {
		administrator = e;
	}
	
	/**
	 * 返回系统管理员
	 * @return 系统管理员实例
	 */
	public Administrator getAdministrator() {
		return administrator;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeAdministratorProduct duplicate() {
		return new TakeAdministratorProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(administrator);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		administrator = reader.readInstance(Administrator.class);
	}

}