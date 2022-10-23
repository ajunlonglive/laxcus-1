/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.scan;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 扫描数据块命令。<br><br>
 * 
 * 命令格式: SCAN ENTITY 数据库名.表名 [TO ALL | DATA SITE, ...] 。<br><br>
 * 
 * FRONT节点处理流程：FRONT -> CALL -> DATA。<br>
 * WATCH节点处理流程：WATCH -> TOP/HOME -> DATA。<br><br>
 * 
 * 弱一致检查，允许扫描过程中，某些节点存在故障。<br>
 * 返回参数：站点地址、数据表名、数据块数目、数据容量。
 * 
 * @author scott.liang
 * @version 1.2 12/3/2013
 * @since laxcus 1.0
 */
public final class ScanEntity extends ScanReference {

	private static final long serialVersionUID = 2416300264730620850L;

	/** 数据表名 **/
	private Space space;
	
	/**
	 * 构造扫描数据块命令
	 */
	public ScanEntity() {
		super();
	}

	/**
	 * 根据传入的扫描数据块命令，生成它的数据副本
	 * @param that CountEntitySize实例
	 */
	private ScanEntity(ScanEntity that) {
		super(that);
		space = that.space;
	}

	/**
	 * 构造扫描数据块命令，指定数据表名
	 * @param space 数据表名
	 */
	public ScanEntity(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 设置数据表名，不允许空指针
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);
		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 从可类化读取器中解析扫描数据块命令参数
	 * @param reader 可类化读取器
	 * @since laxcus 1.2
	 */
	public ScanEntity(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public ScanEntity duplicate() {
		return new ScanEntity(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.scan.ScanReference#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(space);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.command.scan.ScanReference#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		space = new Space(reader);
	}
}