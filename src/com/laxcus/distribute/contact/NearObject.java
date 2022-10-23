/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.contact;

import com.laxcus.distribute.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;

/**
 * 收集数据，完成最终计算任务。<br>
 * 此对象实例位于最终显示或者存储阶段，即FRONT节点上<br>
 * 
 * @author scott.liang
 * @version 1.0 5/5/2020
 * @since laxcus 1.0
 */
public final class NearObject extends AccessObject {

	private static final long serialVersionUID = 3066949713467553843L;

	/** 数据写入磁盘的文件名 */
	private String writeTo;

	/**
	 * 根据传入的NEAR阶段命名对象参数，生成它的副本
	 * @param that NearObject实例
	 */
	private NearObject(NearObject that) {
		super(that);
		setWriteTo(that.writeTo);
	}

	/**
	 * 构造一个默认的NEAR阶段命名对象
	 */
	public NearObject() {
		super();
	}

	/**
	 * 构造一个NEAR阶段命名对象，并且指定它的阶段命名
	 * @param phase 阶段命名
	 */
	public NearObject(Phase phase) {
		this();
		setPhase(phase);
	}

	/**
	 * 从可类化读取器解析NEAR参数
	 * @param reader 可类化读取器
	 */
	public NearObject(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置本地文件名
	 * 
	 * @param filename 本地文件名
	 */
	public void setWriteTo(String filename) {
		if (filename == null) {
			this.writeTo = null;
		} else {
			this.writeTo = new String(filename);
		}
	}

	/**
	 * 返回本地文件名
	 * 
	 * @return 本地文件名
	 */
	public String getWriteTo() {
		return writeTo;
	}

	/**
	 * 生成NEAR命名阶段对象的副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public NearObject duplicate() {
		return new NearObject(this);
	}

	/**
	 * 将当前NEAR对象信息写入可类化存储器
	 * @see com.laxcus.distribute.AccessObject#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 前缀
		super.buildSuffix(writer);
		// 存储目录
		writer.writeString(writeTo);
	}

	/**
	 * 从可类化读取器中解析NEAR对象信息
	 * @see com.laxcus.distribute.AccessObject#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 解析前缀
		super.resolveSuffix(reader);
		// 存储目录
		writeTo = reader.readString();
	}
}