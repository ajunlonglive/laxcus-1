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
 * 数据计算中的平均分配处理接口。<br><br><code>
 * 
 * BALANCE阶段处理内容：<br>
 * BALANCE接口位于CALL节点上，数据从DATA/WORK节点上产生后，向CALL节点返回一个数据分布图，
 * CALL节点汇总全部数据图谱，实现数据平均分割和分配后续TO阶段的资源，分发到多个WORK节点上，
 * WORK节点根据分配到的资源，到DATA/WORK节点上取数据，完成一次TO阶段计算，计算结果再次返回给CALL节点。
 * 结果有两种可能：元数据或者最终计算结果。<br> </code>
 * 
 * @author scott.liang
 * @version 1.1 7/15/2015
 * @since laxcus 1.0
 */
public final class BalanceObject extends AccessObject {

	private static final long serialVersionUID = -3877955340894668127L;

	/**
	 * 根据传入的BALANCE阶段命名参数，构造它的副本
	 * @param that BalanceObject实例
	 */
	private BalanceObject(BalanceObject that) {
		super(that);
	}

	/**
	 * 构造一个默认的BALANCE阶段命名对象
	 */
	public BalanceObject() {
		super();
	}

	/**
	 * 构造BALANCE阶段对象，并且指定它的阶段命名
	 * @param phase 阶段命名
	 */
	public BalanceObject(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 从可类化读取器中解析BALANCE对象
	 * @param reader 可类化读取器
	 */
	public BalanceObject(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成BALANCE阶段命名对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public DistributedObject duplicate() {
		return new BalanceObject(this);
	}

}