/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.stub.site;

import java.util.*;

import com.laxcus.echo.product.*;
import com.laxcus.util.classable.*;

/**
 * 数据块编号从站点查询报告。
 * 
 * @author scott.liang
 * @version 1.1 5/17/2015
 * @since laxcus 1.0
 */
public class SlaveStubSiteProduct extends EchoProduct {

	private static final long serialVersionUID = -6502316330955814789L;

	/** 从站点集合 **/
	private Set<SlaveStubSite> sites = new TreeSet<SlaveStubSite>();
	
	/**
	 * 构造默认的数据块编号从站点查询报告
	 */
	public SlaveStubSiteProduct() {
		super();
	}

	/**
	 * 根据传入实例，生成浅层数据副本
	 * @param that
	 */
	private SlaveStubSiteProduct(SlaveStubSiteProduct that) {
		super(that);
		sites.addAll(that.sites);
	}
	
	/**
	 * 从可类化数据读取器中解析数据块编号从站点查询报告
	 * @param reader - 可类化数据读取器
	 * @since 1.1
	 */
	public SlaveStubSiteProduct(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 保存一个从站点
	 * @param e
	 */
	public void add(SlaveStubSite e) {
		sites.add(e);
	}

	/**
	 * 返回全部从站点
	 * @return
	 */
	public List<SlaveStubSite> list() {
		return new ArrayList<SlaveStubSite>(sites);
	}

	/**
	 * 返回从站点数目
	 * @return
	 */
	public int size() {
		return sites.size();
	}

	/**
	 * 判断是空集合
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#duplicate()
	 */
	@Override
	public SlaveStubSiteProduct duplicate() {
		return new SlaveStubSiteProduct(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.product.EchoProduct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(sites.size());
		for (SlaveStubSite e : sites) {
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
			SlaveStubSite e = new SlaveStubSite(reader);
			sites.add(e);
		}
	}

}
