/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cross;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.law.cross.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 共享资源单元报告
 * 
 * @author scott.liang
 * @version 1.0 7/2/2017
 * @since laxcus 1.0
 */
public class ShareCrossProduct extends EchoProduct {

	private static final long serialVersionUID = -4906217393457618337L;

	/** 受理单元 **/
	private TreeSet<ShareCrossItem> array = new TreeSet<ShareCrossItem>();

	/**
	 * 构造默认的共享资源单元报告
	 */
	public ShareCrossProduct() {
		super();
	}

	/**
	 * 从可类化数据读取器中解析共享资源处理结果
	 * @param reader 可类化数据读取器
	 */
	public ShareCrossProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成共享资源单元报告的数据副本
	 * @param that 共享资源单元报告实例
	 */
	private ShareCrossProduct(ShareCrossProduct that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 保存一个单元
	 * @param e 共享资源单元实例
	 * @return 返回真或者假
	 */
	public boolean add(ShareCrossItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存单元
	 * @param siger 授权人/被授权人签名
	 * @param flag 共享资源标识
	 * @return 返回真或者假
	 */
	public boolean add(Siger siger, CrossFlag flag) {
		return add(new ShareCrossItem(siger, flag));
	}

	/**
	 * 保存一批单元
	 * @param a 共享资源单元数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<ShareCrossItem> a) {
		int size = array.size();
		array.addAll(a);
		return array.size() - size;
	}

	/**
	 * 保存一批单元
	 * @param a 共享资源单元报告
	 * @return 返回新增成员数目
	 */
	public int addAll(ShareCrossProduct a) {
		return addAll(a.array);
	}

	/**
	 * 输出全部共享资源单元
	 * @return 共享资源单元列表
	 */
	public List<ShareCrossItem> list() {
		return new ArrayList<ShareCrossItem>(array);
	}

	/**
	 * 统计成员数
	 * @return 共享资源单元数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return  返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public ShareCrossProduct duplicate() {
		return new ShareCrossProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (ShareCrossItem e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			ShareCrossItem e = new ShareCrossItem(reader);
			array.add(e);
		}
	}

}