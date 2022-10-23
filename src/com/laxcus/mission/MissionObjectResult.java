/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.mission;

import com.laxcus.util.*;

/**
 * 包含各种对象的驱动处理结果
 * 
 * @author scott.liang
 * @version 1.0 5/2/2013
 * @since laxcus 1.0
 */
public class MissionObjectResult extends MissionResult {

	/** 异步报告 **/
	private Object object;

	/**
	 * 构造默认的驱动处理结果
	 */
	public MissionObjectResult() {
		super(MissionResultTag.OBJECT);
	}

	/**
	 * 构造驱动处理结果，指定各种对象
	 * @param e 各种对象
	 */
	public MissionObjectResult(Object e) {
		this();
		setObject(e);
	}

	/**
	 * 设置各种对象。<br>
	 * 将类实例转换成字节保存。
	 * 
	 * @param e 各种对象实例
	 */
	public void setObject(Object e) {
		object = e;
		if (e != null) {
			setThumb(e.getClass());
		}
	}

	/**
	 * 返回各种对象
	 * @return 各种对象实例
	 */
	public Object getObject() {
		return object;
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
		if (object == null) {
			return null;
		} else if (!Laxkit.isClassFrom(object, clazz)) {
			String e = String.format("%s != %s", object.getClass().getName(), clazz.getName());
			throw new ClassCastException(e);
		}
		// 返回类实例
		return (T) object;
	}

	/**
	 * 判断结果是指定类类型
	 * @param clazz 类定义
	 * @return 返回真或者假
	 */
	public boolean isObject(java.lang.Class<?> clazz) {
		return object != null && Laxkit.isClassFrom(object, clazz);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.driver.mission.TubResult#destroy()
	 */
	@Override
	protected void destroy() {
		super.destroy();
		// 处理结果
		if (object != null) {
			object = null;
		}
	}
}