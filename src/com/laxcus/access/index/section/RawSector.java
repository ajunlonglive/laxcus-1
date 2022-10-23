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
 * 可变长二进制数组分区
 * 
 * @author scott.liang
 * @version 1.1 03/17/2015
 * @since laxcus 1.0
 */
public final class RawSector extends VariableSector {

	private static final long serialVersionUID = 2581166723632739716L;

	/**
	 * 根据传入的二进制数组分区，生成它的副本
	 * @param that
	 */
	private RawSector(RawSector that) {
		super(that);
	}

	/**
	 * 生成默认的二进制数组分区
	 */
	public RawSector() {
		super();
	}

	/**
	 * 从可类化读取器中解析字节数组分区记录
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public RawSector(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#duplicate()
	 */
	@Override
	public RawSector duplicate() {
		return new RawSector(this);
	}

	/**
	 * 根据列对象的首字节，定位它在数组集合的下标位置
	 * @param column
	 * @return
	 */
	private int seekRaw(com.laxcus.access.column.Raw column) {
		// null or empty，默认是0下标区域
		if (column.isNull() || column.isEmpty()) {
			return 0;
		}

//		// 首先选择用户自定义的码位计算器
//		VariableScaler scaler = createCodeScaler();
		
		// 对象定位器
		VariableSlider slider = getSlider();
		// 如果没有，使用系统的码位计算器
		if (slider == null) {
			slider = new RawSlider();
			slider.setPacking(getPacking());
		}

		try {
			java.lang.Long seek = (java.lang.Long) slider.seek(column);
			return seekIndex(seek.longValue());
		} catch (SliderException e) {
			Logger.error(e);
		}
		return -1;
	}

	/**
	 * @param that
	 * @return
	 */
	private int seekArray(Object that) {
		byte[] array = null;
		if (that.getClass() == java.lang.Byte.class) {
			array = new byte[] { ((java.lang.Byte) that).byteValue() };
		} else if (that.getClass() == new byte[0].getClass()) {
			array = (byte[]) that;
		} else {
			throw new ClassCastException("only java.lang.Byte or byte array");
		}

		if (array == null || array.length == 0) {
			return 0;
		}

		return seekIndex(array[0]);
	}

	/**
	 * 根据传入的对象实例，判断它在分区数组的下标位置。
	 * 传入的对象允许有3种：
	 * (1) com.laxcus.access.column.Raw
	 * (2) java.lang.Byte
	 * (3) byte数组
	 * @see com.laxcus.access.index.section.ColumnSector#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object that) {
		// 检查空集合
		super.check();
		// 空指针排在最前面
		if (that == null) {
			return 0;
		}

		if (that.getClass() == com.laxcus.access.column.Raw.class) {
			return this.seekRaw((com.laxcus.access.column.Raw) that);
		} else {
			return this.seekArray(that);
		}
	}

}