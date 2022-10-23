/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.bank;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * BANK站点资源。<br>
 * 记录一个BANK集群的全部元数据。是一组用户账号下的资源配置。
 * 
 * @author scott.liang
 * @version 1.0 06/25/2018
 * @since laxcus 1.0
 */
public final class BankSite extends Site {

	private static final long serialVersionUID = 3174237320062236760L;

	/** 管理站点(只能有一个) **/
	private boolean manager;

	/**
	 * 将BANK站点属性写入可类化写入器
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 运行状态
		writer.writeBoolean(manager);
	}

	/**
	 * 从可类化读取器中解析BANK站点属性信息
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		// 运行状态
		manager = reader.readBoolean();
	}

	/**
	 * 根据传入的BANK站点实例，生成它的副本
	 * @param that BankSite实例
	 */
	private BankSite(BankSite that) {
		super(that);
		manager = that.manager;
	}

	/**
	 * 构造一个默认的BANK站点
	 */
	public BankSite() {
		super(SiteTag.BANK_SITE);
		manager = false;
	}

	/**
	 * 从可类化读取器中解析BANK站点地址
	 * @param reader 可类化读取器
	 */
	public BankSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置管理站点标记
	 * @param b 管理站点标记
	 */
	public void setManager(boolean b) {
		manager = b;
	}

	/**
	 * 检查是管理站点（否则即是监视站点）
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
	public BankSite duplicate() {
		return new BankSite(this);
	}

}