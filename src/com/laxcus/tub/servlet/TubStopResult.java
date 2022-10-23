/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 边缘容器停止结果
 * 
 * @author scott.liang
 * @version 1.0 6/20/2019
 * @since laxcus 1.0
 */
public class TubStopResult extends TubProcessResult {

	private static final long serialVersionUID = 1337698684884874647L;

	/**
	 * 构造默认的边缘容器停止结果
	 */
	public TubStopResult() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析边缘容器停止结果
	 * @param reader 可类化读取器
	 */
	public TubStopResult(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 生成边缘容器停止结果副本
	 * @param that 边缘容器停止结果
	 */
	private TubStopResult(TubStopResult that) {
		this();
	}

	/**
	 * 构造边缘容器停止结果，指定状态码
	 * @param status 状态码
	 */
	public TubStopResult(int status) {
		this();
		setStatus(status);
	}
	
	/**
	 * 构造边缘容器启动结果
	 * @param status
	 * @param naming
	 * @param processId
	 */
	public TubStopResult(int status, Naming naming, long processId) {
		this(status);
		setNaming(naming);
		setProcessId(processId);
	}


	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.TubProcessResult#duplicate()
	 */
	@Override
	public TubStopResult duplicate() {
		return new TubStopResult(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.TubProcessResult#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.TubProcessResult#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
	
	}
}
