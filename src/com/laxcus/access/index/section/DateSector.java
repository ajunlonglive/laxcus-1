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
 * 日期分区 <br>
 * 
 * @author scott.liang
 * @version 1.1 03/17/2015
 * @since laxcus 1.0
 */
public final class DateSector extends Bit32Sector {

	private static final long serialVersionUID = 5657454076843239411L;

	/**
	 * 根据传入参数，构造一个日期分区副本
	 * @param that
	 */
	private DateSector(DateSector that) {
		super(that);
	}
	
	/**
	 * 构造一个默认的日期分区
	 */
	public DateSector() {
		super();
	}
	
	/**
	 * 从可类化读取器中解析日期分区记录
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public DateSector(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 使用码位计算器，确定一个整形在分区中的下标位置
	 * @param column - 整形列
	 * @return - 返回整形值的下标位置
	 */
	private int seekColumn(com.laxcus.access.column.Date column) {
//		CodeScaler scaler = super.createCodeScaler();
		
		Slider slider = getSlider();
		if (slider == null) {
			slider = new DateSlider();
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

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#duplicate()
	 */
	@Override
	public DateSector duplicate() {
		return new DateSector(this);
	}
	

	/**
	 * 根据传入的对象实例，判断它在索引数组中的下标位置。<br>
	 * 允许的对象包括:  <br>
	 * (1) com.laxcus.access.column.Date <br>
	 * (2) java.lang.Integer <br>
	 * (3) int数组 <br>
	 * 
	 * @see com.laxcus.access.index.section.ColumnSector#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object that) {
		// 分区数组检查
		check();
		// 空对象排在前面
		if (that == null) {
			return 0;
		}

		if (that.getClass() == com.laxcus.access.column.Date.class) {
			return seekColumn((com.laxcus.access.column.Date) that);
		} else {
			return super.seekInteger(that);
		}
	}

}