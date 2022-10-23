/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 设置账号WORK节点数目。<br>
 * 这项工作只能由管理员或者等于同管理员身份的用户来实施。<br>
 * WATCH站点不执行这项工作。<br>
 * 
 * @author scott.liang
 * @version 1.0 05/07/2017
 * @since laxcus 1.0
 */
public final class SetMaxWorkers extends SetMultiUserParameter {
	
	private static final long serialVersionUID = -5053879211318995894L;

	/** WORK节点数目 **/
	private int workers;

	/**
	 * 根据传入的设置设置账号WORK节点数目，生成它的数据副本
	 * @param that 设置设置账号WORK节点数目
	 */
	private SetMaxWorkers(SetMaxWorkers that) {
		super(that);
		workers = that.workers;
	}

	/**
	 * 构造默认和私有的设置设置账号WORK节点数目
	 */
	private SetMaxWorkers() {
		super();
	}

	/**
	 * 构造设置WORK节点数目，指定数目
	 * @param max WORK节点数目
	 */
	public SetMaxWorkers(int max) {
		this();
		setWorkers(max);
	}

	/**
	 * 构造设置设置账号WORK节点数目，指定用户签名
	 * @param siger 用户签名
	 */
	public SetMaxWorkers(Siger siger) {
		this();
		addUser(siger);
	}

	/**
	 * 从可类化数据读取器中解析设置设置账号WORK节点数目
	 * @param reader 可类化数据读取器
	 */
	public SetMaxWorkers(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置设置账号WORK节点数目
	 * @param i 设置账号WORK节点数目
	 */
	public void setWorkers(int i) {
		if (i < 1) {
			throw new IllegalValueException("illegal %d", i);
		}
		workers = i;
	}

	/**
	 * 返回设置账号WORK节点数目
	 * @return 设置账号WORK节点数目
	 */
	public int getWorkers() {
		return workers;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.access.user.SetMultiUserParameter#split()
	 */
	@Override
	public List<SetSingleUserParameter> split() {
		ArrayList<SetSingleUserParameter> array = new ArrayList<SetSingleUserParameter>();
		for (Siger siger : users) {
			SetSingleMaxWorkers e = new SetSingleMaxWorkers(siger, workers);
			array.add(e);
		}
		return array;
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMaxWorkers duplicate() {
		return new SetMaxWorkers(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeInt(workers);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.access.user.RefreshResource#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		workers = reader.readInt();
	}
}