/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.top;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * TOP站点配置
 * 
 * @author scott.liang
 * @version 1.1 05/02/2015
 * @since laxcus 1.0
 */
public final class TopSite extends Site {

	private static final long serialVersionUID = -2578051885362780993L;

	/** 管理站点(只能有一个) **/
	private boolean manager;

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeBoolean(manager);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		manager = reader.readBoolean();
	}

	/**
	 * 根据传入的TOP站点实例，生成它的数据副本。
	 * @param that TOP站点实例
	 */
	private TopSite(TopSite that) {
		super(that);
		manager = that.manager;
	}

	/**
	 * 构造默认的TOP站点
	 */
	public TopSite() {
		super(SiteTag.TOP_SITE);
		manager = false;
	}

	/**
	 * 从可类化数据读取器中解析TOP站点参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public TopSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置管理站点
	 * @param b 管理站点标记
	 */
	public void setManager(boolean b) {
		manager = b;
	}

	/**
	 * 判断是管理站点
	 * @return 返回真或者假
	 */
	public boolean isManager() {
		return manager;
	}

	/**
	 * 判断是监视站点
	 * @return 返回真或者假
	 */
	public boolean isMonitor() {
		return !isManager();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public TopSite duplicate() {
		return new TopSite(this);
	}

}