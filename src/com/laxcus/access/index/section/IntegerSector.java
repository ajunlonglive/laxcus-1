/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.section;

import com.laxcus.access.index.slide.*;
import com.laxcus.log.client.*;
import com.laxcus.util.classable.*;

/**
 * 整型数据分区
 * 
 * @author scott.liang
 * @version 1.1 03/17/2015
 * @since laxcus 1.0
 */
public final class IntegerSector extends Bit32Sector {

	private static final long serialVersionUID = 422232265779433550L;

	/**
	 * 根据传入参数，构造一个整型列分区
	 * @param that
	 */
	private IntegerSector(IntegerSector that) {
		super(that);
	}

	/**
	 * 构造默认的整型列分区
	 */
	public IntegerSector() {
		super();
	}

	/**
	 * 从可类化读取器中解析整型分区记录
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public IntegerSector(ClassReader reader) {
		this();
		resolve(reader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#duplicate()
	 */
	@Override
	public IntegerSector duplicate() {
		return new IntegerSector(this);
	}

	/**
	 * 使用码位计算器，确定一个整形在分区中的下标位置
	 * @param column
	 * @return - 返回整形值的下标位置
	 */
	private int seekColumn(com.laxcus.access.column.Integer column) {
//		CodeScaler scaler = super.createCodeScaler();
		
		Slider slider = getSlider();
		if (slider == null) {
			slider = new IntegerSlider();
		}

		try {
			// 确定码值
			java.lang.Integer seek = (java.lang.Integer) slider.seek(column);
			// 根据码值，计算它在分区中的位置
			return seekIndex(seek.intValue());
		} catch (SliderException e) {
			Logger.error(e);
		}
		return -1;
	}

	/**
	 * 根据传入的对象实例，判断它在索引数组中的下标位置。<br>
	 * 允许的对象包括:  <br>
	 * (1) com.laxcus.access.column.Integer <br>
	 * (2) java.lang.Integer <br>
	 * (3) int数组 <br>
	 * 
	 * @see com.laxcus.access.index.section.ColumnSector#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object that) {
		// 分区检查
		this.check();
		// 空对象排在前面
		if (that == null) {
			return 0;
		}

		if (that.getClass() == com.laxcus.access.column.Integer.class) {
			return seekColumn((com.laxcus.access.column.Integer) that);
		} else {
			return super.seekInteger(that);
		}
	}

}