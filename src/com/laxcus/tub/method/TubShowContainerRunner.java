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
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 显示边缘容器组件运行器
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public class TubShowContainerRunner extends TubCommandRunner {

	/**
	 * 构造显示边缘容器组件运行器，指定命令
	 * @param cmd 显示边缘容器组件
	 */
	public TubShowContainerRunner(TubShowContainer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.method.TubCommandRunner#getCommand()
	 */
	@Override
	public TubShowContainer getCommand() {
		return (TubShowContainer) super.getCommand();
	}
	
	/**
	 * 判断匹配
	 * @param tag
	 * @param names
	 * @return
	 */
	private boolean matchs(TubTag tag, Naming[] names) {
		for (Naming e : names) {
			if (Laxkit.compareTo(tag.getNaming(), e) == 0) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.tub.method.TubCommandRunner#launch()
	 */
	@Override
	public TubShowContainerProduct launch() {
		TubShowContainer cmd = getCommand();
		Naming[] names = cmd.getNamings();
		
		// 找到结果
		List<TubTag> tubs = TubPool.getInstance().getTags();
		
		TubShowContainerProduct product = new TubShowContainerProduct();
		for(TubTag e : tubs) {
			// 检查匹配
			boolean success = cmd.isAll();
			if (!success) {
				success = matchs(e, names);
			}
			if (!success) {
				continue;
			}
			
			TubContainerItem item = new TubContainerItem();
			item.setNaming(e.getNaming());
			item.setClassName(e.getClassName());
			item.setCaption(e.getCaption());
			product.add(item);
		}
		
		return product;
	}

}