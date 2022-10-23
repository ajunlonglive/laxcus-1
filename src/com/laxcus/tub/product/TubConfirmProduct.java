/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.product;

import com.laxcus.util.classable.*;

/**
 * 执行确认结果
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public class TubConfirmProduct extends TubProduct {

	/** 成功标识 **/
	private boolean successful;

	/**
	 * 构造默认的确认返回结果
	 */
	protected TubConfirmProduct() {
		super();
		successful = false;
	}

	/**
	 * 建立确认返回结果的数据副本
	 * @param that TubConfirmProduct实例
	 */
	protected TubConfirmProduct(TubConfirmProduct that) {
		super(that);
		successful = that.successful;
	}

	/**
	 * 构造确认返回结果，指定它
	 * @param successful 成功或者失败
	 */
	public TubConfirmProduct(boolean successful) {
		this();
		setSuccessful(successful);
	}
	
	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 */
	public TubConfirmProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置成功标识
	 * @param b 成功标识
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断是成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 判断是失败
	 * @return 返回真或者假
	 */
	public boolean isFailed() {
		return !isSuccessful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(successful);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		successful = reader.readBoolean();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return isSuccessful() ? "successful" : "failed";
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TubConfirmProduct duplicate() {
		return new TubConfirmProduct(this);
	}

}