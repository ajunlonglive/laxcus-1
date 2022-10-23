/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.contact;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * CONDUCT.DISTANT阶段参数输入接口。<br>
 * 
 * 参数来源自“图形终端/字符控制台/驱动程序”三种界面的用户输入。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/9/2020
 * @since laxcus 1.0
 */
public final class DistantInputter extends ContactInputter {

	private static final long serialVersionUID = -889135597474982560L;

	/** DISTANT阶段工作模式，由外部方法在初始化时定义 **/
	private int mode;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.ConductInputter#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 生成前缀
		super.buildSuffix(writer);
		// 写入节点数目
		writer.writeInt(mode);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.conduct.ConductInputter#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀
		super.resolveSuffix(reader);
		// 节点数量
		mode = reader.readInt();
	}
	
	/**
	 * 根据输入参数，生成它的副本
	 * @param that DistantInputter实例
	 */
	private DistantInputter(DistantInputter that) {
		super(that);
		mode = that.mode;
	}

	/**
	 * 构造一个默认的CONDUCT.DISTANT阶段参数输入器
	 */
	public DistantInputter() {
		super();
	}

	/**
	 * 构造DISTANT阶段对象输入器，同时指定的DISTANT阶段工作模式
	 * @param mode DISTANT阶段工作模式（GENERATE/EVALUATE）
	 */
	public DistantInputter(int mode) {
		this();
		setMode(mode);
	}

	/**
	 * 构造一个CONDUCT.DISTANT阶段参数输入器，指定它的阶段命名
	 * @param phase 阶段命名
	 */
	public DistantInputter(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 构造DISTANT阶段命名对象，同指定的DISTANT阶段工作模式和阶段命名
	 * @param mode DISTANT阶段工作模式
	 * @param phase 阶段命名
	 */
	public DistantInputter(int mode, Phase phase) {
		this(mode);
		setPhase(phase);
	}

	/**
	 * 从可类化数据读取器中解析CONDUCT.DISTANT阶段参数输入器
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public DistantInputter(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置DISTANT阶段对象的工作模式（产生数据/计算数据的任意一种）
	 * @param who 工作模式
	 */
	public void setMode(int who) {
		if(!DistantMode.isMode(who)) {
			throw new IllegalValueException("illegal mode:%d", who);
		}
		mode = who;
	}

	/**
	 * 返回DISTANT阶段对象的工作模式（产生数据/计算数据中的任意一种）
	 * @return 工作模式
	 */
	public int getMode() {
		return mode;
	}
	
	/**
	 * 根据当前实例，生成DISTANT阶段参数输入器的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public DistantInputter duplicate() {
		return new DistantInputter(this);
	}

}