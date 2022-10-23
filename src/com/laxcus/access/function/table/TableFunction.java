/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.function.table;

import com.laxcus.access.function.*;
import com.laxcus.access.schema.*;
import com.laxcus.util.classable.*;

/**
 * 表函数 <br>
 * 所有基于表的函数
 * 
 * @author scott.liang
 * @version 1.1 4/21/2009
 * @since laxcus 1.0
 */
public abstract class TableFunction extends Function { 

	private static final long serialVersionUID = 3971939758002788272L;

	/** 数据表名 **/
	private Space space;

	/**
	 * 将子类私有的参数写入可类化存储器
	 * @param writer 可类化存储器
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInstance(space);
	}

	/**
	 * 从可类化读取器中解析子类私有的参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		space = reader.readInstance(Space.class);
	}

	/**
	 * 构造一个表函数
	 */
	protected TableFunction() {
		super();
		// 默认不支持生成默认列
		setSupportDefault(false);
	}

	/**
	 * 生成表函数的数据副本
	 * @param that TableFunction实例
	 */
	protected TableFunction(TableFunction that) {
		super(that);
		setSpace(that.space);
	}

	/**
	 * 设置函数所属表
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
		space = e;
	}

	/**
	 * 返回函数所属表
	 * @return 数据表名
	 */
	public Space getSpace() {
		return space;
	}

}