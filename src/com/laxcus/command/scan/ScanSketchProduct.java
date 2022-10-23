/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.scan;

import com.laxcus.access.schema.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 表数据容量检测结果。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 9/25/2015
 * @since laxcus 1.0
 */
public final class ScanSketchProduct extends EchoProduct {

	private static final long serialVersionUID = 924927956433369242L;

	/** 数据表名 **/
	private Space space;

	/** 主表单元 **/
	private MasterSketchItem prime; 

	/** 从表单元 **/
	private SlaveSketchItem slave;

	/**
	 * 构造默认和私有的表数据容量检测结果
	 */
	private ScanSketchProduct() {
		super();
	}

	/**
	 * 通过传入的表数据容量检测结果，生成它的数据副本
	 * @param that ScanSketchProduct实例
	 */
	private ScanSketchProduct(ScanSketchProduct that) {
		super(that);
		space = that.space;
		prime = that.prime;
		slave = that.slave;
	}

	/**
	 * 构造表数据容量检测结果，指定数据表名
	 * @param space 数据表名
	 */
	public ScanSketchProduct(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析表数据容量检测结果
	 * @param reader 可类化数据读取器
	 */
	public ScanSketchProduct(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 累加一个表数据容量检测结果
	 * @param that ScanSketchProduct实例
	 */
	public void add(ScanSketchProduct that) {
		if (Laxkit.compareTo(space, that.space) != 0) {
			throw new IllegalValueException("cannot be match:%s - %s", space, that.space);
		}
		// 主表数据
		this.addCapacityItem(that.prime);
		// 从表数据
		this.addCapacityItem(that.slave);
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 * @throws NullPointerException
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
	 * 累加表检测单元数据
	 * @param that CapacityItem实例
	 */
	public void addCapacityItem(ScanSketchItem that) {
		// 忽略
		if(that == null) {
			return;
		}
		if (that.isMaster()) {
			if (prime == null) {
				prime = (MasterSketchItem) that.duplicate();
			} else {
				prime.add(that);
			}
		} else {
			if (slave == null) {
				slave = (SlaveSketchItem) that.duplicate();
			} else {
				slave.add(that);
			}
		}
	}

	/**
	 * 返回主表检测单元数据
	 * @return PrimeCapacityItem实例
	 */
	public MasterSketchItem getPrimeCapacityItem() {
		return prime;
	}

	/**
	 * 返回从表检测单元数据
	 * @return SlaveCapacityItem实例
	 */
	public SlaveSketchItem getSlaveCapacityItem() {
		return slave;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ScanSketchProduct duplicate() {
		return new ScanSketchProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
		// 主表和从表
		writer.writeInstance(prime);
		writer.writeInstance(slave);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
		// 主表检测容量和从表检测容量
		prime = reader.readInstance(MasterSketchItem.class);
		slave = reader.readInstance(SlaveSketchItem.class);
	}

}
