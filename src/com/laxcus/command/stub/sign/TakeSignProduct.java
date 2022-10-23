/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.sign;

import com.laxcus.access.stub.sign.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块签名处理结果
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public final class TakeSignProduct extends EchoProduct {

	private static final long serialVersionUID = 2202580148969204934L;

	/** 签名站点 **/
	private SignSite site;

	/**
	 * 构造默认的数据块签名处理结果
	 */
	public TakeSignProduct() {
		super();
	}

	/**
	 * 生成数据块签名处理结果的数据副本
	 * @param that TakeSignProduct实例
	 */
	private TakeSignProduct(TakeSignProduct that) {
		super(that);
		site = that.site;
	}

	/**
	 * 构造数据块签名处理结果，指定签名站点
	 * @param site SignSite实例
	 */
	public TakeSignProduct(SignSite site) {
		this();
		setSite(site);
	}
	
	/**
	 * 从可类化数据读取器中解析数据块签名处理结果
	 * @param reader 可类化数据读取器
	 */
	public TakeSignProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置签名站点
	 * @param e SignSite实例
	 */
	public void setSite(SignSite e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回签名站点
	 * @return SignSite实例
	 */
	public SignSite getSite() {
		return site;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public TakeSignProduct duplicate() {
		return new TakeSignProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		site = new SignSite(reader); 
	}

}