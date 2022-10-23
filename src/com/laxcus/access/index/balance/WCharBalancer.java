/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.balance;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.index.section.*;

/**
 * 宽字符(UTF16编码)平衡分割器
 * 
 * @author scott.liang
 * @version 1.0 8/12/2009
 * @since laxcus 1.0
 */
public final class WCharBalancer extends WordBalancer {

	/**
	 * 构造宽字符(UTF16)平衡分割器
	 */
	public WCharBalancer() {
		super();
	}

	/**
	 * 构造宽字符平衡分割器，指定参数
	 * @param packing 数据封装
	 * @param sentient 大小写敏感
	 */
	public WCharBalancer(Packing packing, boolean sentient) {
		this();
		setPacking(packing);
		setSentient(sentient);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.Bit64Balancer#getSector()
	 */
	@Override
	public Bit64Sector getSector() {
		WCharSector sector = new WCharSector();
		sector.setPacking(getPacking());
		sector.setSentient(isSentient());
		return sector;
	}

}
