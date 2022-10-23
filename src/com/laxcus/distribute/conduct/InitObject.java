/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.conduct;

import com.laxcus.distribute.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * DIFFUSE/CONVERGE分布计算过程的初始化命名对象。<br>
 * 任务启动前，初始化相关接口和数据。此接口位于CALL节点。<br>
 * 
 * @author scott.liang
 * @version 1.1 7/15/2015
 * @since laxcus 1.0
 */
public final class InitObject extends AccessObject {

	private static final long serialVersionUID = 4986137596608887107L;

	/**
	 * 根据传入参数，生成INIT阶段命名的副本
	 * @param that InitObject实例
	 */
	private InitObject(InitObject that) {
		super(that);
	}

	/**
	 * 构造一个默认的INIT命名阶段对象
	 */
	public InitObject() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析INIT命名阶段对象
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public InitObject(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造一个INIT命名阶段对象，并且指定它的阶段命名
	 * @param phase 阶段命名
	 */
	public InitObject(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 生成INIT阶段命名对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public InitObject duplicate() {
		return new InitObject(this);
	}

}