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
 * 大字符(UTF32编码)平衡分割器
 * 
 * @author scott.liang
 * @version 1.0 8/13/2009
 * @since laxcus 1.0
 */
public final class HCharBalancer extends WordBalancer {

	/**
	 * 构造大字符(UTF32)平衡分割器
	 */
	public HCharBalancer() {
		super();
	}

	/**
	 * 构造大字符平衡分割器，指定参数
	 * @param packing 数据封装
	 * @param sentient 大小写敏感
	 */
	public HCharBalancer(Packing packing, boolean sentient) {
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
		HCharSector sector = new HCharSector();
		sector.setPacking(getPacking());
		sector.setSentient(isSentient());
		return sector;
	}

}