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
 * 单浮点分区<br>
 * 
 * @author scott.liang
 * @version 1.1 03/17/2015
 * @since laxcus 1.0
 */
public final class FloatSector extends ColumnSector {

	private static final long serialVersionUID = 4476431475199363953L;

	/** 单浮点分区集合 */
	private TreeSet<FloatRange> ranges = new TreeSet<FloatRange>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		
		writer.writeInt(ranges.size());
		for (FloatRange e : ranges) {
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
			FloatRange e = new FloatRange(reader);
			ranges.add(e);
		}
	}

	/**
	 * 根据传入参数，构造一个单浮点分区副本
	 * @param that
	 */
	private FloatSector(FloatSector that) {
		super(that);
		ranges.addAll(that.ranges);
	}

	/**
	 * 构造一个单浮点分区
	 */
	public FloatSector() {
		super();
	}

	/**
	 * 从可类化读取器中解析浮点分区记录
	 * @param reader
	 * @since 1.1
	 */
	public FloatSector(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个单浮点范围
	 * @param e 单浮点范围
	 * @return 保存成功返回真，否则假
	 * @throws NullPointerException 如果对象是空指针时，弹出错误
	 */
	public boolean add(FloatRange e){
		Laxkit.nullabled(e);

		return ranges.add(e);
	}

	/**
	 * 删除一个单浮点范围
	 * @param e 单浮点范围
	 * @return 删除成功返回真，否则假
	 * @throws NullPointerException 如果对象是空指针时，弹出错误
	 */
	public boolean remove(FloatRange e) {
		Laxkit.nullabled(e);

		return ranges.remove(e);
	}

	/**
	 * 输出全部单浮点范围
	 * @return FloatRange列表
	 */
	public List<FloatRange> list() {
		return new ArrayList<FloatRange>(ranges);
	}

	/**
	 * 清除全部
	 */
	public void clear() {
		ranges.clear();
	}

	/**
	 * 生成当前FloatSector对象实例的副本
	 * @see com.laxcus.access.index.section.ColumnSector#duplicate()
	 */
	@Override
	public FloatSector duplicate() {
		return new FloatSector(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#size()
	 */
	@Override
	public int size() {
		return ranges.size();
	}

	/**
	 * 根据传入的参数，定义它在索引分区数组中的下标位置
	 * @param value
	 * @return
	 */
	private int seekIndex(float value) {
		int index = 0;
		for (FloatRange s : ranges) {
			if (value < s.begin() || s.inside(value)) {
				return index;
			}
			index++;
		}
		return index - 1;
	}

	/**
	 * 使用码位计算器，确定一个单浮点在分区中的下标位置
	 * @param column 单浮点列
	 * @return 返回单浮点值的下标位置
	 */
	private int seekColumn(com.laxcus.access.column.Float column) {
//		CodeScaler scaler = super.createCodeScaler();
		
		Slider slider = getSlider();
		if (slider == null) {
			slider = new FloatSlider();
		}

		try {
			// 确定码值
			java.lang.Float seek = (java.lang.Float) slider.seek(column);
			// 根据码值，计算它在分区中的位置
			return seekIndex(seek.floatValue());
		} catch (SliderException e) {
			Logger.error(e);
		}
		return -1;
	}

	/**
	 * 根据传入的对象，定位它在数组集合的下标位置
	 * @param that
	 * @return
	 */
	private int seekFloat(Object that) {
		float[] array = null;
		if (that.getClass() == java.lang.Float.class) {
			array = new float[] { ((java.lang.Float) that).floatValue() };
		} else if (that.getClass() == new float[0].getClass()) {
			array = (float[]) that;
		} else {
			throw new ClassCastException("only float or float array");
		}

		// 空数组
		if (array == null || array.length == 0) {
			return 0;
		}

		return seekIndex(array[0]);
	}

	/**
	 * 根据传入的对象实例，判断它在分区数组的下标位置。<br>
	 * 允许的对象包括: <br>
	 * (1) com.laxcus.access.column.Float <br>
	 * (2) java.lang.Float <br>
	 * (3) float数组 <br>
	 * @see com.laxcus.access.index.section.ColumnSector#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object that) {
		check();
		// 空对象排在前面
		if (that == null) {
			return 0;
		}

		if (that.getClass() == com.laxcus.access.column.Float.class) {
			return seekColumn((com.laxcus.access.column.Float) that);
		} else {
			return seekFloat(that);
		}
	}

}