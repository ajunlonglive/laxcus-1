/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.mid;

import java.util.*;

import com.laxcus.access.stub.index.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * ESTABLISH.SIFT阶段数据构建区。<br>
 * SIFT数据构建区由BUILD.SIFT阶段的任务实例产生。提交给CALL.ASSIGN分析和处理。<br>
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public final class SiftArea extends EstablishArea { 

	private static final long serialVersionUID = -398063958953412566L;

	/** SIFT阶段处理结果集合 **/
	private Map<EstablishFlag, SiftField> fields = new TreeMap<EstablishFlag, SiftField>();

	/**
	 * 将SIFT阶段数据构建区写入可类化存储器
	 * @see com.laxcus.distribute.establish.mid.EstablishArea#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 上级数据
		super.buildSuffix(writer);
		// 当前数据
		writer.writeInt(fields.size());
		for (SiftField e : fields.values()) {
			writer.writeObject(e);
		}
	}

	/**
	 * 从可类化读取器中解析SIFT数据构建区参数
	 * @see com.laxcus.distribute.establish.mid.EstablishArea#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 上级数据
		super.resolveSuffix(reader);
		// 当前数据
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SiftField e = new SiftField(reader);
			fields.put(e.getFlag(), e);
		}
	}

	/**
	 * 构造默认的SIFT阶段数据构建区
	 */
	public SiftArea() {
		super();
	}

	/**
	 * 根据传入的SIFT阶段数据构建区，生成它的数据副本
	 * @param that - SiftArea实例
	 */
	private SiftArea(SiftArea that) {
		this();
		fields.putAll(that.fields);
	}

	/**
	 * 构造SIFT阶段数据构建区，指定源主机地址
	 * @param source - 源主机（BUILD节点主机）
	 */
	public SiftArea(Node source) {
		this();
		setSource(source);
	}

	/**
	 * 从可类化读取器中解析SIFT阶段数据构建区
	 * @param reader 可类化读取器
	 * @since 1.1
	 */
	public SiftArea(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个表下的数据块
	 * @param flag - 扫描标识
	 * @param item - 数据块属性
	 * @return - 成功返回真，否则假
	 */
	public boolean add(EstablishFlag flag, StubItem item) {
		SiftField field = fields.get(flag);
		if (field == null) {
			field = new SiftField(flag);
			fields.put(field.getFlag(), field);
		}
		return field.addStubItem(item);
	}

	/**
	 * 保存一个表下的全部数据扫描域。如果已经存在，旧的将被清除。
	 * @param field - 扫描区域
	 * @return - 成功返回true，失败返回false
	 */
	public boolean add(SiftField field) {
		EstablishFlag flag = field.getFlag();
		return fields.put(flag, field) == null;
	}

	/**
	 * 输出全部
	 * @return SiftField列表
	 */
	public List<SiftField> list() {
		return new ArrayList<SiftField>(fields.values());
	}

	/**
	 * 返回KEY值
	 * @return EstablishFlag列表
	 */
	public List<EstablishFlag> getKeys() {
		return new ArrayList<EstablishFlag>(fields.keySet());
	}

	/**
	 * 统计表空间扫描集合长度
	 * @return 表空间扫描集合长度
	 */
	public int size() {
		return fields.size();
	}

	/**
	 * 判断是否空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#duplicate()
	 */
	@Override
	public SiftArea duplicate() {
		return new SiftArea(this);
	}

}