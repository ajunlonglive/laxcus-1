/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 列存储表最大压缩倍数
 * 
 * @author scott.liang
 * @version 1.0 6/2/2019
 * @since laxcus 1.0
 */
public class SetMaxDSMReduce extends SetSingleUserParameter {

	private static final long serialVersionUID = 3072857671738805421L;

	/** 数据表名 **/
	private Space space;
	
	/** 压缩倍数 **/
	private int multiple;

	/**
	 * 根据传入的列存储表最大压缩倍数，生成它的数据副本
	 * @param that SetMaxDSMReduce实例
	 */
	private SetMaxDSMReduce(SetMaxDSMReduce that) {
		super(that);	
		space = that.space;
		multiple = that.multiple;
	}

	/**
	 * 构造默认的列存储表最大压缩倍数。
	 */
	private SetMaxDSMReduce() {
		super();
	}

	/**
	 * 构造列存储表最大压缩倍数，指定数据表名
	 * @param siger 用户签名
	 * @param space 数据表名
	 * @param multiple 压缩倍烽
	 */
	public SetMaxDSMReduce(Siger siger, Space space, int multiple) {
		this();
		setSiger(siger);
		setSpace(space);
		setMultiple(multiple);
	}

	/**
	 * 从可类化数据读取器中解析数据优化命令
	 * @param reader 可类化数据读取器
	 */
	public SetMaxDSMReduce(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名
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
	 * 设置压缩倍数，不能小于1！！！
	 * @param much 压缩倍数
	 */
	public void setMultiple(int much) {
		if (much < 1) {
			throw new IllegalValueException("illegal value: %d", much);
		}
		multiple = much;
	}

	/**
	 * 返回压缩倍数
	 * @return 压缩倍数
	 */
	public int getMultiple() {
		return multiple;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public SetMaxDSMReduce duplicate() {
		return new SetMaxDSMReduce(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		writer.writeObject(space);
		// 压缩倍数
		writer.writeInt(multiple);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 数据表名
		space = new Space(reader);
		// 压缩倍数
		multiple = reader.readInt();
	}

}
