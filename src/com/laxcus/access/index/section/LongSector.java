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
 * 长整型分区
 * 
 * @author scott.liang
 * @version 1.1 03/17/2015
 * @since laxcus 1.0
 */
public final class LongSector extends Bit64Sector {

	private static final long serialVersionUID = -5526214059362026041L;

	/**
	 * 根据传入的参数，构造一个长整列分区的副本
	 * @param that
	 */
	private LongSector(LongSector that) {
		super(that);
	}

	/**
	 * 构造一个长整列分区
	 */
	public LongSector() {
		super();
	}

	/**
	 * 从可类化读取器中解析长整型分区记录
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public LongSector(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 使用码位计算器，确定一个长整形在分区中的下标位置
	 * @param column
	 * @return - 返回长整型值的下标位置
	 */
	private int seekColumn(com.laxcus.access.column.Long column) {
//		CodeScaler scaler = super.createCodeScaler();
		
		Slider slider = getSlider();
		if (slider == null) {
			slider = new LongSlider();
		}

		try {
			// 确定码值
			java.lang.Long seek = (java.lang.Long) slider.seek(column);
			// 根据码值，计算它在分区中的位置
			return seekIndex(seek.longValue());
		} catch (SliderException e) {
			Logger.error(e);
		}
		return -1;
	}

	/**
	 * 生成当前LongSector对象实例的副本
	 * @see com.laxcus.access.index.section.ColumnSector#duplicate()
	 */
	@Override
	public LongSector duplicate() {
		return new LongSector(this);
	}

	/**
	 * 根据传入的对象实例，判断它在分区数组的下标位置。
	 * 允许的对象包括: 
	 * (1) com.laxcus.access.column.Long
	 * (2) java.lang.Long
	 * (3) long数组
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

		if (that.getClass() == com.laxcus.access.column.Long.class) {
			return seekColumn((com.laxcus.access.column.Long) that);
		} else {
			return super.seekLong(that);
		}
	}

}