/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 目标节点结果
 * 
 * @author scott.liang
 * @version 1.0 3/21/2022
 * @since laxcus 1.0
 */
public class CheckRemoteSiteProduct extends EchoProduct {

	private static final long serialVersionUID = -6689080200290768165L;
	
	/** 数组 **/
	private ArrayList<CheckRemoteSiteItem> array = new ArrayList<CheckRemoteSiteItem>();
	
	/**
	 * 构造默认的目标节点
	 */
	public CheckRemoteSiteProduct() {
		super();
	}
	
	/**
	 * 生成目标节点副本
	 * @param that
	 */
	private CheckRemoteSiteProduct(CheckRemoteSiteProduct that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 从可类化读取器中解析目标节点
	 * @param reader 可类化读取器
	 */
	public CheckRemoteSiteProduct(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 增加一个单元
	 * @param e 单元实例
	 */
	public void add(CheckRemoteSiteItem e) {
		Laxkit.nullabled(e);
		if (!array.contains(e)) {
			array.add(e);
		}
	}

	/**
	 * 输出全部单元
	 * @return
	 */
	public List<CheckRemoteSiteItem> list() {
		return new ArrayList<CheckRemoteSiteItem>(array);
	}

	/**
	 * 全部成员数
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public CheckRemoteSiteProduct duplicate() {
		return new CheckRemoteSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(array.size());
		for (CheckRemoteSiteItem e : array) {
			writer.writeObject(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			CheckRemoteSiteItem e = new CheckRemoteSiteItem(reader);
			array.add(e);
		}
	}

}
