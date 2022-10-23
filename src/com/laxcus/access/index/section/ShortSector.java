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
 * 短整型分片分区记录器
 * 
 * @author scott.liang
 * @version 1.1 03/17/2015
 * @since laxcus 1.0
 */
public final class ShortSector extends ColumnSector {

	private static final long serialVersionUID = -7845274336842708995L;

	/** SHORT范围集合 */
	private TreeSet<ShortRange> ranges = new TreeSet<ShortRange>();

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.section.IndexSector#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		
		writer.writeInt(ranges.size());
		for (ShortRange e : ranges) {
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
			ShortRange e = new ShortRange(reader);
			ranges.add(e);
		}
	}

	/**
	 * 使用传入参数，构造一个新的短整型分区副本
	 * @param that
	 */
	private ShortSector(ShortSector that) {
		super(that);
		ranges.addAll(that.ranges);
	}

	/**
	 * 构造一个默认的短整型分区副本
	 */
	public ShortSector() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析短整型扇区参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ShortSector(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个短整型范围
	 * @param e 短整型范围
	 * @return 保存成功返回真，否则假
	 * @throws NullPointerException 如果对象是空指针时，弹出错误
	 */
	public boolean add(ShortRange e){
		Laxkit.nullabled(e);

		return ranges.add(e);
	}

	/**
	 * 删除一个短整型范围
	 * @param e 短整型范围
	 * @return 删除成功返回真，否则假
	 * @throws NullPointerException 如果对象是空指针时，弹出错误
	 */
	public boolean remove(ShortRange e) {
		Laxkit.nullabled(e);

		return ranges.remove(e);
	}

	/**
	 * 输出全部短整型范围
	 * @return ShortRange列表
	 */
	public List<ShortRange> list() {
		return new ArrayList<ShortRange>(ranges);
	}

	/**
	 * 清除全部分区记录
	 */
	public void clear() {
		ranges.clear();
	}

	/**
	 * 生成当前ShortSector实例的副本
	 * @see com.laxcus.access.index.section.ColumnSector#duplicate()
	 */
	@Override
	public ShortSector duplicate() {
		return new ShortSector(this);
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
	 * 根据值定位它在数组集合中的索引下标位置
	 * @param value
	 * @return
	 */
	private int seekIndex(short value) {
		int index = 0;
		for (ShortRange s : ranges) {
			if (value < s.begin() || s.inside(value)) {
				return index;
			}
			index++;
		}
		return index - 1;
	}

	/**
	 * 调用码位计算器，确定一个短整型在分区中的下标位置
	 * @param column 短整型类
	 * @return 返回短整值的下标位置
	 */
	private int seekColumn(com.laxcus.access.column.Short column) {
//		CodeScaler scaler = super.createCodeScaler();
		
		Slider slider = getSlider();
		if (slider == null) {
			slider = new ShortSlider();
		}

		try {
			// 确定码值
			java.lang.Short seek = (java.lang.Short) slider.seek(column);
			// 根据码值，计算它在分区中的位置
			return seekIndex(seek.shortValue());
		} catch (SliderException e) {
			Logger.error(e);
		}
		return -1;
	}

	/**
	 * 根据传入的对象，定位它在数组集合中的下标位置
	 * @param that
	 * @return
	 */
	private int seekShort(Object that) {
		short[] array = null;
		if (that.getClass() == java.lang.Short.class) {
			array = new short[] { ((java.lang.Short) that).shortValue() };
		} else if (that.getClass() == new short[0].getClass()) {
			array = (short[]) that;
		} else {
			throw new ClassCastException("only short or short array");
		}
		// 空数组
		if (array == null || array.length == 0) {
			return 0;
		}

		return seekIndex(array[0]);
	}

	/**
	 * 根据传入的对象实例，判断它在分区数组的下标位置。
	 * 允许的对象包括: 
	 * (1) com.laxcus.access.column.Short
	 * (2) java.lang.Short
	 * (3) short数组
	 * 
	 * @see com.laxcus.access.index.section.ColumnSector#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object that) {
		// 检查
		check();
		// 空对象排在前面
		if (that == null) {
			return 0;
		}

		if (that.getClass() == com.laxcus.access.column.Short.class) {
			return seekColumn((com.laxcus.access.column.Short) that);
		} else {
			return seekShort(that);
		}
	}

	/**
	 * 转换成short类型
	 * @param s
	 * @return
	 */
	public short toShort(String s) {
		short value = 0;
		int shift = 0;
		int end = s.length();
		while (end > 0) {
			int gap = (end - 2 >= 0 ? 2 : 1);
			String sub = s.substring(end - gap, end);
			short num = java.lang.Short.valueOf(sub, 16);
			value |= (num << shift);
			shift += 8;
			end -= gap;
		}
		return value;
	}

}