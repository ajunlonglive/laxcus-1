/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.mid;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 站点数据存取区 <br><br>
 * 
 * 站点数据存取区在分布处理过程中产生，每个站点只能产生一个。
 * SiteArea提供一个站点地址，描述它存在的唯一性。
 * 
 * @author scott.liang
 * @version 1.1 4/23/2015
 * @since laxcus 1.0
 */
public abstract class SiteArea extends MiddleZone { 

	private static final long serialVersionUID = -4200561508409685510L;

	/** 元数据站点地址(DATA/WORK/BUILD) **/
	private Node node;

	/**
	 * 根据传入参数生成站点数据存取区浅层数据副本
	 * @param that SiteArea实例
	 */
	protected SiteArea(SiteArea that) {
		super(that);
		node = that.node.duplicate();
	}

	/**
	 * 构造默认的站点数据存取区
	 */
	protected SiteArea() {
		super();
	}

	/**
	 * 设置数据源头地址
	 * @param e Node实例
	 */
	public void setSource(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回数据源头地址
	 * @return Node实例
	 */
	public Node getSource() {
		return node;
	}

	/**
	 * 向可类化存储器写入子类数据信息
	 * @param writer 可类化存储器
	 */
	protected void buildSuffix(ClassWriter writer) {
		// 当前站点地址
		writer.writeObject(node);
	}

	/**
	 * 从可类化读取器中读出子类数据信息
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		// 当前站点地址
		node = new Node(reader);
	}	

}