/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.cyber;

import java.io.*;
import java.util.*;

import com.laxcus.distribute.calculate.mid.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 网络数据矩阵。<br>
 * 
 * DIFFUSE/CONVERGER计算过程中，一次FROM/TO阶段完成后，形成以“模->CyberArea”关系映像的数据集合。<br><br>
 * 
 * 过程: <br>
 * 1. CALL节点收集DATA/WORK节点返回的FluxArea信息<br>
 * 2. 将FluxArea中的信息拆解为FluxField<br>
 * 3. 以FluxField中的模为键，CyberField为值，形成这个信息集合<br>
 * 
 * @author scott.liang
 * @version 1.1 03/18/2015
 * @since laxcus 1.0
 */
public final class CyberMatrix implements Serializable, Cloneable, Classable {

	private static final long serialVersionUID = 3638410133496151568L;

	/** 模(mod，唯一性) -> 数据分布存储信息(多个节点同一模值的信息)  **/ 
	private TreeMap<Long, CyberArea> areas = new TreeMap<Long, CyberArea>();

	/**
	 * 将网络数据矩阵写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 写入记录数目
		writer.writeInt(areas.size());
		// 写入每一记录
		Iterator<Map.Entry<Long, CyberArea>> iterators = areas.entrySet().iterator();
		while (iterators.hasNext()) {
			Map.Entry<Long, CyberArea> entry = iterators.next();
			writer.writeLong(entry.getKey());
			writer.writeObject(entry.getValue());
		}
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析网络数据矩阵
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 记录数目
		int size = reader.readInt();
		// 读取每一个记录并且保存
		for (int i = 0; i < size; i++) {
			long mod = reader.readLong();
			CyberArea value = new CyberArea(reader);
			areas.put(mod, value);
		}
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造网络数据矩阵
	 */
	public CyberMatrix() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析网络数据矩阵
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CyberMatrix(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 构造网络数据矩阵，同时保存一个数据磁盘区域
	 * @param area FluxArea实例
	 */
	public CyberMatrix(FluxArea area) {
		this();
		add(area);
	}

	/**
	 * 构造网络数据矩阵，同时保存一批数据磁盘区域
	 * @param array FluxArea列表
	 */
	public CyberMatrix(List<FluxArea> array) {
		this();
		add(array);
	}

	/**
	 * 构造网络数据矩阵，同时保存一批数据磁盘区域
	 * @param array FluxArea数组
	 */
	public CyberMatrix(FluxArea[] array) {
		this();
		add(array);
	}

	/**
	 * 拆解FluxArea将FluxField按模值保存，返回保存的FluxField数量
	 * @param area FluxArea实例
	 * @return 返回FluxField数量
	 */
	public int add(FluxArea area) {
		int count = 0;
		for (FluxField field : area.list()) {
			long mod = field.getMod();
			CyberArea that = areas.get(mod);
			if (that == null) {
				that = new CyberArea(mod);
				areas.put(mod, that);
			}
			// 保存参数
			long taskId = area.getTaskId();
			Node site = area.getSource();
			boolean success = that.add(site, taskId, field);
			if (success) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 拆解一批FluxArea，将FluxField按模值保存，返回保存的FluxField数量
	 * @param areas FluxArea数组
	 * @return 保存的FluxField数量
	 */
	public int add(FluxArea[] areas) {
		int count = 0;
		for(int i = 0; areas != null && i < areas.length; i++) {
			count += add(areas[i]);
		}
		return count;
	}

	/**
	 * 拆解一组FluxArea，将FluxField按模值保存，返回保存的FluxField数量
	 * @param areas FluxArea列表
	 * @return 保存的FluxField数量
	 */
	public int add(List<FluxArea> areas) {
		int count = 0;
		for (FluxArea area : areas) {
			count += add(area);
		}
		return count;
	}

	/**
	 * 返回模的键值集合
	 * @return 长整型集合
	 */
	public Set<Long> keys() {
		return new TreeSet<Long>(areas.keySet());
	}

	/**
	 * 根据模值，查找对应的网络数据区
	 * @param mod 模值
	 * @return CyberArea实例
	 */
	public CyberArea find(long mod) {
		return areas.get(mod);
	}

	/**
	 * 判断集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 返回集合的成员数目
	 * @return 成员数目
	 */
	public int size() {
		return areas.size();
	}

	/**
	 * 统计各存储节点(data、work)上的数据总长度
	 * 
	 * @return 数据总长度
	 */
	public long length() {
		long len = 0L;
		for (CyberArea area : areas.values()) {
			len += area.length();
		}
		return len;
	}

	/**
	 * 根据节点数和模值为依据进行分割/合并<br>
	 * 算法：<br>
	 * <1> 如果节点数目大于模值总量，返回模值总量的数据片数组<br>
	 * <2> 如果节点数目小于模值总量，返回节点数量的数据片数组，这时的数量片以相邻模值进行了合并。<br><br>
	 * 
	 * 不会发生分配后的结果，数据片数组大于节点数目的可能。即 sites>spheres[].size 不会出现<br>
	 * 模值(mod)的隐性含义: 相邻的模值，它们对应的实际数据也是相邻的。这一点是分片的基本规则。<br><br>
	 * 
	 * 自定义的balance算法也要遵循此规定。<br>
	 * 
	 * @param sites 站点数目
	 * @return 返回CyberSphere数组
	 */
	public CyberSphere[] balance(final int sites) {
		if (sites < 1) {
			throw new IllegalValueException("invalid sites:%d", sites);
		}

		// 如果网络节点数大于分片数，以分片数量为准
		if (sites >= areas.size()) {
			CyberSphere[] array = new CyberSphere[areas.size()];
			int index = 0;
			for (CyberArea area : areas.values()) {
				array[index] = new CyberSphere();
				array[index].add(area);
				index++;
			}
			return array;
		}

		// 否则，将以节点数量为准进行收缩
		ArrayList<Integer> array = new ArrayList<Integer>(sites);
		int count = 0;
		// 收缩后，每一组分配的数量。值保存到数组
		int scale = areas.size() / sites;
		if (areas.size() % sites != 0) {
			scale++;
		}

		while (array.size() < sites) {
			// 默认每组分配量
			int number = scale;
			// 预分配后，还剩下的数量
			int left = areas.size() - (count + number);
			// 剩余量不足填满时，值减1。"sites - (a.size() + 1)"， 是添填后的剩余量
			if (left < sites - (array.size() + 1)) {
				number--;
			}
			// 记录实际分配量
			array.add(number);
			count += number;
		}

		// 取相邻的点合并
		CyberSphere[] spheres = new CyberSphere[array.size()];
		ArrayList<Long> mods = new ArrayList<Long>(areas.keySet());
		int index = 0;
		for (int number : array) {
			// 每次取几个模值
			for (int i = 0; i < number; i++) {
				long mod = mods.remove(0);
				CyberArea area = areas.get(mod);
				if (spheres[index] == null) {
					spheres[index] = new CyberSphere();
				}
				spheres[index].add(area);
			}
			index++;
		}

		return spheres;
	}

}