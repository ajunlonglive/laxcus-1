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
import com.laxcus.util.*;
import com.laxcus.util.each.*;

/**
 * 可变长列对象定位器。<br><br>
 * 
 * 所有可变长列对象定位器的码位都是一个64位的有符号长整型值。这个值描述一列数据在所在集合的位置。<br>
 * 
 * 默认的列码位是根据它的字节数组，使用Sign.sing方法，生成一个64位的散列值。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public abstract class VariableSlider extends ColumnSlider {

	/** 数据封装 **/
	private Packing packing;

	/**
	 * 构造可变长列对象定位器，指定列数据类型
	 * @param family 列数据类型
	 */
	protected VariableSlider(byte family) {
		super(family);
		if (!ColumnType.isVariable(family)) {
			throw new IllegalValueException("illegal variable: %d", family);
		}
	}

	/**
	 * 设置数据封装
	 * @param e 数据封装实例
	 */
	public void setPacking(Packing e) {
		packing = e;
	}

	/**
	 * 返回数据封装
	 * @return 数据封装实例
	 */
	public Packing getPacking() {
		return packing;
	}

	/**
	 * 根据指定可变长列，生成一个度量值
	 * @param variable 可变长列
	 * @return 返回Number子类实例
	 * @throws SliderException
	 */
	protected java.lang.Number doDefaultSeek(Variable variable) throws SliderException {
		// 不允许空指针
		Laxkit.nullabled(variable);
		// 判断是空
		if (variable.isNull() || variable.isEmpty()) {
			return new java.lang.Long(0L);
		}

		// 返回首选项。有索引就返回索引值，否则返回数据值
		byte[] array = variable.getPreferred();
		// 如果数据被打包（加密、压缩），先执行反操作还原数据
		if (packing != null && packing.isEnabled()) {
			try {
				array = VariableGenerator.depacking(packing, array, 0, array.length);
			} catch (IOException e) {
				throw new SliderException(e);
			}
		}

		// 生成散列码
		long point = EachTrustor.sign(array);
		return new java.lang.Long(point);
	}

}