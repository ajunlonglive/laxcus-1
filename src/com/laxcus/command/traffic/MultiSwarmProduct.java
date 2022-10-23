/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.traffic;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 多点流量测试报告
 * 
 * @author scott.liang
 * @version 1.0 6/15/2022
 * @since laxcus 1.0
 */
public class MultiSwarmProduct extends EchoProduct {
	
	private static final long serialVersionUID = 6766761410401746877L;

	/** 错误 **/
	private int faults;
	
	/** 输出结果 **/
	private ArrayList<TrafficProduct> array = new ArrayList<TrafficProduct>();

	/**
	 * 构造多点流量测试报告
	 */
	public MultiSwarmProduct() {
		super();
	}
	
	/**
	 * 生成多点流量测试报告副本
	 * @param reader
	 */
	public MultiSwarmProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造多点流量测试报告副本
	 * @param that
	 */
	private MultiSwarmProduct(MultiSwarmProduct that) {
		super(that);
		faults = that.faults;
		array.addAll(that.array);
	}

	/**
	 * 构造多点流量测试报告
	 * @param faults
	 * @param a
	 */
	public MultiSwarmProduct(int faults, List<TrafficProduct> a) {
		this();
		setFaults(faults);
		addAll(a);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public MultiSwarmProduct duplicate() {
		return new MultiSwarmProduct(this);
	}

	/**
	 * 设置错误结果
	 * @param n
	 */
	public void setFaults(int n) {
		faults = n;
	}

	/**
	 * 返回错误统计
	 * @return
	 */
	public int getFaults() {
		return faults;
	}

	/**
	 * 增加
	 * @param e
	 */
	public void add(TrafficProduct e) {
		array.add(e);
	}

	/**
	 * 增加
	 * @param a
	 */
	public void addAll(Collection<TrafficProduct> a) {
		array.addAll(a);
	}
	
	/**
	 * 输出
	 * @return
	 */
	public List<TrafficProduct> list() {
		return new ArrayList<TrafficProduct>(array);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(faults);
		writer.writeInt(array.size());
		for (TrafficProduct e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		faults = reader.readInt();
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			TrafficProduct e = new TrafficProduct(reader);
			array.add(e);
		}
	}

}