/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.establish.mid;

import java.util.*;

import com.laxcus.util.classable.*;

/**
 * RISE阶段删除数据块域 <br><br>
 * 
 * RISE阶段删除数据块域由CALL.ASSIGN阶段分配，被DATA.RISE执行，它的目标是DATA站点,随RiseSession发送到DATA站点执行。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public class RiseOldHead extends EstablishZone  {

	private static final long serialVersionUID = -1841614947538346152L;

	/** 数据块编号 **/
	private TreeSet<Long> stubs = new TreeSet<Long>();

	/**
	 * 根据传入实例，生成一个RISE阶段请求单元浅层数据副本
	 * @param that RiseOldHead实例
	 */
	private RiseOldHead(RiseOldHead that) {
		super(that);
		stubs.addAll(that.stubs);
	}

	/**
	 * 构造默认的RISE阶段请求单元
	 */
	public RiseOldHead() {
		super();
	}

	/**
	 * 构造RISE阶段请求单元，指定数据构建标识
	 * @param flag 数据构建标识
	 */
	public RiseOldHead(EstablishFlag flag) {
		this();
		setFlag(flag);
	}

	/**
	 * 从可类化数据读取器中解析和构造RISE阶段请求单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RiseOldHead(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存被删除的数据块编号
	 * @param stub 数据块编号
	 * @return 保存成功返回真，否则假
	 */
	public boolean addStub(Long stub) {
		return stubs.add(stub);
	}
	
	/**
	 * 判断数据块编号存在
	 * @param stub 数据块编号
	 * @return 返回真或者假
	 */
	public boolean hasStub(Long stub) {
		return stubs.contains(stub);
	}

	/**
	 * 保存一组被删除的数据块编号
	 * @param a 数据块编号数组
	 * @return 返回删除的成员数目
	 */
	public int addStubs(Collection<Long> a) {
		int size = stubs.size();
		for (Long stub : a) {
			addStub(stub);
		}
		return stubs.size() - size;
	}

	/**
	 * 输出被删除的数据块编号
	 * @return 数据块编号列表
	 */
	public List<Long> getStubs() {
		return new ArrayList<Long>(stubs);
	}

	/**
	 * 统计数据块编号
	 * @return 数据块编号数目
	 */
	public int getStubCount() {
		return stubs.size();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.establish.mid.EstablishStubField#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
		// 要求删除的数据块
		writer.writeInt(stubs.size());
		for (Long e : stubs) {
			writer.writeLong(e.longValue());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.establish.mid.EstablishStubField#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
		// 被删除的数据块
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			stubs.add(stub);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.distribute.mid.MiddleZone#duplicate()
	 */
	@Override
	public RiseOldHead duplicate() {
		return new RiseOldHead(this);
	}
}