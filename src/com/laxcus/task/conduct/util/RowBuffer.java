/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.conduct.util;

import java.util.*;

import com.laxcus.access.row.*;
import com.laxcus.access.schema.*;
import com.laxcus.access.type.*;
import com.laxcus.util.classable.*;

/**
 * 数据分片后，存储所在区域的行记录
 * 
 * @author scott.liang
 * @version 1.0 11/12/2009
 * @since laxcus 1.0
 */
public final class RowBuffer {

	/** 分片模值 **/
	private long mod;

	/** 对应的数据表 */
	private Space space;
	
	/** 数据块标识号，在build中生成 **/
	private MassFlag flag;

	/** 记录集合 */
	private List<Row> array = new ArrayList<Row>();

	/**
	 * 构造一个行记录存储集合，同时指定模和表名
	 * @param mod - 模值
	 * @param space - 表名
	 */
	public RowBuffer(long mod, Space space) {
		super();
		this.setMod(mod);
		this.setSpace(space);
	}

	/**
	 * 定义模值
	 * @param i 模值
	 */
	public void setMod(long i) {
		this.mod = i;
	}

	/**
	 * 返回模值
	 * @return 模值
	 */
	public long getMod() {
		return this.mod;
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return this.space;
	}

	/**
	 * 保存一行记录
	 * @param row 行记录
	 * @return 返回真或者假
	 */
	public boolean add(Row row) {
		return array.add(row);
	}

	/**
	 * 保存一组记录
	 * @param a 一组记录
	 * @return 新增成员数目
	 */
	public int addAll(Collection<Row> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}

	/**
	 * 返回行记录集合
	 * @return Row列表
	 */
	public List<Row> list() {
		return this.array;
	}

	/**
	 * 返回指定下标的行
	 * @param index
	 * @return Row实例
	 */
	public Row get(int index) {
		return this.array.get(index);
	}

	/**
	 * 统计记录数目
	 * @return 行数
	 */
	public int size() {
		return this.array.size();
	}
	
	/**
	 * 返回数据块标识号
	 * @return MassFlag实例
	 */
	public MassFlag getFlag() {
		return this.flag;
	}
	
	/**
	 * 产生数据流(确定是行存储模式)
	 * @return 字节数组
	 */
	public byte[] build() {
		// 数据头部信息(输出的数据流，确定是行存储模式)
		flag = new MassFlag();
		flag.setMod(this.mod);
		flag.setRows(array.size());
		flag.setColumns((short) array.get(0).size());
		flag.setModel(StorageModel.NSM);
		flag.setSpace(this.space);
		byte[] head = flag.build();

		// 确定分配所需空间
		int total = head.length;
		for(Row row : this.array) {
			total += row.capacity();
		}

		// 开辟内存空间
		ClassWriter buff = new ClassWriter(total - (total % 32) + 32);
		// 保存头记录
		buff.write(head, 0, head.length);
		// 保存行记录
		for (Row row : array) {
			row.buildX(buff);
		}
		
		// 输出数据流
		byte[] data = buff.effuse();
		// 重装定义数据流长度
		flag.setLength(data.length - head.length);
		// 更新数据头部信息
		head = flag.build();
		// 输出到磁盘开始位置
		System.arraycopy(head, 0, data, 0, head.length);

		return data;
	}
}