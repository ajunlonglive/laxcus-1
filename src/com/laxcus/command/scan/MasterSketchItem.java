/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.scan;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 主表数据容量检测单元。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 9/25/2015
 * @since laxcus 1.0
 */
public final class MasterSketchItem extends ScanSketchItem {

	private static final long serialVersionUID = -6720161648929663014L;

	/**
	 * 生成主表数据容量检测单元数据副本
	 * @param that PrimeCapacityItem实例
	 */
	private MasterSketchItem(MasterSketchItem that) {
		super(that);
	}

	/**
	 * 构造默认的主表数据容量检测单元
	 */
	public MasterSketchItem() {
		super(RankTag.MASTER);
	}

	/**
	 * 从可类化数据读取器中解析主表数据容量检测单元
	 * @param reader 可类化数据读取器
	 */
	public MasterSketchItem(ClassReader reader) {
		super();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.access.table.CapacityItem#duplicate()
	 */
	@Override
	public ScanSketchItem duplicate() {
		return new MasterSketchItem(this);
	}

}
