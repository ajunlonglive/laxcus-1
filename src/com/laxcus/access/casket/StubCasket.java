/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.casket;

import com.laxcus.util.classable.*;

/**
 * 数据块封装包。<br>
 * 
 * 指定用户要操作的数据块 
 * 
 * @author scott.liang
 * @version 1.0 12/08/2013
 * @since laxcus 1.0
 */
public class StubCasket extends AccessCasket {

	/** 数据块编号 **/
	private long stub;

	/**
	 * 构造数据块编号的封装命令
	 */
	protected StubCasket() {
		super();
	}

	/**
	 * 设置数据块编号
	 * @param id 数据块编号
	 */
	public void setStub(long id) {
		stub = id;
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public long getStub() {
		return stub;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.act.PutAct#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		// 数据块编号
		writer.writeLong(stub);
	}

}