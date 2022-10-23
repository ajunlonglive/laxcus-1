/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.balance;

import java.math.*;
import java.util.*;

import com.laxcus.access.index.section.*;
import com.laxcus.access.index.slide.*;
import com.laxcus.access.index.zone.*;
import com.laxcus.util.range.*;

/**
 * 单浮点平衡分割器 <br>
 * 
 * @author scott.liang
 * @version 1.0 8/21/2009
 * @since laxcus 1.0
 */
public final class FloatBalancer extends ColumnBalancer {

	/** 一组可以衔接，但是不能重叠的整型值集合  **/
	private List<FloatZone> array = new ArrayList<FloatZone>();

	/**
	 * 构造单浮点平衡分割器
	 */
	public FloatBalancer() {
		super();
	}

	/**
	 * 返回单浮点分布区域列表
	 * @return FloatZone列表
	 */
	public List<FloatZone> list() {
		return new ArrayList<FloatZone>(array);
	}

	/**
	 * 判断单浮点平衡分割器的成员数目
	 * @return 返回成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断当前分割器是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() ==0;
	}

	/**
	 * 分割区域
	 * @param range
	 * @param blocks
	 * @return
	 */
	private FloatRange[] split(FloatRange range, int blocks) {
		BigDecimal min = new BigDecimal(String.valueOf(range.begin()));
		BigDecimal max = new BigDecimal(String.valueOf(range.end()));
		BigDecimal count = new BigDecimal(blocks);

		BigDecimal size = max.subtract(min).add(BigDecimal.ONE);
		BigDecimal gap = size.divide(count);

		List<FloatRange> ranges = new ArrayList<FloatRange>(blocks);
		BigDecimal seek = min;
		for (int i = 0; i < blocks; i++) {
			BigDecimal last = seek.add(gap);
			if (last.compareTo(max) > 0 || i + 1 == blocks)
				last = max;

			FloatRange rg = new FloatRange(seek.floatValue(), last.floatValue());
			ranges.add(rg);

			if (last.compareTo(max) >= 0) break; // last >= max
			seek = last;
		}

		FloatRange[] s = new FloatRange[ranges.size()];
		return ranges.toArray(s);
	}

	/**
	 * 扩大分布范围数量：逐次找到统计频率最大分布，分成两部分，再保存!
	 * @param sites
	 */
	private void extend(int sites) {
		while(array.size() < sites) {
			Collections.sort(array);
			// 找到频率值最大的分片，分割成两部分
			int index = -1, count = -1;
			for(int i = 0; i < array.size(); i++) {
				FloatZone zone = array.get(i);
				if(zone.getWeight() > count) {
					index = i;
					count = zone.getWeight();
				}
			}
			FloatZone zone = array.get(index); 
			FloatRange[] ranges = split(zone.getRange(), 2);
			if (ranges.length < 2) break; //最小范围，不再分割，退出
			int middle = zone.getWeight() / ranges.length;

			// 删除旧的
			array.remove(index);
			// 保存分隔新值
			for (int i = 0; i < ranges.length; i++) {
				int weight = middle;
				if (i + 1 == ranges.length) weight = zone.getWeight() - middle;
				this.add(new FloatZone(ranges[i], weight));
			}
		}
		// 再次排序
		Collections.sort(array);
	}

	/**
	 * 缩小分布范围数目：找到衔接的两个分片，合为一体
	 * @param sites
	 */
	private void shrink(int sites) {
		while (array.size() > sites) {
			// 必须大于1才可以比较
			if (array.size() < 2) break;
			java.util.Collections.sort(array);
			// 找到统计值最小的
			int index = 0, count = 0;
			for (int i = 0; i < array.size(); i++) {
				FloatZone volumn = array.get(i);
				if (i == 0 || volumn.getWeight() < count) {
					index = i;
					count = volumn.getWeight();
				}
			}

			if (index == 0) {
				// 与它之后的合并
				FloatZone v1 = array.get(0);
				FloatZone v2 = array.get(1);

				FloatZone zone = new FloatZone(v1.getRange().begin(), v2.getRange().end(), v1.getWeight());
				zone.addWeight(v2.getWeight());

				array.remove(v1);
				array.remove(v2);
				array.add(zone);
			} else if (index + 1 == array.size()) {
				// 与它前面的合并
				FloatZone v1 = array.get(index - 1);
				FloatZone v2 = array.get(index);

				FloatZone zone = new FloatZone(v1.getRange().begin(), v2.getRange().end(), v1.getWeight());
				zone.addWeight(v2.getWeight());

				array.remove(v1);
				array.remove(v2);
				array.add(zone);
			} else {
				// 与它后面的合并
				FloatZone v1 = array.get(index);
				FloatZone v2 = array.get(index + 1);

				FloatZone zone = new FloatZone(v1.getRange().begin(), v2.getRange().end(), v1.getWeight());
				zone.addWeight(v2.getWeight());

				array.remove(v1);
				array.remove(v2);
				array.add(zone);
			}
		}
		java.util.Collections.sort(array);
	}

	/**
	 * 合并衔接的分布
	 */
	private void join() {		
		java.util.Collections.sort(array);
		for (int index = 0; index < array.size() - 1; index++) {
			if (index + 2 > array.size()) break;

			FloatZone v1 = array.get(index);
			FloatZone v2 = array.get(index + 1);
			FloatRange r1 = v1.getRange();
			FloatRange r2 = v2.getRange();
			if (r1.end() < r2.begin()) {
				continue; // 不关联，继续下一个比较
			}

			// 开始位置取最小值，结束位置取最大值
			float begin = (r1.begin() < r2.begin() ? r1.begin() : r2.end());
			float end = (r1.end() > r2.end() ? r1.end() : r2.end());

			FloatZone zone = new FloatZone(begin, end, v1.getWeight());
			zone.addWeight(v2.getWeight());

			array.remove(v1);
			array.remove(v2);
			array.add(zone);
			index = -1; // 从新开始比较
			java.util.Collections.sort(array);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexBalancer#balance(int, com.laxcus.access.index.slide.Slider)
	 */
	@Override
	public ColumnSector balance(int sites, Slider slider) {
		if (array.isEmpty()) {
			throw new ArrayIndexOutOfBoundsException("array < 1");
		} else if (sites < 1) {
			throw new IndexOutOfBoundsException("sites < 1");
		}

		// 将存在衔接的情况进行合并
		this.join();
		// 如果当前片段数量小于主机数，扩展片段到主机数
		if(array.size() < sites) {
			this.extend(sites);
		} else if(array.size() > sites) {
			// 如果当前片段数量大于主机数，缩小片段到主机数（允许不到主机数目）
			this.shrink(sites);
		}

		// 输出分片范围
		FloatSector sector = new FloatSector();
		// 设置对象定位器
		sector.setSlider(slider);
		// 保存索引范围
		for (FloatZone zone : array) {
			sector.add(zone.getRange());
		}
		return sector;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexBalancer#balance(int)
	 */
	@Override
	public ColumnSector balance(int sites) {
		return balance(sites, null);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexBalancer#add(com.laxcus.access.index.balance.IndexZone)
	 */
	@Override
	public boolean add(IndexZone zone) {
		if (zone.getClass() != FloatZone.class) {
			throw new ClassCastException("illegal class");
		}
		return array.add((FloatZone) zone);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexBalancer#add(com.laxcus.util.range.Range, int)
	 */
	@Override
	public boolean add(Range range, int weight) {
		if (range.getClass() != FloatRange.class) {
			throw new ClassCastException("illegal class");
		}
		FloatRange sub = (FloatRange) range;
		FloatZone zone = new FloatZone(sub.begin(), sub.end(), weight);
		return this.add(zone);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexBalancer#add(java.lang.Number, java.lang.Number, int)
	 */
	@Override
	public boolean add(Number begin, Number end, int weight) {
		if (begin.getClass() != java.lang.Float.class || end.getClass() != java.lang.Float.class) {
			throw new ClassCastException("illegal class");
		}		
		return add(new FloatZone(((Float) begin).floatValue(), ((Float) end).floatValue(), weight));
	}
}