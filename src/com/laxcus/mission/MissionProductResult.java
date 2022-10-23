/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;

/**
 * 包含回显报告的驱动处理结果
 * 
 * @author scott.liang
 * @version 1.0 5/2/2013
 * @since laxcus 1.0
 */
public class MissionProductResult extends MissionResult {

	/** 异步报告 **/
	private EchoProduct product;

	/**
	 * 构造默认的驱动处理结果
	 */
	public MissionProductResult() {
		super(MissionResultTag.PRODUCT);
	}

	/**
	 * 构造驱动处理结果，指定回显报告
	 * @param e 回显报告
	 */
	public MissionProductResult(EchoProduct e) {
		this();
		setProduct(e);
	}

	/**
	 * 设置回显报告。<br>
	 * 将类实例转换成字节保存。
	 * 
	 * @param e 回显报告实例
	 */
	public void setProduct(EchoProduct e) {
		product = e;
		if (e != null) {
			setThumb(e.getClass());
		}
	}

	/**
	 * 返回回显报告
	 * @return 回显报告实例
	 */
	public EchoProduct getProduct() {
		return product;
	}

	/**
	 * 返回指定的类实例
	 * @param <T> 泛式类型
	 * @param clazz 类定义
	 * @return 返回指定类实例，没有返回空指针，不匹配弹出错误。
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Class<T> clazz) {
		if (product == null) {
			return null;
		} else if (!Laxkit.isClassFrom(product, clazz)) {
			String e = String.format("%s != %s", product.getClass().getName(), clazz.getName());
			throw new ClassCastException(e);
		}
		// 返回类实例
		return (T) product;
	}

	/**
	 * 判断结果是指定类类型
	 * @param clazz 类定义
	 * @return 返回真或者假
	 */
	public boolean isProduct(java.lang.Class<?> clazz) {
		return product != null && Laxkit.isClassFrom(product, clazz);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.driver.mission.TubResult#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
		// 处理结果
		if (product != null) {
			product = null;
		}
	}
}