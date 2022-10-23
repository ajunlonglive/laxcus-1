/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.balance;

import java.util.*;

import com.laxcus.access.index.section.*;
import com.laxcus.access.index.slide.*;
import com.laxcus.access.index.zone.*;
import com.laxcus.util.range.*;

/**
 * 短整型平衡分割器。<br>
 * 整型索引分布范围（可以衔接，但是不能重叠，发生重叠情况将合并) <br>
 * 
 * @author scott.liang
 * @version 1.0 8/13/2009
 * @since laxcus 1.0
 */
public final class ShortBalancer extends ColumnBalancer {

	/** 一组可以衔接，但是不能重叠的整型值集合  **/
	private ArrayList<ShortZone> array = new ArrayList<ShortZone>();

	/**
	 * 构造短整型平衡分割器
	 */
	public ShortBalancer() {
		super();
	}

	/**
	 * 返回短整型分布区域列表
	 * @return ShortZone列表
	 */
	public List<ShortZone> list() {
		return new ArrayList<ShortZone>(array);
	}

	/**
	 * 判断短整型平衡分割器的成员数目
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
	 * 扩大分布范围数量：逐次找到统计频率最大分布，分成两部分，再保存!
	 * @param sites
	 */
	private void extend(int sites) {
		while(array.size() < sites) {
			java.util.Collections.sort(array);
			// 找到频率值最大的分片，分割成两部分
			int index = -1, weight = -1;
			for(int i = 0; i < array.size(); i++) {
				ShortZone zone = array.get(i);
				if(zone.getWeight() > weight) {
					index = i;
					weight = zone.getWeight();
				}
			}
			ShortZone zone = array.get(index); 
			ShortRange[] ranges = zone.getRange().split(2);
			if (ranges.length < 2) break; // 最小范围，退出
			int middle = zone.getWeight() / 2;

			// 删除旧的
			array.remove(index);
			// 增加分隔的新值
			add(new Short(ranges[0].begin()), new Short(ranges[0].end()), middle);
			add(new Short(ranges[1].begin()), new Short(ranges[1].end()), zone.getWeight() - middle);
		}
		java.util.Collections.sort(array);
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
				ShortZone volumn = array.get(i);
				if (i == 0 || volumn.getWeight() < count) {
					index = i;
					count = volumn.getWeight();
				}
			}

			if (index == 0) {
				// 与它之后的合并
				ShortZone v1 = array.get(0);
				ShortZone v2 = array.get(1);

				ShortZone zone = new ShortZone(v1.getRange().begin(), v2.getRange().end(), v1.getWeight());
				zone.addWeight(v2.getWeight());

				array.remove(v1);
				array.remove(v2);
				array.add(zone);
			} else if (index + 1 == array.size()) {
				// 与它前面的合并
				ShortZone v1 = array.get(index - 1);
				ShortZone v2 = array.get(index);

				ShortZone zone = new ShortZone(v1.getRange().begin(), v2.getRange().end(), v1.getWeight());
				zone.addWeight(v2.getWeight());

				array.remove(v1);
				array.remove(v2);
				array.add(zone);
			} else {
				// 与它后面的合并
				ShortZone v1 = array.get(index);
				ShortZone v2 = array.get(index + 1);

				ShortZone zone = new ShortZone(v1.getRange().begin(), v2.getRange().end(), v1.getWeight());
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

			ShortZone v1 = array.get(index);
			ShortZone v2 = array.get(index + 1);
			ShortRange r1 = v1.getRange();
			ShortRange r2 = v2.getRange();
			if (r1.end() < r2.begin()) {
				continue; // 不关联，继续下一个比较
			}

			// 开始位置取最小值，结束位置取最大值
			short begin = (r1.begin() < r2.begin() ? r1.begin() : r2.end());
			short end = (r1.end() > r2.end() ? r1.end() : r2.end());

			ShortZone zone = new ShortZone(begin, end, v1.getWeight());
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
		join();
		// 如果当前片段数量小于主机数，扩展片段到主机数
		if(array.size() < sites) {
			extend(sites);
		} else if(array.size() > sites) {
			// 如果当前片段数量大于主机数，缩小片段到主机数（允许不到主机数目）
			shrink(sites);
		}

		// 输出分片范围
		ShortSector sector = new ShortSector();
		// 设置对象定位器，为分区做准备
		sector.setSlider(slider);
		// 保存数据范围
		for (ShortZone zone : array) {
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
		if (zone.getClass() != ShortZone.class) {
			throw new ClassCastException("illegal class");
		}
		return array.add((ShortZone) zone);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexBalancer#add(com.laxcus.util.range.Range, int)
	 */
	@Override
	public boolean add(Range range, int weight) {
		if (range.getClass() != ShortRange.class) {
			throw new ClassCastException("illegal class");
		}
		ShortRange sub = (ShortRange) range;
		ShortZone zone = new ShortZone(sub.begin(), sub.end(), weight);
		return add(zone);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.index.balance.IndexBalancer#add(java.lang.Number, java.lang.Number, int)
	 */
	@Override
	public boolean add(Number begin, Number end, int weight) {
		if (begin.getClass() != java.lang.Short.class || end.getClass() != java.lang.Short.class) {
			throw new ClassCastException("illegal class");
		}		
		return add(new ShortZone(((Short) begin).shortValue(), ((Short) end).shortValue(), weight));
	}
}