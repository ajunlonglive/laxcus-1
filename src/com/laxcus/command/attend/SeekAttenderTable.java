/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.attend;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 查找签到器结果表 <br>
 * 保存多个查找签到器处理结果。
 * 
 * @author scott.liang
 * @version 3/20/2017
 * @since laxcus 1.0
 */
public class SeekAttenderTable extends EchoProduct {
	
	private static final long serialVersionUID = 2198578187177929777L;
	
	/** 参数集合 **/
	private Set<SeekAttenderProduct> array = new TreeSet<SeekAttenderProduct>();

	/**
	 * 构造默认的查找签到器结果表
	 */
	public SeekAttenderTable() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析查找签到器结果表
	 * @param reader 可类化数据读取器
	 */
	public SeekAttenderTable(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成传入的查找签到器结果表数据副本
	 * @param that SeekAttenderTable实例
	 */
	private SeekAttenderTable(SeekAttenderTable that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存查找签到器结果表
	 * @param e SeekAttenderProduct实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(SeekAttenderProduct e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 输出全部查找签到器结果表
	 * @return 返回SeekAttenderProduct列表
	 */
	public List<SeekAttenderProduct> list() {
		return new ArrayList<SeekAttenderProduct>(array);
	}

	/**
	 * 统计查找签到器成员数目
	 * @return 返回查找签到器成员数目
	 */
	public int size() {
		return array.size();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SeekAttenderTable duplicate() {
		return new SeekAttenderTable(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (SeekAttenderProduct e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SeekAttenderProduct e = new SeekAttenderProduct(reader);
			array.add(e);
		}
	}

}
