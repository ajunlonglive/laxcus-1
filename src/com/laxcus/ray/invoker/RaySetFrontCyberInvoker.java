/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import java.util.*;

import com.laxcus.command.cyber.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 设置成员虚拟空间调用器
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public class RaySetFrontCyberInvoker extends RayInvoker {

	/**
	 * 构造设置成员虚拟空间，指定命令
	 * @param cmd 设置成员虚拟空间
	 */
	public RaySetFrontCyberInvoker(SetFrontCyber cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetFrontCyber getCommand() {
		return (SetFrontCyber) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 投递到TOP/HOME/BANK节点
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		VirtualCyberProduct product = null; 
		int index = findEchoKey(0);
		try {
			if(isSuccessObjectable(index)) {
				product = getObject(VirtualCyberProduct.class, index);
			}
		} catch(VisitException e){
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			print(product.list());
		} else {
			printFault();
		}

		return useful(success);
	}

	/**
	 * 显示反馈结果
	 * @param a
	 */
	private void print(List<VirtualCyberItem> a) {
		// 显示处理结果
		printRuntime();
		// 显示标题
		createShowTitle(new String[] { "VIRTUAL-CYBER/STATUS", "VIRTUAL-CYBER/SITE" });
		// 处理单元
		for (VirtualCyberItem e : a) {
			Object[] result = new Object[] { e.isSuccessful(), e.getSite() };
			printRow(result);
		}
		// 输出全部记录
		flushTable();
	}
}