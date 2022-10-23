/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.refer;

import com.laxcus.access.diagram.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 获得被授权用户账号处理结果
 * 
 * @author scott.liang
 * @version 1.0 7/28/2017
 * @since laxcus 1.0
 */
public final class TakeReferProduct extends EchoProduct {
	
	private static final long serialVersionUID = 8993572201356023987L;
	
	/** 被授权用户资源引用 **/
	private Refer refer;

	/**
	 * 构造默认的获得被授权用户账号处理结果
	 */
	private TakeReferProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析获得被授权用户账号处理结果
	 * @param reader 可类化数据读取器
	 */
	public TakeReferProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造获得被授权用户账号处理结果，指定用户资源引用
	 * @param refer 用户资源引用
	 */
	public TakeReferProduct(Refer refer) {
		this();
		setRefer(refer);
	}

	/**
	 * 生成获得被授权用户账号处理结果的数据副本
	 * @param that TakeReferProduct实例
	 */
	private TakeReferProduct(TakeReferProduct that) {
		super(that);
		refer = that.refer;
	}

	/**
	 * 设置用户资源引用
	 * @param e Refer实例
	 */
	public void setRefer(Refer e) {
		Laxkit.nullabled(e);
		refer = e;
	}
	
	/**
	 * 返回用户资源引用
	 * @return Refer实例
	 */
	public Refer getRefer() {
		return refer;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeReferProduct duplicate() {
		return new TakeReferProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(refer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		refer = new Refer(reader);
	}

}