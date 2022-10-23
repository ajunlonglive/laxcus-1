/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.section;

import java.util.*;

import com.laxcus.access.index.slide.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * 双浮点分区 <br>
 * 
 * @author scott.liang
 * @version 1.1 03/17/2015
 * @since laxcus 1.0
 */
public final class DoubleSector extends ColumnSector {

	private static final long serialVersionUID = 7627554520386091438L;

	/** 双浮点分区范围集合 */
	private TreeSet<DoubleRange> ranges = new TreeSet<DoubleRange>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		
		writer.writeInt(ranges.size());
		for (DoubleRange e : ranges) {
			writer.writeObject(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			DoubleRange e = new DoubleRange(reader);
			ranges.add(e);
		}
	}

	/**
	 * 根据传入参数，构造一个双浮点分区副本
	 * @param that
	 */
	private DoubleSector(DoubleSector that) {
		super(that);
		ranges.addAll(that.ranges);
	}

	/**
	 * 构造一个默认的双浮点分区
	 */
	public DoubleSector() {
		super();
	}

	/**
	 * 从可类化读取器中解析双浮点分区记录
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public DoubleSector(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个双浮点范围
	 * @param e 双浮点范围
	 * @return 保存成功返回真，否则假
	 * @throws NullPointerException 如果对象是空指针时，弹出错误
	 */
	public boolean add(DoubleRange e){
		Laxkit.nullabled(e);
		
		return ranges.add(e);
	}

	/**
	 * 删除一个双浮点范围
	 * @param e 双浮点范围
	 * @return 删除成功返回真，否则假
	 * @throws NullPointerException 如果对象是空指针时，弹出错误
	 */
	public boolean remove(DoubleRange e) {
		Laxkit.nullabled(e);

		return ranges.remove(e);
	}

	/**
	 * 输出全部双浮点范围
	 * @return DoubleRange列表
	 */
	public List<DoubleRange> list() {
		return new ArrayList<DoubleRange>(ranges);
	}

	/**
	 * 清除全部对象
	 */
	public void clear() {
		this.ranges.clear();
	}

	/**
	 *生成一个当前DoubleSector实例副本
	 * @see com.laxcus.access.index.section.ColumnSector#duplicate()
	 */
	@Override
	public DoubleSector duplicate() {
		return new DoubleSector(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#size()
	 */
	@Override
	public int size() {
		return this.ranges.size();
	}

	/**
	 * 根据传入值，定位它在索引分区中的下标位置
	 * @param value
	 * @return
	 */
	private int seekIndex(double value) {
		int index = 0;
		for (DoubleRange s : ranges) {
			if (value < s.begin() || s.inside(value)) {
				return index;
			}
			index++;
		}
		return index - 1;
	}

	/**
	 * 使用码位计算器，确定一个双浮点在分区中的下标位置
	 * @param column 双浮点列
	 * @return 返回双浮点值的下标位置
	 */
	private int seekColumn(com.laxcus.access.column.Double column) {
//		CodeScaler scaler = super.createCodeScaler();
		
		Slider slider = getSlider();
		if (slider == null) {
			slider = new DoubleSlider();
		}

		try {
			// 确定码值
			java.lang.Double seek = (java.lang.Double) slider.seek(column);
			// 根据码值，计算它在分区中的位置
			return seekIndex(seek.doubleValue());
		} catch (SliderException e) {
			Logger.error(e);
		}
		return -1;
	}

	/**
	 * 根据传入的Double对象，判断它在分区数组的下标位置。
	 * @param that
	 * @return
	 */
	private int seekDouble(Object that) {
		double[] array = null;
		if (that.getClass() == java.lang.Double.class) {
			array = new double[] { ((java.lang.Double) that).doubleValue() };
		} else if (that.getClass() == new double[0].getClass()) {
			array = (double[]) that;
		} else {
			throw new ClassCastException("only double or double array");
		}
		// 空数组
		if (array == null || array.length == 0) {
			return 0;
		}

		return this.seekIndex(array[0]);
	}

	/**
	 * 根据传入的对象实例，判断它在索引数组中的下标位置。<br>
	 * 允许的对象包括:  <br>
	 * (1) com.laxcus.access.column.Double <br>
	 * (2) java.lang.Double <br>
	 * (3) double数组 <br>
	 * 
	 * @see com.laxcus.access.index.section.ColumnSector#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object that) {
		// 检查
		this.check();
		// 空对象排在前面
		if (that == null) {
			return 0;
		}

		if (that.getClass() == com.laxcus.access.column.Double.class) {
			return seekColumn((com.laxcus.access.column.Double) that);
		} else {
			return this.seekDouble(that);
		}
	}

}