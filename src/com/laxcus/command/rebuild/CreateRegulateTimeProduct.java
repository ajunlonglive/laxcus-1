/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.rebuild;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 建立数据优化时间处理结果
 * 
 * @author scott.liang
 * @version 1.1 12/01/2015
 * @since laxcus 1.0
 */
public final class CreateRegulateTimeProduct extends ConfirmProduct {
	
	private static final long serialVersionUID = 3446877934759048921L;

	/** 触发时间 **/
	private SwitchTime switchTime;

	/**
	 * 构造默认的建立数据优化时间处理结果
	 */
	private CreateRegulateTimeProduct() {
		super();
	}

	/**
	 * 构造建立数据优化时间处理结果，指定成功标识
	 * @param switchTime 触发时间
	 * @param successful 成功标识
	 */
	public CreateRegulateTimeProduct(SwitchTime switchTime, boolean successful) {
		this();
		setSwitchTime(switchTime);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中解析建立数据优化时间处理结果
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CreateRegulateTimeProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成建立数据优化时间处理结果数据副本
	 * @param that CreateRegulateTimeProduct实例
	 */
	private CreateRegulateTimeProduct(CreateRegulateTimeProduct that) {
		super(that);
		switchTime = that.switchTime;
	}

	/**
	 * 设置触发时间
	 * @param e 触发时间
	 */
	public void setSwitchTime(SwitchTime e) {
		Laxkit.nullabled(e);
		switchTime = e;
	}

	/**
	 * 返回触发时间
	 * @return 触发时间
	 */
	public SwitchTime getSwitchTime() {
		return switchTime;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CreateRegulateTimeProduct duplicate() {
		return new CreateRegulateTimeProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(switchTime);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		switchTime = new SwitchTime(reader);
	}

}