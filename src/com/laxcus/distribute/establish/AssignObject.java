/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish;

import com.laxcus.distribute.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 数据构建的“ASSIGN”阶段对象。<br>
 * 
 * @author scott.liang
 * @version 1.3 4/2/2015
 * @since laxcus 1.0
 */
public class AssignObject extends AccessObject {

	private static final long serialVersionUID = -8658090835713623163L;

	/**
	 * 根据传入的“ASSIGN”阶段对象实例，生成它的数据副本
	 * @param that AssignObject实例
	 */
	private AssignObject(AssignObject that) {
		super(that);
	}

	/**
	 * 构造一个默认的“ASSIGN”阶段对象
	 */
	public AssignObject() {
		super();
	}

	/**
	 * 构造一个的“ASSIGN”阶段对象，指定它的阶段命名。
	 * @param phase 阶段命名。
	 */
	public AssignObject(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 使用可类化读取器解析“ASSIGN”对象参数
	 * @param reader 可类化读取器
	 */
	public AssignObject(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 根据当前实例，生成“ASSIGN”阶段对象的数据副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public AssignObject duplicate() {
		return new AssignObject(this);
	}

}