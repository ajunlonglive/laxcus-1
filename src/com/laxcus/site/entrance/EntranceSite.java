/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.entrance;

import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * ENTRANCE站点。站点信息注册到BANK站点
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class EntranceSite extends GatewaySite {

	private static final long serialVersionUID = 3814742936691423326L;

	/**
	 * 根据传入的ENTRANCE站点实例，生成它的数据副本
	 * @param that EntranceSite实例
	 */
	private EntranceSite(EntranceSite that) {
		super(that);
	}

	/**
	 * 构造一个默认的ENTRANCE站点地址
	 */
	public EntranceSite() {
		super(SiteTag.ENTRANCE_SITE);
	}

	/**
	 * 从可类化读取器中解析ENTRANCE站点地址
	 * @param reader 可类化数据读取器
	 */
	public EntranceSite(ClassReader reader) {
		this();
		resolve(reader);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#duplicate()
	 */
	@Override
	public EntranceSite duplicate() {
		return new EntranceSite(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		super.buildSuffix(writer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.site.Site#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		super.resolveSuffix(reader);
	}

}