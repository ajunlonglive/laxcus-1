/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.slide;

import java.io.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;
import com.laxcus.access.type.*;
import com.laxcus.access.util.*;

/**
 * 字节数组列对象定位器。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public class RawSlider extends VariableSlider {

	/**
	 * 构造默认的字节数组列对象定位器
	 */
	public RawSlider() {
		super(ColumnType.RAW);
	}

	/**
	 * 构造字节数组列对象定位器，指定数据封装
	 * @param packing 数据封装
	 */
	public RawSlider(Packing packing) {
		this();
		setPacking(packing);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.scale.CodeScaler#isSupport(java.lang.Object)
	 */
	@Override
	public boolean isSupport(Object e) {
		return (e.getClass() == com.laxcus.access.column.Raw.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.scale.ColumnScaler#seek(com.laxcus.access.column.Column)
	 */
	@Override
	public java.lang.Number seek(Object e) throws SliderException {
		// 不支持弹出异常
		if (!this.isSupport(e)) {
			throw new SliderException("cannot be support %s", e.getClass().getName());
		}

		Raw raw = (Raw) e;
		if (raw.isNull() || raw.isEmpty()) {
			return new java.lang.Long(0L);
		}		
		// 返回首选项。有索引就返回索引值，否则返回数据值
		byte[] array = raw.getPreferred();
		// 如果数据被打包（加密、压缩），先执行反操作还原数据
		Packing packing = this.getPacking();
		if (packing != null && packing.isEnabled()) {
			try {
				array = VariableGenerator.depacking(packing, array, 0, array.length);
			} catch (IOException ex) {
				throw new SliderException(ex);
			}
		}

		// 以首字符做为区分
		return new java.lang.Long(array[0]);
	}

}
