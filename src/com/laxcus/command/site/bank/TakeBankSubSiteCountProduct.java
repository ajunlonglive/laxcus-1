/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.bank;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 获得BANK子站点数目反馈结果。<br>
 * BANK站点发出、ACCOUNT/HASH/GATE/ENTRANCE站点接收。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class TakeBankSubSiteCountProduct extends EchoProduct {

	private static final long serialVersionUID = 8161303914797806813L;
	
	/** 站点统计数 **/
	private int count;
	
	/**
	 * 构造默认的获得BANK子站点数目命令
	 */
	public TakeBankSubSiteCountProduct() {
		super();
	}
	
	/**
	 * 构造获得BANK子站点数目命令，指定数目
	 * @param count 统计数
	 */
	public TakeBankSubSiteCountProduct(int count) {
		this();
		this.setCount(count);
	}

	/**
	 * 生成获得BANK子站点数目的数据副本
	 * @param that 获得BANK子站点数目
	 */
	private TakeBankSubSiteCountProduct(TakeBankSubSiteCountProduct that) {
		super(that);
		count = that.count;
	}

	/**
	 * 从可类化数据读取器中解析获得BANK子站点数目
	 * @param reader 可类化数据读取器
	 */
	public TakeBankSubSiteCountProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置统计数
	 * @param i
	 */
	public void setCount(int i) {
		count = i;
	}

	/**
	 * 返回统计数
	 * @return
	 */
	public int getCount() {
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeBankSubSiteCountProduct duplicate() {
		return new TakeBankSubSiteCountProduct(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 统计数
		writer.writeInt(count);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 统计数
		count = reader.readInt();
	}

}