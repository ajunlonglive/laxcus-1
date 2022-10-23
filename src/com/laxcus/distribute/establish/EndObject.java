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
 * 数据构建的“END”阶段对象。<br>
 * 
 * END阶段们于FRONT站点，主要是保存和显示最后的处理结果。
 * 
 * @author scott.liang
 * @version 1.3 4/2/2015
 * @since laxcus 1.0
 */
public class EndObject extends AccessObject {

	private static final long serialVersionUID = -3221241811825742948L;

	/** 数据写入磁盘的文件名 */
	private String writeTo;

	/**
	 * 根据传入的“END”阶段对象实例，生成它的数据副本
	 * @param that EndObject实例
	 */
	private EndObject(EndObject that) {
		super(that);
	}

	/**
	 * 构造一个默认的“END”阶段对象
	 */
	public EndObject() {
		super();
	}

	/**
	 * 构造“END”阶段对象，指定它的阶段命名
	 * @param phase 阶段命名
	 */
	public EndObject(Phase phase) {
		this();
		super.setPhase(phase);
	}

	/**
	 * 使用可类化读取器解析“END”对象参数
	 * @param reader 可类化读取器
	 */
	public EndObject(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置本地文件名
	 * @param filename 本地文件名
	 */
	public void setWriteTo(String filename) {
		if (filename == null) {
			writeTo = null;
		} else {
			writeTo = new String(filename);
		}
	}

	/**
	 * 返回本地文件名
	 * @return 本地文件名
	 */
	public String getWriteTo() {
		return writeTo;
	}

	/**
	 * 将当前对象信息写入可类化存储器
	 * @see com.laxcus.distribute.AccessObject#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeString(writeTo);
	}

	/**
	 * 从可类化读取器中解析当前对象信息
	 * @see com.laxcus.distribute.AccessObject#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		writeTo = reader.readString();
	}

	/**
	 * 根据当前实例，生成“END”阶段对象的数据副本
	 * @see com.laxcus.distribute.DistributedObject#duplicate()
	 */
	@Override
	public EndObject duplicate() {
		return new EndObject(this);
	}

}