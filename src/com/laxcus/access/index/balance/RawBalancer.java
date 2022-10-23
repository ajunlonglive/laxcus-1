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
 * 二进制数据流平衡分割器。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/22/2009
 * @since laxcus 1.0
 */
public final class RawBalancer extends VariableBalancer {

	/**
	 * 构造二进制数据流平衡分割器
	 */
	public RawBalancer() {
		super();
	}

	/**
	 * 构造二进制数据流平衡分割器，指定数据封装
	 * @param packing 数据封装实例
	 */
	public RawBalancer(Packing packing) {
		this();
		setPacking(packing);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.Bit64Balancer#getSector()
	 */
	@Override
	public Bit64Sector getSector() {
		RawSector e = new RawSector();
		e.setPacking(getPacking());
		return e;
	}

}