/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.dock;

import com.laxcus.application.manage.*;
import com.laxcus.util.classable.*;

/**
 * DOCK按纽单元
 * 
 * @author scott.liang
 * @version 1.0 7/29/2021
 * @since laxcus 1.0
 */
public class RayDockButtonItem extends WProgram { 

	/** 生成时间 **/
	private long createTime;
	
	/** 系统级应用或者否 **/
	private boolean system;

	/**
	 * 构造默认的按纽单元
	 */
	public RayDockButtonItem() {
		super();
		system = false;
		// 建立时间
		createTime = System.currentTimeMillis();
	}
	
	/**
	 * 构造按纽单元副本
	 * @param that 按纽单元
	 */
	private RayDockButtonItem(RayDockButtonItem that) {
		super(that);
		system = that.system;
		createTime = that.createTime;
	}
	
	/**
	 * 生成按纽单元实例
	 * @param w
	 */
	public RayDockButtonItem(WProgram w) {
		super(w);
		createTime = System.currentTimeMillis();
	}

	/**
	 * 生成按纽单元实例
	 * @param w
	 */
	public RayDockButtonItem(WProgram w, boolean system) {
		super(w);
		setSystem(system);
		createTime = System.currentTimeMillis();
	}
	
	/**
	 * 从可类化读取器中读取参数
	 * @param reader 可类化读取器
	 */
	public RayDockButtonItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setSystem(boolean b) {
		system = b;
	}

	public boolean isSystem() {
		return system;
	}

	public boolean isUser() {
		return !system;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.application.manage.WProgram#duplicate()
	 */
	@Override
	public RayDockButtonItem duplicate(){
		return new RayDockButtonItem(this);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeBoolean(system);
		writer.writeLong(createTime);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.application.manage.WToken#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		system = reader.readBoolean();
		createTime = reader.readLong();
	}
	
}
