/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.table;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.access.stub.sign.*;
import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 表数据一致性检测结果
 * 
 * @author scott.liang
 * @version 1.0 9/21/2015
 * @since laxcus 1.0
 */
public final class CheckEntityConsistencyProduct extends EchoProduct {

	private static final long serialVersionUID = -8330180032294229227L;

	/** 数据表名 **/
	private Space space;

	/** 被检查的数据块总数目 **/
	private long stubs;

	/** 检查正确/有效的数据块总数目 **/
	private long validates;
	
	/** 签名站点集合 **/
	private TreeSet<SignSite> array = new TreeSet<SignSite>();
	
	/**
	 * 生成表数据一致性检测结果数据副本
	 * @param that CheckEntityConsistencyProduct实例
	 */
	private CheckEntityConsistencyProduct(CheckEntityConsistencyProduct that) {
		super(that);
		space = that.space;
		stubs = that.stubs;
		validates = that.validates;
		array.addAll(that.array);
	}

	/**
	 * 构造默认的表数据一致性检测结果
	 */
	public CheckEntityConsistencyProduct() {
		super();
		stubs = validates = 0;
	}

	/**
	 * 从可类化数据读取器中解析表数据一致性检测结果
	 * @param reader 可类化数据读取器
	 */
	public CheckEntityConsistencyProduct(ClassReader reader) {
		super();
		resolve(reader);
	}
	
	/**
	 * 保存一个签名站点
	 * @param e 实例 
	 * @return 保存成功返回真，否则假
	 */
	public boolean addSite(SignSite e) {
		Laxkit.nullabled(e);
		return array.add(e);
	}
	
	/**
	 * 保存一组签名站点
	 * @param a 签名站点实例 
	 * @return 返回新增成员数目
	 */
	public int addSites(java.util.Collection<SignSite> a) {
		Laxkit.nullabled(a);
		int size = array.size();
		for (SignSite e : a) {
			addSite(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出签名站点实例 
	 * @return 实例列表
	 */
	public List<SignSite> getSites() {
		return new ArrayList<SignSite>(array);
	}
	
	/**
	 * 设置数据表名
	 * @param e Space实例
	 * @throws NullPointerException
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置数据块数目
	 * @param i 数据块数目
	 */
	public void setStubs(long i) {
		stubs = i;
	}

	/**
	 * 返回数据块数目 
	 * @return 数据块数目
	 */
	public long getStubs() {
		return stubs;
	}

	/**
	 * 设置一致性数据块数目
	 * @param i 一致性数据块数目
	 */
	public void setValidates(long i) {
		validates = i;
	}

	/**
	 * 返回一致性数据块数目
	 * @return 一致性数据块数目
	 */
	public long getValidates() {
		return validates;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CheckEntityConsistencyProduct duplicate() {
		return new CheckEntityConsistencyProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(space);
		writer.writeLong(stubs);
		writer.writeLong(validates);
		// 签名站点
		writer.writeInt(array.size());
		for (SignSite e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		space = new Space(reader);
		stubs = reader.readLong();
		validates = reader.readLong();
		// 签名站点
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SignSite e = new SignSite(reader);
			addSite(e);
		}
	}

}