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
 * 获取账号结果
 * 
 * @author scott.liang
 * @version 1.1 4/9/2015
 * @since laxcus 1.0
 */
public class TakeAccountProduct extends EchoProduct {

	private static final long serialVersionUID = 2792428170979963971L;

	/** 账号实例 **/
	private Account account;

	/**
	 * 根据传入的账号集合，生成它的浅层副本
	 * @param that 获取账号结果实例
	 */
	private TakeAccountProduct(TakeAccountProduct that) {
		super(that);
		account = that.account;
	}
	
	/**
	 * 构造默认的账号集合
	 */
	public TakeAccountProduct() {
		super();
	}

	/**
	 * 构造默认的账号集合，指定账号
	 * @param account 账号实例，或者空指针
	 */
	public TakeAccountProduct(Account account) {
		super();
		setAccount(account);
	}
	
	/**
	 * 从可类化读取器中解析账号集合参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TakeAccountProduct(ClassReader reader) {
		super();
		resolve(reader);
	}
		
	/**
	 * 设置账号，允许空指针
	 * @param e 账号实例
	 */
	public void setAccount(Account e) {
		account = e;
	}
	
	/**
	 * 返回账号
	 * @return 账号实例
	 */
	public Account getAccount() {
		return account;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeAccountProduct duplicate() {
		return new TakeAccountProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(account);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		account = reader.readInstance(Account.class);
	}

}