/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.method;

import java.util.*;

import com.laxcus.tub.command.*;
import com.laxcus.tub.product.*;
import com.laxcus.tub.servlet.*;
import com.laxcus.tub.servlet.pool.*;
import com.laxcus.util.naming.*;

/**
 * 检查边缘监听运行器
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public class TubPrintServiceRunner extends TubCommandRunner {

	/**
	 * 构造检查边缘监听运行器，指定命令
	 * @param cmd 检查边缘监听
	 */
	public TubPrintServiceRunner(TubPrintService cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.method.TubCommandRunner#getCommand()
	 */
	@Override
	public TubPrintService getCommand() {
		return (TubPrintService) super.getCommand();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.method.TubCommandRunner#launch()
	 */
	@Override
	public TubPrintServiceProduct launch() {
		TubPrintService cmd = this.getCommand();
		Naming[] names = cmd.getNamings();

		// 找到结果
		List<TubToken> tubs = TubPool.getInstance().findTubs(names);
		
		TubPrintServiceProduct product = new TubPrintServiceProduct();
		for(TubToken e : tubs) {
			TubServiceItem item = new TubServiceItem();
			item.setHost(e.getHost());
			item.setId( e.getId() );
			item.setNaming(e.getNaming());
			item.setRunTime(e.getRunTime());
			product.add(item);
		}
		
		return product;
	}

}