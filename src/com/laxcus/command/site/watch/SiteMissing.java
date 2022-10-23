/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import com.laxcus.command.*;
import com.laxcus.util.classable.*;

import java.util.*;

import com.laxcus.util.*;

/**
 * 节点不足通知。<br>
 * 
 * TOP/HOME通知WATCH站点，某类节点不足，需要处理。
 * 
 * @author scott.liang
 * @version 1.0 6/8/2018
 * @since laxcus 1.0
 */
public class SiteMissing extends Command {

	private static final long serialVersionUID = 8864216247252498843L;

	/** 单元集合 **/
	private TreeSet<SiteMissingItem> array = new TreeSet<SiteMissingItem>();

	/**
	 * 从传入的节点不足通知，生成它的数据副本
	 * @param that SiteMissing实例
	 */
	private SiteMissing(SiteMissing that) {
		super(that);
		array.addAll(that.array);
	}

	/**
	 * 构造节点不足通知
	 */
	public SiteMissing() {
		super();
	}
	
	/**
	 * 从可类化数据读取器中解析节点不足通知参数
	 * @param reader 可类化数据读取器
	 */
	public SiteMissing(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 增加一个通知单元，不允许空指针
	 * @param e SiteMissingItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(SiteMissingItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一个通知单元
	 * @param siger 用户签名
	 * @param siteFamily 节点类型
	 * @return 成功返回真，否则假
	 */
	public boolean add(Siger siger, byte siteFamily) {
		SiteMissingItem e = new SiteMissingItem(siger, siteFamily);
		return add(e);
	}
	
	/**
	 * 保存一批通知单元数组
	 * @param a SiteMissingItem数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<SiteMissingItem> a) {
		int size = array.size();
		for (SiteMissingItem e : a) {
			add(e);
		}
		return array.size() - size;
	}
	
	/**
	 * 保存另一组节点不足通知
	 * @param e SiteMissing实例
	 * @return 返回新增成员数目
	 */
	public int addAll(SiteMissing e) {
		return addAll(e.array);
	}

	/**
	 * 输出全部通知单元
	 * @return SiteMissingItem列表
	 */
	public List<SiteMissingItem> list() {
		return new ArrayList<SiteMissingItem>(array);
	}
	
	/**
	 * 统计通知单元数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * 
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SiteMissing duplicate() {
		return new SiteMissing(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (SiteMissingItem item : array) {
			writer.writeObject(item);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			SiteMissingItem item = new SiteMissingItem(reader);
			array.add(item);
		}
	}

}