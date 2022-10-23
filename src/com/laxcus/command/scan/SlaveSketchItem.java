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
 * 从表数据容量检测单元。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 9/25/2015
 * @since laxcus 1.0
 */
public final class SlaveSketchItem extends ScanSketchItem {

	private static final long serialVersionUID = 3191896485385941585L;

	/**
	 * 生成从表数据容量检测单元数据副本
	 * @param that SlaveCapacityItem实例
	 */
	private SlaveSketchItem(SlaveSketchItem that) {
		super(that);
	}

	/**
	 * 构造默认的从表数据容量检测单元
	 */
	public SlaveSketchItem() {
		super(RankTag.SLAVE);
	}

	/**
	 * 从可类化数据读取器中解析从表数据容量检测单元
	 * @param reader - 可类化数据读取器
	 */
	public SlaveSketchItem(ClassReader reader) {
		super();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.access.table.CapacityItem#duplicate()
	 */
	@Override
	public SlaveSketchItem duplicate() {
		return new SlaveSketchItem(this);
	}

}
