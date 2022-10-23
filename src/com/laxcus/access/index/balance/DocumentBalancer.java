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
 * 文档数据平衡分割器。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/22/2009
 * @since laxcus 1.0
 */
public final class DocumentBalancer extends VariableBalancer {

	/**
	 * 构造文档数据平衡分割器
	 */
	public DocumentBalancer() {
		super();
	}

	/**
	 * 构造文档数据平衡分割器，指定数据封装
	 * @param packing 数据封装
	 */
	public DocumentBalancer(Packing packing) {
		this();
		setPacking(packing);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.balance.Bit64Balancer#getSector()
	 */
	@Override
	public Bit64Sector getSector() {
		DocumentSector sector = new DocumentSector();
		sector.setPacking(getPacking());
		return sector;
	}

}